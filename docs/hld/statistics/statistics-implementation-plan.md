# Statistics System - Implementation Plan

## Overview

This plan covers the statistics framework implementation across two plugins:

1. **Statistics Framework** (McCore) - Generic, fully-typed statistic tracking for any McCore plugin
2. **McRPG Statistics Integration** - McRPG registers and increments game-specific statistics during gameplay

The achievements system (Runic Achievements) is a follow-up. Design docs for that are preserved in:
- `McCore/docs/hld/achievements/` - Core RA design + StatisticRepository pattern
- `McRPG/docs/hld/achievements/` - McRPG-specific integration design

---

## Part 1: Statistics Framework (McCore)

### 1.1 Core Abstractions

#### `Statistic` (Interface)
The definition/template for a trackable statistic. Registered once globally, keyed by `NamespacedKey`.

```
com.diamonddagger590.mccore.statistic.Statistic
├── getStatisticKey(): NamespacedKey          // unique identifier (eg. "mcrpg:blocks_mined")
├── getStatisticType(): StatisticType         // INT, LONG, DOUBLE, STRING, TIMESTAMP, SET_STRING
├── getDefaultValue(): Object                 // default value for new players
├── getDisplayName(): String                  // human-readable name
└── getDescription(): String                  // what this stat tracks
```

#### `StatisticType` (Enum)
Defines the supported data types and how they're serialized to/from the database.

```
com.diamonddagger590.mccore.statistic.StatisticType
├── INT          // Block counts, kill counts, etc.
├── LONG         // Large counters (total XP earned all-time)
├── DOUBLE       // Ratios, percentages, damage dealt
├── STRING       // "Last mob killed", "Favorite skill"
├── TIMESTAMP    // Instant values ("First login", "Last achievement earned")
├── SET_STRING   // Set<String> serialized as JSON - unique collections ("unique players partied with")
```

**Design Decision: Fully typed with enum-based dispatch**

*Pros:*
- Covers "many shapes and sizes" requirement
- SET_STRING enables "play with same group of 5" style tracking
- TIMESTAMP enables "when was this earned" natively
- Each type has clear serialization rules (no ambiguity)
- Type safety at compile time for statistic definitions

*Cons:*
- More complex DB schema than simple key-value
- Adding new types requires McCore changes (enum not extensible by third parties)
- SET_STRING as JSON blob is not queryable by SQL (acceptable tradeoff for flexibility)

*Mitigation for extensibility:* We include a `STRING` type as an escape hatch - third parties can serialize anything into a string and deserialize it themselves. If custom types are needed later, we could add a `CUSTOM` type with a serializer/deserializer pair.

#### `SimpleStatistic` (Concrete Implementation)
A convenience implementation for plugins that just need to register basic stats without creating their own class.

```
com.diamonddagger590.mccore.statistic.SimpleStatistic implements Statistic
```

Constructed via builder or constructor with all fields. Covers 90% of use cases.

#### `StatisticRegistry` (Registry)
Extends the existing registry pattern. Registered statistics are definitions, not per-player data.

```
com.diamonddagger590.mccore.statistic.StatisticRegistry implements Registry<Statistic>
├── register(Statistic)
├── registered(Statistic): boolean
├── getStatistic(NamespacedKey): Optional<Statistic>
├── getRegisteredStatistics(): Set<Statistic>
└── getRegisteredStatisticKeys(): Set<NamespacedKey>
```

A new `RegistryKey.STATISTIC` constant will be added to McCore's `RegistryKey`.

#### `PlayerStatisticData` (Per-player data holder)
Holds all statistic values for a single player, analogous to how `SkillHolder` holds skill data.

```
com.diamonddagger590.mccore.statistic.PlayerStatisticData
├── getUUID(): UUID
├── getValue(NamespacedKey): Optional<Object>
├── getIntValue(NamespacedKey): OptionalInt
├── getLongValue(NamespacedKey): OptionalLong
├── getDoubleValue(NamespacedKey): OptionalDouble
├── getStringValue(NamespacedKey): Optional<String>
├── getTimestampValue(NamespacedKey): Optional<Instant>
├── getSetValue(NamespacedKey): Optional<Set<String>>
├── setValue(NamespacedKey, Object)                    // fires StatisticModifyEvent
├── incrementInt(NamespacedKey, int)                   // convenience for counters
├── incrementLong(NamespacedKey, long)
├── incrementDouble(NamespacedKey, double)
├── bulkIncrementInt(NamespacedKey, int)               // fires ONE event for bulk operations
├── addToSet(NamespacedKey, String): boolean            // returns true if actually added (new element)
├── setMaxInt(NamespacedKey, int)                       // only updates if new value > old ("highest combo")
├── setMaxLong(NamespacedKey, long)
├── setMaxDouble(NamespacedKey, double)
└── setTimestampIfAbsent(NamespacedKey, Instant)        // only sets if not already set ("first kill")
```

**Design Decision: Composition into `CorePlayer`**

`CorePlayer` gets a `PlayerStatisticData` field directly. Statistics are universally available at the McCore level.

```java
// In CorePlayer
private final PlayerStatisticData statisticData;

@NotNull
public PlayerStatisticData getStatisticData() {
    return statisticData;
}
```

*Pros:*
- Any McCore plugin (McRPG, Runic Achievements, third parties) can access stats through `CorePlayer`
- Follows the composition pattern already used (`SkillHolder` in `McRPGPlayer`)
- Statistics load/save naturally hooks into the existing player lifecycle

*Cons:*
- Every `CorePlayer` subclass gets the stats field whether they use it or not (minimal - empty map)
- Slightly increases `CorePlayer`'s responsibilities

*Alternative considered:* Making it opt-in via `StatisticHolder` interface. Rejected because universality is the whole point of putting it in McCore.

#### Events

```
com.diamonddagger590.mccore.event.statistic.StatisticModifyEvent extends Event implements Cancellable
├── getPlayer(): CorePlayer
├── getStatisticKey(): NamespacedKey
├── getStatistic(): Statistic
├── getOldValue(): Object
├── getNewValue(): Object
├── getModificationType(): ModificationType  // SET, INCREMENT, ADD_TO_SET, SET_MAX, etc.
└── isCancelled() / setCancelled()

com.diamonddagger590.mccore.event.statistic.PostStatisticModifyEvent extends Event
├── (same getters, not cancellable - for reactive systems like achievements)

com.diamonddagger590.mccore.event.statistic.StatisticResetEvent extends Event
├── getPlayer(): CorePlayer
├── getStatisticKey(): NamespacedKey  // null if all stats reset
├── getOldValue(): Object
```

**Two-event pattern** matches the existing `SkillGainExpEvent` / `PostSkillGainExpEvent` convention. Pre-event is cancellable for validation, post-event is for reactions. The `StatisticResetEvent` ensures downstream systems are notified when admin commands reset stats.

### 1.2 Database Schema

#### Table: `core_player_statistics`

```sql
CREATE TABLE IF NOT EXISTS core_player_statistics (
    uuid          TEXT NOT NULL,
    statistic_key TEXT NOT NULL,  -- NamespacedKey as "namespace:key"
    stat_type     TEXT NOT NULL,  -- Enum name: INT, LONG, DOUBLE, STRING, TIMESTAMP, SET_STRING
    stat_value    TEXT NOT NULL,  -- All values serialized as text
    PRIMARY KEY (uuid, statistic_key)
);
```

**Design Decision: Single table with text serialization**

*Pros:*
- Simple schema, no joins needed
- Works with any `StatisticType` (current and future)
- Easy to add new statistics without schema migration
- Supports the existing `TableVersionHistoryDAO` pattern for versioning
- SET_STRING serialized as JSON array: `["player1","player2","player3"]`

*Cons:*
- No type safety at DB level (`stat_value` is always TEXT)
- Cannot do SQL-level aggregation on numeric values easily (need to cast)
- Slightly larger storage than typed columns

*Alternative considered:* Separate columns per type (`int_value`, `double_value`, `string_value`, etc.). Rejected because it wastes space (most columns null) and is harder to extend.

#### DAO: `PlayerStatisticDAO`

```
com.diamonddagger590.mccore.database.table.impl.PlayerStatisticDAO
├── attemptCreateTable(Connection, Database): boolean
├── updateTable(Connection)
├── getAllPlayerStatistics(Connection, UUID): Map<NamespacedKey, StatisticEntry>
├── getPlayerStatistic(Connection, UUID, NamespacedKey): Optional<StatisticEntry>
├── savePlayerStatistic(Connection, UUID, NamespacedKey, StatisticType, Object): List<PreparedStatement>
├── saveAllPlayerStatistics(Connection, UUID, PlayerStatisticData): List<PreparedStatement>
└── deletePlayerStatistic(Connection, UUID, NamespacedKey): List<PreparedStatement>
```

`StatisticEntry` is a simple record holding the deserialized key, type, and value.

### 1.3 Integration Points

#### Player Lifecycle
- **Load**: `PlayerStatisticDAO.getAllPlayerStatistics()` called during player load, populates `PlayerStatisticData`
- **Save**: `PlayerStatisticDAO.saveAllPlayerStatistics()` called alongside existing player save (periodic + logout)
- **New player**: Empty `PlayerStatisticData`; defaults populated lazily on first access per stat

#### Third-Party Registration
```java
// Any McCore plugin during onEnable:
StatisticRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.STATISTIC);
registry.register(new SimpleStatistic(
    new NamespacedKey(myPlugin, "custom_currency_earned"),
    StatisticType.LONG,
    0L,
    "Custom Currency Earned",
    "Total custom currency earned"
));
```

### 1.4 Known Gaps & Mitigations

**Gap 1: Offline player stat queries.** PAPI can request stats for `OfflinePlayer`. Current design loads stats on join. We need `PlayerStatisticDAO.getPlayerStatistic(Connection, UUID, NamespacedKey)` for direct DB lookups. Should use an LRU cache with configurable TTL (default 5 min) to prevent DB hammering from scoreboards.

**Gap 2: Thread safety.** `PlayerStatisticData` will be modified from multiple threads. Internal map must use `ConcurrentHashMap`. Numeric increment operations should use `AtomicInteger`/`AtomicLong` wrappers or synchronized access. SET_STRING needs `Collections.synchronizedSet` backed by `LinkedHashSet`.

**Gap 3: Stat value overflow.** INT stats that increment frequently could overflow. Guidance: use LONG for anything expected to accumulate over a player's lifetime. Not enforced - developer responsibility.

**Gap 4: Bulk stat operations.** Mining 64 blocks at once shouldn't fire 64 events. `bulkIncrementInt` fires a single `StatisticModifyEvent` with the total delta.

**Gap 5: Database ownership.** Statistics live in McCore, but McCore's existing `Database` subclasses (`McRPGDatabase`) manage table creation. The `core_player_statistics` table should be created by McCore's own `CreateCoreTablesFunction` / `UpdateCoreTablesFunction`, not by downstream plugins. This ensures the table exists regardless of which McCore plugin initializes first.

**Gap 6: SET_STRING size limits.** A "unique players killed" set could grow unbounded. `Statistic` interface gets an optional `getMaxSetSize()` method (default -1 = unlimited). When exceeded, oldest entries are evicted via `LinkedHashSet` internally.

---

## Part 2: McRPG Statistics Integration

### 2.1 McRPG Statistics Registration

McRPG registers game-specific statistics into McCore's `StatisticRegistry`. This happens regardless of whether any achievement plugin is installed.

```
us.eunoians.mcrpg.statistic.McRPGStatistics
├── BLOCKS_MINED (mcrpg:blocks_mined, LONG)
├── ORES_MINED (mcrpg:ores_mined, LONG)
├── TREES_CHOPPED (mcrpg:trees_chopped, LONG)
├── CROPS_HARVESTED (mcrpg:crops_harvested, LONG)
├── MOBS_KILLED (mcrpg:mobs_killed, LONG)
├── DAMAGE_DEALT (mcrpg:damage_dealt, DOUBLE)
├── DAMAGE_TAKEN (mcrpg:damage_taken, DOUBLE)
├── ABILITIES_ACTIVATED (mcrpg:abilities_activated, LONG)
├── SKILL_LEVELS_GAINED (mcrpg:skill_levels_gained, LONG)
├── TOTAL_SKILL_EXPERIENCE (mcrpg:total_skill_experience, LONG)
├── QUESTS_COMPLETED (mcrpg:quests_completed, LONG)
├── BLEED_PROCS (mcrpg:bleed_procs, LONG)
├── ... per-skill and per-ability stats
```

### 2.2 Gameplay Listeners

McRPG listeners increment stats during gameplay:

```java
// In McRPG's block break listener
player.getStatisticData().incrementLong(McRPGStatistics.BLOCKS_MINED.getKey(), 1);
```

### 2.3 PAPI Placeholders

PAPI placeholders for McRPG stats go through McRPG's existing `McRPGPapiExpansion`:
- `%mcrpg_stat_blocks_mined%`

### 2.4 Data Migration

Existing McRPG data (skill XP, levels) predates the statistics system. A one-time migration task populates statistics from existing data:
- Total skill XP → `mcrpg:total_mining_xp`, etc.
- Skill levels → `mcrpg:skill_levels_gained`
- Run on first boot after update, flag as complete in config to not re-run

---

## Implementation Order

### Phase 1: Statistics Framework (McCore)
Files: ~8-10 new files in McCore
1. Add `StatisticType` enum
2. Add `Statistic` interface + `SimpleStatistic` implementation
3. Add `StatisticRegistry` + `RegistryKey.STATISTIC`
4. Add `PlayerStatisticData` with all typed getters/setters + thread safety
5. Add `StatisticModifyEvent`, `PostStatisticModifyEvent`, `StatisticResetEvent`
6. Add `PlayerStatisticDAO` + table creation in `CreateCoreTablesFunction`
7. Integrate `PlayerStatisticData` into `CorePlayer`
8. Add load/save hooks in McCore's player lifecycle

### Phase 2: McRPG Statistics Integration
Files: ~5-8 new files in McRPG
1. Define `McRPGStatistics` constants (all McRPG-specific stat definitions)
2. Register stats in McRPG's bootstrap
3. Add listeners that increment stats during gameplay (mining, combat, woodcutting, herbalism, quests)
4. Add PAPI placeholders for McRPG statistics
5. Wire up statistics loading/saving in `McRPGPlayerLoadTask` and `McRPGPlayer.savePlayer()`
6. One-time migration task for existing player data → statistics

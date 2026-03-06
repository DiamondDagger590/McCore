# Statistics Framework - High-Level Design

## Overview

McCore provides a generic, typed statistic tracking framework that any McCore-based plugin can use. Statistics are registered globally via McCore's registry system, stored per-player, and persisted to McCore's database. The framework is event-driven, enabling reactive downstream systems (e.g., a future achievements plugin) to respond to stat changes without coupling.

## Plugin Dependency Context

```
McCore (statistics framework - THIS)
  ├── Future: Runic Achievements (queries stats, reacts to PostStatisticModifyEvent)
  └── McRPG (registers and increments game-specific stats)
```

Statistics are owned by McCore. Plugins register statistic definitions during bootstrap and increment values during gameplay. Any future system (achievements, leaderboards, PAPI placeholders) can consume statistics through McCore's event API or direct `PlayerStatisticData` access.

---

## Core Abstractions

### `Statistic` (Interface)

The definition/template for a trackable statistic. Registered once globally, keyed by `NamespacedKey`.

```
com.diamonddagger590.mccore.statistic.Statistic
├── getStatisticKey(): NamespacedKey          // unique identifier (e.g., "mcrpg:blocks_mined")
├── getStatisticType(): StatisticType         // INT, LONG, DOUBLE, STRING, TIMESTAMP, SET_STRING
├── getDefaultValue(): Object                 // default value for new players
├── getDisplayName(): String                  // human-readable name for UIs/PAPI
└── getDescription(): String                  // what this stat tracks (for documentation/admin tools)
```

### `StatisticType` (Enum)

Defines the supported data types and their serialization to/from the database.

```
com.diamonddagger590.mccore.statistic.StatisticType
├── INT          // Small counters, toggle counts
├── LONG         // Large counters (total XP earned all-time, blocks mined)
├── DOUBLE       // Fractional values (damage dealt, ratios)
├── STRING       // Freeform text ("last mob killed", "favorite skill")
├── TIMESTAMP    // Instant values ("first login", "last achievement earned")
├── SET_STRING   // Set<String> serialized as JSON array ("unique biomes visited")
```

**Design Decisions:**
- **Enum-based dispatch over generic `<T>` typing**: Each type has clear, unambiguous serialization rules. The enum is not extensible by third parties, but this is intentional — consistency across all plugins is more valuable than per-plugin custom types. Plugins needing exotic storage can use `STRING` as an escape hatch and handle their own serialization.
- **`LONG` as the default for counters**: INT overflows at ~2.1 billion. Any stat expected to accumulate over a player's lifetime (blocks mined, damage dealt) should use LONG. The HLD recommends LONG for all counter-type statistics.

### `SimpleStatistic` (Record)

A convenience implementation for plugins that just need to register basic stats without creating their own class. Covers the vast majority of use cases.

```
com.diamonddagger590.mccore.statistic.SimpleStatistic implements Statistic
├── Constructor: (NamespacedKey key, StatisticType type, Object defaultValue, String displayName, String description)
```

This is a `record` (Java 21) — immutable, compact, and suitable for constant definitions.

### `StatisticRegistry`

Extends the existing `Registry<Statistic>` pattern. Stores statistic definitions (not per-player data).

```
com.diamonddagger590.mccore.statistic.StatisticRegistry implements Registry<Statistic>
├── register(Statistic)                                   // from Registry<Statistic>
├── registered(Statistic): boolean                        // from Registry<Statistic>
├── getStatistic(NamespacedKey): Optional<Statistic>
├── getRegisteredStatistics(): Set<Statistic>
└── getRegisteredStatisticKeys(): Set<NamespacedKey>
```

A new `RegistryKey.STATISTIC` constant will be added to `RegistryKey`:

```java
RegistryKey<StatisticRegistry> STATISTIC = create(StatisticRegistry.class);
```

The `StatisticRegistry` is created and registered in `CoreBootstrap.start()` alongside the existing registries (`ManagerRegistry`, `PluginHookRegistry`, `PlayerSettingRegistry`).

---

## Per-Player Data: `PlayerStatisticData`

Holds all statistic values for a single player. This is the primary API surface for reading and writing stats.

```
com.diamonddagger590.mccore.statistic.PlayerStatisticData
├── getUUID(): UUID
│
│  // Typed getters — return empty if stat not set or wrong type
├── getValue(NamespacedKey): Optional<Object>
├── getIntValue(NamespacedKey): OptionalInt
├── getLongValue(NamespacedKey): OptionalLong
├── getDoubleValue(NamespacedKey): OptionalDouble
├── getStringValue(NamespacedKey): Optional<String>
├── getTimestampValue(NamespacedKey): Optional<Instant>
├── getSetValue(NamespacedKey): Optional<Set<String>>
│
│  // Mutators — each fires StatisticModifyEvent (cancellable) then PostStatisticModifyEvent
├── setValue(NamespacedKey, Object)                    // generic set
├── incrementInt(NamespacedKey, int)                   // counter convenience
├── incrementLong(NamespacedKey, long)
├── incrementDouble(NamespacedKey, double)
├── addToSet(NamespacedKey, String): boolean           // returns true if element was new
├── removeFromSet(NamespacedKey, String): boolean      // returns true if element was present
│
│  // Conditional mutators — set only if condition met, fire events only if value changed
├── setMaxInt(NamespacedKey, int)                      // only updates if new > old ("highest combo")
├── setMaxLong(NamespacedKey, long)
├── setMaxDouble(NamespacedKey, double)
├── setTimestampIfAbsent(NamespacedKey, Instant)       // only sets if not already set ("first kill")
│
│  // Bulk operations — fire ONE event with the total delta
├── bulkIncrementLong(NamespacedKey, long)
│
│  // Internal — called by lifecycle, not by plugin code
├── loadFromDatabase(Map<NamespacedKey, StatisticEntry>)
├── isDirty(): boolean                                 // true if any stat changed since last save
├── getModifiedEntries(): Map<NamespacedKey, StatisticEntry>  // only dirty entries for efficient save
└── markClean()                                        // called after successful save
```

### Thread Safety Strategy

`PlayerStatisticData` will be accessed from both the main server thread and async event handlers. The internal storage uses `ConcurrentHashMap<NamespacedKey, Object>`. Numeric operations (`incrementLong`, `setMaxLong`, etc.) use `synchronized` blocks scoped to the individual key to ensure atomicity without global locking. `SET_STRING` values are stored as `CopyOnWriteArraySet` to allow concurrent iteration without modification exceptions.

### Dirty Tracking

Rather than saving all statistics on every save cycle, `PlayerStatisticData` tracks which entries have been modified since the last save. The `isDirty()` / `getModifiedEntries()` / `markClean()` trio enables efficient delta saves — only modified statistics are written to the database. This is important because a player could have 50+ registered statistics but only modify 2-3 per save cycle.

### Composition into `CorePlayer`

`CorePlayer` gains a `PlayerStatisticData` field via composition, following the same pattern McRPG uses with `SkillHolder` on `McRPGPlayer`.

```java
// In CorePlayer
private final PlayerStatisticData statisticData;

public CorePlayer(@NotNull UUID uuid, @NotNull CorePlugin corePlugin) {
    // ... existing init ...
    this.statisticData = new PlayerStatisticData(uuid);
}

@NotNull
public PlayerStatisticData getStatisticData() {
    return statisticData;
}
```

**Tradeoff:** Every `CorePlayer` subclass gets the `statisticData` field whether they use statistics or not. This is acceptable — an empty `PlayerStatisticData` is just an empty `ConcurrentHashMap` (~48 bytes). The benefit is universal access: any McCore-based plugin can read/write stats through `CorePlayer` without downcasting.

---

## Events

### Two-Event Pattern

Following the established convention used by McRPG's `SkillGainExpEvent` / `PostSkillGainExpEvent`:

```
com.diamonddagger590.mccore.event.statistic.StatisticModifyEvent extends CorePlayerEvent implements Cancellable
├── getStatisticKey(): NamespacedKey
├── getStatistic(): Statistic
├── getOldValue(): Object
├── getNewValue(): Object
├── setNewValue(Object)                              // allows listeners to adjust the value
├── getModificationType(): ModificationType          // SET, INCREMENT, ADD_TO_SET, REMOVE_FROM_SET, SET_MAX, SET_IF_ABSENT
├── isCancelled() / setCancelled(boolean)
```

```
com.diamonddagger590.mccore.event.statistic.PostStatisticModifyEvent extends CorePlayerEvent
├── getStatisticKey(): NamespacedKey
├── getStatistic(): Statistic
├── getOldValue(): Object
├── getNewValue(): Object                            // final value after any listener adjustments
├── getModificationType(): ModificationType
```

```
com.diamonddagger590.mccore.event.statistic.ModificationType (Enum)
├── SET
├── INCREMENT
├── ADD_TO_SET
├── REMOVE_FROM_SET
├── SET_MAX
├── SET_IF_ABSENT
```

**Pre-event (`StatisticModifyEvent`):** Cancellable. Listeners can cancel the modification or adjust `newValue`. Use case: a plugin wants to apply a multiplier to XP stats, or block certain stat changes in specific worlds.

**Post-event (`PostStatisticModifyEvent`):** Not cancellable. For reactive systems — a future achievements plugin listens here to check if thresholds are met. This event fires only if the pre-event was not cancelled and the value actually changed.

### Event Firing Rules

- `setValue()`, `incrementX()`, `setMaxX()`, `setTimestampIfAbsent()` — each fires one pre-event and (if not cancelled) one post-event.
- `bulkIncrementLong()` — fires ONE pre-event with the total delta and ONE post-event. This prevents event spam when mining 64 blocks at once.
- `addToSet()` / `removeFromSet()` — fires events only if the set actually changed (element was new / was present).
- Conditional mutators (`setMaxX`, `setTimestampIfAbsent`) — fire events only if the value actually changes.

---

## Database Schema

### Table: `core_player_statistics`

```sql
CREATE TABLE IF NOT EXISTS core_player_statistics (
    uuid            VARCHAR(36) NOT NULL,
    statistic_key   VARCHAR(256) NOT NULL,  -- NamespacedKey as "namespace:key"
    stat_type       VARCHAR(32) NOT NULL,   -- Enum name: INT, LONG, DOUBLE, STRING, TIMESTAMP, SET_STRING
    int_value       INTEGER,                -- Used by INT type
    long_value      BIGINT,                 -- Used by LONG type
    double_value    DOUBLE,                 -- Used by DOUBLE type
    string_value    TEXT,                   -- Used by STRING and SET_STRING (JSON array) types
    timestamp_value BIGINT,                 -- Used by TIMESTAMP type (epoch millis)
    PRIMARY KEY (uuid, statistic_key)
);

CREATE INDEX IF NOT EXISTS idx_core_stats_uuid ON core_player_statistics (uuid);
```

**Design Decisions:**
- **Typed columns**: Each `StatisticType` writes to its native column (`INT` → `int_value`, `LONG` → `long_value`, etc.). Numbers are stored in their binary representation rather than as text strings, making storage more data-efficient. Unused columns are `NULL` which costs almost nothing in storage overhead. This also enables SQL-level numeric operations — `ORDER BY long_value DESC` works directly for future leaderboard queries without casting.
- **`stat_type` column stored alongside values**: Tells the DAO which column to read without needing the `StatisticRegistry`. Also useful for data migration tools, admin queries, and defensive deserialization.
- **`STRING` and `SET_STRING` share `string_value`**: Both are text-based. `SET_STRING` is serialized as a JSON array `["value1","value2"]`. The `stat_type` column distinguishes them during deserialization.
- **`TIMESTAMP` stored as epoch millis in `timestamp_value`**: `BIGINT` is more portable across database engines and directly sortable. Converted to/from `java.time.Instant` in the DAO layer.
- **One row per stat, one table total**: Avoids the complexity of separate tables per type (6 tables, 6 indexes, 6 queries to load a player). A single `SELECT * FROM core_player_statistics WHERE uuid = ?` loads all stats for a player in one query.
- **UUID index**: Matches the pattern used by `PlayerSettingDAO` for efficient per-player lookups.

### `StatisticEntry` (Record)

A simple data carrier for deserialized statistic data from the database.

```
com.diamonddagger590.mccore.statistic.StatisticEntry
├── key(): NamespacedKey
├── type(): StatisticType
├── value(): Object
```

### `PlayerStatisticDAO`

Follows the established DAO pattern (static methods, `Connection` as first parameter):

```
com.diamonddagger590.mccore.database.table.impl.PlayerStatisticDAO
├── attemptCreateTable(Connection, Database): boolean
├── updateTable(Connection)
├── getAllPlayerStatistics(Connection, UUID): Map<NamespacedKey, StatisticEntry>
├── getPlayerStatistic(Connection, UUID, NamespacedKey): Optional<StatisticEntry>
├── savePlayerStatistics(Connection, UUID, Map<NamespacedKey, StatisticEntry>): List<PreparedStatement>
├── savePlayerStatistic(Connection, UUID, StatisticEntry): PreparedStatement
└── deletePlayerStatistic(Connection, UUID, NamespacedKey): PreparedStatement
```

**Table creation**: Added to McCore's existing `CreateTableFunction` / `UpdateTableFunction` pipeline so the table is created automatically alongside `player_settings`, `mutex`, and `table_version_history`.

---

## Player Lifecycle Integration

### Loading

Statistics are loaded as part of the player load pipeline. In McCore, `PlayerLoadTask` is abstract — each downstream plugin implements `loadPlayer()`. Statistics loading needs to happen at the McCore level so that stats are available regardless of which downstream plugin is running.

**Approach:** Add a `loadPlayerStatistics()` method to `PlayerLoadTask` that downstream plugins call from their `loadPlayer()` implementation. This follows the same pattern as `PlayerSettingDAO.getPlayerSettings()` being called from `McRPGPlayerLoadTask.loadPlayerSettings()`.

```java
// In McRPGPlayerLoadTask.loadPlayer():
updatePlayerDataSyncFunctions.add(loadPlayerStatistics(connection));  // NEW
updatePlayerDataSyncFunctions.add(loadPlayerSkills(connection));
// ... existing loads ...
```

The statistics load calls `PlayerStatisticDAO.getAllPlayerStatistics()` and populates `CorePlayer.getStatisticData()` via `loadFromDatabase()`.

### Saving

Statistics are saved alongside existing player data in `McRPGPlayer.savePlayer()`:

```java
// In McRPGPlayer.savePlayer():
if (getStatisticData().isDirty()) {
    FailSafeTransaction statisticTransaction = new FailSafeTransaction(connection);
    statisticTransaction.addAll(
        PlayerStatisticDAO.savePlayerStatistics(connection, getUUID(), getStatisticData().getModifiedEntries())
    );
    if (statisticTransaction.executeTransaction()) {
        getStatisticData().markClean();
    }
}
```

`markClean()` is only called after the transaction succeeds. If the transaction fails, the dirty entries are preserved and will be retried on the next save cycle. The dirty tracking ensures only modified stats are written, reducing database load during periodic saves.

### New Players

New players start with an empty `PlayerStatisticData`. Default values from `Statistic.getDefaultValue()` are returned lazily on first access — no database rows are created until a value is actually set.

---

## Offline Player Stat Queries

PAPI, leaderboards, and a future achievements plugin may need to query stats for offline players. Since `PlayerStatisticData` only exists for online players (loaded on join, discarded on quit), offline queries go directly to the database.

**Approach:** `PlayerStatisticDAO.getPlayerStatistic(Connection, UUID, NamespacedKey)` already supports single-stat lookups. For PAPI, this is called on demand. For bulk queries (leaderboards, achievement evaluation), `PlayerStatisticDAO.getAllPlayerStatistics()` handles it.

**Caching:** A [Caffeine](https://github.com/ben-manes/caffeine) cache wraps offline stat queries to prevent database hammering from scoreboards that refresh every tick. Caffeine is already a dependency in the McRPG ecosystem and provides high-performance, thread-safe caching with configurable max size, TTL-based expiration, and a W-TinyLfu eviction policy (superior hit rates compared to a traditional LRU cache). The cache is keyed by `(UUID, NamespacedKey)` pairs.

```
com.diamonddagger590.mccore.statistic.cache.StatisticCache
├── Backed by: Cache<StatisticCacheKey, StatisticEntry>  // Caffeine cache
├── get(UUID, NamespacedKey): Optional<StatisticEntry>
├── invalidate(UUID)                                     // called on player join (live data takes over)
├── invalidate(UUID, NamespacedKey)                      // called on save
└── size(): int
```

`StatisticCacheKey` is a record of `(UUID uuid, NamespacedKey key)` used as the Caffeine cache key.

The cache is optional — if disabled in config, no cache is created and all offline queries go directly to the database. Configuration lives in McCore's config under a `statistics` section.

---

## PAPI Integration

McCore provides a `StatisticPlaceholderExpansion` that registers placeholders for all registered statistics:

```
%mccore_stat_<namespace>_<key>%           → formatted value
%mccore_stat_<namespace>_<key>_raw%       → raw value (no formatting)
%mccore_stat_set_<namespace>_<key>_size%  → set size (for SET_STRING)
```

Examples:
```
%mccore_stat_mcrpg_blocks_mined%     → "1,234,567"
%mccore_stat_mcrpg_blocks_mined_raw% → "1234567"
%mccore_stat_mcrpg_unique_biomes_size% → "12"
```

The expansion uses `CorePlayer.getStatisticData()` for online players and falls back to `StatisticCache` / direct DB query for offline players.

---

## Configuration

McCore's config gains a `statistics` section:

```yaml
statistics:
  # Whether to enable the statistics framework. If false, no stats are tracked or persisted.
  enabled: true

  # Save behavior
  save:
    # Only save modified stats (recommended). If false, saves all stats every cycle.
    delta-only: true

  # Offline query cache (used by PAPI, leaderboards, etc.)
  cache:
    # Whether to cache offline stat queries
    enabled: true
    # Maximum number of entries in the cache
    max-size: 1000
    # How long cached entries live before being re-fetched from the database (seconds)
    ttl: 300
```

---

## Extension Points for Future Systems

The statistics framework is designed to support a future Runic Achievements plugin without requiring changes to McCore:

1. **`PostStatisticModifyEvent`**: Achievements listens to this event to check thresholds in real-time.
2. **`StatisticRegistry`**: Achievements can enumerate all registered statistics to build its UI.
3. **`PlayerStatisticDAO`**: Achievements can bulk-query offline player stats for retroactive evaluation.
4. **`StatisticCache`**: Shared cache prevents duplicate DB queries when both PAPI and achievements request the same stat.

The achievements plugin would also benefit from a `StatisticRepository` pattern — an interface that plugins implement to allow bulk stat queries across plugin boundaries. This is deferred to the achievements HLD.

---

## Known Gaps & Mitigations

### Gap 1: SET_STRING Unbounded Growth
A "unique players killed" set could grow indefinitely. **Mitigation:** `Statistic` interface includes an optional `getMaxSetSize()` method (default `-1` = unlimited). When the limit is reached, the oldest entries (insertion order via `LinkedHashSet`) are evicted. Plugins define the cap per-statistic at registration time.

### Gap 2: Bulk Stat Operations
Mining 64 blocks shouldn't fire 64 separate events. **Mitigation:** `bulkIncrementLong()` fires a single `StatisticModifyEvent` with the total delta. Plugin listeners should batch their increments where possible.

### Gap 3: Cross-Server Consistency
In a multi-server (BungeeCord/Velocity) environment, two servers could load the same player's stats simultaneously. **Mitigation:** McCore already has `Mutexable` / `MutexDAO` for cross-server locking. `CorePlayer.useMutex()` controls whether mutex is used. Statistics piggyback on this existing mechanism — if mutex is enabled, stats are safe.

### Gap 4: Large-Scale Offline Queries
Retroactive achievement evaluation might need to scan all players' stats. `getAllPlayerStatistics()` per-player is O(n) in total. **Mitigation:** Deferred to the achievements HLD. A materialized summary table or batch query API can be added later without changing the core schema.

---

## Implementation Phases

### Phase 1: Core Framework (~8-10 new files in McCore)
1. `StatisticType` enum with serialization/deserialization logic
2. `Statistic` interface + `SimpleStatistic` record implementation
3. `StatisticEntry` record
4. `StatisticRegistry` + `RegistryKey.STATISTIC` constant
5. `PlayerStatisticData` with typed getters/setters, thread safety, dirty tracking
6. `StatisticModifyEvent`, `PostStatisticModifyEvent`, `ModificationType` enum
7. `PlayerStatisticDAO` + table creation in existing pipeline
8. `PlayerStatisticData` field added to `CorePlayer`
9. `StatisticCache` for offline queries
10. PAPI `StatisticPlaceholderExpansion`
11. Configuration section in McCore config

### Phase 2: McRPG Integration (~5-8 new files in McRPG)
See the [McRPG Statistics Integration HLD](../../../McRPG/docs/hld/statistics/statistics-integration-hld.md).

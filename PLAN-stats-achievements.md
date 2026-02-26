# Statistics & Achievements System - Implementation Plan

## Overview

This plan adds two systems across three plugins:

1. **Statistics Framework** (McCore) - Generic, fully-typed statistic tracking for any McCore plugin
2. **Runic Achievements** (NEW standalone plugin) - Multi-stage achievement system powered by statistics
3. **McRPG Integration** - McRPG soft-depends on Runic Achievements to register game-specific stats and achievements

### Plugin Dependency Graph

```
McCore (statistics framework)
  ├── Runic Achievements (hard depends on McCore)
  │     └── Achievement core, rewards, global stats, PAPI
  └── McRPG (hard depends on McCore, soft depends on Runic Achievements)
        ├── Registers McRPG-specific statistics into McCore
        └── Registers McRPG-specific achievements into Runic Achievements (when present)
```

### Branding

- **Runic Achievements** - first product in the new "Runic" brand family
- Package: `us.eunoians.runic.achievements`
- New repository: `DiamondDagger590/RunicAchievements`
- Future potential: `Runic Core` as McCore rebrand, other `Runic X` products

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

**Two-event pattern** matches the existing `SkillGainExpEvent` / `PostSkillGainExpEvent` convention. Pre-event is cancellable for validation, post-event is for reactions. The `StatisticResetEvent` ensures achievement systems are notified when admin commands reset stats.

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

### 1.4 Criticism & Gaps for Statistics

**Gap 1: Offline player stat queries.** PAPI can request stats for `OfflinePlayer`. Current design loads stats on join. We need `PlayerStatisticDAO.getPlayerStatistic(Connection, UUID, NamespacedKey)` for direct DB lookups. Should use an LRU cache with configurable TTL (default 5 min) to prevent DB hammering from scoreboards.

**Gap 2: Thread safety.** `PlayerStatisticData` will be modified from multiple threads. Internal map must use `ConcurrentHashMap`. Numeric increment operations should use `AtomicInteger`/`AtomicLong` wrappers or synchronized access. SET_STRING needs `Collections.synchronizedSet` backed by `LinkedHashSet`.

**Gap 3: Stat value overflow.** INT stats that increment frequently could overflow. Guidance: use LONG for anything expected to accumulate over a player's lifetime. Not enforced - developer responsibility.

**Gap 4: Bulk stat operations.** Mining 64 blocks at once shouldn't fire 64 events. `bulkIncrementInt` fires a single `StatisticModifyEvent` with the total delta.

**Gap 5: Database ownership.** Statistics live in McCore, but McCore's existing `Database` subclasses (`McRPGDatabase`) manage table creation. The `core_player_statistics` table should be created by McCore's own `CreateCoreTablesFunction` / `UpdateCoreTablesFunction`, not by downstream plugins. This ensures the table exists regardless of which McCore plugin initializes first.

---

## Part 2: Runic Achievements (NEW Standalone Plugin)

### 2.1 Plugin Architecture

Runic Achievements is a standalone Bukkit plugin that **hard depends on McCore**.

```yaml
# plugin.yml
name: RunicAchievements
version: 1.0.0
main: us.eunoians.runic.achievements.RunicAchievements
depend: [McCore]
softdepend: []  # No McRPG dependency!
```

```
us.eunoians.runic.achievements.RunicAchievements extends CorePlugin
├── bootstrap/RunicAchievementsBootstrap extends CoreBootstrap
├── database/RunicAchievementsDatabase extends Database
├── Registry keys, manager keys, etc.
```

Runic Achievements manages:
- Achievement definitions (registry)
- Per-player achievement state (load/save on join/quit)
- Global completion stats (background task)
- Achievement events
- Reward processing
- PAPI integration
- Its own database tables

**It does NOT depend on McRPG in any way.** Any McRPG-specific integration (skill XP rewards, McRPG-specific achievements) is registered by McRPG when it detects Runic Achievements is present.

### 2.2 Core Abstractions

#### `Achievement` (Abstract Class)
The definition of an achievement.

```
us.eunoians.runic.achievements.achievement.Achievement
├── getAchievementKey(): NamespacedKey
├── getDisplayName(): String                       // human-readable name
├── getDescription(): String                       // what this achievement tracks
├── getStages(): List<AchievementStage>            // ordered stages (iron→bronze→...→master)
├── getStage(int tier): Optional<AchievementStage>
├── getMaxTier(): int
├── isMultiStage(): boolean                        // convenience: stages.size() > 1
├── getProgressionType(): AchievementProgressionType
├── isEnabled(): boolean                           // can be disabled via config
└── getIcon(): ItemStack                           // for GUI display
```

**Key difference from original plan:** No `McRPGContent` interface, no `McRPGPlayer` parameter for display names. Runic Achievements is McRPG-agnostic. Localization for achievement names can be handled by the plugin's own localization system or simple config strings for v1.

#### `AchievementStage`
Represents one tier of an achievement (eg. "Bronze - Mine 1000 blocks").

```
us.eunoians.runic.achievements.achievement.AchievementStage
├── getTier(): int                             // 1, 2, 3, ... (ordinal)
├── getTierName(): String                      // "Iron", "Bronze", "Silver", "Gold", "Master"
├── getThreshold(): long                       // value needed to unlock this stage (absolute, not relative)
├── getRewards(): List<AchievementReward>      // rewards given when this stage is reached
└── getDescription(): String                   // tier-specific description
```

**Design Decision: Absolute thresholds**

Stage thresholds are absolute values against the tracked metric. If "Iron" is 100 blocks and "Bronze" is 1000 blocks, a player with 500 blocks is past Iron but still working toward Bronze. This matches LoL's model and is simpler to reason about than relative deltas.

#### `AchievementProgressionType` (Enum)

```
us.eunoians.runic.achievements.achievement.AchievementProgressionType
├── STATISTIC   // Progress driven by a McCore Statistic value (automatic via PostStatisticModifyEvent)
└── CUSTOM      // Progress driven by custom logic (manual calls to AchievementManager)
```

#### `StatisticAchievement` extends `Achievement`
Automatically evaluates progress when the linked statistic changes.

```
us.eunoians.runic.achievements.achievement.StatisticAchievement
├── getLinkedStatistic(): NamespacedKey         // the McCore statistic key this achievement tracks
```

#### `CustomAchievement` extends `Achievement`
For complex conditions that can't be expressed as a single statistic.

```
us.eunoians.runic.achievements.achievement.CustomAchievement
├── getTracker(): AchievementTracker            // custom tracking logic
```

#### `AchievementTracker` (Interface)
For CUSTOM achievements, provides the hook for evaluating complex conditions.

```
us.eunoians.runic.achievements.achievement.tracker.AchievementTracker
├── getCurrentProgress(UUID playerUUID): long
├── registerListeners()
└── unregisterListeners()
```

This is what handles the "play with same group of 5" scenario - a custom tracker registers its own Bukkit listeners, evaluates the condition internally, and reports progress back to the achievement system.

### 2.3 Per-Player Achievement Data

#### `PlayerAchievementData`
Stored per-player in Runic Achievements' own data manager.

```
us.eunoians.runic.achievements.player.PlayerAchievementData
├── getUUID(): UUID
├── getAchievementState(NamespacedKey): Optional<AchievementState>
├── getAllAchievementStates(): Map<NamespacedKey, AchievementState>
├── setAchievementState(NamespacedKey, AchievementState)
└── hasEarnedStage(NamespacedKey, int tier): boolean
```

#### `AchievementState`
Per-player state for a single achievement.

```
us.eunoians.runic.achievements.player.AchievementState
├── getAchievementKey(): NamespacedKey
├── getCurrentTier(): int                      // 0 = not earned, 1 = first stage, etc.
├── getCurrentProgress(): long                 // progress toward next stage (for CUSTOM type; STATISTIC reads live from stat value)
├── getTimestampForTier(int tier): Optional<Instant>
├── getAllTimestamps(): Map<Integer, Instant>
├── setCurrentTier(int tier)
├── setCurrentProgress(long progress)
└── addTierTimestamp(int tier, Instant timestamp)
```

**Design Decision: STATISTIC achievements don't store progress separately**

For `StatisticAchievement`, the "current progress" IS the statistic value. There's no need to duplicate it. `AchievementState.getCurrentProgress()` for statistic achievements reads directly from `CorePlayer.getStatisticData().getValue(linkedStatKey)`. Only CUSTOM achievements store progress in `AchievementState`.

*Pros:*
- No data duplication between statistics and achievement progress
- Statistics are always the single source of truth
- Less DB storage

*Cons:*
- Evaluating progress for a statistic achievement requires looking up the player's statistic data
- If the player is offline, we need to query the statistics DB table

### 2.4 Player Data Management

**Runic Achievements manages its own player data independently from McRPG.**

```
us.eunoians.runic.achievements.player.AchievementPlayerManager
├── Map<UUID, PlayerAchievementData> playerData  // ConcurrentHashMap
├── loadPlayerData(UUID): CompletableFuture<PlayerAchievementData>
├── savePlayerData(UUID)
├── getPlayerData(UUID): Optional<PlayerAchievementData>
└── removePlayerData(UUID)
```

Data lifecycle:
- **`PlayerJoinEvent`**: Load achievement data from DB async, store in `AchievementPlayerManager`
- **Periodic save**: Save all online players' data every N seconds (configurable, default 300s - matching McRPG's pattern)
- **`PlayerQuitEvent`**: Save and remove from memory
- **Shutdown**: Save all online players

This is intentionally decoupled from McCore's `PlayerManager` / `CorePlayer` lifecycle. Runic Achievements listens to Bukkit events directly rather than coupling to McCore's player load task pattern. This keeps the plugin truly standalone.

### 2.5 Achievement Management

#### `AchievementRegistry`

```
us.eunoians.runic.achievements.registry.AchievementRegistry implements Registry<Achievement>
├── register(Achievement)
├── registered(Achievement): boolean
├── getAchievement(NamespacedKey): Optional<Achievement>
├── getRegisteredAchievements(): Set<Achievement>
├── getAchievementsByType(AchievementProgressionType): Set<Achievement>
└── getStatisticAchievements(NamespacedKey statisticKey): Set<Achievement>
```

The `getStatisticAchievements()` method maintains an internal index (`Map<NamespacedKey, Set<Achievement>>`) mapping statistic keys to achievements that track them. This index is built during registration and enables O(1) lookup when a stat changes, avoiding the cascade problem.

#### `AchievementManager`

```
us.eunoians.runic.achievements.manager.AchievementManager extends Manager<RunicAchievements>
├── evaluateAchievementProgress(UUID playerUUID, Achievement achievement)
├── evaluateAllStatisticAchievements(UUID playerUUID, NamespacedKey statisticKey)
├── progressCustomAchievement(UUID playerUUID, NamespacedKey achievementKey, long progress)
├── awardAchievementStage(UUID playerUUID, Achievement, AchievementStage)
├── getGlobalCompletionPercentage(NamespacedKey achievementKey, int tier): double
├── refreshGlobalCompletionStats()
└── getGlobalCompletionStats(): Map<NamespacedKey, Map<Integer, Double>>
```

**Note:** Methods take `UUID` not `McRPGPlayer` - this plugin knows nothing about McRPG.

### 2.6 Stat → Achievement Bridge (Listener)

```java
public class StatisticAchievementListener implements Listener {
    @EventHandler
    public void onStatisticModify(PostStatisticModifyEvent event) {
        NamespacedKey statKey = event.getStatisticKey();
        AchievementRegistry registry = ...;
        AchievementManager manager = ...;

        // O(1) lookup: find all achievements linked to this statistic
        Set<Achievement> linked = registry.getStatisticAchievements(statKey);
        for (Achievement achievement : linked) {
            // Short-circuit: check if player already maxed this achievement
            manager.evaluateAchievementProgress(event.getPlayer().getUUID(), achievement);
        }
    }
}
```

This listener lives in Runic Achievements and listens for McCore's `PostStatisticModifyEvent`. It's the key bridge between the two systems.

### 2.7 Events

```
us.eunoians.runic.achievements.event.AchievementProgressEvent extends Event
├── getPlayerUUID(): UUID
├── getAchievement(): Achievement
├── getOldProgress(): long
├── getNewProgress(): long

us.eunoians.runic.achievements.event.AchievementStageCompleteEvent extends Event implements Cancellable
├── getPlayerUUID(): UUID
├── getAchievement(): Achievement
├── getStage(): AchievementStage
├── getTier(): int
├── getTimestamp(): Instant

us.eunoians.runic.achievements.event.PostAchievementStageCompleteEvent extends Event
├── (same getters, not cancellable - for reactive systems)
```

All events use `UUID` for player identification, not any plugin-specific player class.

### 2.8 Reward System

#### `AchievementReward` (Interface)
```
us.eunoians.runic.achievements.reward.AchievementReward
├── giveReward(UUID playerUUID, Achievement, AchievementStage)
├── getRewardDescription(): String                // for GUI/chat display
└── getRewardType(): String                       // type identifier for serialization
```

#### `AchievementRewardRegistry`
A registry where reward types are registered. This is how McRPG (and other plugins) add their own reward types.

```
us.eunoians.runic.achievements.reward.AchievementRewardRegistry
├── registerRewardType(String typeId, AchievementRewardFactory factory)
├── createReward(String typeId, ConfigurationSection config): Optional<AchievementReward>
└── getRegisteredTypes(): Set<String>
```

`AchievementRewardFactory` is a `Function<ConfigurationSection, AchievementReward>` that parses config into a reward instance.

#### Built-in Reward Implementations (in Runic Achievements):
```
us.eunoians.runic.achievements.reward.impl
├── CommandReward           // executes console commands with %player%, %achievement%, %tier% placeholders
├── ItemReward              // gives items (material, amount, optional NBT)
├── MessageReward           // sends a chat message to the player
└── StatisticReward         // modifies a McCore statistic (meta-reward: earning an achievement can boost a stat)
```

#### McRPG-Registered Reward Types (in McRPG, when Runic Achievements is present):
```
us.eunoians.mcrpg.external.runic
├── ExperienceReward        // grants McRPG skill experience
├── LevelReward             // grants McRPG skill levels
└── UpgradePointReward      // grants McRPG upgrade points
```

This decoupling is clean: Runic Achievements has no idea what "skill experience" is. McRPG registers the reward type into Runic Achievements' `AchievementRewardRegistry` when both plugins are present.

#### Config example:
```yaml
achievements:
  master_miner:
    enabled: true
    display-name: "Master Miner"
    description: "Mine your way to mastery!"
    linked-statistic: "mcrpg:blocks_mined"
    icon: DIAMOND_PICKAXE
    stages:
      1:
        name: "Iron"
        threshold: 100
        rewards:
          - type: command
            command: "eco give %player% 100"
          - type: mcrpg_experience           # registered by McRPG when present
            skill: "mcrpg:mining"
            amount: 500
      2:
        name: "Bronze"
        threshold: 1000
        rewards:
          - type: item
            material: DIAMOND
            amount: 5
      3:
        name: "Silver"
        threshold: 10000
        rewards:
          - type: command
            command: "lp user %player% permission set runic.title.master_miner true"
      4:
        name: "Gold"
        threshold: 100000
        rewards:
          - type: item
            material: NETHERITE_PICKAXE
            amount: 1
      5:
        name: "Master"
        threshold: 1000000
        rewards:
          - type: command
            command: "broadcast %player% has achieved Master Miner!"
```

### 2.9 Global Completion Stats (% of players)

**Periodic Background Task:**

```
us.eunoians.runic.achievements.task.GlobalStatsRefreshTask extends RepeatableCoreTask
```

- Runs every N minutes (configurable, default 15)
- Queries DB:
  ```sql
  SELECT achievement_key, current_tier, COUNT(*) as count
  FROM runic_player_achievements
  WHERE current_tier > 0
  GROUP BY achievement_key, current_tier
  ```
- Total player count: `SELECT COUNT(DISTINCT uuid) FROM runic_player_achievements`
- For each achievement/tier combo: `completion_percentage = tier_count / total_players * 100`
- Results cached in memory in `AchievementManager`, persisted to `runic_achievement_global_stats` table
- GUI/PAPI reads from cache, never from DB directly

**Design Decision: "Total players" = players who have achievement data rows**

Only players who've been online since Runic Achievements was installed. This provides a meaningful denominator - not inflated by legacy players who never interacted with the system.

### 2.10 Database Schema

#### Table: `runic_player_achievements` (per-player achievement state)

```sql
CREATE TABLE IF NOT EXISTS runic_player_achievements (
    uuid              TEXT    NOT NULL,
    achievement_key   TEXT    NOT NULL,
    current_tier      INTEGER NOT NULL DEFAULT 0,
    current_progress  BIGINT  NOT NULL DEFAULT 0,  -- only used for CUSTOM type
    PRIMARY KEY (uuid, achievement_key)
);
```

#### Table: `runic_achievement_timestamps` (when each tier was earned)

```sql
CREATE TABLE IF NOT EXISTS runic_achievement_timestamps (
    uuid              TEXT    NOT NULL,
    achievement_key   TEXT    NOT NULL,
    tier              INTEGER NOT NULL,
    earned_at         TEXT    NOT NULL,  -- ISO-8601 timestamp
    PRIMARY KEY (uuid, achievement_key, tier)
);
```

#### Table: `runic_achievement_global_stats` (cached completion percentages)

```sql
CREATE TABLE IF NOT EXISTS runic_achievement_global_stats (
    achievement_key   TEXT    NOT NULL,
    tier              INTEGER NOT NULL,
    completion_count  INTEGER NOT NULL DEFAULT 0,
    total_players     INTEGER NOT NULL DEFAULT 0,
    last_updated      TEXT    NOT NULL,
    PRIMARY KEY (achievement_key, tier)
);
```

#### Table: `runic_pending_rewards` (rewards for offline players)

```sql
CREATE TABLE IF NOT EXISTS runic_pending_rewards (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    uuid              TEXT    NOT NULL,
    achievement_key   TEXT    NOT NULL,
    tier              INTEGER NOT NULL,
    reward_data       TEXT    NOT NULL,  -- JSON serialized reward info
    created_at        TEXT    NOT NULL
);
```

**Design Decision: Separate timestamps table**

*Pros:*
- Clean normalization (each tier earned is its own row)
- Easy to query "when did player earn tier X" or "who earned tier X this week" for leaderboards
- No wasted columns for tiers not yet earned

*Cons:*
- Requires JOIN when loading full achievement state
- More rows total

### 2.11 PAPI Integration

Runic Achievements registers its own PAPI expansion (independent of McRPG's):

```
us.eunoians.runic.achievements.external.papi.RunicAchievementsPapiExpansion
```

Placeholders:
- `%runicachievements_tier_<achievement_key>%` - current tier (0 if unearned)
- `%runicachievements_progress_<achievement_key>%` - current progress value
- `%runicachievements_percent_<achievement_key>_<tier>%` - % of players who earned this tier
- `%runicachievements_earned_at_<achievement_key>_<tier>%` - timestamp when tier was earned
- `%runicachievements_total_achievements%` - total achievements earned by player
- `%runicachievements_has_<achievement_key>_<tier>%` - true/false if player has this tier

### 2.12 Hybrid Config Model

Achievements are **defined in code** (Java classes registered by plugins) but **configured in YAML** for thresholds, rewards, enabled state, and display strings.

```java
// In McRPG's RunicAchievements integration:
public class MasterMiner extends StatisticAchievement {
    public MasterMiner() {
        super(new NamespacedKey("mcrpg", "master_miner"),
              new NamespacedKey("mcrpg", "blocks_mined"));
    }

    @Override
    public List<AchievementStage> getStages() {
        // Reads from Runic Achievements config, falling back to code defaults
        return loadStagesFromConfig("achievements.master_miner.stages", getDefaultStages());
    }

    private List<AchievementStage> getDefaultStages() {
        return List.of(
            new AchievementStage(1, "Iron", 100, List.of()),
            new AchievementStage(2, "Bronze", 1000, List.of()),
            new AchievementStage(3, "Silver", 10000, List.of()),
            new AchievementStage(4, "Gold", 100000, List.of()),
            new AchievementStage(5, "Master", 1000000, List.of())
        );
    }
}
```

The YAML config allows server owners to:
- Change thresholds per tier
- Enable/disable specific achievements
- Modify rewards per tier
- Change display names and descriptions

They **cannot** create entirely new achievements via config alone (requires a plugin). This is intentional for v1. A future enhancement could add "simple YAML-only achievements" for stat-threshold achievements.

### 2.13 Third-Party Achievement Registration

```java
// Any plugin that depends on Runic Achievements:
public class MyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        // Register a statistic (in McCore)
        StatisticRegistry statRegistry = RegistryAccess.registryAccess().registry(RegistryKey.STATISTIC);
        statRegistry.register(new SimpleStatistic(
            new NamespacedKey(this, "coins_earned"), StatisticType.LONG, 0L,
            "Coins Earned", "Total coins earned"
        ));

        // Register an achievement (in Runic Achievements)
        AchievementRegistry achievementRegistry = RunicAchievements.getInstance()
            .registryAccess().registry(RunicRegistryKey.ACHIEVEMENT);
        achievementRegistry.register(new CoinCollector(this));

        // Optionally register a custom reward type
        RunicAchievements.getInstance().getRewardRegistry()
            .registerRewardType("my_coins", config -> new CoinReward(config.getInt("amount")));
    }
}
```

---

## Part 3: McRPG Integration (Soft Dependency)

### 3.1 McRPG → McCore Statistics

McRPG registers game-specific statistics into McCore's `StatisticRegistry`. This happens regardless of whether Runic Achievements is installed.

```
us.eunoians.mcrpg.statistic.McRPGStatistics  // enum or constants class defining all McRPG stats
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

McRPG listeners increment these stats during gameplay:
```java
// In McRPG's block break listener
player.getStatisticData().incrementLong(McRPGStatistics.BLOCKS_MINED.getKey(), 1);
```

PAPI placeholders for McRPG stats go through McRPG's existing `McRPGPapiExpansion`:
- `%mcrpg_stat_blocks_mined%`

### 3.2 McRPG → Runic Achievements (When Present)

McRPG uses a soft dependency hook to interact with Runic Achievements.

```
us.eunoians.mcrpg.external.runic.RunicAchievementsHook extends PluginHook
├── isRunicAchievementsPresent(): boolean
├── registerMcRPGAchievements()
├── registerMcRPGRewardTypes()
```

In McRPG's bootstrap (only when Runic Achievements is detected):
```java
if (Bukkit.getPluginManager().isPluginEnabled("RunicAchievements")) {
    RunicAchievementsHook hook = new RunicAchievementsHook();
    hook.registerMcRPGAchievements();  // registers MasterMiner, BladeMaster, etc.
    hook.registerMcRPGRewardTypes();   // registers ExperienceReward, LevelReward, etc.
}
```

This keeps McRPG completely functional without Runic Achievements. If Runic Achievements isn't installed, McRPG still tracks statistics (via McCore) - they just don't power any achievements.

### 3.3 McRPG-Specific Achievements (Registered by McRPG)

These are defined in McRPG's codebase and registered into Runic Achievements when present:

```
us.eunoians.mcrpg.external.runic.achievement
├── MasterMiner extends StatisticAchievement      // blocks mined
├── BladeMaster extends StatisticAchievement       // mobs killed with sword
├── LumberjackLegend extends StatisticAchievement  // trees chopped
├── HarvestKing extends StatisticAchievement       // crops harvested
├── BloodThirsty extends StatisticAchievement      // bleed procs
├── SkillSavant extends StatisticAchievement       // total skill levels gained
├── QuestConqueror extends StatisticAchievement    // quests completed
├── ... more McRPG-specific achievements
```

### 3.4 McRPG-Specific Reward Types (Registered by McRPG)

```
us.eunoians.mcrpg.external.runic.reward
├── ExperienceReward implements AchievementReward  // type: "mcrpg_experience"
├── LevelReward implements AchievementReward       // type: "mcrpg_levels"
└── UpgradePointReward implements AchievementReward // type: "mcrpg_upgrade_points"
```

These are registered into Runic Achievements' `AchievementRewardRegistry` so they can be used in YAML configs:
```yaml
rewards:
  - type: mcrpg_experience
    skill: "mcrpg:mining"
    amount: 500
```

---

## Part 4: Design Criticism & Gaps

### Critical Flaws Identified

**1. Two-plugin data synchronization**
McCore statistics and Runic Achievements player data are loaded/saved independently. If McCore loads stats but Runic Achievements hasn't loaded achievement data yet, a stat change could trigger achievement evaluation before the player's achievement state is ready.

*Mitigation:* `AchievementManager.evaluateAchievementProgress()` should check if `PlayerAchievementData` is loaded before evaluating. If not loaded, queue the evaluation and process it once data loads. Alternatively, Runic Achievements can listen for McCore's player-ready event before starting to process stat events for that player.

**2. Database sharing vs. separation**
Runic Achievements has its own `Database` instance, but both it and McRPG connect to the same SQLite file (or different files). If they share a file, connection pool contention. If separate files, no cross-queries.

*Mitigation:* Each plugin should use its own SQLite database file. Cross-queries aren't needed since the global stats task works within Runic Achievements' own tables, and statistic data is accessed through McCore's API (not direct SQL).

**3. Offline player achievement queries for PAPI**
Runic Achievements' PAPI placeholders need to support `OfflinePlayer`. Achievement state must be queryable from DB for offline players.

*Mitigation:* Same LRU cache pattern as McCore statistics. `AchievementDAO` gets a cached offline query path with configurable TTL.

**4. Plugin load order**
McRPG needs to register achievements after Runic Achievements is fully initialized. Bukkit's `depend`/`softdepend` handles load order, but there's a race condition if McRPG's bootstrap runs before Runic Achievements' registries are ready.

*Mitigation:* McRPG registers its achievements in a `PluginEnableEvent` listener for Runic Achievements, or in a delayed task on the next tick after its own `onEnable()`. The safest approach: Runic Achievements fires a custom `RunicAchievementsReadyEvent` after its bootstrap completes, and McRPG listens for it.

**5. SET_STRING stat type has no size limit**
A "unique players killed" set could grow unbounded for active PvP players.

*Mitigation:* `Statistic` interface gets an optional `getMaxSetSize()` method (default -1 = unlimited). When exceeded, oldest entries are evicted via `LinkedHashSet` internally.

**6. Race condition: stat update + achievement evaluation**
If a stat is modified on an async thread and the achievement listener fires on the main thread, there's a window where the achievement reads stale stat data.

*Mitigation:* `PostStatisticModifyEvent` carries the new value directly in the event payload. The achievement evaluator uses that value rather than re-reading from `PlayerStatisticData`.

**7. No migration path for existing McRPG data**
McRPG already tracks implicit statistics (skill XP, levels). These aren't in the new statistics system.

*Mitigation:* McRPG adds a one-time migration task that populates statistics from existing data (eg. total skill XP → `mcrpg:total_mining_xp`). Run on first boot after the update, flag as complete in config to not re-run.

**8. Achievement definition updates across versions**
If McRPG v1.1 adds new stages to an existing achievement, what happens to players who already completed the old max tier?

*Mitigation:* Adding new higher tiers is safe - existing players keep their earned tiers and see new ones to work toward. Removing existing tiers is dangerous and should be disallowed/documented. Changing thresholds: grandfathered players keep earned tiers regardless.

**9. Pending rewards for offline players adds significant complexity**
The `runic_pending_rewards` table needs reward serialization/deserialization, cleanup for expired rewards, and delivery logic on login.

*Mitigation:* This should be Phase 5+, not v1. For v1, rewards are only given to online players. If a stat sync triggers an achievement while offline, the tier is recorded but rewards are given on next login via a simple check (compare earned tiers vs delivered tiers). No separate pending table needed initially.

### Open Design Questions

**Q1: Should Runic Achievements share McRPG's content expansion system?**
*Answer:* No. Runic Achievements is standalone. It uses McCore's `Registry` pattern for extensibility. McRPG's `ContentExpansion` system is McRPG-specific and shouldn't leak into a standalone plugin. Third parties register achievements directly via the registry API.

**Q2: Should Runic Achievements have its own localization system?**
*Answer:* For v1, simple config strings for display names/descriptions. A full localization system (like McRPG's per-player locale support) can be added later. The display strings in YAML config are sufficient for most servers.

**Q3: Can achievements be revoked?**
*Answer:* No for v1. Achievements are permanent. Admin commands can reset them for debugging.

---

## Part 5: Pros & Cons Summary

### Statistics System (McCore)

| Pros | Cons |
|------|------|
| Universal framework - any McCore plugin gets stats for free | Adds complexity to CorePlayer lifecycle (load/save) |
| Fully typed - covers simple counters to complex sets | SET_STRING not queryable by SQL |
| Event-driven - enables reactive systems like achievements | All-text DB schema sacrifices type safety at DB level |
| Third parties can't add new StatisticTypes without McCore change | Thread-safe design needs careful implementation |
| Simple registration API | Offline player queries need caching |

### Achievement System (Runic Achievements - Standalone)

| Pros | Cons |
|------|------|
| **Standalone** - no McRPG dependency, reusable by any McCore plugin | Two-plugin data sync requires careful ordering |
| **Reduces McRPG bloat** - feature lives in its own plugin | Extra JAR for server owners to install |
| Multi-stage progression matches LoL model perfectly | Hybrid config means server owners can't create achievements without code |
| Both stat-powered and custom-tracker achievements | Global stats requires periodic DB queries |
| Extensible reward system (plugins register their own reward types) | Separate database from McRPG |
| Timestamps per tier + global completion % | Plugin load order needs explicit coordination |
| PAPI integration independent of McRPG | Two config locations (McRPG stats + Runic config) |
| New "Runic" brand identity | More complex third-party developer experience (depend on 2 plugins) |

### McRPG Integration Layer

| Pros | Cons |
|------|------|
| McRPG works fully without Runic Achievements | Soft dependency hook adds integration code |
| Clean separation of concerns | McRPG-specific achievements live outside McRPG's main code |
| Statistics tracked regardless of achievement plugin | One-time data migration needed for existing servers |
| McRPG reward types extend the system naturally | Players need both plugins for the full experience |

---

## Part 6: Implementation Order (Phased)

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

### Phase 3: Runic Achievements - Core Plugin Scaffold
Files: ~15-20 new files (new repository)
1. Create repository, project structure, build configuration (Maven/Gradle)
2. `RunicAchievements` main plugin class extending `CorePlugin`
3. `RunicAchievementsBootstrap` extending `CoreBootstrap`
4. `RunicAchievementsDatabase` extending `Database`
5. Registry keys, manager keys
6. `AchievementPlayerManager` for player data lifecycle

### Phase 4: Runic Achievements - Achievement System
Files: ~15-20 new files
1. `AchievementStage`, `Achievement`, `StatisticAchievement`, `CustomAchievement`, `AchievementTracker`
2. `AchievementState`, `PlayerAchievementData`
3. `AchievementRegistry`
4. `AchievementManager`
5. DB tables + DAOs (`AchievementDAO`, `AchievementTimestampDAO`)
6. Achievement events
7. `StatisticAchievementListener` (stat → achievement bridge)
8. Config loading for achievement definitions

### Phase 5: Runic Achievements - Rewards & Polish
Files: ~10-15 new files
1. `AchievementReward` interface + `AchievementRewardRegistry`
2. Built-in rewards: `CommandReward`, `ItemReward`, `MessageReward`, `StatisticReward`
3. YAML configuration parsing for stages and rewards
4. PAPI integration (`RunicAchievementsPapiExpansion`)
5. `RunicAchievementsReadyEvent` for plugin coordination

### Phase 6: McRPG ↔ Runic Achievements Integration
Files: ~8-12 new files in McRPG
1. `RunicAchievementsHook` (soft dependency hook)
2. McRPG-specific achievements (`MasterMiner`, `BladeMaster`, etc.)
3. McRPG-specific reward types (`ExperienceReward`, `LevelReward`, `UpgradePointReward`)
4. Registration logic in McRPG bootstrap (when Runic Achievements present)

### Phase 7: Global Stats & Advanced Features
Files: ~5-8 new files
1. `GlobalStatsRefreshTask` for % completion
2. `runic_achievement_global_stats` table + DAO
3. Admin commands (reset, grant, view, reload)
4. Login-time reward delivery for achievements earned while offline
5. Documentation for third-party developers

### Phase 8: Future Enhancements (Not in v1)
- Achievement GUI (view progress, stages, rewards, % of players)
- Leaderboards for statistics and achievements
- Simple YAML-only achievement templates (no code required)
- Achievement categories/groups for organization
- Achievement notifications (chat, action bar, title screen)
- Full localization system for Runic Achievements
- Cross-server stat synchronization

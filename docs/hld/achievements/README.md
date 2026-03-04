# Achievements System (Runic Achievements) - High-Level Design

## Overview

Runic Achievements (RA) is a standalone Bukkit plugin that provides a multi-stage achievement system. It hard-depends on McCore for general infrastructure but does **not** depend on McRPG. Any McCore-based plugin can register achievements and reward types into RA.

Achievements are primarily driven by McCore statistics, but also support custom tracking logic for complex conditions that can't be expressed as a single statistic value.

## Plugin Dependency Graph

```
McCore (statistics framework)
  ├── Runic Achievements (hard depends on McCore)
  │     └── Achievement core, rewards, global stats, PAPI
  └── McRPG (hard depends on McCore, soft depends on Runic Achievements)
        ├── Registers McRPG-specific statistics into McCore
        └── Registers McRPG-specific achievements into Runic Achievements (when present)
```

### Branding

- **Runic Achievements** - first product in the "Runic" brand family
- Package: `us.eunoians.runic.achievements`
- New repository: `DiamondDagger590/RunicAchievements`
- Future potential: `Runic Core` as McCore rebrand, other `Runic X` products

---

## Retroactive Achievement Evaluation via StatisticRepository

A key design requirement is **retroactive achievement application**: when a new achievement is added that's backed by an existing statistic, players who already meet the threshold should receive it without needing to make further progress.

This rules out a purely event-driven approach (listening to `PostStatisticModifyEvent`) as the sole evaluation mechanism. Instead, RA uses a **polling model** via the `StatisticRepository` pattern.

### `StatisticRepository` (Interface - lives in RA)

Each plugin that exposes statistics registers a `StatisticRepository` with RA. The repository is a callback that RA invokes on a regular interval to bulk-query statistic values.

```java
// Defined in Runic Achievements
public interface StatisticRepository {

    /**
     * Bulk-query statistic values for a set of players and statistic keys.
     * The repository owns threading and connection management.
     */
    CompletableFuture<Map<UUID, Map<StatisticKey, Number>>> getBulkValues(
        Set<UUID> playerUUIDs,
        Set<StatisticKey> statisticKeys
    );
}
```

### Registration Pattern

Each plugin registers **one** repository that handles all of its statistics. The repository closes over its own database reference at registration time.

```java
// McRPG registers this during bootstrap when RA is present
Database mcRPGDatabase = ...; // McRPG's own database instance

raHook.registerRepository(new StatisticRepository() {
    @Override
    public CompletableFuture<Map<UUID, Map<StatisticKey, Number>>> getBulkValues(
            Set<UUID> playerUUIDs, Set<StatisticKey> statisticKeys) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = mcRPGDatabase.getConnection()) {
                return PlayerStatisticDAO.getBulkStatistics(conn, playerUUIDs, statisticKeys);
            }
        }, mcRPGDatabase.getExecutor()); // runs on McRPG's DB thread pool
        }
});
```

### Evaluation Loop

RA runs a periodic evaluation task (configurable interval, 10-30 seconds):

1. Collect online player UUIDs
2. For each registered repository, determine which statistic keys are actually referenced by achievement conditions (no point querying stats nothing cares about)
3. Fire all repository calls **in parallel** - each plugin hits its own DB concurrently
4. Join the futures, evaluate achievement conditions against returned values
5. Award anything newly satisfied

### Efficiency Considerations

- **One repository per plugin, one DB round-trip per cycle**: The repository batches all requested stat keys into a single query rather than one per statistic
- **Only query referenced stats**: RA maintains a reverse index from statistic keys to achievements. Only keys actually used by achievement conditions are requested
- **Value caching**: RA can cache last-seen values and skip re-evaluation for players whose values haven't changed, cutting evaluation work when most players are idle
- **Connection pool awareness**: Repository calls run on the owning plugin's thread pool/executor, so RA's evaluation frequency affects the plugin's connection pool load (not RA's)

### Why Not Pure Event-Driven?

The `PostStatisticModifyEvent` bridge (Section below) still exists for **real-time responsiveness** on stat changes. But events alone cannot handle:

- Retroactive achievement application for newly added achievements
- Server restarts where stat changes happened before RA loaded
- Offline player evaluation (admin commands, background sweeps)

The polling model handles all of these. The event bridge is an optimization for instant feedback, not the source of truth.

---

## Core Abstractions

### `Achievement` (Abstract Class)

```
us.eunoians.runic.achievements.achievement.Achievement
├── getAchievementKey(): NamespacedKey
├── getDisplayName(): String
├── getDescription(): String
├── getStages(): List<AchievementStage>
├── getStage(int tier): Optional<AchievementStage>
├── getMaxTier(): int
├── isMultiStage(): boolean
├── getProgressionType(): AchievementProgressionType
├── isEnabled(): boolean
└── getIcon(): ItemStack
```

No McRPG-specific interfaces or player types. Runic Achievements is McRPG-agnostic.

### `AchievementStage`

```
us.eunoians.runic.achievements.achievement.AchievementStage
├── getTier(): int                             // 1, 2, 3, ... (ordinal)
├── getTierName(): String                      // "Iron", "Bronze", "Silver", "Gold", "Master"
├── getThreshold(): long                       // value needed to unlock (absolute, not relative)
├── getRewards(): List<AchievementReward>
└── getDescription(): String
```

**Absolute thresholds**: If "Iron" is 100 and "Bronze" is 1000, a player with 500 is past Iron but working toward Bronze.

### `AchievementProgressionType` (Enum)

```
├── STATISTIC   // Progress driven by a McCore Statistic value
└── CUSTOM      // Progress driven by custom logic
```

### `StatisticAchievement` extends `Achievement`

```
├── getLinkedStatistic(): NamespacedKey         // the McCore statistic key this achievement tracks
```

### `CustomAchievement` extends `Achievement`

```
├── getTracker(): AchievementTracker            // custom tracking logic
```

### `AchievementTracker` (Interface)

For CUSTOM achievements, provides the hook for evaluating complex conditions (e.g., "play with same group of 5").

```
us.eunoians.runic.achievements.achievement.tracker.AchievementTracker
├── getCurrentProgress(UUID playerUUID): long
├── registerListeners()
└── unregisterListeners()
```

---

## Per-Player Achievement Data

### `PlayerAchievementData`

```
us.eunoians.runic.achievements.player.PlayerAchievementData
├── getUUID(): UUID
├── getAchievementState(NamespacedKey): Optional<AchievementState>
├── getAllAchievementStates(): Map<NamespacedKey, AchievementState>
├── setAchievementState(NamespacedKey, AchievementState)
└── hasEarnedStage(NamespacedKey, int tier): boolean
```

### `AchievementState`

```
us.eunoians.runic.achievements.player.AchievementState
├── getAchievementKey(): NamespacedKey
├── getCurrentTier(): int                      // 0 = not earned
├── getCurrentProgress(): long                 // for CUSTOM type only; STATISTIC reads live from stat value
├── getTimestampForTier(int tier): Optional<Instant>
├── getAllTimestamps(): Map<Integer, Instant>
├── setCurrentTier(int tier)
├── setCurrentProgress(long progress)
└── addTierTimestamp(int tier, Instant timestamp)
```

**STATISTIC achievements don't duplicate progress** - the statistic value IS the progress. No data duplication between statistics and achievement progress.

### Player Data Management

```
us.eunoians.runic.achievements.player.AchievementPlayerManager
├── loadPlayerData(UUID): CompletableFuture<PlayerAchievementData>
├── savePlayerData(UUID)
├── getPlayerData(UUID): Optional<PlayerAchievementData>
└── removePlayerData(UUID)
```

Lifecycle: load on `PlayerJoinEvent`, periodic save (default 300s), save+remove on `PlayerQuitEvent`, save all on shutdown. Decoupled from McCore's `PlayerManager` / `CorePlayer` lifecycle.

---

## Achievement Management

### `AchievementRegistry`

```
us.eunoians.runic.achievements.registry.AchievementRegistry implements Registry<Achievement>
├── register(Achievement)
├── registered(Achievement): boolean
├── getAchievement(NamespacedKey): Optional<Achievement>
├── getRegisteredAchievements(): Set<Achievement>
├── getAchievementsByType(AchievementProgressionType): Set<Achievement>
└── getStatisticAchievements(NamespacedKey statisticKey): Set<Achievement>
```

`getStatisticAchievements()` maintains an internal index (`Map<NamespacedKey, Set<Achievement>>`) for O(1) lookup when a stat changes.

### `AchievementManager`

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

All methods take `UUID`, not any plugin-specific player class.

---

## Stat → Achievement Bridge (Event Listener)

For real-time responsiveness (complementing the polling loop):

```java
public class StatisticAchievementListener implements Listener {
    @EventHandler
    public void onStatisticModify(PostStatisticModifyEvent event) {
        NamespacedKey statKey = event.getStatisticKey();
        Set<Achievement> linked = registry.getStatisticAchievements(statKey);
        for (Achievement achievement : linked) {
            manager.evaluateAchievementProgress(event.getPlayer().getUUID(), achievement);
        }
    }
}
```

This provides instant feedback when a stat changes, while the polling loop handles retroactive evaluation.

---

## Events

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
├── (same getters, not cancellable)
```

All events use `UUID` for player identification.

---

## Reward System

### `AchievementReward` (Interface)

```
us.eunoians.runic.achievements.reward.AchievementReward
├── giveReward(UUID playerUUID, Achievement, AchievementStage)
├── getRewardDescription(): String
└── getRewardType(): String
```

### `AchievementRewardRegistry`

```
us.eunoians.runic.achievements.reward.AchievementRewardRegistry
├── registerRewardType(String typeId, AchievementRewardFactory factory)
├── createReward(String typeId, ConfigurationSection config): Optional<AchievementReward>
└── getRegisteredTypes(): Set<String>
```

### Built-in Rewards (in Runic Achievements)

```
us.eunoians.runic.achievements.reward.impl
├── CommandReward           // executes console commands with %player%, %achievement%, %tier% placeholders
├── ItemReward              // gives items
├── MessageReward           // sends a chat message
└── StatisticReward         // modifies a McCore statistic (meta-reward)
```

### Config Example

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
      2:
        name: "Bronze"
        threshold: 1000
        rewards:
          - type: item
            material: DIAMOND
            amount: 5
```

### Hybrid Config Model

Achievements are **defined in code** (Java classes registered by plugins) but **configured in YAML** for thresholds, rewards, enabled state, and display strings. Server owners cannot create entirely new achievements via config alone in v1.

---

## Database Schema

### `runic_player_achievements`

```sql
CREATE TABLE IF NOT EXISTS runic_player_achievements (
    uuid              TEXT    NOT NULL,
    achievement_key   TEXT    NOT NULL,
    current_tier      INTEGER NOT NULL DEFAULT 0,
    current_progress  BIGINT  NOT NULL DEFAULT 0,  -- only used for CUSTOM type
    PRIMARY KEY (uuid, achievement_key)
);
```

### `runic_achievement_timestamps`

```sql
CREATE TABLE IF NOT EXISTS runic_achievement_timestamps (
    uuid              TEXT    NOT NULL,
    achievement_key   TEXT    NOT NULL,
    tier              INTEGER NOT NULL,
    earned_at         TEXT    NOT NULL,  -- ISO-8601 timestamp
    PRIMARY KEY (uuid, achievement_key, tier)
);
```

### `runic_achievement_global_stats`

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

---

## Global Completion Stats

Periodic background task (`GlobalStatsRefreshTask`, default every 15 minutes):
- Queries DB for tier completion counts
- Total players = players who have achievement data rows (not inflated by legacy players)
- Results cached in memory, read by GUI/PAPI

---

## PAPI Integration

```
%runicachievements_tier_<achievement_key>%
%runicachievements_progress_<achievement_key>%
%runicachievements_percent_<achievement_key>_<tier>%
%runicachievements_earned_at_<achievement_key>_<tier>%
%runicachievements_total_achievements%
%runicachievements_has_<achievement_key>_<tier>%
```

---

## Design Criticism & Known Gaps

### 1. Two-plugin data synchronization
McCore statistics and RA player data load independently. If stats load before achievement data, a stat change could trigger evaluation before achievement state is ready.
**Mitigation:** `AchievementManager.evaluateAchievementProgress()` checks if `PlayerAchievementData` is loaded before evaluating. Queue evaluation if not ready.

### 2. Database separation
Each plugin uses its own database file. No cross-queries needed since stat data is accessed through the `StatisticRepository` contract (not direct SQL).

### 3. Plugin load order
McRPG registers achievements after RA is initialized. RA fires `RunicAchievementsReadyEvent` after bootstrap; McRPG listens for it.

### 4. Race condition: stat update + achievement evaluation
`PostStatisticModifyEvent` carries the new value directly in the event payload. The evaluator uses that value rather than re-reading from `PlayerStatisticData`.

### 5. Achievement definition updates across versions
Adding new higher tiers is safe. Removing existing tiers is dangerous/disallowed. Changing thresholds: grandfathered players keep earned tiers.

### 6. Pending rewards for offline players
v1: rewards only given to online players. If a tier is earned while offline, rewards are given on next login via a simple check (compare earned tiers vs delivered tiers). No separate pending table needed initially.

---

## Implementation Phases

### Phase 1: Plugin Scaffold
1. Create repository, project structure, build configuration
2. `RunicAchievements` main class extending `CorePlugin`
3. `RunicAchievementsBootstrap` extending `CoreBootstrap`
4. `RunicAchievementsDatabase` extending `Database`
5. Registry keys, manager keys
6. `AchievementPlayerManager` for player data lifecycle

### Phase 2: Achievement System
1. Core abstractions: `AchievementStage`, `Achievement`, `StatisticAchievement`, `CustomAchievement`, `AchievementTracker`
2. `AchievementState`, `PlayerAchievementData`
3. `AchievementRegistry` with statistic key index
4. `AchievementManager`
5. DB tables + DAOs
6. Achievement events
7. `StatisticAchievementListener` (event bridge)
8. `StatisticRepository` interface + evaluation loop
9. Config loading for achievement definitions

### Phase 3: Rewards & Polish
1. `AchievementReward` interface + `AchievementRewardRegistry`
2. Built-in rewards: `CommandReward`, `ItemReward`, `MessageReward`, `StatisticReward`
3. YAML configuration parsing for stages and rewards
4. PAPI integration
5. `RunicAchievementsReadyEvent`

### Phase 4: Global Stats & Advanced Features
1. `GlobalStatsRefreshTask` for % completion
2. Admin commands (reset, grant, view, reload)
3. Login-time reward delivery for offline-earned achievements

### Future (Not in v1)
- Achievement GUI
- Leaderboards
- Simple YAML-only achievement templates (no code required)
- Achievement categories/groups
- Achievement notifications (chat, action bar, title)
- Full localization system
- Cross-server stat synchronization

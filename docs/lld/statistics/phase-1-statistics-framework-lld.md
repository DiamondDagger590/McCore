# Phase 1: Statistics Framework — Low-Level Design

**Parent HLD:** [Statistics Framework HLD](../../hld/statistics/statistics-framework-hld.md)

This LLD covers the implementation of the McCore statistics framework — the data model, registry, per-player data, events, persistence, caching, commands, and tests.

---

## Table of Contents

1. [New Files Summary](#new-files-summary)
2. [Data Model](#data-model)
3. [Registry](#registry)
4. [Per-Player Data](#per-player-data)
5. [Events](#events)
6. [Persistence](#persistence)
7. [CorePlayer Integration](#coreplayer-integration)
8. [Caching](#caching)
9. [Commands](#commands)
10. [Exceptions](#exceptions)
11. [Test Fixtures & Unit Tests](#test-fixtures--unit-tests)
12. [Bootstrap Changes](#bootstrap-changes)
13. [Implementation Order](#implementation-order)

---

## New Files Summary

### Production Code (`src/main/java/com/diamonddagger590/mccore/`)

| # | File | Package | Type |
|---|------|---------|------|
| 1 | `StatisticType.java` | `statistic` | Enum |
| 2 | `Statistic.java` | `statistic` | Interface |
| 3 | `SimpleStatistic.java` | `statistic` | Record |
| 4 | `StatisticEntry.java` | `statistic` | Record |
| 5 | `StatisticRegistry.java` | `statistic` | Class (implements `Registry<Statistic>`) |
| 6 | `PlayerStatisticData.java` | `statistic` | Class |
| 7 | `StatisticModifyEvent.java` | `event.statistic` | Class (extends `CorePlayerEvent`, implements `Cancellable`) |
| 8 | `PostStatisticModifyEvent.java` | `event.statistic` | Class (extends `CorePlayerEvent`) |
| 9 | `ModificationType.java` | `event.statistic` | Enum |
| 10 | `PlayerStatisticDAO.java` | `database.table.impl` | Class |
| 11 | `StatisticCache.java` | `statistic.cache` | Class |
| 12 | `StatisticCacheKey.java` | `statistic.cache` | Record |
| 13 | `StatisticNotRegisteredException.java` | `exception.statistic` | Class (extends `RuntimeException`) |

### Modified Files

| File | Change |
|------|--------|
| `RegistryKey.java` | Add `STATISTIC` constant |
| `CoreBootstrap.java` | Register `StatisticRegistry` in `start()` |
| `CorePlayer.java` | Add `PlayerStatisticData` field and getter |
| `CreateCoreTablesFunction.java` | Add `PlayerStatisticDAO.attemptCreateTable()` call |
| `UpdateCoreTablesFunction.java` | Add `PlayerStatisticDAO.updateTable()` call |
| `RegistryResetExtension.java` (testFixtures) | Register `StatisticRegistry` in `setupRegistry()` |

### Test Code (`src/test/java/com/diamonddagger590/mccore/`)

| # | File | Package |
|---|------|---------|
| 1 | `StatisticTypeTest.java` | `statistic` |
| 2 | `SimpleStatisticTest.java` | `statistic` |
| 3 | `StatisticRegistryTest.java` | `statistic` |
| 4 | `PlayerStatisticDataTest.java` | `statistic` |
| 5 | `PlayerStatisticDAOTest.java` | `database.table.impl` |
| 6 | `StatisticCacheTest.java` | `statistic.cache` |

### Dependencies

| Dependency | Purpose | Change |
|------------|---------|--------|
| Caffeine | `StatisticCache` backing store | Add to `build.gradle.kts` as `api` dependency |

---

## Data Model

### `StatisticType` (Enum)

**File:** `src/main/java/com/diamonddagger590/mccore/statistic/StatisticType.java`

```java
package com.diamonddagger590.mccore.statistic;

public enum StatisticType {
    INT,
    LONG,
    DOUBLE,
    STRING,
    TIMESTAMP,
    SET_STRING;

    /**
     * Returns {@code true} if this type is numeric ({@link #INT}, {@link #LONG}, or {@link #DOUBLE}).
     *
     * @return {@code true} if this type is numeric.
     */
    public boolean isNumeric() {
        return this == INT || this == LONG || this == DOUBLE;
    }
}
```

The enum is primarily a discriminator — serialization/deserialization logic lives in `PlayerStatisticDAO` (write to/read from the correct column) and `PlayerStatisticData` (type validation on getters). The one behavioral method is `isNumeric()`, a convenience helper used by callers that need to branch on "any numeric type" without enumerating all three constants (e.g., command validation, display formatting).

**Rationale:** Putting serialize/deserialize on the enum would couple it to the database schema. Keeping serialization in the DAO follows the existing `PlayerSettingDAO` pattern where the DAO owns column-level details.

### `Statistic` (Interface)

**File:** `src/main/java/com/diamonddagger590/mccore/statistic/Statistic.java`

```java
package com.diamonddagger590.mccore.statistic;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public interface Statistic {

    /**
     * Gets the unique {@link NamespacedKey} identifying this statistic.
     *
     * @return The unique {@link NamespacedKey} for this statistic.
     */
    @NotNull
    NamespacedKey getStatisticKey();

    /**
     * Gets the {@link StatisticType} of this statistic, defining what kind
     * of value it holds.
     *
     * @return The {@link StatisticType} of this statistic.
     */
    @NotNull
    StatisticType getStatisticType();

    /**
     * Gets the default value for this statistic when a player has no stored value.
     * <p>
     * The returned object must be compatible with the {@link StatisticType}:
     * <ul>
     *     <li>{@code INT} → {@link Integer}</li>
     *     <li>{@code LONG} → {@link Long}</li>
     *     <li>{@code DOUBLE} → {@link Double}</li>
     *     <li>{@code STRING} → {@link String}</li>
     *     <li>{@code TIMESTAMP} → {@link java.time.Instant}</li>
     *     <li>{@code SET_STRING} → {@link java.util.Set}{@code <String>}</li>
     * </ul>
     *
     * @return The default value for new players.
     */
    @NotNull
    Object getDefaultValue();

    /**
     * Gets the human-readable display name for this statistic.
     *
     * @return The display name for this statistic.
     */
    @NotNull
    String getDisplayName();

    /**
     * Gets a description of what this statistic tracks.
     *
     * @return A description of this statistic.
     */
    @NotNull
    String getDescription();

    /**
     * Gets the maximum number of elements allowed in a {@link StatisticType#SET_STRING} statistic.
     * A value of {@code -1} means unlimited.
     * <p>
     * Only meaningful for {@link StatisticType#SET_STRING}. Other types ignore this value.
     *
     * @return The maximum set size, or {@code -1} for unlimited.
     */
    default int getMaxSetSize() {
        return -1;
    }
}
```

### `SimpleStatistic` (Record)

**File:** `src/main/java/com/diamonddagger590/mccore/statistic/SimpleStatistic.java`

```java
package com.diamonddagger590.mccore.statistic;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * A convenience {@link Statistic} implementation for plugins that need basic statistic
 * definitions without creating their own class.
 *
 * @param statisticKey  The unique key for this statistic.
 * @param statisticType The data type of this statistic.
 * @param defaultValue  The default value for new players.
 * @param displayName   The human-readable name.
 * @param description   What this statistic tracks.
 * @param maxSetSize    Maximum set size for {@link StatisticType#SET_STRING}, or {@code -1} for unlimited.
 */
public record SimpleStatistic(
        @NotNull NamespacedKey statisticKey,
        @NotNull StatisticType statisticType,
        @NotNull Object defaultValue,
        @NotNull String displayName,
        @NotNull String description,
        int maxSetSize
) implements Statistic {

    /**
     * Convenience constructor for non-set statistics (maxSetSize defaults to -1).
     */
    public SimpleStatistic(
            @NotNull NamespacedKey statisticKey,
            @NotNull StatisticType statisticType,
            @NotNull Object defaultValue,
            @NotNull String displayName,
            @NotNull String description
    ) {
        this(statisticKey, statisticType, defaultValue, displayName, description, -1);
    }

    @Override
    @NotNull
    public NamespacedKey getStatisticKey() {
        return statisticKey;
    }

    @Override
    @NotNull
    public StatisticType getStatisticType() {
        return statisticType;
    }

    @Override
    @NotNull
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    @Override
    @NotNull
    public String getDescription() {
        return description;
    }

    @Override
    public int getMaxSetSize() {
        return maxSetSize;
    }
}
```

### `StatisticEntry` (Record)

**File:** `src/main/java/com/diamonddagger590/mccore/statistic/StatisticEntry.java`

A data carrier for deserialized statistic data from the database. Used as the transfer object between `PlayerStatisticDAO` and `PlayerStatisticData`. Provides type-safe getters that throw `ClassCastException` if the caller uses the wrong accessor for the entry's type.

```java
package com.diamonddagger590.mccore.statistic;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Set;

/**
 * A data carrier for a single statistic value as stored in or loaded from the database.
 *
 * @param key   The {@link NamespacedKey} of the statistic.
 * @param type  The {@link StatisticType} of the statistic.
 * @param value The deserialized value.
 */
public record StatisticEntry(
        @NotNull NamespacedKey key,
        @NotNull StatisticType type,
        @NotNull Object value
) {

    /**
     * Returns the value as an {@code int}.
     *
     * @return The value cast to {@code int}.
     * @throws ClassCastException if the value is not an {@link Integer}.
     */
    public int getAsInt() {
        return (Integer) value;
    }

    /**
     * Returns the value as a {@code long}.
     *
     * @return The value cast to {@code long}.
     * @throws ClassCastException if the value is not a {@link Long}.
     */
    public long getAsLong() {
        return (Long) value;
    }

    /**
     * Returns the value as a {@code double}.
     *
     * @return The value cast to {@code double}.
     * @throws ClassCastException if the value is not a {@link Double}.
     */
    public double getAsDouble() {
        return (Double) value;
    }

    /**
     * Returns the value as a {@link String}.
     *
     * @return The value cast to {@link String}.
     * @throws ClassCastException if the value is not a {@link String}.
     */
    @NotNull
    public String getAsString() {
        return (String) value;
    }

    /**
     * Returns the value as an {@link Instant}.
     *
     * @return The value cast to {@link Instant}.
     * @throws ClassCastException if the value is not an {@link Instant}.
     */
    @NotNull
    public Instant getAsTimestamp() {
        return (Instant) value;
    }

    /**
     * Returns the value as a {@code Set<String>}.
     *
     * @return The value cast to {@code Set<String>}.
     * @throws ClassCastException if the value is not a {@link Set}.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public Set<String> getAsSetString() {
        return (Set<String>) value;
    }
}
```

**Type-safe getters:** Each getter performs a direct cast and throws `ClassCastException` if the caller uses the wrong accessor for the entry's `StatisticType`. This is intentional — callers know the type at registration time and should use the matching getter. The exception makes misuse obvious at development time rather than silently returning a wrong value.

---

## Registry

### `StatisticRegistry`

**File:** `src/main/java/com/diamonddagger590/mccore/statistic/StatisticRegistry.java`

Follows the exact pattern of `PlayerSettingRegistry` — a `HashMap<NamespacedKey, Statistic>` keyed by the statistic's `NamespacedKey`.

```java
package com.diamonddagger590.mccore.statistic;

import com.diamonddagger590.mccore.registry.Registry;
import com.google.common.collect.ImmutableSet;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A registry for all {@link Statistic} definitions. Statistics are registered once during
 * plugin bootstrap and are not unregistered during runtime.
 */
public final class StatisticRegistry implements Registry<Statistic> {

    private final Map<NamespacedKey, Statistic> statistics;

    public StatisticRegistry() {
        this.statistics = new HashMap<>();
    }

    @Override
    public void register(@NotNull Statistic statistic) {
        if (statistics.containsKey(statistic.getStatisticKey())) {
            throw new IllegalArgumentException(
                    "Statistic already registered: " + statistic.getStatisticKey()
            );
        }
        statistics.put(statistic.getStatisticKey(), statistic);
    }

    @Override
    public boolean registered(@NotNull Statistic statistic) {
        return statistics.containsKey(statistic.getStatisticKey());
    }

    /**
     * Gets a {@link Statistic} by its {@link NamespacedKey}.
     *
     * @param key The key to look up.
     * @return An {@link Optional} containing the statistic, or empty if not registered.
     */
    @NotNull
    public Optional<Statistic> getStatistic(@NotNull NamespacedKey key) {
        return Optional.ofNullable(statistics.get(key));
    }

    /**
     * Gets all registered {@link Statistic} definitions.
     *
     * @return An {@link ImmutableSet} of all registered statistics.
     */
    @NotNull
    public Set<Statistic> getRegisteredStatistics() {
        return ImmutableSet.copyOf(statistics.values());
    }

    /**
     * Gets all registered {@link NamespacedKey}s.
     *
     * @return An {@link ImmutableSet} of all registered statistic keys.
     */
    @NotNull
    public Set<NamespacedKey> getRegisteredStatisticKeys() {
        return ImmutableSet.copyOf(statistics.keySet());
    }
}
```

**Duplicate detection:** Unlike `PlayerSettingRegistry`, `StatisticRegistry.register()` throws `IllegalArgumentException` if a key is already registered. This is intentional — duplicate statistic keys indicate a plugin configuration error (e.g., two plugins accidentally using the same namespace), and failing fast at bootstrap is better than silently overwriting.

### `RegistryKey` Change

**File:** `src/main/java/com/diamonddagger590/mccore/registry/RegistryKey.java`

Add one new constant:

```java
RegistryKey<StatisticRegistry> STATISTIC = create(StatisticRegistry.class);
```

Add the import:

```java
import com.diamonddagger590.mccore.statistic.StatisticRegistry;
```

---

## Per-Player Data

### `PlayerStatisticData`

**File:** `src/main/java/com/diamonddagger590/mccore/statistic/PlayerStatisticData.java`

This is the largest new class. It holds all statistic values for a single player, provides typed accessors, fires events on mutation, and tracks dirty state for efficient persistence.

#### Internal Storage

```java
private final UUID uuid;
private final ConcurrentHashMap<NamespacedKey, Object> values;
private final Set<NamespacedKey> dirtyKeys;
```

- `values` — the live stat values. Uses `ConcurrentHashMap` for thread-safe reads.
- `dirtyKeys` — tracks which keys have been modified since the last save. This is a `ConcurrentHashMap.newKeySet()` (backed by `ConcurrentHashMap<NamespacedKey, Boolean>`).

#### Constructor

```java
public PlayerStatisticData(@NotNull UUID uuid) {
    this.uuid = uuid;
    this.values = new ConcurrentHashMap<>();
    this.dirtyKeys = ConcurrentHashMap.newKeySet();
}
```

No statistic values are populated at construction time. Values are populated either via `loadFromDatabase()` during player load, or lazily when a mutator is first called for a key.

#### Typed Getters

Each getter retrieves from `values`, falling back to the default from `StatisticRegistry` if not present. If the key is not registered in the registry, returns empty.

```java
@NotNull
public OptionalLong getLongValue(@NotNull NamespacedKey key) {
    Object value = values.get(key);
    if (value instanceof Long l) {
        return OptionalLong.of(l);
    }
    // Fall back to default from registry
    return getRegisteredStatistic(key)
            .filter(s -> s.getStatisticType() == StatisticType.LONG)
            .map(s -> OptionalLong.of((Long) s.getDefaultValue()))
            .orElse(OptionalLong.empty());
}
```

The same pattern applies for `getIntValue()` (returns `OptionalInt`), `getDoubleValue()` (returns `OptionalDouble`), `getStringValue()` (returns `Optional<String>`), `getTimestampValue()` (returns `Optional<Instant>`), and `getSetValue()` (returns `Optional<Set<String>>`).

A generic `getValue(@NotNull NamespacedKey key): Optional<Object>` getter is also provided for callers that don't need type safety.

**Default value behavior:** Getters return the `Statistic.getDefaultValue()` when the key has no stored value. This means new players get meaningful defaults without any database rows being created. The default is *not* written to the `values` map — it's returned on-the-fly. This avoids marking a key as dirty just because it was read.

#### Private Helper

```java
@NotNull
private Optional<Statistic> getRegisteredStatistic(@NotNull NamespacedKey key) {
    return RegistryAccess.registryAccess()
            .registry(RegistryKey.STATISTIC)
            .getStatistic(key);
}
```

#### Mutators

All mutators follow this flow:

1. Resolve the `Statistic` from the registry (throw `StatisticNotRegisteredException` if not registered)
2. Compute the old value (from `values` map, or the statistic's default)
3. Compute the new value
4. Create and fire `StatisticModifyEvent`
5. If cancelled, return without applying
6. Read back `getNewValue()` from the event (listeners may have adjusted it)
7. Write to `values` map
8. Add key to `dirtyKeys`
9. Fire `PostStatisticModifyEvent`

**Thread safety for numeric mutators:** `incrementLong`, `incrementInt`, `incrementDouble`, `setMaxLong`, `setMaxInt`, `setMaxDouble` wrap steps 2-8 in a `synchronized` block keyed on the `NamespacedKey` instance retrieved from the registered `Statistic`:

```java
public void incrementLong(@NotNull NamespacedKey key, long delta) {
    Statistic statistic = getRegisteredStatisticOrThrow(key);
    Object syncKey = statistic.getStatisticKey();
    synchronized (syncKey) {
        long oldValue = getLongValue(key).orElse(0L);
        long newValue = oldValue + delta;
        // fire pre-event, apply, fire post-event...
    }
}
```

**Why synchronize on `statistic.getStatisticKey()`?** `NamespacedKey` instances from the registry are stable references (registered once, never replaced). Synchronizing on them gives per-key granularity without needing a `ConcurrentHashMap<NamespacedKey, Object>` of lock objects.

**`bulkIncrementLong`**: Identical to `incrementLong` except the `ModificationType` is `INCREMENT` and the delta is the full accumulated amount. There is no difference in event firing — one pre-event, one post-event. The method exists to make caller intent explicit: "I accumulated a count and am applying it all at once."

```java
public void bulkIncrementLong(@NotNull NamespacedKey key, long delta) {
    // Identical implementation to incrementLong — the semantic difference is caller intent.
    incrementLong(key, delta);
}
```

**Conditional mutators:**

- `setMaxLong(key, value)` — only updates if `value > current`. If `current` doesn't exist, treats it as the default value. Fires events only if the value changes.
- `setMaxInt(key, value)` / `setMaxDouble(key, value)` — same pattern.
- `setTimestampIfAbsent(key, instant)` — only sets if the key has no stored value (not even a default that was written). Fires events only if a value was actually set.

**Set mutators:**

- `addToSet(key, element)` — retrieves the current `Set<String>` (or creates a new `LinkedHashSet` from the default). If the element is new, enforces `getMaxSetSize()` — if at capacity, removes the oldest entry (iteration order of `LinkedHashSet`). Fires events only if the set changed. Returns `true` if the element was new.
- `removeFromSet(key, element)` — removes from the set. Fires events only if the element was present. Returns `true` if the element was removed.

**Set storage:** `SET_STRING` values are stored internally as `LinkedHashSet<String>`. `LinkedHashSet` preserves insertion order, which is needed for the `getMaxSetSize()` eviction policy (evict oldest). The set returned by `getSetValue()` is an unmodifiable view (`Collections.unmodifiableSet()`). Callers must use `addToSet` / `removeFromSet` to mutate.

#### Event Firing

Mutators create the event, call `Bukkit.getPluginManager().callEvent()`, and check `isCancelled()`. This follows the exact pattern used by `CorePlayer.setPlayerSetting()`.

```java
private boolean firePreEvent(
        @NotNull CorePlayer player,
        @NotNull Statistic statistic,
        @NotNull Object oldValue,
        @NotNull Object newValue,
        @NotNull ModificationType modificationType,
        @NotNull StatisticModifyEvent outEvent
) {
    Bukkit.getPluginManager().callEvent(outEvent);
    return !outEvent.isCancelled();
}
```

**Resolving the `CorePlayer`:** `PlayerStatisticData` holds the player's `UUID`. To fire events, it needs the `CorePlayer` reference. Rather than storing a `CorePlayer` reference (which would create a circular dependency since `CorePlayer` holds `PlayerStatisticData`), the mutator looks up the player via `PlayerManager`:

```java
@NotNull
private CorePlayer getCorePlayer() {
    return CorePlugin.getInstance().registryAccess()
            .registry(RegistryKey.MANAGER)
            .manager(CoreManagerKey.CORE_PLAYER_MANAGER)
            .getPlayer(uuid)
            .orElseThrow(() -> new CorePlayerOfflineException(uuid));
}
```

This lookup is cheap — `PlayerManager` stores players in a `HashMap<UUID, CorePlayer>`.

#### Lifecycle Methods

```java
/**
 * Populates this data from database-loaded entries. Called during player load.
 * Overwrites any existing values and clears the dirty set.
 */
public void loadFromDatabase(@NotNull Map<NamespacedKey, StatisticEntry> entries) {
    values.clear();
    dirtyKeys.clear();
    for (var entry : entries.entrySet()) {
        values.put(entry.getKey(), entry.getValue().value());
    }
}
```

```java
/**
 * Returns {@code true} if any statistic has been modified since the last save.
 */
public boolean isDirty() {
    return !dirtyKeys.isEmpty();
}
```

```java
/**
 * Gets only the modified entries for efficient delta saves.
 * Each returned entry includes the current value and its type from the registry.
 */
@NotNull
public Map<NamespacedKey, StatisticEntry> getModifiedEntries() {
    Map<NamespacedKey, StatisticEntry> modified = new HashMap<>();
    for (NamespacedKey key : dirtyKeys) {
        Object value = values.get(key);
        if (value == null) {
            continue;
        }
        getRegisteredStatistic(key).ifPresent(statistic ->
                modified.put(key, new StatisticEntry(key, statistic.getStatisticType(), value))
        );
    }
    return modified;
}
```

```java
/**
 * Clears the dirty set. Called after a successful save transaction.
 */
public void markClean() {
    dirtyKeys.clear();
}
```

---

## Events

### `ModificationType` (Enum)

**File:** `src/main/java/com/diamonddagger590/mccore/event/statistic/ModificationType.java`

```java
package com.diamonddagger590.mccore.event.statistic;

/**
 * Describes the type of modification being made to a statistic.
 */
public enum ModificationType {
    SET,
    INCREMENT,
    ADD_TO_SET,
    REMOVE_FROM_SET,
    SET_MAX,
    SET_IF_ABSENT
}
```

### `StatisticModifyEvent`

**File:** `src/main/java/com/diamonddagger590/mccore/event/statistic/StatisticModifyEvent.java`

```java
package com.diamonddagger590.mccore.event.statistic;

import com.diamonddagger590.mccore.event.player.CorePlayerEvent;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.statistic.Statistic;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired before a statistic value is modified. Cancellable — listeners can prevent
 * the change or adjust the new value via {@link #setNewValue(Object)}.
 */
public class StatisticModifyEvent extends CorePlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final NamespacedKey statisticKey;
    private final Statistic statistic;
    private final Object oldValue;
    private Object newValue;
    private final ModificationType modificationType;
    private boolean cancelled;

    public StatisticModifyEvent(
            @NotNull CorePlayer corePlayer,
            @NotNull NamespacedKey statisticKey,
            @NotNull Statistic statistic,
            @NotNull Object oldValue,
            @NotNull Object newValue,
            @NotNull ModificationType modificationType
    ) {
        super(corePlayer);
        this.statisticKey = statisticKey;
        this.statistic = statistic;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.modificationType = modificationType;
        this.cancelled = false;
    }

    @NotNull
    public NamespacedKey getStatisticKey() {
        return statisticKey;
    }

    @NotNull
    public Statistic getStatistic() {
        return statistic;
    }

    @NotNull
    public Object getOldValue() {
        return oldValue;
    }

    @NotNull
    public Object getNewValue() {
        return newValue;
    }

    /**
     * Adjusts the new value that will be applied. Listeners can use this to
     * apply multipliers or clamp values.
     *
     * @param newValue The adjusted value.
     */
    public void setNewValue(@NotNull Object newValue) {
        this.newValue = newValue;
    }

    @NotNull
    public ModificationType getModificationType() {
        return modificationType;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
```

### `PostStatisticModifyEvent`

**File:** `src/main/java/com/diamonddagger590/mccore/event/statistic/PostStatisticModifyEvent.java`

Same fields as `StatisticModifyEvent` minus `setCancelled` / `Cancellable`. The `newValue` is the final value after any listener adjustments from the pre-event.

```java
package com.diamonddagger590.mccore.event.statistic;

import com.diamonddagger590.mccore.event.player.CorePlayerEvent;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.statistic.Statistic;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired after a statistic value has been successfully modified. Not cancellable.
 * Used by reactive systems (e.g., future achievements) to check thresholds.
 * <p>
 * This event only fires if the pre-event ({@link StatisticModifyEvent}) was not cancelled
 * and the value actually changed.
 */
public class PostStatisticModifyEvent extends CorePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    private final NamespacedKey statisticKey;
    private final Statistic statistic;
    private final Object oldValue;
    private final Object newValue;
    private final ModificationType modificationType;

    public PostStatisticModifyEvent(
            @NotNull CorePlayer corePlayer,
            @NotNull NamespacedKey statisticKey,
            @NotNull Statistic statistic,
            @NotNull Object oldValue,
            @NotNull Object newValue,
            @NotNull ModificationType modificationType
    ) {
        super(corePlayer);
        this.statisticKey = statisticKey;
        this.statistic = statistic;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.modificationType = modificationType;
    }

    @NotNull
    public NamespacedKey getStatisticKey() {
        return statisticKey;
    }

    @NotNull
    public Statistic getStatistic() {
        return statistic;
    }

    @NotNull
    public Object getOldValue() {
        return oldValue;
    }

    @NotNull
    public Object getNewValue() {
        return newValue;
    }

    @NotNull
    public ModificationType getModificationType() {
        return modificationType;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
```

---

## Persistence

### `PlayerStatisticDAO`

**File:** `src/main/java/com/diamonddagger590/mccore/database/table/impl/PlayerStatisticDAO.java`

Follows the exact pattern of `PlayerSettingDAO`: static methods, `Connection` as first parameter, `PreparedStatement`-based CRUD.

#### Constants

```java
private static final String TABLE_NAME = "core_player_statistics";
private static final int CURRENT_TABLE_VERSION = 1;
```

#### Table Creation

```java
public static boolean attemptCreateTable(@NotNull Connection connection, @NotNull Database database) {
    if (database.tableExists(connection, TABLE_NAME)) {
        return false;
    }
    try (PreparedStatement statement = connection.prepareStatement(
            "CREATE TABLE `" + TABLE_NAME + "` (" +
            "`uuid` VARCHAR(36) NOT NULL, " +
            "`statistic_key` VARCHAR(256) NOT NULL, " +
            "`stat_type` VARCHAR(32) NOT NULL, " +
            "`int_value` INTEGER, " +
            "`long_value` BIGINT, " +
            "`double_value` DOUBLE, " +
            "`string_value` TEXT, " +
            "`timestamp_value` BIGINT, " +
            "PRIMARY KEY (`uuid`, `statistic_key`)" +
            ");"
    )) {
        statement.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }

    try (PreparedStatement statement = connection.prepareStatement(
            "CREATE INDEX IF NOT EXISTS idx_core_stats_uuid ON " + TABLE_NAME + " (uuid)"
    )) {
        statement.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
    return true;
}
```

#### Table Update

```java
public static void updateTable(@NotNull Connection connection) {
    int lastStoredVersion = TableVersionHistoryDAO.getLatestVersion(connection, TABLE_NAME);
    if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
        return;
    }
    if (lastStoredVersion == 0) {
        TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
    }
}
```

#### Load All Player Statistics

```java
@NotNull
public static Map<NamespacedKey, StatisticEntry> getAllPlayerStatistics(
        @NotNull Connection connection,
        @NotNull UUID playerUUID
) {
    Map<NamespacedKey, StatisticEntry> entries = new HashMap<>();
    try (PreparedStatement statement = connection.prepareStatement(
            "SELECT statistic_key, stat_type, int_value, long_value, double_value, " +
            "string_value, timestamp_value FROM " + TABLE_NAME + " WHERE uuid = ?"
    )) {
        statement.setString(1, playerUUID.toString());
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String keyString = rs.getString("statistic_key");
                NamespacedKey key = NamespacedKey.fromString(keyString);
                if (key == null) {
                    // Log warning and skip — corrupt key in database
                    CorePlugin.getInstance().getLogger().warning(
                            "Skipping statistic with invalid key: " + keyString
                    );
                    continue;
                }
                StatisticType type = StatisticType.valueOf(rs.getString("stat_type"));
                Object value = readValueFromResultSet(rs, type);
                entries.put(key, new StatisticEntry(key, type, value));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return entries;
}
```

#### `readValueFromResultSet` (private helper)

```java
@NotNull
private static Object readValueFromResultSet(
        @NotNull ResultSet rs,
        @NotNull StatisticType type
) throws SQLException {
    return switch (type) {
        case INT -> rs.getInt("int_value");
        case LONG -> rs.getLong("long_value");
        case DOUBLE -> rs.getDouble("double_value");
        case STRING -> rs.getString("string_value");
        case TIMESTAMP -> Instant.ofEpochMilli(rs.getLong("timestamp_value"));
        case SET_STRING -> deserializeStringSet(rs.getString("string_value"));
    };
}
```

#### `deserializeStringSet` / `serializeStringSet` (private helpers)

`SET_STRING` values are serialized as JSON arrays using simple string manipulation — no external JSON library needed.

```java
@NotNull
private static Set<String> deserializeStringSet(@NotNull String json) {
    // Expected format: ["value1","value2","value3"]
    // Use a LinkedHashSet to preserve insertion order for maxSetSize eviction
    Set<String> result = new LinkedHashSet<>();
    if (json.equals("[]") || json.isEmpty()) {
        return result;
    }
    // Strip brackets
    String inner = json.substring(1, json.length() - 1);
    // Split by ","  (each element is quoted)
    for (String element : inner.split(",")) {
        String trimmed = element.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            result.add(trimmed.substring(1, trimmed.length() - 1));
        }
    }
    return result;
}

@NotNull
private static String serializeStringSet(@NotNull Set<String> set) {
    StringBuilder sb = new StringBuilder("[");
    var iterator = set.iterator();
    while (iterator.hasNext()) {
        sb.append("\"").append(iterator.next().replace("\"", "\\\"")).append("\"");
        if (iterator.hasNext()) {
            sb.append(",");
        }
    }
    sb.append("]");
    return sb.toString();
}
```

**Note:** The simple JSON serialization escapes `"` within values using `\"`. This covers the common case (set elements are typically simple strings like biome names or player UUIDs). A full JSON library is not warranted for this single use case.

#### Load Single Player Statistic

```java
@NotNull
public static Optional<StatisticEntry> getPlayerStatistic(
        @NotNull Connection connection,
        @NotNull UUID playerUUID,
        @NotNull NamespacedKey key
) {
    try (PreparedStatement statement = connection.prepareStatement(
            "SELECT stat_type, int_value, long_value, double_value, " +
            "string_value, timestamp_value FROM " + TABLE_NAME +
            " WHERE uuid = ? AND statistic_key = ?"
    )) {
        statement.setString(1, playerUUID.toString());
        statement.setString(2, key.toString());
        try (ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                StatisticType type = StatisticType.valueOf(rs.getString("stat_type"));
                Object value = readValueFromResultSet(rs, type);
                return Optional.of(new StatisticEntry(key, type, value));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return Optional.empty();
}
```

#### Save

```java
@NotNull
public static List<PreparedStatement> savePlayerStatistics(
        @NotNull Connection connection,
        @NotNull UUID playerUUID,
        @NotNull Map<NamespacedKey, StatisticEntry> entries
) {
    return entries.values().stream()
            .map(entry -> savePlayerStatistic(connection, playerUUID, entry))
            .toList();
}

@NotNull
public static PreparedStatement savePlayerStatistic(
        @NotNull Connection connection,
        @NotNull UUID playerUUID,
        @NotNull StatisticEntry entry
) {
    try {
        PreparedStatement statement = connection.prepareStatement(
                "REPLACE INTO " + TABLE_NAME +
                " (uuid, statistic_key, stat_type, int_value, long_value, " +
                "double_value, string_value, timestamp_value) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        );
        statement.setString(1, playerUUID.toString());
        statement.setString(2, entry.key().toString());
        statement.setString(3, entry.type().name());

        // Null all typed columns, then set the one that matches
        statement.setNull(4, java.sql.Types.INTEGER);
        statement.setNull(5, java.sql.Types.BIGINT);
        statement.setNull(6, java.sql.Types.DOUBLE);
        statement.setNull(7, java.sql.Types.VARCHAR);
        statement.setNull(8, java.sql.Types.BIGINT);

        switch (entry.type()) {
            case INT -> statement.setInt(4, (Integer) entry.value());
            case LONG -> statement.setLong(5, (Long) entry.value());
            case DOUBLE -> statement.setDouble(6, (Double) entry.value());
            case STRING -> statement.setString(7, (String) entry.value());
            case TIMESTAMP -> statement.setLong(8, ((Instant) entry.value()).toEpochMilli());
            case SET_STRING -> {
                @SuppressWarnings("unchecked")
                Set<String> set = (Set<String>) entry.value();
                statement.setString(7, serializeStringSet(set));
            }
        }
        return statement;
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
}
```

#### Delete

```java
@NotNull
public static PreparedStatement deletePlayerStatistic(
        @NotNull Connection connection,
        @NotNull UUID playerUUID,
        @NotNull NamespacedKey key
) {
    try {
        PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM " + TABLE_NAME + " WHERE uuid = ? AND statistic_key = ?"
        );
        statement.setString(1, playerUUID.toString());
        statement.setString(2, key.toString());
        return statement;
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
}
```

### Table Lifecycle Wiring

**`CreateCoreTablesFunction`** — add after the `PlayerSettingDAO.attemptCreateTable()` call:

```java
logger.log(Level.INFO, "Database Creation - Player Statistic DAO "
        + (PlayerStatisticDAO.attemptCreateTable(connection, database) ? "created a new table." : "already existed so skipping creation."));
```

**`UpdateCoreTablesFunction`** — add after the `PlayerSettingDAO.updateTable()` call:

```java
PlayerStatisticDAO.updateTable(connection);
```

---

## CorePlayer Integration

**File:** `src/main/java/com/diamonddagger590/mccore/player/CorePlayer.java`

Add field and getter:

```java
private final PlayerStatisticData statisticData;
```

In the constructor, after `this.playerSettings = new HashMap<>()`:

```java
this.statisticData = new PlayerStatisticData(uuid);
```

New method:

```java
/**
 * Gets the {@link PlayerStatisticData} for this player.
 *
 * @return The {@link PlayerStatisticData} for this player.
 */
@NotNull
public PlayerStatisticData getStatisticData() {
    return statisticData;
}
```

New import:

```java
import com.diamonddagger590.mccore.statistic.PlayerStatisticData;
```

**Cost:** An empty `PlayerStatisticData` is just an empty `ConcurrentHashMap` (~48 bytes) plus an empty `ConcurrentHashMap.newKeySet()` (~48 bytes). This is negligible even with thousands of online players.

---

## Caching

### `StatisticCacheKey` (Record)

**File:** `src/main/java/com/diamonddagger590/mccore/statistic/cache/StatisticCacheKey.java`

```java
package com.diamonddagger590.mccore.statistic.cache;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Composite cache key for offline statistic lookups.
 *
 * @param uuid The player's UUID.
 * @param key  The statistic's key.
 */
public record StatisticCacheKey(
        @NotNull UUID uuid,
        @NotNull NamespacedKey key
) {
}
```

### `StatisticCache`

**File:** `src/main/java/com/diamonddagger590/mccore/statistic/cache/StatisticCache.java`

```java
package com.diamonddagger590.mccore.statistic.cache;

import com.diamonddagger590.mccore.statistic.StatisticEntry;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * An optional Caffeine-backed cache for offline player statistic queries.
 * <p>
 * Downstream plugins construct and configure this cache programmatically.
 * If no cache is provided, offline queries go directly to the database.
 */
public class StatisticCache {

    private final Cache<StatisticCacheKey, StatisticEntry> cache;

    /**
     * Creates a new {@link StatisticCache}.
     *
     * @param maxSize    Maximum number of entries.
     * @param ttlSeconds Time-to-live in seconds for each entry.
     */
    public StatisticCache(long maxSize, long ttlSeconds) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Gets a cached {@link StatisticEntry} for an offline player.
     *
     * @param uuid The player's UUID.
     * @param key  The statistic key.
     * @return An {@link Optional} containing the cached entry, or empty on cache miss.
     */
    @NotNull
    public Optional<StatisticEntry> get(@NotNull UUID uuid, @NotNull NamespacedKey key) {
        return Optional.ofNullable(cache.getIfPresent(new StatisticCacheKey(uuid, key)));
    }

    /**
     * Puts a {@link StatisticEntry} into the cache.
     *
     * @param uuid  The player's UUID.
     * @param key   The statistic key.
     * @param entry The entry to cache.
     */
    public void put(@NotNull UUID uuid, @NotNull NamespacedKey key, @NotNull StatisticEntry entry) {
        cache.put(new StatisticCacheKey(uuid, key), entry);
    }

    /**
     * Invalidates all cached entries for a player. Called when a player joins
     * and live data takes over.
     *
     * @param uuid The player's UUID.
     */
    public void invalidate(@NotNull UUID uuid) {
        cache.asMap().keySet().removeIf(cacheKey -> cacheKey.uuid().equals(uuid));
    }

    /**
     * Invalidates a specific cached entry. Called after a stat is saved.
     *
     * @param uuid The player's UUID.
     * @param key  The statistic key.
     */
    public void invalidate(@NotNull UUID uuid, @NotNull NamespacedKey key) {
        cache.invalidate(new StatisticCacheKey(uuid, key));
    }

    /**
     * Gets the current number of entries in the cache.
     *
     * @return The number of cached entries.
     */
    public long size() {
        return cache.estimatedSize();
    }
}
```

### Caffeine Dependency

Add to `build.gradle.kts`:

```kotlin
val caffeineVersion = "3.1.8"
api("com.github.ben-manes.caffeine:caffeine:$caffeineVersion")
```

---

## Commands

McCore provides abstract/base command classes that downstream plugins compose into their own command tree. Commands use Cloud annotations.

The HLD defines 5 base commands. These are **not** registered by McCore itself — they are composed by downstream plugins (e.g., McRPG mounts them under `/mcrpg statistic ...`).

### Design Approach

Each command is a class with methods annotated with `@Command` / `@Permission` etc. from Cloud's annotation API. Downstream plugins pass their own command path prefix when constructing these commands.

All commands take a `statistic` argument that is resolved from the `StatisticRegistry`. A custom Cloud parser/suggestion provider (`StatisticKeyParser`) maps string input to `NamespacedKey` and validates against the registry.

### File List

| File | Package | Description |
|------|---------|-------------|
| `StatisticKeyParser.java` | `command.statistic` | Cloud argument parser for `NamespacedKey` statistic arguments |
| `StatisticCommands.java` | `command.statistic` | Single class containing all 5 base statistic command methods |

### `StatisticKeyParser`

**File:** `src/main/java/com/diamonddagger590/mccore/command/statistic/StatisticKeyParser.java`

A Cloud `ArgumentParser<CommandSourceStack, NamespacedKey>` that:
1. Parses the input string as a `NamespacedKey`
2. Validates it exists in `StatisticRegistry`
3. Provides tab completion from `StatisticRegistry.getRegisteredStatisticKeys()`

### `StatisticCommands`

**File:** `src/main/java/com/diamonddagger590/mccore/command/statistic/StatisticCommands.java`

Contains annotated methods for: `view`, `list`, `set`, `modify`, `reset`. Downstream plugins register this class with their `AnnotationParser`.

The exact command method signatures are deferred to implementation — they depend on Cloud annotation API specifics (parameter injection, `CommandSourceStack` resolution, etc.). The important contract is:

- **view** — reads `PlayerStatisticData` for online players, `PlayerStatisticDAO` for offline. Displays the formatted value.
- **list** — iterates `StatisticRegistry.getRegisteredStatistics()` and displays key + display name.
- **set** — parses the value string based on `StatisticType`, calls `PlayerStatisticData.setValue()`.
- **modify** — parses the delta, calls `incrementInt/Long/Double()`. Only for numeric types.
- **reset** — requires confirmation via `ConfirmationManager`, then calls `PlayerStatisticData.setValue(key, statistic.getDefaultValue())`.

---

## Exceptions

### `StatisticNotRegisteredException`

**File:** `src/main/java/com/diamonddagger590/mccore/exception/statistic/StatisticNotRegisteredException.java`

Follows the same pattern as `SettingNotRegisteredException`:

```java
package com.diamonddagger590.mccore.exception.statistic;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when a {@link com.diamonddagger590.mccore.statistic.Statistic} is expected
 * to be registered but is not found in the {@link com.diamonddagger590.mccore.statistic.StatisticRegistry}.
 */
public class StatisticNotRegisteredException extends RuntimeException {

    private final NamespacedKey statisticKey;

    public StatisticNotRegisteredException(@NotNull NamespacedKey statisticKey) {
        this.statisticKey = statisticKey;
    }

    /**
     * Gets the {@link NamespacedKey} of the unregistered statistic.
     *
     * @return The {@link NamespacedKey} that was not found.
     */
    @NotNull
    public NamespacedKey getStatisticKey() {
        return statisticKey;
    }

    @Override
    public String getMessage() {
        return "A statistic with key " + statisticKey + " was not found in the StatisticRegistry.";
    }
}
```

---

## Test Fixtures & Unit Tests

### Test Fixture Change

**File:** `src/testFixtures/java/com/diamonddagger590/mccore/testing/RegistryResetExtension.java`

Add `StatisticRegistry` registration in `setupRegistry()`:

```java
RegistryAccess.registryAccess().register(new StatisticRegistry());
```

### Unit Tests

All tests are under `src/test/java/com/diamonddagger590/mccore/`.

#### `StatisticTypeTest`

**File:** `statistic/StatisticTypeTest.java`

- Verify all 6 enum values exist
- Verify `valueOf()` round-trips for each type name
- Verify `values()` returns exactly 6 entries

#### `SimpleStatisticTest`

**File:** `statistic/SimpleStatisticTest.java`

- Record construction with all fields
- Convenience constructor defaults `maxSetSize` to `-1`
- Two `SimpleStatistic` instances with the same key are `equals()`
- Two instances with different keys are not `equals()`

#### `StatisticRegistryTest`

**File:** `statistic/StatisticRegistryTest.java`

Uses `RegistryResetExtension.setupRegistry()` / `resetRegistry()` in `@BeforeEach` / `@AfterEach`.

- `register()` succeeds for new key
- `register()` throws `IllegalArgumentException` for duplicate key
- `registered()` returns `true` for registered, `false` for unregistered
- `getStatistic()` returns `Optional` of registered, empty for unregistered
- `getRegisteredStatistics()` returns all registered stats
- `getRegisteredStatisticKeys()` returns all keys
- Registering multiple stats from different namespaces works

#### `PlayerStatisticDataTest`

**File:** `statistic/PlayerStatisticDataTest.java`

This is the largest test class. Uses `RegistryResetExtension` and registers test statistics in `@BeforeEach`.

**Note:** Tests that call mutators will need to either:
- Mock the `CorePlayer` lookup (since `PlayerStatisticData` calls `CorePlugin.getInstance()` to resolve the player), or
- Restructure event firing to be injectable/skippable in tests

The recommended approach is to make `PlayerStatisticData` accept an optional event-firing strategy (a `@FunctionalInterface`) that defaults to Bukkit event dispatch in production but can be replaced with a no-op or recording mock in tests. This avoids full Bukkit server bootstrapping in unit tests.

```java
@FunctionalInterface
interface StatisticEventDispatcher {
    void dispatch(@NotNull Event event);
}
```

`PlayerStatisticData` constructor gains an overload:

```java
// Production use — dispatches via Bukkit
public PlayerStatisticData(@NotNull UUID uuid) {
    this(uuid, event -> Bukkit.getPluginManager().callEvent(event));
}

// Test use — injectable dispatcher
PlayerStatisticData(@NotNull UUID uuid, @NotNull StatisticEventDispatcher dispatcher) {
    ...
}
```

The test-only constructor uses package-private visibility.

**Test cases:**

- **Typed getters:**
  - Each type returns correct value after `loadFromDatabase()`
  - Returns default value when key has no stored value
  - Returns empty when key is not registered
  - Type mismatch returns empty (e.g., `getIntValue()` on a LONG stat)

- **Mutators (setValue):**
  - `setValue()` stores value and marks dirty
  - Pre-event cancellation prevents value change
  - Pre-event `setNewValue()` adjusts the applied value

- **Increment operations:**
  - `incrementLong()` adds delta to current value
  - `incrementLong()` on unset key starts from default
  - `incrementInt()` and `incrementDouble()` work correctly
  - Increment fires one pre-event and one post-event

- **Conditional mutators:**
  - `setMaxLong()` updates when new > old
  - `setMaxLong()` does not update when new <= old
  - `setMaxLong()` fires no event when value doesn't change
  - `setTimestampIfAbsent()` sets on first call
  - `setTimestampIfAbsent()` does not overwrite on second call

- **Set operations:**
  - `addToSet()` adds element, returns `true`
  - `addToSet()` for existing element returns `false`, fires no event
  - `removeFromSet()` removes element, returns `true`
  - `removeFromSet()` for absent element returns `false`, fires no event
  - `addToSet()` enforces `getMaxSetSize()` — evicts oldest

- **Dirty tracking:**
  - `isDirty()` returns `false` after construction
  - `isDirty()` returns `false` after `loadFromDatabase()`
  - `isDirty()` returns `true` after mutation
  - `getModifiedEntries()` returns only mutated keys
  - `markClean()` clears dirty state

- **`loadFromDatabase()`:**
  - Populates values correctly
  - Clears previous values
  - Clears dirty state

- **Thread safety:** (concurrent test)
  - Multiple threads calling `incrementLong()` on the same key should produce the correct accumulated total

#### `PlayerStatisticDAOTest`

**File:** `database/table/impl/PlayerStatisticDAOTest.java`

DAO tests require a real database connection. Since McCore uses SQLite, tests can use an in-memory SQLite database.

**Setup:** Create an in-memory SQLite connection, call `PlayerStatisticDAO.attemptCreateTable()` and `TableVersionHistoryDAO.attemptCreateTable()` in `@BeforeEach`.

**Test cases:**

- `attemptCreateTable()` creates the table
- `attemptCreateTable()` returns `false` when table already exists
- `updateTable()` sets version in version history
- `savePlayerStatistic()` + `getPlayerStatistic()` round-trips for each `StatisticType`
- `savePlayerStatistics()` saves multiple stats, `getAllPlayerStatistics()` loads them all
- `getAllPlayerStatistics()` for unknown UUID returns empty map
- `deletePlayerStatistic()` removes the entry
- `SET_STRING` serialization/deserialization round-trip (empty set, single element, multiple elements)
- `SET_STRING` values containing quotes are properly escaped
- `TIMESTAMP` stores as epoch millis and deserializes back to `Instant`

#### `StatisticCacheTest`

**File:** `statistic/cache/StatisticCacheTest.java`

- `get()` returns empty on cache miss
- `put()` then `get()` returns the entry
- `invalidate(uuid)` removes all entries for that UUID
- `invalidate(uuid, key)` removes only that specific entry
- `size()` reflects current entry count

Note: TTL-based expiration tests are unreliable in unit tests due to timing. We verify TTL configuration is passed to Caffeine by constructing with a 1-second TTL and verifying `get()` returns empty after a brief `Thread.sleep()`. If this proves flaky, it can be removed — the TTL behavior is Caffeine's responsibility, not ours.

---

## Bootstrap Changes

### `CoreBootstrap.start()`

**File:** `src/main/java/com/diamonddagger590/mccore/bootstrap/CoreBootstrap.java`

Add after the existing `registryAccess.register(new PlayerSettingRegistry())` line:

```java
registryAccess.register(new StatisticRegistry());
```

New import:

```java
import com.diamonddagger590.mccore.statistic.StatisticRegistry;
```

---

## Implementation Order

The implementation should proceed in this order, with each step building on the previous:

### Step 1: Data Model (no dependencies)
1. `StatisticType`
2. `Statistic` interface
3. `SimpleStatistic` record
4. `StatisticEntry` record
5. `StatisticNotRegisteredException`
6. `StatisticTypeTest`
7. `SimpleStatisticTest`

### Step 2: Registry (depends on Step 1)
1. `StatisticRegistry`
2. Add `RegistryKey.STATISTIC`
3. Add `StatisticRegistry` to `CoreBootstrap.start()`
4. Update `RegistryResetExtension`
5. `StatisticRegistryTest`

### Step 3: Events (depends on Step 1)
1. `ModificationType`
2. `StatisticModifyEvent`
3. `PostStatisticModifyEvent`

### Step 4: Per-Player Data (depends on Steps 1-3)
1. `PlayerStatisticData`
2. Add `PlayerStatisticData` field to `CorePlayer`
3. `PlayerStatisticDataTest`

### Step 5: Persistence (depends on Steps 1-2)
1. `PlayerStatisticDAO`
2. Wire into `CreateCoreTablesFunction`
3. Wire into `UpdateCoreTablesFunction`
4. `PlayerStatisticDAOTest`

### Step 6: Caching (depends on Step 1)
1. Add Caffeine dependency to `build.gradle.kts`
2. `StatisticCacheKey`
3. `StatisticCache`
4. `StatisticCacheTest`

### Step 7: Commands (depends on Steps 1-4)
1. `StatisticKeyParser`
2. `StatisticCommands`

---

## Resolved Questions

1. **~~`FailSafeTransaction.executeTransaction()` returns `void`~~** — **Resolved: use `BatchTransaction`, not `FailSafeTransaction`.** Player statistics are independent writes where partial success is acceptable (one stat failing shouldn't lose the others). `BatchTransaction` commits whatever succeeds and logs individual failures. Calling `markClean()` unconditionally after `executeTransaction()` is safe because:
   - Successfully saved entries are already persisted — clearing their dirty flag is correct.
   - Failed entries will be re-dirtied on their next mutation and retried on the next save cycle.
   - Worst case (entry dirtied between save and `markClean()`): the entry is saved again next cycle with `REPLACE INTO`, which is idempotent.

   No changes to the `Transaction` API are needed.

2. **~~Caffeine version conflict~~** — **Resolved: add a relocation rule.** McCore's shadow JAR will relocate Caffeine to avoid classpath conflicts with downstream plugins that may shade their own copy:
   ```kotlin
   relocate("com.github.benmanes.caffeine", "com.diamonddagger590.mccore.caffeine")
   ```

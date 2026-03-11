package com.diamonddagger590.mccore.statistic;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.event.statistic.ModificationType;
import com.diamonddagger590.mccore.event.statistic.PostStatisticModifyEvent;
import com.diamonddagger590.mccore.event.statistic.StatisticModifyEvent;
import com.diamonddagger590.mccore.exception.statistic.StatisticNotRegisteredException;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.CoreManagerKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds all statistic values for a single player. Provides typed accessors,
 * fires events on mutation, and tracks dirty state for efficient persistence.
 */
public class PlayerStatisticData {

    /**
     * Strategy for dispatching statistic events. Allows tests to inject a no-op
     * or recording dispatcher without bootstrapping a Bukkit server.
     */
    @FunctionalInterface
    interface StatisticEventDispatcher {
        void dispatch(@NotNull Event event);
    }

    /**
     * Strategy for resolving the {@link CorePlayer} from the player's UUID.
     * Allows tests to inject a mock player without bootstrapping the full server.
     */
    @FunctionalInterface
    interface CorePlayerResolver {
        @NotNull
        CorePlayer resolve(@NotNull UUID uuid);
    }

    private final UUID uuid;
    private final ConcurrentHashMap<NamespacedKey, Object> values;
    private final Set<NamespacedKey> dirtyKeys;
    private final StatisticEventDispatcher dispatcher;
    private final CorePlayerResolver playerResolver;

    /**
     * Production constructor — dispatches events via Bukkit and resolves the
     * {@link CorePlayer} from the {@link com.diamonddagger590.mccore.player.PlayerManager}.
     *
     * @param uuid The player's UUID.
     */
    public PlayerStatisticData(@NotNull UUID uuid) {
        this(uuid,
                event -> Bukkit.getPluginManager().callEvent(event),
                id -> {
                    @SuppressWarnings("unchecked")
                    Optional<CorePlayer> opt = (Optional<CorePlayer>) (Optional<?>) CorePlugin.getInstance()
                            .registryAccess()
                            .registry(RegistryKey.MANAGER)
                            .manager(CoreManagerKey.CORE_PLAYER_MANAGER)
                            .getPlayer(id);
                    return opt.orElseThrow(() -> new IllegalStateException(
                            "CorePlayer with UUID " + id + " is not online and was expected to be."
                    ));
                }
        );
    }

    /**
     * Test constructor — allows injectable event dispatching and player resolution.
     */
    PlayerStatisticData(@NotNull UUID uuid,
                        @NotNull StatisticEventDispatcher dispatcher,
                        @NotNull CorePlayerResolver playerResolver) {
        this.uuid = uuid;
        this.values = new ConcurrentHashMap<>();
        this.dirtyKeys = ConcurrentHashMap.newKeySet();
        this.dispatcher = dispatcher;
        this.playerResolver = playerResolver;
    }

    /**
     * Gets the player's UUID.
     *
     * @return The player's UUID.
     */
    @NotNull
    public UUID getUUID() {
        return uuid;
    }

    // ── Typed Getters ──────────────────────────────────────────────────

    /**
     * Gets a statistic value as an {@code int}.
     *
     * @param key The statistic key.
     * @return The value, or empty if not registered or wrong type.
     */
    @NotNull
    public OptionalInt getIntValue(@NotNull NamespacedKey key) {
        Object value = values.get(key);
        if (value instanceof Integer i) {
            return OptionalInt.of(i);
        }
        return getRegisteredStatistic(key)
                .filter(s -> s.getStatisticType() == StatisticType.INT)
                .map(s -> OptionalInt.of((Integer) s.getDefaultValue()))
                .orElse(OptionalInt.empty());
    }

    /**
     * Gets a statistic value as a {@code long}.
     *
     * @param key The statistic key.
     * @return The value, or empty if not registered or wrong type.
     */
    @NotNull
    public OptionalLong getLongValue(@NotNull NamespacedKey key) {
        Object value = values.get(key);
        if (value instanceof Long l) {
            return OptionalLong.of(l);
        }
        return getRegisteredStatistic(key)
                .filter(s -> s.getStatisticType() == StatisticType.LONG)
                .map(s -> OptionalLong.of((Long) s.getDefaultValue()))
                .orElse(OptionalLong.empty());
    }

    /**
     * Gets a statistic value as a {@code double}.
     *
     * @param key The statistic key.
     * @return The value, or empty if not registered or wrong type.
     */
    @NotNull
    public OptionalDouble getDoubleValue(@NotNull NamespacedKey key) {
        Object value = values.get(key);
        if (value instanceof Double d) {
            return OptionalDouble.of(d);
        }
        return getRegisteredStatistic(key)
                .filter(s -> s.getStatisticType() == StatisticType.DOUBLE)
                .map(s -> OptionalDouble.of((Double) s.getDefaultValue()))
                .orElse(OptionalDouble.empty());
    }

    /**
     * Gets a statistic value as a {@link String}.
     *
     * @param key The statistic key.
     * @return The value, or empty if not registered or wrong type.
     */
    @NotNull
    public Optional<String> getStringValue(@NotNull NamespacedKey key) {
        Object value = values.get(key);
        if (value instanceof String s) {
            return Optional.of(s);
        }
        return getRegisteredStatistic(key)
                .filter(s -> s.getStatisticType() == StatisticType.STRING)
                .map(s -> (String) s.getDefaultValue());
    }

    /**
     * Gets a statistic value as an {@link Instant}.
     *
     * @param key The statistic key.
     * @return The value, or empty if not registered or wrong type.
     */
    @NotNull
    public Optional<Instant> getTimestampValue(@NotNull NamespacedKey key) {
        Object value = values.get(key);
        if (value instanceof Instant i) {
            return Optional.of(i);
        }
        return getRegisteredStatistic(key)
                .filter(s -> s.getStatisticType() == StatisticType.TIMESTAMP)
                .map(s -> (Instant) s.getDefaultValue());
    }

    /**
     * Gets a statistic value as a {@code Set<String>}. The returned set is unmodifiable.
     *
     * @param key The statistic key.
     * @return The value, or empty if not registered or wrong type.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public Optional<Set<String>> getSetValue(@NotNull NamespacedKey key) {
        Object value = values.get(key);
        if (value instanceof Set<?>) {
            return Optional.of(Collections.unmodifiableSet((Set<String>) value));
        }
        return getRegisteredStatistic(key)
                .filter(s -> s.getStatisticType() == StatisticType.SET_STRING)
                .map(s -> Collections.unmodifiableSet((Set<String>) s.getDefaultValue()));
    }

    /**
     * Gets a statistic value as a raw {@link Object}.
     *
     * @param key The statistic key.
     * @return The value, or empty if not stored and not registered.
     */
    @NotNull
    public Optional<Object> getValue(@NotNull NamespacedKey key) {
        Object value = values.get(key);
        if (value != null) {
            return Optional.of(value);
        }
        return getRegisteredStatistic(key).map(Statistic::getDefaultValue);
    }

    // ── Mutators ───────────────────────────────────────────────────────

    /**
     * Sets a statistic value directly.
     *
     * @param key      The statistic key.
     * @param newValue The new value.
     * @throws StatisticNotRegisteredException if the key is not registered.
     */
    public void setValue(@NotNull NamespacedKey key, @NotNull Object newValue) {
        Statistic statistic = getRegisteredStatisticOrThrow(key);
        Object oldValue = resolveCurrentValue(key, statistic);
        StatisticModifyEvent preEvent = new StatisticModifyEvent(
                playerResolver.resolve(uuid), key, statistic, oldValue, newValue, ModificationType.SET
        );
        dispatcher.dispatch(preEvent);
        if (preEvent.isCancelled()) {
            return;
        }
        Object finalValue = preEvent.getNewValue();
        values.put(key, finalValue);
        dirtyKeys.add(key);
        dispatcher.dispatch(new PostStatisticModifyEvent(
                playerResolver.resolve(uuid), key, statistic, oldValue, finalValue, ModificationType.SET
        ));
    }

    /**
     * Increments a {@link StatisticType#LONG} statistic by the given delta.
     *
     * @param key   The statistic key.
     * @param delta The amount to add (may be negative).
     * @throws StatisticNotRegisteredException if the key is not registered.
     */
    public void incrementLong(@NotNull NamespacedKey key, long delta) {
        Statistic statistic = getRegisteredStatisticOrThrow(key);
        Object syncKey = statistic.getStatisticKey();
        synchronized (syncKey) {
            long oldValue = getLongValue(key).orElse(0L);
            long newValue = oldValue + delta;
            StatisticModifyEvent preEvent = new StatisticModifyEvent(
                    playerResolver.resolve(uuid), key, statistic, oldValue, newValue, ModificationType.INCREMENT
            );
            dispatcher.dispatch(preEvent);
            if (preEvent.isCancelled()) {
                return;
            }
            long finalValue = (Long) preEvent.getNewValue();
            values.put(key, finalValue);
            dirtyKeys.add(key);
            dispatcher.dispatch(new PostStatisticModifyEvent(
                    playerResolver.resolve(uuid), key, statistic, oldValue, finalValue, ModificationType.INCREMENT
            ));
        }
    }

    /**
     * Increments a {@link StatisticType#LONG} statistic by the given accumulated delta.
     * Semantically identical to {@link #incrementLong(NamespacedKey, long)} — exists to
     * make caller intent explicit.
     *
     * @param key   The statistic key.
     * @param delta The accumulated amount to add.
     * @throws StatisticNotRegisteredException if the key is not registered.
     */
    public void bulkIncrementLong(@NotNull NamespacedKey key, long delta) {
        incrementLong(key, delta);
    }

    /**
     * Increments a {@link StatisticType#INT} statistic by the given delta.
     *
     * @param key   The statistic key.
     * @param delta The amount to add (may be negative).
     * @throws StatisticNotRegisteredException if the key is not registered.
     */
    public void incrementInt(@NotNull NamespacedKey key, int delta) {
        Statistic statistic = getRegisteredStatisticOrThrow(key);
        Object syncKey = statistic.getStatisticKey();
        synchronized (syncKey) {
            int oldValue = getIntValue(key).orElse(0);
            int newValue = oldValue + delta;
            StatisticModifyEvent preEvent = new StatisticModifyEvent(
                    playerResolver.resolve(uuid), key, statistic, oldValue, newValue, ModificationType.INCREMENT
            );
            dispatcher.dispatch(preEvent);
            if (preEvent.isCancelled()) {
                return;
            }
            int finalValue = (Integer) preEvent.getNewValue();
            values.put(key, finalValue);
            dirtyKeys.add(key);
            dispatcher.dispatch(new PostStatisticModifyEvent(
                    playerResolver.resolve(uuid), key, statistic, oldValue, finalValue, ModificationType.INCREMENT
            ));
        }
    }

    /**
     * Increments a {@link StatisticType#DOUBLE} statistic by the given delta.
     *
     * @param key   The statistic key.
     * @param delta The amount to add (may be negative).
     * @throws StatisticNotRegisteredException if the key is not registered.
     */
    public void incrementDouble(@NotNull NamespacedKey key, double delta) {
        Statistic statistic = getRegisteredStatisticOrThrow(key);
        Object syncKey = statistic.getStatisticKey();
        synchronized (syncKey) {
            double oldValue = getDoubleValue(key).orElse(0.0);
            double newValue = oldValue + delta;
            StatisticModifyEvent preEvent = new StatisticModifyEvent(
                    playerResolver.resolve(uuid), key, statistic, oldValue, newValue, ModificationType.INCREMENT
            );
            dispatcher.dispatch(preEvent);
            if (preEvent.isCancelled()) {
                return;
            }
            double finalValue = (Double) preEvent.getNewValue();
            values.put(key, finalValue);
            dirtyKeys.add(key);
            dispatcher.dispatch(new PostStatisticModifyEvent(
                    playerResolver.resolve(uuid), key, statistic, oldValue, finalValue, ModificationType.INCREMENT
            ));
        }
    }

    /**
     * Sets a {@link StatisticType#LONG} statistic to the given value only if it is
     * greater than the current value.
     *
     * @param key   The statistic key.
     * @param value The candidate new value.
     * @throws StatisticNotRegisteredException if the key is not registered.
     */
    public void setMaxLong(@NotNull NamespacedKey key, long value) {
        Statistic statistic = getRegisteredStatisticOrThrow(key);
        Object syncKey = statistic.getStatisticKey();
        synchronized (syncKey) {
            long oldValue = getLongValue(key).orElse((Long) statistic.getDefaultValue());
            if (value <= oldValue) {
                return;
            }
            StatisticModifyEvent preEvent = new StatisticModifyEvent(
                    playerResolver.resolve(uuid), key, statistic, oldValue, value, ModificationType.SET_MAX
            );
            dispatcher.dispatch(preEvent);
            if (preEvent.isCancelled()) {
                return;
            }
            long finalValue = (Long) preEvent.getNewValue();
            values.put(key, finalValue);
            dirtyKeys.add(key);
            dispatcher.dispatch(new PostStatisticModifyEvent(
                    playerResolver.resolve(uuid), key, statistic, oldValue, finalValue, ModificationType.SET_MAX
            ));
        }
    }

    /**
     * Sets a {@link StatisticType#INT} statistic to the given value only if it is
     * greater than the current value.
     *
     * @param key   The statistic key.
     * @param value The candidate new value.
     * @throws StatisticNotRegisteredException if the key is not registered.
     */
    public void setMaxInt(@NotNull NamespacedKey key, int value) {
        Statistic statistic = getRegisteredStatisticOrThrow(key);
        Object syncKey = statistic.getStatisticKey();
        synchronized (syncKey) {
            int oldValue = getIntValue(key).orElse((Integer) statistic.getDefaultValue());
            if (value <= oldValue) {
                return;
            }
            StatisticModifyEvent preEvent = new StatisticModifyEvent(
                    playerResolver.resolve(uuid), key, statistic, oldValue, value, ModificationType.SET_MAX
            );
            dispatcher.dispatch(preEvent);
            if (preEvent.isCancelled()) {
                return;
            }
            int finalValue = (Integer) preEvent.getNewValue();
            values.put(key, finalValue);
            dirtyKeys.add(key);
            dispatcher.dispatch(new PostStatisticModifyEvent(
                    playerResolver.resolve(uuid), key, statistic, oldValue, finalValue, ModificationType.SET_MAX
            ));
        }
    }

    /**
     * Sets a {@link StatisticType#DOUBLE} statistic to the given value only if it is
     * greater than the current value.
     *
     * @param key   The statistic key.
     * @param value The candidate new value.
     * @throws StatisticNotRegisteredException if the key is not registered.
     */
    public void setMaxDouble(@NotNull NamespacedKey key, double value) {
        Statistic statistic = getRegisteredStatisticOrThrow(key);
        Object syncKey = statistic.getStatisticKey();
        synchronized (syncKey) {
            double oldValue = getDoubleValue(key).orElse((Double) statistic.getDefaultValue());
            if (value <= oldValue) {
                return;
            }
            StatisticModifyEvent preEvent = new StatisticModifyEvent(
                    playerResolver.resolve(uuid), key, statistic, oldValue, value, ModificationType.SET_MAX
            );
            dispatcher.dispatch(preEvent);
            if (preEvent.isCancelled()) {
                return;
            }
            double finalValue = (Double) preEvent.getNewValue();
            values.put(key, finalValue);
            dirtyKeys.add(key);
            dispatcher.dispatch(new PostStatisticModifyEvent(
                    playerResolver.resolve(uuid), key, statistic, oldValue, finalValue, ModificationType.SET_MAX
            ));
        }
    }

    /**
     * Sets a {@link StatisticType#TIMESTAMP} statistic only if no value is currently stored.
     *
     * @param key     The statistic key.
     * @param instant The timestamp to set.
     * @throws StatisticNotRegisteredException if the key is not registered.
     */
    public void setTimestampIfAbsent(@NotNull NamespacedKey key, @NotNull Instant instant) {
        Statistic statistic = getRegisteredStatisticOrThrow(key);
        if (values.containsKey(key)) {
            return;
        }
        StatisticModifyEvent preEvent = new StatisticModifyEvent(
                playerResolver.resolve(uuid), key, statistic, statistic.getDefaultValue(), instant, ModificationType.SET_IF_ABSENT
        );
        dispatcher.dispatch(preEvent);
        if (preEvent.isCancelled()) {
            return;
        }
        Instant finalValue = (Instant) preEvent.getNewValue();
        values.put(key, finalValue);
        dirtyKeys.add(key);
        dispatcher.dispatch(new PostStatisticModifyEvent(
                playerResolver.resolve(uuid), key, statistic, statistic.getDefaultValue(), finalValue, ModificationType.SET_IF_ABSENT
        ));
    }

    /**
     * Adds an element to a {@link StatisticType#SET_STRING} statistic. If the set is at
     * {@link Statistic#getMaxSetSize()} capacity, the oldest entry is evicted.
     *
     * @param key     The statistic key.
     * @param element The element to add.
     * @return {@code true} if the element was new (the set changed).
     * @throws StatisticNotRegisteredException if the key is not registered.
     */
    @SuppressWarnings("unchecked")
    public boolean addToSet(@NotNull NamespacedKey key, @NotNull String element) {
        Statistic statistic = getRegisteredStatisticOrThrow(key);
        Object syncKey = statistic.getStatisticKey();
        synchronized (syncKey) {
            LinkedHashSet<String> currentSet = getOrCreateSet(key, statistic);
            if (currentSet.contains(element)) {
                return false;
            }
            Set<String> oldSnapshot = new LinkedHashSet<>(currentSet);
            LinkedHashSet<String> newSet = new LinkedHashSet<>(currentSet);
            int maxSize = statistic.getMaxSetSize();
            if (maxSize > 0 && newSet.size() >= maxSize) {
                var iterator = newSet.iterator();
                if (iterator.hasNext()) {
                    iterator.next();
                    iterator.remove();
                }
            }
            newSet.add(element);
            StatisticModifyEvent preEvent = new StatisticModifyEvent(
                    playerResolver.resolve(uuid), key, statistic, oldSnapshot, newSet, ModificationType.ADD_TO_SET
            );
            dispatcher.dispatch(preEvent);
            if (preEvent.isCancelled()) {
                return false;
            }
            Set<String> finalSet = (Set<String>) preEvent.getNewValue();
            values.put(key, new LinkedHashSet<>(finalSet));
            dirtyKeys.add(key);
            dispatcher.dispatch(new PostStatisticModifyEvent(
                    playerResolver.resolve(uuid), key, statistic, oldSnapshot, finalSet, ModificationType.ADD_TO_SET
            ));
            return true;
        }
    }

    /**
     * Removes an element from a {@link StatisticType#SET_STRING} statistic.
     *
     * @param key     The statistic key.
     * @param element The element to remove.
     * @return {@code true} if the element was present (the set changed).
     * @throws StatisticNotRegisteredException if the key is not registered.
     */
    @SuppressWarnings("unchecked")
    public boolean removeFromSet(@NotNull NamespacedKey key, @NotNull String element) {
        Statistic statistic = getRegisteredStatisticOrThrow(key);
        Object syncKey = statistic.getStatisticKey();
        synchronized (syncKey) {
            LinkedHashSet<String> currentSet = getOrCreateSet(key, statistic);
            if (!currentSet.contains(element)) {
                return false;
            }
            Set<String> oldSnapshot = new LinkedHashSet<>(currentSet);
            LinkedHashSet<String> newSet = new LinkedHashSet<>(currentSet);
            newSet.remove(element);
            StatisticModifyEvent preEvent = new StatisticModifyEvent(
                    playerResolver.resolve(uuid), key, statistic, oldSnapshot, newSet, ModificationType.REMOVE_FROM_SET
            );
            dispatcher.dispatch(preEvent);
            if (preEvent.isCancelled()) {
                return false;
            }
            Set<String> finalSet = (Set<String>) preEvent.getNewValue();
            values.put(key, new LinkedHashSet<>(finalSet));
            dirtyKeys.add(key);
            dispatcher.dispatch(new PostStatisticModifyEvent(
                    playerResolver.resolve(uuid), key, statistic, oldSnapshot, finalSet, ModificationType.REMOVE_FROM_SET
            ));
            return true;
        }
    }

    // ── Lifecycle ──────────────────────────────────────────────────────

    /**
     * Populates this data from database-loaded entries. Called during player load.
     * Overwrites any existing values and clears the dirty set.
     *
     * @param entries The entries to load.
     */
    public void loadFromDatabase(@NotNull Map<NamespacedKey, StatisticEntry> entries) {
        values.clear();
        dirtyKeys.clear();
        for (var entry : entries.entrySet()) {
            values.put(entry.getKey(), entry.getValue().value());
        }
    }

    /**
     * Returns {@code true} if any statistic has been modified since the last save.
     *
     * @return {@code true} if dirty.
     */
    public boolean isDirty() {
        return !dirtyKeys.isEmpty();
    }

    /**
     * Gets only the modified entries for efficient delta saves.
     * Each returned entry includes the current value and its type from the registry.
     *
     * @return A map of modified entries.
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

    /**
     * Clears the dirty set. Called after a successful save transaction.
     */
    public void markClean() {
        dirtyKeys.clear();
    }

    // ── Private Helpers ────────────────────────────────────────────────

    @NotNull
    private Optional<Statistic> getRegisteredStatistic(@NotNull NamespacedKey key) {
        return RegistryAccess.registryAccess()
                .registry(RegistryKey.STATISTIC)
                .getStatistic(key);
    }

    @NotNull
    private Statistic getRegisteredStatisticOrThrow(@NotNull NamespacedKey key) {
        return getRegisteredStatistic(key)
                .orElseThrow(() -> new StatisticNotRegisteredException(key));
    }

    @NotNull
    private Object resolveCurrentValue(@NotNull NamespacedKey key, @NotNull Statistic statistic) {
        Object value = values.get(key);
        return value != null ? value : statistic.getDefaultValue();
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private LinkedHashSet<String> getOrCreateSet(@NotNull NamespacedKey key, @NotNull Statistic statistic) {
        Object value = values.get(key);
        if (value instanceof LinkedHashSet<?>) {
            return (LinkedHashSet<String>) value;
        }
        if (value instanceof Set<?>) {
            return new LinkedHashSet<>((Set<String>) value);
        }
        return new LinkedHashSet<>((Set<String>) statistic.getDefaultValue());
    }
}

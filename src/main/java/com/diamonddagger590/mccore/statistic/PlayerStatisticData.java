package com.diamonddagger590.mccore.statistic;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player mutable container for statistic values. Tracks which entries
 * have been modified since the last clean so that only dirty entries are
 * flushed to the database.
 */
public class PlayerStatisticData {

    private final Map<NamespacedKey, Object> values = new ConcurrentHashMap<>();
    private final Set<NamespacedKey> dirtyKeys = ConcurrentHashMap.newKeySet();

    /**
     * Gets the current value of a statistic.
     *
     * @param key The statistic key.
     * @return An {@link Optional} containing the value, or empty if not set.
     */
    @NotNull
    public Optional<Object> getValue(@NotNull NamespacedKey key) {
        return Optional.ofNullable(values.get(key));
    }

    /**
     * Sets the value of a statistic, marking it as dirty.
     *
     * @param key   The statistic key.
     * @param value The new value.
     */
    public void setValue(@NotNull NamespacedKey key, @NotNull Object value) {
        values.put(key, value);
        dirtyKeys.add(key);
    }

    /**
     * Increments a long statistic value by the given amount.
     *
     * @param key    The statistic key.
     * @param amount The amount to increment by.
     */
    public void incrementLong(@NotNull NamespacedKey key, long amount) {
        values.merge(key, amount, (existing, inc) -> ((Number) existing).longValue() + (Long) inc);
        dirtyKeys.add(key);
    }

    /**
     * Increments a double statistic value by the given amount.
     *
     * @param key    The statistic key.
     * @param amount The amount to increment by.
     */
    public void incrementDouble(@NotNull NamespacedKey key, double amount) {
        values.merge(key, amount, (existing, inc) -> ((Number) existing).doubleValue() + (Double) inc);
        dirtyKeys.add(key);
    }

    /**
     * Sets the value to the maximum of the current value and the provided value.
     *
     * @param key   The statistic key.
     * @param value The candidate value.
     */
    public void setMaxInt(@NotNull NamespacedKey key, int value) {
        values.merge(key, value, (existing, candidate) -> Math.max(((Number) existing).intValue(), (Integer) candidate));
        dirtyKeys.add(key);
    }

    /**
     * Gets all entries that have been modified since the last {@link #markClean(Set)} call.
     *
     * @return A map of dirty statistic entries.
     */
    @NotNull
    public Map<NamespacedKey, StatisticEntry> getModifiedEntries() {
        Map<NamespacedKey, StatisticEntry> modified = new HashMap<>();
        for (NamespacedKey key : dirtyKeys) {
            Object value = values.get(key);
            if (value != null) {
                modified.put(key, new StatisticEntry(value));
            }
        }
        return modified;
    }

    /**
     * Marks the given keys as clean (no longer dirty).
     *
     * @param keys The keys to mark clean.
     */
    public void markClean(@NotNull Set<NamespacedKey> keys) {
        dirtyKeys.removeAll(keys);
    }

    /**
     * Populates this data container from a map of persisted entries.
     *
     * @param entries The entries loaded from the database.
     */
    public void populateFromEntries(@NotNull Map<NamespacedKey, StatisticEntry> entries) {
        for (var entry : entries.entrySet()) {
            values.put(entry.getKey(), entry.getValue().value());
        }
    }
}

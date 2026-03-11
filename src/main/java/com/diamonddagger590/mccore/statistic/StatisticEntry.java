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

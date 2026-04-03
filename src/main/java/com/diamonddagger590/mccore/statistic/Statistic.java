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

package com.diamonddagger590.mccore.statistic;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * A tracked statistic that can be registered in a {@link StatisticRegistry}
 * and stored per-player via {@link PlayerStatisticData}.
 */
public interface Statistic {

    /**
     * Gets the unique key identifying this statistic.
     *
     * @return The {@link NamespacedKey} for this statistic.
     */
    @NotNull
    NamespacedKey getStatisticKey();

    /**
     * Gets the data type of this statistic's value.
     *
     * @return The {@link StatisticType} of this statistic.
     */
    @NotNull
    StatisticType getStatisticType();

    /**
     * Gets the default value for this statistic when no data exists.
     *
     * @return The default value.
     */
    @NotNull
    Object getDefaultValue();

    /**
     * Gets the human-readable display name for this statistic.
     *
     * @return The display name.
     */
    @NotNull
    String getDisplayName();

    /**
     * Gets a description of what this statistic tracks.
     *
     * @return The description.
     */
    @NotNull
    String getDescription();
}

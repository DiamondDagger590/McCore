package com.diamonddagger590.mccore.statistic;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * A simple implementation of {@link Statistic} backed by a record.
 *
 * @param statisticKey  The unique key identifying this statistic.
 * @param statisticType The data type of this statistic's value.
 * @param defaultValue  The default value when no data exists.
 * @param displayName   The human-readable display name.
 * @param description   A description of what this statistic tracks.
 */
public record SimpleStatistic(
        @NotNull NamespacedKey statisticKey,
        @NotNull StatisticType statisticType,
        @NotNull Object defaultValue,
        @NotNull String displayName,
        @NotNull String description
) implements Statistic {

    @NotNull
    @Override
    public NamespacedKey getStatisticKey() {
        return statisticKey;
    }

    @NotNull
    @Override
    public StatisticType getStatisticType() {
        return statisticType;
    }

    @NotNull
    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    @Override
    public String getDescription() {
        return description;
    }
}

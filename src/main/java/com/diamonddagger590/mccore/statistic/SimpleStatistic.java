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

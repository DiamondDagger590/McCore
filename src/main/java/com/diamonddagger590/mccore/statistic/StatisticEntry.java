package com.diamonddagger590.mccore.statistic;

import org.jetbrains.annotations.NotNull;

/**
 * A single statistic value entry, used for storage and caching.
 *
 * @param value The current value of the statistic.
 */
public record StatisticEntry(@NotNull Object value) {
}

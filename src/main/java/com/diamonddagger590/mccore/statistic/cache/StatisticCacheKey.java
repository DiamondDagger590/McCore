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

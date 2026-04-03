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
        var keysToRemove = cache.asMap().keySet().stream()
                .filter(cacheKey -> cacheKey.uuid().equals(uuid))
                .toList();
        cache.invalidateAll(keysToRemove);
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

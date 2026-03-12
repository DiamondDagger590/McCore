package com.diamonddagger590.mccore.statistic.cache;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.statistic.StatisticEntry;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An optional Caffeine-backed cache for offline player statistic queries.
 * <p>
 * Downstream plugins construct and configure this cache programmatically.
 * If no cache is provided, offline queries go directly to the database.
 */
public class StatisticCache {

    private final Cache<StatisticCacheKey, StatisticEntry> cache;
    private final AtomicBoolean missWarningLogged = new AtomicBoolean(false);

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
        var result = Optional.ofNullable(cache.getIfPresent(new StatisticCacheKey(uuid, key)));
        if (result.isEmpty() && missWarningLogged.compareAndSet(false, true)) {
            try {
                CorePlugin.getInstance().getLogger().warning("StatisticCache miss for offline player " + uuid
                        + " (key: " + key + "). This query will fall through to the database. "
                        + "This warning is logged once per cache instance.");
            }
            catch (NullPointerException ignored) {
                // CorePlugin not initialized (e.g., unit tests) — silently skip warning
            }
        }
        return result;
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
        cache.asMap().keySet().removeIf(cacheKey -> cacheKey.uuid().equals(uuid));
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

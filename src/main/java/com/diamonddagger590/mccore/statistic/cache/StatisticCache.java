package com.diamonddagger590.mccore.statistic.cache;

import com.diamonddagger590.mccore.statistic.StatisticEntry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A time-limited cache for offline player statistic lookups. Entries expire
 * after a configurable TTL and the cache has a maximum size.
 */
public class StatisticCache {

    private final long maxSize;
    private final long ttlMillis;
    private final Map<String, CachedEntry> cache = new ConcurrentHashMap<>();

    /**
     * Creates a new statistic cache.
     *
     * @param maxSize    The maximum number of entries.
     * @param ttlSeconds The time-to-live for each entry in seconds.
     */
    public StatisticCache(long maxSize, long ttlSeconds) {
        this.maxSize = maxSize;
        this.ttlMillis = ttlSeconds * 1000L;
    }

    /**
     * Gets a cached statistic entry for a player.
     *
     * @param uuid The player's UUID.
     * @param key  The statistic key.
     * @return An {@link Optional} containing the entry, or empty if not cached or expired.
     */
    @NotNull
    public Optional<StatisticEntry> get(@NotNull UUID uuid, @NotNull NamespacedKey key) {
        String cacheKey = toCacheKey(uuid, key);
        CachedEntry entry = cache.get(cacheKey);
        if (entry == null) {
            return Optional.empty();
        }
        if (System.currentTimeMillis() - entry.timestamp > ttlMillis) {
            cache.remove(cacheKey);
            return Optional.empty();
        }
        return Optional.of(entry.entry);
    }

    /**
     * Puts a statistic entry into the cache.
     *
     * @param uuid  The player's UUID.
     * @param key   The statistic key.
     * @param entry The entry to cache.
     */
    public void put(@NotNull UUID uuid, @NotNull NamespacedKey key, @NotNull StatisticEntry entry) {
        if (cache.size() >= maxSize) {
            evictOldest();
        }
        cache.put(toCacheKey(uuid, key), new CachedEntry(entry, System.currentTimeMillis()));
    }

    /**
     * Invalidates all cached entries for a player.
     *
     * @param uuid The player's UUID.
     */
    public void invalidate(@NotNull UUID uuid) {
        String prefix = uuid.toString() + ":";
        cache.keySet().removeIf(k -> k.startsWith(prefix));
    }

    @NotNull
    private String toCacheKey(@NotNull UUID uuid, @NotNull NamespacedKey key) {
        return uuid.toString() + ":" + key.toString();
    }

    private void evictOldest() {
        String oldestKey = null;
        long oldestTime = Long.MAX_VALUE;
        for (var entry : cache.entrySet()) {
            if (entry.getValue().timestamp < oldestTime) {
                oldestTime = entry.getValue().timestamp;
                oldestKey = entry.getKey();
            }
        }
        if (oldestKey != null) {
            cache.remove(oldestKey);
        }
    }

    private record CachedEntry(@NotNull StatisticEntry entry, long timestamp) {
    }
}

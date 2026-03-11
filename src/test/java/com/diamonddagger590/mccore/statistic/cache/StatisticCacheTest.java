package com.diamonddagger590.mccore.statistic.cache;

import com.diamonddagger590.mccore.statistic.StatisticEntry;
import com.diamonddagger590.mccore.statistic.StatisticType;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatisticCacheTest {

    private StatisticCache cache;

    @SuppressWarnings("deprecation")
    private static NamespacedKey key(String namespace, String key) {
        return new NamespacedKey(namespace, key);
    }

    @BeforeEach
    void setUp() {
        cache = new StatisticCache(100, 300);
    }

    @Test
    void getReturnsEmptyOnCacheMiss() {
        UUID uuid = UUID.randomUUID();
        assertFalse(cache.get(uuid, key("test", "stat")).isPresent());
    }

    @Test
    void putThenGetReturnsEntry() {
        UUID uuid = UUID.randomUUID();
        NamespacedKey key = key("test", "kills");
        StatisticEntry entry = new StatisticEntry(key, StatisticType.INT, 42);
        cache.put(uuid, key, entry);
        assertTrue(cache.get(uuid, key).isPresent());
        assertEquals(42, cache.get(uuid, key).get().getAsInt());
    }

    @Test
    void invalidateByUuidRemovesAllEntriesForPlayer() {
        UUID uuid = UUID.randomUUID();
        NamespacedKey key1 = key("test", "kills");
        NamespacedKey key2 = key("test", "deaths");
        cache.put(uuid, key1, new StatisticEntry(key1, StatisticType.INT, 1));
        cache.put(uuid, key2, new StatisticEntry(key2, StatisticType.INT, 2));
        cache.invalidate(uuid);
        assertFalse(cache.get(uuid, key1).isPresent());
        assertFalse(cache.get(uuid, key2).isPresent());
    }

    @Test
    void invalidateByKeyRemovesOnlySpecificEntry() {
        UUID uuid = UUID.randomUUID();
        NamespacedKey key1 = key("test", "kills");
        NamespacedKey key2 = key("test", "deaths");
        cache.put(uuid, key1, new StatisticEntry(key1, StatisticType.INT, 1));
        cache.put(uuid, key2, new StatisticEntry(key2, StatisticType.INT, 2));
        cache.invalidate(uuid, key1);
        assertFalse(cache.get(uuid, key1).isPresent());
        assertTrue(cache.get(uuid, key2).isPresent());
    }

    @Test
    void sizeReflectsCurrentEntryCount() {
        UUID uuid = UUID.randomUUID();
        NamespacedKey key1 = key("test", "a");
        NamespacedKey key2 = key("test", "b");
        assertEquals(0, cache.size());
        cache.put(uuid, key1, new StatisticEntry(key1, StatisticType.INT, 1));
        cache.put(uuid, key2, new StatisticEntry(key2, StatisticType.INT, 2));
        // Caffeine may not reflect size immediately; cleanUp for accurate count
        assertEquals(2, cache.size());
    }

    @Test
    void ttlExpiresEntries() throws InterruptedException {
        StatisticCache shortTtlCache = new StatisticCache(100, 1);
        UUID uuid = UUID.randomUUID();
        NamespacedKey key = key("test", "ttl");
        shortTtlCache.put(uuid, key, new StatisticEntry(key, StatisticType.INT, 1));
        assertTrue(shortTtlCache.get(uuid, key).isPresent());
        Thread.sleep(1500);
        assertFalse(shortTtlCache.get(uuid, key).isPresent());
    }
}

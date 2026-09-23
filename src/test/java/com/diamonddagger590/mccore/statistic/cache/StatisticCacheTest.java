package com.diamonddagger590.mccore.statistic.cache;

import com.diamonddagger590.mccore.statistic.StatisticEntry;
import com.diamonddagger590.mccore.statistic.StatisticType;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("Given an empty cache, when getting a non-existent entry, then returns empty Optional")
    void get_returnsEmpty_whenCacheMiss() {
        UUID uuid = UUID.randomUUID();
        assertFalse(cache.get(uuid, key("test", "stat")).isPresent());
    }

    @Test
    @DisplayName("Given a cached entry, when getting by same UUID and key, then returns the entry")
    void get_returnsEntry_afterPut() {
        UUID uuid = UUID.randomUUID();
        NamespacedKey key = key("test", "kills");
        StatisticEntry entry = new StatisticEntry(key, StatisticType.INT, 42);
        cache.put(uuid, key, entry);
        assertTrue(cache.get(uuid, key).isPresent());
        assertEquals(42, cache.get(uuid, key).get().getAsInt());
    }

    @Test
    @DisplayName("Given multiple entries for one player, when invalidating by UUID, then removes all entries for that player")
    void invalidate_byUuid_removesAllEntriesForPlayer() {
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
    @DisplayName("Given two entries for one player, when invalidating a specific key, then removes only that entry")
    void invalidate_byKey_removesOnlySpecificEntry() {
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
    @DisplayName("Given entries added to the cache, when calling size, then returns the current entry count")
    void size_reflectsCurrentEntryCount() {
        UUID uuid = UUID.randomUUID();
        NamespacedKey key1 = key("test", "a");
        NamespacedKey key2 = key("test", "b");
        assertEquals(0, cache.size());
        cache.put(uuid, key1, new StatisticEntry(key1, StatisticType.INT, 1));
        cache.put(uuid, key2, new StatisticEntry(key2, StatisticType.INT, 2));
        assertEquals(2, cache.size());
    }

    @Test
    @DisplayName("Given a cache with short TTL, when entry expires, then get returns empty")
    void get_returnsEmpty_afterTtlExpires() throws InterruptedException {
        StatisticCache shortTtlCache = new StatisticCache(100, 1);
        UUID uuid = UUID.randomUUID();
        NamespacedKey key = key("test", "ttl");
        shortTtlCache.put(uuid, key, new StatisticEntry(key, StatisticType.INT, 1));
        assertTrue(shortTtlCache.get(uuid, key).isPresent());
        Thread.sleep(1500);
        assertFalse(shortTtlCache.get(uuid, key).isPresent());
    }

    @Test
    @DisplayName("Given entries for two players, when invalidating one player by UUID, then the other player's entries remain")
    void invalidate_byUuid_doesNotAffectOtherPlayers() {
        UUID playerA = UUID.randomUUID();
        UUID playerB = UUID.randomUUID();
        NamespacedKey key1 = key("test", "kills");
        NamespacedKey key2 = key("test", "deaths");
        cache.put(playerA, key1, new StatisticEntry(key1, StatisticType.INT, 10));
        cache.put(playerA, key2, new StatisticEntry(key2, StatisticType.INT, 5));
        cache.put(playerB, key1, new StatisticEntry(key1, StatisticType.INT, 20));
        cache.put(playerB, key2, new StatisticEntry(key2, StatisticType.INT, 15));

        cache.invalidate(playerA);

        assertFalse(cache.get(playerA, key1).isPresent());
        assertFalse(cache.get(playerA, key2).isPresent());
        assertTrue(cache.get(playerB, key1).isPresent());
        assertEquals(20, cache.get(playerB, key1).get().getAsInt());
        assertTrue(cache.get(playerB, key2).isPresent());
        assertEquals(15, cache.get(playerB, key2).get().getAsInt());
    }

    @Test
    @DisplayName("Given an existing entry, when putting a new entry with the same UUID and key, then the value is overwritten")
    void put_overwritesExistingEntry_whenSameUuidAndKey() {
        UUID uuid = UUID.randomUUID();
        NamespacedKey key = key("test", "score");
        cache.put(uuid, key, new StatisticEntry(key, StatisticType.INT, 100));
        assertEquals(100, cache.get(uuid, key).get().getAsInt());

        cache.put(uuid, key, new StatisticEntry(key, StatisticType.INT, 200));
        assertEquals(200, cache.get(uuid, key).get().getAsInt());
    }

    @Test
    @DisplayName("Given an empty cache, when invalidating by UUID, then no error occurs and cache remains empty")
    void invalidate_byUuid_doesNotThrow_whenNoEntriesExist() {
        UUID uuid = UUID.randomUUID();
        cache.invalidate(uuid);
        assertEquals(0, cache.size());
    }

    @Test
    @DisplayName("Given an empty cache, when invalidating by UUID and key, then no error occurs and cache remains empty")
    void invalidate_byKey_doesNotThrow_whenNoEntryExists() {
        UUID uuid = UUID.randomUUID();
        cache.invalidate(uuid, key("test", "nonexistent"));
        assertEquals(0, cache.size());
    }

    @Test
    @DisplayName("Given entries for multiple players, when putting overwrites one, then size does not change")
    void put_overwrite_doesNotChangeSize() {
        UUID uuid = UUID.randomUUID();
        NamespacedKey key = key("test", "xp");
        cache.put(uuid, key, new StatisticEntry(key, StatisticType.INT, 50));
        assertEquals(1, cache.size());

        cache.put(uuid, key, new StatisticEntry(key, StatisticType.INT, 75));
        assertEquals(1, cache.size());
    }

    @Test
    @DisplayName("Given entries for two players with same key, when invalidating one player's key, then the other player's entry remains")
    void invalidate_byKey_doesNotAffectOtherPlayersWithSameKey() {
        UUID playerA = UUID.randomUUID();
        UUID playerB = UUID.randomUUID();
        NamespacedKey key = key("test", "kills");
        cache.put(playerA, key, new StatisticEntry(key, StatisticType.INT, 10));
        cache.put(playerB, key, new StatisticEntry(key, StatisticType.INT, 20));

        cache.invalidate(playerA, key);

        assertFalse(cache.get(playerA, key).isPresent());
        assertTrue(cache.get(playerB, key).isPresent());
        assertEquals(20, cache.get(playerB, key).get().getAsInt());
    }

    @Test
    @DisplayName("Given entries added and then invalidated, when checking size, then size reflects removals")
    void size_reflectsRemovals_afterInvalidate() {
        UUID uuid = UUID.randomUUID();
        NamespacedKey key1 = key("test", "a");
        NamespacedKey key2 = key("test", "b");
        cache.put(uuid, key1, new StatisticEntry(key1, StatisticType.INT, 1));
        cache.put(uuid, key2, new StatisticEntry(key2, StatisticType.INT, 2));
        assertEquals(2, cache.size());

        cache.invalidate(uuid, key1);
        assertEquals(1, cache.size());

        cache.invalidate(uuid, key2);
        assertEquals(0, cache.size());
    }

    @Test
    @DisplayName("Given entries of different statistic types, when retrieving them, then each returns the correct typed value")
    void get_returnsCorrectTypedValues_forDifferentStatisticTypes() {
        UUID uuid = UUID.randomUUID();
        NamespacedKey intKey = key("test", "int_stat");
        NamespacedKey longKey = key("test", "long_stat");
        NamespacedKey doubleKey = key("test", "double_stat");
        NamespacedKey stringKey = key("test", "string_stat");

        cache.put(uuid, intKey, new StatisticEntry(intKey, StatisticType.INT, 42));
        cache.put(uuid, longKey, new StatisticEntry(longKey, StatisticType.LONG, 100_000L));
        cache.put(uuid, doubleKey, new StatisticEntry(doubleKey, StatisticType.DOUBLE, 3.14));
        cache.put(uuid, stringKey, new StatisticEntry(stringKey, StatisticType.STRING, "hello"));

        assertEquals(42, cache.get(uuid, intKey).get().getAsInt());
        assertEquals(100_000L, cache.get(uuid, longKey).get().getAsLong());
        assertEquals(3.14, cache.get(uuid, doubleKey).get().getAsDouble(), 0.001);
        assertEquals("hello", cache.get(uuid, stringKey).get().getAsString());
    }
}

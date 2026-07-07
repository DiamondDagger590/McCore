package com.diamonddagger590.mccore.statistic.cache;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatisticCacheKeyTest {

    @SuppressWarnings("deprecation")
    private static NamespacedKey key(String namespace, String key) {
        return new NamespacedKey(namespace, key);
    }

    private static final UUID UUID_A = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID UUID_B = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final NamespacedKey KEY_KILLS = key("test", "kills");
    private static final NamespacedKey KEY_DEATHS = key("test", "deaths");

    @Test
    @DisplayName("Given a UUID and key, when creating a cache key, then accessors return the same values")
    void accessors_returnCorrectValues_whenKeyIsCreated() {
        StatisticCacheKey cacheKey = new StatisticCacheKey(UUID_A, KEY_KILLS);
        assertEquals(UUID_A, cacheKey.uuid());
        assertEquals(KEY_KILLS, cacheKey.key());
    }

    @Test
    @DisplayName("Given two cache keys with the same UUID and key, when comparing, then they are equal")
    void equals_returnsTrue_whenBothFieldsMatch() {
        StatisticCacheKey key1 = new StatisticCacheKey(UUID_A, KEY_KILLS);
        StatisticCacheKey key2 = new StatisticCacheKey(UUID_A, KEY_KILLS);
        assertEquals(key1, key2);
    }

    @Test
    @DisplayName("Given two cache keys with different UUIDs, when comparing, then they are not equal")
    void equals_returnsFalse_whenUuidsDiffer() {
        StatisticCacheKey key1 = new StatisticCacheKey(UUID_A, KEY_KILLS);
        StatisticCacheKey key2 = new StatisticCacheKey(UUID_B, KEY_KILLS);
        assertNotEquals(key1, key2);
    }

    @Test
    @DisplayName("Given two cache keys with different NamespacedKeys, when comparing, then they are not equal")
    void equals_returnsFalse_whenNamespacedKeysDiffer() {
        StatisticCacheKey key1 = new StatisticCacheKey(UUID_A, KEY_KILLS);
        StatisticCacheKey key2 = new StatisticCacheKey(UUID_A, KEY_DEATHS);
        assertNotEquals(key1, key2);
    }

    @Test
    @DisplayName("Given two cache keys with completely different fields, when comparing, then they are not equal")
    void equals_returnsFalse_whenBothFieldsDiffer() {
        StatisticCacheKey key1 = new StatisticCacheKey(UUID_A, KEY_KILLS);
        StatisticCacheKey key2 = new StatisticCacheKey(UUID_B, KEY_DEATHS);
        assertNotEquals(key1, key2);
    }

    @Test
    @DisplayName("Given two equal cache keys, when comparing hash codes, then they are equal")
    void hashCode_isSame_whenKeysAreEqual() {
        StatisticCacheKey key1 = new StatisticCacheKey(UUID_A, KEY_KILLS);
        StatisticCacheKey key2 = new StatisticCacheKey(UUID_A, KEY_KILLS);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    @DisplayName("Given a cache key, when calling hashCode repeatedly, then it is consistent")
    void hashCode_isConsistent_whenCalledRepeatedly() {
        StatisticCacheKey key = new StatisticCacheKey(UUID_A, KEY_KILLS);
        assertEquals(key.hashCode(), key.hashCode());
    }

    @Test
    @DisplayName("Given a cache key, when calling toString, then it contains uuid and key info")
    void toString_containsFieldInfo_whenCalled() {
        StatisticCacheKey cacheKey = new StatisticCacheKey(UUID_A, KEY_KILLS);
        String str = cacheKey.toString();
        assertNotNull(str);
        assertTrue(str.contains(UUID_A.toString()));
    }

    @Test
    @DisplayName("Given a cache key compared to itself, when checking equality, then returns true")
    void equals_returnsTrue_whenComparedToSelf() {
        StatisticCacheKey cacheKey = new StatisticCacheKey(UUID_A, KEY_KILLS);
        assertEquals(cacheKey, cacheKey);
    }

    @Test
    @DisplayName("Given a cache key compared to null, when checking equality, then returns false")
    void equals_returnsFalse_whenComparedToNull() {
        StatisticCacheKey cacheKey = new StatisticCacheKey(UUID_A, KEY_KILLS);
        assertNotEquals(null, cacheKey);
    }

    @Test
    @DisplayName("Given a cache key used as a map key, when retrieving by equal key, then value is found")
    void cacheKey_worksAsMapKey_whenUsedInHashMap() {
        Map<StatisticCacheKey, String> map = new HashMap<>();
        StatisticCacheKey key1 = new StatisticCacheKey(UUID_A, KEY_KILLS);
        map.put(key1, "value");

        StatisticCacheKey key2 = new StatisticCacheKey(UUID_A, KEY_KILLS);
        assertEquals("value", map.get(key2));
    }

    @Test
    @DisplayName("Given a cache key used as a map key, when retrieving by different key, then value is not found")
    void cacheKey_returnsNull_whenMapKeyDoesNotMatch() {
        Map<StatisticCacheKey, String> map = new HashMap<>();
        StatisticCacheKey key1 = new StatisticCacheKey(UUID_A, KEY_KILLS);
        map.put(key1, "value");

        StatisticCacheKey key2 = new StatisticCacheKey(UUID_B, KEY_KILLS);
        assertNull(map.get(key2));
    }

}

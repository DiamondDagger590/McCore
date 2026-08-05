package com.diamonddagger590.mccore.statistic.cache;

import com.diamonddagger590.mccore.statistic.StatisticEntry;
import com.diamonddagger590.mccore.statistic.StatisticType;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    private static final UUID PLAYER_A = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID PLAYER_B = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final NamespacedKey KEY_KILLS = key("test", "kills");
    private static final NamespacedKey KEY_DEATHS = key("test", "deaths");
    private static final NamespacedKey KEY_XP = key("test", "xp");

    @BeforeEach
    void setUp() {
        cache = new StatisticCache(100, 300);
    }

    @Nested
    @DisplayName("Get and put operations")
    class GetAndPutTests {

        @Test
        @DisplayName("Given an empty cache, when getting a non-existent entry, then returns empty")
        void get_returnsEmpty_whenCacheMiss() {
            assertFalse(cache.get(PLAYER_A, KEY_KILLS).isPresent());
        }

        @Test
        @DisplayName("Given a cached entry, when getting it, then returns the correct value")
        void get_returnsEntry_whenPreviouslyPut() {
            StatisticEntry entry = new StatisticEntry(KEY_KILLS, StatisticType.INT, 42);
            cache.put(PLAYER_A, KEY_KILLS, entry);
            assertTrue(cache.get(PLAYER_A, KEY_KILLS).isPresent());
            assertEquals(42, cache.get(PLAYER_A, KEY_KILLS).get().getAsInt());
        }

        @Test
        @DisplayName("Given an existing entry, when overwriting with new value, then returns updated value")
        void put_overwritesExistingEntry_whenSameKeyUsed() {
            cache.put(PLAYER_A, KEY_KILLS, new StatisticEntry(KEY_KILLS, StatisticType.INT, 10));
            cache.put(PLAYER_A, KEY_KILLS, new StatisticEntry(KEY_KILLS, StatisticType.INT, 99));
            assertTrue(cache.get(PLAYER_A, KEY_KILLS).isPresent());
            assertEquals(99, cache.get(PLAYER_A, KEY_KILLS).get().getAsInt());
        }

        @Test
        @DisplayName("Given different statistic types, when cached, then each returns correct typed value")
        void put_supportsDifferentStatisticTypes() {
            cache.put(PLAYER_A, KEY_KILLS, new StatisticEntry(KEY_KILLS, StatisticType.INT, 5));
            cache.put(PLAYER_A, KEY_XP, new StatisticEntry(KEY_XP, StatisticType.DOUBLE, 3.14));

            assertEquals(5, cache.get(PLAYER_A, KEY_KILLS).get().getAsInt());
            assertEquals(3.14, cache.get(PLAYER_A, KEY_XP).get().getAsDouble(), 0.001);
        }
    }

    @Nested
    @DisplayName("Multi-player isolation")
    class MultiPlayerTests {

        @Test
        @DisplayName("Given two players with same stat key, when getting, then returns each player's own value")
        void get_returnsSeparateValues_forDifferentPlayers() {
            cache.put(PLAYER_A, KEY_KILLS, new StatisticEntry(KEY_KILLS, StatisticType.INT, 10));
            cache.put(PLAYER_B, KEY_KILLS, new StatisticEntry(KEY_KILLS, StatisticType.INT, 20));

            assertEquals(10, cache.get(PLAYER_A, KEY_KILLS).get().getAsInt());
            assertEquals(20, cache.get(PLAYER_B, KEY_KILLS).get().getAsInt());
        }

        @Test
        @DisplayName("Given player A has an entry but player B doesn't, when B queries, then returns empty")
        void get_returnsEmpty_whenDifferentPlayerQueriesSameKey() {
            cache.put(PLAYER_A, KEY_KILLS, new StatisticEntry(KEY_KILLS, StatisticType.INT, 10));
            assertFalse(cache.get(PLAYER_B, KEY_KILLS).isPresent());
        }
    }

    @Nested
    @DisplayName("Invalidate operations")
    class InvalidateTests {

        @Test
        @DisplayName("Given multiple entries for a player, when invalidating by UUID, then all their entries are removed")
        void invalidateByUuid_removesAllEntries_whenPlayerHasMultiple() {
            cache.put(PLAYER_A, KEY_KILLS, new StatisticEntry(KEY_KILLS, StatisticType.INT, 1));
            cache.put(PLAYER_A, KEY_DEATHS, new StatisticEntry(KEY_DEATHS, StatisticType.INT, 2));
            cache.invalidate(PLAYER_A);
            assertFalse(cache.get(PLAYER_A, KEY_KILLS).isPresent());
            assertFalse(cache.get(PLAYER_A, KEY_DEATHS).isPresent());
        }

        @Test
        @DisplayName("Given entries for two players, when invalidating one by UUID, then other player's entries remain")
        void invalidateByUuid_doesNotAffectOtherPlayers() {
            cache.put(PLAYER_A, KEY_KILLS, new StatisticEntry(KEY_KILLS, StatisticType.INT, 1));
            cache.put(PLAYER_B, KEY_KILLS, new StatisticEntry(KEY_KILLS, StatisticType.INT, 2));

            cache.invalidate(PLAYER_A);

            assertFalse(cache.get(PLAYER_A, KEY_KILLS).isPresent());
            assertTrue(cache.get(PLAYER_B, KEY_KILLS).isPresent());
            assertEquals(2, cache.get(PLAYER_B, KEY_KILLS).get().getAsInt());
        }

        @Test
        @DisplayName("Given multiple entries, when invalidating by specific key, then only that entry is removed")
        void invalidateByKey_removesOnlyTargetEntry() {
            cache.put(PLAYER_A, KEY_KILLS, new StatisticEntry(KEY_KILLS, StatisticType.INT, 1));
            cache.put(PLAYER_A, KEY_DEATHS, new StatisticEntry(KEY_DEATHS, StatisticType.INT, 2));
            cache.invalidate(PLAYER_A, KEY_KILLS);
            assertFalse(cache.get(PLAYER_A, KEY_KILLS).isPresent());
            assertTrue(cache.get(PLAYER_A, KEY_DEATHS).isPresent());
        }

        @Test
        @DisplayName("Given an empty cache, when invalidating by UUID, then no exception is thrown")
        void invalidateByUuid_doesNotThrow_whenCacheIsEmpty() {
            cache.invalidate(PLAYER_A);
            assertEquals(0, cache.size());
        }

        @Test
        @DisplayName("Given an empty cache, when invalidating by specific key, then no exception is thrown")
        void invalidateByKey_doesNotThrow_whenEntryDoesNotExist() {
            cache.invalidate(PLAYER_A, KEY_KILLS);
            assertEquals(0, cache.size());
        }
    }

    @Nested
    @DisplayName("Size tracking")
    class SizeTests {

        @Test
        @DisplayName("Given an empty cache, when checking size, then returns 0")
        void size_returnsZero_whenCacheIsEmpty() {
            assertEquals(0, cache.size());
        }

        @Test
        @DisplayName("Given entries added, when checking size, then reflects current entry count")
        void size_reflectsEntryCount_whenEntriesAdded() {
            cache.put(PLAYER_A, KEY_KILLS, new StatisticEntry(KEY_KILLS, StatisticType.INT, 1));
            cache.put(PLAYER_A, KEY_DEATHS, new StatisticEntry(KEY_DEATHS, StatisticType.INT, 2));
            assertEquals(2, cache.size());
        }

        @Test
        @DisplayName("Given entries from multiple players, when checking size, then counts all entries")
        void size_countsEntriesAcrossPlayers() {
            cache.put(PLAYER_A, KEY_KILLS, new StatisticEntry(KEY_KILLS, StatisticType.INT, 1));
            cache.put(PLAYER_B, KEY_KILLS, new StatisticEntry(KEY_KILLS, StatisticType.INT, 2));
            assertEquals(2, cache.size());
        }

        @Test
        @DisplayName("Given overwritten entry, when checking size, then count does not increase")
        void size_doesNotIncrease_whenEntryIsOverwritten() {
            cache.put(PLAYER_A, KEY_KILLS, new StatisticEntry(KEY_KILLS, StatisticType.INT, 1));
            cache.put(PLAYER_A, KEY_KILLS, new StatisticEntry(KEY_KILLS, StatisticType.INT, 99));
            assertEquals(1, cache.size());
        }
    }

    @Nested
    @DisplayName("TTL expiration")
    class TtlTests {

        @Test
        @DisplayName("Given a short TTL, when entry expires, then get returns empty")
        void get_returnsEmpty_whenTtlExpires() throws InterruptedException {
            StatisticCache shortTtlCache = new StatisticCache(100, 1);
            shortTtlCache.put(PLAYER_A, KEY_KILLS, new StatisticEntry(KEY_KILLS, StatisticType.INT, 1));
            assertTrue(shortTtlCache.get(PLAYER_A, KEY_KILLS).isPresent());
            Thread.sleep(1500);
            assertFalse(shortTtlCache.get(PLAYER_A, KEY_KILLS).isPresent());
        }
    }
}

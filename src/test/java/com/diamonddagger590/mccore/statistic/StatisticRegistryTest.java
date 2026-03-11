package com.diamonddagger590.mccore.statistic;

import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatisticRegistryTest {

    @SuppressWarnings("deprecation")
    private static NamespacedKey key(String namespace, String key) {
        return new NamespacedKey(namespace, key);
    }

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
    }

    private StatisticRegistry registry() {
        return RegistryAccess.registryAccess().registry(RegistryKey.STATISTIC);
    }

    @Test
    void registerSucceedsForNewKey() {
        Statistic stat = new SimpleStatistic(key("test", "kills"), StatisticType.INT, 0, "Kills", "Total kills");
        registry().register(stat);
        assertTrue(registry().registered(stat));
    }

    @Test
    void registerThrowsForDuplicateKey() {
        Statistic stat = new SimpleStatistic(key("test", "kills"), StatisticType.INT, 0, "Kills", "Total kills");
        registry().register(stat);
        assertThrows(IllegalArgumentException.class, () -> registry().register(stat));
    }

    @Test
    void registeredReturnsFalseForUnregistered() {
        Statistic stat = new SimpleStatistic(key("test", "unknown"), StatisticType.INT, 0, "Unknown", "Unknown stat");
        assertFalse(registry().registered(stat));
    }

    @Test
    void getStatisticReturnsRegisteredStatistic() {
        NamespacedKey key = key("test", "kills");
        Statistic stat = new SimpleStatistic(key, StatisticType.INT, 0, "Kills", "Total kills");
        registry().register(stat);
        assertTrue(registry().getStatistic(key).isPresent());
        assertEquals(stat, registry().getStatistic(key).get());
    }

    @Test
    void getStatisticReturnsEmptyForUnregistered() {
        assertFalse(registry().getStatistic(key("test", "none")).isPresent());
    }

    @Test
    void getRegisteredStatisticsReturnsAllStats() {
        Statistic stat1 = new SimpleStatistic(key("test", "a"), StatisticType.INT, 0, "A", "Stat A");
        Statistic stat2 = new SimpleStatistic(key("test", "b"), StatisticType.LONG, 0L, "B", "Stat B");
        registry().register(stat1);
        registry().register(stat2);
        assertEquals(2, registry().getRegisteredStatistics().size());
    }

    @Test
    void getRegisteredStatisticKeysReturnsAllKeys() {
        Statistic stat1 = new SimpleStatistic(key("test", "a"), StatisticType.INT, 0, "A", "Stat A");
        Statistic stat2 = new SimpleStatistic(key("other", "b"), StatisticType.LONG, 0L, "B", "Stat B");
        registry().register(stat1);
        registry().register(stat2);
        assertEquals(2, registry().getRegisteredStatisticKeys().size());
        assertTrue(registry().getRegisteredStatisticKeys().contains(key("test", "a")));
        assertTrue(registry().getRegisteredStatisticKeys().contains(key("other", "b")));
    }
}

package com.diamonddagger590.mccore.statistic;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SimpleStatisticTest {

    @SuppressWarnings("deprecation")
    private static NamespacedKey key(String namespace, String key) {
        return new NamespacedKey(namespace, key);
    }

    @Test
    void fullConstructorPopulatesAllFields() {
        NamespacedKey key = key("test", "kills");
        SimpleStatistic stat = new SimpleStatistic(key, StatisticType.INT, 0, "Kills", "Total kills", 10);
        assertEquals(key, stat.getStatisticKey());
        assertEquals(StatisticType.INT, stat.getStatisticType());
        assertEquals(0, stat.getDefaultValue());
        assertEquals("Kills", stat.getDisplayName());
        assertEquals("Total kills", stat.getDescription());
        assertEquals(10, stat.getMaxSetSize());
    }

    @Test
    void convenienceConstructorDefaultsMaxSetSizeToNegativeOne() {
        NamespacedKey key = key("test", "deaths");
        SimpleStatistic stat = new SimpleStatistic(key, StatisticType.LONG, 0L, "Deaths", "Total deaths");
        assertEquals(-1, stat.getMaxSetSize());
    }

    @Test
    void sameKeyStatisticsAreEqual() {
        NamespacedKey key = key("test", "xp");
        SimpleStatistic stat1 = new SimpleStatistic(key, StatisticType.DOUBLE, 0.0, "XP", "Experience");
        SimpleStatistic stat2 = new SimpleStatistic(key, StatisticType.DOUBLE, 0.0, "XP", "Experience");
        assertEquals(stat1, stat2);
        assertEquals(stat1.hashCode(), stat2.hashCode(), "Equal objects must have equal hash codes");
    }

    @Test
    void differentKeyStatisticsAreNotEqual() {
        SimpleStatistic stat1 = new SimpleStatistic(key("test", "a"), StatisticType.INT, 0, "A", "Stat A");
        SimpleStatistic stat2 = new SimpleStatistic(key("test", "b"), StatisticType.INT, 0, "B", "Stat B");
        assertNotEquals(stat1, stat2);
    }
}

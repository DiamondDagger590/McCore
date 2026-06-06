package com.diamonddagger590.mccore.exception.statistic;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatisticNotRegisteredExceptionTest {

    @SuppressWarnings("deprecation")
    private static NamespacedKey key(String namespace, String key) {
        return new NamespacedKey(namespace, key);
    }

    @Test
    @DisplayName("Given a statistic key, when constructing, then the key is stored")
    void constructor_storesStatisticKey() {
        NamespacedKey key = key("myplugin", "kills");
        StatisticNotRegisteredException ex = new StatisticNotRegisteredException(key);
        assertEquals(key, ex.getStatisticKey());
    }

    @Test
    @DisplayName("Given a statistic key, when calling getMessage, then message contains the key")
    void getMessage_containsStatisticKey() {
        NamespacedKey key = key("myplugin", "kills");
        StatisticNotRegisteredException ex = new StatisticNotRegisteredException(key);

        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains(key.toString()));
    }

    @Test
    @DisplayName("StatisticNotRegisteredException is a RuntimeException")
    void statisticNotRegisteredException_isRuntimeException() {
        assertInstanceOf(RuntimeException.class, new StatisticNotRegisteredException(key("test", "stat")));
    }
}

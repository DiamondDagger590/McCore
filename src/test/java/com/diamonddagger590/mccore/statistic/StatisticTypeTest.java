package com.diamonddagger590.mccore.statistic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatisticTypeTest {

    @Test
    void allEnumValuesExist() {
        assertEquals(6, StatisticType.values().length);
        StatisticType.valueOf("INT");
        StatisticType.valueOf("LONG");
        StatisticType.valueOf("DOUBLE");
        StatisticType.valueOf("STRING");
        StatisticType.valueOf("TIMESTAMP");
        StatisticType.valueOf("SET_STRING");
    }

    @Test
    void valueOfRoundTrips() {
        for (StatisticType type : StatisticType.values()) {
            assertEquals(type, StatisticType.valueOf(type.name()));
        }
    }

    @Test
    void isNumericReturnsTrueForNumericTypes() {
        assertTrue(StatisticType.INT.isNumeric());
        assertTrue(StatisticType.LONG.isNumeric());
        assertTrue(StatisticType.DOUBLE.isNumeric());
    }

    @Test
    void isNumericReturnsFalseForNonNumericTypes() {
        assertFalse(StatisticType.STRING.isNumeric());
        assertFalse(StatisticType.TIMESTAMP.isNumeric());
        assertFalse(StatisticType.SET_STRING.isNumeric());
    }
}

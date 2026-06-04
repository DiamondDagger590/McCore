package com.diamonddagger590.mccore.statistic;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatisticEntryTest {

    @SuppressWarnings("deprecation")
    private static final NamespacedKey TEST_KEY = new NamespacedKey("mccore", "test_stat");

    @Test
    @DisplayName("Given an INT entry, when getAsInt is called, then returns the integer value")
    void getAsInt_returnsValue_whenTypeIsInt() {
        StatisticEntry entry = new StatisticEntry(TEST_KEY, StatisticType.INT, 42);
        assertEquals(42, entry.getAsInt());
    }

    @Test
    @DisplayName("Given a LONG entry, when getAsLong is called, then returns the long value")
    void getAsLong_returnsValue_whenTypeIsLong() {
        StatisticEntry entry = new StatisticEntry(TEST_KEY, StatisticType.LONG, 100_000L);
        assertEquals(100_000L, entry.getAsLong());
    }

    @Test
    @DisplayName("Given a DOUBLE entry, when getAsDouble is called, then returns the double value")
    void getAsDouble_returnsValue_whenTypeIsDouble() {
        StatisticEntry entry = new StatisticEntry(TEST_KEY, StatisticType.DOUBLE, 3.14);
        assertEquals(3.14, entry.getAsDouble(), 0.001);
    }

    @Test
    @DisplayName("Given a STRING entry, when getAsString is called, then returns the string value")
    void getAsString_returnsValue_whenTypeIsString() {
        StatisticEntry entry = new StatisticEntry(TEST_KEY, StatisticType.STRING, "hello");
        assertEquals("hello", entry.getAsString());
    }

    @Test
    @DisplayName("Given a TIMESTAMP entry, when getAsTimestamp is called, then returns the instant value")
    void getAsTimestamp_returnsValue_whenTypeIsTimestamp() {
        Instant fixedInstant = Instant.ofEpochSecond(1_000_000);
        StatisticEntry entry = new StatisticEntry(TEST_KEY, StatisticType.TIMESTAMP, fixedInstant);
        assertEquals(fixedInstant, entry.getAsTimestamp());
    }

    @Test
    @DisplayName("Given a SET_STRING entry, when getAsSetString is called, then returns an unmodifiable copy")
    void getAsSetString_returnsUnmodifiableCopy_whenTypeIsSetString() {
        Set<String> original = new LinkedHashSet<>();
        original.add("a");
        original.add("b");
        StatisticEntry entry = new StatisticEntry(TEST_KEY, StatisticType.SET_STRING, original);

        Set<String> result = entry.getAsSetString();
        assertEquals(2, result.size());
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
        assertNotSame(original, result);
        assertThrows(UnsupportedOperationException.class, () -> result.add("c"));
    }

    @Test
    @DisplayName("Given a SET_STRING entry, when original set is modified after getting copy, then copy is unaffected")
    void getAsSetString_isDefensiveCopy_whenOriginalModified() {
        Set<String> original = new LinkedHashSet<>();
        original.add("x");
        StatisticEntry entry = new StatisticEntry(TEST_KEY, StatisticType.SET_STRING, original);

        Set<String> result = entry.getAsSetString();
        original.add("y");

        assertEquals(1, result.size());
        assertTrue(result.contains("x"));
    }

    @Test
    @DisplayName("Given a STRING entry, when getAsInt is called, then throws ClassCastException")
    void getAsInt_throwsClassCast_whenValueIsNotInteger() {
        StatisticEntry entry = new StatisticEntry(TEST_KEY, StatisticType.STRING, "not-a-number");
        assertThrows(ClassCastException.class, entry::getAsInt);
    }

    @Test
    @DisplayName("Given an INT entry, when getAsString is called, then throws ClassCastException")
    void getAsString_throwsClassCast_whenValueIsNotString() {
        StatisticEntry entry = new StatisticEntry(TEST_KEY, StatisticType.INT, 42);
        assertThrows(ClassCastException.class, entry::getAsString);
    }

    @Test
    @DisplayName("Given an INT entry, when getAsLong is called, then throws ClassCastException")
    void getAsLong_throwsClassCast_whenValueIsNotLong() {
        StatisticEntry entry = new StatisticEntry(TEST_KEY, StatisticType.INT, 42);
        assertThrows(ClassCastException.class, entry::getAsLong);
    }

    @Test
    @DisplayName("Given a STRING entry, when getAsTimestamp is called, then throws ClassCastException")
    void getAsTimestamp_throwsClassCast_whenValueIsNotInstant() {
        StatisticEntry entry = new StatisticEntry(TEST_KEY, StatisticType.STRING, "not-an-instant");
        assertThrows(ClassCastException.class, entry::getAsTimestamp);
    }

    @Test
    @DisplayName("Given an entry, when record accessors are called, then returns constructor values")
    void recordAccessors_returnConstructorValues() {
        StatisticEntry entry = new StatisticEntry(TEST_KEY, StatisticType.INT, 99);
        assertEquals(TEST_KEY, entry.key());
        assertEquals(StatisticType.INT, entry.type());
        assertEquals(99, entry.value());
    }

    @Test
    @DisplayName("Given two entries with same values, when equals is called, then returns true")
    void equals_returnsTrue_whenSameValues() {
        StatisticEntry a = new StatisticEntry(TEST_KEY, StatisticType.INT, 1);
        StatisticEntry b = new StatisticEntry(TEST_KEY, StatisticType.INT, 1);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}

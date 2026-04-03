package com.diamonddagger590.mccore.database.table.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link PlayerStatisticDAO}'s SET_STRING serialization and deserialization.
 * These are pure-Java round-trip tests with no database or Bukkit dependency.
 */
class PlayerStatisticDAOSerializationTest {

    @DisplayName("Empty set serializes to [] and round-trips")
    @Test
    void emptySet_roundTrips() {
        Set<String> original = new LinkedHashSet<>();
        String json = PlayerStatisticDAO.serializeStringSet(original);
        assertEquals("[]", json);
        Set<String> result = PlayerStatisticDAO.deserializeStringSet(json);
        assertTrue(result.isEmpty());
    }

    @DisplayName("Single element round-trips")
    @Test
    void singleElement_roundTrips() {
        Set<String> original = new LinkedHashSet<>();
        original.add("hello");
        String json = PlayerStatisticDAO.serializeStringSet(original);
        Set<String> result = PlayerStatisticDAO.deserializeStringSet(json);
        assertEquals(original, result);
    }

    @DisplayName("Multiple elements round-trip preserving order")
    @Test
    void multipleElements_roundTrips() {
        Set<String> original = new LinkedHashSet<>();
        original.add("alpha");
        original.add("beta");
        original.add("gamma");
        String json = PlayerStatisticDAO.serializeStringSet(original);
        Set<String> result = PlayerStatisticDAO.deserializeStringSet(json);
        assertEquals(original, result);
    }

    @DisplayName("Element containing a comma round-trips")
    @Test
    void elementWithComma_roundTrips() {
        Set<String> original = new LinkedHashSet<>();
        original.add("hello,world");
        original.add("normal");
        String json = PlayerStatisticDAO.serializeStringSet(original);
        Set<String> result = PlayerStatisticDAO.deserializeStringSet(json);
        assertEquals(original, result);
    }

    @DisplayName("Element containing a double quote round-trips")
    @Test
    void elementWithQuote_roundTrips() {
        Set<String> original = new LinkedHashSet<>();
        original.add("say \"hello\"");
        original.add("normal");
        String json = PlayerStatisticDAO.serializeStringSet(original);
        Set<String> result = PlayerStatisticDAO.deserializeStringSet(json);
        assertEquals(original, result);
    }

    @DisplayName("Element containing a backslash round-trips")
    @Test
    void elementWithBackslash_roundTrips() {
        Set<String> original = new LinkedHashSet<>();
        original.add("path\\to\\file");
        original.add("normal");
        String json = PlayerStatisticDAO.serializeStringSet(original);
        Set<String> result = PlayerStatisticDAO.deserializeStringSet(json);
        assertEquals(original, result);
    }

    @DisplayName("Element containing both backslash and quote round-trips")
    @Test
    void elementWithBackslashAndQuote_roundTrips() {
        Set<String> original = new LinkedHashSet<>();
        original.add("escaped\\\"quote");
        String json = PlayerStatisticDAO.serializeStringSet(original);
        Set<String> result = PlayerStatisticDAO.deserializeStringSet(json);
        assertEquals(original, result);
    }

    @DisplayName("Deserializing empty string returns empty set")
    @Test
    void emptyString_returnsEmptySet() {
        Set<String> result = PlayerStatisticDAO.deserializeStringSet("");
        assertTrue(result.isEmpty());
    }

    @DisplayName("Deserializing [] returns empty set")
    @Test
    void emptyBrackets_returnsEmptySet() {
        Set<String> result = PlayerStatisticDAO.deserializeStringSet("[]");
        assertTrue(result.isEmpty());
    }
}

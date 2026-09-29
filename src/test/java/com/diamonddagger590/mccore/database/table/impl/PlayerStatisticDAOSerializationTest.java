package com.diamonddagger590.mccore.database.table.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @DisplayName("Control characters are stripped during serialization")
    @Test
    void controlCharacters_strippedOnSerialize() {
        Set<String> original = new LinkedHashSet<>();
        original.add("hello\nworld");
        original.add("null\0byte");
        original.add("tab\there");
        String json = PlayerStatisticDAO.serializeStringSet(original);
        assertFalse(json.contains("\n"));
        assertFalse(json.contains("\0"));
        assertFalse(json.contains("\t"));
        Set<String> result = PlayerStatisticDAO.deserializeStringSet(json);
        assertTrue(result.contains("helloworld"));
        assertTrue(result.contains("nullbyte"));
        assertTrue(result.contains("tabhere"));
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

    @DisplayName("Given unquoted elements, when deserializing, then parses them correctly")
    @Test
    void unquotedElements_parsedCorrectly() {
        Set<String> result = PlayerStatisticDAO.deserializeStringSet("[hello,world]");
        assertEquals(2, result.size());
        assertTrue(result.contains("hello"));
        assertTrue(result.contains("world"));
    }

    @DisplayName("Given single unquoted element, when deserializing, then parses it correctly")
    @Test
    void singleUnquotedElement_parsedCorrectly() {
        Set<String> result = PlayerStatisticDAO.deserializeStringSet("[hello]");
        assertEquals(1, result.size());
        assertTrue(result.contains("hello"));
    }

    @DisplayName("Given backslash followed by non-special character, when deserializing, then preserves both characters")
    @Test
    void unknownEscapeSequence_preservesBothCharacters() {
        Set<String> result = PlayerStatisticDAO.deserializeStringSet("[\"hello\\nworld\"]");
        assertEquals(1, result.size());
        assertTrue(result.contains("hello\\nworld"));
    }

    @DisplayName("Given elements with leading whitespace, when deserializing, then trims whitespace")
    @Test
    void elementsWithWhitespace_trimmedCorrectly() {
        Set<String> result = PlayerStatisticDAO.deserializeStringSet("[\"alpha\", \"beta\"]");
        assertEquals(2, result.size());
        assertTrue(result.contains("alpha"));
        assertTrue(result.contains("beta"));
    }

    @DisplayName("Given brackets with only whitespace, when deserializing, then returns empty set")
    @Test
    void bracketsWithWhitespace_returnsEmptySet() {
        Set<String> result = PlayerStatisticDAO.deserializeStringSet("[   ]");
        assertTrue(result.isEmpty());
    }

    @DisplayName("Given unquoted elements with whitespace, when deserializing, then trims values")
    @Test
    void unquotedElementsWithWhitespace_trimmed() {
        Set<String> result = PlayerStatisticDAO.deserializeStringSet("[ alpha , beta ]");
        assertEquals(2, result.size());
        assertTrue(result.contains("alpha"));
        assertTrue(result.contains("beta"));
    }

    @DisplayName("Given backslash at end of quoted element, when deserializing, then treats as literal backslash")
    @Test
    void backslashAtEndOfQuotedElement_treatedAsLiteralEnd() {
        Set<String> result = PlayerStatisticDAO.deserializeStringSet("[\"test\\\\\"]");
        assertEquals(1, result.size());
        assertTrue(result.contains("test\\"));
    }
}

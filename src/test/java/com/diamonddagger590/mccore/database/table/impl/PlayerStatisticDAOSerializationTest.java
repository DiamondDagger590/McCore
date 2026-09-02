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

    @DisplayName("Unquoted elements are parsed correctly")
    @Test
    void unquotedElements_parsedCorrectly() {
        Set<String> result = PlayerStatisticDAO.deserializeStringSet("[foo,bar,baz]");
        assertEquals(3, result.size());
        assertTrue(result.contains("foo"));
        assertTrue(result.contains("bar"));
        assertTrue(result.contains("baz"));
    }

    @DisplayName("Unquoted single element without trailing comma is parsed")
    @Test
    void unquotedSingleElement_parsed() {
        Set<String> result = PlayerStatisticDAO.deserializeStringSet("[hello]");
        assertEquals(1, result.size());
        assertTrue(result.contains("hello"));
    }

    @DisplayName("Whitespace between quoted elements is skipped")
    @Test
    void whitespace_betweenQuotedElements_skipped() {
        Set<String> result = PlayerStatisticDAO.deserializeStringSet("[\"alpha\" , \"beta\" , \"gamma\"]");
        assertEquals(3, result.size());
        assertTrue(result.contains("alpha"));
        assertTrue(result.contains("beta"));
        assertTrue(result.contains("gamma"));
    }

    @DisplayName("Leading whitespace before first element is skipped")
    @Test
    void leadingWhitespace_skipped() {
        Set<String> result = PlayerStatisticDAO.deserializeStringSet("[   \"hello\"]");
        assertEquals(1, result.size());
        assertTrue(result.contains("hello"));
    }

    @DisplayName("Whitespace-only content after bracket removal returns empty set")
    @Test
    void whitespaceOnly_returnsEmpty() {
        Set<String> result = PlayerStatisticDAO.deserializeStringSet("[   ]");
        assertTrue(result.isEmpty());
    }

    @DisplayName("Unknown escape sequences are preserved literally")
    @Test
    void unknownEscapeSequence_preservedLiterally() {
        String input = "[\"hello\\nworld\"]";
        Set<String> result = PlayerStatisticDAO.deserializeStringSet(input);
        assertEquals(1, result.size());
        assertTrue(result.contains("hello\\nworld"));
    }

    @DisplayName("Multiple unknown escape sequences are preserved")
    @Test
    void multipleUnknownEscapes_preserved() {
        String input = "[\"\\t\\r\\n\"]";
        Set<String> result = PlayerStatisticDAO.deserializeStringSet(input);
        assertEquals(1, result.size());
        assertTrue(result.contains("\\t\\r\\n"));
    }

    @DisplayName("Unquoted elements with whitespace are trimmed")
    @Test
    void unquotedElements_trimmed() {
        Set<String> result = PlayerStatisticDAO.deserializeStringSet("[ foo , bar ]");
        assertEquals(2, result.size());
        assertTrue(result.contains("foo"));
        assertTrue(result.contains("bar"));
    }

    @DisplayName("Mixed quoted and unquoted is not produced by serializer but parses gracefully")
    @Test
    void mixedQuotedAndUnquoted_parsesGracefully() {
        Set<String> result = PlayerStatisticDAO.deserializeStringSet("[\"quoted\",unquoted]");
        assertEquals(2, result.size());
        assertTrue(result.contains("quoted"));
        assertTrue(result.contains("unquoted"));
    }
}

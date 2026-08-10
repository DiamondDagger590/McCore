package com.diamonddagger590.mccore.database.table.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Nested;

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

    @Nested
    @DisplayName("deserializeStringSet branch coverage")
    class DeserializeStringSetBranchCoverage {

        @DisplayName("Given whitespace between quoted elements, when deserialized, then elements are parsed correctly")
        @Test
        void whitespace_betweenQuotedElements_parsedCorrectly() {
            Set<String> result = PlayerStatisticDAO.deserializeStringSet("[\"alpha\", \"beta\"]");
            assertEquals(2, result.size());
            assertTrue(result.contains("alpha"));
            assertTrue(result.contains("beta"));
        }

        @DisplayName("Given leading whitespace inside brackets, when deserialized, then elements are parsed correctly")
        @Test
        void leadingWhitespace_parsedCorrectly() {
            Set<String> result = PlayerStatisticDAO.deserializeStringSet("[  \"hello\"]");
            assertEquals(1, result.size());
            assertTrue(result.contains("hello"));
        }

        @DisplayName("Given only whitespace inside brackets, when deserialized, then returns empty set")
        @Test
        void whitespaceOnly_returnsEmptySet() {
            Set<String> result = PlayerStatisticDAO.deserializeStringSet("[   ]");
            assertTrue(result.isEmpty());
        }

        @DisplayName("Given unquoted single element, when deserialized, then element is parsed")
        @Test
        void unquotedSingleElement_parsed() {
            Set<String> result = PlayerStatisticDAO.deserializeStringSet("[foo]");
            assertEquals(1, result.size());
            assertTrue(result.contains("foo"));
        }

        @DisplayName("Given unquoted elements with comma separator, when deserialized, then all elements are parsed")
        @Test
        void unquotedElements_withComma_allParsed() {
            Set<String> result = PlayerStatisticDAO.deserializeStringSet("[foo,bar,baz]");
            assertEquals(3, result.size());
            assertTrue(result.contains("foo"));
            assertTrue(result.contains("bar"));
            assertTrue(result.contains("baz"));
        }

        @DisplayName("Given unquoted elements with whitespace, when deserialized, then elements are trimmed")
        @Test
        void unquotedElements_withWhitespace_trimmed() {
            Set<String> result = PlayerStatisticDAO.deserializeStringSet("[ foo , bar ]");
            assertEquals(2, result.size());
            assertTrue(result.contains("foo"));
            assertTrue(result.contains("bar"));
        }

        @DisplayName("Given unknown escape sequence in quoted element, when deserialized, then both characters are preserved")
        @Test
        void unknownEscapeSequence_bothCharsPreserved() {
            Set<String> result = PlayerStatisticDAO.deserializeStringSet("[\"hello\\nworld\"]");
            assertEquals(1, result.size());
            assertTrue(result.contains("hello\\nworld"));
        }

        @DisplayName("Given backslash at end of input inside quoted element, when deserialized, then backslash is included")
        @Test
        void backslashAtEndOfInput_included() {
            // Inner content is: "test\ — backslash is the very last char of inner,
            // so i+1 >= inner.length() and the backslash falls to the else branch
            Set<String> result = PlayerStatisticDAO.deserializeStringSet("[\"test\\]");
            assertEquals(1, result.size());
            assertTrue(result.contains("test\\"));
        }

        @DisplayName("Given whitespace and comma after quoted element, when deserialized, then separator is skipped")
        @Test
        void whitespaceAndCommaAfterElement_separatorSkipped() {
            Set<String> result = PlayerStatisticDAO.deserializeStringSet("[\"a\" , \"b\"]");
            assertEquals(2, result.size());
            assertTrue(result.contains("a"));
            assertTrue(result.contains("b"));
        }

        @DisplayName("Given mixed quoted and unquoted elements, when deserialized, then both types are parsed")
        @Test
        void mixedQuotedAndUnquoted_bothParsed() {
            Set<String> result = PlayerStatisticDAO.deserializeStringSet("[\"quoted\",unquoted]");
            assertEquals(2, result.size());
            assertTrue(result.contains("quoted"));
            assertTrue(result.contains("unquoted"));
        }

        @DisplayName("Given multiple unknown escape sequences, when deserialized, then all are preserved as-is")
        @Test
        void multipleUnknownEscapes_allPreserved() {
            Set<String> result = PlayerStatisticDAO.deserializeStringSet("[\"\\a\\b\\c\"]");
            assertEquals(1, result.size());
            assertTrue(result.contains("\\a\\b\\c"));
        }

        @DisplayName("Given trailing whitespace after last quoted element, when deserialized, then parses correctly")
        @Test
        void trailingWhitespaceAfterLastElement_parsesCorrectly() {
            Set<String> result = PlayerStatisticDAO.deserializeStringSet("[\"only\"   ]");
            assertEquals(1, result.size());
            assertTrue(result.contains("only"));
        }
    }
}

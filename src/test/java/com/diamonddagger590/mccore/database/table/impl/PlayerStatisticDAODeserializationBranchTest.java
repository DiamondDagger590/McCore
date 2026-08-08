package com.diamonddagger590.mccore.database.table.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for uncovered branches in {@link PlayerStatisticDAO#deserializeStringSet(String)},
 * specifically the unquoted element path and unknown escape sequences.
 */
class PlayerStatisticDAODeserializationBranchTest {

    @Nested
    @DisplayName("Unquoted elements")
    class UnquotedElements {

        @Test
        @DisplayName("Given an unquoted single element, when deserialized, then returns set with that element")
        void singleUnquotedElement_parsedCorrectly() {
            Set<String> result = PlayerStatisticDAO.deserializeStringSet("[hello]");
            assertEquals(1, result.size());
            assertTrue(result.contains("hello"));
        }

        @Test
        @DisplayName("Given multiple unquoted elements, when deserialized, then returns set with all elements")
        void multipleUnquotedElements_parsedCorrectly() {
            Set<String> result = PlayerStatisticDAO.deserializeStringSet("[alpha,beta,gamma]");
            assertEquals(3, result.size());
            assertTrue(result.contains("alpha"));
            assertTrue(result.contains("beta"));
            assertTrue(result.contains("gamma"));
        }

        @Test
        @DisplayName("Given unquoted elements with spaces, when deserialized, then trims whitespace")
        void unquotedElementsWithSpaces_trimmed() {
            Set<String> result = PlayerStatisticDAO.deserializeStringSet("[ hello , world ]");
            assertEquals(2, result.size());
            assertTrue(result.contains("hello"));
            assertTrue(result.contains("world"));
        }
    }

    @Nested
    @DisplayName("Unknown escape sequences")
    class UnknownEscapeSequences {

        @Test
        @DisplayName("Given a quoted element with an unknown escape like \\n, when deserialized, then preserves both characters")
        void unknownEscape_preservesBothCharacters() {
            Set<String> result = PlayerStatisticDAO.deserializeStringSet("[\"hello\\nworld\"]");
            assertEquals(1, result.size());
            assertTrue(result.contains("hello\\nworld"));
        }

        @Test
        @DisplayName("Given a quoted element with an unknown escape like \\t, when deserialized, then preserves both characters")
        void unknownEscapeTab_preservesBothCharacters() {
            Set<String> result = PlayerStatisticDAO.deserializeStringSet("[\"tab\\there\"]");
            assertEquals(1, result.size());
            assertTrue(result.contains("tab\\there"));
        }
    }

    @Nested
    @DisplayName("Whitespace edge cases")
    class WhitespaceEdgeCases {

        @Test
        @DisplayName("Given leading whitespace before a quoted element, when deserialized, then skips whitespace and parses correctly")
        void leadingWhitespaceBeforeQuotedElement_parsedCorrectly() {
            Set<String> result = PlayerStatisticDAO.deserializeStringSet("[  \"hello\"]");
            assertEquals(1, result.size());
            assertTrue(result.contains("hello"));
        }

        @Test
        @DisplayName("Given whitespace between quoted elements, when deserialized, then parses all elements")
        void whitespaceBetweenQuotedElements_parsedCorrectly() {
            Set<String> result = PlayerStatisticDAO.deserializeStringSet("[\"alpha\" , \"beta\"]");
            assertEquals(2, result.size());
            assertTrue(result.contains("alpha"));
            assertTrue(result.contains("beta"));
        }

        @Test
        @DisplayName("Given only whitespace inside brackets, when deserialized, then returns empty set")
        void whitespaceOnlyInsideBrackets_returnsEmptySet() {
            Set<String> result = PlayerStatisticDAO.deserializeStringSet("[   ]");
            assertTrue(result.isEmpty());
        }
    }
}

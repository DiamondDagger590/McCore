package com.diamonddagger590.mccore.pair;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PairTest {

    @Test
    void immutablePairOfCreatesWithCorrectValues() {
        ImmutablePair<String, Integer> pair = ImmutablePair.of("hello", 42);
        assertEquals("hello", pair.getLeft());
        assertEquals(42, pair.getRight());
    }

    @Test
    void mutablePairOfCreatesWithCorrectValues() {
        MutablePair<String, Integer> pair = MutablePair.of("hello", 42);
        assertEquals("hello", pair.getLeft());
        assertEquals(42, pair.getRight());
    }

    @Test
    void mutablePairSetLeftUpdatesValue() {
        MutablePair<String, Integer> pair = MutablePair.of("hello", 42);
        pair.setLeft("world");
        assertEquals("world", pair.getLeft());
        assertEquals(42, pair.getRight());
    }

    @Test
    void mutablePairSetRightUpdatesValue() {
        MutablePair<String, Integer> pair = MutablePair.of("hello", 42);
        pair.setRight(99);
        assertEquals("hello", pair.getLeft());
        assertEquals(99, pair.getRight());
    }

    @Test
    void mutablePairBothSidesUpdatable() {
        MutablePair<String, String> pair = MutablePair.of("a", "b");
        pair.setLeft("x");
        pair.setRight("y");
        assertEquals("x", pair.getLeft());
        assertEquals("y", pair.getRight());
    }

    @Test
    void equalPairsAreEqual() {
        ImmutablePair<String, Integer> pair1 = ImmutablePair.of("test", 1);
        ImmutablePair<String, Integer> pair2 = ImmutablePair.of("test", 1);
        assertEquals(pair1, pair2);
    }

    @Test
    void differentLeftsAreNotEqual() {
        ImmutablePair<String, Integer> pair1 = ImmutablePair.of("a", 1);
        ImmutablePair<String, Integer> pair2 = ImmutablePair.of("b", 1);
        assertNotEquals(pair1, pair2);
    }

    @Test
    void differentRightsAreNotEqual() {
        ImmutablePair<String, Integer> pair1 = ImmutablePair.of("a", 1);
        ImmutablePair<String, Integer> pair2 = ImmutablePair.of("a", 2);
        assertNotEquals(pair1, pair2);
    }

    @Test
    void equalPairsHaveSameHashCode() {
        ImmutablePair<String, Integer> pair1 = ImmutablePair.of("test", 42);
        ImmutablePair<String, Integer> pair2 = ImmutablePair.of("test", 42);
        assertEquals(pair1.hashCode(), pair2.hashCode());
    }

    @Test
    void pairEqualsSelf() {
        ImmutablePair<String, Integer> pair = ImmutablePair.of("self", 1);
        assertEquals(pair, pair);
    }

    @Test
    void pairNotEqualToNull() {
        ImmutablePair<String, Integer> pair = ImmutablePair.of("test", 1);
        assertNotEquals(null, pair);
    }

    @Test
    void pairNotEqualToDifferentType() {
        ImmutablePair<String, Integer> pair = ImmutablePair.of("test", 1);
        assertNotEquals("not a pair", pair);
    }

    @Test
    void immutableAndMutableWithSameValuesAreEqual() {
        ImmutablePair<String, Integer> immutable = ImmutablePair.of("test", 1);
        MutablePair<String, Integer> mutable = MutablePair.of("test", 1);
        assertEquals(immutable, mutable);
        assertEquals(mutable, immutable);
    }

    @Test
    void toStringContainsClassName() {
        ImmutablePair<String, Integer> immutable = ImmutablePair.of("a", 1);
        assertTrue(immutable.toString().contains("ImmutablePair"));
        assertTrue(immutable.toString().contains("a"));
        assertTrue(immutable.toString().contains("1"));

        MutablePair<String, Integer> mutable = MutablePair.of("b", 2);
        assertTrue(mutable.toString().contains("MutablePair"));
        assertTrue(mutable.toString().contains("b"));
        assertTrue(mutable.toString().contains("2"));
    }

    @Test
    void toStringFormatIsCorrect() {
        ImmutablePair<String, Integer> pair = ImmutablePair.of("left", 42);
        assertEquals("ImmutablePair(left;42)", pair.toString());
    }

    @Test
    void mutablePairToStringReflectsUpdatedValues() {
        MutablePair<String, Integer> pair = MutablePair.of("old", 1);
        pair.setLeft("new");
        pair.setRight(2);
        assertEquals("MutablePair(new;2)", pair.toString());
    }

    @Test
    void pairsWithDifferentGenericTypes() {
        ImmutablePair<Integer, Double> pair = ImmutablePair.of(1, 2.5);
        assertEquals(1, pair.getLeft());
        assertEquals(2.5, pair.getRight());
    }

    @Test
    void mutablePairEqualityAfterMutation() {
        MutablePair<String, Integer> pair1 = MutablePair.of("a", 1);
        MutablePair<String, Integer> pair2 = MutablePair.of("b", 2);
        assertNotEquals(pair1, pair2);

        pair2.setLeft("a");
        pair2.setRight(1);
        assertEquals(pair1, pair2);
    }

    private static void assertTrue(boolean condition) {
        org.junit.jupiter.api.Assertions.assertTrue(condition);
    }
}

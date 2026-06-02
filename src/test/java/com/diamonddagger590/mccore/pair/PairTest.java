package com.diamonddagger590.mccore.pair;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PairTest {

    @Test
    @DisplayName("Given left and right values, when creating ImmutablePair, then stores both values correctly")
    void of_storesBothValues_whenCreatingImmutablePair() {
        ImmutablePair<String, Integer> pair = ImmutablePair.of("hello", 42);
        assertEquals("hello", pair.getLeft());
        assertEquals(42, pair.getRight());
    }

    @Test
    @DisplayName("Given left and right values, when creating MutablePair, then stores both values correctly")
    void of_storesBothValues_whenCreatingMutablePair() {
        MutablePair<String, Integer> pair = MutablePair.of("hello", 42);
        assertEquals("hello", pair.getLeft());
        assertEquals(42, pair.getRight());
    }

    @Test
    @DisplayName("Given a MutablePair, when setting left value, then left updates and right is unchanged")
    void setLeft_updatesLeftOnly_whenCalled() {
        MutablePair<String, Integer> pair = MutablePair.of("hello", 42);
        pair.setLeft("world");
        assertEquals("world", pair.getLeft());
        assertEquals(42, pair.getRight());
    }

    @Test
    @DisplayName("Given a MutablePair, when setting right value, then right updates and left is unchanged")
    void setRight_updatesRightOnly_whenCalled() {
        MutablePair<String, Integer> pair = MutablePair.of("hello", 42);
        pair.setRight(99);
        assertEquals("hello", pair.getLeft());
        assertEquals(99, pair.getRight());
    }

    @Test
    @DisplayName("Given a MutablePair, when setting both sides, then both values update")
    void setLeftAndRight_updatesBothSides_whenBothCalled() {
        MutablePair<String, String> pair = MutablePair.of("a", "b");
        pair.setLeft("x");
        pair.setRight("y");
        assertEquals("x", pair.getLeft());
        assertEquals("y", pair.getRight());
    }

    @Test
    @DisplayName("Given two pairs with equal values, when comparing, then they are equal")
    void equals_returnsTrue_whenBothSidesMatch() {
        ImmutablePair<String, Integer> pair1 = ImmutablePair.of("test", 1);
        ImmutablePair<String, Integer> pair2 = ImmutablePair.of("test", 1);
        assertEquals(pair1, pair2);
    }

    @Test
    @DisplayName("Given two pairs with different left values, when comparing, then they are not equal")
    void equals_returnsFalse_whenLeftValuesDiffer() {
        ImmutablePair<String, Integer> pair1 = ImmutablePair.of("a", 1);
        ImmutablePair<String, Integer> pair2 = ImmutablePair.of("b", 1);
        assertNotEquals(pair1, pair2);
    }

    @Test
    @DisplayName("Given two pairs with different right values, when comparing, then they are not equal")
    void equals_returnsFalse_whenRightValuesDiffer() {
        ImmutablePair<String, Integer> pair1 = ImmutablePair.of("a", 1);
        ImmutablePair<String, Integer> pair2 = ImmutablePair.of("a", 2);
        assertNotEquals(pair1, pair2);
    }

    @Test
    @DisplayName("Given two equal pairs, when computing hash codes, then hash codes are equal")
    void hashCode_isEqual_whenPairsAreEqual() {
        ImmutablePair<String, Integer> pair1 = ImmutablePair.of("test", 42);
        ImmutablePair<String, Integer> pair2 = ImmutablePair.of("test", 42);
        assertEquals(pair1.hashCode(), pair2.hashCode());
    }

    @Test
    @DisplayName("Given a pair, when compared to itself, then returns equal")
    void equals_returnsTrue_whenComparedToSelf() {
        ImmutablePair<String, Integer> pair = ImmutablePair.of("self", 1);
        assertEquals(pair, pair);
    }

    @Test
    @DisplayName("Given a pair, when compared to null, then returns not equal")
    void equals_returnsFalse_whenComparedToNull() {
        ImmutablePair<String, Integer> pair = ImmutablePair.of("test", 1);
        assertNotEquals(null, pair);
    }

    @Test
    @DisplayName("Given a pair, when compared to a non-Pair object, then returns not equal")
    void equals_returnsFalse_whenComparedToDifferentType() {
        ImmutablePair<String, Integer> pair = ImmutablePair.of("test", 1);
        assertNotEquals("not a pair", pair);
    }

    @Test
    @DisplayName("Given an ImmutablePair and MutablePair with same values, when comparing, then they are equal")
    void equals_returnsTrue_whenImmutableAndMutableHaveSameValues() {
        ImmutablePair<String, Integer> immutable = ImmutablePair.of("test", 1);
        MutablePair<String, Integer> mutable = MutablePair.of("test", 1);
        assertEquals(immutable, mutable);
        assertEquals(mutable, immutable);
    }

    @Test
    @DisplayName("Given an ImmutablePair, when calling toString, then includes class name and values")
    void toString_containsClassNameAndValues_whenCalledOnImmutablePair() {
        ImmutablePair<String, Integer> pair = ImmutablePair.of("left", 42);
        assertEquals("ImmutablePair(left;42)", pair.toString());
    }

    @Test
    @DisplayName("Given a MutablePair with updated values, when calling toString, then reflects new values")
    void toString_reflectsUpdatedValues_whenMutablePairMutated() {
        MutablePair<String, Integer> pair = MutablePair.of("old", 1);
        pair.setLeft("new");
        pair.setRight(2);
        assertEquals("MutablePair(new;2)", pair.toString());
    }

    @Test
    @DisplayName("Given two MutablePairs with initially different values, when mutated to match, then they are equal")
    void equals_returnsTrue_whenMutablePairMutatedToMatch() {
        MutablePair<String, Integer> pair1 = MutablePair.of("a", 1);
        MutablePair<String, Integer> pair2 = MutablePair.of("b", 2);
        assertNotEquals(pair1, pair2);

        pair2.setLeft("a");
        pair2.setRight(1);
        assertEquals(pair1, pair2);
    }
}

package com.diamonddagger590.mccore.util.comparator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChainComparatorTest {

    @Test
    @DisplayName("Given a single comparator, when comparing, then delegates to that comparator")
    void compare_delegatesToSingleComparator_whenOnlyOneProvided() {
        ChainComparator<Integer> chain = new ChainComparator<>(Comparator.<Integer>naturalOrder());
        assertTrue(chain.compare(1, 2) < 0);
        assertTrue(chain.compare(2, 1) > 0);
        assertEquals(0, chain.compare(5, 5));
    }

    @Test
    @DisplayName("Given two comparators where the first differentiates, when comparing, then first comparator decides")
    void compare_usesFirstComparator_whenFirstComparatorDifferentiates() {
        Comparator<String> byLength = Comparator.comparingInt(String::length);
        Comparator<String> alphabetical = Comparator.naturalOrder();

        ChainComparator<String> chain = new ChainComparator<>(byLength, alphabetical);

        assertTrue(chain.compare("a", "bb") < 0);
        assertTrue(chain.compare("bb", "a") > 0);
    }

    @Test
    @DisplayName("Given two comparators where the first ties, when comparing, then second comparator breaks tie")
    void compare_usesSecondComparator_whenFirstComparatorTies() {
        Comparator<String> byLength = Comparator.comparingInt(String::length);
        Comparator<String> alphabetical = Comparator.naturalOrder();

        ChainComparator<String> chain = new ChainComparator<>(byLength, alphabetical);

        assertTrue(chain.compare("ab", "ba") < 0);
        assertTrue(chain.compare("ba", "ab") > 0);
    }

    @Test
    @DisplayName("Given all comparators return zero, when comparing, then returns zero")
    void compare_returnsZero_whenAllComparatorsReturnZero() {
        Comparator<Integer> alwaysZero1 = (a, b) -> 0;
        Comparator<Integer> alwaysZero2 = (a, b) -> 0;

        ChainComparator<Integer> chain = new ChainComparator<>(alwaysZero1, alwaysZero2);
        assertEquals(0, chain.compare(1, 2));
    }

    @Test
    @DisplayName("Given a chain of length-then-alpha comparators, when sorting a list, then sorts by length first and alphabetically within same length")
    void compare_sortsCorrectly_whenUsedToSortList() {
        Comparator<String> byLength = Comparator.comparingInt(String::length);
        Comparator<String> alphabetical = Comparator.naturalOrder();

        ChainComparator<String> chain = new ChainComparator<>(byLength, alphabetical);

        List<String> list = new ArrayList<>(List.of("bb", "a", "ba", "c", "ab"));
        list.sort(chain);

        assertEquals(List.of("a", "c", "ab", "ba", "bb"), list);
    }

    @Test
    @DisplayName("Given three comparators for last/first/age, when comparing people with same last and first names, then age breaks tie")
    void compare_usesThirdComparator_whenFirstTwoTie() {
        record Person(String first, String last, int age) {}

        Comparator<Person> byLast = Comparator.comparing(Person::last);
        Comparator<Person> byFirst = Comparator.comparing(Person::first);
        Comparator<Person> byAge = Comparator.comparingInt(Person::age);

        ChainComparator<Person> chain = new ChainComparator<>(byLast, byFirst, byAge);

        Person alice30 = new Person("Alice", "Smith", 30);
        Person alice25 = new Person("Alice", "Smith", 25);
        Person bob30 = new Person("Bob", "Smith", 30);

        assertTrue(chain.compare(alice30, bob30) < 0);
        assertTrue(chain.compare(alice25, alice30) < 0);
    }

    @Test
    @DisplayName("Given a reverse-order comparator in the chain, when comparing, then orders descending")
    void compare_ordersDescending_whenReverseComparatorUsed() {
        ChainComparator<Integer> chain = new ChainComparator<>(Comparator.<Integer>reverseOrder());

        assertTrue(chain.compare(1, 2) > 0);
        assertTrue(chain.compare(2, 1) < 0);
    }
}

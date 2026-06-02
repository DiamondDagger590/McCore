package com.diamonddagger590.mccore.util.comparator;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChainComparatorTest {

    @Test
    void singleComparatorDelegatesToIt() {
        ChainComparator<Integer> chain = new ChainComparator<>(Comparator.naturalOrder());
        assertTrue(chain.compare(1, 2) < 0);
        assertTrue(chain.compare(2, 1) > 0);
        assertEquals(0, chain.compare(5, 5));
    }

    @Test
    void firstComparatorBreaksTie() {
        Comparator<String> byLength = Comparator.comparingInt(String::length);
        Comparator<String> alphabetical = Comparator.naturalOrder();

        ChainComparator<String> chain = new ChainComparator<>(byLength, alphabetical);

        assertTrue(chain.compare("a", "bb") < 0);
        assertTrue(chain.compare("bb", "a") > 0);
    }

    @Test
    void secondComparatorUsedWhenFirstTies() {
        Comparator<String> byLength = Comparator.comparingInt(String::length);
        Comparator<String> alphabetical = Comparator.naturalOrder();

        ChainComparator<String> chain = new ChainComparator<>(byLength, alphabetical);

        assertTrue(chain.compare("ab", "ba") < 0);
        assertTrue(chain.compare("ba", "ab") > 0);
    }

    @Test
    void returnsZeroWhenAllComparatorsReturnZero() {
        Comparator<Integer> alwaysZero1 = (a, b) -> 0;
        Comparator<Integer> alwaysZero2 = (a, b) -> 0;

        ChainComparator<Integer> chain = new ChainComparator<>(alwaysZero1, alwaysZero2);
        assertEquals(0, chain.compare(1, 2));
    }

    @Test
    void sortsListCorrectlyWithChain() {
        Comparator<String> byLength = Comparator.comparingInt(String::length);
        Comparator<String> alphabetical = Comparator.naturalOrder();

        ChainComparator<String> chain = new ChainComparator<>(byLength, alphabetical);

        List<String> list = new ArrayList<>(List.of("bb", "a", "ba", "c", "ab"));
        list.sort(chain);

        assertEquals(List.of("a", "c", "ab", "ba", "bb"), list);
    }

    @Test
    void threeComparatorChain() {
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
    void reverseComparatorInChain() {
        Comparator<Integer> descending = Comparator.reverseOrder();
        ChainComparator<Integer> chain = new ChainComparator<>(descending);

        assertTrue(chain.compare(1, 2) > 0);
        assertTrue(chain.compare(2, 1) < 0);
    }
}

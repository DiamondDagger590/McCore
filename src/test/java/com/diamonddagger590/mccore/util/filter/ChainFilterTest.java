package com.diamonddagger590.mccore.util.filter;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChainFilterTest {

    @Test
    void singleFilterApplied() {
        Filter<Integer> evensOnly = collection ->
                collection.stream().filter(n -> n % 2 == 0).collect(Collectors.toList());

        ChainFilter<Integer> chain = new ChainFilter<>(evensOnly);
        Collection<Integer> result = chain.filter(new ArrayList<>(List.of(1, 2, 3, 4, 5, 6)));

        assertEquals(List.of(2, 4, 6), new ArrayList<>(result));
    }

    @Test
    void multipleFiltersAppliedInOrder() {
        Filter<Integer> evensOnly = collection ->
                collection.stream().filter(n -> n % 2 == 0).collect(Collectors.toList());

        Filter<Integer> greaterThanThree = collection ->
                collection.stream().filter(n -> n > 3).collect(Collectors.toList());

        ChainFilter<Integer> chain = new ChainFilter<>(evensOnly, greaterThanThree);
        Collection<Integer> result = chain.filter(new ArrayList<>(List.of(1, 2, 3, 4, 5, 6)));

        assertEquals(List.of(4, 6), new ArrayList<>(result));
    }

    @Test
    void filterOrderMatters() {
        Filter<String> takeFirstTwo = collection ->
                collection.stream().limit(2).collect(Collectors.toList());

        Filter<String> keepLong = collection ->
                collection.stream().filter(s -> s.length() > 3).collect(Collectors.toList());

        ChainFilter<String> longThenLimit = new ChainFilter<>(keepLong, takeFirstTwo);
        ChainFilter<String> limitThenLong = new ChainFilter<>(takeFirstTwo, keepLong);

        List<String> input = new ArrayList<>(List.of("ab", "abcde", "cd", "efghi", "fghij"));

        Collection<String> result1 = longThenLimit.filter(new ArrayList<>(input));
        assertEquals(List.of("abcde", "efghi"), new ArrayList<>(result1));

        Collection<String> result2 = limitThenLong.filter(new ArrayList<>(input));
        assertEquals(List.of("abcde"), new ArrayList<>(result2));
    }

    @Test
    void emptyInputReturnsEmpty() {
        Filter<Integer> evensOnly = collection ->
                collection.stream().filter(n -> n % 2 == 0).collect(Collectors.toList());

        ChainFilter<Integer> chain = new ChainFilter<>(evensOnly);
        Collection<Integer> result = chain.filter(new ArrayList<>());

        assertTrue(result.isEmpty());
    }

    @Test
    void filterThatRemovesAllReturnsEmpty() {
        Filter<Integer> removeAll = collection ->
                collection.stream().filter(n -> false).collect(Collectors.toList());

        ChainFilter<Integer> chain = new ChainFilter<>(removeAll);
        Collection<Integer> result = chain.filter(new ArrayList<>(List.of(1, 2, 3)));

        assertTrue(result.isEmpty());
    }

    @Test
    void identityFilterReturnsAllElements() {
        Filter<String> identity = collection -> collection;

        ChainFilter<String> chain = new ChainFilter<>(identity);
        Collection<String> input = new ArrayList<>(List.of("a", "b", "c"));
        Collection<String> result = chain.filter(input);

        assertEquals(List.of("a", "b", "c"), new ArrayList<>(result));
    }

    @Test
    void threeFiltersChained() {
        Filter<Integer> removeNegatives = collection ->
                collection.stream().filter(n -> n >= 0).collect(Collectors.toList());

        Filter<Integer> evensOnly = collection ->
                collection.stream().filter(n -> n % 2 == 0).collect(Collectors.toList());

        Filter<Integer> lessThanTen = collection ->
                collection.stream().filter(n -> n < 10).collect(Collectors.toList());

        ChainFilter<Integer> chain = new ChainFilter<>(removeNegatives, evensOnly, lessThanTen);
        Collection<Integer> result = chain.filter(
                new ArrayList<>(List.of(-2, -1, 0, 1, 2, 3, 4, 5, 6, 10, 12)));

        assertEquals(List.of(0, 2, 4, 6), new ArrayList<>(result));
    }

    @Test
    void stringFilterChain() {
        Filter<String> nonEmpty = collection ->
                collection.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());

        Filter<String> startsWithA = collection ->
                collection.stream().filter(s -> s.startsWith("a")).collect(Collectors.toList());

        ChainFilter<String> chain = new ChainFilter<>(nonEmpty, startsWithA);
        Collection<String> result = chain.filter(
                new ArrayList<>(List.of("apple", "", "banana", "avocado", "cherry", "")));

        assertEquals(List.of("apple", "avocado"), new ArrayList<>(result));
    }
}

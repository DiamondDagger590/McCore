package com.diamonddagger590.mccore.util.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChainFilterTest {

    @Test
    @DisplayName("Given a single evens-only filter, when filtering a list, then returns only even numbers")
    void filter_returnsEvenNumbers_whenSingleEvensFilterApplied() {
        Filter<Integer> evensOnly = collection ->
                collection.stream().filter(n -> n % 2 == 0).collect(Collectors.toList());

        ChainFilter<Integer> chain = new ChainFilter<>(evensOnly);
        Collection<Integer> result = chain.filter(new ArrayList<>(List.of(1, 2, 3, 4, 5, 6)));

        assertEquals(List.of(2, 4, 6), new ArrayList<>(result));
    }

    @Test
    @DisplayName("Given evens-only then greater-than-three filters, when filtering, then returns even numbers greater than three")
    void filter_appliesBothFilters_whenTwoFiltersChained() {
        Filter<Integer> evensOnly = collection ->
                collection.stream().filter(n -> n % 2 == 0).collect(Collectors.toList());

        Filter<Integer> greaterThanThree = collection ->
                collection.stream().filter(n -> n > 3).collect(Collectors.toList());

        ChainFilter<Integer> chain = new ChainFilter<>(evensOnly, greaterThanThree);
        Collection<Integer> result = chain.filter(new ArrayList<>(List.of(1, 2, 3, 4, 5, 6)));

        assertEquals(List.of(4, 6), new ArrayList<>(result));
    }

    @Test
    @DisplayName("Given two filters in different orders, when filtering same input, then produces different results")
    void filter_producesDifferentResults_whenFilterOrderDiffers() {
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
    @DisplayName("Given an empty input collection, when filtering, then returns empty collection")
    void filter_returnsEmpty_whenInputIsEmpty() {
        Filter<Integer> evensOnly = collection ->
                collection.stream().filter(n -> n % 2 == 0).collect(Collectors.toList());

        ChainFilter<Integer> chain = new ChainFilter<>(evensOnly);
        Collection<Integer> result = chain.filter(new ArrayList<>());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Given a filter that rejects all elements, when filtering, then returns empty collection")
    void filter_returnsEmpty_whenFilterRejectsAll() {
        Filter<Integer> removeAll = collection ->
                collection.stream().filter(n -> false).collect(Collectors.toList());

        ChainFilter<Integer> chain = new ChainFilter<>(removeAll);
        Collection<Integer> result = chain.filter(new ArrayList<>(List.of(1, 2, 3)));

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Given an identity filter, when filtering, then returns all original elements")
    void filter_returnsAllElements_whenIdentityFilterUsed() {
        Filter<String> identity = collection -> collection;

        ChainFilter<String> chain = new ChainFilter<>(identity);
        Collection<String> input = new ArrayList<>(List.of("a", "b", "c"));
        Collection<String> result = chain.filter(input);

        assertEquals(List.of("a", "b", "c"), new ArrayList<>(result));
    }

    @Test
    @DisplayName("Given three chained filters (non-negative, even, less-than-ten), when filtering, then all three conditions apply")
    void filter_appliesAllThreeConditions_whenThreeFiltersChained() {
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
    @DisplayName("Given non-empty and starts-with-a filters, when filtering strings, then returns only non-empty strings starting with 'a'")
    void filter_returnsMatchingStrings_whenStringFiltersChained() {
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

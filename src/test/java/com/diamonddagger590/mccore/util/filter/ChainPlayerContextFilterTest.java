package com.diamonddagger590.mccore.util.filter;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChainPlayerContextFilterTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();
    private TestCorePlayer testPlayer;

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();
        testPlayer = new TestCorePlayer(PLAYER_UUID);
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
    }

    @Test
    @DisplayName("Given a single filter, when filtering, then applies that filter correctly")
    void filter_appliesSingleFilter_correctly() {
        PlayerContextFilter<Integer, TestCorePlayer> evenOnly = (player, list) ->
                list.stream().filter(i -> i % 2 == 0).collect(Collectors.toList());

        ChainPlayerContextFilter<Integer, TestCorePlayer> chain = new ChainPlayerContextFilter<>(evenOnly);

        Collection<Integer> result = chain.filter(testPlayer, List.of(1, 2, 3, 4, 5, 6));
        assertEquals(List.of(2, 4, 6), new ArrayList<>(result));
    }

    @Test
    @DisplayName("Given two filters, when filtering, then applies both in order")
    void filter_appliesMultipleFilters_inOrder() {
        PlayerContextFilter<Integer, TestCorePlayer> evenOnly = (player, list) ->
                list.stream().filter(i -> i % 2 == 0).collect(Collectors.toList());
        PlayerContextFilter<Integer, TestCorePlayer> greaterThanThree = (player, list) ->
                list.stream().filter(i -> i > 3).collect(Collectors.toList());

        @SuppressWarnings("unchecked")
        ChainPlayerContextFilter<Integer, TestCorePlayer> chain =
                new ChainPlayerContextFilter<>(evenOnly, greaterThanThree);

        Collection<Integer> result = chain.filter(testPlayer, List.of(1, 2, 3, 4, 5, 6, 7, 8));
        assertEquals(List.of(4, 6, 8), new ArrayList<>(result));
    }

    @Test
    @DisplayName("Given two filters in reverse order, when filtering, then order matters")
    void filter_orderMatters_forFilterChain() {
        PlayerContextFilter<Integer, TestCorePlayer> greaterThanThree = (player, list) ->
                list.stream().filter(i -> i > 3).collect(Collectors.toList());
        PlayerContextFilter<Integer, TestCorePlayer> evenOnly = (player, list) ->
                list.stream().filter(i -> i % 2 == 0).collect(Collectors.toList());

        @SuppressWarnings("unchecked")
        ChainPlayerContextFilter<Integer, TestCorePlayer> chain =
                new ChainPlayerContextFilter<>(greaterThanThree, evenOnly);

        Collection<Integer> result = chain.filter(testPlayer, List.of(1, 2, 3, 4, 5, 6, 7, 8));
        assertEquals(List.of(4, 6, 8), new ArrayList<>(result));
    }

    @Test
    @DisplayName("Given an empty input collection, when filtering, then returns empty collection")
    void filter_returnsEmpty_whenInputIsEmpty() {
        PlayerContextFilter<String, TestCorePlayer> noopFilter = (player, list) -> list;

        ChainPlayerContextFilter<String, TestCorePlayer> chain = new ChainPlayerContextFilter<>(noopFilter);

        Collection<String> result = chain.filter(testPlayer, List.of());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Given a filter that reduces to empty, when chaining with another, then result is empty")
    void filter_returnsEmpty_whenFirstFilterRemovesAll() {
        PlayerContextFilter<Integer, TestCorePlayer> removeAll = (player, list) -> List.of();
        PlayerContextFilter<Integer, TestCorePlayer> identity = (player, list) -> list;

        @SuppressWarnings("unchecked")
        ChainPlayerContextFilter<Integer, TestCorePlayer> chain =
                new ChainPlayerContextFilter<>(removeAll, identity);

        Collection<Integer> result = chain.filter(testPlayer, List.of(1, 2, 3));
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Given three chained filters, when filtering, then all three are applied sequentially")
    void filter_appliesThreeFilters_sequentially() {
        PlayerContextFilter<Integer, TestCorePlayer> removeNegatives = (player, list) ->
                list.stream().filter(i -> i >= 0).collect(Collectors.toList());
        PlayerContextFilter<Integer, TestCorePlayer> removeLargeThan10 = (player, list) ->
                list.stream().filter(i -> i <= 10).collect(Collectors.toList());
        PlayerContextFilter<Integer, TestCorePlayer> evenOnly = (player, list) ->
                list.stream().filter(i -> i % 2 == 0).collect(Collectors.toList());

        @SuppressWarnings("unchecked")
        ChainPlayerContextFilter<Integer, TestCorePlayer> chain =
                new ChainPlayerContextFilter<>(removeNegatives, removeLargeThan10, evenOnly);

        Collection<Integer> result = chain.filter(testPlayer, List.of(-5, -2, 0, 1, 2, 3, 4, 8, 11, 20));
        assertEquals(List.of(0, 2, 4, 8), new ArrayList<>(result));
    }

    @Test
    @DisplayName("Given string filters, when filtering with player context, then player object is accessible")
    void filter_passesPlayerContext_toEachFilter() {
        PlayerContextFilter<String, TestCorePlayer> filterByPlayerUUID = (player, list) ->
                list.stream().filter(s -> !s.equals(player.getUUID().toString())).collect(Collectors.toList());

        ChainPlayerContextFilter<String, TestCorePlayer> chain =
                new ChainPlayerContextFilter<>(filterByPlayerUUID);

        String playerUuidString = PLAYER_UUID.toString();
        Collection<String> result = chain.filter(testPlayer, List.of("hello", playerUuidString, "world"));
        assertEquals(List.of("hello", "world"), new ArrayList<>(result));
    }

    private static class TestCorePlayer extends CorePlayer {

        TestCorePlayer(UUID uuid) {
            super(uuid, null);
        }

        @Override
        public boolean useMutex() {
            return false;
        }
    }
}

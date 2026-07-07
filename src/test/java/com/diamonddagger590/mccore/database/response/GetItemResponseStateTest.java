package com.diamonddagger590.mccore.database.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GetItemResponseStateTest {

    @Test
    @DisplayName("Given GetItemResponseState enum, when accessing all values, then all four states exist")
    void values_returnsFourStates_always() {
        GetItemResponseState[] values = GetItemResponseState.values();
        assertEquals(4, values.length);
    }

    @Test
    @DisplayName("Given PENDING_RESPONSE, when accessed via valueOf, then returns correct constant")
    void valueOf_returnsPendingResponse_whenGivenPendingResponseString() {
        assertEquals(GetItemResponseState.PENDING_RESPONSE, GetItemResponseState.valueOf("PENDING_RESPONSE"));
    }

    @Test
    @DisplayName("Given ERRORED, when accessed via valueOf, then returns correct constant")
    void valueOf_returnsErrored_whenGivenErroredString() {
        assertEquals(GetItemResponseState.ERRORED, GetItemResponseState.valueOf("ERRORED"));
    }

    @Test
    @DisplayName("Given ITEM_FOUND, when accessed via valueOf, then returns correct constant")
    void valueOf_returnsItemFound_whenGivenItemFoundString() {
        assertEquals(GetItemResponseState.ITEM_FOUND, GetItemResponseState.valueOf("ITEM_FOUND"));
    }

    @Test
    @DisplayName("Given ITEM_NOT_FOUND, when accessed via valueOf, then returns correct constant")
    void valueOf_returnsItemNotFound_whenGivenItemNotFoundString() {
        assertEquals(GetItemResponseState.ITEM_NOT_FOUND, GetItemResponseState.valueOf("ITEM_NOT_FOUND"));
    }

    @Test
    @DisplayName("Given all enum constants, when checking non-null, then all are non-null")
    void constants_areNonNull_always() {
        for (GetItemResponseState state : GetItemResponseState.values()) {
            assertNotNull(state);
        }
    }
}

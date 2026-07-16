package com.diamonddagger590.mccore.database.transaction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TransactionStateTest {

    @Test
    @DisplayName("Enum contains exactly three values")
    void values_containsThreeEntries() {
        assertEquals(3, TransactionState.values().length);
    }

    @ParameterizedTest
    @EnumSource(TransactionState.class)
    @DisplayName("valueOf round-trips for all values")
    void valueOf_roundTrips(TransactionState state) {
        assertNotNull(TransactionState.valueOf(state.name()));
        assertEquals(state, TransactionState.valueOf(state.name()));
    }
}

package com.diamonddagger590.mccore.event.statistic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModificationTypeTest {

    @Test
    @DisplayName("Given the ModificationType enum, when calling values(), then all six types are present")
    void values_returnsAllSixTypes() {
        ModificationType[] values = ModificationType.values();
        assertEquals(6, values.length);
    }

    @Test
    @DisplayName("Given SET, when calling valueOf, then returns SET")
    void valueOf_returnsSET_whenGivenSET() {
        assertEquals(ModificationType.SET, ModificationType.valueOf("SET"));
    }

    @Test
    @DisplayName("Given INCREMENT, when calling valueOf, then returns INCREMENT")
    void valueOf_returnsINCREMENT_whenGivenINCREMENT() {
        assertEquals(ModificationType.INCREMENT, ModificationType.valueOf("INCREMENT"));
    }

    @Test
    @DisplayName("Given ADD_TO_SET, when calling valueOf, then returns ADD_TO_SET")
    void valueOf_returnsADD_TO_SET_whenGivenADD_TO_SET() {
        assertEquals(ModificationType.ADD_TO_SET, ModificationType.valueOf("ADD_TO_SET"));
    }

    @Test
    @DisplayName("Given REMOVE_FROM_SET, when calling valueOf, then returns REMOVE_FROM_SET")
    void valueOf_returnsREMOVE_FROM_SET_whenGivenREMOVE_FROM_SET() {
        assertEquals(ModificationType.REMOVE_FROM_SET, ModificationType.valueOf("REMOVE_FROM_SET"));
    }

    @Test
    @DisplayName("Given SET_MAX, when calling valueOf, then returns SET_MAX")
    void valueOf_returnsSET_MAX_whenGivenSET_MAX() {
        assertEquals(ModificationType.SET_MAX, ModificationType.valueOf("SET_MAX"));
    }

    @Test
    @DisplayName("Given SET_IF_ABSENT, when calling valueOf, then returns SET_IF_ABSENT")
    void valueOf_returnsSET_IF_ABSENT_whenGivenSET_IF_ABSENT() {
        assertEquals(ModificationType.SET_IF_ABSENT, ModificationType.valueOf("SET_IF_ABSENT"));
    }

    @Test
    @DisplayName("Given an invalid name, when calling valueOf, then throws IllegalArgumentException")
    void valueOf_throwsException_whenGivenInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> ModificationType.valueOf("INVALID"));
    }

    @Test
    @DisplayName("Given null, when calling valueOf, then throws NullPointerException")
    void valueOf_throwsNullPointerException_whenGivenNull() {
        assertThrows(NullPointerException.class, () -> ModificationType.valueOf(null));
    }

    @Test
    @DisplayName("Given each ModificationType, when calling name(), then returns the expected string")
    void name_returnsExpectedString_forEachType() {
        assertEquals("SET", ModificationType.SET.name());
        assertEquals("INCREMENT", ModificationType.INCREMENT.name());
        assertEquals("ADD_TO_SET", ModificationType.ADD_TO_SET.name());
        assertEquals("REMOVE_FROM_SET", ModificationType.REMOVE_FROM_SET.name());
        assertEquals("SET_MAX", ModificationType.SET_MAX.name());
        assertEquals("SET_IF_ABSENT", ModificationType.SET_IF_ABSENT.name());
    }

    @Test
    @DisplayName("Given each ModificationType, when calling ordinal(), then ordinals are sequential from zero")
    void ordinal_isSequentialFromZero() {
        assertEquals(0, ModificationType.SET.ordinal());
        assertEquals(1, ModificationType.INCREMENT.ordinal());
        assertEquals(2, ModificationType.ADD_TO_SET.ordinal());
        assertEquals(3, ModificationType.REMOVE_FROM_SET.ordinal());
        assertEquals(4, ModificationType.SET_MAX.ordinal());
        assertEquals(5, ModificationType.SET_IF_ABSENT.ordinal());
    }

    @Test
    @DisplayName("Given each ModificationType, when referenced, then is not null")
    void allValues_areNotNull() {
        for (ModificationType type : ModificationType.values()) {
            assertNotNull(type);
        }
    }
}

package com.diamonddagger590.mccore.builder.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemPluginTypeTest {

    @Test
    @DisplayName("Given the enum, when calling values, then returns exactly 3 constants")
    void values_returnsThreeConstants() {
        assertEquals(3, ItemPluginType.values().length);
    }

    @Test
    @DisplayName("Given the enum, when checking constants, then NEXO, ITEMS_ADDER, and NONE exist")
    void values_containsExpectedConstants() {
        assertNotNull(ItemPluginType.valueOf("NEXO"));
        assertNotNull(ItemPluginType.valueOf("ITEMS_ADDER"));
        assertNotNull(ItemPluginType.valueOf("NONE"));
    }

    // --- fromName tests ---

    @Test
    @DisplayName("Given 'nexo', when calling fromName, then returns NEXO")
    void fromName_returnsNexo_forNexoString() {
        assertEquals(ItemPluginType.NEXO, ItemPluginType.fromName("nexo"));
    }

    @Test
    @DisplayName("Given 'NEXO' (uppercase), when calling fromName, then returns NEXO")
    void fromName_returnsNexo_forUppercaseNexo() {
        assertEquals(ItemPluginType.NEXO, ItemPluginType.fromName("NEXO"));
    }

    @Test
    @DisplayName("Given 'Nexo' (mixed case), when calling fromName, then returns NEXO")
    void fromName_returnsNexo_forMixedCaseNexo() {
        assertEquals(ItemPluginType.NEXO, ItemPluginType.fromName("Nexo"));
    }

    @Test
    @DisplayName("Given 'itemsadder', when calling fromName, then returns ITEMS_ADDER")
    void fromName_returnsItemsAdder_forItemsadderString() {
        assertEquals(ItemPluginType.ITEMS_ADDER, ItemPluginType.fromName("itemsadder"));
    }

    @Test
    @DisplayName("Given 'items_adder', when calling fromName, then returns ITEMS_ADDER")
    void fromName_returnsItemsAdder_forItemsUnderscoreAdder() {
        assertEquals(ItemPluginType.ITEMS_ADDER, ItemPluginType.fromName("items_adder"));
    }

    @Test
    @DisplayName("Given 'ITEMS_ADDER' (uppercase), when calling fromName, then returns ITEMS_ADDER")
    void fromName_returnsItemsAdder_forUppercaseItemsAdder() {
        assertEquals(ItemPluginType.ITEMS_ADDER, ItemPluginType.fromName("ITEMS_ADDER"));
    }

    @Test
    @DisplayName("Given 'none', when calling fromName, then returns NONE")
    void fromName_returnsNone_forNoneString() {
        assertEquals(ItemPluginType.NONE, ItemPluginType.fromName("none"));
    }

    @Test
    @DisplayName("Given 'NONE' (uppercase), when calling fromName, then returns NONE")
    void fromName_returnsNone_forUppercaseNone() {
        assertEquals(ItemPluginType.NONE, ItemPluginType.fromName("NONE"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"unknown", "oraxen", "mmoitems", "customitems", "", "  "})
    @DisplayName("Given an unrecognized name, when calling fromName, then returns NONE as fallback")
    void fromName_returnsNone_forUnrecognizedName(String name) {
        assertEquals(ItemPluginType.NONE, ItemPluginType.fromName(name));
    }

    @Test
    @DisplayName("Given null input, when calling fromName, then throws NullPointerException")
    void fromName_throwsNullPointerException_forNullInput() {
        assertThrows(NullPointerException.class, () -> ItemPluginType.fromName(null));
    }

    @Test
    @DisplayName("Given all enum constants, when iterating, then each has a non-null name")
    void allConstants_haveNonNullName() {
        for (ItemPluginType type : ItemPluginType.values()) {
            assertNotNull(type.name());
            assertTrue(type.name().length() > 0);
        }
    }
}

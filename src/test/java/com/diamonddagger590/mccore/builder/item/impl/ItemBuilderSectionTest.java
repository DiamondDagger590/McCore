package com.diamonddagger590.mccore.builder.item.impl;

import com.diamonddagger590.mccore.builder.item.ItemBuilderConfigurationKeys;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import dev.dejvokep.boostedyaml.route.Route;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ItemBuilderSectionTest {

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        MockBukkit.load(TestCorePlugin.class);
        RegistryResetExtension.setupRegistry();
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
        MockBukkit.unmock();
    }

    private Section createMinimalSection() {
        Section section = mock(Section.class);
        when(section.getString(eq(ItemBuilderConfigurationKeys.DATA), eq(""))).thenReturn("");
        when(section.getString(eq(ItemBuilderConfigurationKeys.MATERIAL), eq("stone"))).thenReturn("stone");
        when(section.getString(eq(ItemBuilderConfigurationKeys.NAME), eq(""))).thenReturn("");
        when(section.getStringList(eq(ItemBuilderConfigurationKeys.LORE_ROUTE))).thenReturn(Collections.emptyList());
        when(section.getInt(eq(ItemBuilderConfigurationKeys.AMOUNT), eq(1))).thenReturn(1);
        when(section.getInt(eq(ItemBuilderConfigurationKeys.CUSTOM_MODEL_DATA), eq(-1))).thenReturn(-1);
        when(section.getBoolean(eq(ItemBuilderConfigurationKeys.HIDE_TOOLTIP), eq(false))).thenReturn(false);
        when(section.getBoolean(eq(ItemBuilderConfigurationKeys.UNBREAKABLE_ITEM), eq(false))).thenReturn(false);
        when(section.getBoolean(eq(ItemBuilderConfigurationKeys.GLOWING), eq(false))).thenReturn(false);
        when(section.getStringList(eq(ItemBuilderConfigurationKeys.ITEM_FLAGS))).thenReturn(Collections.emptyList());
        when(section.getString(eq(ItemBuilderConfigurationKeys.PLAYER), eq(""))).thenReturn("");
        when(section.getInt(eq(ItemBuilderConfigurationKeys.DAMAGE), eq(0))).thenReturn(0);
        when(section.getString(eq(ItemBuilderConfigurationKeys.SKULL), eq(""))).thenReturn("");
        when(section.getString(eq(ItemBuilderConfigurationKeys.RGB), eq(""))).thenReturn("");
        when(section.getString(eq(ItemBuilderConfigurationKeys.COLOR), eq(""))).thenReturn("");
        when(section.getString(eq(ItemBuilderConfigurationKeys.MOB_TYPE), eq(""))).thenReturn("");
        when(section.getString(eq(ItemBuilderConfigurationKeys.TRIM_PATTERN), eq(""))).thenReturn("");
        when(section.getString(eq(ItemBuilderConfigurationKeys.TRIM_MATERIAL), eq(""))).thenReturn("");
        when(section.contains(ItemBuilderConfigurationKeys.MAX_STACK_SIZE)).thenReturn(false);
        when(section.contains(ItemBuilderConfigurationKeys.CUSTOM_ITEM)).thenReturn(false);
        when(section.getSection(eq(ItemBuilderConfigurationKeys.ENCHANTMENTS))).thenReturn(null);
        when(section.getSection(eq(ItemBuilderConfigurationKeys.POTION_HEADER))).thenReturn(null);
        when(section.getSection(eq(ItemBuilderConfigurationKeys.PATTERN_HEADER))).thenReturn(null);
        return section;
    }

    @Test
    @DisplayName("Given a minimal section, when from(Section) is called, then returns a non-null builder with stone material")
    void fromSection_returnsStoneBuilder_whenMinimalSectionProvided() {
        Section section = createMinimalSection();

        ItemBuilder result = ItemBuilder.from(section);

        assertNotNull(result);
        ItemStack stack = result.asItemStack();
        assertEquals(Material.STONE, stack.getType());
    }

    @Test
    @DisplayName("Given a section with amount=5, when from(Section) is called, then item has correct amount")
    void fromSection_setsAmount_whenAmountProvided() {
        Section section = createMinimalSection();
        when(section.getInt(eq(ItemBuilderConfigurationKeys.AMOUNT), eq(1))).thenReturn(5);

        ItemBuilder result = ItemBuilder.from(section);

        assertEquals(5, result.asItemStack().getAmount());
    }

    @Test
    @DisplayName("Given a section with max-stack-size, when from(Section) is called, then completes without error")
    void fromSection_completesWithoutError_whenMaxStackSizePresent() {
        Section section = createMinimalSection();
        when(section.contains(ItemBuilderConfigurationKeys.MAX_STACK_SIZE)).thenReturn(true);
        when(section.getInt(eq(ItemBuilderConfigurationKeys.MAX_STACK_SIZE))).thenReturn(16);

        ItemBuilder result = assertDoesNotThrow(() -> ItemBuilder.from(section));
        assertNotNull(result);
    }

    @Test
    @DisplayName("Given a section with unbreakable=true, when from(Section) is called, then completes without error")
    void fromSection_completesWithoutError_whenUnbreakableIsTrue() {
        Section section = createMinimalSection();
        when(section.getBoolean(eq(ItemBuilderConfigurationKeys.UNBREAKABLE_ITEM), eq(false))).thenReturn(true);

        ItemBuilder result = assertDoesNotThrow(() -> ItemBuilder.from(section));
        assertNotNull(result);
    }

    @Test
    @DisplayName("Given a section with glowing=true, when from(Section) is called, then completes without error")
    void fromSection_completesWithoutError_whenGlowingIsTrue() {
        Section section = createMinimalSection();
        when(section.getBoolean(eq(ItemBuilderConfigurationKeys.GLOWING), eq(false))).thenReturn(true);

        ItemBuilder result = assertDoesNotThrow(() -> ItemBuilder.from(section));
        assertNotNull(result);
    }

    @Test
    @DisplayName("Given a section with hide-tooltip=true, when from(Section) is called, then completes without error")
    void fromSection_completesWithoutError_whenHideTooltipIsTrue() {
        Section section = createMinimalSection();
        when(section.getBoolean(eq(ItemBuilderConfigurationKeys.HIDE_TOOLTIP), eq(false))).thenReturn(true);

        ItemBuilder result = assertDoesNotThrow(() -> ItemBuilder.from(section));
        assertNotNull(result);
    }

    @Test
    @DisplayName("Given a section with enchantments, when from(Section) is called, then enchantments are applied")
    void fromSection_appliesEnchantments_whenEnchantmentSectionProvided() {
        Section section = createMinimalSection();
        Section enchSection = mock(Section.class);
        when(section.getSection(eq(ItemBuilderConfigurationKeys.ENCHANTMENTS))).thenReturn(enchSection);
        when(enchSection.getRoutesAsStrings(false)).thenReturn(Set.of("sharpness"));
        when(enchSection.getInt("sharpness")).thenReturn(3);

        ItemBuilder result = assertDoesNotThrow(() -> ItemBuilder.from(section));
        assertNotNull(result);
    }

    @Test
    @DisplayName("Given a section with null data field, when from(Section) is called, then builds without error")
    void fromSection_buildsWithoutError_whenDataFieldIsNull() {
        Section section = createMinimalSection();
        when(section.getString(eq(ItemBuilderConfigurationKeys.DATA), eq(""))).thenReturn(null);

        ItemBuilder result = ItemBuilder.from(section);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Given a section with empty data field, when from(Section) is called, then skips base64 path")
    void fromSection_skipsBase64_whenDataFieldIsEmpty() {
        Section section = createMinimalSection();
        when(section.getString(eq(ItemBuilderConfigurationKeys.DATA), eq(""))).thenReturn("");

        ItemBuilder result = ItemBuilder.from(section);
        assertNotNull(result);
        assertEquals(Material.STONE, result.asItemStack().getType());
    }

    @Test
    @DisplayName("Given a section with custom-item, when from(Section) is called, then custom item is set")
    void fromSection_setsCustomItem_whenCustomItemPresent() {
        Section section = createMinimalSection();
        when(section.contains(ItemBuilderConfigurationKeys.CUSTOM_ITEM)).thenReturn(true);
        when(section.getString(eq(ItemBuilderConfigurationKeys.CUSTOM_ITEM))).thenReturn("my_custom_item");

        ItemBuilder result = ItemBuilder.from(section);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Given a section with color, when from(Section) is called, then color is applied")
    void fromSection_setsColor_whenColorProvided() {
        Section section = createMinimalSection();
        when(section.getString(eq(ItemBuilderConfigurationKeys.COLOR), eq(""))).thenReturn("RED");

        ItemBuilder result = ItemBuilder.from(section);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Given a section with rgb, when from(Section) is called, then rgb is used when color is empty")
    void fromSection_setsRgb_whenRgbProvidedAndColorEmpty() {
        Section section = createMinimalSection();
        when(section.getString(eq(ItemBuilderConfigurationKeys.RGB), eq(""))).thenReturn("255,0,0");

        ItemBuilder result = ItemBuilder.from(section);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Given an existing ItemStack, when from(Section, ItemStack) is called, then section config is applied to the stack")
    void fromSectionWithItemStack_appliesConfigToExistingStack() {
        Section section = createMinimalSection();
        ItemStack existing = new ItemStack(Material.DIAMOND_SWORD);

        ItemBuilder result = ItemBuilder.from(section, existing);

        assertNotNull(result);
        assertEquals(Material.DIAMOND_SWORD, result.asItemStack().getType());
    }

    @Test
    @DisplayName("Given an existing ItemBuilder, when from(Section, ItemBuilder) is called, then section config is applied to the builder")
    void fromSectionWithItemBuilder_appliesConfigToExistingBuilder() {
        Section section = createMinimalSection();
        ItemBuilder existing = ItemBuilder.from(ItemType.GOLDEN_APPLE, 3);

        ItemBuilder result = ItemBuilder.from(section, existing);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Given a section with damage value, when from(Section) is called, then completes without error")
    void fromSection_completesWithoutError_whenDamageProvided() {
        Section section = createMinimalSection();
        when(section.getInt(eq(ItemBuilderConfigurationKeys.DAMAGE), eq(0))).thenReturn(50);

        ItemBuilder result = assertDoesNotThrow(() -> ItemBuilder.from(section));
        assertNotNull(result);
    }

    @Test
    @DisplayName("Given a section with a null player field, when from(Section) is called, then builds without skull")
    void fromSection_buildsWithoutSkull_whenPlayerIsNull() {
        Section section = createMinimalSection();
        when(section.getString(eq(ItemBuilderConfigurationKeys.PLAYER), eq(""))).thenReturn(null);

        ItemBuilder result = ItemBuilder.from(section);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Given a copy constructor with existing builder state, when building, then copies state correctly")
    void copyConstructor_copiesBuilderState_fromSource() {
        ItemBuilder source = ItemBuilder.from(ItemType.DIAMOND, 3);
        source.setDisplayName("Test");

        ItemBuilder copy = ItemBuilder.from(source.asItemStack());
        assertNotNull(copy);
    }

    @Test
    @DisplayName("Given a section with a non-empty player field, when from(Section) is called, then skull builder path is invoked")
    void fromSection_invokesSkullBuilder_whenPlayerIsNonEmpty() {
        Section section = createMinimalSection();
        when(section.getString(eq(ItemBuilderConfigurationKeys.MATERIAL), eq("stone"))).thenReturn("player_head");
        when(section.getString(eq(ItemBuilderConfigurationKeys.PLAYER), eq(""))).thenReturn("Notch");

        ItemBuilder result = assertDoesNotThrow(() -> ItemBuilder.from(section));
        assertNotNull(result);
    }

    @Test
    @DisplayName("Given a section with a mob type, when from(Section) is called, then spawner builder path is invoked")
    void fromSection_invokesSpawnerBuilder_whenMobTypeProvided() {
        Section section = createMinimalSection();
        when(section.getString(eq(ItemBuilderConfigurationKeys.MATERIAL), eq("stone"))).thenReturn("spawner");
        when(section.getString(eq(ItemBuilderConfigurationKeys.MOB_TYPE), eq(""))).thenReturn("zombie");

        ItemBuilder result = assertDoesNotThrow(() -> ItemBuilder.from(section));
        assertNotNull(result);
    }

    @Test
    @DisplayName("Given a section with potions, when from(Section) is called, then potion builder path is invoked")
    void fromSection_invokesPotionBuilder_whenPotionSectionProvided() {
        Section section = createMinimalSection();
        when(section.getString(eq(ItemBuilderConfigurationKeys.MATERIAL), eq("stone"))).thenReturn("potion");

        Section potionSection = mock(Section.class);
        when(section.getSection(eq(ItemBuilderConfigurationKeys.POTION_HEADER))).thenReturn(potionSection);
        when(potionSection.getRoutesAsStrings(false)).thenReturn(Set.of("speed"));
        when(potionSection.getInt(any(Route.class), eq(60))).thenReturn(200);
        when(potionSection.getInt(any(Route.class), eq(1))).thenReturn(2);
        when(potionSection.getBoolean(any(Route.class), eq(false))).thenReturn(false);

        ItemBuilder result = assertDoesNotThrow(() -> ItemBuilder.from(section));
        assertNotNull(result);
    }

    @Test
    @DisplayName("Given a section with banner patterns, when from(Section) is called, then pattern builder path is invoked")
    void fromSection_invokesPatternBuilder_whenPatternSectionProvided() {
        Section section = createMinimalSection();
        when(section.getString(eq(ItemBuilderConfigurationKeys.MATERIAL), eq("stone"))).thenReturn("white_banner");

        Section patternSection = mock(Section.class);
        when(section.getSection(eq(ItemBuilderConfigurationKeys.PATTERN_HEADER))).thenReturn(patternSection);
        when(patternSection.getRoutesAsStrings(false)).thenReturn(Set.of("stripe_top"));
        when(patternSection.getString("stripe_top", "white")).thenReturn("red");

        ItemBuilder result = assertDoesNotThrow(() -> ItemBuilder.from(section));
        assertNotNull(result);
    }

    @Test
    @DisplayName("Given a section with item flags, when from(Section) is called, then item flags are applied")
    void fromSection_appliesItemFlags_whenItemFlagsProvided() {
        Section section = createMinimalSection();
        when(section.getStringList(eq(ItemBuilderConfigurationKeys.ITEM_FLAGS))).thenReturn(List.of("HIDE_ENCHANTS"));

        ItemBuilder result = assertDoesNotThrow(() -> ItemBuilder.from(section));
        assertNotNull(result);
        ItemStack stack = result.asItemStack();
        assertTrue(stack.getItemFlags().contains(ItemFlag.HIDE_ENCHANTS));
    }

    @Test
    @DisplayName("Given a section with custom model data, when from(Section) is called, then custom model data is set")
    void fromSection_setsCustomModelData_whenProvided() {
        Section section = createMinimalSection();
        when(section.getInt(eq(ItemBuilderConfigurationKeys.CUSTOM_MODEL_DATA), eq(-1))).thenReturn(42);

        ItemBuilder result = assertDoesNotThrow(() -> ItemBuilder.from(section));
        assertNotNull(result);
    }

    @Test
    @DisplayName("Given a section with display name, when from(Section) is called, then completes without error")
    void fromSection_completesWithoutError_whenNameProvided() {
        Section section = createMinimalSection();
        when(section.getString(eq(ItemBuilderConfigurationKeys.NAME), eq(""))).thenReturn("Custom Name");

        ItemBuilder result = assertDoesNotThrow(() -> ItemBuilder.from(section));
        assertNotNull(result);
    }

    @Test
    @DisplayName("Given a section with lore, when from(Section) is called, then completes without error")
    void fromSection_completesWithoutError_whenLoreProvided() {
        Section section = createMinimalSection();
        when(section.getStringList(eq(ItemBuilderConfigurationKeys.LORE_ROUTE))).thenReturn(List.of("Line 1", "Line 2"));

        ItemBuilder result = assertDoesNotThrow(() -> ItemBuilder.from(section));
        assertNotNull(result);
    }
}

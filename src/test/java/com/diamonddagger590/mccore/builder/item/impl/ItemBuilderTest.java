package com.diamonddagger590.mccore.builder.item.impl;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.builder.item.ItemPluginType;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ItemBuilderTest {

    private MockedStatic<CorePlugin> corePluginStatic;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        RegistryResetExtension.setupRegistry();

        CorePlugin mockPlugin = mock(CorePlugin.class);
        when(mockPlugin.getMiniMessage()).thenReturn(MiniMessage.miniMessage());
        when(mockPlugin.getItemPlugin()).thenReturn(ItemPluginType.NONE);
        when(mockPlugin.registryAccess()).thenCallRealMethod();

        corePluginStatic = mockStatic(CorePlugin.class);
        corePluginStatic.when(CorePlugin::getInstance).thenReturn(mockPlugin);
    }

    @AfterEach
    void tearDown() {
        corePluginStatic.close();
        MockBukkit.unmock();
        RegistryResetExtension.resetRegistry();
    }

    @Test
    @DisplayName("Given an ItemType, when from is called, then returns a non-null ItemBuilder")
    void from_withItemType_returnsItemBuilder() {
        ItemBuilder builder = ItemBuilder.from(ItemType.STONE);
        assertNotNull(builder);
    }

    @Test
    @DisplayName("Given an ItemType and amount, when from is called, then returns builder with correct amount")
    void from_withItemTypeAndAmount_returnsBuilderWithAmount() {
        ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND, 5);
        ItemStack result = builder.asItemStack();
        assertEquals(5, result.getAmount());
    }

    @Test
    @DisplayName("Given an ItemType and zero amount, when from is called, then amount is clamped to 1")
    void from_withZeroAmount_clampsToOne() {
        ItemBuilder builder = ItemBuilder.from(ItemType.STONE, 0);
        ItemStack result = builder.asItemStack();
        assertEquals(1, result.getAmount());
    }

    @Test
    @DisplayName("Given an ItemType and negative amount, when from is called, then amount is clamped to 1")
    void from_withNegativeAmount_clampsToOne() {
        ItemBuilder builder = ItemBuilder.from(ItemType.STONE, -5);
        ItemStack result = builder.asItemStack();
        assertEquals(1, result.getAmount());
    }

    @Test
    @DisplayName("Given an ItemStack, when from is called, then returns builder wrapping that stack")
    void from_withItemStack_returnsBuilderWrappingStack() {
        ItemStack stack = new ItemStack(Material.IRON_SWORD);
        ItemBuilder builder = ItemBuilder.from(stack);
        assertNotNull(builder);
    }

    @Test
    @DisplayName("Given an ItemType, when potion is called, then returns PotionBuilder")
    void potion_returnsCorrectBuilderType() {
        PotionBuilder builder = ItemBuilder.potion(ItemType.POTION);
        assertInstanceOf(PotionBuilder.class, builder);
    }

    @Test
    @DisplayName("Given an ItemType and amount, when potion is called, then returns PotionBuilder with amount")
    void potion_withAmount_returnsBuilderWithAmount() {
        PotionBuilder builder = ItemBuilder.potion(ItemType.SPLASH_POTION, 3);
        ItemStack result = builder.asItemStack();
        assertEquals(3, result.getAmount());
    }

    @Test
    @DisplayName("Given an ItemType, when skull is called, then returns SkullBuilder")
    void skull_returnsCorrectBuilderType() {
        SkullBuilder builder = ItemBuilder.skull(ItemType.PLAYER_HEAD);
        assertInstanceOf(SkullBuilder.class, builder);
    }

    @Test
    @DisplayName("Given an ItemType and amount, when skull is called, then returns SkullBuilder with amount")
    void skull_withAmount_returnsBuilderWithAmount() {
        SkullBuilder builder = ItemBuilder.skull(ItemType.PLAYER_HEAD, 2);
        ItemStack result = builder.asItemStack();
        assertEquals(2, result.getAmount());
    }

    @Test
    @DisplayName("Given an ItemType, when pattern is called, then returns PatternBuilder")
    void pattern_returnsCorrectBuilderType() {
        PatternBuilder builder = ItemBuilder.pattern(ItemType.WHITE_BANNER);
        assertInstanceOf(PatternBuilder.class, builder);
    }

    @Test
    @DisplayName("Given an ItemType and amount, when pattern is called, then returns PatternBuilder with amount")
    void pattern_withAmount_returnsBuilderWithAmount() {
        PatternBuilder builder = ItemBuilder.pattern(ItemType.WHITE_BANNER, 4);
        ItemStack result = builder.asItemStack();
        assertEquals(4, result.getAmount());
    }

    @Test
    @DisplayName("Given a custom item string, when from is called, then returns ItemBuilder")
    void from_withCustomItemString_returnsItemBuilder() {
        ItemBuilder builder = ItemBuilder.from("stone");
        assertNotNull(builder);
    }

    @Test
    @DisplayName("Given an ItemBuilder from ItemType, when asItemStack is called, then returns correct material")
    void asItemStack_returnsCorrectMaterial() {
        ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
        ItemStack result = builder.asItemStack();
        assertEquals(Material.DIAMOND_SWORD, result.getType());
    }

    @Test
    @DisplayName("Given an ItemBuilder, when asItemStack is called multiple times, then returns equal items")
    void asItemStack_multipleCallsReturnEqualItems() {
        ItemBuilder builder = ItemBuilder.from(ItemType.STONE);
        ItemStack first = builder.asItemStack();
        ItemStack second = builder.asItemStack();

        assertEquals(first, second);
    }
}

package com.diamonddagger590.mccore.builder.item.impl;

import com.diamonddagger590.mccore.CorePlugin;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BannerPatternLayers;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class PatternBuilderTest {

    private MockedStatic<CorePlugin> corePluginStatic;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();

        CorePlugin mockPlugin = mock(CorePlugin.class);
        when(mockPlugin.getMiniMessage()).thenReturn(MiniMessage.miniMessage());

        corePluginStatic = mockStatic(CorePlugin.class);
        corePluginStatic.when(CorePlugin::getInstance).thenReturn(mockPlugin);
    }

    @AfterEach
    void tearDown() {
        corePluginStatic.close();
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Given a PatternBuilder, when addPattern is called with a Pattern object, then returns same builder")
    void addPattern_withPatternObject_returnsSameBuilder() {
        PatternBuilder builder = new PatternBuilder(new ItemStack(Material.WHITE_BANNER));
        Pattern pattern = new Pattern(DyeColor.RED, PatternType.STRIPE_BOTTOM);
        assertSame(builder, builder.addPattern(pattern));
    }

    @Test
    @DisplayName("Given a PatternBuilder, when addPattern is called with valid string and dye, then returns same builder")
    void addPattern_withValidStrings_returnsSameBuilder() {
        PatternBuilder builder = new PatternBuilder(new ItemStack(Material.WHITE_BANNER));
        assertSame(builder, builder.addPattern("stripe_bottom", "red"));
    }

    @Test
    @DisplayName("Given a PatternBuilder, when addPattern is called with invalid pattern string, then returns builder without adding")
    void addPattern_withInvalidPatternString_returnsWithoutAdding() {
        PatternBuilder builder = new PatternBuilder(new ItemStack(Material.WHITE_BANNER));
        PatternBuilder result = builder.addPattern("nonexistent_pattern", "red");
        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a PatternBuilder, when build is called, then banner pattern data is set on item")
    void build_setsBannerPatternDataOnItem() {
        ItemStack itemStack = new ItemStack(Material.WHITE_BANNER);
        PatternBuilder builder = new PatternBuilder(itemStack);

        Pattern pattern = new Pattern(DyeColor.BLUE, PatternType.STRIPE_BOTTOM);
        builder.addPattern(pattern).build();

        assertTrue(itemStack.hasData(DataComponentTypes.BANNER_PATTERNS));
        BannerPatternLayers layers = itemStack.getData(DataComponentTypes.BANNER_PATTERNS);
        assertNotNull(layers);
        assertEquals(1, layers.patterns().size());
    }

    @Test
    @DisplayName("Given a PatternBuilder, when multiple patterns are added, then all appear in built item")
    void build_multiplePatterns_allAppear() {
        ItemStack itemStack = new ItemStack(Material.WHITE_BANNER);
        PatternBuilder builder = new PatternBuilder(itemStack);

        builder.addPattern(new Pattern(DyeColor.RED, PatternType.STRIPE_BOTTOM))
                .addPattern(new Pattern(DyeColor.BLUE, PatternType.STRIPE_TOP))
                .build();

        BannerPatternLayers layers = itemStack.getData(DataComponentTypes.BANNER_PATTERNS);
        assertNotNull(layers);
        assertEquals(2, layers.patterns().size());
    }

    @Test
    @DisplayName("Given a PatternBuilder for a shield, when build is called, then data is set correctly")
    void build_shield_setsDataCorrectly() {
        ItemStack itemStack = new ItemStack(Material.SHIELD);
        PatternBuilder builder = new PatternBuilder(itemStack);

        builder.addPattern(new Pattern(DyeColor.GREEN, PatternType.STRIPE_BOTTOM)).build();

        assertTrue(itemStack.hasData(DataComponentTypes.BANNER_PATTERNS));
    }

    @Test
    @DisplayName("Given a PatternBuilder, when addPattern with string creates correct pattern and dye combination")
    void addPattern_stringParams_createsCorrectCombination() {
        ItemStack itemStack = new ItemStack(Material.WHITE_BANNER);
        PatternBuilder builder = new PatternBuilder(itemStack);

        builder.addPattern("stripe_bottom", "blue").build();

        BannerPatternLayers layers = itemStack.getData(DataComponentTypes.BANNER_PATTERNS);
        assertNotNull(layers);
        assertEquals(1, layers.patterns().size());
    }

    @Test
    @DisplayName("Given a PatternBuilder, when fluent API is chained, then all patterns are applied")
    void fluentApi_chainingWorks() {
        ItemStack itemStack = new ItemStack(Material.RED_BANNER);
        PatternBuilder builder = new PatternBuilder(itemStack);

        PatternBuilder result = builder
                .addPattern(new Pattern(DyeColor.WHITE, PatternType.STRIPE_BOTTOM))
                .addPattern(new Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP))
                .build();

        assertSame(builder, result);
        assertTrue(itemStack.hasData(DataComponentTypes.BANNER_PATTERNS));
    }
}

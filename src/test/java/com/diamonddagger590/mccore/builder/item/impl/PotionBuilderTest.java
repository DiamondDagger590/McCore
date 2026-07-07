package com.diamonddagger590.mccore.builder.item.impl;

import com.diamonddagger590.mccore.CorePlugin;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class PotionBuilderTest {

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
    @DisplayName("Given a PotionBuilder, when withPotionEffect is called with all parameters, then returns same builder")
    void withPotionEffect_allParams_returnsSameBuilder() {
        PotionBuilder builder = new PotionBuilder(new ItemStack(Material.POTION));
        PotionBuilder result = builder.withPotionEffect(PotionEffectType.SPEED, 200, 1, true, true, true);
        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a PotionBuilder, when withPotionEffect is called with 3 parameters, then defaults ambient/particles/icon to true")
    void withPotionEffect_threeParams_returnsSameBuilder() {
        PotionBuilder builder = new PotionBuilder(new ItemStack(Material.POTION));
        PotionBuilder result = builder.withPotionEffect(PotionEffectType.REGENERATION, 100, 2);
        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a PotionBuilder, when withPotionType is called, then returns same builder")
    void withPotionType_returnsSameBuilder() {
        PotionBuilder builder = new PotionBuilder(new ItemStack(Material.POTION));
        PotionBuilder result = builder.withPotionType(PotionType.HEALING);
        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a PotionBuilder, when withCustomName is called, then returns same builder")
    void withCustomName_returnsSameBuilder() {
        PotionBuilder builder = new PotionBuilder(new ItemStack(Material.POTION));
        PotionBuilder result = builder.withCustomName("test_potion");
        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a PotionBuilder, when build is called, then potion contents data is set on the item")
    void build_setPotionContentsOnItem() {
        ItemStack itemStack = new ItemStack(Material.POTION);
        PotionBuilder builder = new PotionBuilder(itemStack);

        builder.withPotionEffect(PotionEffectType.SPEED, 200, 1).build();

        assertTrue(itemStack.hasData(DataComponentTypes.POTION_CONTENTS));
        PotionContents contents = itemStack.getData(DataComponentTypes.POTION_CONTENTS);
        assertNotNull(contents);

        List<PotionEffect> effects = contents.customEffects();
        assertEquals(1, effects.size());
        assertEquals(PotionEffectType.SPEED, effects.get(0).getType());
        assertEquals(200, effects.get(0).getDuration());
        assertEquals(1, effects.get(0).getAmplifier());
    }

    @Test
    @DisplayName("Given a PotionBuilder, when multiple effects are added, then all appear in built item")
    void build_multipleEffects_allAppear() {
        ItemStack itemStack = new ItemStack(Material.POTION);
        PotionBuilder builder = new PotionBuilder(itemStack);

        builder.withPotionEffect(PotionEffectType.SPEED, 200, 1)
                .withPotionEffect(PotionEffectType.REGENERATION, 100, 2)
                .build();

        PotionContents contents = itemStack.getData(DataComponentTypes.POTION_CONTENTS);
        assertNotNull(contents);
        assertEquals(2, contents.customEffects().size());
    }

    @Test
    @DisplayName("Given a PotionBuilder, when withPotionType is set, then built item has base potion type")
    void build_withPotionType_setsBaseType() {
        ItemStack itemStack = new ItemStack(Material.POTION);
        PotionBuilder builder = new PotionBuilder(itemStack);

        builder.withPotionType(PotionType.HEALING).build();

        PotionContents contents = itemStack.getData(DataComponentTypes.POTION_CONTENTS);
        assertNotNull(contents);
        assertEquals(PotionType.HEALING, contents.potion());
    }

    @Test
    @DisplayName("Given a PotionBuilder for splash potion, when build is called, then data is set correctly")
    void build_splashPotion_setsDataCorrectly() {
        ItemStack itemStack = new ItemStack(Material.SPLASH_POTION);
        PotionBuilder builder = new PotionBuilder(itemStack);

        builder.withPotionEffect(PotionEffectType.POISON, 300, 0).build();

        assertTrue(itemStack.hasData(DataComponentTypes.POTION_CONTENTS));
        PotionContents contents = itemStack.getData(DataComponentTypes.POTION_CONTENTS);
        assertNotNull(contents);
        assertEquals(1, contents.customEffects().size());
    }

    @Test
    @DisplayName("Given a PotionBuilder for lingering potion, when build is called, then effect data is set correctly")
    void build_lingeringPotion_setsEffectDataCorrectly() {
        ItemStack itemStack = new ItemStack(Material.LINGERING_POTION);
        PotionBuilder builder = new PotionBuilder(itemStack);

        builder.withPotionEffect(PotionEffectType.SLOWNESS, 400, 1).build();

        assertTrue(itemStack.hasData(DataComponentTypes.POTION_CONTENTS));
        PotionContents contents = itemStack.getData(DataComponentTypes.POTION_CONTENTS);
        assertNotNull(contents);
        List<PotionEffect> effects = contents.customEffects();
        assertEquals(1, effects.size());
        assertEquals(PotionEffectType.SLOWNESS, effects.get(0).getType());
        assertEquals(400, effects.get(0).getDuration());
        assertEquals(1, effects.get(0).getAmplifier());
    }

    @Test
    @DisplayName("Given a PotionBuilder, when withPotionEffect full params sets ambient/particles/icon to false, then effect reflects those settings")
    void withPotionEffect_allFalse_effectReflectsSettings() {
        ItemStack itemStack = new ItemStack(Material.POTION);
        PotionBuilder builder = new PotionBuilder(itemStack);

        builder.withPotionEffect(PotionEffectType.STRENGTH, 100, 0, false, false, false).build();

        PotionContents contents = itemStack.getData(DataComponentTypes.POTION_CONTENTS);
        assertNotNull(contents);
        PotionEffect effect = contents.customEffects().get(0);
        assertEquals(false, effect.isAmbient());
        assertEquals(false, effect.hasParticles());
        assertEquals(false, effect.hasIcon());
    }

    @Test
    @DisplayName("Given a PotionBuilder, when setColor is called with a named color, then built item has custom color")
    void setColor_namedColor_setsCustomColor() {
        ItemStack itemStack = new ItemStack(Material.POTION);
        PotionBuilder builder = new PotionBuilder(itemStack);

        builder.setColor("RED").build();

        PotionContents contents = itemStack.getData(DataComponentTypes.POTION_CONTENTS);
        assertNotNull(contents);
        assertNotNull(contents.customColor());
    }

    @Test
    @DisplayName("Given a PotionBuilder, when setColor is called with unrecognized value, then built item uses white as fallback")
    void setColor_unrecognizedValue_fallsBackToWhite() {
        ItemStack itemStack = new ItemStack(Material.POTION);
        PotionBuilder builder = new PotionBuilder(itemStack);

        builder.setColor("255,0,0").build();

        PotionContents contents = itemStack.getData(DataComponentTypes.POTION_CONTENTS);
        assertNotNull(contents);
        Color color = contents.customColor();
        assertNotNull(color);
        assertEquals(Color.WHITE, color);
    }

    @Test
    @DisplayName("Given a PotionBuilder, when fluent API is chained, then all settings are applied")
    void fluentApi_chainingWorks() {
        ItemStack itemStack = new ItemStack(Material.POTION);
        PotionBuilder builder = new PotionBuilder(itemStack);

        PotionBuilder result = builder
                .withPotionType(PotionType.HEALING)
                .withPotionEffect(PotionEffectType.SPEED, 100, 1)
                .withCustomName("custom_healing")
                .build();

        assertSame(builder, result);
        assertTrue(itemStack.hasData(DataComponentTypes.POTION_CONTENTS));
    }
}

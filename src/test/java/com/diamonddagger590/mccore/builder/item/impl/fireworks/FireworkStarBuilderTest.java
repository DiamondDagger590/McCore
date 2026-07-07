package com.diamonddagger590.mccore.builder.item.impl.fireworks;

import com.diamonddagger590.mccore.CorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
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

class FireworkStarBuilderTest {

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
    @DisplayName("Given a new FireworkStarBuilder, when getBuilder called, then returns non-null FireworkEffect.Builder")
    void getBuilder_returnsNonNull() {
        FireworkStarBuilder builder = new FireworkStarBuilder(new ItemStack(Material.FIREWORK_STAR));
        assertNotNull(builder.getBuilder());
    }

    @Test
    @DisplayName("Given a FireworkStarBuilder, when flicker is set, then returns same builder for chaining")
    void flicker_returnsSameBuilder() {
        FireworkStarBuilder builder = new FireworkStarBuilder(new ItemStack(Material.FIREWORK_STAR));
        assertSame(builder, builder.flicker(true));
    }

    @Test
    @DisplayName("Given a FireworkStarBuilder, when trail is set, then returns same builder for chaining")
    void trail_returnsSameBuilder() {
        FireworkStarBuilder builder = new FireworkStarBuilder(new ItemStack(Material.FIREWORK_STAR));
        assertSame(builder, builder.trail(true));
    }

    @Test
    @DisplayName("Given a FireworkStarBuilder with flicker and trail, then built effect reflects both properties")
    void flickerAndTrail_reflectedInBuiltEffect() {
        FireworkStarBuilder builder = new FireworkStarBuilder(new ItemStack(Material.FIREWORK_STAR));
        builder.flicker(true).trail(true).withColor(Color.RED).with(FireworkEffect.Type.BALL);

        FireworkEffect effect = builder.getBuilder().build();
        assertEquals(true, effect.hasFlicker());
        assertEquals(true, effect.hasTrail());
    }

    @Test
    @DisplayName("Given a FireworkStarBuilder, when withColor is called with a single color, then effect contains it")
    void withColorSingle_addsColorToEffect() {
        FireworkStarBuilder builder = new FireworkStarBuilder(new ItemStack(Material.FIREWORK_STAR));
        builder.withColor(Color.BLUE).with(FireworkEffect.Type.BALL);

        FireworkEffect effect = builder.getBuilder().build();
        assertEquals(1, effect.getColors().size());
        assertEquals(Color.BLUE, effect.getColors().get(0));
    }

    @Test
    @DisplayName("Given a FireworkStarBuilder, when withColor is called with varargs, then effect contains all colors")
    void withColorVarargs_addsAllColors() {
        FireworkStarBuilder builder = new FireworkStarBuilder(new ItemStack(Material.FIREWORK_STAR));
        builder.withColor(Color.RED, Color.GREEN, Color.BLUE).with(FireworkEffect.Type.BALL);

        FireworkEffect effect = builder.getBuilder().build();
        assertEquals(3, effect.getColors().size());
    }

    @Test
    @DisplayName("Given a FireworkStarBuilder, when withColor is called with a list, then effect contains all colors")
    void withColorList_addsAllColors() {
        FireworkStarBuilder builder = new FireworkStarBuilder(new ItemStack(Material.FIREWORK_STAR));
        builder.withColor(List.of(Color.RED, Color.YELLOW)).with(FireworkEffect.Type.BALL);

        FireworkEffect effect = builder.getBuilder().build();
        assertEquals(2, effect.getColors().size());
    }

    @Test
    @DisplayName("Given a FireworkStarBuilder, when withFade is called with a single color, then effect has fade color")
    void withFadeSingle_addsFadeColor() {
        FireworkStarBuilder builder = new FireworkStarBuilder(new ItemStack(Material.FIREWORK_STAR));
        builder.withColor(Color.WHITE).withFade(Color.BLACK).with(FireworkEffect.Type.BALL);

        FireworkEffect effect = builder.getBuilder().build();
        assertEquals(1, effect.getFadeColors().size());
        assertEquals(Color.BLACK, effect.getFadeColors().get(0));
    }

    @Test
    @DisplayName("Given a FireworkStarBuilder, when withFade is called with varargs, then effect has all fade colors")
    void withFadeVarargs_addsAllFadeColors() {
        FireworkStarBuilder builder = new FireworkStarBuilder(new ItemStack(Material.FIREWORK_STAR));
        builder.withColor(Color.WHITE).withFade(Color.RED, Color.BLUE).with(FireworkEffect.Type.BALL);

        FireworkEffect effect = builder.getBuilder().build();
        assertEquals(2, effect.getFadeColors().size());
    }

    @Test
    @DisplayName("Given a FireworkStarBuilder, when withFade is called with a list, then effect has all fade colors")
    void withFadeList_addsAllFadeColors() {
        FireworkStarBuilder builder = new FireworkStarBuilder(new ItemStack(Material.FIREWORK_STAR));
        builder.withColor(Color.WHITE).withFade(List.of(Color.GREEN, Color.ORANGE)).with(FireworkEffect.Type.BALL);

        FireworkEffect effect = builder.getBuilder().build();
        assertEquals(2, effect.getFadeColors().size());
    }

    @Test
    @DisplayName("Given a FireworkStarBuilder, when type is set to STAR, then effect has STAR type")
    void withType_setsEffectType() {
        FireworkStarBuilder builder = new FireworkStarBuilder(new ItemStack(Material.FIREWORK_STAR));
        builder.withColor(Color.WHITE).with(FireworkEffect.Type.STAR);

        FireworkEffect effect = builder.getBuilder().build();
        assertEquals(FireworkEffect.Type.STAR, effect.getType());
    }

    @Test
    @DisplayName("Given a FireworkStarBuilder, when build is called, then firework explosion data is set on item")
    void build_setsFireworkExplosionDataOnItem() {
        ItemStack itemStack = new ItemStack(Material.FIREWORK_STAR);
        FireworkStarBuilder builder = new FireworkStarBuilder(itemStack);

        builder.withColor(Color.RED).with(FireworkEffect.Type.BALL).build();

        assertTrue(itemStack.hasData(DataComponentTypes.FIREWORK_EXPLOSION));
    }

    @Test
    @DisplayName("Given a FireworkStarBuilder with full configuration, then all properties are reflected in built effect")
    void fullConfiguration_allPropertiesReflected() {
        FireworkStarBuilder builder = new FireworkStarBuilder(new ItemStack(Material.FIREWORK_STAR));
        FireworkStarBuilder result = builder
                .flicker(true)
                .trail(false)
                .withColor(Color.RED)
                .withFade(Color.BLUE)
                .with(FireworkEffect.Type.CREEPER);

        assertSame(builder, result);

        FireworkEffect effect = builder.getBuilder().build();
        assertEquals(true, effect.hasFlicker());
        assertEquals(false, effect.hasTrail());
        assertEquals(FireworkEffect.Type.CREEPER, effect.getType());
        assertEquals(List.of(Color.RED), effect.getColors());
        assertEquals(List.of(Color.BLUE), effect.getFadeColors());
    }
}

package com.diamonddagger590.mccore.builder.item.impl.fireworks;

import com.diamonddagger590.mccore.CorePlugin;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Fireworks;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
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

class FireworkBuilderTest {

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
    @DisplayName("Given a FireworkBuilder, when addEffect is called with a FireworkEffect, then returns same builder")
    void addEffect_withFireworkEffect_returnsSameBuilder() {
        FireworkBuilder builder = new FireworkBuilder(new ItemStack(Material.FIREWORK_ROCKET));
        FireworkEffect effect = FireworkEffect.builder().withColor(Color.RED).with(FireworkEffect.Type.BALL).build();

        assertSame(builder, builder.addEffect(effect));
    }

    @Test
    @DisplayName("Given a FireworkBuilder, when addEffect is called with parameters, then returns same builder")
    void addEffect_withParameters_returnsSameBuilder() {
        FireworkBuilder builder = new FireworkBuilder(new ItemStack(Material.FIREWORK_ROCKET));
        FireworkBuilder result = builder.addEffect(true, false, FireworkEffect.Type.BALL_LARGE,
                List.of(Color.RED), List.of(Color.BLUE));

        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a FireworkBuilder, when addEffect with null colors, then effect is added without colors")
    void addEffect_withNullColors_addsEffectWithoutColors() {
        ItemStack itemStack = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkBuilder builder = new FireworkBuilder(itemStack);

        builder.addEffect(false, false, FireworkEffect.Type.BALL, null, null).build();

        Fireworks fireworks = itemStack.getData(DataComponentTypes.FIREWORKS);
        assertNotNull(fireworks);
        assertEquals(1, fireworks.effects().size());
        FireworkEffect effect = fireworks.effects().get(0);
        assertEquals(FireworkEffect.Type.BALL, effect.getType());
        assertTrue(effect.getColors().isEmpty());
        assertTrue(effect.getFadeColors().isEmpty());
    }

    @Test
    @DisplayName("Given a FireworkBuilder, when withDuration is called, then returns same builder")
    void withDuration_returnsSameBuilder() {
        FireworkBuilder builder = new FireworkBuilder(new ItemStack(Material.FIREWORK_ROCKET));
        assertSame(builder, builder.withDuration(3));
    }

    @Test
    @DisplayName("Given a FireworkBuilder, when build is called, then fireworks data is set on the item")
    void build_setsFireworksDataOnItem() {
        ItemStack itemStack = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkBuilder builder = new FireworkBuilder(itemStack);

        FireworkEffect effect = FireworkEffect.builder().withColor(Color.GREEN).with(FireworkEffect.Type.STAR).build();
        builder.addEffect(effect).withDuration(2).build();

        assertTrue(itemStack.hasData(DataComponentTypes.FIREWORKS));
        Fireworks fireworks = itemStack.getData(DataComponentTypes.FIREWORKS);
        assertNotNull(fireworks);
        assertEquals(2, fireworks.flightDuration());
        assertEquals(1, fireworks.effects().size());
    }

    @Test
    @DisplayName("Given a FireworkBuilder, when multiple effects are added, then all appear in built item")
    void build_multipleEffects_allAppear() {
        ItemStack itemStack = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkBuilder builder = new FireworkBuilder(itemStack);

        FireworkEffect effect1 = FireworkEffect.builder().withColor(Color.RED).with(FireworkEffect.Type.BALL).build();
        FireworkEffect effect2 = FireworkEffect.builder().withColor(Color.BLUE).with(FireworkEffect.Type.CREEPER).build();
        builder.addEffect(effect1).addEffect(effect2).build();

        Fireworks fireworks = itemStack.getData(DataComponentTypes.FIREWORKS);
        assertNotNull(fireworks);
        assertEquals(2, fireworks.effects().size());
    }

    @Test
    @DisplayName("Given a FireworkBuilder, when addEffect is called with parameters including colors and fade, then effect has correct properties")
    void addEffect_withColorsAndFade_buildsCorrectEffect() {
        ItemStack itemStack = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkBuilder builder = new FireworkBuilder(itemStack);

        builder.addEffect(true, true, FireworkEffect.Type.BURST,
                List.of(Color.RED, Color.GREEN), List.of(Color.YELLOW)).build();

        Fireworks fireworks = itemStack.getData(DataComponentTypes.FIREWORKS);
        assertNotNull(fireworks);
        assertEquals(1, fireworks.effects().size());

        FireworkEffect effect = fireworks.effects().get(0);
        assertTrue(effect.hasFlicker());
        assertTrue(effect.hasTrail());
        assertEquals(FireworkEffect.Type.BURST, effect.getType());
        assertEquals(2, effect.getColors().size());
        assertEquals(1, effect.getFadeColors().size());
    }

    @Test
    @DisplayName("Given a FireworkBuilder, when fluent API is chained, then all settings are applied")
    void fluentApi_chainingWorks() {
        ItemStack itemStack = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkBuilder builder = new FireworkBuilder(itemStack);

        FireworkEffect effect = FireworkEffect.builder().withColor(Color.WHITE).with(FireworkEffect.Type.BALL).build();
        FireworkBuilder result = builder
                .addEffect(effect)
                .withDuration(5)
                .build();

        assertSame(builder, result);

        Fireworks fireworks = itemStack.getData(DataComponentTypes.FIREWORKS);
        assertNotNull(fireworks);
        assertEquals(5, fireworks.flightDuration());
        assertEquals(1, fireworks.effects().size());
    }
}

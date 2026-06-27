package com.diamonddagger590.mccore.builder.item.impl;

import com.diamonddagger590.mccore.CorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class SpawnerBuilderTest {

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
    @DisplayName("Given a SpawnerBuilder, when withEntityType is called, then returns same builder")
    void withEntityType_returnsSameBuilder() {
        SpawnerBuilder builder = new SpawnerBuilder(new ItemStack(Material.SPAWNER));
        assertSame(builder, builder.withEntityType(EntityType.ZOMBIE));
    }

    @Test
    @DisplayName("Given a SpawnerBuilder, when withEntityType is called with null, then returns same builder")
    void withEntityType_null_returnsSameBuilder() {
        SpawnerBuilder builder = new SpawnerBuilder(new ItemStack(Material.SPAWNER));
        assertSame(builder, builder.withEntityType(null));
    }

    @Test
    @DisplayName("Given a SpawnerBuilder, when withSpawnCount is called, then returns same builder")
    void withSpawnCount_returnsSameBuilder() {
        SpawnerBuilder builder = new SpawnerBuilder(new ItemStack(Material.SPAWNER));
        assertSame(builder, builder.withSpawnCount(5));
    }

    @Test
    @DisplayName("Given a SpawnerBuilder, when withSpawnDelay is called, then returns same builder")
    void withSpawnDelay_returnsSameBuilder() {
        SpawnerBuilder builder = new SpawnerBuilder(new ItemStack(Material.SPAWNER));
        assertSame(builder, builder.withSpawnDelay(10));
    }

    @Test
    @DisplayName("Given a SpawnerBuilder, when withSpawnRange is called, then returns same builder")
    void withSpawnRange_returnsSameBuilder() {
        SpawnerBuilder builder = new SpawnerBuilder(new ItemStack(Material.SPAWNER));
        assertSame(builder, builder.withSpawnRange(16));
    }

    @Test
    @DisplayName("Given a SpawnerBuilder with null entityType, when build is called, then returns builder without modifying meta")
    void build_nullEntityType_returnsWithoutModifyingMeta() {
        SpawnerBuilder builder = new SpawnerBuilder(new ItemStack(Material.SPAWNER));
        SpawnerBuilder result = builder.build();
        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a SpawnerBuilder with entityType set, when build is called, then returns same builder")
    void build_withEntityType_returnsSameBuilder() {
        SpawnerBuilder builder = new SpawnerBuilder(new ItemStack(Material.SPAWNER));
        SpawnerBuilder result = builder.withEntityType(EntityType.SKELETON).build();
        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a SpawnerBuilder, when fluent API is chained, then all settings are applied")
    void fluentApi_chainingWorks() {
        SpawnerBuilder builder = new SpawnerBuilder(new ItemStack(Material.SPAWNER));
        SpawnerBuilder result = builder
                .withEntityType(EntityType.CREEPER)
                .withSpawnCount(4)
                .withSpawnDelay(20)
                .withSpawnRange(8)
                .build();

        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a SpawnerBuilder with zero count, when build is called, then count is not set on spawner")
    void build_zeroCount_countNotSet() {
        SpawnerBuilder builder = new SpawnerBuilder(new ItemStack(Material.SPAWNER));
        SpawnerBuilder result = builder
                .withEntityType(EntityType.ZOMBIE)
                .withSpawnCount(0)
                .build();

        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a SpawnerBuilder with zero delay, when build is called, then delay is not set on spawner")
    void build_zeroDelay_delayNotSet() {
        SpawnerBuilder builder = new SpawnerBuilder(new ItemStack(Material.SPAWNER));
        SpawnerBuilder result = builder
                .withEntityType(EntityType.ZOMBIE)
                .withSpawnDelay(0)
                .build();

        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a SpawnerBuilder with zero range, when build is called, then range is not set on spawner")
    void build_zeroRange_rangeNotSet() {
        SpawnerBuilder builder = new SpawnerBuilder(new ItemStack(Material.SPAWNER));
        SpawnerBuilder result = builder
                .withEntityType(EntityType.ZOMBIE)
                .withSpawnRange(0)
                .build();

        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a SpawnerBuilder with non-spawner item, when build is called with entityType, then meta is not modified as CreatureSpawner")
    void build_nonSpawnerItem_metaNotModified() {
        SpawnerBuilder builder = new SpawnerBuilder(new ItemStack(Material.STONE));
        SpawnerBuilder result = builder
                .withEntityType(EntityType.ZOMBIE)
                .build();

        assertSame(builder, result);
    }
}

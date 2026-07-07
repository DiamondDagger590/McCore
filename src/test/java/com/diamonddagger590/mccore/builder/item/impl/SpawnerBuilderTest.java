package com.diamonddagger590.mccore.builder.item.impl;

import com.diamonddagger590.mccore.CorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    @DisplayName("Given a SpawnerBuilder with null entityType, when build is called, then item meta is not modified")
    void build_nullEntityType_doesNotModifyMeta() {
        ItemStack itemStack = new ItemStack(Material.SPAWNER);
        ItemStack before = itemStack.clone();
        SpawnerBuilder builder = new SpawnerBuilder(itemStack);

        SpawnerBuilder result = builder.build();

        assertSame(builder, result);
        assertEquals(before, itemStack);
    }

    @Test
    @DisplayName("Given a SpawnerBuilder with entityType set, when build is called, then build completes without error")
    void build_withEntityType_completesWithoutError() {
        ItemStack itemStack = new ItemStack(Material.SPAWNER);
        SpawnerBuilder builder = new SpawnerBuilder(itemStack);

        SpawnerBuilder result = builder.withEntityType(EntityType.SKELETON)
                .withSpawnCount(4)
                .withSpawnDelay(10)
                .withSpawnRange(8)
                .build();

        assertSame(builder, result);
        assertNotNull(itemStack.getItemMeta());
    }

    @Test
    @DisplayName("Given a SpawnerBuilder, when fluent API is chained, then all setters return same builder")
    void fluentApi_chainingWorks() {
        ItemStack itemStack = new ItemStack(Material.SPAWNER);
        SpawnerBuilder builder = new SpawnerBuilder(itemStack);
        SpawnerBuilder result = builder
                .withEntityType(EntityType.CREEPER)
                .withSpawnCount(4)
                .withSpawnDelay(20)
                .withSpawnRange(8)
                .build();

        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a SpawnerBuilder with zero count, when build is called, then build completes without error")
    void build_zeroCount_completesWithoutError() {
        ItemStack itemStack = new ItemStack(Material.SPAWNER);
        SpawnerBuilder builder = new SpawnerBuilder(itemStack);

        SpawnerBuilder result = builder
                .withEntityType(EntityType.ZOMBIE)
                .withSpawnCount(0)
                .build();

        assertSame(builder, result);
        assertNotNull(itemStack.getItemMeta());
    }

    @Test
    @DisplayName("Given a SpawnerBuilder with zero delay, when build is called, then build completes without error")
    void build_zeroDelay_completesWithoutError() {
        ItemStack itemStack = new ItemStack(Material.SPAWNER);
        SpawnerBuilder builder = new SpawnerBuilder(itemStack);

        SpawnerBuilder result = builder
                .withEntityType(EntityType.ZOMBIE)
                .withSpawnDelay(0)
                .build();

        assertSame(builder, result);
        assertNotNull(itemStack.getItemMeta());
    }

    @Test
    @DisplayName("Given a SpawnerBuilder with zero range, when build is called, then build completes without error")
    void build_zeroRange_completesWithoutError() {
        ItemStack itemStack = new ItemStack(Material.SPAWNER);
        SpawnerBuilder builder = new SpawnerBuilder(itemStack);

        SpawnerBuilder result = builder
                .withEntityType(EntityType.ZOMBIE)
                .withSpawnRange(0)
                .build();

        assertSame(builder, result);
        assertNotNull(itemStack.getItemMeta());
    }

    @Test
    @DisplayName("Given a SpawnerBuilder with non-spawner item, when build is called, then item meta is not BlockStateMeta")
    void build_nonSpawnerItem_metaNotBlockStateMeta() {
        ItemStack itemStack = new ItemStack(Material.STONE);
        SpawnerBuilder builder = new SpawnerBuilder(itemStack);

        builder.withEntityType(EntityType.ZOMBIE).build();

        assertFalse(itemStack.getItemMeta() instanceof BlockStateMeta,
                "Non-spawner item should not have BlockStateMeta");
    }
}

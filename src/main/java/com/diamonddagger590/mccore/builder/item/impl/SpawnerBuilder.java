package com.diamonddagger590.mccore.builder.item.impl;

import com.diamonddagger590.mccore.builder.item.BaseItemBuilder;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An item builder that is used to build spawner items.
 */
public class SpawnerBuilder extends BaseItemBuilder<SpawnerBuilder> {

    private EntityType entityType;
    private int count = 0;
    private int delay = 3;
    private int range = 0;

    public SpawnerBuilder(@NotNull final ItemStack itemStack) {
        super(itemStack);
    }

    /**
     * Sets the {@link EntityType} of the spawner being built.
     *
     * @param entityType The {@link EntityType} to use.
     * @return This builder.
     */
    @NotNull
    public SpawnerBuilder withEntityType(@Nullable final EntityType entityType) {
        this.entityType = entityType;
        return this;
    }

    /**
     * Sets the spawn count of the spawner being built.
     *
     * @param count The spawn count to use.
     * @return This builder.
     */
    @NotNull
    public SpawnerBuilder withSpawnCount(final int count) {
        this.count = count;
        return this;
    }

    /**
     * Sets the spawn delay of the spawner being built.
     *
     * @param delay The delay to use.
     * @return This builder.
     */
    @NotNull
    public SpawnerBuilder withSpawnDelay(final int delay) {
        this.delay = delay;
        return this;
    }

    /**
     * Sets the spawn range of the spawner being built.
     *
     * @param range The range to use.
     * @return This builder.
     */
    @NotNull
    public SpawnerBuilder withSpawnRange(final int range) {
        this.range = range;
        return this;
    }

    @NotNull
    @Override
    public SpawnerBuilder build() {
        if (this.entityType == null) return this;
        getItemStack().editMeta(itemMeta -> {
            if (itemMeta instanceof CreatureSpawner spawner) {
                if (count > 0) {
                    spawner.setSpawnCount(count);
                }
                if (delay > 0) {
                    spawner.setDelay(delay);
                }
                if (range > 0) {
                    spawner.setSpawnRange(range);
                }
                spawner.setSpawnedType(this.entityType);
            }
        });
        return this;
    }
}
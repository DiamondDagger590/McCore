package com.diamonddagger590.mccore.external.common;

import com.diamonddagger590.mccore.util.item.CustomBlockWrapper;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A type of {@link com.diamonddagger590.mccore.registry.plugin.PluginHook} which supports custom block models.
 */
public interface CustomBlockHook {

    /**
     * Checks to see if the provided {@link Block} has a custom model on it.
     *
     * @param block The block to check.
     * @return {@code true} if the provided {@link Block} has a custom model on it.
     */
    boolean isCustomBlock(@NotNull Block block);

    /**
     * Checks to see if the provided block model is a valid custom model.
     *
     * @param customBlock The model name to check.
     * @return {@code true} if the provided block model is a valid custom model.
     */
    boolean isCustomBlock(@NotNull String customBlock);

    /**
     * Checks to see if the provided {@link CustomBlockWrapper} is an instance of a custom block.
     *
     * @param customItemWrapper The wrapper to check.
     * @return {@code true} if the provided {@link CustomBlockWrapper} is an instance of a custom block.
     */
    default boolean isCustomBlock(@NotNull CustomBlockWrapper customItemWrapper) {
        return customItemWrapper.customBlock().map(this::isCustomBlock).orElse(false);
    }

    /**
     * Checks to see if the provided {@link Block} has the specified model on it.
     *
     * @param block           The block to check.
     * @param customBlockType The custom model to check.
     * @return {@code true} if the provided {@link Block} has the specified model on it.
     */
    boolean isCustomBlockOfType(@NotNull Block block, @NotNull String customBlockType);

    /**
     * Places a custom block at the provided location with the specified model.
     *
     * @param location The location to place the block at.
     * @param blockId  The custom model id of the block to be placed.
     */
    void placeCustomBlock(@NotNull Location location, @NotNull String blockId);

    /**
     * Gets a list of drops that this block would drop if broken with the provided {@link ItemStack}.
     *
     * @param block           The block to get drops from.
     * @param itemToBreakWith The item to break the block with.
     * @param entityBreaking  The entity breaking the block.
     * @return A list of drops that this block would drop if broken with the provided {@link ItemStack}.
     */
    @NotNull
    List<ItemStack> drops(@NotNull Block block, @NotNull ItemStack itemToBreakWith, @Nullable Entity entityBreaking);

    /**
     * Plays visual and/or auditory effects for when a block is dropped.
     *
     * @param block The block for which to play the drop effects.
     */
    void playBlockDropEffects(@NotNull Block block);

    /**
     * Removes the specified block from the world.
     *
     * @param block The block to remove.
     */
    void removeBlock(@NotNull Block block);

    /**
     * Gets an {@link Optional} containing all the models that the provided {@link Block} has on it.
     *
     * @param block The block to get the models from.
     * @return An {@link Optional} containing all the models that the provided {@link Block} has on it. This
     * optional will be empty if there are no models on the block.
     */
    @NotNull
    Optional<Set<String>> blockModels(@NotNull Block block);
}

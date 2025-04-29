package com.diamonddagger590.mccore.external;

import com.diamonddagger590.mccore.util.item.CustomBlockWrapper;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

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
     * Gets an {@link Optional} containing all the models that the provided {@link Block} has on it.
     *
     * @param block The block to get the models from.
     * @return An {@link Optional} containing all the models that the provided {@link Block} has on it. This
     * optional will be empty if there are no models on the block.
     */
    @NotNull
    Optional<Set<String>> blockModels(@NotNull Block block);
}

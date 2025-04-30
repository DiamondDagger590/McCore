package com.diamonddagger590.mccore.util.item;

import com.diamonddagger590.mccore.external.CustomBlockHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.Methods;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A wrapper around {@link Block}s, allowing a {@link Material}
 * and a custom block name (used by model plugins) to be used interchangeably.
 * <p>
 * This is handy when a plugin wants to provide configuration options for server
 * owners where there doesn't need to be a distinct difference between vanilla and
 * custom blocks.
 * <p>
 * It's expected that a wrapper will either have a material or a custom block id but
 * never both or neither.
 */
public class CustomBlockWrapper {

    @Nullable
    private final Material material;
    @Nullable
    private final String customBlock;

    public CustomBlockWrapper(@NotNull Material material) {
        this.material = material;
        this.customBlock = null;
    }

    public CustomBlockWrapper(@NotNull String customBlock) {
        ItemType itemType = io.papermc.paper.registry.RegistryAccess.registryAccess().getRegistry(io.papermc.paper.registry.RegistryKey.ITEM).get(Methods.getMinecraftKey(customBlock));
        if (itemType != null) {
            this.material = itemType.asMaterial();
            this.customBlock = null;
        }
        else {
            this.material = null;
            this.customBlock = customBlock;
        }
    }

    /**
     * Gets an {@link Optional} containing the {@link Material} represented
     * by this wrapper.
     *
     * @return An {@link Optional} containing the {@link Material} represented
     * by this wrapper. If this optional is empty, it can be assumed that
     * {@link #customBlock()} will not return an empty optional.
     */
    @NotNull
    public Optional<Material> material() {
        return Optional.ofNullable(material);
    }

    /**
     * Gets an {@link Optional} containing the custom block represented
     * by this wrapper.
     *
     * @return An {@link Optional} containing the custom block represented
     * by this wrapper. If this optional is empty, it can be assumed that
     * {@link #material()} will not return an empty optional.
     */
    @NotNull
    public Optional<String> customBlock() {
        return Optional.ofNullable(customBlock);
    }

    /**
     * Checks to see if the provided {@link Material} equals this wrapper.
     *
     * @param material The material to check.
     * @return {@code true} of the provided {@link Material} equals this wrapper.
     */
    public boolean equals(@NotNull Material material) {
        return material == this.material;
    }

    /**
     * Checks to see if the provided {@link Block} equals this
     * wrapper.
     *
     * @param block The {@link Block} to check.
     * @return {@code true} if the provided {@link Block} equals
     * this wrapper.
     */
    public boolean equals(@NotNull Block block) {
        if (customBlock != null) {
            List<CustomBlockHook> customBlockHooks = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHooks(CustomBlockHook.class);
            for (CustomBlockHook customBlockHook : customBlockHooks) {
                if (customBlockHook.isCustomBlockOfType(block, customBlock)) {
                    return true;
                }
            }
            return false;        }
        else return material == block.getType();
    }

    /**
     * Checks to see if the provided custom block equals
     * this wrapper.
     *
     * @param customBlock The custom block to check.
     * @return {@code true} if the provided custom block equals
     * this wrapper.
     */
    public boolean equals(@NotNull String customBlock) {
        if (this.customBlock != null) {
            return this.customBlock.equalsIgnoreCase(customBlock);
        }
        return false;
    }

    /**
     * Checks to see if the provided wrapper equals
     * this wrapper.
     *
     * @param customMaterial The wrapper to check.
     * @return {@code true} if the provided wrapper equals
     * this wrapper.
     */
    public boolean equals(@NotNull CustomItemWrapper customMaterial) {
        if (this.customBlock != null && customMaterial.customItem().isPresent()) {
            return customBlock.equalsIgnoreCase(customMaterial.customItem().get());
        }
        else return this.material != null && customMaterial.material().isPresent() && customMaterial.material().get().equals(material);
    }

    @Override
    public int hashCode() {
        return material != null ? material.hashCode() : customBlock.hashCode();
    }

    /**
     * Gets an {@link Optional} containing all the block models that the provided {@link Block}
     * currently has on it.
     *
     * @param block The {@link Block} to get custom models from.
     * @return An {@link Optional} containing all the item models that the item models that the
     * provided {@link Block} currently has on it.
     */
    @NotNull
    public static Optional<Set<String>> customModels(@NotNull Block block) {
        List<CustomBlockHook> pluginHooks = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHooks(CustomBlockHook.class);
        Set<String> customModels = new HashSet<>();
        for (CustomBlockHook hook : pluginHooks) {
            hook.blockModels(block).ifPresent(customModels::addAll);
        }
        return Optional.of(customModels);
    }
}

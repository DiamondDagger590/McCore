package com.diamonddagger590.mccore.util.item;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.external.common.CustomBlockHook;
import com.diamonddagger590.mccore.external.common.CustomItemHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.Methods;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.SoundGroup;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
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
        BlockType blockType = lookupVanillaBlock(customBlock);
        if (blockType != null) {
            this.material = blockType.asMaterial();
            this.customBlock = null;
        } else {
            this.material = null;
            this.customBlock = customBlock;
        }
    }

    public CustomBlockWrapper(@NotNull Block block) {
        List<CustomBlockHook> pluginHooks = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHooks(CustomBlockHook.class);
        String customBlockResult = null;
        for (CustomBlockHook hook : pluginHooks) {
            if (hook.isCustomBlock(block)) {
                var blockModelsOptional = hook.blockModels(block);
                if (blockModelsOptional.isPresent() && !blockModelsOptional.get().isEmpty()) {
                    customBlockResult = blockModelsOptional.get().iterator().next();
                    break;
                }
            }
        }
        this.material = customBlockResult == null ? block.getType() : null;
        this.customBlock = customBlockResult;
    }

    /**
     * Checks whether this wrapper represents a vanilla block (resolved via the Paper registry).
     *
     * @return {@code true} if this wrapper holds a {@link Material}, meaning the block
     *         was recognized as a vanilla Minecraft block.
     */
    public boolean isVanilla() {
        return material != null;
    }

    /**
     * Checks whether this wrapper represents a custom block (from a model plugin).
     *
     * @return {@code true} if this wrapper holds a custom block identifier, meaning the block
     *         was not recognized as a vanilla Minecraft block.
     */
    public boolean isCustom() {
        return customBlock != null;
    }

    /**
     * Checks whether the given identifier resolves to a vanilla block in the Paper block registry.
     *
     * @param id The block identifier to check (e.g. {@code "stone"}, {@code "iron_ore"}).
     * @return {@code true} if the identifier is recognized as a vanilla block.
     */
    public static boolean isVanillaBlock(@NotNull String id) {
        return lookupVanillaBlock(id) != null;
    }

    /**
     * Looks up a block identifier in the Paper block registry.
     *
     * @param id The block identifier to look up (e.g. {@code "stone"}, {@code "iron_ore"}).
     * @return The {@link BlockType} if the identifier is a vanilla block, or {@code null} if not found.
     */
    @Nullable
    private static BlockType lookupVanillaBlock(@NotNull String id) {
        return io.papermc.paper.registry.RegistryAccess.registryAccess()
                .getRegistry(io.papermc.paper.registry.RegistryKey.BLOCK)
                .get(Methods.getMinecraftKey(id));
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
     * Returns a player-friendly display name for this block as a MiniMessage string.
     * <p>
     * For vanilla materials this returns a MiniMessage {@code <lang:key>} tag (e.g.
     * {@code <lang:block.minecraft.iron_ore>}), which the Minecraft client resolves to the
     * block's localized name in the player's own language.
     * <p>
     * For custom blocks the name is resolved via the registered {@link CustomBlockHook}, falling
     * back to {@code "Missing Block"} if no hook recognises the custom block identifier.
     *
     * @return A MiniMessage string representing the display name of this block.
     */
    @NotNull
    public String blockName() {
        if (customBlock != null) {
            List<CustomBlockHook> pluginHooks = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHooks(CustomBlockHook.class);
            for (CustomBlockHook hook : pluginHooks) {
                if (hook.isCustomBlock(customBlock)) {
                    return hook.blockName(this);
                }
            }
            return "Missing Block";
        } else {
            return "<lang:" + material.translationKey() + ">";
        }
    }

    /**
     * Get an {@link ItemBuilder} representation of this custom block, meant to allow for conversion between
     * custom blocks and custom items.
     *
     * @return A new {@link ItemBuilder} for this custom block.
     */
    @NotNull
    public ItemBuilder itemBuilder() {
        if (customBlock != null) {
            List<CustomItemHook> pluginHooks = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHooks(CustomItemHook.class);
            for (CustomItemHook hook : pluginHooks) {
                var itemOptional = hook.item(customBlock);
                if (itemOptional.isPresent()) {
                    return ItemBuilder.from(itemOptional.get());
                }
            }
            return ItemBuilder.from(new ItemStack(Material.AIR));
        }
        assert material != null;
        return ItemBuilder.from(new ItemStack(material));
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
            return false;
        } else return material == block.getType();
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
            return this.customBlock.equals(customBlock);
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
            return customBlock.equals(customMaterial.customItem().get());
        } else
            return this.material != null && customMaterial.material().isPresent() && customMaterial.material().get().equals(material);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomBlockWrapper other)) return false;

        if (this.material != null && other.material != null) {
            return this.material == other.material;
        }

        if (this.customBlock != null && other.customBlock != null) {
            return this.customBlock.equals(other.customBlock);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return material != null ? material.hashCode() : customBlock.hashCode();
    }

    @Override
    public String toString() {
        return "CustomBlockWrapper - [material=" + material + ", customBlock=" + customBlock + "]";
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

    /**
     * Gets a list of drops that this block would drop if broken with the provided {@link ItemStack}.
     *
     * @param block           The block to get drops from.
     * @param itemToBreakWith The item to break the block with.
     * @param entityBreaking  The entity breaking the block.
     * @return A list of drops that this block would drop if broken with the provided {@link ItemStack}.
     */
    @NotNull
    public static List<ItemStack> drops(@NotNull Block block, @NotNull ItemStack itemToBreakWith, @Nullable Entity entityBreaking) {
        List<CustomBlockHook> pluginHooks = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHooks(CustomBlockHook.class);
        for (CustomBlockHook hook : pluginHooks) {
            return hook.drops(block, itemToBreakWith, entityBreaking);
        }
        return List.copyOf(block.getDrops(itemToBreakWith, entityBreaking));
    }

    /**
     * Plays visual and/or auditory effects for when a block is dropped.
     *
     * @param block The block for which to play the drop effects.
     */
    public static void playBlockDropEffects(@NotNull Block block) {
        List<CustomBlockHook> pluginHooks = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHooks(CustomBlockHook.class);
        for (CustomBlockHook hook : pluginHooks) {
            hook.playBlockDropEffects(block);
            return;
        }
        World world = block.getWorld();
        BlockData data = block.getBlockData();
        SoundGroup soundGroup = block.getBlockSoundGroup();
        world.playSound(block.getLocation(), soundGroup.getBreakSound(), soundGroup.getVolume(), soundGroup.getPitch());
        world.spawnParticle(
                Particle.BLOCK,
                block.getLocation().clone().add(0.5, 0.5, 0.5),
                20,
                0.25, 0.25, 0.25,
                0.05,
                data);
    }

    /**
     * Removes the specified block from the world.
     *
     * @param block The block to remove.
     */
    public static void removeBlock(@NotNull Block block) {
        List<CustomBlockHook> pluginHooks = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHooks(CustomBlockHook.class);
        for (CustomBlockHook hook : pluginHooks) {
            hook.removeBlock(block);
            return;
        }
        block.setType(Material.AIR);
    }

}

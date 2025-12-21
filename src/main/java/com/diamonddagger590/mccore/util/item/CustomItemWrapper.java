package com.diamonddagger590.mccore.util.item;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.external.common.CustomItemHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.Methods;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A wrapper around {@link ItemStack}s, allowing a {@link Material}
 * and a custom item name (used by model plugins) to be used interchangeably.
 * <p>
 * This is handy when a plugin wants to provide configuration options for server
 * owners where there doesn't need to be a distinct difference between vanilla and
 * custom items.
 * <p>
 * It's expected that a wrapper will either have a material or a custom item id but
 * never both or neither.
 */
public class CustomItemWrapper {

    @Nullable
    private final Material material;
    @Nullable
    private final String customItem;

    public CustomItemWrapper(@NotNull Material material) {
        this.material = material;
        this.customItem = null;
    }

    public CustomItemWrapper(@NotNull String customItem) {
        ItemType itemType = io.papermc.paper.registry.RegistryAccess.registryAccess().getRegistry(io.papermc.paper.registry.RegistryKey.ITEM).get(Methods.getMinecraftKey(customItem));
        if (itemType != null) {
            this.material = itemType.asMaterial();
            this.customItem = null;
        }
        else {
            this.material = null;
            this.customItem = customItem;
        }
    }

    public CustomItemWrapper(@NotNull ItemStack itemStack) {
        List<CustomItemHook> pluginHooks = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHooks(CustomItemHook.class);
        String customItemResult = null;
        for (CustomItemHook hook : pluginHooks) {
            if (hook.isItem(itemStack)) {
                var itemModelsOptional = hook.itemModels(itemStack);
                if (itemModelsOptional.isPresent() && !itemModelsOptional.get().isEmpty()) {
                    customItemResult =  itemModelsOptional.get().iterator().next();
                    break;
                }
            }
        }
        this.material = customItemResult == null ? itemStack.getType() : null;
        this.customItem = customItemResult;
    }

    /**
     * Gets an {@link Optional} containing the {@link Material} represented
     * by this wrapper.
     *
     * @return An {@link Optional} containing the {@link Material} represented
     * by this wrapper. If this optional is empty, it can be assumed that
     * {@link #customItem()} will not return an empty optional.
     */
    @NotNull
    public Optional<Material> material() {
        return Optional.ofNullable(material);
    }

    /**
     * Gets an {@link Optional} containing the custom item id represented
     * by this wrapper.
     *
     * @return An {@link Optional} containing the custom item id represented
     * by this wrapper. If this optional is empty, it can be assumed that
     * {@link #material()} will not return an empty optional.
     */
    @NotNull
    public Optional<String> customItem() {
        return Optional.ofNullable(customItem);
    }

    @NotNull
    public ItemBuilder itemBuilder() {
        if (customItem != null) {
            List<CustomItemHook> pluginHooks = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHooks(CustomItemHook.class);
            for (CustomItemHook hook : pluginHooks) {
                var itemOptional = hook.item(customItem);
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
     * Checks to see if the provided {@link ItemStack} equals this
     * wrapper.
     *
     * @param itemStack The {@link ItemStack} to check.
     * @return {@code true} if the provided {@link ItemStack} equals
     * this wrapper.
     */
    public boolean equals(@NotNull ItemStack itemStack) {
        if (customItem != null) {
            return CorePlugin.getInstance().getItemPlugin().getCustomItem(customItem).isSimilar(itemStack);
        } else return material == itemStack.getType();
    }

    /**
     * Checks to see if the provided custom item id equals
     * this wrapper.
     *
     * @param customItem The custom item id to check.
     * @return {@code true} if the provided custom item id equals
     * this wrapper.
     */
    public boolean equals(@NotNull String customItem) {
        if (this.customItem != null) {
            return this.customItem.equals(customItem);
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
        if (this.customItem != null && customMaterial.customItem().isPresent()) {
            return customItem.equals(customMaterial.customItem().get());
        } else
            return this.material != null && customMaterial.material().isPresent() && customMaterial.material().get().equals(material);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomItemWrapper other)) return false;

        if (this.material != null && other.material != null) {
            return this.material == other.material;
        }

        if (this.customItem != null && other.customItem != null) {
            return this.customItem.equals(other.customItem);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return material != null ? material.hashCode() : customItem.hashCode();
    }

    /**
     * Gets an {@link Optional} containing all the item models that the provided {@link ItemStack}
     * currently has on it.
     *
     * @param itemStack The {@link ItemStack} to get custom models from.
     * @return An {@link Optional} containing all the item models that the item models that the
     * provided {@link ItemStack} currently has on it.
     */
    @NotNull
    public static Optional<Set<String>> customModels(@NotNull ItemStack itemStack) {
        List<CustomItemHook> pluginHooks = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHooks(CustomItemHook.class);
        Set<String> customModels = new HashSet<>();
        for (CustomItemHook hook : pluginHooks) {
            hook.itemModels(itemStack).ifPresent(customModels::addAll);
        }
        return Optional.of(customModels);
    }
}

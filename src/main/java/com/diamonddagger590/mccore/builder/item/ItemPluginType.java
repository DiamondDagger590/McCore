package com.diamonddagger590.mccore.builder.item;

import com.diamonddagger590.mccore.CorePlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.diamonddagger590.mccore.util.Methods.fromBase64;
import static com.diamonddagger590.mccore.util.Methods.getItemType;

/**
 * All different custom item plugins supported are listed and supported
 * here.
 */
public enum ItemPluginType {

    NEXO((customItem) -> {
        CorePlugin corePlugin = CorePlugin.getInstance();
        var nexoHookOptional = corePlugin.getNexoHook();
        if (nexoHookOptional.isPresent()) {
            var itemOptional = nexoHookOptional.get().getNexoItem(customItem);
            if (itemOptional.isPresent()) {
                return itemOptional.get();
            }
        }
        Optional<ItemType> itemType = getItemType(customItem);
        if (itemType.isPresent()) {
            return itemType.get().createItemStack(1);
        } else {
            try {
                return fromBase64(customItem);
            } catch (Exception exception) {
                return ItemType.STONE.createItemStack(1);
            }
        }
    }, "nexo"),
    ITEMS_ADDER(customItem -> {
        CorePlugin corePlugin = CorePlugin.getInstance();
        var itemsAdderOptional = corePlugin.getItemsAdderHook();
        if (itemsAdderOptional.isPresent()) {
            var itemOptional = itemsAdderOptional.get().getItemsAdderItem(customItem);
            if (itemOptional.isPresent()) {
                return itemOptional.get();
            }
        }
        Optional<ItemType> itemType = getItemType(customItem);
        if (itemType.isPresent()) {
            return itemType.get().createItemStack(1);
        } else {
            try {
                return fromBase64(customItem);
            } catch (Exception exception) {
                return ItemType.STONE.createItemStack(1);
            }
        }
    }, "itemsadder", "items_adder"),
    NONE(customItem -> {
        CorePlugin corePlugin = CorePlugin.getInstance();
        var nexoHookOptional = corePlugin.getNexoHook();
        var itemsAdderHookOptional = corePlugin.getItemsAdderHook();
        if (nexoHookOptional.isPresent() && nexoHookOptional.get().doesNexoItemExist(customItem)) {
            var itemOptional = nexoHookOptional.get().getNexoItem(customItem);
            if (itemOptional.isPresent()) {
                return itemOptional.get();
            }
        }
        if (itemsAdderHookOptional.isPresent() && itemsAdderHookOptional.get().doesItemsAdderExist(customItem)) {
            var itemOptional = itemsAdderHookOptional.get().getItemsAdderItem(customItem);
            if (itemOptional.isPresent()) {
                return itemOptional.get();
            }
        }

        Optional<ItemType> itemType = getItemType(customItem);
        if (itemType.isPresent()) {
            return itemType.get().createItemStack(1);
        } else {
            try {
                return fromBase64(customItem);
            } catch (Exception exception) {
                return ItemType.STONE.createItemStack(1);
            }
        }
    }, "none"),
    ;

    @NotNull
    private final CustomItemFunction customItemFunction;
    @NotNull
    private final Set<String> names;

    ItemPluginType(@NotNull CustomItemFunction customItemFunction, @NotNull final String... names) {
        this.customItemFunction = customItemFunction;
        this.names = new HashSet<>(Arrays.asList(names));
    }

    @NotNull
    public ItemStack getCustomItem(@NotNull final String customItem) {
        return customItemFunction.buildFromCustomItem(customItem);
    }

    /**
     * Gets the item plugin type from the provided string.
     *
     * @param name The plugin type to get.
     * @return The item plugin type matching the provided string or {@link #NONE} if there were
     * no matches.
     */
    @NotNull
    public static ItemPluginType fromName(@NotNull final String name) {
        return Arrays.stream(values())
                .sequential()
                .filter(itemPluginType -> itemPluginType.names.contains(name.toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElse(ItemPluginType.NONE);
    }

    /**
     * A function to turn a string into an {@link ItemStack} for custom item plugins.
     */
    private interface CustomItemFunction {

        /**
         * Parses the custom item string into an actual {@link ItemStack} based on
         * the custom item plugin's implementation.
         *
         * @param customItem The custom item string to parse.
         * @return An {@link ItemStack} built from the provided string.
         */
        @NotNull
        ItemStack buildFromCustomItem(@NotNull String customItem);
    }
}

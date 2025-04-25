package com.diamonddagger590.mccore.external.itemsadder;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * The hook needed to support
 * <a href="https://www.spigotmc.org/resources/%E2%9C%A8itemsadder%E2%AD%90emotes-mobs-items-armors-hud-gui-emojis-blocks-wings-hats-liquids.73355/">ItemsAdder</a>
 * for this plugin.
 */
public class CoreItemsAdderHook extends PluginHook<CorePlugin> {

    public CoreItemsAdderHook(@NotNull CorePlugin plugin) {
        super(plugin);
    }

    /**
     * Gets the ItemsAdder {@link ItemStack} representation from the provided item.
     *
     * @param item The item to get the ItemsAdder {@link ItemStack} from.
     * @return An {@link Optional} containing the ItemsAdder {@link ItemStack} representation
     * from the provided item.
     */
    @NotNull
    public Optional<ItemStack> getItemsAdderItem(@NotNull final String item) {
        CustomStack customStack = CustomStack.getInstance(item);
        if (customStack == null) {
            return Optional.empty();
        }
        return Optional.of(customStack.getItemStack());
    }

    /**
     * Checks to see if the provided item is a valid ItemsAdder item.
     *
     * @param item The item to check.
     * @return {@code true} if the provided item is a valid ItemsAdder item.
     */
    public boolean doesItemsAdderExist(@NotNull final String item) {
        return CustomStack.isInRegistry(item);
    }
}

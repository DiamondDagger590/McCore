package com.diamonddagger590.mccore.external.headdatabase;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.external.common.CustomItemHook;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

/**
 * The hook needed to support <a href="https://www.spigotmc.org/resources/head-database.14280/">HeadDatabase</a>
 * for this plugin.
 */
public class CoreHeadDatabaseHook extends PluginHook<CorePlugin> implements CustomItemHook {

    private final HeadDatabaseAPI headDatabaseAPI;

    public CoreHeadDatabaseHook(@NotNull final CorePlugin plugin) {
        super(plugin);
        this.headDatabaseAPI = new HeadDatabaseAPI();
    }

    /**
     * Checks to see if the provided item is a HeadDatabase head.
     *
     * @param item The item to check.
     * @return {@code true} if the provided item is a HeadDatabase head.
     */
    @Override
    public boolean isItem(@NotNull final String item) {
        return headDatabaseAPI.isHead(item);
    }

    @Override
    public boolean isItem(@NotNull ItemStack itemStack) {
        return headDatabaseAPI.getItemID(itemStack) != null;
    }

    @Override
    public boolean isItemOfType(@NotNull ItemStack itemStack, @NotNull String itemName) {
        return headDatabaseAPI.getItemID(itemStack).equalsIgnoreCase(itemName);
    }

    /**
     * Gets the HeadDatabase {@link ItemStack} representation from the provided item.
     *
     * @param item The item to get the HeadDatabase {@link ItemStack} from.
     * @return An {@link Optional} containing the HeadDatabase {@link ItemStack} representation
     * from the provided item.
     */
    @NotNull
    @Override
    public Optional<ItemStack> item(@NotNull final String item) {
        return Optional.ofNullable(headDatabaseAPI.getItemHead(item));
    }

    @NotNull
    @Override
    public Optional<Set<String>> itemModels(@NotNull ItemStack itemStack) {
        String itemID = headDatabaseAPI.getItemID(itemStack);
        return Optional.ofNullable(itemID == null ? null : Set.of(itemID));
    }
}

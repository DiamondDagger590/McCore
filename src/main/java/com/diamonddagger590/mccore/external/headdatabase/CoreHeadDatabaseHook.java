package com.diamonddagger590.mccore.external.headdatabase;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * The hook needed to support <a href="https://www.spigotmc.org/resources/head-database.14280/">HeadDatabase</a>
 * for this plugin.
 */
public class CoreHeadDatabaseHook extends PluginHook<CorePlugin> {

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
    public boolean isHead(@NotNull final String item) {
        return headDatabaseAPI.isHead(item);
    }

    /**
     * Gets the HeadDatabase {@link ItemStack} representation from the provided item.
     *
     * @param item The item to get the HeadDatabase {@link ItemStack} from.
     * @return An {@link Optional} containing the HeadDatabase {@link ItemStack} representation
     * from the provided item.
     */
    @NotNull
    public Optional<ItemStack> getHead(@NotNull final String item) {
        return Optional.ofNullable(headDatabaseAPI.getItemHead(item));
    }
}

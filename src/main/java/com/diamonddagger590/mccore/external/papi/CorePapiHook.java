package com.diamonddagger590.mccore.external.papi;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * A hook for containing all code related to <a href="https://www.spigotmc.org/resources/placeholderapi.6245/">PlaceholderAPI</a>
 * that this plugin needs in order to support it.
 */
public class CorePapiHook extends PluginHook<CorePlugin> {

    public CorePapiHook(@NotNull CorePlugin plugin) {
        super(plugin);
    }

    /**
     * Translates the provided message using the provided {@link OfflinePlayer} to use
     * for placeholders.
     *
     * @param player  The {@link OfflinePlayer} to use for placeholders.
     * @param message The message that needs placeholders replaced.
     * @return The message with placeholders replaced.
     */
    @NotNull
    public String translateMessage(@NotNull OfflinePlayer player, @NotNull String message) {
        return PlaceholderAPI.setPlaceholders(player, message);
    }
}

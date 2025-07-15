package com.diamonddagger590.mccore.external.papi;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import io.lumine.mythic.bukkit.utils.adventure.text.Component;
import io.lumine.mythic.bukkit.utils.adventure.text.serializer.legacy.LegacyComponentSerializer;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
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

    @NotNull
    public TagResolver getTagResolver(@NotNull Player player) {
        // Pulled from https://docs.advntr.dev/faq.html#how-can-i-use-bukkits-placeholderapi-in-minimessage-messages
        return TagResolver.resolver("papi", ((argumentQueue, context) -> {
            // Get the string placeholder that they want to use.
            final String papiPlaceholder = argumentQueue.popOr("papi tag requires an argument").value();

            // Then get PAPI to parse the placeholder for the given player.
            final String parsedPlaceholder = PlaceholderAPI.setPlaceholders(player, '%' + papiPlaceholder + '%');

            // We need to turn this ugly legacy string into a nice component.
            final Component componentPlaceholder = LegacyComponentSerializer.legacySection().deserialize(parsedPlaceholder);

            // Finally, return the tag instance to insert the placeholder!
            return Tag.selfClosingInserting((net.kyori.adventure.text.Component) componentPlaceholder);
        }));
    }
}

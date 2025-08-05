package com.diamonddagger590.mccore.external.cmi;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.external.common.AfkPluginHook;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Hooks into {@link CMI} to provide utility methods such as checking if a player is AFK.
 */
public class CoreCMIHook extends PluginHook<CorePlugin> implements AfkPluginHook {

    public CoreCMIHook(@NotNull CorePlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean isAfk(@NotNull CorePlayer corePlayer) {
        var playerOptional = corePlayer.getAsBukkitPlayer();
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
            return user != null && user.isAfk();
        }
        return false;
    }
}

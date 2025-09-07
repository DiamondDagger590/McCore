package com.diamonddagger590.mccore.external.citizens;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.external.common.NpcPluginHook;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Hooks into {@link net.citizensnpcs.Citizens} to provide utility methods such as checking if an
 * entity is an NPC.
 */
public class CoreCitizensHook extends PluginHook<CorePlugin> implements NpcPluginHook {

    public CoreCitizensHook(@NotNull CorePlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean isEntityNpc(@NotNull Entity entity) {
        return CitizensAPI.getNPCRegistry().isNPC(entity);
    }
}

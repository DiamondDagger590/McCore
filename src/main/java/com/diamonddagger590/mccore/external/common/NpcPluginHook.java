package com.diamonddagger590.mccore.external.common;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * This plugin hook allows for checking if a {@link Entity} is an NPC or not based
 * on the criteria of the plugin is being hooked into.
 */
public interface NpcPluginHook {

    /**
     * Checks to see if the provided {@link Entity} is an NPC or not.
     * @param entity The entity to check.
     * @return {@code true}
     */
    boolean isEntityNpc(@NotNull Entity entity);
}

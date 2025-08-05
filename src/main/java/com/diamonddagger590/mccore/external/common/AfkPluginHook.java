package com.diamonddagger590.mccore.external.common;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * This plugin hooks allows for checking if a player is afk or not based on the
 * criteria of the plugin is being hooked into.
 */
public interface AfkPluginHook {

    /**
     * Checks to see if the provided {@link CorePlayer} is currently afk.
     *
     * @param corePlayer The player to check.
     * @return {@code true} if the provided {@link CorePlayer} is currently afk.
     */
    boolean isAfk(@NotNull CorePlayer corePlayer);
}

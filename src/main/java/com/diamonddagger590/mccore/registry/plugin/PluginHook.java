package com.diamonddagger590.mccore.registry.plugin;

import com.diamonddagger590.mccore.CorePlugin;
import org.jetbrains.annotations.NotNull;

/**
 * A plugin hook allows for hooking into external plugins on either a hard or soft
 * dependency basis.
 */
public abstract class PluginHook {

    private final CorePlugin plugin;

    public PluginHook(@NotNull CorePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets the {@link CorePlugin} that created this hook.
     *
     * @return The {@link CorePlugin} that created this hook.
     */
    @NotNull
    public CorePlugin plugin() {
        return plugin;
    }
}

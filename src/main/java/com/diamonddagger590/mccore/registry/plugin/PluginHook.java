package com.diamonddagger590.mccore.registry.plugin;

import com.diamonddagger590.mccore.CorePlugin;
import org.jetbrains.annotations.NotNull;

/**
 * A plugin hook allows for hooking into external plugins on either a hard or soft
 * dependency basis.
 */
public abstract class PluginHook<CP extends CorePlugin> {

    private final CP plugin;

    public PluginHook(@NotNull CP plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets the {@link CP} that created this hook.
     *
     * @return The {@link CP} that created this hook.
     */
    @NotNull
    public CP plugin() {
        return plugin;
    }
}

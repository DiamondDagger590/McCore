package com.diamonddagger590.mccore.registry.plugin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.jetbrains.annotations.NotNull;

/**
 * A key that allows access to a {@link PluginHook} through the {@link PluginHookRegistry}.
 * <p>
 * To access the hook, users will need to call {@link RegistryAccess#registryAccess()} and provide
 * {@link com.diamonddagger590.mccore.registry.RegistryKey#PLUGIN_HOOK} to get back the {@link PluginHookRegistry}.
 * <p>
 * From there, users can call {@link PluginHookRegistry#pluginHook(PluginHookKey)} to get the plugin hook belonging to the
 * provided key.
 *
 * @param <P> The {@link PluginHook} being represented by this key.
 */
public interface PluginHookKey<P extends PluginHook> {

    /**
     * Gets the {@link Class} of the {@link PluginHook} represented by this key.
     *
     * @return The {@link Class} of the {@link PluginHook} represented by this key.
     */
    @NotNull
    Class<P> hookClass();
}

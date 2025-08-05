package com.diamonddagger590.mccore.registry.plugin;

import com.diamonddagger590.mccore.external.common.CustomBlockHook;
import com.diamonddagger590.mccore.registry.Registry;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is the main entry point to access {@link PluginHook}s for this plugin.
 * <p>
 * It is expected that if a plugin hook is not present in this registry, then
 * the plugin is not running on the server. As such, its functionality is not available.
 * <p>
 * To access this registry, call {@link com.diamonddagger590.mccore.registry.RegistryAccess#registry(RegistryKey)}
 * while providing {@link RegistryKey#PLUGIN_HOOK}.
 */
public class PluginHookRegistry implements Registry<PluginHook<?>> {

    private final Map<Class<?>, PluginHook<?>> hooks;

    public PluginHookRegistry() {
        this.hooks = new HashMap<>();
    }

    @Override
    public void register(@NotNull PluginHook<?> pluginHook) {
        if (hooks.containsKey(pluginHook.getClass())) {
            throw new IllegalArgumentException("Plugin hook already registered: " + pluginHook.getClass());
        }
        hooks.put(pluginHook.getClass(), pluginHook);
    }

    @Override
    public boolean registered(@NotNull PluginHook<?> pluginHook) {
        return hooks.containsKey(pluginHook.getClass());
    }

    /**
     * Gets an {@link Optional} containing the instance of the {@link PluginHook} corresponding
     * to the provided {@link PluginHookKey}.
     *
     * @param pluginHookKey The key to use to get a {@link PluginHook} for.
     * @param <T>           The implementation of the {@link PluginHook} to return.
     * @return An {@link Optional} containing the instance of the {@link PluginHook} corresponding
     * to the provided {@link PluginHookKey} if present. Otherwise, the optional will be empty, and it is
     * assumed the plugin hooked by the {@link PluginHook} is not running on the server.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public <T extends PluginHook<?>> Optional<T> pluginHook(@NotNull PluginHookKey<T> pluginHookKey) {
        return hooks.containsKey(pluginHookKey.hookClass()) ? Optional.of((T) hooks.get(pluginHookKey.hookClass())) : Optional.empty();
    }

    /**
     * Gets a list of all registered {@link PluginHook}s that are a child of the provided class.
     * <p>
     * This allows for easy fetching of common plugin hooks such as providing {@link CustomBlockHook}
     * to get all custom block hooks that are registered.
     *
     * @param extensible The class to get children plugin hooks of.
     * @param <T>        The class type to return the {@link PluginHook}s as.
     * @return A {@link List} of all registered {@link PluginHook}s that are a child of the provided
     * class.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public <T> List<T> pluginHooks(@NotNull Class<T> extensible) {
        return hooks.values().stream()
                .filter(pluginHook -> extensible.isAssignableFrom(pluginHook.getClass()))
                .map(pluginHook -> (T) pluginHook).toList();
    }
}

package com.diamonddagger590.mccore.registry.plugin;

import org.jetbrains.annotations.NotNull;

/**
 * The implementation of a {@link PluginHookKey}. To create
 * instances of a key, users should call {@link #create(Class)}.
 *
 * @param clazz The {@link Class} of the {@link PluginHook} being represented by this key.
 * @param <T>   The {@link PluginHook} class stored in this key.
 */
record PluginHookKeyImpl<T extends PluginHook>(Class<T> clazz) implements PluginHookKey<T> {

    /**
     * Creates a new instance of a key using the provided {@link Class}.
     *
     * @param clazz The class to store in the key.
     * @param <T>   The type of {@link PluginHook} to store in this key.
     * @return A {@link PluginHookKey} representing a {@link PluginHook}.
     */
    @NotNull
    public static <T extends PluginHook> PluginHookKey<T> create(@NotNull Class<T> clazz) {
        return new PluginHookKeyImpl<>(clazz);
    }

    @NotNull
    @Override
    public Class<T> hookClass() {
        return clazz;
    }
}

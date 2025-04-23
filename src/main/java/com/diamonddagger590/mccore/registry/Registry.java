package com.diamonddagger590.mccore.registry;

import org.jetbrains.annotations.NotNull;

/**
 * A Registry is a type of class with the following set of behavior expectations:
 * <ul>
 *     <li>Items will be registered during runtime</li>
 *     <li>Registered items will not be unregistered during runtime</li>
 *     <li>Additional functionality is limited beyond providing access to registered items</li>
 * </ul>
 * <p>
 * A similar but different concept is a {@link com.diamonddagger590.mccore.registry.manager.Manager} which has its
 * own documentation outlining the use cases it applies to.
 * <p>
 * This class doesn't provide a "get" stub because it is expected that each registry
 * may have special casing they want to do for their return types. An example is
 * the {@link com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry} returning registered
 * {@link com.diamonddagger590.mccore.registry.plugin.PluginHook}s wrapped in an {@link java.util.Optional} to support
 * the concept of "soft dependencies".
 *
 * @param <T> The type of object being stored in this registry.
 */
public interface Registry<T> {

    /**
     * Registers the provided {@link T} to this registry.
     *
     * @param t The item to register.
     */
    void register(@NotNull T t);

    /**
     * Checks to see if the provided {@link T} is registered in this registry.
     *
     * @param t The item to check.
     * @return {@code true} if the provided {@link T} is registered.
     */
    boolean registered(@NotNull T t);
}

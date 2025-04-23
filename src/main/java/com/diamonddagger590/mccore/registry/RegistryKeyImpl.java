package com.diamonddagger590.mccore.registry;

import org.jetbrains.annotations.NotNull;

/**
 * The implementation of a {@link RegistryKey}. To create
 * instances of a key, users should call {@link #create(Class)}.
 *
 * @param value The {@link Class} of the {@link Registry} being represented by this key.
 * @param <T>   The {@link Registry} class stored in this key.
 */
public record RegistryKeyImpl<T extends Registry<?>>(@NotNull Class<T> value) implements RegistryKey<T> {

    /**
     * Creates a new instance of a key using the provided {@link Class}.
     *
     * @param value The class to store in the key.
     * @param <T>   The type of {@link Registry} to store in this key.
     * @return A {@link RegistryKey} representing a {@link Registry}.
     */
    @NotNull
    public static <T extends Registry<?>> RegistryKey<T> create(@NotNull Class<T> value) {
        return new RegistryKeyImpl<>(value);
    }

    @NotNull
    @Override
    public Class<T> registryClass() {
        return value;
    }
}

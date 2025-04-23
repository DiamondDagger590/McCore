package com.diamonddagger590.mccore.registry.manager;

import org.jetbrains.annotations.NotNull;

/**
 * The implementation of a {@link ManagerKey}. To create
 * instances of a key, users should call {@link #create(Class)}.
 *
 * @param manager The {@link Class} of the {@link Manager} being represented by this key.
 * @param <M>     The {@link Manager} class stored in this key.
 */
public record ManagerKeyImpl<M extends Manager<?>>(@NotNull Class<M> manager) implements ManagerKey<M> {

    /**
     * Creates a new instance of a key using the provided {@link Class}.
     *
     * @param clazz The class to store in the key.
     * @param <M>   The type of {@link Manager} to store in this key.
     * @return A {@link ManagerKey} representing a {@link Manager}.
     */
    @NotNull
    public static <M extends Manager<?>> ManagerKey<M> create(@NotNull Class<M> clazz) {
        return new ManagerKeyImpl<>(clazz);
    }

    @NotNull
    @Override
    public Class<M> managerClass() {
        return manager;
    }
}

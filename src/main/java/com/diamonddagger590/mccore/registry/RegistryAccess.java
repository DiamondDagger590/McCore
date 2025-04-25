package com.diamonddagger590.mccore.registry;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the main entry point for access to {@link Registry Registries}.
 * <p>
 * To add a registry, users just need to pass in the instance of the registry
 * via {@link #register(Registry)}. From there, the registry can be retrieved by passing
 * in the {@link RegistryKey} corresponding to that registry via {@link #registry(RegistryKey)}.
 */
public final class RegistryAccess implements Registry<Registry<?>> {

    private static final RegistryAccess INSTANCE = new RegistryAccess();
    private final Map<Class<?>, Registry<?>> registryMap;

    private RegistryAccess() {
        this.registryMap = new HashMap<>();
    }

    /**
     * Registers the provided {@link Registry} to be accessible.
     *
     * @param registry The registry to register.
     */
    public void register(@NotNull Registry<?> registry) {
        if (registryMap.containsKey(registry.getClass())) {
            throw new IllegalArgumentException("Registry already registered: " + registry.getClass());
        }
        this.registryMap.put(registry.getClass(), registry);
    }

    /**
     * Checks to see if the provided {@link Registry} is accessible.
     *
     * @param registry The registry to check.
     * @return {@code true} if the provided {@link Registry} is accessible.
     */
    @Override
    public boolean registered(@NotNull Registry<?> registry) {
        return registryMap.containsKey(registry.getClass());
    }

    /**
     * Gets the {@link Registry} belonging to the provided {@link RegistryKey}.
     *
     * @param registryKey The key to get the corresponding {@link Registry}.
     * @param <T>         The implementation of {@link Registry} which is being returned.
     * @return The {@link Registry} belonging to the provided {@link RegistryKey}.
     * @throws IllegalStateException If the provided {@link RegistryKey} doesn't have
     *                               a corresponding {@link Registry} accessible.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public <T extends Registry<?>> T registry(@NotNull RegistryKey<T> registryKey) {
        return (T) registryMap.get(registryKey.getClass());
    }

    /**
     * Gets the instance of the registry access.
     *
     * @return The instance of the registry access.
     */
    @NotNull
    public static RegistryAccess registryAccess() {
        return INSTANCE;
    }
}

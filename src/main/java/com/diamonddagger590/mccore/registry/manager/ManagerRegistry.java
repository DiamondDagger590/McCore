package com.diamonddagger590.mccore.registry.manager;

import com.diamonddagger590.mccore.registry.Registry;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is the entry point for all {@link Manager}s for this plugin.
 * <p>
 * To access this registry, call {@link com.diamonddagger590.mccore.registry.RegistryAccess#registry(RegistryKey)}
 * while providing {@link RegistryKey#MANAGER}.
 */
public class ManagerRegistry implements Registry<Manager<?>> {

    private final Map<Class<?>, Manager<?>> managers;

    public ManagerRegistry() {
        managers = new HashMap<>();
    }

    @Override
    public void register(@NotNull Manager<?> manager) {
        managers.put(manager.getClass(), manager);
    }

    @Override
    public boolean registered(@NotNull Manager<?> manager) {
        return managers.containsKey(manager.getClass());
    }

    /**
     * Gets the {@link Manager} belonging to the provided {@link ManagerKey}.
     *
     * @param managerKey The key to get the corresponding {@link Manager}.
     * @param <T>        The implementation of {@link Manager} which is being returned.
     * @return The {@link Manager} belonging to the provided {@link ManagerKey}.
     * @throws IllegalStateException If the provided {@link ManagerKey} doesn't have
     *                               a corresponding {@link Manager} accessible.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public <T extends Manager<?>> T manager(@NotNull ManagerKey<T> managerKey) {
        /*
         This logic is being special-cased to support generic managers which we need
         access to in this core plugin but will have their implementation provided by
         consuming plugins.

         This can be seen with the GuiManager and PlayerManagers. To still allow
         access to these managers at compile time for this plugin, we need to be able to
         get children classes from the parent class which is not a behavior we really want
         elsewhere.
         */
        for (Map.Entry<Class<?>, Manager<?>> entry : managers.entrySet()) {
            if (entry.getKey().isAssignableFrom(managerKey.getClass())) {
                return (T) entry.getValue();
            }
        }
        return (T) managers.get(managerKey.managerClass());
    }
}

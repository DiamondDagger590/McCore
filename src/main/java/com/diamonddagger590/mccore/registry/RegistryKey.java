package com.diamonddagger590.mccore.registry;

import com.diamonddagger590.mccore.database.driver.DriverRegistry;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry;
import com.diamonddagger590.mccore.setting.PlayerSettingRegistry;
import org.jetbrains.annotations.NotNull;

import static com.diamonddagger590.mccore.registry.RegistryKeyImpl.create;

/**
 * A Registry Key allows access to a specific {@link Registry} via {@link RegistryAccess#registryAccess()}.
 *
 * @param <T> The {@link Registry} being represented by a key.
 */
public interface RegistryKey<T extends Registry<?>> {

    RegistryKey<ManagerRegistry> MANAGER = create(ManagerRegistry.class);
    RegistryKey<PluginHookRegistry> PLUGIN_HOOK = create(PluginHookRegistry.class);
    RegistryKey<PlayerSettingRegistry> PLAYER_SETTING = create(PlayerSettingRegistry.class);
    RegistryKey<DriverRegistry> DRIVER = create(DriverRegistry.class);

    /**
     * Gets the {@link Class} stored by this key.
     *
     * @return The {@link Class} stored by this key.
     */
    @NotNull
    Class<T> registryClass();
}

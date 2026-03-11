package com.diamonddagger590.mccore.testing;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry;
import com.diamonddagger590.mccore.setting.PlayerSettingRegistry;
import com.diamonddagger590.mccore.statistic.StatisticRegistry;

/**
 * Provides an extension to automatically reset the {@link com.diamonddagger590.mccore.registry.RegistryAccess}
 * before and after a class of unit tests run in order to not pollute the test suite.
 */
public class RegistryResetExtension {

    private static final String REGISTRY_CLASS_PATH = "com.diamonddagger590.mccore.registry.RegistryAccess";

    public static void setupRegistry() {
        InternalResetTestTools.resetRegistryAccess(REGISTRY_CLASS_PATH);
        try {
            RegistryAccess.registryAccess().register(new PluginHookRegistry());
            RegistryAccess.registryAccess().register(new PlayerSettingRegistry());
            RegistryAccess.registryAccess().register(new ManagerRegistry());
            RegistryAccess.registryAccess().register(new StatisticRegistry());
        }
        catch (Exception e) {
            /*
            Silent swallow
             */
        }
    }

    /**
     * Resets {@link RegistryAccess} and all of its internals.
     */
    public static void resetRegistry() {
        InternalResetTestTools.resetRegistryAccess(REGISTRY_CLASS_PATH);
    }
}

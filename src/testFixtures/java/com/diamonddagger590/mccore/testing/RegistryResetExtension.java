package com.diamonddagger590.mccore.testing;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry;
import com.diamonddagger590.mccore.setting.PlayerSettingRegistry;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Provides an extension to automatically reset the {@link com.diamonddagger590.mccore.registry.RegistryAccess}
 * before and after a class of unit tests run in order to not pollute the test suite.
 */
public class RegistryResetExtension implements BeforeAllCallback, AfterAllCallback {

    private static final String REGISTRY_CLASS_PATH = "com.diamonddagger590.mccore.registry.RegistryAccess";

    @Override
    public void beforeAll(@NotNull ExtensionContext context) {
        InternalResetTestTools.resetRegistryAccess(REGISTRY_CLASS_PATH);
        RegistryAccess.registryAccess().register(new ManagerRegistry());
        RegistryAccess.registryAccess().register(new PluginHookRegistry());
        RegistryAccess.registryAccess().register(new PlayerSettingRegistry());
    }

    @Override
    public void afterAll(@NotNull ExtensionContext context) {
        InternalResetTestTools.resetRegistryAccess(REGISTRY_CLASS_PATH);
    }
}

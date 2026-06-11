package com.diamonddagger590.mccore.registry;

import com.diamonddagger590.mccore.database.driver.DriverRegistry;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry;
import com.diamonddagger590.mccore.setting.PlayerSettingRegistry;
import com.diamonddagger590.mccore.statistic.StatisticRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RegistryKeyImplTest {

    @Test
    void createReturnsKeyWithCorrectClass() {
        RegistryKey<ManagerRegistry> key = RegistryKeyImpl.create(ManagerRegistry.class);
        assertEquals(ManagerRegistry.class, key.registryClass());
    }

    @Test
    void registryClassDelegatesFromRecordField() {
        var impl = new RegistryKeyImpl<>(PluginHookRegistry.class);
        assertEquals(PluginHookRegistry.class, impl.registryClass());
        assertEquals(PluginHookRegistry.class, impl.value());
    }

    @Test
    void recordEqualityForSameClass() {
        var a = new RegistryKeyImpl<>(ManagerRegistry.class);
        var b = new RegistryKeyImpl<>(ManagerRegistry.class);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void recordInequalityForDifferentClasses() {
        var managerKey = new RegistryKeyImpl<>(ManagerRegistry.class);
        var hookKey = new RegistryKeyImpl<>(PluginHookRegistry.class);
        assertNotEquals(managerKey, hookKey);
    }

    @Test
    void managerConstantReturnsManagerRegistryClass() {
        assertNotNull(RegistryKey.MANAGER);
        assertEquals(ManagerRegistry.class, RegistryKey.MANAGER.registryClass());
    }

    @Test
    void pluginHookConstantReturnsPluginHookRegistryClass() {
        assertNotNull(RegistryKey.PLUGIN_HOOK);
        assertEquals(PluginHookRegistry.class, RegistryKey.PLUGIN_HOOK.registryClass());
    }

    @Test
    void playerSettingConstantReturnsPlayerSettingRegistryClass() {
        assertNotNull(RegistryKey.PLAYER_SETTING);
        assertEquals(PlayerSettingRegistry.class, RegistryKey.PLAYER_SETTING.registryClass());
    }

    @Test
    void driverConstantReturnsDriverRegistryClass() {
        assertNotNull(RegistryKey.DRIVER);
        assertEquals(DriverRegistry.class, RegistryKey.DRIVER.registryClass());
    }

    @Test
    void statisticConstantReturnsStatisticRegistryClass() {
        assertNotNull(RegistryKey.STATISTIC);
        assertEquals(StatisticRegistry.class, RegistryKey.STATISTIC.registryClass());
    }

    @Test
    void allConstantsAreDistinct() {
        RegistryKey<?>[] keys = {
                RegistryKey.MANAGER,
                RegistryKey.PLUGIN_HOOK,
                RegistryKey.PLAYER_SETTING,
                RegistryKey.DRIVER,
                RegistryKey.STATISTIC,
        };
        for (int i = 0; i < keys.length; i++) {
            for (int j = i + 1; j < keys.length; j++) {
                assertNotEquals(keys[i], keys[j],
                        "Constants at index " + i + " and " + j + " should not be equal");
            }
        }
    }

    @Test
    void toStringContainsClassName() {
        var key = new RegistryKeyImpl<>(ManagerRegistry.class);
        String str = key.toString();
        assertNotNull(str);
        assertEquals(true, str.contains("ManagerRegistry"));
    }
}

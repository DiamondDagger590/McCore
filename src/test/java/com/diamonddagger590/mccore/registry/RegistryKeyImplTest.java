package com.diamonddagger590.mccore.registry;

import com.diamonddagger590.mccore.database.driver.DriverRegistry;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry;
import com.diamonddagger590.mccore.setting.PlayerSettingRegistry;
import com.diamonddagger590.mccore.statistic.StatisticRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistryKeyImplTest {

    @Test
    @DisplayName("Given a registry class, when creating a key via factory method, then key returns that class")
    void create_returnsKeyWithCorrectClass_whenGivenRegistryClass() {
        RegistryKey<ManagerRegistry> key = RegistryKeyImpl.create(ManagerRegistry.class);
        assertEquals(ManagerRegistry.class, key.registryClass());
    }

    @Test
    @DisplayName("Given a RegistryKeyImpl, when accessing registryClass, then it delegates to the record value field")
    void registryClass_delegatesToValue_whenCalled() {
        var impl = new RegistryKeyImpl<>(PluginHookRegistry.class);
        assertEquals(PluginHookRegistry.class, impl.registryClass());
        assertEquals(PluginHookRegistry.class, impl.value());
    }

    @Test
    @DisplayName("Given two RegistryKeyImpl instances with the same class, when compared, then they are equal")
    void equals_returnsTrue_whenSameClassUsed() {
        var a = new RegistryKeyImpl<>(ManagerRegistry.class);
        var b = new RegistryKeyImpl<>(ManagerRegistry.class);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("Given two RegistryKeyImpl instances with different classes, when compared, then they are not equal")
    void equals_returnsFalse_whenDifferentClassesUsed() {
        var managerKey = new RegistryKeyImpl<>(ManagerRegistry.class);
        var hookKey = new RegistryKeyImpl<>(PluginHookRegistry.class);
        assertNotEquals(managerKey, hookKey);
    }

    @Test
    @DisplayName("Given MANAGER constant, when accessing registryClass, then it returns ManagerRegistry")
    void managerConstant_returnsManagerRegistryClass_whenAccessed() {
        assertNotNull(RegistryKey.MANAGER);
        assertEquals(ManagerRegistry.class, RegistryKey.MANAGER.registryClass());
    }

    @Test
    @DisplayName("Given PLUGIN_HOOK constant, when accessing registryClass, then it returns PluginHookRegistry")
    void pluginHookConstant_returnsPluginHookRegistryClass_whenAccessed() {
        assertNotNull(RegistryKey.PLUGIN_HOOK);
        assertEquals(PluginHookRegistry.class, RegistryKey.PLUGIN_HOOK.registryClass());
    }

    @Test
    @DisplayName("Given PLAYER_SETTING constant, when accessing registryClass, then it returns PlayerSettingRegistry")
    void playerSettingConstant_returnsPlayerSettingRegistryClass_whenAccessed() {
        assertNotNull(RegistryKey.PLAYER_SETTING);
        assertEquals(PlayerSettingRegistry.class, RegistryKey.PLAYER_SETTING.registryClass());
    }

    @Test
    @DisplayName("Given DRIVER constant, when accessing registryClass, then it returns DriverRegistry")
    void driverConstant_returnsDriverRegistryClass_whenAccessed() {
        assertNotNull(RegistryKey.DRIVER);
        assertEquals(DriverRegistry.class, RegistryKey.DRIVER.registryClass());
    }

    @Test
    @DisplayName("Given STATISTIC constant, when accessing registryClass, then it returns StatisticRegistry")
    void statisticConstant_returnsStatisticRegistryClass_whenAccessed() {
        assertNotNull(RegistryKey.STATISTIC);
        assertEquals(StatisticRegistry.class, RegistryKey.STATISTIC.registryClass());
    }

    @Test
    @DisplayName("Given all RegistryKey constants, when compared pairwise, then all are distinct")
    void allConstants_areDistinct_whenComparedPairwise() {
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
    @DisplayName("Given a RegistryKeyImpl, when calling toString, then it contains the registry class name")
    void toString_containsClassName_whenCalled() {
        var key = new RegistryKeyImpl<>(ManagerRegistry.class);
        String str = key.toString();
        assertNotNull(str);
        assertTrue(str.contains("ManagerRegistry"));
    }
}

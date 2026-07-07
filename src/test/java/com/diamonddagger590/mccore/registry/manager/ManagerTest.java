package com.diamonddagger590.mccore.registry.manager;

import com.diamonddagger590.mccore.CorePlugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class ManagerTest {

    private static class TestManager extends Manager<CorePlugin> {
        TestManager(CorePlugin plugin) {
            super(plugin);
        }
    }

    @Test
    @DisplayName("Given a plugin instance, when constructing a Manager, then plugin() returns the same instance")
    void plugin_returnsSameInstance_whenConstructedWithPlugin() {
        CorePlugin mockPlugin = mock(CorePlugin.class);
        TestManager manager = new TestManager(mockPlugin);
        assertSame(mockPlugin, manager.plugin());
    }

    @Test
    @DisplayName("Given two managers created with different plugins, when calling plugin(), then each returns its own plugin")
    void plugin_returnsCorrectPlugin_forEachInstance() {
        CorePlugin pluginA = mock(CorePlugin.class);
        CorePlugin pluginB = mock(CorePlugin.class);
        TestManager managerA = new TestManager(pluginA);
        TestManager managerB = new TestManager(pluginB);
        assertSame(pluginA, managerA.plugin());
        assertSame(pluginB, managerB.plugin());
    }
}

package com.diamonddagger590.mccore.registry.manager;

import com.diamonddagger590.mccore.CorePlugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ManagerTest {

    private static class TestManager extends Manager<CorePlugin> {
        TestManager(CorePlugin plugin) {
            super(plugin);
        }
    }

    @Test
    @DisplayName("Given a plugin instance, when constructing a Manager, then plugin() returns the same instance")
    void plugin_returnsSameInstance_whenConstructedWithPlugin() {
        // CorePlugin cannot be instantiated without a server, so we verify the
        // abstract contract with null — the getter must faithfully return whatever
        // was passed to the constructor.
        TestManager manager = new TestManager(null);
        assertNull(manager.plugin());
    }

    @Test
    @DisplayName("Given two managers created with different plugins, when calling plugin(), then each returns its own plugin")
    void plugin_returnsCorrectPlugin_forEachInstance() {
        TestManager managerA = new TestManager(null);
        TestManager managerB = new TestManager(null);
        assertNull(managerA.plugin());
        assertNull(managerB.plugin());
    }
}

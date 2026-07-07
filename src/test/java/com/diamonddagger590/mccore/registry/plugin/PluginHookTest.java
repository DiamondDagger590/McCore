package com.diamonddagger590.mccore.registry.plugin;

import com.diamonddagger590.mccore.CorePlugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class PluginHookTest {

    private static class TestPluginHook extends PluginHook<CorePlugin> {
        TestPluginHook(CorePlugin plugin) {
            super(plugin);
        }
    }

    @Test
    @DisplayName("Given a plugin instance, when constructing a PluginHook, then plugin() returns the same instance")
    void plugin_returnsSameInstance_whenConstructedWithPlugin() {
        TestPluginHook hook = new TestPluginHook(null);
        assertNull(hook.plugin());
    }

    @Test
    @DisplayName("Given two hooks created with different plugins, when calling plugin(), then each returns its own plugin")
    void plugin_returnsCorrectPlugin_forEachInstance() {
        TestPluginHook hookA = new TestPluginHook(null);
        TestPluginHook hookB = new TestPluginHook(null);
        assertNull(hookA.plugin());
        assertNull(hookB.plugin());
    }
}

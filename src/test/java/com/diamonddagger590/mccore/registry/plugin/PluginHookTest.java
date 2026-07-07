package com.diamonddagger590.mccore.registry.plugin;

import com.diamonddagger590.mccore.CorePlugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class PluginHookTest {

    private static class TestPluginHook extends PluginHook<CorePlugin> {
        TestPluginHook(CorePlugin plugin) {
            super(plugin);
        }
    }

    @Test
    @DisplayName("Given a plugin instance, when constructing a PluginHook, then plugin() returns the same instance")
    void plugin_returnsSameInstance_whenConstructedWithPlugin() {
        CorePlugin mockPlugin = mock(CorePlugin.class);
        TestPluginHook hook = new TestPluginHook(mockPlugin);
        assertSame(mockPlugin, hook.plugin());
    }

    @Test
    @DisplayName("Given two hooks created with different plugins, when calling plugin(), then each returns its own plugin")
    void plugin_returnsCorrectPlugin_forEachInstance() {
        CorePlugin pluginA = mock(CorePlugin.class);
        CorePlugin pluginB = mock(CorePlugin.class);
        TestPluginHook hookA = new TestPluginHook(pluginA);
        TestPluginHook hookB = new TestPluginHook(pluginB);
        assertSame(pluginA, hookA.plugin());
        assertSame(pluginB, hookB.plugin());
    }
}

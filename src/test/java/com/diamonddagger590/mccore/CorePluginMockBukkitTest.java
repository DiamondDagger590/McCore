package com.diamonddagger590.mccore;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CorePluginMockBukkitTest {

    private TestCorePlugin plugin;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(TestCorePlugin.class);
        RegistryResetExtension.setupRegistry();
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Given MockBukkit loaded plugin, when getInstance is called, then returns the loaded plugin")
    void getInstance_returnsLoadedPlugin_whenPluginIsEnabled() {
        assertSame(plugin, CorePlugin.getInstance());
    }

    @Test
    @DisplayName("Given MockBukkit loaded plugin, when getMiniMessage is called, then returns non-null MiniMessage")
    void getMiniMessage_returnsNonNull_whenPluginIsEnabled() {
        MiniMessage miniMessage = plugin.getMiniMessage();

        assertNotNull(miniMessage);
    }

    @Test
    @DisplayName("Given MockBukkit loaded plugin, when getMiniMessage is called twice, then returns same instance")
    void getMiniMessage_returnsSameInstance_onSubsequentCalls() {
        MiniMessage first = plugin.getMiniMessage();
        MiniMessage second = plugin.getMiniMessage();

        assertSame(first, second);
    }

    @Test
    @DisplayName("Given MockBukkit loaded plugin, when registryAccess is called, then returns RegistryAccess singleton")
    void registryAccess_returnsSingleton_whenPluginIsEnabled() {
        RegistryAccess registryAccess = plugin.registryAccess();

        assertNotNull(registryAccess);
        assertSame(RegistryAccess.registryAccess(), registryAccess);
    }

    @Test
    @DisplayName("Given MockBukkit loaded plugin, when getTimeProvider is called, then returns non-null TimeProvider")
    void getTimeProvider_returnsNonNull_whenPluginIsEnabled() {
        assertNotNull(plugin.getTimeProvider());
    }
}

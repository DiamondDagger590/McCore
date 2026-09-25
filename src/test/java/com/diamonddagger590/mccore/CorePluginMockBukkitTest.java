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
import org.mockbukkit.mockbukkit.ServerMock;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CorePluginMockBukkitTest {

    private ServerMock server;
    private TestCorePlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestCorePlugin.class);
        RegistryResetExtension.setupRegistry();
    }

    @AfterEach
    void tearDown() throws Exception {
        RegistryResetExtension.resetRegistry();
        MockBukkit.unmock();
        Field instanceField = CorePlugin.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @Test
    @DisplayName("Given a loaded plugin, when onEnable has run, then getInstance returns the plugin")
    void getInstance_returnsPlugin_afterOnEnable() {
        assertSame(plugin, CorePlugin.getInstance());
    }

    @Test
    @DisplayName("Given a loaded plugin, when getMiniMessage is called, then returns non-null MiniMessage")
    void getMiniMessage_returnsNonNull_afterOnEnable() {
        MiniMessage miniMessage = plugin.getMiniMessage();
        assertNotNull(miniMessage);
    }

    @Test
    @DisplayName("Given a loaded plugin, when getMiniMessage is called, then returns the standard MiniMessage instance")
    void getMiniMessage_returnsStandardInstance_afterOnEnable() {
        MiniMessage miniMessage = plugin.getMiniMessage();
        assertSame(MiniMessage.miniMessage(), miniMessage);
    }

    @Test
    @DisplayName("Given a loaded plugin, when registryAccess is called, then returns the RegistryAccess singleton")
    void registryAccess_returnsSingleton_afterOnEnable() {
        RegistryAccess fromPlugin = plugin.registryAccess();
        assertNotNull(fromPlugin);
        assertSame(RegistryAccess.registryAccess(), fromPlugin);
    }

    @Test
    @DisplayName("Given a loaded plugin, when onDisable is called, then no exception is thrown")
    void onDisable_doesNotThrow_whenCalled() {
        assertDoesNotThrow(() -> plugin.onDisable());
    }

    @Test
    @DisplayName("Given a loaded plugin, when getTimeProvider is called, then returns non-null TimeProvider")
    void getTimeProvider_returnsNonNull_afterOnEnable() {
        assertNotNull(plugin.getTimeProvider());
    }
}

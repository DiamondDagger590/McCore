package com.diamonddagger590.mccore.gui;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.event.gui.CoreGuiOpenEvent;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GuiManagerKeyedGuiTest {

    private GuiManager<CorePlayer, CorePlugin> guiManager;
    private CorePlugin mockPlugin;
    private Server mockServer;
    private PluginManager mockPluginManager;
    private CoreGuiOpenEvent capturedEvent;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        mockPlugin = mock(CorePlugin.class);
        mockServer = mock(Server.class);
        mockPluginManager = mock(PluginManager.class);
        when(mockPlugin.getServer()).thenReturn(mockServer);
        when(mockServer.getPluginManager()).thenReturn(mockPluginManager);
        when(mockServer.getLogger()).thenReturn(Logger.getLogger("TestServer"));
        when(mockServer.isPrimaryThread()).thenReturn(true);
        doNothing().when(mockPluginManager).callEvent(any());

        Field serverField = Bukkit.class.getDeclaredField("server");
        serverField.setAccessible(true);
        serverField.set(null, mockServer);

        guiManager = new GuiManager<>(mockPlugin);
    }

    @AfterEach
    void tearDown() throws Exception {
        Field serverField = Bukkit.class.getDeclaredField("server");
        serverField.setAccessible(true);
        serverField.set(null, null);
    }

    @SuppressWarnings("deprecation")
    private static NamespacedKey key(String namespace, String key) {
        return new NamespacedKey(namespace, key);
    }

    @Test
    @DisplayName("Given a KeyedGui with a key, when trackPlayerGui, then event contains the key")
    void trackPlayerGui_passesKey_whenGuiIsKeyed() {
        UUID playerUUID = UUID.randomUUID();
        NamespacedKey guiKey = key("test", "my_gui");

        @SuppressWarnings("unchecked")
        TestKeyedGui mockGui = mock(TestKeyedGui.class);
        when(mockGui.getUUID()).thenReturn(UUID.randomUUID());
        when(mockGui.getGuiKey()).thenReturn(Optional.of(guiKey));

        guiManager.trackPlayerGui(playerUUID, mockGui);

        verify(mockPluginManager).callEvent(any(CoreGuiOpenEvent.class));
        verify(mockGui).getGuiKey();
    }

    @Test
    @DisplayName("Given a KeyedGui with empty key, when trackPlayerGui, then event has null key")
    void trackPlayerGui_passesNull_whenKeyedGuiHasEmptyKey() {
        UUID playerUUID = UUID.randomUUID();

        @SuppressWarnings("unchecked")
        TestKeyedGui mockGui = mock(TestKeyedGui.class);
        when(mockGui.getUUID()).thenReturn(UUID.randomUUID());
        when(mockGui.getGuiKey()).thenReturn(Optional.empty());

        guiManager.trackPlayerGui(playerUUID, mockGui);

        verify(mockPluginManager).callEvent(any(CoreGuiOpenEvent.class));
        verify(mockGui).getGuiKey();
    }

    @Test
    @DisplayName("Given a non-KeyedGui, when trackPlayerGui, then event has null key")
    void trackPlayerGui_passesNull_whenGuiIsNotKeyed() {
        UUID playerUUID = UUID.randomUUID();

        @SuppressWarnings("unchecked")
        Gui<CorePlayer> mockGui = mock(Gui.class);
        when(mockGui.getUUID()).thenReturn(UUID.randomUUID());

        guiManager.trackPlayerGui(playerUUID, mockGui);

        verify(mockPluginManager).callEvent(any(CoreGuiOpenEvent.class));
    }

    @Test
    @DisplayName("Given a KeyedGui, when trackPlayerGui, then gui is properly tracked")
    void trackPlayerGui_tracksGui_whenKeyedGuiProvided() {
        UUID playerUUID = UUID.randomUUID();
        NamespacedKey guiKey = key("test", "tracked_gui");

        @SuppressWarnings("unchecked")
        TestKeyedGui mockGui = mock(TestKeyedGui.class);
        when(mockGui.getUUID()).thenReturn(UUID.randomUUID());
        when(mockGui.getGuiKey()).thenReturn(Optional.of(guiKey));

        guiManager.trackPlayerGui(playerUUID, mockGui);

        assertTrue(guiManager.doesPlayerHaveGui(playerUUID));
        Optional<Gui<CorePlayer>> result = guiManager.getOpenedGui(playerUUID);
        assertTrue(result.isPresent());
        assertEquals(mockGui, result.get());
    }

    private interface TestKeyedGui extends Gui<CorePlayer>, KeyedGui {}
}

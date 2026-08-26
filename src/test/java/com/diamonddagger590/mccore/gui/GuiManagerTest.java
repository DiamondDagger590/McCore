package com.diamonddagger590.mccore.gui;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.event.gui.CoreGuiOpenEvent;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GuiManagerTest {

    private GuiManager<CorePlayer, CorePlugin> guiManager;
    private CorePlugin mockPlugin;
    private Server mockServer;
    private PluginManager mockPluginManager;

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

        // Set the Bukkit server so Bukkit.getPluginManager() works
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

    @SuppressWarnings("unchecked")
    private Gui<CorePlayer> createMockGui() {
        Gui<CorePlayer> gui = mock(Gui.class);
        when(gui.getUUID()).thenReturn(UUID.randomUUID());
        return gui;
    }

    private CorePlayer createMockPlayer(UUID uuid) {
        CorePlayer player = mock(CorePlayer.class);
        when(player.getUUID()).thenReturn(uuid);
        return player;
    }

    @Test
    @DisplayName("Given no tracked players, when doesPlayerHaveGui with UUID, then returns false")
    void doesPlayerHaveGui_returnsFalse_whenNoPlayersTracked() {
        assertFalse(guiManager.doesPlayerHaveGui(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Given a tracked player, when doesPlayerHaveGui with UUID, then returns true")
    void doesPlayerHaveGui_returnsTrue_whenPlayerIsTracked() {
        UUID playerUUID = UUID.randomUUID();
        Gui<CorePlayer> gui = createMockGui();

        guiManager.trackPlayerGui(playerUUID, gui);

        assertTrue(guiManager.doesPlayerHaveGui(playerUUID));
    }

    @Test
    @DisplayName("Given a tracked player, when doesPlayerHaveGui with CorePlayer, then delegates to UUID check")
    void doesPlayerHaveGui_delegatesToUUID_whenCalledWithCorePlayer() {
        UUID playerUUID = UUID.randomUUID();
        CorePlayer corePlayer = createMockPlayer(playerUUID);
        Gui<CorePlayer> gui = createMockGui();

        guiManager.trackPlayerGui(playerUUID, gui);

        assertTrue(guiManager.doesPlayerHaveGui(corePlayer));
    }

    @Test
    @DisplayName("Given a tracked player, when doesPlayerHaveGui with Player, then delegates to UUID check")
    void doesPlayerHaveGui_delegatesToUUID_whenCalledWithPlayer() {
        UUID playerUUID = UUID.randomUUID();
        Player bukkitPlayer = mock(Player.class);
        when(bukkitPlayer.getUniqueId()).thenReturn(playerUUID);
        Gui<CorePlayer> gui = createMockGui();

        guiManager.trackPlayerGui(playerUUID, gui);

        assertTrue(guiManager.doesPlayerHaveGui(bukkitPlayer));
    }

    @Test
    @DisplayName("Given a tracked player, when getOpenedGui with UUID, then returns the gui")
    void getOpenedGui_returnsGui_whenPlayerIsTracked() {
        UUID playerUUID = UUID.randomUUID();
        Gui<CorePlayer> gui = createMockGui();

        guiManager.trackPlayerGui(playerUUID, gui);

        Optional<Gui<CorePlayer>> result = guiManager.getOpenedGui(playerUUID);
        assertTrue(result.isPresent());
        assertEquals(gui, result.get());
    }

    @Test
    @DisplayName("Given no tracked players, when getOpenedGui with UUID, then returns empty")
    void getOpenedGui_returnsEmpty_whenPlayerNotTracked() {
        Optional<Gui<CorePlayer>> result = guiManager.getOpenedGui(UUID.randomUUID());
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Given a tracked player, when getOpenedGui with CorePlayer, then delegates to UUID lookup")
    void getOpenedGui_delegatesToUUID_whenCalledWithCorePlayer() {
        UUID playerUUID = UUID.randomUUID();
        CorePlayer corePlayer = createMockPlayer(playerUUID);
        Gui<CorePlayer> gui = createMockGui();

        guiManager.trackPlayerGui(playerUUID, gui);

        Optional<Gui<CorePlayer>> result = guiManager.getOpenedGui(corePlayer);
        assertTrue(result.isPresent());
        assertEquals(gui, result.get());
    }

    @Test
    @DisplayName("Given a tracked player, when getOpenedGui with Player, then returns the gui")
    void getOpenedGui_returnsGui_whenCalledWithPlayer() {
        UUID playerUUID = UUID.randomUUID();
        Player bukkitPlayer = mock(Player.class);
        when(bukkitPlayer.getUniqueId()).thenReturn(playerUUID);
        Gui<CorePlayer> gui = createMockGui();

        guiManager.trackPlayerGui(playerUUID, gui);

        Optional<Gui<CorePlayer>> result = guiManager.getOpenedGui(bukkitPlayer);
        assertTrue(result.isPresent());
        assertEquals(gui, result.get());
    }

    @Test
    @DisplayName("Given a new gui, when trackPlayerGui, then registers listeners")
    void trackPlayerGui_registersListeners_whenGuiIsNew() {
        UUID playerUUID = UUID.randomUUID();
        Gui<CorePlayer> gui = createMockGui();

        guiManager.trackPlayerGui(playerUUID, gui);

        verify(gui).registerListeners();
    }

    @Test
    @DisplayName("Given gui already tracked by another player, when trackPlayerGui, then does not re-register listeners")
    void trackPlayerGui_doesNotReRegisterListeners_whenGuiAlreadyTracked() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        Gui<CorePlayer> gui = createMockGui();

        guiManager.trackPlayerGui(player1, gui);
        guiManager.trackPlayerGui(player2, gui);

        verify(gui).registerListeners();
    }

    @Test
    @DisplayName("Given a tracked player, when trackPlayerGui with a different gui, then old gui is stopped")
    void trackPlayerGui_stopsOldGui_whenPlayerAlreadyTracked() {
        UUID playerUUID = UUID.randomUUID();
        Gui<CorePlayer> gui1 = createMockGui();
        Gui<CorePlayer> gui2 = createMockGui();

        guiManager.trackPlayerGui(playerUUID, gui1);
        guiManager.trackPlayerGui(playerUUID, gui2);

        verify(gui1).unregisterListeners();
        verify(gui2).registerListeners();
        Optional<Gui<CorePlayer>> result = guiManager.getOpenedGui(playerUUID);
        assertTrue(result.isPresent());
        assertEquals(gui2, result.get());
    }

    @Test
    @DisplayName("Given a tracked player, when trackPlayerGui with CorePlayer, then delegates to UUID version")
    void trackPlayerGui_delegatesToUUID_whenCalledWithCorePlayer() {
        UUID playerUUID = UUID.randomUUID();
        CorePlayer corePlayer = createMockPlayer(playerUUID);
        Gui<CorePlayer> gui = createMockGui();

        guiManager.trackPlayerGui(corePlayer, gui);

        assertTrue(guiManager.doesPlayerHaveGui(playerUUID));
    }

    @Test
    @DisplayName("Given a tracked player, when trackPlayerGui with Player, then delegates to UUID version")
    void trackPlayerGui_delegatesToUUID_whenCalledWithPlayer() {
        UUID playerUUID = UUID.randomUUID();
        Player bukkitPlayer = mock(Player.class);
        when(bukkitPlayer.getUniqueId()).thenReturn(playerUUID);
        Gui<CorePlayer> gui = createMockGui();

        guiManager.trackPlayerGui(bukkitPlayer, gui);

        assertTrue(guiManager.doesPlayerHaveGui(playerUUID));
    }

    @Test
    @DisplayName("Given sole viewer, when stopTrackingPlayer with UUID, then unregisters listeners")
    void stopTrackingPlayer_unregistersListeners_whenLastViewer() {
        UUID playerUUID = UUID.randomUUID();
        Gui<CorePlayer> gui = createMockGui();

        guiManager.trackPlayerGui(playerUUID, gui);
        guiManager.stopTrackingPlayer(playerUUID);

        verify(gui).unregisterListeners();
        assertFalse(guiManager.doesPlayerHaveGui(playerUUID));
    }

    @Test
    @DisplayName("Given multiple viewers, when stopTrackingPlayer for one, then does not unregister listeners")
    void stopTrackingPlayer_doesNotUnregister_whenOtherViewersRemain() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        Gui<CorePlayer> gui = createMockGui();

        guiManager.trackPlayerGui(player1, gui);
        guiManager.trackPlayerGui(player2, gui);
        guiManager.stopTrackingPlayer(player1);

        verify(gui, never()).unregisterListeners();
        assertFalse(guiManager.doesPlayerHaveGui(player1));
        assertTrue(guiManager.doesPlayerHaveGui(player2));
    }

    @Test
    @DisplayName("Given an untracked player, when stopTrackingPlayer, then no exception is thrown")
    void stopTrackingPlayer_doesNothing_whenPlayerNotTracked() {
        assertDoesNotThrow(() -> guiManager.stopTrackingPlayer(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Given a tracked player, when stopTrackingPlayer with CorePlayer, then delegates to UUID version")
    void stopTrackingPlayer_delegatesToUUID_whenCalledWithCorePlayer() {
        UUID playerUUID = UUID.randomUUID();
        CorePlayer corePlayer = createMockPlayer(playerUUID);
        Gui<CorePlayer> gui = createMockGui();

        guiManager.trackPlayerGui(playerUUID, gui);
        guiManager.stopTrackingPlayer(corePlayer);

        assertFalse(guiManager.doesPlayerHaveGui(playerUUID));
    }

    @Test
    @DisplayName("Given a tracked player, when stopTrackingPlayer with Player, then delegates to UUID version")
    void stopTrackingPlayer_delegatesToUUID_whenCalledWithPlayer() {
        UUID playerUUID = UUID.randomUUID();
        Player bukkitPlayer = mock(Player.class);
        when(bukkitPlayer.getUniqueId()).thenReturn(playerUUID);
        Gui<CorePlayer> gui = createMockGui();

        guiManager.trackPlayerGui(playerUUID, gui);
        guiManager.stopTrackingPlayer(bukkitPlayer);

        assertFalse(guiManager.doesPlayerHaveGui(playerUUID));
    }

    @Test
    @DisplayName("Given a gui with online viewer, when refreshGui, then calls updateInventory")
    void refreshGui_callsUpdateInventory_whenPlayerIsOnline() {
        UUID playerUUID = UUID.randomUUID();
        Gui<CorePlayer> gui = createMockGui();
        Player onlinePlayer = mock(Player.class);

        guiManager.trackPlayerGui(playerUUID, gui);
        when(mockServer.getPlayer(playerUUID)).thenReturn(onlinePlayer);

        guiManager.refreshGui(gui);

        verify(onlinePlayer).updateInventory();
    }

    @Test
    @DisplayName("Given a gui with offline viewer, when refreshGui, then does not throw")
    void refreshGui_doesNotThrow_whenPlayerIsOffline() {
        UUID playerUUID = UUID.randomUUID();
        Gui<CorePlayer> gui = createMockGui();

        guiManager.trackPlayerGui(playerUUID, gui);
        when(mockServer.getPlayer(playerUUID)).thenReturn(null);

        assertDoesNotThrow(() -> guiManager.refreshGui(gui));
    }

    @Test
    @DisplayName("Given a keyed gui, when trackPlayerGui, then fires CoreGuiOpenEvent")
    void trackPlayerGui_firesEvent_always() {
        UUID playerUUID = UUID.randomUUID();
        Gui<CorePlayer> gui = createMockGui();

        guiManager.trackPlayerGui(playerUUID, gui);

        verify(mockPluginManager).callEvent(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Given a KeyedGui with a key, when trackPlayerGui, then CoreGuiOpenEvent carries the key")
    void trackPlayerGui_firesEventWithKey_whenGuiIsKeyed() {
        UUID playerUUID = UUID.randomUUID();
        NamespacedKey expectedKey = new NamespacedKey("testplugin", "test_gui");

        Gui<CorePlayer> gui = mock(Gui.class, org.mockito.Mockito.withSettings().extraInterfaces(KeyedGui.class));
        when(gui.getUUID()).thenReturn(UUID.randomUUID());
        when(((KeyedGui) gui).getGuiKey()).thenReturn(Optional.of(expectedKey));

        guiManager.trackPlayerGui(playerUUID, gui);

        ArgumentCaptor<CoreGuiOpenEvent> captor = ArgumentCaptor.forClass(CoreGuiOpenEvent.class);
        verify(mockPluginManager).callEvent(captor.capture());
        CoreGuiOpenEvent event = captor.getValue();
        assertTrue(event.getGuiKey().isPresent());
        assertEquals(expectedKey, event.getGuiKey().get());
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Given a KeyedGui with empty key, when trackPlayerGui, then CoreGuiOpenEvent has empty key")
    void trackPlayerGui_firesEventWithEmptyKey_whenKeyedGuiReturnsEmpty() {
        UUID playerUUID = UUID.randomUUID();

        Gui<CorePlayer> gui = mock(Gui.class, org.mockito.Mockito.withSettings().extraInterfaces(KeyedGui.class));
        when(gui.getUUID()).thenReturn(UUID.randomUUID());
        when(((KeyedGui) gui).getGuiKey()).thenReturn(Optional.empty());

        guiManager.trackPlayerGui(playerUUID, gui);

        ArgumentCaptor<CoreGuiOpenEvent> captor = ArgumentCaptor.forClass(CoreGuiOpenEvent.class);
        verify(mockPluginManager).callEvent(captor.capture());
        CoreGuiOpenEvent event = captor.getValue();
        assertFalse(event.getGuiKey().isPresent());
    }

    @Test
    @DisplayName("Given a non-keyed gui, when trackPlayerGui, then CoreGuiOpenEvent has empty key")
    void trackPlayerGui_firesEventWithNullKey_whenGuiIsNotKeyed() {
        UUID playerUUID = UUID.randomUUID();
        Gui<CorePlayer> gui = createMockGui();

        guiManager.trackPlayerGui(playerUUID, gui);

        ArgumentCaptor<CoreGuiOpenEvent> captor = ArgumentCaptor.forClass(CoreGuiOpenEvent.class);
        verify(mockPluginManager).callEvent(captor.capture());
        CoreGuiOpenEvent event = captor.getValue();
        assertFalse(event.getGuiKey().isPresent());
    }
}

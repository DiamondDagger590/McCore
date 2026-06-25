package com.diamonddagger590.mccore.listener;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.gui.ClosableGui;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.GuiManager;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.CoreManagerKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class GuiCloseListenerTest {

    private GuiCloseListener listener;

    @Mock
    private CorePlugin mockPlugin;

    @Mock
    private GuiManager<CorePlayer, CorePlugin> mockGuiManager;

    @Mock
    private Player mockPlayer;

    @Mock
    private InventoryCloseEvent mockEvent;

    @Mock
    private Inventory mockInventory;

    private MockedStatic<CorePlugin> corePluginStatic;

    @BeforeEach
    void setUp() {
        listener = new GuiCloseListener();
        RegistryResetExtension.setupRegistry();
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockGuiManager);

        corePluginStatic = mockStatic(CorePlugin.class);
        corePluginStatic.when(CorePlugin::getInstance).thenReturn(mockPlugin);
        org.mockito.Mockito.lenient().when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());
    }

    @AfterEach
    void tearDown() {
        corePluginStatic.close();
        RegistryResetExtension.resetRegistry();
    }

    @Test
    @DisplayName("Given a non-Player HumanEntity closes inventory, when handling close, then no GUI tracking occurs")
    void handleGuiClose_noOp_whenEntityIsNotPlayer() {
        HumanEntity nonPlayer = mock(HumanEntity.class);
        when(mockEvent.getPlayer()).thenReturn(nonPlayer);

        listener.handleGuiClose(mockEvent);

        verify(mockGuiManager, never()).getOpenedGui(mockPlayer);
        verify(mockGuiManager, never()).stopTrackingPlayer(mockPlayer);
    }

    @Test
    @DisplayName("Given a Player with no open GUI, when closing inventory, then no tracking change occurs")
    void handleGuiClose_noOp_whenPlayerHasNoGui() {
        when(mockEvent.getPlayer()).thenReturn(mockPlayer);
        when(mockGuiManager.getOpenedGui(mockPlayer)).thenReturn(Optional.empty());

        listener.handleGuiClose(mockEvent);

        verify(mockGuiManager, never()).stopTrackingPlayer(mockPlayer);
    }

    @Test
    @DisplayName("Given a Player with an open GUI and matching inventory, when closing, then stops tracking player")
    void handleGuiClose_stopsTracking_whenInventoryMatches() {
        Gui<CorePlayer> mockGui = mock(Gui.class);
        when(mockEvent.getPlayer()).thenReturn(mockPlayer);
        when(mockEvent.getInventory()).thenReturn(mockInventory);
        doReturn(Optional.of(mockGui)).when(mockGuiManager).getOpenedGui(mockPlayer);
        when(mockGui.getInventory()).thenReturn(mockInventory);

        listener.handleGuiClose(mockEvent);

        verify(mockGuiManager).stopTrackingPlayer(mockPlayer);
    }

    @Test
    @DisplayName("Given a Player with an open GUI but different inventory, when closing, then does not stop tracking")
    void handleGuiClose_doesNotStopTracking_whenInventoryDoesNotMatch() {
        Gui<CorePlayer> mockGui = mock(Gui.class);
        Inventory differentInventory = mock(Inventory.class);
        when(mockEvent.getPlayer()).thenReturn(mockPlayer);
        when(mockEvent.getInventory()).thenReturn(mockInventory);
        doReturn(Optional.of(mockGui)).when(mockGuiManager).getOpenedGui(mockPlayer);
        when(mockGui.getInventory()).thenReturn(differentInventory);

        listener.handleGuiClose(mockEvent);

        verify(mockGuiManager, never()).stopTrackingPlayer(mockPlayer);
    }

    @Test
    @DisplayName("Given a ClosableGui with matching inventory, when closing, then calls onClose and stops tracking")
    void handleGuiClose_callsOnClose_whenGuiIsClosable() {
        TestClosableGui mockClosableGui = mock(TestClosableGui.class);
        when(mockEvent.getPlayer()).thenReturn(mockPlayer);
        when(mockEvent.getInventory()).thenReturn(mockInventory);
        doReturn(Optional.of(mockClosableGui)).when(mockGuiManager).getOpenedGui(mockPlayer);
        when(mockClosableGui.getInventory()).thenReturn(mockInventory);

        listener.handleGuiClose(mockEvent);

        verify(mockGuiManager).stopTrackingPlayer(mockPlayer);
        verify(mockClosableGui).onClose(mockEvent);
    }

    @Test
    @DisplayName("Given a ClosableGui with different inventory, when closing, then calls onClose but does not stop tracking")
    void handleGuiClose_callsOnClose_whenClosableGuiButDifferentInventory() {
        TestClosableGui mockClosableGui = mock(TestClosableGui.class);
        Inventory differentInventory = mock(Inventory.class);
        when(mockEvent.getPlayer()).thenReturn(mockPlayer);
        when(mockEvent.getInventory()).thenReturn(mockInventory);
        doReturn(Optional.of(mockClosableGui)).when(mockGuiManager).getOpenedGui(mockPlayer);
        when(mockClosableGui.getInventory()).thenReturn(differentInventory);

        listener.handleGuiClose(mockEvent);

        verify(mockGuiManager, never()).stopTrackingPlayer(mockPlayer);
        verify(mockClosableGui).onClose(mockEvent);
    }

    @Test
    @DisplayName("Given a non-ClosableGui with matching inventory, when closing, then stops tracking but does not call onClose")
    void handleGuiClose_doesNotCallOnClose_whenGuiIsNotClosable() {
        Gui<CorePlayer> mockGui = mock(Gui.class);
        when(mockEvent.getPlayer()).thenReturn(mockPlayer);
        when(mockEvent.getInventory()).thenReturn(mockInventory);
        doReturn(Optional.of(mockGui)).when(mockGuiManager).getOpenedGui(mockPlayer);
        when(mockGui.getInventory()).thenReturn(mockInventory);

        listener.handleGuiClose(mockEvent);

        verify(mockGuiManager).stopTrackingPlayer(mockPlayer);
    }

    private interface TestClosableGui extends Gui<CorePlayer>, ClosableGui<CorePlayer> {}
}

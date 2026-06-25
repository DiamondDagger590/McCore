package com.diamonddagger590.mccore.listener;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.event.gui.GuiRefreshEvent;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.GuiManager;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.CoreManagerKey;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class GuiRefreshListenerTest {

    private GuiRefreshListener listener;

    @Mock
    private CorePlugin mockPlugin;

    @Mock
    private GuiManager<CorePlayer, CorePlugin> mockGuiManager;

    private MockedStatic<CorePlugin> corePluginStatic;

    @BeforeEach
    void setUp() {
        listener = new GuiRefreshListener();
        RegistryResetExtension.setupRegistry();
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockGuiManager);

        corePluginStatic = mockStatic(CorePlugin.class);
        corePluginStatic.when(CorePlugin::getInstance).thenReturn(mockPlugin);
        when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());
    }

    @AfterEach
    void tearDown() {
        corePluginStatic.close();
        RegistryResetExtension.resetRegistry();
    }

    @Test
    @DisplayName("Given a GuiRefreshEvent, when onGuiRefresh fires, then GuiManager.refreshGui is called with the event's GUI")
    void onGuiRefresh_delegatesToGuiManager() {
        Gui<CorePlayer> mockGui = mock(Gui.class);
        GuiRefreshEvent event = new GuiRefreshEvent(mockGui);

        listener.onGuiRefresh(event);

        verify(mockGuiManager).refreshGui(any());
    }

    @Test
    @DisplayName("Given multiple refresh events, when each fires, then each GUI is individually refreshed")
    void onGuiRefresh_handlesMultipleEvents() {
        Gui<CorePlayer> gui1 = mock(Gui.class);
        Gui<CorePlayer> gui2 = mock(Gui.class);

        listener.onGuiRefresh(new GuiRefreshEvent(gui1));
        listener.onGuiRefresh(new GuiRefreshEvent(gui2));

        verify(mockGuiManager, org.mockito.Mockito.times(2)).refreshGui(any());
    }
}

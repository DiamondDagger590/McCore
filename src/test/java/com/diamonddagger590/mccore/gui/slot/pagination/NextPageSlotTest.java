package com.diamonddagger590.mccore.gui.slot.pagination;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.GuiManager;
import com.diamonddagger590.mccore.gui.PaginatedGui;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NextPageSlotTest {

    @Mock
    private CorePlugin mockPlugin;

    @Mock
    private GuiManager<CorePlayer, CorePlugin> mockGuiManager;

    private MockedStatic<CorePlugin> corePluginStatic;

    private final NextPageSlot<CorePlayer> slot = new NextPageSlot<>() {};

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();
        ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        managerRegistry.register(mockGuiManager);

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
    @DisplayName("Given a valid gui types set, when checking, then it contains PaginatedGui")
    void getValidGuiTypes_containsPaginatedGui() {
        Set<Class<?>> validTypes = slot.getValidGuiTypes();
        assertEquals(1, validTypes.size());
        assertTrue(validTypes.contains(PaginatedGui.class));
    }

    @Test
    @DisplayName("Given a click event, when onClick is called, then returns true")
    void onClick_returnsTrue() {
        CorePlayer corePlayer = mock(CorePlayer.class);
        when(mockGuiManager.getOpenedGui(corePlayer)).thenReturn(Optional.empty());

        boolean result = slot.onClick(corePlayer, ClickType.LEFT);
        assertTrue(result);
    }

    @Test
    @DisplayName("Given no open gui, when onClick, then no page change occurs")
    void onClick_noGui_noPageChange() {
        CorePlayer corePlayer = mock(CorePlayer.class);
        when(mockGuiManager.getOpenedGui(corePlayer)).thenReturn(Optional.empty());

        slot.onClick(corePlayer, ClickType.LEFT);
        verify(mockGuiManager).getOpenedGui(corePlayer);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Given a paginated gui at page 1 with max 3, when onClick, then page increments to 2")
    void onClick_withPaginatedGui_incrementsPage() {
        CorePlayer corePlayer = mock(CorePlayer.class);
        PaginatedGui<CorePlayer> paginatedGui = mock(PaginatedGui.class);
        Player bukkitPlayer = mock(Player.class);

        when(mockGuiManager.getOpenedGui(corePlayer)).thenReturn(Optional.of(paginatedGui));
        when(paginatedGui.getPage()).thenReturn(1);
        when(paginatedGui.getMaximumPage()).thenReturn(3);
        when(corePlayer.getAsBukkitPlayer()).thenReturn(Optional.of(bukkitPlayer));

        slot.onClick(corePlayer, ClickType.LEFT);

        verify(paginatedGui).setPage(2);
        verify(paginatedGui).refreshGUI();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Given a paginated gui already at max page, when onClick, then page does not change")
    void onClick_atMaxPage_doesNotIncrement() {
        CorePlayer corePlayer = mock(CorePlayer.class);
        PaginatedGui<CorePlayer> paginatedGui = mock(PaginatedGui.class);

        when(mockGuiManager.getOpenedGui(corePlayer)).thenReturn(Optional.of(paginatedGui));
        when(paginatedGui.getPage()).thenReturn(3);
        when(paginatedGui.getMaximumPage()).thenReturn(3);

        slot.onClick(corePlayer, ClickType.LEFT);

        verify(paginatedGui, never()).setPage(4);
        verify(paginatedGui, never()).refreshGUI();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Given a paginated gui but player is offline, when onClick, then page does not change")
    void onClick_playerOffline_doesNotIncrement() {
        CorePlayer corePlayer = mock(CorePlayer.class);
        PaginatedGui<CorePlayer> paginatedGui = mock(PaginatedGui.class);

        when(mockGuiManager.getOpenedGui(corePlayer)).thenReturn(Optional.of(paginatedGui));
        when(paginatedGui.getPage()).thenReturn(1);
        when(paginatedGui.getMaximumPage()).thenReturn(3);
        when(corePlayer.getAsBukkitPlayer()).thenReturn(Optional.empty());

        slot.onClick(corePlayer, ClickType.LEFT);

        verify(paginatedGui, never()).setPage(2);
        verify(paginatedGui, never()).refreshGUI();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Given a paginated gui at page 2 of 3, when onClick, then page increments to 3")
    void onClick_middlePage_incrementsToNext() {
        CorePlayer corePlayer = mock(CorePlayer.class);
        PaginatedGui<CorePlayer> paginatedGui = mock(PaginatedGui.class);
        Player bukkitPlayer = mock(Player.class);

        when(mockGuiManager.getOpenedGui(corePlayer)).thenReturn(Optional.of(paginatedGui));
        when(paginatedGui.getPage()).thenReturn(2);
        when(paginatedGui.getMaximumPage()).thenReturn(3);
        when(corePlayer.getAsBukkitPlayer()).thenReturn(Optional.of(bukkitPlayer));

        slot.onClick(corePlayer, ClickType.LEFT);

        verify(paginatedGui).setPage(3);
        verify(paginatedGui).refreshGUI();
    }

    @Test
    @DisplayName("Given a non-paginated gui is open, when onClick, then no page change occurs")
    void onClick_nonPaginatedGui_noPageChange() {
        CorePlayer corePlayer = mock(CorePlayer.class);
        Gui<CorePlayer> nonPaginatedGui = mock(Gui.class);
        when(mockGuiManager.getOpenedGui(corePlayer)).thenReturn(Optional.of(nonPaginatedGui));

        boolean result = slot.onClick(corePlayer, ClickType.LEFT);

        assertTrue(result);
    }

    @Test
    @DisplayName("Given a valid gui types set, then set is immutable")
    void getValidGuiTypes_isImmutable() {
        Set<Class<?>> validTypes = slot.getValidGuiTypes();
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> validTypes.add(Object.class));
    }
}

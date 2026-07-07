package com.diamonddagger590.mccore.gui.slot.setting;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.GuiManager;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.util.LinkedNode;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerSettingSlotTest {

    @Mock
    private CorePlugin mockPlugin;

    @Mock
    private GuiManager<CorePlayer, CorePlugin> mockGuiManager;

    @Mock
    private PlayerSetting mockSetting;

    @Mock
    private PlayerSetting nextSetting;

    @Mock
    private CorePlayer mockCorePlayer;

    @Mock
    private Player mockBukkitPlayer;

    private MockedStatic<CorePlugin> corePluginStatic;

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();
        ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        managerRegistry.register(mockGuiManager);
    }

    @AfterEach
    void tearDown() {
        if (corePluginStatic != null) {
            corePluginStatic.close();
        }
        RegistryResetExtension.resetRegistry();
    }

    private PlayerSettingSlot<PlayerSetting, CorePlayer> createSlot(CorePlayer player, PlayerSetting setting) {
        return new PlayerSettingSlot<>(player, setting) {
            @Override
            public boolean onClick(@org.jetbrains.annotations.NotNull CorePlayer cp, @org.jetbrains.annotations.NotNull ClickType ct) {
                return super.onClick(cp, ct);
            }
        };
    }

    @Test
    @DisplayName("Given an online player, when constructing slot, then fields are set correctly")
    void constructor_onlinePlayer_fieldsSet() {
        when(mockCorePlayer.getAsBukkitPlayer()).thenReturn(Optional.of(mockBukkitPlayer));

        PlayerSettingSlot<PlayerSetting, CorePlayer> slot = createSlot(mockCorePlayer, mockSetting);

        assertNotNull(slot);
        assertSame(mockSetting, slot.getSetting());
    }

    @Test
    @DisplayName("Given an offline player, when constructing slot, then throws CorePlayerOfflineException")
    void constructor_offlinePlayer_throwsException() {
        when(mockCorePlayer.getAsBukkitPlayer()).thenReturn(Optional.empty());

        assertThrows(CorePlayerOfflineException.class, () -> createSlot(mockCorePlayer, mockSetting));
    }

    @Test
    @DisplayName("Given a setting, when getSetting is called, then returns the correct setting")
    void getSetting_returnsCorrectSetting() {
        when(mockCorePlayer.getAsBukkitPlayer()).thenReturn(Optional.of(mockBukkitPlayer));

        PlayerSettingSlot<PlayerSetting, CorePlayer> slot = createSlot(mockCorePlayer, mockSetting);

        assertSame(mockSetting, slot.getSetting());
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Given a gui is open, when onClick, then setting advances and gui refreshes")
    void onClick_withOpenGui_advancesSettingAndRefreshes() {
        when(mockCorePlayer.getAsBukkitPlayer()).thenReturn(Optional.of(mockBukkitPlayer));
        when(mockCorePlayer.getPlugin()).thenReturn(mockPlugin);
        when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());

        @SuppressWarnings("unchecked")
        LinkedNode<PlayerSetting> nextNode = mock(LinkedNode.class);
        when(mockSetting.getNextSetting()).thenAnswer(inv -> nextNode);
        when(nextNode.getNodeValue()).thenReturn(nextSetting);

        Gui<CorePlayer> mockGui = mock(Gui.class);
        when(mockGuiManager.getOpenedGui(mockCorePlayer)).thenReturn(Optional.of(mockGui));

        PlayerSettingSlot<PlayerSetting, CorePlayer> slot = createSlot(mockCorePlayer, mockSetting);
        boolean result = slot.onClick(mockCorePlayer, ClickType.LEFT);

        assertTrue(result);
        verify(mockCorePlayer).setPlayerSetting(nextSetting);
        verify(mockGui).refreshGUI();
    }

    @Test
    @DisplayName("Given no gui is open, when onClick, then setting does not change")
    void onClick_noGui_settingUnchanged() {
        when(mockCorePlayer.getAsBukkitPlayer()).thenReturn(Optional.of(mockBukkitPlayer));
        when(mockCorePlayer.getPlugin()).thenReturn(mockPlugin);
        when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());
        when(mockGuiManager.getOpenedGui(mockCorePlayer)).thenReturn(Optional.empty());

        PlayerSettingSlot<PlayerSetting, CorePlayer> slot = createSlot(mockCorePlayer, mockSetting);
        boolean result = slot.onClick(mockCorePlayer, ClickType.LEFT);

        assertTrue(result);
        verify(mockCorePlayer, never()).setPlayerSetting(any(PlayerSetting.class));
    }

    @Test
    @DisplayName("Given an online player, when accessing corePlayer field, then it is the same as constructor arg")
    void corePlayerField_isSetFromConstructor() {
        when(mockCorePlayer.getAsBukkitPlayer()).thenReturn(Optional.of(mockBukkitPlayer));

        PlayerSettingSlot<PlayerSetting, CorePlayer> slot = createSlot(mockCorePlayer, mockSetting);

        assertSame(mockCorePlayer, slot.corePlayer);
    }

    @Test
    @DisplayName("Given an online player, when accessing player field, then it is the Bukkit player")
    void playerField_isBukkitPlayer() {
        when(mockCorePlayer.getAsBukkitPlayer()).thenReturn(Optional.of(mockBukkitPlayer));

        PlayerSettingSlot<PlayerSetting, CorePlayer> slot = createSlot(mockCorePlayer, mockSetting);

        assertSame(mockBukkitPlayer, slot.player);
    }
}

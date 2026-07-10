package com.diamonddagger590.mccore.player;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.event.setting.setting.PlayerSettingChangeEvent;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import com.diamonddagger590.mccore.util.LinkedNode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorePlayerMockBukkitTest {

    private enum TestSetting implements PlayerSetting {
        OPTION_A,
        OPTION_B;

        private static final NamespacedKey KEY = new NamespacedKey("test", "my_setting");

        @Override
        @NotNull
        public NamespacedKey getSettingKey() {
            return KEY;
        }

        @Override
        @NotNull
        public LinkedNode<? extends PlayerSetting> getFirstSetting() {
            return new LinkedNode<>(OPTION_A);
        }

        @Override
        @NotNull
        public LinkedNode<? extends PlayerSetting> getNextSetting() {
            return this == OPTION_A ? new LinkedNode<>(OPTION_B) : new LinkedNode<>(OPTION_A);
        }

        @Override
        public void onSettingChange(@NotNull CorePlayer player, @NotNull Optional<PlayerSetting> oldSetting) {
        }

        @Override
        @NotNull
        public Optional<? extends PlayerSetting> fromString(@NotNull String setting) {
            try {
                return Optional.of(TestSetting.valueOf(setting));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
    }

    private static class TestCorePlayer extends CorePlayer {
        TestCorePlayer(@NotNull UUID uuid, @NotNull CorePlugin plugin) {
            super(uuid, plugin);
        }

        @Override
        public boolean useMutex() {
            return false;
        }
    }

    private ServerMock server;
    private TestCorePlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestCorePlugin.class);
        RegistryResetExtension.setupRegistry();
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Given a player online in MockBukkit, when getAsBukkitPlayer is called, then returns the player")
    void getAsBukkitPlayer_returnsPlayer_whenPlayerIsOnline() {
        Player bukkitPlayer = server.addPlayer();
        UUID uuid = bukkitPlayer.getUniqueId();
        TestCorePlayer corePlayer = new TestCorePlayer(uuid, plugin);

        Optional<Player> result = corePlayer.getAsBukkitPlayer();

        assertTrue(result.isPresent());
        assertEquals(uuid, result.get().getUniqueId());
    }

    @Test
    @DisplayName("Given no player online, when getAsBukkitPlayer is called, then returns empty Optional")
    void getAsBukkitPlayer_returnsEmpty_whenPlayerIsOffline() {
        UUID offlineUuid = UUID.randomUUID();
        TestCorePlayer corePlayer = new TestCorePlayer(offlineUuid, plugin);

        Optional<Player> result = corePlayer.getAsBukkitPlayer();

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Given a CorePlayer, when setPlayerSetting is called, then getPlayerSetting returns the new setting")
    void setPlayerSetting_storesSetting_andRetrievableByKey() {
        UUID uuid = UUID.randomUUID();
        TestCorePlayer corePlayer = new TestCorePlayer(uuid, plugin);

        corePlayer.setPlayerSetting(TestSetting.OPTION_A);

        Optional<? extends PlayerSetting> retrieved = corePlayer.getPlayerSetting(TestSetting.KEY);
        assertTrue(retrieved.isPresent());
        assertEquals(TestSetting.OPTION_A, retrieved.get());
    }

    @Test
    @DisplayName("Given a CorePlayer with an existing setting, when setPlayerSetting is called with new value, then setting is overwritten")
    void setPlayerSetting_overwritesPreviousSetting_whenKeyAlreadyPresent() {
        UUID uuid = UUID.randomUUID();
        TestCorePlayer corePlayer = new TestCorePlayer(uuid, plugin);

        corePlayer.setPlayerSetting(TestSetting.OPTION_A);
        corePlayer.setPlayerSetting(TestSetting.OPTION_B);

        Optional<? extends PlayerSetting> retrieved = corePlayer.getPlayerSetting(TestSetting.KEY);
        assertTrue(retrieved.isPresent());
        assertEquals(TestSetting.OPTION_B, retrieved.get());
    }

    @Test
    @DisplayName("Given a CorePlayer, when setPlayerSetting is called, then getPlayerSettings contains the setting")
    void setPlayerSetting_addsToPlayerSettings_set() {
        UUID uuid = UUID.randomUUID();
        TestCorePlayer corePlayer = new TestCorePlayer(uuid, plugin);

        corePlayer.setPlayerSetting(TestSetting.OPTION_A);

        assertEquals(1, corePlayer.getPlayerSettings().size());
        assertTrue(corePlayer.getPlayerSettings().contains(TestSetting.OPTION_A));
    }

    @Test
    @DisplayName("Given a CorePlayer, when setPlayerSetting is called, then PlayerSettingChangeEvent is fired")
    void setPlayerSetting_firesChangeEvent_withCorrectData() {
        UUID uuid = UUID.randomUUID();
        TestCorePlayer corePlayer = new TestCorePlayer(uuid, plugin);

        AtomicReference<PlayerSettingChangeEvent> capturedEvent = new AtomicReference<>();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onSettingChange(PlayerSettingChangeEvent event) {
                capturedEvent.set(event);
            }
        }, plugin);

        corePlayer.setPlayerSetting(TestSetting.OPTION_A);

        assertNotNull(capturedEvent.get());
        assertEquals(TestSetting.OPTION_A, capturedEvent.get().getNewSetting());
        assertFalse(capturedEvent.get().getOldSetting().isPresent());
    }

    @Test
    @DisplayName("Given a CorePlayer with existing setting, when setPlayerSetting is called, then event has old setting")
    void setPlayerSetting_firesChangeEvent_withOldSetting() {
        UUID uuid = UUID.randomUUID();
        TestCorePlayer corePlayer = new TestCorePlayer(uuid, plugin);

        corePlayer.setPlayerSetting(TestSetting.OPTION_A);

        AtomicReference<PlayerSettingChangeEvent> capturedEvent = new AtomicReference<>();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onSettingChange(PlayerSettingChangeEvent event) {
                capturedEvent.set(event);
            }
        }, plugin);

        corePlayer.setPlayerSetting(TestSetting.OPTION_B);

        assertNotNull(capturedEvent.get());
        assertEquals(TestSetting.OPTION_B, capturedEvent.get().getNewSetting());
        assertTrue(capturedEvent.get().getOldSetting().isPresent());
        assertEquals(TestSetting.OPTION_A, capturedEvent.get().getOldSetting().get());
    }

    @Test
    @DisplayName("Given a CorePlayer with a setting, when setPlayerSetting replaces it, then event references the correct player")
    void setPlayerSetting_firesChangeEvent_withCorrectPlayer() {
        UUID uuid = UUID.randomUUID();
        TestCorePlayer corePlayer = new TestCorePlayer(uuid, plugin);

        AtomicReference<PlayerSettingChangeEvent> capturedEvent = new AtomicReference<>();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onSettingChange(PlayerSettingChangeEvent event) {
                capturedEvent.set(event);
            }
        }, plugin);

        corePlayer.setPlayerSetting(TestSetting.OPTION_A);

        assertNotNull(capturedEvent.get());
        assertSame(corePlayer, capturedEvent.get().getCorePlayer());
    }
}

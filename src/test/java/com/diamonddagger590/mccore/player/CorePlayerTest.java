package com.diamonddagger590.mccore.player;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.event.setting.setting.PlayerSettingChangeEvent;
import com.diamonddagger590.mccore.external.common.AfkPluginHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.util.LinkedNode;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CorePlayerTest {

    private static class TestCorePlayer extends CorePlayer {
        private final boolean useMutex;

        TestCorePlayer(UUID uuid, boolean useMutex) {
            super(uuid, null);
            this.useMutex = useMutex;
        }

        TestCorePlayer(UUID uuid) {
            this(uuid, false);
        }

        @Override
        public boolean useMutex() {
            return useMutex;
        }
    }

    private static class TestAfkPluginHook extends PluginHook<CorePlugin> implements AfkPluginHook {
        private final boolean afk;

        TestAfkPluginHook(boolean afk) {
            super(null);
            this.afk = afk;
        }

        @Override
        public boolean isAfk(CorePlayer corePlayer) {
            return afk;
        }
    }

    private UUID testUUID;
    private TestCorePlayer player;

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();
        testUUID = UUID.randomUUID();
        player = new TestCorePlayer(testUUID);
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
    }

    // --- Constructor and getters ---

    @Test
    @DisplayName("Given a UUID, when creating CorePlayer, then getUUID returns the same UUID")
    void getUUID_returnsSameUUID_afterConstruction() {
        assertEquals(testUUID, player.getUUID());
    }

    @Test
    @DisplayName("Given a CorePlayer, when calling getPlugin, then returns the plugin passed to constructor")
    void getPlugin_returnsPluginPassedToConstructor() {
        assertNull(player.getPlugin());
    }

    @Test
    @DisplayName("Given a CorePlayer, when calling getStatisticData, then returns non-null with matching UUID")
    void getStatisticData_returnsNonNullWithMatchingUUID() {
        assertNotNull(player.getStatisticData());
        assertEquals(testUUID, player.getStatisticData().getUUID());
    }

    @Test
    @DisplayName("Given a new CorePlayer, when calling getPlayerSettings, then returns empty set")
    void getPlayerSettings_returnsEmptySet_whenNoSettingsSet() {
        assertTrue(player.getPlayerSettings().isEmpty());
    }

    @Test
    @DisplayName("Given a new CorePlayer with no setting for a key, when calling getPlayerSetting, then returns empty Optional")
    void getPlayerSetting_returnsEmpty_whenKeyNotPresent() {
        var key = new org.bukkit.NamespacedKey("test", "nonexistent");
        assertTrue(player.getPlayerSetting(key).isEmpty());
    }

    // --- useMutex ---

    @Test
    @DisplayName("Given a CorePlayer with useMutex=false, when calling useMutex, then returns false")
    void useMutex_returnsFalse_whenConfiguredFalse() {
        assertFalse(player.useMutex());
    }

    @Test
    @DisplayName("Given a CorePlayer with useMutex=true, when calling useMutex, then returns true")
    void useMutex_returnsTrue_whenConfiguredTrue() {
        TestCorePlayer mutexPlayer = new TestCorePlayer(testUUID, true);
        assertTrue(mutexPlayer.useMutex());
    }

    // --- isAfk ---

    @Test
    @DisplayName("Given no AfkPluginHook registered, when calling isAfk, then returns false")
    void isAfk_returnsFalse_whenNoHooksRegistered() {
        assertFalse(player.isAfk());
    }

    @Test
    @DisplayName("Given an AfkPluginHook that returns true, when calling isAfk, then returns true")
    void isAfk_returnsTrue_whenRegisteredHookReturnsTrue() {
        RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(new TestAfkPluginHook(true));
        assertTrue(player.isAfk());
    }

    @Test
    @DisplayName("Given an AfkPluginHook that returns false, when calling isAfk, then returns false")
    void isAfk_returnsFalse_whenRegisteredHookReturnsFalse() {
        RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(new TestAfkPluginHook(false));
        assertFalse(player.isAfk());
    }

    // --- equals ---

    @Test
    @DisplayName("Given two CorePlayers with same UUID, when comparing, then they are equal")
    void equals_returnsTrue_whenSameUUID() {
        TestCorePlayer other = new TestCorePlayer(testUUID);
        assertEquals(player, other);
    }

    @Test
    @DisplayName("Given two CorePlayers with different UUIDs, when comparing, then they are not equal")
    void equals_returnsFalse_whenDifferentUUID() {
        TestCorePlayer other = new TestCorePlayer(UUID.randomUUID());
        assertNotEquals(player, other);
    }

    @Test
    @DisplayName("Given a CorePlayer, when compared to itself, then returns equal")
    void equals_returnsTrue_whenComparedToSelf() {
        assertEquals(player, player);
    }

    @Test
    @DisplayName("Given a CorePlayer, when compared to a non-CorePlayer object, then returns not equal")
    void equals_returnsFalse_whenComparedToNonCorePlayer() {
        assertNotEquals("not a player", player);
    }

    @Test
    @DisplayName("Given a CorePlayer, when compared to null, then returns not equal")
    void equals_returnsFalse_whenComparedToNull() {
        assertNotEquals(null, player);
    }

    // --- hashCode ---

    @Test
    @DisplayName("Given two CorePlayers with same UUID, when computing hashCode, then they are equal")
    void hashCode_isEqual_whenSameUUID() {
        TestCorePlayer other = new TestCorePlayer(testUUID);
        assertEquals(player.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("Given a CorePlayer, when calling hashCode twice, then returns consistent value")
    void hashCode_isConsistent_acrossMultipleCalls() {
        int first = player.hashCode();
        int second = player.hashCode();
        assertEquals(first, second);
    }

    // --- toString ---

    @Test
    @DisplayName("Given a CorePlayer, when calling toString, then includes UUID")
    void toString_containsUUID() {
        String result = player.toString();
        assertTrue(result.contains(testUUID.toString()));
    }

    @Test
    @DisplayName("Given a CorePlayer, when calling toString, then matches expected format")
    void toString_matchesExpectedFormat() {
        String expected = "CorePlayer - [uuid=" + testUUID + "]";
        assertEquals(expected, player.toString());
    }

    // --- setPlayerSetting, getPlayerSettings, getAsBukkitPlayer ---

    @Nested
    @DisplayName("Bukkit-dependent methods")
    class BukkitDependentMethods {

        private PluginManager mockPluginManager;

        @BeforeEach
        void setUpBukkitServer() throws Exception {
            Server mockServer = mock(Server.class);
            mockPluginManager = mock(PluginManager.class);
            when(mockServer.getPluginManager()).thenReturn(mockPluginManager);
            when(mockServer.getLogger()).thenReturn(Logger.getLogger("TestServer"));
            when(mockServer.isPrimaryThread()).thenReturn(true);
            when(mockServer.getPlayer(testUUID)).thenReturn(null);
            doNothing().when(mockPluginManager).callEvent(any());

            Field serverField = Bukkit.class.getDeclaredField("server");
            serverField.setAccessible(true);
            serverField.set(null, mockServer);
        }

        @AfterEach
        void tearDownBukkitServer() throws Exception {
            Field serverField = Bukkit.class.getDeclaredField("server");
            serverField.setAccessible(true);
            serverField.set(null, null);
        }

        @Test
        @DisplayName("Given a PlayerSetting, when setPlayerSetting, then getPlayerSetting returns the setting")
        void setPlayerSetting_storesSetting_whenCalled() {
            NamespacedKey settingKey = new NamespacedKey("test", "my_setting");
            PlayerSetting setting = createTestSetting(settingKey, "VALUE_A");

            player.setPlayerSetting(setting);

            Optional<? extends PlayerSetting> result = player.getPlayerSetting(settingKey);
            assertTrue(result.isPresent());
            assertEquals(setting, result.get());
        }

        @Test
        @DisplayName("Given a PlayerSetting, when setPlayerSetting, then fires PlayerSettingChangeEvent with null old setting and correct player")
        void setPlayerSetting_firesEventWithNullOldSetting_whenNoExistingSetting() {
            NamespacedKey settingKey = new NamespacedKey("test", "my_setting");
            PlayerSetting setting = createTestSetting(settingKey, "VALUE_A");

            player.setPlayerSetting(setting);

            ArgumentCaptor<PlayerSettingChangeEvent> captor = ArgumentCaptor.forClass(PlayerSettingChangeEvent.class);
            verify(mockPluginManager).callEvent(captor.capture());
            PlayerSettingChangeEvent event = captor.getValue();
            assertFalse(event.getOldSetting().isPresent());
            assertEquals(setting, event.getNewSetting());
            assertEquals(player, event.getCorePlayer());
        }

        @Test
        @DisplayName("Given an existing PlayerSetting, when setPlayerSetting with same key, then fires event with old setting and correct player")
        void setPlayerSetting_firesEventWithOldSetting_whenReplacingExistingSetting() {
            NamespacedKey settingKey = new NamespacedKey("test", "my_setting");
            PlayerSetting oldSetting = createTestSetting(settingKey, "VALUE_A");
            PlayerSetting newSetting = createTestSetting(settingKey, "VALUE_B");

            player.setPlayerSetting(oldSetting);
            player.setPlayerSetting(newSetting);

            ArgumentCaptor<PlayerSettingChangeEvent> captor = ArgumentCaptor.forClass(PlayerSettingChangeEvent.class);
            verify(mockPluginManager, times(2)).callEvent(captor.capture());
            PlayerSettingChangeEvent secondEvent = captor.getAllValues().get(1);
            assertTrue(secondEvent.getOldSetting().isPresent());
            assertEquals(oldSetting, secondEvent.getOldSetting().get());
            assertEquals(newSetting, secondEvent.getNewSetting());
            assertEquals(player, secondEvent.getCorePlayer());
        }

        @Test
        @DisplayName("Given multiple PlayerSettings set, when getPlayerSettings, then returns all settings")
        void getPlayerSettings_returnsAllSettings_whenMultipleSettingsSet() {
            NamespacedKey key1 = new NamespacedKey("test", "setting_one");
            NamespacedKey key2 = new NamespacedKey("test", "setting_two");
            PlayerSetting setting1 = createTestSetting(key1, "VALUE_1");
            PlayerSetting setting2 = createTestSetting(key2, "VALUE_2");

            player.setPlayerSetting(setting1);
            player.setPlayerSetting(setting2);

            assertEquals(2, player.getPlayerSettings().size());
            assertTrue(player.getPlayerSettings().contains(setting1));
            assertTrue(player.getPlayerSettings().contains(setting2));
        }

        @Test
        @DisplayName("Given player is online, when getAsBukkitPlayer, then returns present Optional with the Player")
        void getAsBukkitPlayer_returnsPlayer_whenOnline() throws Exception {
            Player mockBukkitPlayer = mock(Player.class);
            Server server = Bukkit.getServer();
            when(server.getPlayer(testUUID)).thenReturn(mockBukkitPlayer);

            Optional<Player> result = player.getAsBukkitPlayer();
            assertTrue(result.isPresent());
            assertEquals(mockBukkitPlayer, result.get());
        }

        @Test
        @DisplayName("Given player is offline, when getAsBukkitPlayer, then returns empty Optional")
        void getAsBukkitPlayer_returnsEmpty_whenOffline() {
            Optional<Player> result = player.getAsBukkitPlayer();
            assertFalse(result.isPresent());
        }
    }

    private static PlayerSetting createTestSetting(NamespacedKey key, String name) {
        return new PlayerSetting() {
            @Override
            @NotNull
            public NamespacedKey getSettingKey() {
                return key;
            }

            @Override
            @NotNull
            public LinkedNode<? extends PlayerSetting> getFirstSetting() {
                return new LinkedNode<>(this);
            }

            @Override
            @NotNull
            public LinkedNode<? extends PlayerSetting> getNextSetting() {
                return new LinkedNode<>(this);
            }

            @Override
            public void onSettingChange(@NotNull CorePlayer player, @NotNull Optional<PlayerSetting> oldSetting) {
            }

            @Override
            @NotNull
            public Optional<? extends PlayerSetting> fromString(@NotNull String setting) {
                return Optional.empty();
            }

            @Override
            @NotNull
            public String name() {
                return name;
            }
        };
    }
}

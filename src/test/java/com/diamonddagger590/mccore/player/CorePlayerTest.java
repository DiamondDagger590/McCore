package com.diamonddagger590.mccore.player;

import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    @DisplayName("Given a CorePlayer, when calling getStatisticData, then returns non-null")
    void getStatisticData_returnsNonNull() {
        assertNotNull(player.getStatisticData());
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
}

package com.diamonddagger590.mccore.player;

import com.diamonddagger590.mccore.CorePlugin;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerManagerTest {

    private static class TestCorePlayer extends CorePlayer {
        private final boolean locked;

        TestCorePlayer(UUID uuid, boolean locked) {
            super(uuid, mock(CorePlugin.class));
            this.locked = locked;
        }

        TestCorePlayer(UUID uuid) {
            this(uuid, false);
        }

        @Override
        public boolean useMutex() {
            return false;
        }

        @Override
        public boolean isLocked() {
            return locked;
        }
    }

    private PlayerManager<CorePlugin, TestCorePlayer> manager;

    @BeforeEach
    void setUp() {
        manager = new PlayerManager<>(mock(CorePlugin.class));
    }

    @Test
    @DisplayName("Given a player, when adding, then hasCorePlayer returns true")
    void addPlayer_thenHasCorePlayerReturnsTrue() {
        UUID uuid = UUID.randomUUID();
        TestCorePlayer player = new TestCorePlayer(uuid);
        manager.addPlayer(player);
        assertTrue(manager.hasCorePlayer(uuid));
    }

    @Test
    @DisplayName("Given no players, when checking hasCorePlayer, then returns false")
    void hasCorePlayer_returnsFalse_whenEmpty() {
        assertFalse(manager.hasCorePlayer(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Given a stored player, when getting by UUID, then returns the player")
    void getPlayer_returnsPlayer_whenStored() {
        UUID uuid = UUID.randomUUID();
        TestCorePlayer player = new TestCorePlayer(uuid);
        manager.addPlayer(player);
        Optional<TestCorePlayer> result = manager.getPlayer(uuid);
        assertTrue(result.isPresent());
        assertEquals(player, result.get());
    }

    @Test
    @DisplayName("Given no players, when getting by UUID, then returns empty")
    void getPlayer_returnsEmpty_whenNotStored() {
        assertTrue(manager.getPlayer(UUID.randomUUID()).isEmpty());
    }

    @Test
    @DisplayName("Given a stored player, when removing by UUID, then returns the player and removes it")
    void removePlayer_returnsAndRemovesPlayer() {
        UUID uuid = UUID.randomUUID();
        TestCorePlayer player = new TestCorePlayer(uuid);
        manager.addPlayer(player);

        Optional<TestCorePlayer> removed = manager.removePlayer(uuid);
        assertTrue(removed.isPresent());
        assertEquals(player, removed.get());
        assertFalse(manager.hasCorePlayer(uuid));
    }

    @Test
    @DisplayName("Given no players, when removing by UUID, then returns empty")
    void removePlayer_returnsEmpty_whenNotStored() {
        assertTrue(manager.removePlayer(UUID.randomUUID()).isEmpty());
    }

    @Test
    @DisplayName("Given a locked player, when checking isPlayerLocked, then returns true")
    void isPlayerLocked_returnsTrue_whenLocked() {
        UUID uuid = UUID.randomUUID();
        manager.addPlayer(new TestCorePlayer(uuid, true));
        assertTrue(manager.isPlayerLocked(uuid));
    }

    @Test
    @DisplayName("Given an unlocked player, when checking isPlayerLocked, then returns false")
    void isPlayerLocked_returnsFalse_whenUnlocked() {
        UUID uuid = UUID.randomUUID();
        manager.addPlayer(new TestCorePlayer(uuid, false));
        assertFalse(manager.isPlayerLocked(uuid));
    }

    @Test
    @DisplayName("Given no players, when checking isPlayerLocked, then returns false")
    void isPlayerLocked_returnsFalse_whenNotStored() {
        assertFalse(manager.isPlayerLocked(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Given multiple players, when getAllPlayers, then returns all of them")
    void getAllPlayers_returnsAllStoredPlayers() {
        TestCorePlayer p1 = new TestCorePlayer(UUID.randomUUID());
        TestCorePlayer p2 = new TestCorePlayer(UUID.randomUUID());
        manager.addPlayer(p1);
        manager.addPlayer(p2);

        Set<TestCorePlayer> all = manager.getAllPlayers();
        assertEquals(2, all.size());
        assertTrue(all.contains(p1));
        assertTrue(all.contains(p2));
    }

    @Test
    @DisplayName("Given no players, when getAllPlayers, then returns empty set")
    void getAllPlayers_returnsEmptySet_whenEmpty() {
        assertTrue(manager.getAllPlayers().isEmpty());
    }

    @Test
    @DisplayName("Given players with Bukkit player available, when getAllBukkitPlayers, then returns bukkit players")
    void getAllBukkitPlayers_returnsBukkitPlayers_whenPresent() {
        UUID uuid = UUID.randomUUID();
        Player bukkitPlayer = mock(Player.class);

        TestCorePlayer corePlayer = mock(TestCorePlayer.class);
        when(corePlayer.getAsBukkitPlayer()).thenReturn(Optional.of(bukkitPlayer));
        when(corePlayer.getUUID()).thenReturn(uuid);

        manager.addPlayer(corePlayer);

        Set<Player> result = manager.getAllBukkitPlayers();
        assertEquals(1, result.size());
        assertTrue(result.contains(bukkitPlayer));
    }

    @Test
    @DisplayName("Given players without Bukkit player, when getAllBukkitPlayers, then returns empty set")
    void getAllBukkitPlayers_returnsEmpty_whenNoBukkitPlayers() {
        UUID uuid = UUID.randomUUID();

        TestCorePlayer corePlayer = mock(TestCorePlayer.class);
        when(corePlayer.getAsBukkitPlayer()).thenReturn(Optional.empty());
        when(corePlayer.getUUID()).thenReturn(uuid);

        manager.addPlayer(corePlayer);

        assertTrue(manager.getAllBukkitPlayers().isEmpty());
    }

    @Test
    @DisplayName("Given getAllPlayers result, when modifying it, then original manager is not affected")
    void getAllPlayers_returnsDefensiveCopy() {
        TestCorePlayer player = new TestCorePlayer(UUID.randomUUID());
        manager.addPlayer(player);

        Set<TestCorePlayer> copy = manager.getAllPlayers();
        copy.clear();

        assertEquals(1, manager.getAllPlayers().size());
    }

    @Test
    @DisplayName("Given an existing player, when adding another with the same UUID, then replaces the original")
    void addPlayer_replacesExisting_whenSameUUID() {
        UUID uuid = UUID.randomUUID();
        TestCorePlayer first = new TestCorePlayer(uuid, false);
        TestCorePlayer second = new TestCorePlayer(uuid, true);

        manager.addPlayer(first);
        manager.addPlayer(second);

        assertEquals(second, manager.getPlayer(uuid).orElseThrow());
        assertTrue(manager.isPlayerLocked(uuid));
    }
}

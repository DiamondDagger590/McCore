package com.diamonddagger590.mccore.external.cmi;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CoreCMIHookTest {

    @Mock
    private CorePlugin mockPlugin;

    private CoreCMIHook createHook() {
        return new CoreCMIHook(mockPlugin);
    }

    private CorePlayer createMockCorePlayer(Player bukkitPlayer) {
        UUID uuid = UUID.randomUUID();
        return new CorePlayer(uuid, mockPlugin) {
            @Override
            public boolean useMutex() {
                return false;
            }

            @Override
            public Optional<Player> getAsBukkitPlayer() {
                return Optional.ofNullable(bukkitPlayer);
            }
        };
    }

    @Nested
    @DisplayName("isAfk")
    class IsAfk {

        @Test
        @DisplayName("Given player is offline, when checking isAfk, then returns false")
        void isAfk_returnsFalse_whenPlayerIsOffline() {
            CorePlayer corePlayer = createMockCorePlayer(null);
            CoreCMIHook hook = createHook();
            assertFalse(hook.isAfk(corePlayer));
        }
    }

    @Nested
    @DisplayName("isEntityNpc")
    class IsEntityNpc {

        @Test
        @DisplayName("Given entity is not a Player, when checking isEntityNpc, then returns false")
        void isEntityNpc_returnsFalse_whenEntityIsNotPlayer() {
            Entity mockEntity = mock(Entity.class);
            CoreCMIHook hook = createHook();
            assertFalse(hook.isEntityNpc(mockEntity));
        }
    }
}

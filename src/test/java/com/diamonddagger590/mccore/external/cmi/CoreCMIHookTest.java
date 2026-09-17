package com.diamonddagger590.mccore.external.cmi;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.Zrips.CMI.PlayerManager;
import com.Zrips.CMI.utils.Util;
import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreCMIHookTest {

    @Mock
    private CorePlugin plugin;

    private CoreCMIHook hook;

    @BeforeEach
    void setUp() {
        hook = new CoreCMIHook(plugin);
    }

    @Nested
    @DisplayName("isAfk")
    class IsAfkTests {

        @Mock
        private CorePlayer corePlayer;

        @Test
        @DisplayName("Returns true when player is online and CMI reports AFK")
        void isAfk_returnsTrue_whenPlayerOnlineAndAfk() {
            Player player = mock(Player.class);
            when(corePlayer.getAsBukkitPlayer()).thenReturn(Optional.of(player));

            try (MockedStatic<CMI> mocked = mockStatic(CMI.class)) {
                CMI cmi = mock(CMI.class);
                mocked.when(CMI::getInstance).thenReturn(cmi);
                PlayerManager playerManager = mock(PlayerManager.class);
                when(cmi.getPlayerManager()).thenReturn(playerManager);
                CMIUser cmiUser = mock(CMIUser.class);
                when(playerManager.getUser(player)).thenReturn(cmiUser);
                when(cmiUser.isAfk()).thenReturn(true);

                assertTrue(hook.isAfk(corePlayer));
            }
        }

        @Test
        @DisplayName("Returns false when player is online and CMI reports not AFK")
        void isAfk_returnsFalse_whenPlayerOnlineAndNotAfk() {
            Player player = mock(Player.class);
            when(corePlayer.getAsBukkitPlayer()).thenReturn(Optional.of(player));

            try (MockedStatic<CMI> mocked = mockStatic(CMI.class)) {
                CMI cmi = mock(CMI.class);
                mocked.when(CMI::getInstance).thenReturn(cmi);
                PlayerManager playerManager = mock(PlayerManager.class);
                when(cmi.getPlayerManager()).thenReturn(playerManager);
                CMIUser cmiUser = mock(CMIUser.class);
                when(playerManager.getUser(player)).thenReturn(cmiUser);
                when(cmiUser.isAfk()).thenReturn(false);

                assertFalse(hook.isAfk(corePlayer));
            }
        }

        @Test
        @DisplayName("Returns false when CMIUser is null")
        void isAfk_returnsFalse_whenCmiUserIsNull() {
            Player player = mock(Player.class);
            when(corePlayer.getAsBukkitPlayer()).thenReturn(Optional.of(player));

            try (MockedStatic<CMI> mocked = mockStatic(CMI.class)) {
                CMI cmi = mock(CMI.class);
                mocked.when(CMI::getInstance).thenReturn(cmi);
                PlayerManager playerManager = mock(PlayerManager.class);
                when(cmi.getPlayerManager()).thenReturn(playerManager);
                when(playerManager.getUser(player)).thenReturn(null);

                assertFalse(hook.isAfk(corePlayer));
            }
        }

        @Test
        @DisplayName("Returns false when player is offline")
        void isAfk_returnsFalse_whenPlayerOffline() {
            when(corePlayer.getAsBukkitPlayer()).thenReturn(Optional.empty());

            assertFalse(hook.isAfk(corePlayer));
        }
    }

    @Nested
    @DisplayName("isEntityNpc")
    class IsEntityNpcTests {

        @Test
        @DisplayName("Returns true when entity is a Player and CMI reports NPC")
        void isEntityNpc_returnsTrue_whenPlayerIsNpc() {
            Player player = mock(Player.class);

            try (MockedStatic<CMI> mocked = mockStatic(CMI.class)) {
                CMI cmi = mock(CMI.class);
                mocked.when(CMI::getInstance).thenReturn(cmi);
                Util utilManager = mock(Util.class);
                when(cmi.getUtilManager()).thenReturn(utilManager);
                when(utilManager.isNPC(player)).thenReturn(true);

                assertTrue(hook.isEntityNpc(player));
            }
        }

        @Test
        @DisplayName("Returns false when entity is a Player but CMI says not NPC")
        void isEntityNpc_returnsFalse_whenPlayerIsNotNpc() {
            Player player = mock(Player.class);

            try (MockedStatic<CMI> mocked = mockStatic(CMI.class)) {
                CMI cmi = mock(CMI.class);
                mocked.when(CMI::getInstance).thenReturn(cmi);
                Util utilManager = mock(Util.class);
                when(cmi.getUtilManager()).thenReturn(utilManager);
                when(utilManager.isNPC(player)).thenReturn(false);

                assertFalse(hook.isEntityNpc(player));
            }
        }

        @Test
        @DisplayName("Returns false when entity is not a Player")
        void isEntityNpc_returnsFalse_whenEntityIsNotPlayer() {
            Entity entity = mock(Entity.class);

            assertFalse(hook.isEntityNpc(entity));
        }
    }
}

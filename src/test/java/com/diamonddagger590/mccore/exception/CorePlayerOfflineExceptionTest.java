package com.diamonddagger590.mccore.exception;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorePlayerOfflineExceptionTest {

    private static class TestCorePlayer extends CorePlayer {
        TestCorePlayer(UUID uuid) {
            super(uuid, null);
        }

        @Override
        public boolean useMutex() {
            return false;
        }
    }

    @Test
    @DisplayName("Given a CorePlayer, when creating with single-arg constructor, then auto-generates message containing UUID")
    void constructor_generatesMessage_containingUUID() {
        UUID uuid = UUID.randomUUID();
        TestCorePlayer player = new TestCorePlayer(uuid);
        CorePlayerOfflineException exception = new CorePlayerOfflineException(player);
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains(uuid.toString()));
    }

    @Test
    @DisplayName("Given a CorePlayer, when creating with single-arg constructor, then getMessage indicates player is not online")
    void constructor_messageIndicatesPlayerNotOnline() {
        UUID uuid = UUID.randomUUID();
        TestCorePlayer player = new TestCorePlayer(uuid);
        CorePlayerOfflineException exception = new CorePlayerOfflineException(player);
        assertTrue(exception.getMessage().contains("not online"));
    }

    @Test
    @DisplayName("Given a CorePlayer and custom message, when creating with two-arg constructor, then uses custom message")
    void constructor_usesCustomMessage_whenProvided() {
        UUID uuid = UUID.randomUUID();
        TestCorePlayer player = new TestCorePlayer(uuid);
        String customMessage = "Custom error message";
        CorePlayerOfflineException exception = new CorePlayerOfflineException(player, customMessage);
        assertEquals(customMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Given a CorePlayerOfflineException, when calling getCorePlayer, then returns the same player")
    void getCorePlayer_returnsSameInstance() {
        UUID uuid = UUID.randomUUID();
        TestCorePlayer player = new TestCorePlayer(uuid);
        CorePlayerOfflineException exception = new CorePlayerOfflineException(player);
        assertSame(player, exception.getCorePlayer());
    }

    @Test
    @DisplayName("Given a CorePlayerOfflineException, then it extends RuntimeException")
    void exception_extendsRuntimeException() {
        UUID uuid = UUID.randomUUID();
        TestCorePlayer player = new TestCorePlayer(uuid);
        CorePlayerOfflineException exception = new CorePlayerOfflineException(player);
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Given two-arg constructor, when calling getCorePlayer, then returns the same player")
    void getCorePlayer_returnsSameInstance_twoArgConstructor() {
        UUID uuid = UUID.randomUUID();
        TestCorePlayer player = new TestCorePlayer(uuid);
        CorePlayerOfflineException exception = new CorePlayerOfflineException(player, "some message");
        assertSame(player, exception.getCorePlayer());
    }
}

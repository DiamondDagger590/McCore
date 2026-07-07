package com.diamonddagger590.mccore.event.player;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;

class CorePlayerEventTest {

    private static class TestCorePlayer extends CorePlayer {
        TestCorePlayer() {
            super(UUID.randomUUID(), null);
        }

        @Override
        public boolean useMutex() {
            return false;
        }
    }

    private static class TestCorePlayerEvent extends CorePlayerEvent {
        private static final HandlerList handlers = new HandlerList();

        TestCorePlayerEvent(@NotNull CorePlayer corePlayer) {
            super(corePlayer);
        }

        @Override
        @NotNull
        public HandlerList getHandlers() {
            return handlers;
        }

        @NotNull
        public static HandlerList getHandlerList() {
            return handlers;
        }
    }

    @Test
    @DisplayName("Given a CorePlayer, when constructing a CorePlayerEvent, then getCorePlayer() returns the same instance")
    void getCorePlayer_returnsSameInstance_whenConstructedWithPlayer() {
        TestCorePlayer player = new TestCorePlayer();
        TestCorePlayerEvent event = new TestCorePlayerEvent(player);
        assertSame(player, event.getCorePlayer());
    }

    @Test
    @DisplayName("Given two events with different players, when getCorePlayer is called, then each returns its own player")
    void getCorePlayer_returnsCorrectPlayer_forEachEvent() {
        TestCorePlayer playerA = new TestCorePlayer();
        TestCorePlayer playerB = new TestCorePlayer();
        TestCorePlayerEvent eventA = new TestCorePlayerEvent(playerA);
        TestCorePlayerEvent eventB = new TestCorePlayerEvent(playerB);
        assertSame(playerA, eventA.getCorePlayer());
        assertSame(playerB, eventB.getCorePlayer());
    }
}

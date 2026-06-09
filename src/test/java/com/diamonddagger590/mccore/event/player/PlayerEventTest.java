package com.diamonddagger590.mccore.event.player;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.util.TimeProvider;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class PlayerEventTest {

    private static class TestCorePlayer extends CorePlayer {
        TestCorePlayer(UUID uuid) {
            super(uuid, null);
        }

        @Override
        public boolean useMutex() {
            return false;
        }
    }

    private final UUID testUUID = UUID.randomUUID();
    private final TestCorePlayer testPlayer = new TestCorePlayer(testUUID);

    // --- PlayerLoadEvent ---

    @Test
    @DisplayName("Given a CorePlayer, when creating PlayerLoadEvent, then getCorePlayer returns the same player")
    void playerLoadEvent_getCorePlayer_returnsSamePlayer() {
        PlayerLoadEvent event = new PlayerLoadEvent(testPlayer);
        assertSame(testPlayer, event.getCorePlayer());
    }

    @Test
    @DisplayName("Given a PlayerLoadEvent, when calling getHandlers, then returns non-null HandlerList")
    void playerLoadEvent_getHandlers_returnsNonNull() {
        PlayerLoadEvent event = new PlayerLoadEvent(testPlayer);
        assertNotNull(event.getHandlers());
    }

    @Test
    @DisplayName("Given a PlayerLoadEvent, when calling getHandlerList, then returns non-null HandlerList")
    void playerLoadEvent_getHandlerList_returnsNonNull() {
        assertNotNull(PlayerLoadEvent.getHandlerList());
    }

    @Test
    @DisplayName("Given a PlayerLoadEvent, when calling getHandlers and getHandlerList, then they return the same instance")
    void playerLoadEvent_handlersAndHandlerList_areSameInstance() {
        PlayerLoadEvent event = new PlayerLoadEvent(testPlayer);
        assertSame(event.getHandlers(), PlayerLoadEvent.getHandlerList());
    }

    // --- PlayerUnloadEvent ---

    @Test
    @DisplayName("Given a CorePlayer, when creating PlayerUnloadEvent, then getCorePlayer returns the same player")
    void playerUnloadEvent_getCorePlayer_returnsSamePlayer() {
        PlayerUnloadEvent event = new PlayerUnloadEvent(testPlayer);
        assertSame(testPlayer, event.getCorePlayer());
    }

    @Test
    @DisplayName("Given a PlayerUnloadEvent, when calling getHandlers, then returns non-null HandlerList")
    void playerUnloadEvent_getHandlers_returnsNonNull() {
        PlayerUnloadEvent event = new PlayerUnloadEvent(testPlayer);
        assertNotNull(event.getHandlers());
    }

    @Test
    @DisplayName("Given a PlayerUnloadEvent, when calling getHandlerList, then returns non-null HandlerList")
    void playerUnloadEvent_getHandlerList_returnsNonNull() {
        assertNotNull(PlayerUnloadEvent.getHandlerList());
    }

    @Test
    @DisplayName("Given a PlayerUnloadEvent, when calling getHandlers and getHandlerList, then they return the same instance")
    void playerUnloadEvent_handlersAndHandlerList_areSameInstance() {
        PlayerUnloadEvent event = new PlayerUnloadEvent(testPlayer);
        assertSame(event.getHandlers(), PlayerUnloadEvent.getHandlerList());
    }

    // --- Handler list isolation ---

    @Test
    @DisplayName("Given PlayerLoadEvent and PlayerUnloadEvent, when getting handler lists, then they are different instances")
    void handlerLists_areDifferentInstances_betweenLoadAndUnloadEvents() {
        assertNotSame(PlayerLoadEvent.getHandlerList(), PlayerUnloadEvent.getHandlerList());
    }

    // --- CorePlayerEvent (tested transitively) ---

    @Test
    @DisplayName("Given a CorePlayer with a specific UUID, when creating a load event, then getCorePlayer preserves the UUID")
    void corePlayerEvent_getCorePlayer_preservesUUID() {
        PlayerLoadEvent event = new PlayerLoadEvent(testPlayer);
        assertEquals(testUUID, event.getCorePlayer().getUUID());
    }
}

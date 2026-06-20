package com.diamonddagger590.mccore.event.player;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class PlayerUnloadEventTest {

    private static CorePlayer createStubPlayer(UUID uuid) {
        return new CorePlayer(uuid, null) {
            @Override
            public boolean useMutex() {
                return false;
            }
        };
    }

    @Test
    @DisplayName("Given a CorePlayer, when constructing PlayerUnloadEvent, then getCorePlayer returns the same player")
    void getCorePlayer_returnsSameInstance_whenConstructed() {
        UUID uuid = UUID.randomUUID();
        CorePlayer player = createStubPlayer(uuid);

        PlayerUnloadEvent event = new PlayerUnloadEvent(player);

        assertSame(player, event.getCorePlayer());
    }

    @Test
    @DisplayName("Given a PlayerUnloadEvent, when calling getCorePlayer, then the UUID matches")
    void getCorePlayer_hasCorrectUUID() {
        UUID uuid = UUID.randomUUID();
        CorePlayer player = createStubPlayer(uuid);

        PlayerUnloadEvent event = new PlayerUnloadEvent(player);

        assertEquals(uuid, event.getCorePlayer().getUUID());
    }

    @Test
    @DisplayName("Given a PlayerUnloadEvent, when calling getHandlers, then returns non-null HandlerList")
    void getHandlers_returnsNonNull() {
        CorePlayer player = createStubPlayer(UUID.randomUUID());
        PlayerUnloadEvent event = new PlayerUnloadEvent(player);

        assertNotNull(event.getHandlers());
    }

    @Test
    @DisplayName("Given the PlayerUnloadEvent class, when calling getHandlerList, then returns non-null HandlerList")
    void getHandlerList_returnsNonNull() {
        assertNotNull(PlayerUnloadEvent.getHandlerList());
    }

    @Test
    @DisplayName("Given a PlayerUnloadEvent, when calling getHandlers and getHandlerList, then they return the same instance")
    void getHandlers_andGetHandlerList_returnSameInstance() {
        CorePlayer player = createStubPlayer(UUID.randomUUID());
        PlayerUnloadEvent event = new PlayerUnloadEvent(player);

        assertSame(event.getHandlers(), PlayerUnloadEvent.getHandlerList());
    }

    @Test
    @DisplayName("Given two PlayerUnloadEvents with different players, when comparing, then each has its own player")
    void events_haveDifferentPlayers_whenCreatedWithDifferentPlayers() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        CorePlayer player1 = createStubPlayer(uuid1);
        CorePlayer player2 = createStubPlayer(uuid2);

        PlayerUnloadEvent event1 = new PlayerUnloadEvent(player1);
        PlayerUnloadEvent event2 = new PlayerUnloadEvent(player2);

        assertSame(player1, event1.getCorePlayer());
        assertSame(player2, event2.getCorePlayer());
        assertEquals(uuid1, event1.getCorePlayer().getUUID());
        assertEquals(uuid2, event2.getCorePlayer().getUUID());
    }

    @Test
    @DisplayName("Given a PlayerUnloadEvent, when calling getEventName, then returns a non-null string")
    void getEventName_returnsNonNull() {
        CorePlayer player = createStubPlayer(UUID.randomUUID());
        PlayerUnloadEvent event = new PlayerUnloadEvent(player);

        assertNotNull(event.getEventName());
    }
}

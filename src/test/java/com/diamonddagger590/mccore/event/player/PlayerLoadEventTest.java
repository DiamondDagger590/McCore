package com.diamonddagger590.mccore.event.player;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class PlayerLoadEventTest {

    private static CorePlayer createStubPlayer(UUID uuid) {
        return new CorePlayer(uuid, null) {
            @Override
            public boolean useMutex() {
                return false;
            }
        };
    }

    @Test
    @DisplayName("Given a CorePlayer, when constructing PlayerLoadEvent, then getCorePlayer returns the same player")
    void getCorePlayer_returnsSameInstance_whenConstructed() {
        UUID uuid = UUID.randomUUID();
        CorePlayer player = createStubPlayer(uuid);

        PlayerLoadEvent event = new PlayerLoadEvent(player);

        assertSame(player, event.getCorePlayer());
    }

    @Test
    @DisplayName("Given a PlayerLoadEvent, when calling getCorePlayer, then the UUID matches")
    void getCorePlayer_hasCorrectUUID() {
        UUID uuid = UUID.randomUUID();
        CorePlayer player = createStubPlayer(uuid);

        PlayerLoadEvent event = new PlayerLoadEvent(player);

        assertEquals(uuid, event.getCorePlayer().getUUID());
    }

    @Test
    @DisplayName("Given a PlayerLoadEvent, when calling getHandlers, then returns non-null HandlerList")
    void getHandlers_returnsNonNull() {
        CorePlayer player = createStubPlayer(UUID.randomUUID());
        PlayerLoadEvent event = new PlayerLoadEvent(player);

        assertNotNull(event.getHandlers());
    }

    @Test
    @DisplayName("Given the PlayerLoadEvent class, when calling getHandlerList, then returns non-null HandlerList")
    void getHandlerList_returnsNonNull() {
        assertNotNull(PlayerLoadEvent.getHandlerList());
    }

    @Test
    @DisplayName("Given a PlayerLoadEvent, when calling getHandlers and getHandlerList, then they return the same instance")
    void getHandlers_andGetHandlerList_returnSameInstance() {
        CorePlayer player = createStubPlayer(UUID.randomUUID());
        PlayerLoadEvent event = new PlayerLoadEvent(player);

        assertSame(event.getHandlers(), PlayerLoadEvent.getHandlerList());
    }

    @Test
    @DisplayName("Given two PlayerLoadEvents with different players, when comparing, then each has its own player")
    void events_haveDifferentPlayers_whenCreatedWithDifferentPlayers() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        CorePlayer player1 = createStubPlayer(uuid1);
        CorePlayer player2 = createStubPlayer(uuid2);

        PlayerLoadEvent event1 = new PlayerLoadEvent(player1);
        PlayerLoadEvent event2 = new PlayerLoadEvent(player2);

        assertSame(player1, event1.getCorePlayer());
        assertSame(player2, event2.getCorePlayer());
        assertEquals(uuid1, event1.getCorePlayer().getUUID());
        assertEquals(uuid2, event2.getCorePlayer().getUUID());
    }

    @Test
    @DisplayName("Given a PlayerLoadEvent, when calling getEventName, then returns a non-null string")
    void getEventName_returnsNonNull() {
        CorePlayer player = createStubPlayer(UUID.randomUUID());
        PlayerLoadEvent event = new PlayerLoadEvent(player);

        assertNotNull(event.getEventName());
    }

    @Test
    @DisplayName("Given null player, when constructing PlayerLoadEvent, then does not throw at construction")
    void constructor_doesNotThrow_whenPlayerIsNull() {
        PlayerLoadEvent event = assertDoesNotThrow(() -> new PlayerLoadEvent(null));
        assertNull(event.getCorePlayer());
    }
}

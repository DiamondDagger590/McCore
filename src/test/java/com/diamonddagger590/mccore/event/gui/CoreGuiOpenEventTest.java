package com.diamonddagger590.mccore.event.gui;

import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreGuiOpenEventTest {

    @SuppressWarnings("deprecation")
    private static NamespacedKey key(String namespace, String key) {
        return new NamespacedKey(namespace, key);
    }

    private static final UUID PLAYER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final NamespacedKey GUI_KEY = key("test", "home_gui");

    @Test
    @DisplayName("Given a player UUID and key, when creating event, then getPlayerUUID returns the UUID")
    void getPlayerUUID_returnsCorrectUUID_whenEventIsCreated() {
        CoreGuiOpenEvent event = new CoreGuiOpenEvent(PLAYER_UUID, Optional.of(GUI_KEY));
        assertEquals(PLAYER_UUID, event.getPlayerUUID());
    }

    @Test
    @DisplayName("Given a non-null GUI key, when calling getGuiKey, then returns present Optional with correct key")
    void getGuiKey_returnsPresentOptional_whenKeyIsProvided() {
        CoreGuiOpenEvent event = new CoreGuiOpenEvent(PLAYER_UUID, Optional.of(GUI_KEY));
        assertTrue(event.getGuiKey().isPresent());
        assertEquals(GUI_KEY, event.getGuiKey().get());
    }

    @Test
    @DisplayName("Given an empty GUI key, when calling getGuiKey, then returns empty Optional")
    void getGuiKey_returnsEmptyOptional_whenKeyIsEmpty() {
        CoreGuiOpenEvent event = new CoreGuiOpenEvent(PLAYER_UUID, Optional.empty());
        assertTrue(event.getGuiKey().isEmpty());
    }

    @Test
    @DisplayName("Given a CoreGuiOpenEvent, when calling getHandlers, then returns non-null HandlerList")
    void getHandlers_returnsNonNullHandlerList_whenCalled() {
        CoreGuiOpenEvent event = new CoreGuiOpenEvent(PLAYER_UUID, Optional.empty());
        assertNotNull(event.getHandlers());
    }

    @Test
    @DisplayName("Given CoreGuiOpenEvent class, when calling getHandlerList, then returns non-null static HandlerList")
    void getHandlerList_returnsNonNullStaticHandlerList_whenCalled() {
        HandlerList handlerList = CoreGuiOpenEvent.getHandlerList();
        assertNotNull(handlerList);
    }

    @Test
    @DisplayName("Given a CoreGuiOpenEvent, when calling getHandlers and getHandlerList, then they return the same instance")
    void getHandlers_returnsSameInstanceAsStaticGetHandlerList() {
        CoreGuiOpenEvent event = new CoreGuiOpenEvent(PLAYER_UUID, Optional.empty());
        assertEquals(CoreGuiOpenEvent.getHandlerList(), event.getHandlers());
    }

    @Test
    @DisplayName("Given different player UUIDs, when creating events, then each event has its own UUID")
    void events_haveDifferentUUIDs_whenCreatedWithDifferentPlayers() {
        UUID uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID uuid2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

        CoreGuiOpenEvent event1 = new CoreGuiOpenEvent(uuid1, Optional.empty());
        CoreGuiOpenEvent event2 = new CoreGuiOpenEvent(uuid2, Optional.empty());

        assertEquals(uuid1, event1.getPlayerUUID());
        assertEquals(uuid2, event2.getPlayerUUID());
    }
}

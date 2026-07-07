package com.diamonddagger590.mccore.event.database;

import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class DatabaseEventTest {

    // --- PreTablesCreateEvent ---

    @Test
    @DisplayName("Given a PreTablesCreateEvent, when constructed, then instance is non-null")
    void preTablesCreateEvent_isNotNull_whenConstructed() {
        PreTablesCreateEvent event = new PreTablesCreateEvent();
        assertNotNull(event);
    }

    @Test
    @DisplayName("Given a PreTablesCreateEvent, when getHandlers called, then returns the static handler list")
    void preTablesCreateEvent_getHandlers_returnsSameAsStaticHandlerList() {
        PreTablesCreateEvent event = new PreTablesCreateEvent();
        HandlerList instanceList = event.getHandlers();
        HandlerList staticList = PreTablesCreateEvent.getHandlerList();
        assertSame(staticList, instanceList);
    }

    @Test
    @DisplayName("Given PreTablesCreateEvent, when getHandlerList called multiple times, then returns same instance")
    void preTablesCreateEvent_getHandlerList_returnsSameInstance() {
        HandlerList first = PreTablesCreateEvent.getHandlerList();
        HandlerList second = PreTablesCreateEvent.getHandlerList();
        assertSame(first, second);
    }

    // --- TablesCreatedEvent ---

    @Test
    @DisplayName("Given a TablesCreatedEvent, when constructed, then instance is non-null")
    void tablesCreatedEvent_isNotNull_whenConstructed() {
        TablesCreatedEvent event = new TablesCreatedEvent();
        assertNotNull(event);
    }

    @Test
    @DisplayName("Given a TablesCreatedEvent, when getHandlers called, then returns the static handler list")
    void tablesCreatedEvent_getHandlers_returnsSameAsStaticHandlerList() {
        TablesCreatedEvent event = new TablesCreatedEvent();
        HandlerList instanceList = event.getHandlers();
        HandlerList staticList = TablesCreatedEvent.getHandlerList();
        assertSame(staticList, instanceList);
    }

    @Test
    @DisplayName("Given TablesCreatedEvent, when getHandlerList called multiple times, then returns same instance")
    void tablesCreatedEvent_getHandlerList_returnsSameInstance() {
        HandlerList first = TablesCreatedEvent.getHandlerList();
        HandlerList second = TablesCreatedEvent.getHandlerList();
        assertSame(first, second);
    }

    // --- PreTablesUpdateEvent ---

    @Test
    @DisplayName("Given a PreTablesUpdateEvent, when constructed, then instance is non-null")
    void preTablesUpdateEvent_isNotNull_whenConstructed() {
        PreTablesUpdateEvent event = new PreTablesUpdateEvent();
        assertNotNull(event);
    }

    @Test
    @DisplayName("Given a PreTablesUpdateEvent, when getHandlers called, then returns the static handler list")
    void preTablesUpdateEvent_getHandlers_returnsSameAsStaticHandlerList() {
        PreTablesUpdateEvent event = new PreTablesUpdateEvent();
        HandlerList instanceList = event.getHandlers();
        HandlerList staticList = PreTablesUpdateEvent.getHandlerList();
        assertSame(staticList, instanceList);
    }

    @Test
    @DisplayName("Given PreTablesUpdateEvent, when getHandlerList called multiple times, then returns same instance")
    void preTablesUpdateEvent_getHandlerList_returnsSameInstance() {
        HandlerList first = PreTablesUpdateEvent.getHandlerList();
        HandlerList second = PreTablesUpdateEvent.getHandlerList();
        assertSame(first, second);
    }

    // --- TablesUpdatedEvent ---

    @Test
    @DisplayName("Given a TablesUpdatedEvent, when constructed, then instance is non-null")
    void tablesUpdatedEvent_isNotNull_whenConstructed() {
        TablesUpdatedEvent event = new TablesUpdatedEvent();
        assertNotNull(event);
    }

    @Test
    @DisplayName("Given a TablesUpdatedEvent, when getHandlers called, then returns the static handler list")
    void tablesUpdatedEvent_getHandlers_returnsSameAsStaticHandlerList() {
        TablesUpdatedEvent event = new TablesUpdatedEvent();
        HandlerList instanceList = event.getHandlers();
        HandlerList staticList = TablesUpdatedEvent.getHandlerList();
        assertSame(staticList, instanceList);
    }

    @Test
    @DisplayName("Given TablesUpdatedEvent, when getHandlerList called multiple times, then returns same instance")
    void tablesUpdatedEvent_getHandlerList_returnsSameInstance() {
        HandlerList first = TablesUpdatedEvent.getHandlerList();
        HandlerList second = TablesUpdatedEvent.getHandlerList();
        assertSame(first, second);
    }

    // --- Cross-event isolation ---

    @Test
    @DisplayName("Given all four database events, when comparing handler lists, then each event has a distinct list")
    void allDatabaseEvents_haveDistinctHandlerLists() {
        HandlerList preCreate = PreTablesCreateEvent.getHandlerList();
        HandlerList created = TablesCreatedEvent.getHandlerList();
        HandlerList preUpdate = PreTablesUpdateEvent.getHandlerList();
        HandlerList updated = TablesUpdatedEvent.getHandlerList();

        assertNotSame(preCreate, created);
        assertNotSame(preCreate, preUpdate);
        assertNotSame(preCreate, updated);
        assertNotSame(created, preUpdate);
        assertNotSame(created, updated);
        assertNotSame(preUpdate, updated);
    }
}

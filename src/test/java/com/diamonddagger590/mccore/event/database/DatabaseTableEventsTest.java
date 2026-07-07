package com.diamonddagger590.mccore.event.database;

import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class DatabaseTableEventsTest {

    @Test
    @DisplayName("Given PreTablesCreateEvent, when constructed, then getHandlers returns non-null HandlerList")
    void preTablesCreateEvent_getHandlers_returnsNonNull() {
        PreTablesCreateEvent event = new PreTablesCreateEvent();
        assertNotNull(event.getHandlers());
    }

    @Test
    @DisplayName("Given PreTablesCreateEvent, when getHandlerList called, then returns same instance as getHandlers")
    void preTablesCreateEvent_getHandlerList_returnsSameInstanceAsGetHandlers() {
        PreTablesCreateEvent event = new PreTablesCreateEvent();
        assertSame(PreTablesCreateEvent.getHandlerList(), event.getHandlers());
    }

    @Test
    @DisplayName("Given two PreTablesCreateEvents, when getHandlers called, then they share the same HandlerList")
    void preTablesCreateEvent_handlerList_isSharedAcrossInstances() {
        PreTablesCreateEvent event1 = new PreTablesCreateEvent();
        PreTablesCreateEvent event2 = new PreTablesCreateEvent();
        assertSame(event1.getHandlers(), event2.getHandlers());
    }

    @Test
    @DisplayName("Given TablesCreatedEvent, when constructed, then getHandlers returns non-null HandlerList")
    void tablesCreatedEvent_getHandlers_returnsNonNull() {
        TablesCreatedEvent event = new TablesCreatedEvent();
        assertNotNull(event.getHandlers());
    }

    @Test
    @DisplayName("Given TablesCreatedEvent, when getHandlerList called, then returns same instance as getHandlers")
    void tablesCreatedEvent_getHandlerList_returnsSameInstanceAsGetHandlers() {
        TablesCreatedEvent event = new TablesCreatedEvent();
        assertSame(TablesCreatedEvent.getHandlerList(), event.getHandlers());
    }

    @Test
    @DisplayName("Given two TablesCreatedEvents, when getHandlers called, then they share the same HandlerList")
    void tablesCreatedEvent_handlerList_isSharedAcrossInstances() {
        TablesCreatedEvent event1 = new TablesCreatedEvent();
        TablesCreatedEvent event2 = new TablesCreatedEvent();
        assertSame(event1.getHandlers(), event2.getHandlers());
    }

    @Test
    @DisplayName("Given PreTablesUpdateEvent, when constructed, then getHandlers returns non-null HandlerList")
    void preTablesUpdateEvent_getHandlers_returnsNonNull() {
        PreTablesUpdateEvent event = new PreTablesUpdateEvent();
        assertNotNull(event.getHandlers());
    }

    @Test
    @DisplayName("Given PreTablesUpdateEvent, when getHandlerList called, then returns same instance as getHandlers")
    void preTablesUpdateEvent_getHandlerList_returnsSameInstanceAsGetHandlers() {
        PreTablesUpdateEvent event = new PreTablesUpdateEvent();
        assertSame(PreTablesUpdateEvent.getHandlerList(), event.getHandlers());
    }

    @Test
    @DisplayName("Given two PreTablesUpdateEvents, when getHandlers called, then they share the same HandlerList")
    void preTablesUpdateEvent_handlerList_isSharedAcrossInstances() {
        PreTablesUpdateEvent event1 = new PreTablesUpdateEvent();
        PreTablesUpdateEvent event2 = new PreTablesUpdateEvent();
        assertSame(event1.getHandlers(), event2.getHandlers());
    }

    @Test
    @DisplayName("Given TablesUpdatedEvent, when constructed, then getHandlers returns non-null HandlerList")
    void tablesUpdatedEvent_getHandlers_returnsNonNull() {
        TablesUpdatedEvent event = new TablesUpdatedEvent();
        assertNotNull(event.getHandlers());
    }

    @Test
    @DisplayName("Given TablesUpdatedEvent, when getHandlerList called, then returns same instance as getHandlers")
    void tablesUpdatedEvent_getHandlerList_returnsSameInstanceAsGetHandlers() {
        TablesUpdatedEvent event = new TablesUpdatedEvent();
        assertSame(TablesUpdatedEvent.getHandlerList(), event.getHandlers());
    }

    @Test
    @DisplayName("Given two TablesUpdatedEvents, when getHandlers called, then they share the same HandlerList")
    void tablesUpdatedEvent_handlerList_isSharedAcrossInstances() {
        TablesUpdatedEvent event1 = new TablesUpdatedEvent();
        TablesUpdatedEvent event2 = new TablesUpdatedEvent();
        assertSame(event1.getHandlers(), event2.getHandlers());
    }

    @Test
    @DisplayName("Given all four event types, when getHandlerList called, then each type has its own distinct HandlerList")
    void allDatabaseEvents_handlerLists_areDistinctPerType() {
        HandlerList preCreate = PreTablesCreateEvent.getHandlerList();
        HandlerList created = TablesCreatedEvent.getHandlerList();
        HandlerList preUpdate = PreTablesUpdateEvent.getHandlerList();
        HandlerList updated = TablesUpdatedEvent.getHandlerList();

        assertNotNull(preCreate);
        assertNotNull(created);
        assertNotNull(preUpdate);
        assertNotNull(updated);
        assertNotSame(preCreate, created);
        assertNotSame(preCreate, preUpdate);
        assertNotSame(preCreate, updated);
        assertNotSame(created, preUpdate);
        assertNotSame(created, updated);
        assertNotSame(preUpdate, updated);
    }
}

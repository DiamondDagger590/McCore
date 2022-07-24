package com.diamonddagger590.mccore.event.database;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called before any database tables have their
 * creation attempted.
 */
public class PreTablesCreateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public PreTablesCreateEvent() {}

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

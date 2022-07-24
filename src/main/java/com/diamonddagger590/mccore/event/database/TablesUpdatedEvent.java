package com.diamonddagger590.mccore.event.database;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called whenever all registered
 * {@link com.diamonddagger590.mccore.database.function.UpdateTableFunction UpdateTableFunctions}
 * have been executed.
 */

public class TablesUpdatedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public TablesUpdatedEvent() {}

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

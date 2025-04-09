package com.diamonddagger590.mccore.event.gui;

import com.diamonddagger590.mccore.gui.BaseGui;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called whenever a {@link BaseGui} is refreshed.
 */
public class GuiRefreshEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final BaseGui gui;

    public GuiRefreshEvent(@NotNull BaseGui gui) {
        this.gui = gui;
    }

    /**
     * Gets the {@link BaseGui} being refreshed.
     *
     * @return The {@link BaseGui} being refreshed.
     */
    @NotNull
    public BaseGui getGui() {
        return gui;
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

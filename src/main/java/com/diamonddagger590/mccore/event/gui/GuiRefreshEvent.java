package com.diamonddagger590.mccore.event.gui;

import com.diamonddagger590.mccore.gui.Guiv2;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called whenever a {@link Guiv2} is refreshed.
 */
public class GuiRefreshEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Guiv2 gui;

    public GuiRefreshEvent(@NotNull Guiv2 gui) {
        this.gui = gui;
    }

    /**
     * Gets the {@link Guiv2} being refreshed.
     *
     * @return The {@link Guiv2} being refreshed.
     */
    @NotNull
    public Guiv2 getGui() {
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

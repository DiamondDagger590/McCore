package com.diamonddagger590.mccore.event.gui;

import com.diamonddagger590.mccore.gui.Gui;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called whenever a {@link Gui} is refreshed.
 */
public class GuiRefreshEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Gui gui;

    public GuiRefreshEvent(@NotNull Gui gui) {
        this.gui = gui;
    }

    /**
     * Gets the {@link Gui} being refreshed.
     *
     * @return The {@link Gui} being refreshed.
     */
    @NotNull
    public Gui getGui() {
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

package com.diamonddagger590.mccore.event.gui;

import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.KeyedGui;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * This event is called whenever a {@link Gui} is opened for a player.
 */
public class CoreGuiOpenEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Gui<?> gui;

    /**
     * Creates a new {@link CoreGuiOpenEvent}.
     *
     * @param gui The {@link Gui} being opened.
     */
    public CoreGuiOpenEvent(@NotNull Gui<?> gui) {
        this.gui = gui;
    }

    /**
     * Gets the {@link Gui} being opened.
     *
     * @return The {@link Gui} being opened.
     */
    @NotNull
    public Gui<?> getGui() {
        return gui;
    }

    /**
     * Gets the {@link java.util.UUID} of the player who opened this GUI.
     *
     * @return The player's {@link java.util.UUID}.
     */
    @NotNull
    public java.util.UUID getPlayerUUID() {
        return gui.getUUID();
    }

    /**
     * Gets the {@link NamespacedKey} of the GUI being opened, if the GUI
     * implements {@link KeyedGui}.
     *
     * @return An {@link Optional} containing the {@link NamespacedKey} for this GUI,
     * or empty if the GUI does not implement {@link KeyedGui} or has no key.
     */
    @NotNull
    public Optional<NamespacedKey> getGuiKey() {
        if (gui instanceof KeyedGui keyedGui) {
            return keyedGui.getGuiKey();
        }
        return Optional.empty();
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the {@link HandlerList} for this event.
     *
     * @return The {@link HandlerList} for this event.
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}

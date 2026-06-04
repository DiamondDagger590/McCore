package com.diamonddagger590.mccore.event.gui;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Fired when a GUI tracked by {@link com.diamonddagger590.mccore.gui.GuiManager} is opened
 * for a player. Carries the optional {@link NamespacedKey} from {@link com.diamonddagger590.mccore.gui.KeyedGui}
 * implementations, enabling listeners to react to specific GUI types.
 */
public class CoreGuiOpenEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final UUID playerUUID;
    private final Optional<NamespacedKey> guiKey;

    /**
     * Constructs a new {@link CoreGuiOpenEvent}.
     *
     * @param playerUUID the UUID of the player who opened the GUI
     * @param guiKey     the GUI's namespaced key, or empty if the GUI does not implement {@link com.diamonddagger590.mccore.gui.KeyedGui}
     */
    public CoreGuiOpenEvent(@NotNull UUID playerUUID, @NotNull Optional<NamespacedKey> guiKey) {
        this.playerUUID = playerUUID;
        this.guiKey = guiKey;
    }

    /**
     * Gets the UUID of the player who opened the GUI.
     *
     * @return the player's UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Gets the namespaced key of the GUI that was opened, if present.
     *
     * @return an optional containing the GUI key, or empty if the GUI has no key
     */
    @NotNull
    public Optional<NamespacedKey> getGuiKey() {
        return guiKey;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    /**
     * Returns the handler list for this event type.
     *
     * @return the handler list
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}

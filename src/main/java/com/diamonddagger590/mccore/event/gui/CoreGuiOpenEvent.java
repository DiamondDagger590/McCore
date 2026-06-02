package com.diamonddagger590.mccore.event.gui;

import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.KeyedGui;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Fired from {@link com.diamonddagger590.mccore.gui.GuiManager#trackPlayerGui(UUID, Gui)}
 * after GUI tracking completes. Carries the player's UUID, the GUI instance, and the
 * GUI's type key if it implements {@link KeyedGui}.
 * <p>
 * "Open" means any GUI creation tracked by the manager — including back-button navigation
 * re-opens.
 */
public class CoreGuiOpenEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final UUID playerUUID;
    private final Gui<?> gui;
    @Nullable
    private final NamespacedKey guiKey;

    /**
     * Creates a new GUI open event.
     *
     * @param playerUUID the UUID of the player whose GUI was tracked
     * @param gui        the GUI instance being tracked
     * @param guiKey     the GUI's type key if it implements {@link KeyedGui}, or {@code null}
     *                   if the GUI does not implement {@link KeyedGui}
     */
    public CoreGuiOpenEvent(@NotNull UUID playerUUID,
                            @NotNull Gui<?> gui,
                            @Nullable NamespacedKey guiKey) {
        this.playerUUID = playerUUID;
        this.gui = gui;
        this.guiKey = guiKey;
    }

    /**
     * Gets the UUID of the player whose GUI was tracked.
     *
     * @return the player UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Gets the GUI instance that was tracked.
     *
     * @return the GUI
     */
    @NotNull
    public Gui<?> getGui() {
        return gui;
    }

    /**
     * Gets the GUI's type key, if the GUI implements {@link KeyedGui}.
     * GUIs that do not implement {@link KeyedGui} produce an event with an empty optional.
     *
     * @return the GUI's type key, or empty if unkeyed
     */
    @NotNull
    public Optional<NamespacedKey> getGuiKey() {
        return Optional.ofNullable(guiKey);
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}

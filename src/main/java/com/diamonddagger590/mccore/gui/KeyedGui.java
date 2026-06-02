package com.diamonddagger590.mccore.gui;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Optional interface that {@link Gui} implementations can adopt to declare a type-level
 * {@link NamespacedKey} identifying what kind of GUI they are (e.g., {@code mcrpg:home},
 * {@code mcrpg:loadout_selection}).
 * <p>
 * Distinct from {@link BaseGui#getUUID()}, which returns the creating player's UUID
 * (instance identity), not a type key. Implementing this interface allows
 * {@link com.diamonddagger590.mccore.event.gui.CoreGuiOpenEvent} consumers to filter
 * GUI open events by type rather than by instance.
 */
public interface KeyedGui {

    /**
     * Returns the type-level key identifying this GUI's purpose.
     * GUIs that do not implement this interface (or return empty) are
     * still tracked by {@link GuiManager} but are not identifiable by key.
     *
     * @return the GUI's type key, or empty if unkeyed
     */
    @NotNull
    Optional<NamespacedKey> getGuiKey();
}

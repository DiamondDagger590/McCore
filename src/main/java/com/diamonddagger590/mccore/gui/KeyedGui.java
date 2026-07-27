package com.diamonddagger590.mccore.gui;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A {@link Gui} that has an associated {@link NamespacedKey} for identification.
 * This allows listeners and other systems to determine which type of GUI is
 * currently open for a player.
 */
public interface KeyedGui {

    /**
     * Gets the {@link NamespacedKey} associated with this GUI, if present.
     *
     * @return An {@link Optional} containing the {@link NamespacedKey} for this GUI,
     * or empty if this GUI does not have a key.
     */
    @NotNull
    Optional<NamespacedKey> getGuiKey();
}

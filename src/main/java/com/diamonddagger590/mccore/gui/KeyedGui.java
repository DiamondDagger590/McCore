package com.diamonddagger590.mccore.gui;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Interface for GUI implementations that can be identified by a {@link NamespacedKey}.
 * Downstream plugins implement this on their GUI classes to enable key-based
 * identification (e.g., for objective tracking or event filtering).
 */
public interface KeyedGui {

    /**
     * Returns the {@link NamespacedKey} identifying this GUI type, or empty if
     * this GUI instance does not expose a key.
     *
     * @return the GUI key, or empty
     */
    @NotNull
    Optional<NamespacedKey> getGuiKey();
}

package com.diamonddagger590.mccore.exception.gui;

import com.diamonddagger590.mccore.gui.Gui;
import org.jetbrains.annotations.NotNull;

/**
 * This exception is thrown whenever a {@link Gui} tries to build a new {@link org.bukkit.inventory.Inventory}
 * when it already has one.
 */
public class InventoryAlreadyExistsForGuiException extends RuntimeException {

    private final Gui gui;

    public InventoryAlreadyExistsForGuiException(@NotNull Gui gui) {
        this.gui = gui;
    }

    /**
     * Gets the {@link Gui} that tried to build another inventory.
     * @return The {@link Gui} that tried to build another inventory.
     */
    @NotNull
    public Gui getGui() {
        return gui;
    }

    @Override
    public String getMessage() {
        return String.format("GUI of UUID %s already has an inventory, but a new one was requested. Gui#refreshGui() should be used instead in this case.", gui.getUUID());
    }
}

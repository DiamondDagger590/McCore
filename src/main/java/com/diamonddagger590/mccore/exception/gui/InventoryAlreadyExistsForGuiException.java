package com.diamonddagger590.mccore.exception.gui;

import com.diamonddagger590.mccore.gui.Guiv2;
import org.jetbrains.annotations.NotNull;

/**
 * This exception is thrown whenever a {@link Guiv2} tries to build a new {@link org.bukkit.inventory.Inventory}
 * when it already has one.
 */
public class InventoryAlreadyExistsForGuiException extends RuntimeException {

    private final Guiv2 gui;

    public InventoryAlreadyExistsForGuiException(@NotNull Guiv2 gui) {
        this.gui = gui;
    }

    /**
     * Gets the {@link Guiv2} that tried to build another inventory.
     * @return The {@link Guiv2} that tried to build another inventory.
     */
    @NotNull
    public Guiv2 getGui() {
        return gui;
    }

    @Override
    public String getMessage() {
        return String.format("GUI of UUID %s already has an inventory, but a new one was requested. Gui#refreshGui() should be used instead in this case.", gui.getUUID());
    }
}

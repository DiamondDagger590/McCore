package com.diamonddagger590.mccore.exception.gui;

import com.diamonddagger590.mccore.gui.BaseGui;
import org.jetbrains.annotations.NotNull;

/**
 * This exception is thrown whenever a {@link BaseGui} tries to build a new {@link org.bukkit.inventory.Inventory}
 * when it already has one.
 */
public class InventoryAlreadyExistsForGuiException extends RuntimeException {

    private final BaseGui gui;

    public InventoryAlreadyExistsForGuiException(@NotNull BaseGui gui) {
        this.gui = gui;
    }

    /**
     * Gets the {@link BaseGui} that tried to build another inventory.
     * @return The {@link BaseGui} that tried to build another inventory.
     */
    @NotNull
    public BaseGui getGui() {
        return gui;
    }

    @Override
    public String getMessage() {
        return String.format("GUI of UUID %s already has an inventory, but a new one was requested. Gui#refreshGui() should be used instead in this case.", gui.getUUID());
    }
}

package com.diamonddagger590.mccore.gui.component;

import com.diamonddagger590.mccore.gui.Gui;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This should be implemented by any GUIs that want to have behavior
 * run whenever the GUI closes
 */
public interface ClosableGui extends Gui {

    /**
     * This method is called whenever the GUI closes. Note that this method being called does
     * not directly mean that it applies to this GUI and validation for this should be performed
     *
     * @param inventoryCloseEvent The close event that was called
     */
    void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent);
}

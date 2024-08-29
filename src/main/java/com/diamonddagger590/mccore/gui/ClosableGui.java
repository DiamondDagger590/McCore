package com.diamonddagger590.mccore.gui;

import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * This should be implemented by any gui that needs to perform an action when it closes.
 */
public interface ClosableGui {

    void onClose(InventoryCloseEvent inventoryCloseEvent);
}

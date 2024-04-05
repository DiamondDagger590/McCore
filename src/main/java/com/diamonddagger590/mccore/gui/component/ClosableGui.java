package com.diamonddagger590.mccore.gui.component;

import com.diamonddagger590.mccore.gui.Gui;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

public interface ClosableGui extends Gui {

    void handleClose(@NotNull InventoryCloseEvent inventoryCloseEvent);
}

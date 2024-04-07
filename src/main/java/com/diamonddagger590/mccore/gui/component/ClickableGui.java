package com.diamonddagger590.mccore.gui.component;

import com.diamonddagger590.mccore.gui.Gui;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * A GUI that should be implemented for any GUI that listens to
 * {@link InventoryClickEvent}s.
 */
public interface ClickableGui extends Gui {

    void handleClick(@NotNull InventoryClickEvent inventoryClickEvent);

}

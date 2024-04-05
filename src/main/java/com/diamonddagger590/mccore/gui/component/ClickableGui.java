package com.diamonddagger590.mccore.gui.component;

import com.diamonddagger590.mccore.gui.Gui;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public interface ClickableGui extends Gui {

    void handleClick(@NotNull InventoryClickEvent inventoryClickEvent);

}

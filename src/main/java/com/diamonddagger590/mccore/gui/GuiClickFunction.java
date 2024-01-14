package com.diamonddagger590.mccore.gui;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public interface GuiClickFunction {

    public void onClick(@NotNull CorePlayer corePlayer, @NotNull Gui gui, @NotNull InventoryClickEvent inventoryClickEvent);
}

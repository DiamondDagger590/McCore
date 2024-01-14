package com.diamonddagger590.mccore.gui;

import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class Gui implements Listener {

    protected final UUID guiUUID = UUID.randomUUID();
    protected GuiFillerFunction guiFillerFunction = (inventory -> {});

    @NotNull
    public abstract Inventory getInventory();

    public abstract void registerListeners();
    public abstract void unregisterListeners();
    public void executeFillerFunction() {
        guiFillerFunction.fillGui(getInventory());
    }

}

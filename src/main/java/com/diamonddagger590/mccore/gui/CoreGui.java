package com.diamonddagger590.mccore.gui;

import com.diamonddagger590.mccore.gui.function.GuiFillerFunction;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class CoreGui implements Gui {

    protected final UUID guiUUID = UUID.randomUUID();
    protected Inventory inventory;
    protected GuiFillerFunction guiFillerFunction = (inventory -> {});

    public void executeFillerFunction() {
        guiFillerFunction.fillGui(getInventory());
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}

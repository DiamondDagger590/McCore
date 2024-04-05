package com.diamonddagger590.mccore.gui.component;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public abstract class GuiPage {

    private final int pageNumber;

    private final Inventory inventory;

    public GuiPage(int pageNumber, @NotNull Inventory inventory) {
        this.pageNumber = pageNumber;
        this.inventory = inventory;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    @NotNull
    public Inventory getInventory() {
        return inventory;
    }
}

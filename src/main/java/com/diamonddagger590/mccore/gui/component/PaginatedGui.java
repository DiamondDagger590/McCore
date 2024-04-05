package com.diamonddagger590.mccore.gui.component;

import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.function.GuiClickFunction;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public interface PaginatedGui extends ClickableGui {

    GuiClickFunction NEXT_PAGE_FUNCTION = (CorePlayer corePlayer, Gui gui, InventoryClickEvent inventoryClickEvent) -> {
        if (gui instanceof PaginatedGui paginatedGui) {
            paginatedGui.progressPage(true);
            inventoryClickEvent.setCancelled(true);
        }
    };

    GuiClickFunction LAST_PAGE_FUNCTION = (CorePlayer corePlayer, Gui gui, InventoryClickEvent inventoryClickEvent) -> {
        if (gui instanceof PaginatedGui paginatedGui) {
            paginatedGui.progressPage(false);
            inventoryClickEvent.setCancelled(true);
        }
    };

    @NotNull
    GuiPage getCurrentPage();

    @NotNull
    @Override
    default Inventory getInventory() {
        return getCurrentPage().getInventory();
    }

    int getMaxPage();

    void setCurrentPage(@NotNull GuiPage guiPage);

    void progressPage(boolean forward);

    void paintGuiPage();
}

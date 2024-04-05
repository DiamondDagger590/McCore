package com.diamonddagger590.mccore.gui;

import com.diamonddagger590.mccore.CorePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface Gui extends Listener {

    UUID guiUUID = UUID.randomUUID();

    void registerListeners();
    void unregisterListeners();

    @NotNull
    Inventory getInventory();

    default UUID getUUID() {
        return guiUUID;
    }

    default boolean canProcessEvent(@NotNull Player player, @NotNull Inventory inventory) {
        GuiTracker guiTracker = CorePlugin.getInstance().getGuiTracker();
        return inventory == getInventory() && guiTracker.getOpenedGui(player).isPresent() && guiTracker.getOpenedGui(player).get() == this;
    }
}

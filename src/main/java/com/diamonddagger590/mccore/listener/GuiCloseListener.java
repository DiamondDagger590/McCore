package com.diamonddagger590.mccore.listener;

import com.diamonddagger590.mccore.CorePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GuiCloseListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleGuiClose(InventoryCloseEvent inventoryCloseEvent) {
        if (inventoryCloseEvent.getPlayer() instanceof Player player) {
            CorePlugin.getInstance().getGuiTracker().stopTrackingPlayer(player);
        }
    }
}

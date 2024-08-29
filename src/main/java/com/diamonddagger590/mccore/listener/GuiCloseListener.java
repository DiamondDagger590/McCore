package com.diamonddagger590.mccore.listener;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.gui.ClosableGui;
import com.diamonddagger590.mccore.gui.GuiTracker;
import com.diamonddagger590.mccore.gui.GuiTrackerv2;
import com.diamonddagger590.mccore.gui.Guiv2;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Optional;

/**
 * A listener that manages removing a player's tracked GUI from the {@link GuiTracker}
 */
public class GuiCloseListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleGuiClose(InventoryCloseEvent inventoryCloseEvent) {
        if (inventoryCloseEvent.getPlayer() instanceof Player player) {
            GuiTrackerv2 guiTracker = CorePlugin.getInstance().getGuiTrackerv2();
            Optional<Guiv2> guiOptional = guiTracker.getOpenedGui(player);
            if (guiOptional.isPresent()) {
                Guiv2 gui = guiOptional.get();
                if (gui.getInventory() == inventoryCloseEvent.getInventory()) {
                    guiTracker.stopTrackingPlayer(player);
                }
                if (gui instanceof ClosableGui closableGui) {
                    closableGui.onClose(inventoryCloseEvent);
                }
            }
        }
    }
}

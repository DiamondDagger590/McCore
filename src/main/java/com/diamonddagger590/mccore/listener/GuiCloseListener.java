package com.diamonddagger590.mccore.listener;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.gui.ClosableGui;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.GuiManager;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.CoreManagerKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * A listener that manages removing a player's tracked GUI from the {@link GuiManager}
 */
public class GuiCloseListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleGuiClose(InventoryCloseEvent inventoryCloseEvent) {
        if (inventoryCloseEvent.getPlayer() instanceof Player player) {
            GuiManager<?, ?> guiManager = CorePlugin.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(CoreManagerKey.CORE_GUI_MANAGER);
            var guiOptional = guiManager.getOpenedGui(player);
            if (guiOptional.isPresent()) {
                Gui<?> gui = guiOptional.get();
                if (gui.getInventory() == inventoryCloseEvent.getInventory()) {
                    guiManager.stopTrackingPlayer(player);
                }
                if (gui instanceof ClosableGui<?> closableGui) {
                    closableGui.onClose(inventoryCloseEvent);
                }
            }
        }
    }
}

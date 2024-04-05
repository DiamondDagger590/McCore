package com.diamonddagger590.mccore.gui.component;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.gui.GuiTracker;
import com.diamonddagger590.mccore.gui.function.GuiClickFunction;
import com.diamonddagger590.mccore.player.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public interface StandardClickableGui extends ClickableGui {

    Map<Integer, GuiClickFunction> guiClickFunctions = new HashMap<>();

    boolean cancelNonFunctionClicks();

    default void addGuiClickFunction(int slot, @NotNull GuiClickFunction guiClickFunction) {
        guiClickFunctions.put(slot, guiClickFunction);
    }

    @Override
    default void handleClick(@NotNull InventoryClickEvent inventoryClickEvent) {
        int slot = inventoryClickEvent.getSlot();
        Inventory inventory = inventoryClickEvent.getClickedInventory();
        if (inventoryClickEvent.getWhoClicked() instanceof Player player
                && inventory != null
                && canProcessEvent(player, inventory)) {

            PlayerManager playerManager = CorePlugin.getInstance().getPlayerManager();
            GuiTracker guiTracker = CorePlugin.getInstance().getGuiTracker();
            AtomicBoolean hasProcessed = new AtomicBoolean(false);

            if (guiClickFunctions.containsKey(slot)) {
                playerManager.getPlayer(player.getUniqueId()).ifPresent(corePlayer -> {
                    guiTracker.getOpenedGui(corePlayer).ifPresent(gui -> {
                        guiClickFunctions.get(inventoryClickEvent.getSlot()).onClick(corePlayer, gui, inventoryClickEvent);
                        hasProcessed.set(true);
                    });
                });
            }
            if (!hasProcessed.get() && cancelNonFunctionClicks()) {
                inventoryClickEvent.setCancelled(true);
            }
        }
    }
}

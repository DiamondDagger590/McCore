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

/**
 * This GUI is designed for standardized GUIs that don't have any special behavior
 * besides doing things when certain slots are clicked.
 * <p>
 * This is done by adding {@link GuiClickFunction}s that will be handled automatically
 * without needing much effort from an implementing class.
 */
public interface StandardClickableGui extends ClickableGui {

    Map<Integer, GuiClickFunction> guiClickFunctions = new HashMap<>();

    /**
     * Checks to see if any clicks on slots without a click function should be cancelled.
     *
     * @return {@code true} if any clicks on slots without a click function should be cancelled.
     */
    boolean cancelNonFunctionClicks();

    /**
     * Adds the provided {@link GuiClickFunction} to be called whenever the provided slot is clicked
     *
     * @param slot             The slot to add the click function for
     * @param guiClickFunction The {@link GuiClickFunction} to run
     */
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

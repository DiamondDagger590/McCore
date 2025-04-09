package com.diamonddagger590.mccore.gui;

import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A gui is a wrapper around Bukkit's {@link Inventory Inventories}.
 * <p>
 * A gui is operated via {@link Slot Slots}. Slots contain an action
 * that should occur whenever a click happens. This allows for reusable pieces of
 * guis to be shared without having bulky logic copy pasted.
 * <p>
 * Every gui also acts as a listener for itself, being registered and unregistered
 * whenever a player starts viewing and whenever no players are viewing the gui respectively.
 */public interface Gui<P extends CorePlayer> {

    /**
     * Gets the {@link UUID} for this gui.
     *
     * @return The {@link UUID} for this gui.
     */
    @NotNull
    UUID getUUID();

    /**
     * Gets the {@link Slot} at the provided index.
     *
     * @param index The index to get the slot for.
     * @return The {@link Slot} at the provided index.
     */
    @NotNull
    Slot<P> getSlot(int index);

    /**
     * Gets the Bukkit {@link Inventory} for this gui.
     *
     * @return The {@link Inventory} for this gui.
     */
    @NotNull
    Inventory getInventory();

    /**
     * Handles the {@link InventoryClickEvent}
     */
    void handleClickEvent(@NotNull InventoryClickEvent inventoryClickEvent);

    /**
     * This method is useful for assigning {@link Slot}s for a gui
     * and overall painting the view that the player will see.
     * <p>
     * This is where setting of slots should occur.
     */
    void paintInventory();

    /**
     * Repaints this gui
     */
    default void refreshGUI() {
        paintInventory();
    }
}

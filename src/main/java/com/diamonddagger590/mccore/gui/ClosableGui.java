package com.diamonddagger590.mccore.gui;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This should be implemented by any gui that needs to perform an action when it closes.
 */
public interface ClosableGui<P extends CorePlayer> extends Gui<P> {

    void onClose(@NotNull InventoryCloseEvent inventoryCloseEvent);
}

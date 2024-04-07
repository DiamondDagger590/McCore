package com.diamonddagger590.mccore.gui;

import com.diamonddagger590.mccore.CorePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A template GUI that provides some basic methods. This class should never be
 * implemented outside of this core plugin. Instead, {@link CoreGui} should be extended,
 * with specific components such as {@link com.diamonddagger590.mccore.gui.component.ClickableGui}
 * being implemented for specific behavior.
 * <p>
 * Every GUI is treated as its own listener and will be registered whenever it is opened, and unregistered whenever
 * all viewers have closed it.
 */
public interface Gui extends Listener {

    UUID guiUUID = UUID.randomUUID();

    /**
     * Register the events that this GUI should be a listener of
     */
    void registerListeners();

    /**
     * Unregister this GUI as a listener
     */
    void unregisterListeners();

    /**
     * Get the Bukkit {@link Inventory} of this GUI
     *
     * @return The Bukkit {@link Inventory} of this GUI
     */
    @NotNull
    Inventory getInventory();

    /**
     * Get the {@link UUID} of this GUI
     *
     * @return The {@link UUID} of this GUI
     */
    default UUID getUUID() {
        return guiUUID;
    }

    /**
     * Checks to see if this GUI can process an event for the provided {@link Player} and {@link Inventory}.
     * <p>
     * This logic should be checked to validate that this is the GUI calling some sort of event such as an {@link org.bukkit.event.inventory.InventoryClickEvent}.
     *
     * @param player    The {@link Player} to check
     * @param inventory The {@link Inventory} to check
     * @return {@code true} if this GUI can process an event for the provided {@link Player} and {@link Inventory}
     */
    default boolean canProcessEvent(@NotNull Player player, @NotNull Inventory inventory) {
        GuiTracker guiTracker = CorePlugin.getInstance().getGuiTracker();
        return inventory == getInventory() && guiTracker.getOpenedGui(player).isPresent() && guiTracker.getOpenedGui(player).get() == this;
    }
}

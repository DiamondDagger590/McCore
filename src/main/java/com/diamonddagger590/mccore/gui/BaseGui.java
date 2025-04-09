package com.diamonddagger590.mccore.gui;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.exception.gui.IllegalSlotAssignmentException;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A basic implementation of {@link Gui} meant to be extended for development.
 *
 * @param <P> The {@link CorePlayer} type supported by this gui.
 */
public abstract class BaseGui<P extends CorePlayer> implements Listener, Gui<P> {

    protected final Slot<P> DEFAULT_SLOT = new Slot<>() {
        @Override
        public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
            return false;
        }
    };
    private final UUID guiUUID;
    @Nullable
    private final P creatingPlayer;
    private Map<Integer, Slot<P>> slots = new HashMap<>();
    protected Inventory inventory;

    public BaseGui() {
        this.guiUUID = UUID.randomUUID();
        this.creatingPlayer = null;
    }

    public BaseGui(@NotNull P creatingPlayer) {
        this.guiUUID = creatingPlayer.getUUID();
        this.creatingPlayer = creatingPlayer;
    }

    @Override
    @NotNull
    public UUID getUUID() {
        return guiUUID;
    }

    /**
     * Gets the {@link P} that created this GUI.
     *
     * @return An {@link Optional} containing the {@link P} that created this GUI if present.
     */
    @NotNull
    public Optional<P> getCreatingPlayer() {
        return Optional.ofNullable(creatingPlayer);
    }

    /**
     * Gets the {@link Slot} at the provided index, or will return a default slot which
     * has no action or item if there is no slot registered for the provided index.
     *
     * @param index The index to get the slot for.
     * @return The {@link Slot} at the provided index, or a default slot with no action
     * or item if there is no slot registered for the provided index.
     */
    @NotNull
    public Slot<P> getSlot(int index) {
        return slots.getOrDefault(index, DEFAULT_SLOT);
    }

    /**
     * Adds the provided {@link Slot} at the provided index for this gui.
     * <p>
     * This method requires that {@link #buildInventory()} has been called already
     * and the index must also be less than the size of the gui's inventory or exceptions will be thrown.
     * <p>
     * This method also adds the {@link Slot#getItem(CorePlayer)} to the Bukkit inventory as well.
     *
     * @param index The index to add the provided slot at.
     * @param slot  The {@link Slot} to register.
     * @throws IllegalSlotAssignmentException whenever {@link Slot#getValidGuiTypes()} is non-empty and doesn't contain this gui type.
     */
    public void setSlot(int index, @NotNull Slot<P> slot) {
        if (!canSlotBelongToGui(slot)) {
            throw new IllegalSlotAssignmentException(this, slot);
        }
        Preconditions.checkNotNull(inventory, "Inventory most not be null before a slot can be added");
        Preconditions.checkArgument(index < inventory.getSize());
        slots.put(index, slot);
        ItemBuilder itemBuilder = slot.getItem(creatingPlayer);
        inventory.setItem(index, creatingPlayer != null ? itemBuilder.asItemStack(creatingPlayer.getPlugin().getAdventure().player(creatingPlayer.getUUID())) : itemBuilder.asItemStack());
    }

    /**
     * Removes the {@link Slot} at the given index from this gui.
     * <p>
     * This method also calls {@link Inventory#clear(int)} for
     * the provided index as well.
     *
     * @param index The index to remove the {@link Slot} for.
     */
    public void removeSlot(int index) {
        slots.remove(index);
        inventory.clear(index);
    }


    @EventHandler(ignoreCancelled = true)
    @Override
    public void handleClickEvent(@NotNull InventoryClickEvent inventoryClickEvent) {
        int slotId = inventoryClickEvent.getSlot();
        Slot<P> slot = getSlot(slotId);
        var corePlayerOptional = CorePlugin.getInstance().getPlayerManager().getPlayer(inventoryClickEvent.getWhoClicked().getUniqueId());
        if (canProcessEvent((Player) inventoryClickEvent.getWhoClicked(), inventoryClickEvent.getView().getTopInventory())) {
            // Handle clicking on the bottom inventory
            if (inventoryClickEvent.getView().getBottomInventory() == inventoryClickEvent.getClickedInventory()) {
                // If we don't allow clicking on the bottom inventory, set event to cancelled
                if (!allowBottomInventoryClick()) {
                    inventoryClickEvent.setCancelled(true);
                }
                // Return after because we don't use slots in the bottom part of the inventory
                return;
            }
            corePlayerOptional.map(corePlayer -> (P) corePlayer).ifPresent(corePlayer -> {
                // Set cancellation state based on result of click
                inventoryClickEvent.setCancelled(slot.onClick(corePlayer, inventoryClickEvent.getClick()));
            });
        }
    }

    /**
     * Builds the Bukkit {@link Inventory} for this gui.
     */
    protected abstract void buildInventory();

    /**
     * Gets the Bukkit {@link Inventory} for this gui.
     * <p>
     * This method will call {@link #buildInventory()} if
     * there is no existing inventory.
     *
     * @return The {@link Inventory} for this gui.
     */
    @NotNull
    @Override
    public Inventory getInventory() {
        if (inventory == null) {
            buildInventory();
        }
        Preconditions.checkNotNull(inventory);
        return inventory;
    }


    /**
     * Checks to see if the provided {@link Slot} is allowed for this gui type.
     *
     * @param slot The {@link Slot} to check
     * @return {@code true} if the provided {@link Slot} can be added to this gui type.
     */
    protected final boolean canSlotBelongToGui(@NotNull Slot<?> slot) {
        for (Class<?> clazz : slot.getValidGuiTypes()) {
            if (clazz.isInstance(this)) {
                return true;
            }
        }
        return slot.getValidGuiTypes().isEmpty();
    }

    /**
     * Register the events that this GUI should be a listener of
     */
    public abstract void registerListeners();

    /**
     * Unregister this GUI as a listener
     */
    public abstract void unregisterListeners();

    /**
     * Checks to see if this GUI can process an event for the provided {@link Player} and {@link Inventory}.
     * <p>
     * This logic should be checked to validate that this is the GUI calling some sort of event such as an {@link org.bukkit.event.inventory.InventoryClickEvent}.
     *
     * @param player    The {@link Player} to check
     * @param inventory The {@link Inventory} to check
     * @return {@code true} if this GUI can process an event for the provided {@link Player} and {@link Inventory}
     */
    public boolean canProcessEvent(@NotNull Player player, @NotNull Inventory inventory) {
        GuiTracker guiTracker = CorePlugin.getInstance().getGuiTracker();
        return inventory == getInventory() && guiTracker.getOpenedGui(player).isPresent() && guiTracker.getOpenedGui(player).get() == this;
    }

    /**
     * Checks to see if this GUI allows player clicking on the bottom half of the GUI.
     *
     * @return {@code true} if this GUI allows players to click on the bottom half of the GUI.
     */
    public boolean allowBottomInventoryClick() {
        return false;
    }
}

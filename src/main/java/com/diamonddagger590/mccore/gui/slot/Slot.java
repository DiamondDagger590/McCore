package com.diamonddagger590.mccore.gui.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.gui.BaseGui;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * A slot is a representation of a slot in a Bukkit {@link org.bukkit.inventory.Inventory}. A slot
 * object provides the {@link ItemStack} representation of the slot, an action to perform when the slot
 * is clicked, and a set of gui types that can accept this slot.
 */
public interface Slot<P extends CorePlayer> {

    /**
     * Gets the {@link ItemStack} used to represent this slot.
     *
     * @return The {@link ItemStack} used to represent this slot.
     */
    @NotNull
    default ItemBuilder getItem(@NotNull P corePlayer) {
        return ItemBuilder.from(ItemType.AIR);
    }

    /**
     * This method is fired whenever this slot is clicked in a gui.
     *
     * @param corePlayer The {@link P} who clicked.
     * @param clickType  The {@link ClickType} that was performed.
     * @return {@code true} if the {@link org.bukkit.event.inventory.InventoryClickEvent} that called this
     * method should be cancelled.
     */
    boolean onClick(@NotNull P corePlayer, @NotNull ClickType clickType);

    /**
     * Gets a {@link Set} of all {@link BaseGui} classes that can accept this slot.
     * If the slot should be accepted into any gui, an empty set will be returned.
     * <p>
     * This allows for making gui specific logic in {@link #onClick(P, ClickType)} without
     * needing to worry too much about edge cases of other guis using this slot.
     *
     * @return A {@link Set} of all {@link BaseGui} classes that can accept this slot.
     * If the slot should be accepted into any gui, an empty set will be returned.
     */
    default Set<Class<?>> getValidGuiTypes() {
        return new HashSet<>();
    }

}

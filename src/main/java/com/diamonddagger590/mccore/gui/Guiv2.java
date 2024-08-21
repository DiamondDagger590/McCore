package com.diamonddagger590.mccore.gui;

import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class Guiv2 {

    private static final Slot DEFAULT_SLOT = new Slot() {
        @Override
        public void onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
        }
    };

    private Map<Integer, Slot> slots = new HashMap<>();
    protected Inventory inventory;

    public Guiv2() {

    }

    @NotNull
    public Slot getSlot(int slotId) {
        return slots.getOrDefault(slotId, DEFAULT_SLOT);
    }

    public void setSlot(int slotId, @NotNull Slot slot) {
        slots.put(slotId, slot);
    }

    public void removeSlot(int slotId) {
        slots.remove(slotId);
    }

    protected abstract void buildInventory();

    @NotNull
    public Inventory getInventory() {
        if (inventory == null) {
            buildInventory();
        }
        Preconditions.checkNotNull(inventory);
        return inventory;
    }
}

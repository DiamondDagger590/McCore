package com.diamonddagger590.mccore.gui.slot;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class Slot {

    public Slot() {
    }

    @NotNull
    public ItemStack getItem() {
        return new ItemStack(Material.AIR);
    }

    protected abstract void onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType);
}

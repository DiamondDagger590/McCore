package com.diamonddagger590.mccore.gui.slot;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.gui.Guiv2;
import com.diamonddagger590.mccore.gui.PaginatedGui;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * This is a slot to be used in {@link PaginatedGui}s to allow for going
 * to the previous page of the gui.
 */
public class PreviousPageSlot extends Slot {

    private static final Set<Class<? extends Guiv2>> VALID_GUIS = Set.of(PaginatedGui.class);
    private static final ItemStack SLOT_ITEM;
    static {
        SLOT_ITEM = new ItemStack(Material.ARROW);
        ItemMeta itemMeta = SLOT_ITEM.getItemMeta();
        itemMeta.displayName(CorePlugin.getInstance().getMiniMessage().deserialize("<red>Previous Page</red>"));
        itemMeta.lore(List.of(CorePlugin.getInstance().getMiniMessage().deserialize("<gray>Click to go back to the previous page.</gray>")));
        SLOT_ITEM.setItemMeta(itemMeta);
    }

    @Override
    public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
        var guiOptional = CorePlugin.getInstance().getGuiTrackerv2().getOpenedGui(corePlayer);
        guiOptional.ifPresent(gui -> {
            if (gui instanceof PaginatedGui paginatedGui && paginatedGui.getPage() > 1) {
                corePlayer.getAsBukkitPlayer().ifPresent(player -> {
                    paginatedGui.setPage(paginatedGui.getPage() - 1);
                    paginatedGui.refreshGUI();
                });
            }
        });
        return true;
    }

    @Override
    public Set<Class<? extends Guiv2>> getValidGuiTypes() {
        return VALID_GUIS;
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        return SLOT_ITEM;
    }
}

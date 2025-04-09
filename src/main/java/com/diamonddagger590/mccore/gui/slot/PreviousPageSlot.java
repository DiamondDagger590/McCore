package com.diamonddagger590.mccore.gui.slot;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.PaginatedGui;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * This is a slot to be used in {@link PaginatedGui}s to allow for going
 * to the previous page of the gui.
 */
public abstract class PreviousPageSlot<P extends CorePlayer> extends Slot<P> {

    private final Set<Class<?>> VALID_GUIS = Set.of(PaginatedGui.class);

    public PreviousPageSlot() {}

    @Override
    public boolean onClick(@NotNull P corePlayer, @NotNull ClickType clickType) {
        var guiOptional = CorePlugin.getInstance().getGuiTracker().getOpenedGui(corePlayer);
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
    public Set<Class<?>> getValidGuiTypes() {
        return VALID_GUIS;
    }
}

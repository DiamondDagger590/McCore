package com.diamonddagger590.mccore.gui.slot.pagination;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.gui.PaginatedGui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.CoreManagerKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * This is a slot to be used in {@link PaginatedGui}s to allow for going
 * to the next page of the gui.
 */
public abstract class NextPageSlot<P extends CorePlayer> implements Slot<P> {

    private final Set<Class<?>> VALID_GUIS = Set.of(PaginatedGui.class);

    public NextPageSlot() {}

    @Override
    public boolean onClick(@NotNull P corePlayer, @NotNull ClickType clickType) {
        var guiOptional = CorePlugin.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(CoreManagerKey.CORE_GUI_MANAGER).getOpenedGui(corePlayer);
        guiOptional.ifPresent(gui -> {
            if (gui instanceof PaginatedGui<?> paginatedGui && paginatedGui.getPage() < paginatedGui.getMaximumPage()) {
                corePlayer.getAsBukkitPlayer().ifPresent(player -> {
                    paginatedGui.setPage(paginatedGui.getPage() + 1);
                    paginatedGui.refreshGUI();
                });
            }
        });
        return true;
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return VALID_GUIS;
    }
}

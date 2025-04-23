package com.diamonddagger590.mccore.listener;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.event.gui.GuiRefreshEvent;
import com.diamonddagger590.mccore.gui.GuiManager;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listens for the {@link GuiRefreshEvent} in order to call {@link GuiManager#refreshGui(com.diamonddagger590.mccore.gui.Gui)}
 */
public class GuiRefreshListener implements Listener {

    @EventHandler
    public void onGuiRefresh(GuiRefreshEvent event) {
        CorePlugin.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.CORE_GUI_MANAGER).refreshGui(event.getGui());
    }
}

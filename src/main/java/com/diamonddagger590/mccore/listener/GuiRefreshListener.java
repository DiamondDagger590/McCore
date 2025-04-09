package com.diamonddagger590.mccore.listener;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.event.gui.GuiRefreshEvent;
import com.diamonddagger590.mccore.gui.BaseGui;
import com.diamonddagger590.mccore.gui.GuiTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listens for the {@link GuiRefreshEvent} in order to call {@link GuiTracker#refreshGui(BaseGui)}
 */
public class GuiRefreshListener implements Listener {

    @EventHandler
    public void onGuiRefresh(GuiRefreshEvent event) {
        CorePlugin.getInstance().getGuiTracker().refreshGui(event.getGui());
    }
}

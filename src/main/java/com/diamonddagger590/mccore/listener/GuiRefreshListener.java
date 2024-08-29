package com.diamonddagger590.mccore.listener;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.event.gui.GuiRefreshEvent;
import com.diamonddagger590.mccore.gui.Guiv2;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listens for the {@link GuiRefreshEvent} in order to call {@link com.diamonddagger590.mccore.gui.GuiTrackerv2#refreshGui(Guiv2)}
 */
public class GuiRefreshListener implements Listener {

    @EventHandler
    public void onGuiRefresh(GuiRefreshEvent event) {
        CorePlugin.getInstance().getGuiTrackerv2().refreshGui(event.getGui());
    }
}

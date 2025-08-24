package com.diamonddagger590.mccore.bootstrap.registrar;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.bootstrap.BootstrapContext;
import com.diamonddagger590.mccore.listener.ChatResponseListener;
import com.diamonddagger590.mccore.listener.GuiCloseListener;
import com.diamonddagger590.mccore.listener.GuiRefreshListener;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class ListenerRegistrar<P extends CorePlugin> implements Registrar<P> {

    @Override
    public void register(@NotNull BootstrapContext<P> context) {
        P plugin = context.plugin();
        Bukkit.getPluginManager().registerEvents(new GuiCloseListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new GuiRefreshListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new ChatResponseListener(), plugin);
    }
}

package com.diamonddagger590.mccore.bootstrap;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.bootstrap.registrar.CommandRegistrar;
import com.diamonddagger590.mccore.bootstrap.registrar.HooksRegistrar;
import com.diamonddagger590.mccore.bootstrap.registrar.ListenerRegistrar;
import com.diamonddagger590.mccore.chat.ChatResponseManager;
import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.database.driver.DriverRegistry;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry;
import com.diamonddagger590.mccore.setting.PlayerSettingRegistry;
import org.jetbrains.annotations.NotNull;

public abstract class CoreBootstrap<P extends CorePlugin> {

    private final P plugin;

    public CoreBootstrap(@NotNull P plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public P getPlugin() {
        return plugin;
    }

    public void start(@NotNull StartupProfile startupProfile) {
        BootstrapContext<P> bootstrapContext = new BootstrapContext<>(plugin, startupProfile);
        RegistryAccess registryAccess = plugin.registryAccess();
        registryAccess.register(new ManagerRegistry());
        registryAccess.register(new PluginHookRegistry());
        registryAccess.register(new PlayerSettingRegistry());
        registryAccess.registry(RegistryKey.MANAGER).register(new ReloadableContentManager(plugin));
        registryAccess.registry(RegistryKey.MANAGER).register(new ChatResponseManager(plugin));


        new HooksRegistrar<P>().register(bootstrapContext);
        new ListenerRegistrar<P>().register(bootstrapContext);
        if (startupProfile == StartupProfile.PROD) {
            registryAccess.register(new DriverRegistry());
            new CommandRegistrar<P>().register(bootstrapContext);
        }
    }
}

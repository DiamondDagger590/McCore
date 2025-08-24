package com.diamonddagger590.mccore.bootstrap.registrar;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.bootstrap.BootstrapContext;
import com.diamonddagger590.mccore.external.cmi.CoreCMIHook;
import com.diamonddagger590.mccore.external.headdatabase.CoreHeadDatabaseHook;
import com.diamonddagger590.mccore.external.itemsadder.CoreItemsAdderHook;
import com.diamonddagger590.mccore.external.modelengine.CoreModelEngineHook;
import com.diamonddagger590.mccore.external.mythicmobs.CoreMythicMobsHook;
import com.diamonddagger590.mccore.external.nexo.CoreNexoHook;
import com.diamonddagger590.mccore.external.papi.CorePapiHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class HooksRegistrar<P extends CorePlugin> implements Registrar<P> {
    
    @Override
    public void register(@NotNull BootstrapContext<P> context) {
        P plugin = context.plugin();
        RegistryAccess registryAccess = plugin.registryAccess();
        Logger logger = plugin.getLogger();
        if (Bukkit.getPluginManager().isPluginEnabled("Nexo")) {
            logger.info("Nexo found... registering hooks for core");
            registryAccess.registry(RegistryKey.PLUGIN_HOOK).register(new CoreNexoHook(plugin));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
            logger.info("ItemsAdder found... registering hook for core");
            registryAccess.registry(RegistryKey.PLUGIN_HOOK).register(new CoreItemsAdderHook(plugin));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("HeadDatabase")) {
            logger.info("HeadDatabase found... registering hooks for core");
            registryAccess.registry(RegistryKey.PLUGIN_HOOK).register(new CoreHeadDatabaseHook(plugin));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            logger.info("PlaceholderAPI found... registering placeholders translation support for core");
            registryAccess.registry(RegistryKey.PLUGIN_HOOK).register(new CorePapiHook(plugin));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("ModelEngine")) {
            logger.info("ModelEngine found... registering placeholders translation support for core");
            registryAccess.registry(RegistryKey.PLUGIN_HOOK).register(new CoreModelEngineHook(plugin));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            logger.info("MythicMobs found... registering placeholders translation support for core");
            registryAccess.registry(RegistryKey.PLUGIN_HOOK).register(new CoreMythicMobsHook(plugin));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("CMI")) {
            logger.info("CMI found... registering hooks for core");
            registryAccess.registry(RegistryKey.PLUGIN_HOOK).register(new CoreCMIHook(plugin));
        }
    }
}

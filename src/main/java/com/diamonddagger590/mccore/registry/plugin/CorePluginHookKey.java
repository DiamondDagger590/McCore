package com.diamonddagger590.mccore.registry.plugin;

import com.diamonddagger590.mccore.external.headdatabase.CoreHeadDatabaseHook;
import com.diamonddagger590.mccore.external.itemsadder.CoreItemsAdderHook;
import com.diamonddagger590.mccore.external.modelengine.CoreModelEngineHook;
import com.diamonddagger590.mccore.external.mythicmobs.CoreMythicMobsHook;
import com.diamonddagger590.mccore.external.nexo.CoreNexoHook;
import com.diamonddagger590.mccore.external.papi.CorePapiHook;
import org.jetbrains.annotations.ApiStatus;

import static com.diamonddagger590.mccore.registry.plugin.PluginHookKeyImpl.create;

/**
 * Plugin hook keys which are only intended for usage inside the core with the
 * intent that their implementations will be registered.
 */
public interface CorePluginHookKey extends PluginHookKey<PluginHook<?>> {

    @ApiStatus.Internal
    PluginHookKey<CoreHeadDatabaseHook> CORE_HEAD_DATABASE = create(CoreHeadDatabaseHook.class);
    @ApiStatus.Internal
    PluginHookKey<CoreNexoHook> CORE_NEXO = create(CoreNexoHook.class);
    @ApiStatus.Internal
    PluginHookKey<CoreItemsAdderHook> CORE_ITEMS_ADDER = create(CoreItemsAdderHook.class);
    @ApiStatus.Internal
    PluginHookKey<CoreModelEngineHook> CORE_MODEL_ENGINE = create(CoreModelEngineHook.class);
    @ApiStatus.Internal
    PluginHookKey<CorePapiHook> CORE_PAPI = create(CorePapiHook.class);
    @ApiStatus.Internal
    PluginHookKey<CoreMythicMobsHook> CORE_MYTHIC_MOBS = create(CoreMythicMobsHook.class);
}

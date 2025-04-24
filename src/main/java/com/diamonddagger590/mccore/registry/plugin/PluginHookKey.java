package com.diamonddagger590.mccore.registry.plugin;

import com.diamonddagger590.mccore.external.headdatabase.HeadDatabaseHook;
import com.diamonddagger590.mccore.external.itemsadder.ItemsAdderHook;
import com.diamonddagger590.mccore.external.nexo.NexoHook;
import com.diamonddagger590.mccore.external.papi.PapiHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import static com.diamonddagger590.mccore.registry.plugin.PluginHookKeyImpl.create;

/**
 * A key that allows access to a {@link PluginHook} through the {@link PluginHookRegistry}.
 * <p>
 * To access the hook, users will need to call {@link RegistryAccess#registryAccess()} and provide
 * {@link com.diamonddagger590.mccore.registry.RegistryKey#PLUGIN_HOOK} to get back the {@link PluginHookRegistry}.
 * <p>
 * From there, users can call {@link PluginHookRegistry#pluginHook(PluginHookKey)} to get the plugin hook belonging to the
 * provided key.
 *
 * @param <P> The {@link PluginHook} being represented by this key.
 */
public interface PluginHookKey<P extends PluginHook> {

    PluginHookKey<HeadDatabaseHook> HEAD_DATABASE = create(HeadDatabaseHook.class);
    PluginHookKey<NexoHook> NEXO = create(NexoHook.class);
    PluginHookKey<ItemsAdderHook> ITEMS_ADDER = create(ItemsAdderHook.class);

    @ApiStatus.Internal
    PluginHookKey<PapiHook> CORE_PAPI = create(PapiHook.class);


    /**
     * Gets the {@link Class} of the {@link PluginHook} represented by this key.
     *
     * @return The {@link Class} of the {@link PluginHook} represented by this key.
     */
    @NotNull
    Class<P> hookClass();
}

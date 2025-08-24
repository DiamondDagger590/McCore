package com.diamonddagger590.mccore.bootstrap.registrar;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.bootstrap.BootstrapContext;
import com.diamonddagger590.mccore.command.CoreCommandManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

public class CommandRegistrar<P extends CorePlugin> implements Registrar<P> {

    @Override
    public void register(@NotNull BootstrapContext<P> context) {
        RegistryAccess registryAccess = context.plugin().registryAccess();
        registryAccess.registry(RegistryKey.MANAGER).register(new CoreCommandManager(context.plugin()));

    }
}

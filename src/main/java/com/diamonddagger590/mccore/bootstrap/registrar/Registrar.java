package com.diamonddagger590.mccore.bootstrap.registrar;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.bootstrap.BootstrapContext;
import org.jetbrains.annotations.NotNull;

public interface Registrar<P extends CorePlugin> {

    void register(@NotNull BootstrapContext<P> context);
}

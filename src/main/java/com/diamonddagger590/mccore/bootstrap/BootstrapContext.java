package com.diamonddagger590.mccore.bootstrap;

import com.diamonddagger590.mccore.CorePlugin;
import org.jetbrains.annotations.NotNull;

public record BootstrapContext<P extends CorePlugin>(@NotNull P plugin, @NotNull StartupProfile startupProfile) {
}

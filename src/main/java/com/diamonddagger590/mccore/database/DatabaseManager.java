package com.diamonddagger590.mccore.database;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.manager.Manager;
import org.jetbrains.annotations.NotNull;

public abstract class DatabaseManager<CP extends CorePlugin> extends Manager<CP> {

    public DatabaseManager(@NotNull CP plugin) {
        super(plugin);
    }

    @NotNull
    public abstract Database getDatabase();
}

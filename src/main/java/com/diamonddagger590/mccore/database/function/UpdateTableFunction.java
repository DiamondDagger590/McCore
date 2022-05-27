package com.diamonddagger590.mccore.database.function;

import com.diamonddagger590.mccore.database.DatabaseManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface UpdateTableFunction {

    @NotNull
    public CompletableFuture<Void> updateTables(@NotNull DatabaseManager databaseManager);
}

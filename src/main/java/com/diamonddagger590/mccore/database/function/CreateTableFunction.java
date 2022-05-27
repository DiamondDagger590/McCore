package com.diamonddagger590.mccore.database.function;

import com.diamonddagger590.mccore.database.DatabaseManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface CreateTableFunction {

    @NotNull
    public CompletableFuture<Void> createTables(@NotNull DatabaseManager databaseManager);
}

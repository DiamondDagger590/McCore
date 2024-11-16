package com.diamonddagger590.mccore.database.function;

import com.diamonddagger590.mccore.database.Database;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * This function allows for the creation of database tables.
 */
@FunctionalInterface
public interface CreateTableFunction {

    /**
     * Gets a {@link CompletableFuture} that is completed whenever the table creations are finished.
     *
     * @param database The {@link Database} running this function.
     * @return A {@link CompletableFuture} that is completed whenever the table creations are finished.
     */
    @NotNull
    CompletableFuture<Void> createTables(@NotNull Database database);
}

package com.diamonddagger590.mccore.database.function;

import com.diamonddagger590.mccore.database.Database;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * This function allows for updating a database table from different versions to the latest
 * version of that table.
 */
@FunctionalInterface
public interface UpdateTableFunction {

    /**
     * Gets a {@link CompletableFuture} that is completed whenever the table updates are finished.
     *
     * @param database The {@link Database} running this function.
     * @return A {@link CompletableFuture} that is completed whenever the table updates are finished.
     */
    @NotNull
    CompletableFuture<Void> updateTables(@NotNull Database database);
}

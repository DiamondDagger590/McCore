package com.diamonddagger590.mccore.database.table.function;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.function.UpdateTableFunction;
import com.diamonddagger590.mccore.database.table.impl.MutexDAO;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This function will be called before any other {@link UpdateTableFunction UpdateTableFunctions} to ensure
 * that core utility tables are updated first, thus allowing plugins to rely on tables from this plugin
 * existing before their tables are updated.
 */
public class UpdateCoreTablesFunction {

    private static final UpdateTableFunction updateCoreTablesFunction = (databaseManager -> {

        CompletableFuture<Void> returnFuture = new CompletableFuture<>();
        Logger logger = CorePlugin.getInstance().getLogger();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            if (databaseManager.getDatabase() == null) {
                logger.log(Level.SEVERE, "Database Update - Table Version History DAO was unable to be updated as the database is null. Please report this instance to the developer.");
                returnFuture.complete(null);
                return;
            }

            Connection connection = databaseManager.getDatabase().getConnection();

            TableVersionHistoryDAO.updateTable(connection)
                .thenAccept(unused -> {

                    MutexDAO.updateTable(connection).thenAccept(unused1 -> {
                        returnFuture.complete(null);
                    });
                });
        });

        return returnFuture;
    });

    /**
     * Gets the {@link UpdateTableFunction} that updates all the core database tables.
     *
     * @return The {@link UpdateTableFunction} that updates all the core database tables.
     */

    @NotNull
    public static UpdateTableFunction getUpdateCoreTablesFunction() {
        return updateCoreTablesFunction;
    }
}

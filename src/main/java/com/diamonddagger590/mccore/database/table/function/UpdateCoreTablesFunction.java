package com.diamonddagger590.mccore.database.table.function;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.function.UpdateTableFunction;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateCoreTablesFunction {

    private static final UpdateTableFunction updateCoreTablesFunction = (databaseManager -> {

        CompletableFuture<Void> returnFuture = new CompletableFuture<>();
        Logger logger = CorePlugin.getInstance().getLogger();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            if (databaseManager.getDatabase() == null) {

                if (databaseManager.getDatabase() == null) {
                    logger.log(Level.SEVERE, "Database Update - Table Version History DAO was unable to be updated as the database is null. Please report this instance to the developer.");
                    returnFuture.complete(null);
                    return;
                }

                TableVersionHistoryDAO.updateTable(databaseManager.getDatabase().getConnection())
                    .thenAccept(unused -> {
                        returnFuture.complete(null);
                    });
            }


        });

        return returnFuture;
    });

    @NotNull
    public static UpdateTableFunction getUpdateCoreTablesFunction() {
        return updateCoreTablesFunction;
    }
}

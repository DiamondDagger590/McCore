package com.diamonddagger590.mccore.database.table.function;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.DatabaseManager;
import com.diamonddagger590.mccore.database.function.CreateTableFunction;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateCoreTablesFunction {

    private static final CreateTableFunction createCoreTablesFunction = (@NotNull DatabaseManager databaseManager) -> {

        CompletableFuture<Void> returnFuture = new CompletableFuture<>();
        Logger logger = CorePlugin.getInstance().getLogger();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            if (databaseManager.getDatabase() == null) {
                logger.log(Level.SEVERE, "Database Creation - Table Version History DAO was unable to be created as the database is null. Please report this instance to the developer.");
                returnFuture.complete(null);
                return;
            }

            TableVersionHistoryDAO.attemptCreateTable(databaseManager.getDatabase().getConnection(), databaseManager)
                .thenAccept(tableVersionHistoryTableCreated -> {

                    logger.log(Level.INFO, "Database Creation - Table Version History DAO "
                                               + (tableVersionHistoryTableCreated ? "created a new table." : "already existed so skipping creation."));
                    returnFuture.complete(null);

                }).exceptionally(throwable -> {
                    returnFuture.completeExceptionally(throwable);
                    return null;
                });

        });


        return returnFuture;
    };

    @NotNull
    public static CreateTableFunction getCreateCoreTablesFunction() {
        return createCoreTablesFunction;
    }
}

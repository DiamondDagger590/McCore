package com.diamonddagger590.mccore.database.table.function;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.DatabaseManager;
import com.diamonddagger590.mccore.database.function.CreateTableFunction;
import com.diamonddagger590.mccore.database.table.impl.MutexDAO;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This function will be called before any other {@link CreateTableFunction CreateTableFunctions} to ensure
 * that core utility tables are created first, thus allowing plugins to rely on tables from this plugin
 * existing before their tables are created.
 */
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

            Connection connection = databaseManager.getDatabase().getConnection();

            TableVersionHistoryDAO.attemptCreateTable(connection, databaseManager)
                .thenAccept(tableVersionHistoryTableCreated -> {

                    logger.log(Level.INFO, "Database Creation - Table Version History DAO "
                                               + (tableVersionHistoryTableCreated ? "created a new table." : "already existed so skipping creation."));

                    MutexDAO.attemptCreateTable(connection, databaseManager).thenAccept(mutexTableCreated -> {
                        logger.log(Level.INFO, "Database Creation - Mutex DAO "
                                                   + (tableVersionHistoryTableCreated ? "created a new table." : "already existed so skipping creation."));
                        returnFuture.complete(null);
                    }).exceptionally(throwable -> {
                        returnFuture.completeExceptionally(throwable);
                        return null;
                    });

                }).exceptionally(throwable -> {
                    returnFuture.completeExceptionally(throwable);
                    return null;
                });

        });


        return returnFuture;
    };

    /**
     * Gets the {@link CreateTableFunction} that creates all the core database tables.
     *
     * @return The {@link CreateTableFunction} that creates all the core database tables.
     */
    @NotNull
    public static CreateTableFunction getCreateCoreTablesFunction() {
        return createCoreTablesFunction;
    }
}

package com.diamonddagger590.mccore.database.table.function;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.function.CreateTableFunction;
import com.diamonddagger590.mccore.database.table.impl.MutexDAO;
import com.diamonddagger590.mccore.database.table.impl.PlayerSettingDAO;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This function will be called before any other {@link CreateTableFunction CreateTableFunctions} to ensure
 * that core utility tables are created first, thus allowing plugins to rely on tables from this plugin
 * existing before their tables are created.
 */
public class CreateCoreTablesFunction {

    private static final CreateTableFunction createCoreTablesFunction = database -> {
        CompletableFuture<Void> returnFuture = new CompletableFuture<>();
        Logger logger = CorePlugin.getInstance().getLogger();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                logger.log(Level.INFO, "Database Creation - Table Version History DAO "
                        + (TableVersionHistoryDAO.attemptCreateTable(connection, database) ? "created a new table." : "already existed so skipping creation."));
                logger.log(Level.INFO, "Database Creation - Mutex DAO "
                        + (MutexDAO.attemptCreateTable(connection, database) ? "created a new table." : "already existed so skipping creation."));
                logger.log(Level.INFO, "Database Creation - Player Setting DAO "
                        + (PlayerSettingDAO.attemptCreateTable(connection, database) ? "created a new table." : "already existed so skipping creation."));
                returnFuture.complete(null);
            }
            catch (SQLException e) {
                returnFuture.completeExceptionally(e);
            }
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

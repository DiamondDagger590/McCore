package com.diamonddagger590.mccore.database.table.function;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.function.UpdateTableFunction;
import com.diamonddagger590.mccore.database.table.impl.MutexDAO;
import com.diamonddagger590.mccore.database.table.impl.PlayerSettingDAO;
import com.diamonddagger590.mccore.database.table.impl.PlayerStatisticDAO;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * This function will be called before any other {@link UpdateTableFunction UpdateTableFunctions} to ensure
 * that core utility tables are updated first, thus allowing plugins to rely on tables from this plugin
 * existing before their tables are updated.
 */
public class UpdateCoreTablesFunction {

    private static final UpdateTableFunction updateCoreTablesFunction = (database -> {
        CompletableFuture<Void> returnFuture = new CompletableFuture<>();
        Logger logger = CorePlugin.getInstance().getLogger();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                TableVersionHistoryDAO.updateTable(connection);
                MutexDAO.updateTable(connection);
                PlayerSettingDAO.updateTable(connection);
                PlayerStatisticDAO.updateTable(connection);
                returnFuture.complete(null);
            }
            catch (SQLException e) {
                e.printStackTrace();
                returnFuture.completeExceptionally(e);
            }
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

package com.diamonddagger590.mccore.database.transaction;

import com.diamonddagger590.mccore.CorePlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * A batch transaction represents a transaction where we don't care about
 * if any particular statement failed or succeeded.
 * <p>
 * Any {@link PreparedStatement}s that succeed, while any that fail will
 * have errors thrown and logged.
 */
public class BatchTransaction extends Transaction {

    public BatchTransaction(@NotNull Connection connection) {
        super(connection);
    }

    public BatchTransaction(@NotNull Connection connection, @NotNull List<PreparedStatement> preparedStatements) {
        super(connection, preparedStatements);
    }

    @Override
    public void executeTransaction() {
        Connection connection = getConnection();
        Logger logger = CorePlugin.getInstance().getLogger();
        // Try to execute each statement
        try {
            connection.setAutoCommit(false);
            for (PreparedStatement preparedStatement : getPreparedStatements()) {
                try (preparedStatement) {
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    logger.severe("Encountered an error while executing a statement for a batch transaction. Failing statement: " + preparedStatement);
                    logger.severe(e.getMessage());
                }
            }
            // Commit everything
            connection.commit();
        } catch (SQLException e) {
            logger.severe("Encountered an exception trying to execute a batch transaction.");
            logger.severe(e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                logger.severe("Encountered an exception trying to set autocommit to true.");
                logger.severe(e.getMessage());
                // Close out the connection if we fail
                try {
                    connection.close();
                } catch (SQLException closeException) {
                    logger.severe("Encountered an exception trying to close connection.");
                    logger.severe(closeException.getMessage());
                }
            }
        }
    }
}

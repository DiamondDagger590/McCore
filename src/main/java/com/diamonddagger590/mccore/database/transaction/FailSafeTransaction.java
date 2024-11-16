package com.diamonddagger590.mccore.database.transaction;

import com.diamonddagger590.mccore.CorePlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * A fail-safe transaction requires all {@link PreparedStatement}s
 * to succeed in order for any of them to be committed.
 * <p>
 * If a single statement fails, then the transaction will instead roll back.
 */
public class FailSafeTransaction extends Transaction {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FailSafeTransaction.class);

    public FailSafeTransaction(@NotNull Connection connection) {
        super(connection);
    }

    public FailSafeTransaction(@NotNull Connection connection, @NotNull List<PreparedStatement> preparedStatements) {
        super(connection, preparedStatements);
    }

    @Override
    public void executeTransaction() {
        Connection connection = getConnection();
        Logger logger = CorePlugin.getInstance().getLogger();
        try {
            // Set auto commit to false, attempt to execute all updates and only commit if there are no issues
            connection.setAutoCommit(false);
            for (PreparedStatement preparedStatement : getPreparedStatements()) {
                try (preparedStatement) {
                    preparedStatement.executeUpdate();
                }
            }
            connection.commit();
        } catch (SQLException e) {
            logger.severe("Encountered an exception while executing a fail safe transaction... rolling back.");
            logger.severe(e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                logger.severe("Encountered an exception while rolling back transaction...");
                logger.severe(rollbackException.getMessage());
            }
        }
    }
}

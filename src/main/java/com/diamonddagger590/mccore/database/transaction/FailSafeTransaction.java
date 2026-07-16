package com.diamonddagger590.mccore.database.transaction;

import com.diamonddagger590.mccore.CorePlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A fail-safe transaction requires all {@link PreparedStatement}s
 * to succeed in order for any of them to be committed.
 * <p>
 * If a single statement fails, then the transaction will instead roll back.
 * <p>
 * After execution, callers can inspect the outcome via {@link #getTransactionState()}
 * and retrieve any failure cause via {@link #getFailureCause()}.
 */
public class FailSafeTransaction extends Transaction {

    private volatile TransactionState transactionState = TransactionState.PENDING;
    private volatile SQLException failureCause;

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
                catch (SQLException ex) {
                    logger.severe("Failing statement: " + preparedStatement.toString());
                    throw new SQLException(ex);
                }
            }
            connection.commit();
            transactionState = TransactionState.COMMITTED;
        } catch (SQLException e) {
            transactionState = TransactionState.ROLLED_BACK;
            failureCause = e;
            logger.severe("Encountered an exception while executing a fail safe transaction... rolling back.");
            logger.log(Level.SEVERE, e.getMessage(), e);
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                logger.severe("Encountered an exception while rolling back transaction...");
                logger.log(Level.SEVERE, rollbackException.getMessage(), rollbackException);
            }
        }
        finally {
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

    /**
     * Gets the current state of this transaction.
     *
     * @return The {@link TransactionState} representing the outcome of this transaction.
     */
    @NotNull
    public TransactionState getTransactionState() {
        return transactionState;
    }

    /**
     * Gets the {@link SQLException} that caused this transaction to roll back, if any.
     *
     * @return An {@link Optional} containing the failure cause, or empty if the transaction
     *         has not failed.
     */
    @NotNull
    public Optional<SQLException> getFailureCause() {
        return Optional.ofNullable(failureCause);
    }
}

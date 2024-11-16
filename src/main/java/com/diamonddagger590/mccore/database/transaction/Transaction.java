package com.diamonddagger590.mccore.database.transaction;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * A transaction represents an ordered collection of {@link PreparedStatement}s that need to be executed
 * in a particular order.
 * <p>
 * These statements will only be committed to the {@link Connection} after all have been executed. Specific
 * implementations of a transaction may have differing contracts on ensuring that all statements executed.
 */
public abstract class Transaction {

    private final Connection connection;
    private final List<PreparedStatement> preparedStatements;

    public Transaction(@NotNull Connection connection) {
        this.connection = connection;
        this.preparedStatements = new ArrayList<>();
    }

    public Transaction(@NotNull Connection connection, @NotNull List<PreparedStatement> preparedStatements) {
        this.connection = connection;
        this.preparedStatements = preparedStatements;
    }

    /**
     * Adds the provided {@link PreparedStatement} to this transaction.
     *
     * @param preparedStatement The {@link PreparedStatement} to add to this transaction.
     */
    public void add(@NotNull PreparedStatement preparedStatement) {
        preparedStatements.add(preparedStatement);
    }

    /**
     * Adds the provided {@link List} of {@link PreparedStatement}s to this transaction.
     *
     * @param preparedStatements The {@link List} of {@link PreparedStatement}s to add to the transaction.
     */
    public void addAll(@NotNull List<PreparedStatement> preparedStatements) {
        this.preparedStatements.addAll(preparedStatements);
    }

    /**
     * Gets a {@link List} of {@link PreparedStatement}s in this transaction.
     *
     * @return An {@link ImmutableList} pf {@link PreparedStatement}s in this transaction.
     */
    @NotNull
    public List<PreparedStatement> getPreparedStatements() {
        return ImmutableList.copyOf(preparedStatements);
    }

    /**
     * Gets the {@link Connection} that this transaction will be executed against.
     *
     * @return The {@link Connection} that this transaction will be executed against.
     */
    @NotNull
    public Connection getConnection() {
        return connection;
    }

    /**
     * Executes this transaction and commits changes to the {@link #getConnection() Connection}.
     */
    public abstract void executeTransaction();
}

package com.diamonddagger590.mccore.database.transaction;

import com.diamonddagger590.mccore.testing.CorePluginMockExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class BatchTransactionTest {

    private Connection connection;

    @BeforeEach
    void setUp() {
        connection = mock(Connection.class);
        CorePluginMockExtension.injectMockPlugin("BatchTransactionTest");
    }

    @AfterEach
    void tearDown() {
        CorePluginMockExtension.clearMockPlugin();
    }

    @Test
    @DisplayName("Given a connection, when constructing with single-arg constructor, then transaction has no statements")
    void constructor_createsEmptyTransaction_whenSingleArgUsed() {
        var transaction = new BatchTransaction(connection);
        assertNotNull(transaction);
        assertEquals(connection, transaction.getConnection());
        assertEquals(0, transaction.getPreparedStatements().size());
    }

    @Test
    @DisplayName("Given a connection and statement list, when constructing with two-arg constructor, then transaction contains those statements")
    void constructor_containsStatements_whenTwoArgUsed() throws SQLException {
        PreparedStatement ps1 = mock(PreparedStatement.class);
        PreparedStatement ps2 = mock(PreparedStatement.class);
        var transaction = new BatchTransaction(connection, List.of(ps1, ps2));
        assertEquals(2, transaction.getPreparedStatements().size());
    }

    @Test
    @DisplayName("Given valid statements, when executing transaction, then all statements are committed")
    void executeTransaction_commitsAllStatements_whenAllSucceed() throws SQLException {
        PreparedStatement ps1 = mock(PreparedStatement.class);
        PreparedStatement ps2 = mock(PreparedStatement.class);

        var transaction = new BatchTransaction(connection, List.of(ps1, ps2));
        transaction.executeTransaction();

        verify(connection).setAutoCommit(false);
        verify(ps1).executeUpdate();
        verify(ps2).executeUpdate();
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
    }

    @Test
    @DisplayName("Given a failing first statement, when executing transaction, then remaining statements still execute and commit")
    void executeTransaction_continuesAndCommits_whenStatementFails() throws SQLException {
        PreparedStatement ps1 = mock(PreparedStatement.class);
        PreparedStatement ps2 = mock(PreparedStatement.class);
        doThrow(new SQLException("ps1 failed")).when(ps1).executeUpdate();

        var transaction = new BatchTransaction(connection, List.of(ps1, ps2));
        assertDoesNotThrow(transaction::executeTransaction);

        verify(ps2).executeUpdate();
        verify(connection).commit();
    }

    @Test
    @DisplayName("Given a successful execution, when autocommit reset fails, then connection is closed")
    void executeTransaction_closesConnection_whenAutoCommitResetFails() throws SQLException {
        PreparedStatement ps = mock(PreparedStatement.class);
        doNothing().when(connection).setAutoCommit(false);
        doThrow(new SQLException("autocommit fail")).when(connection).setAutoCommit(true);

        var transaction = new BatchTransaction(connection, List.of(ps));
        assertDoesNotThrow(transaction::executeTransaction);

        verify(connection).close();
    }

    @Test
    @DisplayName("Given autocommit reset failure, when connection close also fails, then no exception propagates")
    void executeTransaction_doesNotThrow_whenAutoCommitResetAndCloseBothFail() throws SQLException {
        PreparedStatement ps = mock(PreparedStatement.class);
        doNothing().when(connection).setAutoCommit(false);
        doThrow(new SQLException("autocommit fail")).when(connection).setAutoCommit(true);
        doThrow(new SQLException("close fail")).when(connection).close();

        var transaction = new BatchTransaction(connection, List.of(ps));
        assertDoesNotThrow(transaction::executeTransaction);
    }

    @Test
    @DisplayName("Given initial setAutoCommit(false) fails, when executing transaction, then commit is never called and no rollback occurs")
    void executeTransaction_skipsCommitAndRollback_whenSetAutoCommitFalseFails() throws SQLException {
        doThrow(new SQLException("initial autocommit fail")).when(connection).setAutoCommit(false);

        var transaction = new BatchTransaction(connection);
        assertDoesNotThrow(transaction::executeTransaction);

        verify(connection, never()).commit();
        verify(connection, never()).rollback();
    }

    @Test
    @DisplayName("Given no statements, when executing transaction, then commit succeeds on empty batch")
    void executeTransaction_commitsEmptyBatch_whenNoStatementsAdded() throws SQLException {
        var transaction = new BatchTransaction(connection);
        assertDoesNotThrow(transaction::executeTransaction);

        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
    }
}

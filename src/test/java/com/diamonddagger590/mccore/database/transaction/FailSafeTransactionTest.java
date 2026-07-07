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

class FailSafeTransactionTest {

    private Connection connection;

    @BeforeEach
    void setUp() {
        connection = mock(Connection.class);
        CorePluginMockExtension.injectMockPlugin("FailSafeTransactionTest");
    }

    @AfterEach
    void tearDown() {
        CorePluginMockExtension.clearMockPlugin();
    }

    @Test
    @DisplayName("Given a connection, when constructing with single-arg constructor, then transaction has no statements")
    void constructor_createsEmptyTransaction_whenSingleArgUsed() {
        var transaction = new FailSafeTransaction(connection);
        assertNotNull(transaction);
        assertEquals(connection, transaction.getConnection());
        assertEquals(0, transaction.getPreparedStatements().size());
    }

    @Test
    @DisplayName("Given a connection and statement list, when constructing with two-arg constructor, then transaction contains those statements")
    void constructor_containsStatements_whenTwoArgUsed() {
        PreparedStatement ps1 = mock(PreparedStatement.class);
        PreparedStatement ps2 = mock(PreparedStatement.class);
        var transaction = new FailSafeTransaction(connection, List.of(ps1, ps2));
        assertEquals(2, transaction.getPreparedStatements().size());
    }

    @Test
    @DisplayName("Given valid statements, when executing transaction, then all statements are committed")
    void executeTransaction_commitsAllStatements_whenAllSucceed() throws SQLException {
        PreparedStatement ps1 = mock(PreparedStatement.class);
        PreparedStatement ps2 = mock(PreparedStatement.class);

        var transaction = new FailSafeTransaction(connection, List.of(ps1, ps2));
        transaction.executeTransaction();

        verify(connection).setAutoCommit(false);
        verify(ps1).executeUpdate();
        verify(ps2).executeUpdate();
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
    }

    @Test
    @DisplayName("Given a failing first statement, when executing transaction, then transaction rolls back without executing remaining statements")
    void executeTransaction_rollsBack_whenStatementFails() throws SQLException {
        PreparedStatement ps1 = mock(PreparedStatement.class);
        PreparedStatement ps2 = mock(PreparedStatement.class);
        doThrow(new SQLException("ps1 failed")).when(ps1).executeUpdate();

        var transaction = new FailSafeTransaction(connection, List.of(ps1, ps2));
        assertDoesNotThrow(transaction::executeTransaction);

        verify(ps2, never()).executeUpdate();
        verify(connection, never()).commit();
        verify(connection).rollback();
    }

    @Test
    @DisplayName("Given a statement failure and rollback failure, when executing transaction, then no exception propagates")
    void executeTransaction_doesNotThrow_whenRollbackAlsoFails() throws SQLException {
        PreparedStatement ps = mock(PreparedStatement.class);
        doThrow(new SQLException("statement failed")).when(ps).executeUpdate();
        doThrow(new SQLException("rollback failed")).when(connection).rollback();

        var transaction = new FailSafeTransaction(connection, List.of(ps));
        assertDoesNotThrow(transaction::executeTransaction);
    }

    @Test
    @DisplayName("Given a successful execution, when autocommit reset fails, then connection is closed")
    void executeTransaction_closesConnection_whenAutoCommitResetFails() throws SQLException {
        PreparedStatement ps = mock(PreparedStatement.class);
        doNothing().when(connection).setAutoCommit(false);
        doThrow(new SQLException("autocommit fail")).when(connection).setAutoCommit(true);

        var transaction = new FailSafeTransaction(connection, List.of(ps));
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

        var transaction = new FailSafeTransaction(connection, List.of(ps));
        assertDoesNotThrow(transaction::executeTransaction);
    }

    @Test
    @DisplayName("Given no statements, when executing transaction, then commit succeeds and no rollback occurs")
    void executeTransaction_commitsSuccessfully_whenNoStatementsAdded() throws SQLException {
        var transaction = new FailSafeTransaction(connection);
        assertDoesNotThrow(transaction::executeTransaction);

        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
        verify(connection, never()).rollback();
    }

    @Test
    @DisplayName("Given initial setAutoCommit(false) fails, when executing transaction, then rollback is called and commit is skipped")
    void executeTransaction_rollsBackWithoutCommit_whenSetAutoCommitFalseFails() throws SQLException {
        doThrow(new SQLException("initial autocommit fail")).when(connection).setAutoCommit(false);

        var transaction = new FailSafeTransaction(connection);
        assertDoesNotThrow(transaction::executeTransaction);

        verify(connection, never()).commit();
        verify(connection).rollback();
    }

    @Test
    @DisplayName("Given three statements where the second fails, when executing transaction, then first executes, third is skipped, and rollback occurs")
    void executeTransaction_rollsBackAfterPartialExecution_whenMiddleStatementFails() throws SQLException {
        PreparedStatement ps1 = mock(PreparedStatement.class);
        PreparedStatement ps2 = mock(PreparedStatement.class);
        PreparedStatement ps3 = mock(PreparedStatement.class);
        doThrow(new SQLException("ps2 failed")).when(ps2).executeUpdate();

        var transaction = new FailSafeTransaction(connection, List.of(ps1, ps2, ps3));
        assertDoesNotThrow(transaction::executeTransaction);

        verify(ps1).executeUpdate();
        verify(ps2).executeUpdate();
        verify(ps3, never()).executeUpdate();
        verify(connection).rollback();
        verify(connection, never()).commit();
    }
}

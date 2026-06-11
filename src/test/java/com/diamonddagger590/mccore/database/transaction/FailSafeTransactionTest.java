package com.diamonddagger590.mccore.database.transaction;

import com.diamonddagger590.mccore.CorePlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FailSafeTransactionTest {

    private Connection connection;
    private Logger logger;

    @BeforeEach
    void setUp() throws Exception {
        connection = mock(Connection.class);
        logger = Logger.getLogger("FailSafeTransactionTest");

        CorePlugin mockPlugin = mock(CorePlugin.class);
        when(mockPlugin.getLogger()).thenReturn(logger);
        Field instanceField = CorePlugin.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, mockPlugin);
    }

    @AfterEach
    void tearDown() throws Exception {
        Field instanceField = CorePlugin.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @Test
    void singleArgConstructorCreatesEmptyTransaction() {
        var transaction = new FailSafeTransaction(connection);
        assertNotNull(transaction);
        assertEquals(connection, transaction.getConnection());
        assertEquals(0, transaction.getPreparedStatements().size());
    }

    @Test
    void twoArgConstructorAcceptsStatements() {
        PreparedStatement ps1 = mock(PreparedStatement.class);
        PreparedStatement ps2 = mock(PreparedStatement.class);
        var transaction = new FailSafeTransaction(connection, List.of(ps1, ps2));
        assertEquals(2, transaction.getPreparedStatements().size());
    }

    @Test
    void executeTransactionCommitsAllStatementsOnSuccess() throws SQLException {
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
    void executeTransactionRollsBackOnStatementFailure() throws SQLException {
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
    void executeTransactionHandlesRollbackFailure() throws SQLException {
        PreparedStatement ps = mock(PreparedStatement.class);
        doThrow(new SQLException("statement failed")).when(ps).executeUpdate();
        doThrow(new SQLException("rollback failed")).when(connection).rollback();

        var transaction = new FailSafeTransaction(connection, List.of(ps));
        assertDoesNotThrow(transaction::executeTransaction);
    }

    @Test
    void executeTransactionHandlesAutoCommitResetFailure() throws SQLException {
        PreparedStatement ps = mock(PreparedStatement.class);
        doNothing().when(connection).setAutoCommit(false);
        doThrow(new SQLException("autocommit fail")).when(connection).setAutoCommit(true);

        var transaction = new FailSafeTransaction(connection, List.of(ps));
        assertDoesNotThrow(transaction::executeTransaction);

        verify(connection).close();
    }

    @Test
    void executeTransactionHandlesAutoCommitResetAndCloseFailure() throws SQLException {
        PreparedStatement ps = mock(PreparedStatement.class);
        doNothing().when(connection).setAutoCommit(false);
        doThrow(new SQLException("autocommit fail")).when(connection).setAutoCommit(true);
        doThrow(new SQLException("close fail")).when(connection).close();

        var transaction = new FailSafeTransaction(connection, List.of(ps));
        assertDoesNotThrow(transaction::executeTransaction);
    }

    @Test
    void executeTransactionWithEmptyStatementListCommitsSuccessfully() throws SQLException {
        var transaction = new FailSafeTransaction(connection);
        assertDoesNotThrow(transaction::executeTransaction);

        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
        verify(connection, never()).rollback();
    }

    @Test
    void executeTransactionHandlesSetAutoCommitFalseFailure() throws SQLException {
        doThrow(new SQLException("initial autocommit fail")).when(connection).setAutoCommit(false);

        var transaction = new FailSafeTransaction(connection);
        assertDoesNotThrow(transaction::executeTransaction);

        verify(connection, never()).commit();
        verify(connection).rollback();
    }

    @Test
    void secondStatementFailureCausesRollback() throws SQLException {
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

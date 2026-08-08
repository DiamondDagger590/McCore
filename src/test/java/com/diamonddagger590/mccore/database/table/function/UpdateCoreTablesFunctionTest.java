package com.diamonddagger590.mccore.database.table.function;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.function.UpdateTableFunction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class UpdateCoreTablesFunctionTest {

    private MockedStatic<CorePlugin> corePluginMock;
    private CorePlugin mockPlugin;
    private ThreadPoolExecutor executor;

    @BeforeEach
    void setUp() {
        mockPlugin = mock(CorePlugin.class);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("TestUpdateCoreTables"));

        corePluginMock = mockStatic(CorePlugin.class);
        corePluginMock.when(CorePlugin::getInstance).thenReturn(mockPlugin);

        executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    @AfterEach
    void tearDown() {
        corePluginMock.close();
        executor.shutdownNow();
    }

    @Test
    @DisplayName("Given no prior invocation, when getting the update core tables function, then returns a non-null instance")
    void getUpdateCoreTablesFunction_returnsNonNull_whenCalled() {
        UpdateTableFunction function = UpdateCoreTablesFunction.getUpdateCoreTablesFunction();
        assertNotNull(function);
    }

    @Test
    @DisplayName("Given the function has already been retrieved, when getting the update core tables function again, then returns the same instance")
    void getUpdateCoreTablesFunction_returnsSameInstance_whenCalledMultipleTimes() {
        UpdateTableFunction func1 = UpdateCoreTablesFunction.getUpdateCoreTablesFunction();
        UpdateTableFunction func2 = UpdateCoreTablesFunction.getUpdateCoreTablesFunction();
        assertSame(func1, func2);
    }

    @Test
    @DisplayName("Given a valid database with mocked connection, when updating tables, then completes successfully without exception")
    void updateTables_completesSuccessfully_whenDatabaseIsValid() throws Exception {
        Database mockDatabase = mock(Database.class);
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockDatabase.getDatabaseExecutorService()).thenReturn(executor);
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockStatement.executeUpdate()).thenReturn(1);
        when(mockResultSet.next()).thenReturn(false);

        UpdateTableFunction function = UpdateCoreTablesFunction.getUpdateCoreTablesFunction();

        CompletableFuture<Void> result = function.updateTables(mockDatabase);
        assertNotNull(result);

        result.get(5, TimeUnit.SECONDS);
        assertTrue(result.isDone());
        assertFalse(result.isCompletedExceptionally());
    }

    @Test
    @DisplayName("Given connection close throws SQLException, when updating tables, then the catch block handles it gracefully")
    void updateTables_handlesSQLException_whenConnectionCloseThrows() throws Exception {
        Database mockDatabase = mock(Database.class);
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockDatabase.getDatabaseExecutorService()).thenReturn(executor);
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockStatement.executeUpdate()).thenReturn(1);
        when(mockResultSet.next()).thenReturn(false);
        doThrow(new SQLException("Close failed")).when(mockConnection).close();

        UpdateTableFunction function = UpdateCoreTablesFunction.getUpdateCoreTablesFunction();

        CompletableFuture<Void> result = function.updateTables(mockDatabase);
        assertNotNull(result);

        result.get(5, TimeUnit.SECONDS);
        assertTrue(result.isDone());
    }
}

package com.diamonddagger590.mccore.database.table.function;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.function.UpdateTableFunction;
import com.diamonddagger590.mccore.database.table.impl.MutexDAO;
import com.diamonddagger590.mccore.database.table.impl.PlayerSettingDAO;
import com.diamonddagger590.mccore.database.table.impl.PlayerStatisticDAO;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import com.diamonddagger590.mccore.testing.CallerRunsExecutor;
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

        executor = new CallerRunsExecutor();
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
    @DisplayName("Given a connection that throws on close, when updating tables, then the catch block executes")
    void updateTables_executesCatchBlock_whenConnectionCloseThrowsSQLException() throws Exception {
        Database mockDatabase = mock(Database.class);
        Connection mockConnection = mock(Connection.class);

        when(mockDatabase.getDatabaseExecutorService()).thenReturn(executor);
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        doThrow(new SQLException("Close failed")).when(mockConnection).close();

        try (MockedStatic<TableVersionHistoryDAO> tvhMock = mockStatic(TableVersionHistoryDAO.class);
             MockedStatic<MutexDAO> mutexMock = mockStatic(MutexDAO.class);
             MockedStatic<PlayerSettingDAO> psMock = mockStatic(PlayerSettingDAO.class);
             MockedStatic<PlayerStatisticDAO> pstMock = mockStatic(PlayerStatisticDAO.class)) {

            UpdateTableFunction function = UpdateCoreTablesFunction.getUpdateCoreTablesFunction();

            CompletableFuture<Void> result = function.updateTables(mockDatabase);
            assertNotNull(result);

            // The future was already completed normally before close() threw,
            // so completeExceptionally is a no-op — the future is done, not exceptionally
            assertTrue(result.isDone());
        }
    }

    @Test
    @DisplayName("Given a valid database, when updating tables, then all four DAO updateTable methods are called")
    void updateTables_callsAllDaoUpdateMethods_whenDatabaseIsValid() throws Exception {
        Database mockDatabase = mock(Database.class);
        Connection mockConnection = mock(Connection.class);

        when(mockDatabase.getDatabaseExecutorService()).thenReturn(executor);
        when(mockDatabase.getConnection()).thenReturn(mockConnection);

        try (MockedStatic<TableVersionHistoryDAO> tvhMock = mockStatic(TableVersionHistoryDAO.class);
             MockedStatic<MutexDAO> mutexMock = mockStatic(MutexDAO.class);
             MockedStatic<PlayerSettingDAO> psMock = mockStatic(PlayerSettingDAO.class);
             MockedStatic<PlayerStatisticDAO> pstMock = mockStatic(PlayerStatisticDAO.class)) {

            UpdateTableFunction function = UpdateCoreTablesFunction.getUpdateCoreTablesFunction();

            CompletableFuture<Void> result = function.updateTables(mockDatabase);
            result.get(5, TimeUnit.SECONDS);

            tvhMock.verify(() -> TableVersionHistoryDAO.updateTable(mockConnection));
            mutexMock.verify(() -> MutexDAO.updateTable(mockConnection));
            psMock.verify(() -> PlayerSettingDAO.updateTable(mockConnection));
            pstMock.verify(() -> PlayerStatisticDAO.updateTable(mockConnection));
        }
    }

    @Test
    @DisplayName("Given UpdateCoreTablesFunction class, when instantiated directly, then instance is created")
    void constructor_createsInstance_whenCalledDirectly() {
        UpdateCoreTablesFunction instance = new UpdateCoreTablesFunction();
        assertNotNull(instance);
    }
}

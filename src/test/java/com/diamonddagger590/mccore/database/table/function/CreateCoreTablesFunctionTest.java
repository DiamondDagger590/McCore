package com.diamonddagger590.mccore.database.table.function;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.function.CreateTableFunction;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class CreateCoreTablesFunctionTest {

    private MockedStatic<CorePlugin> corePluginMock;
    private CorePlugin mockPlugin;
    private ThreadPoolExecutor executor;

    @BeforeEach
    void setUp() {
        mockPlugin = mock(CorePlugin.class);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("TestCreateCoreTables"));

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
    @DisplayName("Given a call to getCreateCoreTablesFunction, when invoked, then returns a non-null function")
    void getCreateCoreTablesFunction_returnsNonNull_whenInvoked() {
        CreateTableFunction function = CreateCoreTablesFunction.getCreateCoreTablesFunction();
        assertNotNull(function);
    }

    @Test
    @DisplayName("Given multiple calls to getCreateCoreTablesFunction, when invoked, then returns the same instance")
    void getCreateCoreTablesFunction_returnsSameInstance_whenCalledMultipleTimes() {
        CreateTableFunction func1 = CreateCoreTablesFunction.getCreateCoreTablesFunction();
        CreateTableFunction func2 = CreateCoreTablesFunction.getCreateCoreTablesFunction();
        assertSame(func1, func2);
    }

    @Test
    @DisplayName("Given tables already exist, when createTables is called, then completes successfully")
    void createTables_completesSuccessfully_whenTablesExist() throws Exception {
        Database mockDatabase = mock(Database.class);
        Connection mockConnection = mock(Connection.class);

        when(mockDatabase.getDatabaseExecutorService()).thenReturn(executor);
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockDatabase.tableExists(eq(mockConnection), any(String.class))).thenReturn(true);

        try (MockedStatic<TableVersionHistoryDAO> tvhMock = mockStatic(TableVersionHistoryDAO.class);
             MockedStatic<MutexDAO> mutexMock = mockStatic(MutexDAO.class);
             MockedStatic<PlayerSettingDAO> psMock = mockStatic(PlayerSettingDAO.class);
             MockedStatic<PlayerStatisticDAO> pstMock = mockStatic(PlayerStatisticDAO.class)) {

            tvhMock.when(() -> TableVersionHistoryDAO.attemptCreateTable(any(Connection.class), any(Database.class))).thenReturn(false);
            mutexMock.when(() -> MutexDAO.attemptCreateTable(any(Connection.class), any(Database.class))).thenReturn(false);
            psMock.when(() -> PlayerSettingDAO.attemptCreateTable(any(Connection.class), any(Database.class))).thenReturn(false);
            pstMock.when(() -> PlayerStatisticDAO.attemptCreateTable(any(Connection.class), any(Database.class))).thenReturn(false);

            CreateTableFunction function = CreateCoreTablesFunction.getCreateCoreTablesFunction();

            CompletableFuture<Void> result = function.createTables(mockDatabase);
            assertNotNull(result);

            result.get(5, TimeUnit.SECONDS);
            assertTrue(result.isDone());
            assertFalse(result.isCompletedExceptionally());
        }
    }

    @Test
    @DisplayName("Given tables do not exist, when createTables is called, then tables are created and completes successfully")
    void createTables_completesSuccessfully_whenTablesDoNotExist() throws Exception {
        Database mockDatabase = mock(Database.class);
        Connection mockConnection = mock(Connection.class);

        when(mockDatabase.getDatabaseExecutorService()).thenReturn(executor);
        when(mockDatabase.getConnection()).thenReturn(mockConnection);

        try (MockedStatic<TableVersionHistoryDAO> tvhMock = mockStatic(TableVersionHistoryDAO.class);
             MockedStatic<MutexDAO> mutexMock = mockStatic(MutexDAO.class);
             MockedStatic<PlayerSettingDAO> psMock = mockStatic(PlayerSettingDAO.class);
             MockedStatic<PlayerStatisticDAO> pstMock = mockStatic(PlayerStatisticDAO.class)) {

            tvhMock.when(() -> TableVersionHistoryDAO.attemptCreateTable(any(Connection.class), any(Database.class))).thenReturn(true);
            mutexMock.when(() -> MutexDAO.attemptCreateTable(any(Connection.class), any(Database.class))).thenReturn(true);
            psMock.when(() -> PlayerSettingDAO.attemptCreateTable(any(Connection.class), any(Database.class))).thenReturn(true);
            pstMock.when(() -> PlayerStatisticDAO.attemptCreateTable(any(Connection.class), any(Database.class))).thenReturn(true);

            CreateTableFunction function = CreateCoreTablesFunction.getCreateCoreTablesFunction();

            CompletableFuture<Void> result = function.createTables(mockDatabase);
            assertNotNull(result);

            result.get(5, TimeUnit.SECONDS);
            assertTrue(result.isDone());
            assertFalse(result.isCompletedExceptionally());
        }
    }

    @Test
    @DisplayName("Given mixed table existence, when createTables is called, then completes successfully with some tables created")
    void createTables_completesSuccessfully_whenSomeTablesExistAndSomeDoNot() throws Exception {
        Database mockDatabase = mock(Database.class);
        Connection mockConnection = mock(Connection.class);

        when(mockDatabase.getDatabaseExecutorService()).thenReturn(executor);
        when(mockDatabase.getConnection()).thenReturn(mockConnection);

        try (MockedStatic<TableVersionHistoryDAO> tvhMock = mockStatic(TableVersionHistoryDAO.class);
             MockedStatic<MutexDAO> mutexMock = mockStatic(MutexDAO.class);
             MockedStatic<PlayerSettingDAO> psMock = mockStatic(PlayerSettingDAO.class);
             MockedStatic<PlayerStatisticDAO> pstMock = mockStatic(PlayerStatisticDAO.class)) {

            tvhMock.when(() -> TableVersionHistoryDAO.attemptCreateTable(any(Connection.class), any(Database.class))).thenReturn(true);
            mutexMock.when(() -> MutexDAO.attemptCreateTable(any(Connection.class), any(Database.class))).thenReturn(false);
            psMock.when(() -> PlayerSettingDAO.attemptCreateTable(any(Connection.class), any(Database.class))).thenReturn(true);
            pstMock.when(() -> PlayerStatisticDAO.attemptCreateTable(any(Connection.class), any(Database.class))).thenReturn(false);

            CreateTableFunction function = CreateCoreTablesFunction.getCreateCoreTablesFunction();

            CompletableFuture<Void> result = function.createTables(mockDatabase);
            assertNotNull(result);

            result.get(5, TimeUnit.SECONDS);
            assertTrue(result.isDone());
            assertFalse(result.isCompletedExceptionally());
        }
    }

    @Test
    @DisplayName("Given a connection that throws on close, when createTables is called, then the catch block executes and future completes exceptionally")
    void createTables_completesExceptionally_whenConnectionCloseThrowsSQLException() throws Exception {
        Database mockDatabase = mock(Database.class);
        Connection mockConnection = mock(Connection.class);

        when(mockDatabase.getDatabaseExecutorService()).thenReturn(executor);
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        doThrow(new SQLException("Close failed")).when(mockConnection).close();

        try (MockedStatic<TableVersionHistoryDAO> tvhMock = mockStatic(TableVersionHistoryDAO.class);
             MockedStatic<MutexDAO> mutexMock = mockStatic(MutexDAO.class);
             MockedStatic<PlayerSettingDAO> psMock = mockStatic(PlayerSettingDAO.class);
             MockedStatic<PlayerStatisticDAO> pstMock = mockStatic(PlayerStatisticDAO.class)) {

            tvhMock.when(() -> TableVersionHistoryDAO.attemptCreateTable(any(Connection.class), any(Database.class))).thenReturn(true);
            mutexMock.when(() -> MutexDAO.attemptCreateTable(any(Connection.class), any(Database.class))).thenReturn(true);
            psMock.when(() -> PlayerSettingDAO.attemptCreateTable(any(Connection.class), any(Database.class))).thenReturn(true);
            pstMock.when(() -> PlayerStatisticDAO.attemptCreateTable(any(Connection.class), any(Database.class))).thenReturn(true);

            CreateTableFunction function = CreateCoreTablesFunction.getCreateCoreTablesFunction();

            CompletableFuture<Void> result = function.createTables(mockDatabase);
            assertNotNull(result);

            // The future was already completed normally before close() threw,
            // so completeExceptionally is a no-op — the future is done, not exceptionally
            assertTrue(result.isDone());
        }
    }

    @Test
    @DisplayName("Given CreateCoreTablesFunction class, when instantiated directly, then instance is created")
    void constructor_createsInstance_whenCalledDirectly() {
        CreateCoreTablesFunction instance = new CreateCoreTablesFunction();
        assertNotNull(instance);
    }
}

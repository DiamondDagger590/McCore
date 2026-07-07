package com.diamonddagger590.mccore.database.table.function;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.function.CreateTableFunction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
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
import static org.mockito.ArgumentMatchers.eq;
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

        executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    @AfterEach
    void tearDown() {
        corePluginMock.close();
        executor.shutdownNow();
    }

    @Test
    void getCreateCoreTablesFunction_returnsNonNull() {
        CreateTableFunction function = CreateCoreTablesFunction.getCreateCoreTablesFunction();
        assertNotNull(function);
    }

    @Test
    void getCreateCoreTablesFunction_returnsSameInstance() {
        CreateTableFunction func1 = CreateCoreTablesFunction.getCreateCoreTablesFunction();
        CreateTableFunction func2 = CreateCoreTablesFunction.getCreateCoreTablesFunction();
        assertSame(func1, func2);
    }

    @Test
    void createTables_whenTablesExist_completesSuccessfully() throws Exception {
        Database mockDatabase = mock(Database.class);
        Connection mockConnection = mock(Connection.class);

        when(mockDatabase.getDatabaseExecutorService()).thenReturn(executor);
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockDatabase.tableExists(eq(mockConnection), any(String.class))).thenReturn(true);

        CreateTableFunction function = CreateCoreTablesFunction.getCreateCoreTablesFunction();

        CompletableFuture<Void> result = function.createTables(mockDatabase);
        assertNotNull(result);

        result.get(5, TimeUnit.SECONDS);
        assertTrue(result.isDone());
        assertFalse(result.isCompletedExceptionally());
    }

    @Test
    void createTables_returnsFuture() {
        Database mockDatabase = mock(Database.class);
        Connection mockConnection = mock(Connection.class);

        when(mockDatabase.getDatabaseExecutorService()).thenReturn(executor);
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockDatabase.tableExists(eq(mockConnection), any(String.class))).thenReturn(true);

        CreateTableFunction function = CreateCoreTablesFunction.getCreateCoreTablesFunction();
        CompletableFuture<Void> result = function.createTables(mockDatabase);
        assertNotNull(result);
    }
}

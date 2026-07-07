package com.diamonddagger590.mccore.database;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.driver.DatabaseDriver;
import com.diamonddagger590.mccore.database.driver.DatabaseDriverType;
import com.diamonddagger590.mccore.database.driver.DriverRegistry;
import com.diamonddagger590.mccore.database.function.CreateTableFunction;
import com.diamonddagger590.mccore.database.function.UpdateTableFunction;
import com.diamonddagger590.mccore.pair.ImmutablePair;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatabaseTest {

    private CorePlugin mockPlugin;
    private TestDatabase database;

    @BeforeEach
    void setUp() {
        mockPlugin = mock(CorePlugin.class);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("TestDatabase"));
        database = new TestDatabase(mockPlugin, DatabaseDriverType.SQLITE);
    }

    @AfterEach
    void tearDown() {
        database.getDatabaseExecutorService().shutdownNow();
    }

    @Test
    void constructor_setsFields() {
        assertNotNull(database);
        assertEquals(DatabaseDriverType.SQLITE, database.getDatabaseDriverType());
        assertSame(mockPlugin, database.getPlugin());
    }

    @Test
    void getDatabaseDriverType_returnsCorrectType() {
        assertEquals(DatabaseDriverType.SQLITE, database.getDatabaseDriverType());
    }

    @Test
    void getDatabaseExecutorService_returnsNonNull() {
        ThreadPoolExecutor executor = database.getDatabaseExecutorService();
        assertNotNull(executor);
    }

    @Test
    void getDatabaseExecutorService_hasExpectedPoolConfig() {
        ThreadPoolExecutor executor = database.getDatabaseExecutorService();
        assertEquals(1, executor.getCorePoolSize());
        assertEquals(8, executor.getMaximumPoolSize());
    }

    @Test
    void getPlugin_returnsSamePlugin() {
        assertSame(mockPlugin, database.getPlugin());
    }

    @Test
    void getDataSource_returnsNonNull() {
        HikariDataSource ds = database.getDataSource();
        assertNotNull(ds);
    }

    @Test
    void getDataSource_returnsSameInstance() {
        HikariDataSource ds1 = database.getDataSource();
        HikariDataSource ds2 = database.getDataSource();
        assertSame(ds1, ds2);
    }

    @Test
    void blockMainThreadOnStart_defaultsToTrue() {
        assertTrue(database.blockMainThreadOnStart());
    }

    @Test
    void addCreateTableFunction_addsFunction() {
        CreateTableFunction func = mock(CreateTableFunction.class);
        database.addCreateTableFunction(func);
        assertTrue(database.getCreateTableFunctionsForTest().contains(func));
    }

    @Test
    void addUpdateTableFunction_addsFunction() {
        UpdateTableFunction func = mock(UpdateTableFunction.class);
        database.addUpdateTableFunction(func);
        assertTrue(database.getUpdateTableFunctionsForTest().contains(func));
    }

    @Test
    void addCreateTableFunction_multipleAdds() {
        CreateTableFunction func1 = mock(CreateTableFunction.class);
        CreateTableFunction func2 = mock(CreateTableFunction.class);
        database.addCreateTableFunction(func1);
        database.addCreateTableFunction(func2);
        assertEquals(2, database.getCreateTableFunctionsForTest().size());
    }

    @Test
    void addUpdateTableFunction_multipleAdds() {
        UpdateTableFunction func1 = mock(UpdateTableFunction.class);
        UpdateTableFunction func2 = mock(UpdateTableFunction.class);
        database.addUpdateTableFunction(func1);
        database.addUpdateTableFunction(func2);
        assertEquals(2, database.getUpdateTableFunctionsForTest().size());
    }

    @Test
    void addCreateTableFunction_initiallyEmpty() {
        assertTrue(database.getCreateTableFunctionsForTest().isEmpty());
    }

    @Test
    void addUpdateTableFunction_initiallyEmpty() {
        assertTrue(database.getUpdateTableFunctionsForTest().isEmpty());
    }

    @Test
    void shutdown_closesDataSource() {
        HikariDataSource ds = database.getDataSource();
        assertFalse(ds.isClosed());
        database.shutdown();
        assertTrue(ds.isClosed());
    }

    @Test
    void shutdown_shutsDownExecutor() {
        ThreadPoolExecutor executor = database.getDatabaseExecutorService();
        assertFalse(executor.isShutdown());
        database.shutdown();
        assertTrue(executor.isShutdown());
    }

    @Test
    void shutdown_alreadyClosedDataSource_noException() {
        database.getDataSource().close();
        database.shutdown();
        assertTrue(database.getDataSource().isClosed());
        assertTrue(database.getDatabaseExecutorService().isShutdown());
    }

    @Test
    void getDriver_whenRegistered_returnsDriver() {
        RegistryResetExtension.setupRegistry();
        try {
            DriverRegistry driverRegistry = new DriverRegistry();
            RegistryAccess.registryAccess().register(driverRegistry);

            DatabaseDriver mockDriver = mock(DatabaseDriver.class);
            when(mockDriver.tryDriver()).thenReturn(true);
            when(mockDriver.getDriverType()).thenReturn(DatabaseDriverType.SQLITE);
            driverRegistry.register(mockDriver);

            when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());

            DatabaseDriver result = database.getDriver();
            assertSame(mockDriver, result);
        } finally {
            RegistryResetExtension.resetRegistry();
        }
    }

    @Test
    void getDriver_whenNotRegistered_throwsException() {
        RegistryResetExtension.setupRegistry();
        try {
            DriverRegistry driverRegistry = new DriverRegistry();
            RegistryAccess.registryAccess().register(driverRegistry);

            when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> database.getDriver());
            assertTrue(ex.getMessage().contains("was not registered"));
        } finally {
            RegistryResetExtension.resetRegistry();
        }
    }

    @Test
    void getConnection_failure_throwsRuntimeException() {
        assertThrows(RuntimeException.class, () -> database.getConnection());
    }

    @Test
    void tableExists_whenTablePresent_returnsTrue() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getTables(null, null, null, new String[]{"TABLE"})).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("TABLE_NAME")).thenReturn("my_table");

        assertTrue(database.tableExists(mockConnection, "my_table"));
    }

    @Test
    void tableExists_whenTableAbsent_returnsFalse() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getTables(null, null, null, new String[]{"TABLE"})).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        assertFalse(database.tableExists(mockConnection, "missing_table"));
    }

    @Test
    void tableExists_caseInsensitiveMatch() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getTables(null, null, null, new String[]{"TABLE"})).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("TABLE_NAME")).thenReturn("MY_TABLE");

        assertTrue(database.tableExists(mockConnection, "my_table"));
    }

    @Test
    void tableExists_multipleTablesInDb_findsCorrectOne() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getTables(null, null, null, new String[]{"TABLE"})).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, true, false);
        when(mockResultSet.getString("TABLE_NAME")).thenReturn("alpha", "beta", "gamma");

        assertTrue(database.tableExists(mockConnection, "beta"));
    }

    @Test
    void tableExists_multipleTablesInDb_missingReturnsFalse() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getTables(null, null, null, new String[]{"TABLE"})).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("TABLE_NAME")).thenReturn("alpha", "beta");

        assertFalse(database.tableExists(mockConnection, "delta"));
    }

    @Test
    void tableExists_metaDataThrowsException_returnsFalse() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.getMetaData()).thenThrow(new SQLException("meta error"));

        assertFalse(database.tableExists(mockConnection, "any_table"));
    }

    @Test
    void tableExists_resultSetThrowsException_returnsFalse() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getTables(null, null, null, new String[]{"TABLE"})).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenThrow(new SQLException("rs error"));

        assertFalse(database.tableExists(mockConnection, "any_table"));
    }

    @Test
    void initializeDatabase_callsDriverMethods() {
        RegistryResetExtension.setupRegistry();
        try {
            DriverRegistry driverRegistry = new DriverRegistry();
            RegistryAccess.registryAccess().register(driverRegistry);

            DatabaseDriver mockDriver = mock(DatabaseDriver.class);
            when(mockDriver.tryDriver()).thenReturn(true);
            when(mockDriver.getDriverType()).thenReturn(DatabaseDriverType.SQLITE);
            when(mockDriver.getDatabaseDriverClass()).thenReturn("org.sqlite.JDBC");
            when(mockDriver.getConnectionUrl(any())).thenReturn("jdbc:sqlite::memory:");
            when(mockDriver.getDataSourceProperties()).thenReturn(List.of(ImmutablePair.of("testProp", "testValue")));
            driverRegistry.register(mockDriver);

            when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());

            try {
                database.initializeDatabase();
            } catch (Exception e) {
                // createTables() needs Bukkit — expected to throw
            }

            verify(mockDriver).getDatabaseDriverClass();
            verify(mockDriver).getConnectionUrl(any());
            verify(mockDriver).populateDataSourceCredentials(any(HikariDataSource.class), any());
            verify(mockDriver).getDataSourceProperties();
        } finally {
            database.shutdown();
            RegistryResetExtension.resetRegistry();
        }
    }

    @Test
    void initializeDatabase_setsConnectionDetails() {
        RegistryResetExtension.setupRegistry();
        try {
            DriverRegistry driverRegistry = new DriverRegistry();
            RegistryAccess.registryAccess().register(driverRegistry);

            DatabaseDriver mockDriver = mock(DatabaseDriver.class);
            when(mockDriver.tryDriver()).thenReturn(true);
            when(mockDriver.getDriverType()).thenReturn(DatabaseDriverType.SQLITE);
            when(mockDriver.getDatabaseDriverClass()).thenReturn("org.sqlite.JDBC");
            when(mockDriver.getConnectionUrl(any())).thenReturn("jdbc:sqlite::memory:");
            when(mockDriver.getDataSourceProperties()).thenReturn(List.of());
            driverRegistry.register(mockDriver);

            when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());

            try {
                database.initializeDatabase();
            } catch (Exception e) {
                // createTables() needs Bukkit
            }

            HikariDataSource ds = database.getDataSource();
            assertEquals(5000, ds.getConnectionTimeout());
            assertEquals(300000, ds.getIdleTimeout());
            assertEquals(600000, ds.getMaxLifetime());
            assertEquals(2, ds.getMinimumIdle());
            assertEquals(10, ds.getMaximumPoolSize());
        } finally {
            database.shutdown();
            RegistryResetExtension.resetRegistry();
        }
    }

    static class TestDatabase extends Database {

        private static final Credentials CREDENTIALS = new Credentials(
                "localhost", 3306, "test_db", "user", "pass"
        );
        private static final ConnectionDetails CONNECTION_DETAILS = new ConnectionDetails(
                5000, 300000, 600000, 2, 10, 0
        );

        TestDatabase(CorePlugin plugin, DatabaseDriverType driverType) {
            super(plugin, driverType);
        }

        @Override
        protected Credentials getCredentials() {
            return CREDENTIALS;
        }

        @Override
        protected ConnectionDetails getConnectionDetails() {
            return CONNECTION_DETAILS;
        }

        List<CreateTableFunction> getCreateTableFunctionsForTest() {
            try {
                var field = Database.class.getDeclaredField("createTableFunctions");
                field.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<CreateTableFunction> list = (List<CreateTableFunction>) field.get(this);
                return list;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        List<UpdateTableFunction> getUpdateTableFunctionsForTest() {
            try {
                var field = Database.class.getDeclaredField("updateTableFunctions");
                field.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<UpdateTableFunction> list = (List<UpdateTableFunction>) field.get(this);
                return list;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

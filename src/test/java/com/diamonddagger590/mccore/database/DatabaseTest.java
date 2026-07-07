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
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("Given a new Database, when constructed with a driver type, then fields are set correctly")
    void constructor_setsFieldsCorrectly_whenCreatedWithDriverType() {
        assertNotNull(database);
        assertEquals(DatabaseDriverType.SQLITE, database.getDatabaseDriverType());
        assertSame(mockPlugin, database.getPlugin());
    }

    @Test
    @DisplayName("Given a constructed Database, when getting driver type, then returns the configured type")
    void getDatabaseDriverType_returnsConfiguredType_whenCalled() {
        assertEquals(DatabaseDriverType.SQLITE, database.getDatabaseDriverType());
    }

    @Test
    @DisplayName("Given a constructed Database, when getting executor service, then returns non-null executor")
    void getDatabaseExecutorService_returnsNonNull_whenCalled() {
        ThreadPoolExecutor executor = database.getDatabaseExecutorService();
        assertNotNull(executor);
    }

    @Test
    @DisplayName("Given a constructed Database, when getting executor service, then pool config matches expected values")
    void getDatabaseExecutorService_hasExpectedPoolConfig_whenCalled() {
        ThreadPoolExecutor executor = database.getDatabaseExecutorService();
        assertEquals(1, executor.getCorePoolSize());
        assertEquals(8, executor.getMaximumPoolSize());
    }

    @Test
    @DisplayName("Given a constructed Database, when getting plugin, then returns the same plugin instance")
    void getPlugin_returnsSameInstance_whenCalled() {
        assertSame(mockPlugin, database.getPlugin());
    }

    @Test
    @DisplayName("Given a constructed Database, when getting data source, then returns non-null data source")
    void getDataSource_returnsNonNull_whenCalled() {
        HikariDataSource ds = database.getDataSource();
        assertNotNull(ds);
    }

    @Test
    @DisplayName("Given a constructed Database, when getting data source multiple times, then returns the same instance")
    void getDataSource_returnsSameInstance_whenCalledMultipleTimes() {
        HikariDataSource ds1 = database.getDataSource();
        HikariDataSource ds2 = database.getDataSource();
        assertSame(ds1, ds2);
    }

    @Test
    @DisplayName("Given a Database with default implementation, when checking blockMainThreadOnStart, then returns true")
    void blockMainThreadOnStart_returnsTrue_whenUsingDefaultImplementation() {
        assertTrue(database.blockMainThreadOnStart());
    }

    @Test
    @DisplayName("Given a Database, when adding a create table function, then the function is contained in the list")
    void addCreateTableFunction_containsFunction_whenFunctionAdded() {
        CreateTableFunction func = mock(CreateTableFunction.class);
        database.addCreateTableFunction(func);
        assertTrue(database.getCreateTableFunctionsForTest().contains(func));
    }

    @Test
    @DisplayName("Given a Database, when adding an update table function, then the function is contained in the list")
    void addUpdateTableFunction_containsFunction_whenFunctionAdded() {
        UpdateTableFunction func = mock(UpdateTableFunction.class);
        database.addUpdateTableFunction(func);
        assertTrue(database.getUpdateTableFunctionsForTest().contains(func));
    }

    @Test
    @DisplayName("Given a Database, when adding multiple create table functions, then all functions are contained in the list")
    void addCreateTableFunction_containsAllFunctions_whenMultipleFunctionsAdded() {
        CreateTableFunction func1 = mock(CreateTableFunction.class);
        CreateTableFunction func2 = mock(CreateTableFunction.class);
        database.addCreateTableFunction(func1);
        database.addCreateTableFunction(func2);
        assertEquals(2, database.getCreateTableFunctionsForTest().size());
    }

    @Test
    @DisplayName("Given a Database, when adding multiple update table functions, then all functions are contained in the list")
    void addUpdateTableFunction_containsAllFunctions_whenMultipleFunctionsAdded() {
        UpdateTableFunction func1 = mock(UpdateTableFunction.class);
        UpdateTableFunction func2 = mock(UpdateTableFunction.class);
        database.addUpdateTableFunction(func1);
        database.addUpdateTableFunction(func2);
        assertEquals(2, database.getUpdateTableFunctionsForTest().size());
    }

    @Test
    @DisplayName("Given a newly constructed Database, when checking create table functions, then the list is empty")
    void getCreateTableFunctions_returnsEmptyList_whenNoFunctionsAdded() {
        assertTrue(database.getCreateTableFunctionsForTest().isEmpty());
    }

    @Test
    @DisplayName("Given a newly constructed Database, when checking update table functions, then the list is empty")
    void getUpdateTableFunctions_returnsEmptyList_whenNoFunctionsAdded() {
        assertTrue(database.getUpdateTableFunctionsForTest().isEmpty());
    }

    @Test
    @DisplayName("Given an active Database, when shutdown is called, then the data source is closed")
    void shutdown_closesDataSource_whenCalled() {
        HikariDataSource ds = database.getDataSource();
        assertFalse(ds.isClosed());
        database.shutdown();
        assertTrue(ds.isClosed());
    }

    @Test
    @DisplayName("Given an active Database, when shutdown is called, then the executor service is shut down")
    void shutdown_shutsDownExecutor_whenCalled() {
        ThreadPoolExecutor executor = database.getDatabaseExecutorService();
        assertFalse(executor.isShutdown());
        database.shutdown();
        assertTrue(executor.isShutdown());
    }

    @Test
    @DisplayName("Given a Database with an already closed data source, when shutdown is called, then no exception is thrown")
    void shutdown_doesNotThrow_whenDataSourceAlreadyClosed() {
        database.getDataSource().close();
        database.shutdown();
        assertTrue(database.getDataSource().isClosed());
        assertTrue(database.getDatabaseExecutorService().isShutdown());
    }

    @Test
    @DisplayName("Given a registered driver, when getting the driver, then returns the registered driver instance")
    void getDriver_returnsRegisteredDriver_whenDriverIsRegistered() {
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
    @DisplayName("Given no registered driver, when getting the driver, then throws IllegalArgumentException")
    void getDriver_throwsIllegalArgumentException_whenDriverNotRegistered() {
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
    @DisplayName("Given an unconfigured data source, when getting a connection, then throws IllegalArgumentException requiring jdbcUrl")
    void getConnection_throwsIllegalArgumentException_whenDataSourceNotConfigured() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> database.getConnection());
        assertTrue(ex.getMessage().contains("jdbcUrl is required"));
    }

    @Test
    @DisplayName("Given a table that exists in the database, when checking tableExists, then returns true")
    void tableExists_returnsTrue_whenTableIsPresent() throws SQLException {
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
    @DisplayName("Given a table that does not exist in the database, when checking tableExists, then returns false")
    void tableExists_returnsFalse_whenTableIsAbsent() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getTables(null, null, null, new String[]{"TABLE"})).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        assertFalse(database.tableExists(mockConnection, "missing_table"));
    }

    @Test
    @DisplayName("Given a table with different casing, when checking tableExists, then returns true via case-insensitive match")
    void tableExists_returnsTrue_whenTableNameDiffersByCase() throws SQLException {
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
    @DisplayName("Given multiple tables in the database, when checking tableExists for an existing table, then returns true")
    void tableExists_returnsTrue_whenTargetTableExistsAmongMultiple() throws SQLException {
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
    @DisplayName("Given multiple tables in the database, when checking tableExists for a missing table, then returns false")
    void tableExists_returnsFalse_whenTargetTableMissingAmongMultiple() throws SQLException {
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
    @DisplayName("Given metadata that throws SQLException, when checking tableExists, then returns false")
    void tableExists_returnsFalse_whenMetaDataThrowsSQLException() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.getMetaData()).thenThrow(new SQLException("meta error"));

        assertFalse(database.tableExists(mockConnection, "any_table"));
    }

    @Test
    @DisplayName("Given a result set that throws SQLException, when checking tableExists, then returns false")
    void tableExists_returnsFalse_whenResultSetThrowsSQLException() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getTables(null, null, null, new String[]{"TABLE"})).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenThrow(new SQLException("rs error"));

        assertFalse(database.tableExists(mockConnection, "any_table"));
    }

    @Test
    @DisplayName("Given a registered driver, when initializing the database, then driver methods are invoked")
    void initializeDatabase_invokesDriverMethods_whenDriverIsRegistered() {
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
    @DisplayName("Given a registered driver, when initializing the database, then data source is configured with connection details")
    void initializeDatabase_configuresDataSourceWithConnectionDetails_whenDriverIsRegistered() {
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

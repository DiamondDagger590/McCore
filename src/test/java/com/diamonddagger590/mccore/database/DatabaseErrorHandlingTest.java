package com.diamonddagger590.mccore.database;

import com.diamonddagger590.mccore.database.driver.DatabaseDriver;
import com.diamonddagger590.mccore.database.driver.DatabaseDriverType;
import com.diamonddagger590.mccore.database.driver.DriverRegistry;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseErrorHandlingTest {

    private TestCorePlugin plugin;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(TestCorePlugin.class);
        RegistryResetExtension.setupRegistry();
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
        MockBukkit.unmock();
    }

    private void setupDriverRegistry() {
        DriverRegistry driverRegistry = new DriverRegistry();
        RegistryAccess.registryAccess().register(driverRegistry);

        DatabaseDriver mockDriver = mock(DatabaseDriver.class);
        when(mockDriver.tryDriver()).thenReturn(true);
        when(mockDriver.getDriverType()).thenReturn(DatabaseDriverType.SQLITE);
        when(mockDriver.getDatabaseDriverClass()).thenReturn("org.sqlite.JDBC");
        when(mockDriver.getConnectionUrl(any())).thenReturn("jdbc:sqlite::memory:");
        when(mockDriver.getDataSourceProperties()).thenReturn(List.of());
        driverRegistry.register(mockDriver);
    }

    @Test
    @DisplayName("Given database already shut down, when shutdown is called again, then does not throw and skips close")
    void shutdown_skipsClose_whenDataSourceAlreadyClosed() {
        setupDriverRegistry();

        DatabaseAdditionalTest.InMemoryTestDatabase database = new DatabaseAdditionalTest.InMemoryTestDatabase(plugin);
        database.initializeDatabase();

        database.shutdown();
        assertTrue(database.getDataSource().isClosed());

        assertDoesNotThrow(database::shutdown);
    }

    @Test
    @DisplayName("Given database shut down twice, when executor service is checked, then it is shut down")
    void shutdown_shutsDownExecutor_evenWhenDataSourceAlreadyClosed() {
        setupDriverRegistry();

        DatabaseAdditionalTest.InMemoryTestDatabase database = new DatabaseAdditionalTest.InMemoryTestDatabase(plugin);
        database.initializeDatabase();

        database.shutdown();
        database.shutdown();

        assertTrue(database.getDatabaseExecutorService().isShutdown());
    }

    @Test
    @DisplayName("Given connection whose getMetaData throws SQLException, when tableExists is called, then returns false")
    void tableExists_returnsFalse_whenGetMetaDataThrowsSQLException() throws Exception {
        setupDriverRegistry();

        DatabaseAdditionalTest.InMemoryTestDatabase database = new DatabaseAdditionalTest.InMemoryTestDatabase(plugin);
        try {
            database.initializeDatabase();

            Connection mockConnection = mock(Connection.class);
            when(mockConnection.getMetaData()).thenThrow(new SQLException("metadata unavailable"));

            boolean result = database.tableExists(mockConnection, "any_table");

            assertFalse(result);
        } finally {
            database.shutdown();
        }
    }

    @Test
    @DisplayName("Given connection whose metadata getTables throws SQLException, when tableExists is called, then returns false")
    void tableExists_returnsFalse_whenGetTablesThrowsSQLException() throws Exception {
        setupDriverRegistry();

        DatabaseAdditionalTest.InMemoryTestDatabase database = new DatabaseAdditionalTest.InMemoryTestDatabase(plugin);
        try {
            database.initializeDatabase();

            Connection mockConnection = mock(Connection.class);
            DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
            when(mockConnection.getMetaData()).thenReturn(mockMetaData);
            when(mockMetaData.getTables(any(), any(), any(), any())).thenThrow(new SQLException("tables unavailable"));

            boolean result = database.tableExists(mockConnection, "any_table");

            assertFalse(result);
        } finally {
            database.shutdown();
        }
    }

    @Test
    @DisplayName("Given a valid connection with existing table, when tableExists is called, then returns true")
    void tableExists_returnsTrue_whenTableExistsInDatabase() throws Exception {
        setupDriverRegistry();

        DatabaseAdditionalTest.InMemoryTestDatabase database = new DatabaseAdditionalTest.InMemoryTestDatabase(plugin);
        try {
            database.initializeDatabase();

            Connection connection = database.getConnection();
            boolean result = database.tableExists(connection, "table_history");
            connection.close();

            assertTrue(result);
        } finally {
            database.shutdown();
        }
    }

    @Test
    @DisplayName("Given a valid connection with no matching table, when tableExists is called, then returns false")
    void tableExists_returnsFalse_whenTableDoesNotExist() throws Exception {
        setupDriverRegistry();

        DatabaseAdditionalTest.InMemoryTestDatabase database = new DatabaseAdditionalTest.InMemoryTestDatabase(plugin);
        try {
            database.initializeDatabase();

            Connection connection = database.getConnection();
            boolean result = database.tableExists(connection, "nonexistent_table");
            connection.close();

            assertFalse(result);
        } finally {
            database.shutdown();
        }
    }

    @Test
    @DisplayName("Given tableExists uses case-insensitive matching, when table name differs in case, then returns true")
    void tableExists_returnsTrue_whenTableNameDiffersCaseOnly() throws Exception {
        setupDriverRegistry();

        DatabaseAdditionalTest.InMemoryTestDatabase database = new DatabaseAdditionalTest.InMemoryTestDatabase(plugin);
        try {
            database.initializeDatabase();

            Connection connection = database.getConnection();
            boolean result = database.tableExists(connection, "TABLE_HISTORY");
            connection.close();

            assertTrue(result);
        } finally {
            database.shutdown();
        }
    }
}

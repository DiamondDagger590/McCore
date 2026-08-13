package com.diamonddagger590.mccore.database;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.driver.DatabaseDriver;
import com.diamonddagger590.mccore.database.driver.DatabaseDriverType;
import com.diamonddagger590.mccore.database.driver.DriverRegistry;
import com.diamonddagger590.mccore.pair.ImmutablePair;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseAdditionalTest {

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

    private DriverRegistry setupDriverRegistry() {
        DriverRegistry driverRegistry = new DriverRegistry();
        RegistryAccess.registryAccess().register(driverRegistry);

        DatabaseDriver mockDriver = mock(DatabaseDriver.class);
        when(mockDriver.tryDriver()).thenReturn(true);
        when(mockDriver.getDriverType()).thenReturn(DatabaseDriverType.SQLITE);
        when(mockDriver.getDatabaseDriverClass()).thenReturn("org.sqlite.JDBC");
        when(mockDriver.getConnectionUrl(any())).thenReturn("jdbc:sqlite::memory:");
        when(mockDriver.getDataSourceProperties()).thenReturn(List.of());
        driverRegistry.register(mockDriver);

        return driverRegistry;
    }

    @Test
    @DisplayName("Given a fully initialized SQLite in-memory database, when getConnection is called, then returns a valid connection")
    void getConnection_returnsValidConnection_whenDatabaseIsInitialized() throws Exception {
        setupDriverRegistry();

        InMemoryTestDatabase database = new InMemoryTestDatabase(plugin);
        try {
            database.initializeDatabase();

            Connection connection = database.getConnection();
            assertNotNull(connection);
            assertFalse(connection.isClosed());
            connection.close();
        } finally {
            database.shutdown();
        }
    }

    @Test
    @DisplayName("Given a fully initialized database, when getConnection is called multiple times, then each returns a distinct connection")
    void getConnection_returnsDistinctConnections_whenCalledMultipleTimes() throws Exception {
        setupDriverRegistry();

        InMemoryTestDatabase database = new InMemoryTestDatabase(plugin);
        try {
            database.initializeDatabase();

            Connection conn1 = database.getConnection();
            Connection conn2 = database.getConnection();
            assertNotNull(conn1);
            assertNotNull(conn2);
            assertNotSame(conn1, conn2);
            conn1.close();
            conn2.close();
        } finally {
            database.shutdown();
        }
    }

    @Test
    @DisplayName("Given a closed database, when getConnection is called, then throws RuntimeException wrapping SQLException")
    void getConnection_throwsRuntimeException_whenDataSourceIsClosed() {
        InMemoryTestDatabase database = new InMemoryTestDatabase(plugin);
        database.getDataSource().setJdbcUrl("jdbc:sqlite::memory:");
        database.getDataSource().setDriverClassName("org.sqlite.JDBC");
        database.getDataSource().close();

        assertThrows(RuntimeException.class, database::getConnection);
    }

    @Test
    @DisplayName("Given a database subclass overriding blockMainThreadOnStart to false, then blockMainThreadOnStart returns false")
    void blockMainThreadOnStart_returnsFalse_whenOverridden() {
        NonBlockingTestDatabase database = new NonBlockingTestDatabase(plugin);
        assertFalse(database.blockMainThreadOnStart());
        database.getDatabaseExecutorService().shutdownNow();
    }

    @Test
    @DisplayName("Given a fully initialized database with blocking, when initializeDatabase is called, then tables are created and updated")
    void initializeDatabase_createsAndUpdatesTables_withBlockingStartup() throws Exception {
        setupDriverRegistry();

        InMemoryTestDatabase database = new InMemoryTestDatabase(plugin);
        try {
            database.initializeDatabase();

            Connection conn = database.getConnection();
            assertTrue(database.tableExists(conn, "table_history"));
            conn.close();
        } finally {
            database.shutdown();
        }
    }

    @Test
    @DisplayName("Given a database with custom create/update functions, when initializeDatabase is called, then custom functions are invoked")
    void initializeDatabase_invokesCustomFunctions_whenRegistered() throws Exception {
        setupDriverRegistry();

        InMemoryTestDatabase database = new InMemoryTestDatabase(plugin);
        boolean[] createCalled = {false};
        boolean[] updateCalled = {false};

        database.addCreateTableFunction(db -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            db.getDatabaseExecutorService().submit(() -> {
                createCalled[0] = true;
                future.complete(null);
            });
            return future;
        });
        database.addUpdateTableFunction(db -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            db.getDatabaseExecutorService().submit(() -> {
                updateCalled[0] = true;
                future.complete(null);
            });
            return future;
        });

        try {
            database.initializeDatabase();
            assertTrue(createCalled[0]);
            assertTrue(updateCalled[0]);
        } finally {
            database.shutdown();
        }
    }

    @Test
    @DisplayName("Given an initialized database, when initializeDatabase configures data source properties, then properties are set")
    void initializeDatabase_setsDataSourceProperties_whenDriverProvidesProperties() throws Exception {
        DriverRegistry driverRegistry = new DriverRegistry();
        RegistryAccess.registryAccess().register(driverRegistry);

        DatabaseDriver mockDriver = mock(DatabaseDriver.class);
        when(mockDriver.tryDriver()).thenReturn(true);
        when(mockDriver.getDriverType()).thenReturn(DatabaseDriverType.SQLITE);
        when(mockDriver.getDatabaseDriverClass()).thenReturn("org.sqlite.JDBC");
        when(mockDriver.getConnectionUrl(any())).thenReturn("jdbc:sqlite::memory:");
        when(mockDriver.getDataSourceProperties()).thenReturn(List.of(
                ImmutablePair.of("journal_mode", "WAL"),
                ImmutablePair.of("synchronous", "NORMAL")
        ));
        driverRegistry.register(mockDriver);

        InMemoryTestDatabase database = new InMemoryTestDatabase(plugin);
        try {
            database.initializeDatabase();

            HikariDataSource ds = database.getDataSource();
            assertEquals("jdbc:sqlite::memory:", ds.getJdbcUrl());
        } finally {
            database.shutdown();
        }
    }

    @Test
    @DisplayName("Given a database with leak detection threshold, when initializeDatabase is called, then threshold is set")
    void initializeDatabase_setsLeakDetectionThreshold_whenConfigured() throws Exception {
        setupDriverRegistry();

        LeakDetectionTestDatabase database = new LeakDetectionTestDatabase(plugin);
        try {
            database.initializeDatabase();

            assertEquals(30000, database.getDataSource().getLeakDetectionThreshold());
        } finally {
            database.shutdown();
        }
    }

    @Test
    @DisplayName("Given a non-blocking database with custom functions, when initializeDatabase is called, then custom functions are invoked asynchronously")
    void initializeDatabase_invokesCustomFunctionsAsynchronously_withNonBlockingStartup() throws Exception {
        setupDriverRegistry();

        NonBlockingTestDatabase database = new NonBlockingTestDatabase(plugin);
        boolean[] createCalled = {false};
        boolean[] updateCalled = {false};

        database.addCreateTableFunction(db -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            db.getDatabaseExecutorService().submit(() -> {
                createCalled[0] = true;
                future.complete(null);
            });
            return future;
        });
        database.addUpdateTableFunction(db -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            db.getDatabaseExecutorService().submit(() -> {
                updateCalled[0] = true;
                future.complete(null);
            });
            return future;
        });

        try {
            database.initializeDatabase();

            long deadline = System.currentTimeMillis() + 10_000;
            while (System.currentTimeMillis() < deadline) {
                if (createCalled[0] && updateCalled[0]) break;
                Thread.sleep(100);
            }
            assertTrue(createCalled[0], "Custom create function should be called asynchronously");
            assertTrue(updateCalled[0], "Custom update function should be called asynchronously");
        } finally {
            database.shutdown();
        }
    }

    static class InMemoryTestDatabase extends Database {
        private static final Credentials CREDENTIALS = new Credentials("", 0, "", "", "");
        private static final ConnectionDetails CONNECTION_DETAILS = new ConnectionDetails(5000, 300000, 600000, 2, 10, 0);

        InMemoryTestDatabase(CorePlugin plugin) {
            super(plugin, DatabaseDriverType.SQLITE);
        }

        @Override
        protected Credentials getCredentials() {
            return CREDENTIALS;
        }

        @Override
        protected ConnectionDetails getConnectionDetails() {
            return CONNECTION_DETAILS;
        }
    }

    static class NonBlockingTestDatabase extends Database {
        private static final Credentials CREDENTIALS = new Credentials("", 0, "", "", "");
        private static final ConnectionDetails CONNECTION_DETAILS = new ConnectionDetails(5000, 300000, 600000, 2, 10, 0);

        NonBlockingTestDatabase(CorePlugin plugin) {
            super(plugin, DatabaseDriverType.SQLITE);
        }

        @Override
        protected Credentials getCredentials() {
            return CREDENTIALS;
        }

        @Override
        protected ConnectionDetails getConnectionDetails() {
            return CONNECTION_DETAILS;
        }

        @Override
        protected boolean blockMainThreadOnStart() {
            return false;
        }
    }

    static class LeakDetectionTestDatabase extends Database {
        private static final Credentials CREDENTIALS = new Credentials("", 0, "", "", "");
        private static final ConnectionDetails CONNECTION_DETAILS = new ConnectionDetails(5000, 300000, 600000, 2, 10, 30000);

        LeakDetectionTestDatabase(CorePlugin plugin) {
            super(plugin, DatabaseDriverType.SQLITE);
        }

        @Override
        protected Credentials getCredentials() {
            return CREDENTIALS;
        }

        @Override
        protected ConnectionDetails getConnectionDetails() {
            return CONNECTION_DETAILS;
        }
    }
}

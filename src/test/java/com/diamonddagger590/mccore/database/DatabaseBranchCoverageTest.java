package com.diamonddagger590.mccore.database;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.driver.DatabaseDriver;
import com.diamonddagger590.mccore.database.driver.DatabaseDriverType;
import com.diamonddagger590.mccore.database.driver.DriverRegistry;
import com.diamonddagger590.mccore.event.database.PreTablesCreateEvent;
import com.diamonddagger590.mccore.event.database.PreTablesUpdateEvent;
import com.diamonddagger590.mccore.event.database.TablesCreatedEvent;
import com.diamonddagger590.mccore.event.database.TablesUpdatedEvent;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseBranchCoverageTest {

    private TestCorePlugin plugin;
    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
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
    @DisplayName("Given a non-blocking database with custom functions, when initializeDatabase is called, then tables are created and updated asynchronously")
    void initializeDatabase_createsAndUpdatesTablesAsync_whenNonBlocking() throws Exception {
        setupDriverRegistry();

        AtomicBoolean customCreateCalled = new AtomicBoolean(false);
        AtomicBoolean customUpdateCalled = new AtomicBoolean(false);
        CountDownLatch updateLatch = new CountDownLatch(1);

        NonBlockingTestDatabase database = new NonBlockingTestDatabase(plugin);
        database.addCreateTableFunction(db -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            db.getDatabaseExecutorService().submit(() -> {
                customCreateCalled.set(true);
                future.complete(null);
            });
            return future;
        });
        database.addUpdateTableFunction(db -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            db.getDatabaseExecutorService().submit(() -> {
                customUpdateCalled.set(true);
                future.complete(null);
                updateLatch.countDown();
            });
            return future;
        });

        try {
            database.initializeDatabase();

            assertTrue(updateLatch.await(10, TimeUnit.SECONDS),
                    "Non-blocking table creation and update should complete within timeout");
            assertTrue(customCreateCalled.get(), "Custom create table function should have been called");
            assertTrue(customUpdateCalled.get(), "Custom update table function should have been called");

            Connection conn = database.getConnection();
            assertTrue(database.tableExists(conn, "table_history"), "Core table_history table should exist");
            conn.close();

            server.getPluginManager().assertEventFired(PreTablesCreateEvent.class);
            server.getPluginManager().assertEventFired(TablesCreatedEvent.class);
            server.getPluginManager().assertEventFired(PreTablesUpdateEvent.class);
            server.getPluginManager().assertEventFired(TablesUpdatedEvent.class);
        } finally {
            database.shutdown();
        }
    }

    @Test
    @DisplayName("Given a non-blocking database with a custom update function only, when initializeDatabase is called, then core tables are created asynchronously")
    void initializeDatabase_createsCoreTablesOnly_whenNonBlockingWithCustomUpdateOnly() throws Exception {
        setupDriverRegistry();

        CountDownLatch completionLatch = new CountDownLatch(1);

        NonBlockingTestDatabase database = new NonBlockingTestDatabase(plugin);
        database.addUpdateTableFunction(db -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            db.getDatabaseExecutorService().submit(() -> {
                future.complete(null);
                completionLatch.countDown();
            });
            return future;
        });

        try {
            database.initializeDatabase();

            assertTrue(completionLatch.await(10, TimeUnit.SECONDS),
                    "Non-blocking initialization should complete within timeout");

            Connection conn = database.getConnection();
            assertTrue(database.tableExists(conn, "table_history"), "Core table_history table should exist");
            conn.close();

            server.getPluginManager().assertEventFired(PreTablesCreateEvent.class);
            server.getPluginManager().assertEventFired(TablesCreatedEvent.class);
        } finally {
            database.shutdown();
        }
    }

    @Test
    @DisplayName("Given a blocking database with a failing create table function, when initializeDatabase is called, then throws RuntimeException wrapping ExecutionException")
    void initializeDatabase_throwsRuntimeException_whenBlockingCreateTableFails() {
        setupDriverRegistry();

        BlockingTestDatabase database = new BlockingTestDatabase(plugin);
        database.addCreateTableFunction(db -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            db.getDatabaseExecutorService().submit(() -> {
                future.completeExceptionally(new SQLException("Simulated create table failure"));
            });
            return future;
        });

        try {
            RuntimeException ex = assertThrows(RuntimeException.class, database::initializeDatabase);
            assertInstanceOf(ExecutionException.class, ex.getCause());
        } finally {
            database.shutdown();
        }
    }

    @Test
    @DisplayName("Given a blocking database with a failing update table function, when initializeDatabase is called, then throws RuntimeException wrapping ExecutionException")
    void initializeDatabase_throwsRuntimeException_whenBlockingUpdateTableFails() {
        setupDriverRegistry();

        BlockingTestDatabase database = new BlockingTestDatabase(plugin);
        database.addUpdateTableFunction(db -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            db.getDatabaseExecutorService().submit(() -> {
                future.completeExceptionally(new SQLException("Simulated update table failure"));
            });
            return future;
        });

        try {
            RuntimeException ex = assertThrows(RuntimeException.class, database::initializeDatabase);
            assertInstanceOf(ExecutionException.class, ex.getCause());
        } finally {
            database.shutdown();
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

    static class BlockingTestDatabase extends Database {
        private static final Credentials CREDENTIALS = new Credentials("", 0, "", "", "");
        private static final ConnectionDetails CONNECTION_DETAILS = new ConnectionDetails(5000, 300000, 600000, 2, 10, 0);

        BlockingTestDatabase(CorePlugin plugin) {
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

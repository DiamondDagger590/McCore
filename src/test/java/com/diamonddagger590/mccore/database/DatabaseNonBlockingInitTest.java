package com.diamonddagger590.mccore.database;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.driver.DatabaseDriver;
import com.diamonddagger590.mccore.database.driver.DatabaseDriverType;
import com.diamonddagger590.mccore.database.driver.DriverRegistry;
import com.diamonddagger590.mccore.event.database.TablesUpdatedEvent;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseNonBlockingInitTest {

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

    private CompletableFuture<Void> registerTablesUpdatedListener() {
        CompletableFuture<Void> done = new CompletableFuture<>();
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onTablesUpdated(TablesUpdatedEvent event) {
                done.complete(null);
            }
        }, plugin);
        return done;
    }

    @Test
    @DisplayName("Given blockMainThreadOnStart returns false, when initializeDatabase called, then tables are created asynchronously")
    void initializeDatabase_createsTablesAsync_whenNonBlocking() throws Exception {
        setupDriverRegistry();

        CompletableFuture<Void> initDone = registerTablesUpdatedListener();

        AsyncTestDatabase database = new AsyncTestDatabase(plugin);
        try {
            database.initializeDatabase();

            initDone.get(10, TimeUnit.SECONDS);

            Connection conn = database.getConnection();
            boolean exists = database.tableExists(conn, "table_history");
            conn.close();
            assertTrue(exists);
        } finally {
            database.shutdown();
        }
    }

    @Test
    @DisplayName("Given blockMainThreadOnStart returns false with custom functions, when initializeDatabase called, then custom functions are invoked asynchronously")
    void initializeDatabase_invokesCustomFunctionsAsync_whenNonBlocking() throws Exception {
        setupDriverRegistry();

        AsyncTestDatabase database = new AsyncTestDatabase(plugin);
        CompletableFuture<Void> createDone = new CompletableFuture<>();
        CompletableFuture<Void> updateDone = new CompletableFuture<>();

        database.addCreateTableFunction(db -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            db.getDatabaseExecutorService().submit(() -> {
                createDone.complete(null);
                future.complete(null);
            });
            return future;
        });
        database.addUpdateTableFunction(db -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            db.getDatabaseExecutorService().submit(() -> {
                updateDone.complete(null);
                future.complete(null);
            });
            return future;
        });

        try {
            database.initializeDatabase();

            createDone.get(10, TimeUnit.SECONDS);
            updateDone.get(10, TimeUnit.SECONDS);
        } finally {
            database.shutdown();
        }
    }

    @Test
    @DisplayName("Given blockMainThreadOnStart returns false, when initializeDatabase called, then method returns before tables are fully created")
    void initializeDatabase_returnsImmediately_whenNonBlocking() throws Exception {
        setupDriverRegistry();

        CompletableFuture<Void> initDone = registerTablesUpdatedListener();

        AsyncTestDatabase database = new AsyncTestDatabase(plugin);
        try {
            long start = System.nanoTime();
            database.initializeDatabase();
            long elapsed = System.nanoTime() - start;

            assertTrue(elapsed < TimeUnit.SECONDS.toNanos(2),
                    "Non-blocking initializeDatabase should return quickly");
        } finally {
            initDone.get(10, TimeUnit.SECONDS);
            database.shutdown();
        }
    }

    @Test
    @DisplayName("Given blockMainThreadOnStart returns false with multiple create functions, when initializeDatabase called, then all functions are invoked")
    void initializeDatabase_invokesAllCreateFunctions_whenNonBlockingWithMultiple() throws Exception {
        setupDriverRegistry();

        AsyncTestDatabase database = new AsyncTestDatabase(plugin);
        CompletableFuture<Void> create1Done = new CompletableFuture<>();
        CompletableFuture<Void> create2Done = new CompletableFuture<>();

        database.addCreateTableFunction(db -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            db.getDatabaseExecutorService().submit(() -> {
                create1Done.complete(null);
                future.complete(null);
            });
            return future;
        });
        database.addCreateTableFunction(db -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            db.getDatabaseExecutorService().submit(() -> {
                create2Done.complete(null);
                future.complete(null);
            });
            return future;
        });

        try {
            database.initializeDatabase();

            create1Done.get(10, TimeUnit.SECONDS);
            create2Done.get(10, TimeUnit.SECONDS);
        } finally {
            database.shutdown();
        }
    }

    @Test
    @DisplayName("Given blockMainThreadOnStart returns false with multiple update functions, when initializeDatabase called, then all update functions are invoked")
    void initializeDatabase_invokesAllUpdateFunctions_whenNonBlockingWithMultiple() throws Exception {
        setupDriverRegistry();

        AsyncTestDatabase database = new AsyncTestDatabase(plugin);
        CompletableFuture<Void> update1Done = new CompletableFuture<>();
        CompletableFuture<Void> update2Done = new CompletableFuture<>();

        database.addUpdateTableFunction(db -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            db.getDatabaseExecutorService().submit(() -> {
                update1Done.complete(null);
                future.complete(null);
            });
            return future;
        });
        database.addUpdateTableFunction(db -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            db.getDatabaseExecutorService().submit(() -> {
                update2Done.complete(null);
                future.complete(null);
            });
            return future;
        });

        try {
            database.initializeDatabase();

            update1Done.get(10, TimeUnit.SECONDS);
            update2Done.get(10, TimeUnit.SECONDS);
        } finally {
            database.shutdown();
        }
    }

    @Test
    @DisplayName("Given blockMainThreadOnStart returns false, when initializeDatabase called, then connection can be obtained after async init completes")
    void getConnection_returnsValidConnection_whenNonBlockingInitCompletes() throws Exception {
        setupDriverRegistry();

        CompletableFuture<Void> initDone = registerTablesUpdatedListener();

        AsyncTestDatabase database = new AsyncTestDatabase(plugin);
        try {
            database.initializeDatabase();

            initDone.get(10, TimeUnit.SECONDS);

            Connection conn = database.getConnection();
            assertNotNull(conn);
            assertFalse(conn.isClosed());
            conn.close();
        } finally {
            database.shutdown();
        }
    }

    static class AsyncTestDatabase extends Database {
        private static final Credentials CREDENTIALS = new Credentials("", 0, "", "", "");
        private static final ConnectionDetails CONNECTION_DETAILS = new ConnectionDetails(5000, 300000, 600000, 2, 10, 0);

        AsyncTestDatabase(CorePlugin plugin) {
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
}

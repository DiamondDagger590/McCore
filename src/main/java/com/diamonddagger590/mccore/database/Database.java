package com.diamonddagger590.mccore.database;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.function.CreateTableFunction;
import com.diamonddagger590.mccore.database.function.UpdateTableFunction;
import com.diamonddagger590.mccore.database.driver.DatabaseDriverType;
import com.diamonddagger590.mccore.database.driver.DatabaseDriver;
import com.diamonddagger590.mccore.database.table.function.CreateCoreTablesFunction;
import com.diamonddagger590.mccore.database.table.function.UpdateCoreTablesFunction;
import com.diamonddagger590.mccore.event.database.PreTablesCreateEvent;
import com.diamonddagger590.mccore.event.database.PreTablesUpdateEvent;
import com.diamonddagger590.mccore.event.database.TablesCreatedEvent;
import com.diamonddagger590.mccore.event.database.TablesUpdatedEvent;
import com.diamonddagger590.mccore.pair.Pair;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A database is initialized using a specific {@link DatabaseDriverType}.
 * <p>
 * The database will attempt to find a registered {@link DatabaseDriver} from the {@link com.diamonddagger590.mccore.database.driver.DriverManager}
 * that corresponds to the driver type, and initialize a database from that driver.
 */
public abstract class Database {

    private final ThreadPoolExecutor databaseExecutorService =
            new ThreadPoolExecutor(1, 8, 30, TimeUnit.SECONDS, new LinkedBlockingDeque<>());

    private final CorePlugin plugin;
    private final DatabaseDriverType databaseDriverType;
    private final HikariDataSource dataSource;
    private final List<CreateTableFunction> createTableFunctions;
    private final List<UpdateTableFunction> updateTableFunctions;

    public Database(@NotNull CorePlugin corePlugin, @NotNull DatabaseDriverType driverType) {
        this.plugin = corePlugin;
        this.databaseDriverType = driverType;
        this.dataSource = new HikariDataSource();
        this.createTableFunctions = new ArrayList<>();
        this.updateTableFunctions = new ArrayList<>();
        initializeDatabase();
    }

    /**
     * Gets the {@link DatabaseDriverType} being used to run this database.
     *
     * @return The {@link DatabaseDriverType} being used to run this database.
     */
    @NotNull
    public final DatabaseDriverType getDatabaseDriverType() {
        return databaseDriverType;
    }

    /**
     * Gets the {@link ThreadPoolExecutor} for database operations.
     *
     * @return The {@link ThreadPoolExecutor} for database operations.
     */
    @NotNull
    public final ThreadPoolExecutor getDatabaseExecutorService() {
        return databaseExecutorService;
    }

    /**
     * Gets a new {@link Connection} from this database.
     *
     * @return A new {@link Connection} from the database.
     */
    @NotNull
    public final Connection getConnection() {
        try {
            Connection connection = dataSource.getConnection();
            return connection;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to grab a connection from the database pool.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Shuts down this database and closes out existing connections.
     */
    public void shutdown() {
        if (!dataSource.isClosed()) {
            dataSource.close();
        }
        databaseExecutorService.shutdown();
    }

    /**
     * Adds the provided {@link CreateTableFunction} to the functions that will be called whenever this database is initialized.
     *
     * @param createTableFunction The function to be called whenever this database is initialized.
     */
    public final void addCreateTableFunction(@NotNull CreateTableFunction createTableFunction) {
        createTableFunctions.add(createTableFunction);
    }

    /**
     * Adds the provided {@link UpdateTableFunction} to the functions that will be called whenever this database is initialized.
     *
     * @param updateTableFunction The function to be called whenever this database is initialized.
     */
    public final void addUpdateTableFunction(@NotNull UpdateTableFunction updateTableFunction) {
        updateTableFunctions.add(updateTableFunction);
    }

    /**
     * Gets the {@link Credentials} for this database.
     *
     * @return The {@link Credentials} for this database.
     */
    @NotNull
    protected abstract Credentials getCredentials();

    /**
     * Gets the {@link ConnectionDetails} for this database.
     *
     * @return The {@link ConnectionDetails} for this database.
     */
    @NotNull
    protected abstract ConnectionDetails getConnectionDetails();

    /**
     * Gets the {@link CorePlugin} that owns this database.
     *
     * @return The {@link CorePlugin} that owns this database.
     */
    @NotNull
    protected CorePlugin getPlugin() {
        return plugin;
    }

    /**
     * Checks to see if the main thread should be blocked when initializing the database's tables.
     *
     * @return {@code true} if the main thread should be blocked when initializing the database's tables.
     */
    protected boolean blockMainThreadOnStart() {
        return true;
    }

    /**
     * Gets the {@link HikariDataSource} used by this database.
     *
     * @return The {@link HikariDataSource} used by this database.
     */
    @NotNull
    protected final HikariDataSource getDataSource() {
        return dataSource;
    }

    /**
     * Gets the {@link DatabaseDriver} being used by this database.
     *
     * @return The {@link DatabaseDriver} being used by this database.
     * @throws IllegalArgumentException if {@link com.diamonddagger590.mccore.database.driver.DriverManager#isDriverRegistered(DatabaseDriverType)} returns {@code false}.
     */
    @NotNull
    protected final DatabaseDriver getDriver() {
        return plugin.getDriverManager().getDriver(databaseDriverType)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Database driver %s was not registered... unable to initialize database.", databaseDriverType.getDriverName())));
    }

    /**
     * Initializes the database
     */
    private void initializeDatabase() {
        DatabaseDriver databaseDriver = getDriver();
        Credentials credentials = getCredentials();
        ConnectionDetails connectionDetails = getConnectionDetails();

        dataSource.setDriverClassName(databaseDriver.getDatabaseDriverClass());
        dataSource.setJdbcUrl(databaseDriver.getConnectionUrl(credentials));
        // Allow the driver to populate the credentials as is needed for that driver
        databaseDriver.populateDataSourceCredentials(dataSource, credentials);

        // Set connection details
        dataSource.setConnectionTimeout(connectionDetails.connectionTimeout());
        dataSource.setIdleTimeout(connectionDetails.idleTimeout());
        dataSource.setMaxLifetime(connectionDetails.maxLifetime());
        dataSource.setLeakDetectionThreshold(connectionDetails.leakDetectionThreshold());
        // Set min/max connections
        dataSource.setMinimumIdle(connectionDetails.minimumConnections());
        dataSource.setMaximumPoolSize(connectionDetails.maximumConnections());

        try {
            dataSource.setLogWriter(new PrintWriter(System.out));
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to set log writer for database...");
            plugin.getLogger().severe(e.getMessage());
        }

        // Add data source properties
        for (Pair<String, String> property : databaseDriver.getDataSourceProperties()) {
            dataSource.addDataSourceProperty(property.getLeft(), property.getRight());
        }

        createTables();
    }

    /**
     * Calls all registered {@link CreateTableFunction CreateTableFunctions} to create all the database tables, provided that they don't
     * exist.
     * <p>
     * This method will fire a {@link PreTablesCreateEvent} before the tables are created and a {@link TablesCreatedEvent} after
     * all the tables are created.
     * <p>
     * After the {@link TablesCreatedEvent} call, this method calls {@link #updateTables()} to ensure that the tables are all updated
     * now that we have ensured the tables exist.
     */
    private void createTables() {
        //Call a pre event for plugins to listen to in case they need to register their functions
        PreTablesCreateEvent preTablesCreateEvent = new PreTablesCreateEvent();
        Bukkit.getPluginManager().callEvent(preTablesCreateEvent);

        if (blockMainThreadOnStart()) {
            try {
                CreateCoreTablesFunction.getCreateCoreTablesFunction().createTables(this).get();

                CompletableFuture<Void>[] completableFutures = new CompletableFuture[createTableFunctions.size()];
                for (int i = 0; i < createTableFunctions.size(); i++) {
                    completableFutures[i] = createTableFunctions.get(i).createTables(this);
                }
                CompletableFuture.allOf(completableFutures).get();

                // Call a post event whenever all tables are created so then the plugins know that their tables now exist
                TablesCreatedEvent tablesCreatedEvent = new TablesCreatedEvent();
                Bukkit.getPluginManager().callEvent(tablesCreatedEvent);
                updateTables();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        } else {

            CompletableFuture<Void>[] completableFutures = new CompletableFuture[createTableFunctions.size()];

            // Force-create the table history table first
            CreateCoreTablesFunction.getCreateCoreTablesFunction().createTables(this).thenAccept(unused -> {
                for (int i = 0; i < createTableFunctions.size(); i++) {
                    completableFutures[i] = createTableFunctions.get(i).createTables(this);
                }
                CompletableFuture<Void> allFuture = CompletableFuture.allOf(completableFutures);

                allFuture.thenAccept(unused1 -> {

                    // Call a post event whenever all tables are created so then the plugins know that their tables now exist
                    TablesCreatedEvent tablesCreatedEvent = new TablesCreatedEvent();
                    Bukkit.getPluginManager().callEvent(tablesCreatedEvent);
                    updateTables();
                });
            });
        }
    }

    /**
     * This method is called at the end of {@link #createTables()} to ensure that all tables exist at the time of this method call.
     * <p>
     * Calls all registered {@link UpdateTableFunction UpdateTableFunctions} to update all the database tables, provided that they are
     * out of date. (Context of what is "out of date" is relative to each plugin and thus why a more anonymous functional appraoch is taken)
     * <p>
     * This method will fire a {@link PreTablesUpdateEvent} before the tables are updated and a {@link TablesUpdatedEvent} after
     * all the tables are updated.
     */
    private void updateTables() {
        //Call a pre event for plugins to listen to in case they need to register their functions
        PreTablesUpdateEvent preTablesUpdateEvent = new PreTablesUpdateEvent();
        Bukkit.getPluginManager().callEvent(preTablesUpdateEvent);

        if (blockMainThreadOnStart()) {
            try {
                // Block thread
                UpdateCoreTablesFunction.getUpdateCoreTablesFunction().updateTables(this).get();
                // Run updates async
                CompletableFuture<Void>[] completableFutures = new CompletableFuture[updateTableFunctions.size()];
                for (int i = 0; i < updateTableFunctions.size(); i++) {
                    completableFutures[i] = updateTableFunctions.get(i).updateTables(this);
                }
                // Block till all are done
                CompletableFuture.allOf(completableFutures).get();
                // Call a post event whenever all tables are updated so then the plugins know that their tables now updated
                TablesUpdatedEvent tablesUpdatedEvent = new TablesUpdatedEvent();
                Bukkit.getPluginManager().callEvent(tablesUpdatedEvent);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        } else {
            CompletableFuture<Void>[] completableFutures = new CompletableFuture[updateTableFunctions.size()];

            // Force-update the table history table first
            UpdateCoreTablesFunction.getUpdateCoreTablesFunction().updateTables(this).thenAccept(unused -> {
                for (int i = 0; i < updateTableFunctions.size(); i++) {
                    completableFutures[i] = updateTableFunctions.get(i).updateTables(this);
                }
                CompletableFuture<Void> allFuture = CompletableFuture.allOf(completableFutures);
                allFuture.thenAccept(unused1 -> {
                    // Call a post event whenever all tables are updated so then the plugins know that their tables now updated
                    TablesUpdatedEvent tablesUpdatedEvent = new TablesUpdatedEvent();
                    Bukkit.getPluginManager().callEvent(tablesUpdatedEvent);
                });
            });
        }
    }

    public boolean tableExists(@NotNull Connection connection, @NotNull String tableName) {
        List<String> tableNames = new ArrayList<>();
        getMetaData(connection).ifPresent(dmd -> {
            try {
                ResultSet rs = dmd.getTables(null, null, null, new String[]{"TABLE"});
                while (rs.next()) {
                    tableNames.add(rs.getString("TABLE_NAME"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return tableNames.stream().anyMatch(s -> s.equalsIgnoreCase(tableName));
    }

    private static Optional<DatabaseMetaData> getMetaData(@NotNull Connection connection) {
        try {
            return Optional.of(connection.getMetaData());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}

package com.diamonddagger590.mccore.database;

import com.diamonddagger590.mccore.database.builder.Database;
import com.diamonddagger590.mccore.database.builder.DatabaseDriver;
import com.diamonddagger590.mccore.database.function.CreateTableFunction;
import com.diamonddagger590.mccore.database.function.DatabaseInitializationFunction;
import com.diamonddagger590.mccore.database.function.UpdateTableFunction;
import com.diamonddagger590.mccore.database.table.function.CreateCoreTablesFunction;
import com.diamonddagger590.mccore.database.table.function.UpdateCoreTablesFunction;
import com.diamonddagger590.mccore.event.database.PreTablesCreateEvent;
import com.diamonddagger590.mccore.event.database.PreTablesUpdateEvent;
import com.diamonddagger590.mccore.event.database.TablesCreatedEvent;
import com.diamonddagger590.mccore.event.database.TablesUpdatedEvent;
import com.diamonddagger590.mccore.exception.CoreDatabaseInitializationException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class serves as a central manager of all database code for any plugin
 * relying on this plugin.
 * <p>
 * This class is in charge of creating the database, creating tables and updating tables
 * as well as serving as the primary access point to the {@link Database} object.
 */
public abstract class DatabaseManager {

    protected Plugin plugin;
    private Database database;
    private final ThreadPoolExecutor databaseExecutorService =
            new ThreadPoolExecutor(1, 4, 30, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    private final List<CreateTableFunction> createTableFunctions;
    private final List<UpdateTableFunction> updateTableFunctions;

    public DatabaseManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.createTableFunctions = new ArrayList<>();
        this.updateTableFunctions = new ArrayList<>();
    }

    @NotNull
    public abstract DatabaseInitializationFunction getDatabaseInitializationFunction();

    /**
     * Gets the {@link DatabaseDriver} used to initialize the database
     *
     * @return The {@link DatabaseDriver} used to initialize the database
     */
    @NotNull
    public abstract DatabaseDriver getDriver();

    /**
     * Initialize the database with the {@link DatabaseDriver} provided by {@link #getDriver()}
     */
    public final void initializeDatabase() throws CoreDatabaseInitializationException {

        Optional<Database> databaseOptional = getDatabaseInitializationFunction().initialize(getDriver());

        if (databaseOptional.isPresent()) {
            this.database = databaseOptional.get();
        } else {
            throw new CoreDatabaseInitializationException("Database encountered an exception when initializing... please reach out to the developer of the " + plugin.getName() + " plugin.");
        }

        createTables();
    }

    /**
     * Adds a {@link CreateTableFunction} to the list of functions that will be executed whenever
     * {@link #createTables()} is called.
     *
     * @param createTableFunction The {@link CreateTableFunction} to add
     */
    public final void addCreateTableFunction(@NotNull CreateTableFunction createTableFunction) {
        this.createTableFunctions.add(createTableFunction);
    }

    /**
     * Adds a {@link UpdateTableFunction} to the list of functions that will be executed whenever {@link #updateTables()}
     * is called.
     *
     * @param updateTableFunction The {@link UpdateTableFunction} to add
     */
    public final void addUpdateTableFunction(@NotNull UpdateTableFunction updateTableFunction) {
        this.updateTableFunctions.add(updateTableFunction);
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
    public final void createTables() {

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
    public final void updateTables() {

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

    protected boolean blockMainThreadOnStart() {
        return true;
    }

    /**
     * Gets the {@link Database} that is being used
     *
     * @return The {@link Database} that is being used
     */
    @Nullable
    public Database getDatabase() {
        return database;
    }

    /**
     * Gets the {@link ThreadPoolExecutor} used to run database queries
     *
     * @return The {@link ThreadPoolExecutor} used to run database queries
     */
    @NotNull
    public ThreadPoolExecutor getDatabaseExecutorService() {
        return databaseExecutorService;
    }
}

package com.diamonddagger590.mccore;

import com.diamonddagger590.mccore.database.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class CorePlugin extends JavaPlugin {

    private static CorePlugin instance;
    protected DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {
        databaseManager.getDatabaseExecutorService().shutdown();
    }

    /**
     * Initializes the databases for the plugin.
     *
     * It is up to the plugin implementing this on the {@link #onEnable()} method
     */
    public abstract void initializeDatabase();

    /**
     * Get the {@link DatabaseManager} used by the plugin
     *
     * @return The {@link DatabaseManager} used by the plugin
     */
    @NotNull
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    @NotNull
    public static CorePlugin getInstance() {
        if (instance == null) {
            throw new NullPointerException("Plugin was not initialized.");
        }
        return instance;
    }
}

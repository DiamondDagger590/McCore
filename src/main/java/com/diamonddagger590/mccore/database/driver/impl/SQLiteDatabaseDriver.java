package com.diamonddagger590.mccore.database.driver.impl;

import com.diamonddagger590.mccore.database.Credentials;
import com.diamonddagger590.mccore.database.driver.DatabaseDriverType;
import com.diamonddagger590.mccore.database.driver.DatabaseDriver;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

/**
 * A base database driver to support SQLite
 */
public abstract class SQLiteDatabaseDriver implements DatabaseDriver {

    private static final String RAW_CONNECTION_URL = "jdbc:%s:%s.db";

    @NotNull
    @Override
    public final String getDatabaseDriverClass() {
        return "org.sqlite.JDBC";
    }

    @NotNull
    @Override
    public final String getConnectionUrl(@NotNull Credentials credentials) {
        return String.format(RAW_CONNECTION_URL, "sqlite", getPath());
    }

    @Override
    public void populateDataSourceCredentials(@NotNull HikariDataSource dataSource, @NotNull Credentials credentials) {
        // Sqlite so don't need passwords
    }

    @NotNull
    public abstract String getPath();

    @NotNull
    @Override
    public DatabaseDriverType getDriverType() {
        return DatabaseDriverType.SQLITE;
    }
}

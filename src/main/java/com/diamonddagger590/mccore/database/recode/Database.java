package com.diamonddagger590.mccore.database.recode;

import com.diamonddagger590.mccore.database.builder.DatabaseDriver;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

public class Database {

    private final HikariDataSource dataSource;

    public Database() {
        dataSource = new HikariDataSource();
    }

    private void initializeDatabase(@NotNull Credentials credentials, @NotNull DatabaseDriver databaseDriver) {
    }
}

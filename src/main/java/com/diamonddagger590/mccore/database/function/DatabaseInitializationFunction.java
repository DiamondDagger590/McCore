package com.diamonddagger590.mccore.database.function;

import com.diamonddagger590.mccore.database.builder.Database;
import com.diamonddagger590.mccore.database.builder.DatabaseDriver;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface DatabaseInitializationFunction {

    @NotNull
    public Optional<Database> initialize(@NotNull DatabaseDriver databaseDriver);
}

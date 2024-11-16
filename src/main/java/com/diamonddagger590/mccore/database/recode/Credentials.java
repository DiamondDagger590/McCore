package com.diamonddagger590.mccore.database.recode;

import org.jetbrains.annotations.NotNull;

public record Credentials(@NotNull String host, @NotNull String database, @NotNull String password) {
}

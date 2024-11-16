package com.diamonddagger590.mccore.database;

import org.jetbrains.annotations.NotNull;

/**
 * A record containing all the credentials needed to initialize a {@link Database}.
 *
 * @param host     The host for the database connection.
 * @param port     The port for the database connection.
 * @param database The database name for the database connection.
 * @param username The username for the database connection.
 * @param password The password for the database connection.
 */
public record Credentials(@NotNull String host, int port, @NotNull String database, @NotNull String username,
                          @NotNull String password) {
}

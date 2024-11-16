package com.diamonddagger590.mccore.database;

/**
 * A record containing details about a {@link Database}'s connections.
 *
 * @param connectionTimeout      The amount of time before the database stops waiting for a new connection in millis.
 * @param idleTimeout            The amount of time a connection can be idling before it closes in millis. A value of 0 disables this feature.
 * @param maxLifetime            The maximum duration of a connection in millis.
 * @param minimumConnections     The minimum amount of connections for the database to keep idling.
 * @param maximumConnections     The maximum amount of connections the database is allowed to have at once.
 * @param leakDetectionThreshold The amount of time that a connection can be out of the pool before a message is logged indicating a possible connection leak in millis. {@code 0} means it is disabled.
 */
public record ConnectionDetails(int connectionTimeout, int idleTimeout, int maxLifetime, int minimumConnections,
                                int maximumConnections, int leakDetectionThreshold) {
}

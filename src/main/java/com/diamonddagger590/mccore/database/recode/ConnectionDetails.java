package com.diamonddagger590.mccore.database.recode;

public record ConnectionDetails(int connectionTimeout, int idleTimeout, int maxLifetime, int minimumConnections, int maximumConnections, int leakDetectionThreshold) {
}

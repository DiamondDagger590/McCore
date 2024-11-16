package com.diamonddagger590.mccore.database.driver;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

/**
 * The types for different {@link DatabaseDriver}s
 */
public enum DatabaseDriverType {

    SQLITE("sqlite"),
    ;

    private final String driverName;

    DatabaseDriverType(@NotNull String driverName) {
        this.driverName = driverName;
    }

    /**
     * Gets the driver name.
     *
     * @return The driver name.
     */
    @NotNull
    public String getDriverName() {
        return driverName;
    }

    /**
     * Gets an {@link Optional} containing the driver type matching the provided string.
     *
     * @param driverName The name of the driver type.
     * @return An {@link Optional} containing the driver type matching the provided string, or an empty
     * optional if no matches were found.
     */
    @NotNull
    public static Optional<DatabaseDriverType> fromString(@NotNull String driverName) {
        return Arrays.stream(values()).filter(driverType -> driverType.getDriverName().equalsIgnoreCase(driverName)).findFirst();
    }
}

package com.diamonddagger590.mccore.database.driver;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.Database;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This manager handles all {@link DatabaseDriver}s for a plugin.
 * <p>
 * Any driver that is allowed to be used in a {@link Database} should be registered
 * here.
 */
public final class DriverManager {

    private final CorePlugin plugin;
    private final Map<DatabaseDriverType, DatabaseDriver> registeredDrivers;

    public DriverManager(@NotNull CorePlugin corePlugin) {
        this.plugin = corePlugin;
        this.registeredDrivers = new HashMap<>();
    }

    /**
     * Registers the provided {@link DatabaseDriver} to be used.
     *
     * @param databaseDriver The {@link DatabaseDriver} to register.
     * @throws RuntimeException If {@link DatabaseDriver#tryDriver()} returns {@code false}.
     */
    public void registerDriver(@NotNull DatabaseDriver databaseDriver) {
        if (!databaseDriver.tryDriver()) {
            throw new RuntimeException(String.format("Driver class for %s is missing and therefore is unable to be registered...", databaseDriver.getDriverType().getDriverName()));
        }
        registeredDrivers.put(databaseDriver.getDriverType(), databaseDriver);
    }

    /**
     * Checks to see if the provided {@link DatabaseDriver} is registered.
     *
     * @param databaseDriver The {@link DatabaseDriver} to check.
     * @return {@code true} if the provided {@link DatabaseDriver} is registered.
     */
    public boolean isDriverRegistered(@NotNull DatabaseDriver databaseDriver) {
        return registeredDrivers.containsValue(databaseDriver);
    }

    /**
     * Checks to see if the provided {@link DatabaseDriverType} has an associated {@link DatabaseDriver}
     * registered.
     *
     * @param databaseDriverType The {@link DatabaseDriverType} to check.
     * @return {@code true} if the provided {@link DatabaseDriverType} has an associated {@link DatabaseDriver} registered.
     */
    public boolean isDriverRegistered(@NotNull DatabaseDriverType databaseDriverType) {
        return registeredDrivers.containsKey(databaseDriverType);
    }

    /**
     * Gets an {@link Optional} containing a {@link DatabaseDriver} associated with the provided {@link DatabaseDriverType}.
     *
     * @param databaseDriverType The {@link DatabaseDriverType} to get a {@link DatabaseDriver} for.
     * @return An {@link Optional} containing a {@link DatabaseDriver} associated with the provided {@link DatabaseDriverType} or
     * an empty optional if no matches are found.
     */
    @NotNull
    public Optional<DatabaseDriver> getDriver(@NotNull DatabaseDriverType databaseDriverType) {
        return Optional.ofNullable(registeredDrivers.get(databaseDriverType));
    }
}

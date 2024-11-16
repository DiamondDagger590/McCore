package com.diamonddagger590.mccore.database.driver;

import com.diamonddagger590.mccore.database.Credentials;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.pair.Pair;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A database drover that can be used to initialize a {@link Database}.
 */
public interface DatabaseDriver {

    /**
     * Gets the database driver class for this driver.
     *
     * @return The database driver class for this driver.
     */
    @NotNull
    String getDatabaseDriverClass();

    /**
     * Gets the connection url for this driver.
     *
     * @param credentials The credentials to use when getting the connection url.
     * @return The connection url for this driver.
     */
    @NotNull
    String getConnectionUrl(@NotNull Credentials credentials);

    /**
     * Populates the {@link HikariDataSource} with the provided {@link Credentials} as is needed for this driver.
     *
     * @param dataSource  The {@link HikariDataSource} to populate.
     * @param credentials The {@link Credentials} to use for populating.
     */
    void populateDataSourceCredentials(@NotNull HikariDataSource dataSource, @NotNull Credentials credentials);

    /**
     * Gets the {@link DatabaseDriverType} that this driver represents.
     *
     * @return The {@link DatabaseDriverType} that this driver represents.
     */
    @NotNull
    DatabaseDriverType getDriverType();

    /**
     * Attempts to initialize the class provided by {@link #getDatabaseDriverClass()} in order to validate
     * that the driver can be initialized.
     *
     * @return {@code true} if the driver can be initialized.
     */
    default boolean tryDriver() {
        try {
            Class.forName(getDatabaseDriverClass()).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            return false;
        }
        return true;
    }

    /**
     * Gets a {@link List} of {@link Pair}s for all the data source properties to use for this driver.
     * <p>
     * Pair Structure:
     * Left -> Name of property
     * Right -> Value of property
     *
     * @return A {@link List} of {@link Pair}s for all the data source properties to use for this driver.
     */
    @NotNull
    default List<Pair<String, String>> getDataSourceProperties() {
        return new ArrayList<>();
    }
}

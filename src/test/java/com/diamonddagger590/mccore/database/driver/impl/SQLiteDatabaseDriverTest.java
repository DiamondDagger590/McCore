package com.diamonddagger590.mccore.database.driver.impl;

import com.diamonddagger590.mccore.database.Credentials;
import com.diamonddagger590.mccore.database.driver.DatabaseDriver;
import com.diamonddagger590.mccore.database.driver.DatabaseDriverType;
import com.diamonddagger590.mccore.pair.Pair;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLiteDatabaseDriverTest {

    private static final String TEST_PATH = "plugins/MyPlugin/data";

    private SQLiteDatabaseDriver driver;

    @BeforeEach
    void setUp() {
        driver = new SQLiteDatabaseDriver() {
            @Override
            public String getPath() {
                return TEST_PATH;
            }
        };
    }

    @Test
    @DisplayName("Given a SQLiteDatabaseDriver, when getDatabaseDriverClass called, then returns the SQLite JDBC class")
    void getDatabaseDriverClass_returnsSqliteJdbcClass() {
        assertEquals("org.sqlite.JDBC", driver.getDatabaseDriverClass());
    }

    @Test
    @DisplayName("Given a SQLiteDatabaseDriver, when getConnectionUrl called, then returns URL with jdbc:sqlite prefix and path")
    void getConnectionUrl_returnsFormattedSqliteUrl() {
        Credentials credentials = new Credentials("localhost", 3306, "testdb", "user", "pass");
        String url = driver.getConnectionUrl(credentials);
        assertEquals("jdbc:sqlite:" + TEST_PATH + ".db", url);
    }

    @Test
    @DisplayName("Given a SQLiteDatabaseDriver, when getDriverType called, then returns SQLITE")
    void getDriverType_returnsSqlite() {
        assertEquals(DatabaseDriverType.SQLITE, driver.getDriverType());
    }

    @Test
    @DisplayName("Given a SQLiteDatabaseDriver, when populateDataSourceCredentials called, then does not throw")
    void populateDataSourceCredentials_doesNotThrow() {
        Credentials credentials = new Credentials("localhost", 3306, "testdb", "user", "pass");
        HikariDataSource dataSource = new HikariDataSource();
        try {
            assertDoesNotThrow(() -> driver.populateDataSourceCredentials(dataSource, credentials));
        } finally {
            dataSource.close();
        }
    }

    @Test
    @DisplayName("Given a SQLiteDatabaseDriver, when getPath called, then returns the configured path")
    void getPath_returnsConfiguredPath() {
        assertEquals(TEST_PATH, driver.getPath());
    }

    // --- DatabaseDriver default methods ---

    @Test
    @DisplayName("Given a SQLiteDatabaseDriver, when getDataSourceProperties called, then returns an empty list")
    void getDataSourceProperties_returnsEmptyList() {
        List<Pair<String, String>> properties = driver.getDataSourceProperties();
        assertNotNull(properties);
        assertTrue(properties.isEmpty());
    }

    @Test
    @DisplayName("Given a SQLiteDatabaseDriver, when tryDriver called, then returns true when JDBC driver is on classpath")
    void tryDriver_returnsTrue_whenDriverIsOnClasspath() {
        assertTrue(driver.tryDriver());
    }

    @Test
    @DisplayName("Given a DatabaseDriver with a valid classpath class, when tryDriver called, then returns true")
    void tryDriver_returnsTrue_whenDriverClassIsOnClasspath() {
        DatabaseDriver validDriver = new DatabaseDriver() {
            @NotNull
            @Override
            public String getDatabaseDriverClass() {
                return "java.lang.Object";
            }

            @NotNull
            @Override
            public String getConnectionUrl(@NotNull Credentials credentials) {
                return "";
            }

            @Override
            public void populateDataSourceCredentials(@NotNull HikariDataSource dataSource, @NotNull Credentials credentials) {}

            @NotNull
            @Override
            public DatabaseDriverType getDriverType() {
                return DatabaseDriverType.SQLITE;
            }
        };
        assertTrue(validDriver.tryDriver());
    }
}

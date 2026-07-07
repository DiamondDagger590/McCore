package com.diamonddagger590.mccore.database.driver;

import com.diamonddagger590.mccore.database.Credentials;
import com.diamonddagger590.mccore.pair.Pair;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseDriverDefaultsTest {

    private static class ValidDriverClassDriver implements DatabaseDriver {
        @Override
        public @NotNull String getDatabaseDriverClass() {
            return "java.lang.String";
        }

        @Override
        public @NotNull String getConnectionUrl(@NotNull Credentials credentials) {
            return "jdbc:test://localhost";
        }

        @Override
        public void populateDataSourceCredentials(@NotNull HikariDataSource dataSource, @NotNull Credentials credentials) {
        }

        @Override
        public @NotNull DatabaseDriverType getDriverType() {
            return DatabaseDriverType.SQLITE;
        }
    }

    private static class InvalidDriverClassDriver implements DatabaseDriver {
        @Override
        public @NotNull String getDatabaseDriverClass() {
            return "com.nonexistent.FakeDriver";
        }

        @Override
        public @NotNull String getConnectionUrl(@NotNull Credentials credentials) {
            return "jdbc:fake://localhost";
        }

        @Override
        public void populateDataSourceCredentials(@NotNull HikariDataSource dataSource, @NotNull Credentials credentials) {
        }

        @Override
        public @NotNull DatabaseDriverType getDriverType() {
            return DatabaseDriverType.SQLITE;
        }
    }

    @Test
    @DisplayName("Given a valid driver class, when tryDriver is called, then returns true")
    void tryDriver_returnsTrue_whenDriverClassExists() {
        DatabaseDriver driver = new ValidDriverClassDriver();
        assertTrue(driver.tryDriver());
    }

    private static class AbstractDriverClassDriver implements DatabaseDriver {
        @Override
        public @NotNull String getDatabaseDriverClass() {
            return "java.lang.Number";
        }

        @Override
        public @NotNull String getConnectionUrl(@NotNull Credentials credentials) {
            return "jdbc:abstract://localhost";
        }

        @Override
        public void populateDataSourceCredentials(@NotNull HikariDataSource dataSource, @NotNull Credentials credentials) {
        }

        @Override
        public @NotNull DatabaseDriverType getDriverType() {
            return DatabaseDriverType.SQLITE;
        }
    }

    @Test
    @DisplayName("Given an invalid driver class, when tryDriver is called, then returns false")
    void tryDriver_returnsFalse_whenDriverClassDoesNotExist() {
        DatabaseDriver driver = new InvalidDriverClassDriver();
        assertFalse(driver.tryDriver());
    }

    @Test
    @DisplayName("Given an abstract driver class, when tryDriver is called, then returns false")
    void tryDriver_returnsFalse_whenDriverClassCannotBeInstantiated() {
        DatabaseDriver driver = new AbstractDriverClassDriver();
        assertFalse(driver.tryDriver());
    }

    @Test
    @DisplayName("Given a default implementation, when getDataSourceProperties is called, then returns empty list")
    void getDataSourceProperties_returnsEmptyList_byDefault() {
        DatabaseDriver driver = new ValidDriverClassDriver();
        List<Pair<String, String>> properties = driver.getDataSourceProperties();
        assertTrue(properties.isEmpty());
    }

    @Test
    @DisplayName("Given a default implementation, when getDataSourceProperties is called multiple times, then returns independent lists")
    void getDataSourceProperties_returnsNewListEachTime() {
        DatabaseDriver driver = new ValidDriverClassDriver();
        List<Pair<String, String>> first = driver.getDataSourceProperties();
        List<Pair<String, String>> second = driver.getDataSourceProperties();
        assertEquals(first, second);
        assertNotSame(first, second);
    }
}

package com.diamonddagger590.mccore.database.driver;

import com.diamonddagger590.mccore.database.Credentials;
import com.diamonddagger590.mccore.pair.Pair;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriverRegistryTest {

    private static class TestDriver implements DatabaseDriver {

        private final boolean driverAvailable;

        TestDriver(boolean driverAvailable) {
            this.driverAvailable = driverAvailable;
        }

        @NotNull
        @Override
        public String getDatabaseDriverClass() {
            return "org.sqlite.JDBC";
        }

        @NotNull
        @Override
        public String getConnectionUrl(@NotNull Credentials credentials) {
            return "jdbc:sqlite:test.db";
        }

        @Override
        public void populateDataSourceCredentials(@NotNull HikariDataSource dataSource, @NotNull Credentials credentials) {
        }

        @NotNull
        @Override
        public DatabaseDriverType getDriverType() {
            return DatabaseDriverType.SQLITE;
        }

        @Override
        public boolean tryDriver() {
            return driverAvailable;
        }

        @NotNull
        @Override
        public List<Pair<String, String>> getDataSourceProperties() {
            return List.of();
        }
    }

    private DriverRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DriverRegistry();
    }

    @Test
    @DisplayName("Given a valid driver, when registering, then registration succeeds")
    void register_succeeds_whenDriverIsValid() {
        TestDriver driver = new TestDriver(true);
        registry.register(driver);
        assertTrue(registry.registered(driver));
    }

    @Test
    @DisplayName("Given a driver whose tryDriver fails, when registering, then throws RuntimeException")
    void register_throwsRuntimeException_whenDriverClassMissing() {
        TestDriver driver = new TestDriver(false);
        assertThrows(RuntimeException.class, () -> registry.register(driver));
    }

    @Test
    @DisplayName("Given a registered driver, when checking registered by instance, then returns true")
    void registered_returnsTrue_whenDriverIsRegistered() {
        TestDriver driver = new TestDriver(true);
        registry.register(driver);
        assertTrue(registry.registered(driver));
    }

    @Test
    @DisplayName("Given no registered drivers, when checking registered by instance, then returns false")
    void registered_returnsFalse_whenDriverIsNotRegistered() {
        assertFalse(registry.registered(new TestDriver(true)));
    }

    @Test
    @DisplayName("Given a registered driver, when checking by driver type, then returns true")
    void isDriverRegistered_returnsTrue_whenDriverTypeIsRegistered() {
        registry.register(new TestDriver(true));
        assertTrue(registry.isDriverRegistered(DatabaseDriverType.SQLITE));
    }

    @Test
    @DisplayName("Given no registered drivers, when checking by driver type, then returns false")
    void isDriverRegistered_returnsFalse_whenDriverTypeIsNotRegistered() {
        assertFalse(registry.isDriverRegistered(DatabaseDriverType.SQLITE));
    }

    @Test
    @DisplayName("Given a registered driver, when getting by driver type, then returns Optional with driver")
    void getDriver_returnsPresent_whenDriverTypeIsRegistered() {
        TestDriver driver = new TestDriver(true);
        registry.register(driver);
        Optional<DatabaseDriver> result = registry.getDriver(DatabaseDriverType.SQLITE);
        assertTrue(result.isPresent());
        assertEquals(driver, result.get());
    }

    @Test
    @DisplayName("Given no registered drivers, when getting by driver type, then returns empty Optional")
    void getDriver_returnsEmpty_whenDriverTypeIsNotRegistered() {
        Optional<DatabaseDriver> result = registry.getDriver(DatabaseDriverType.SQLITE);
        assertFalse(result.isPresent());
    }
}

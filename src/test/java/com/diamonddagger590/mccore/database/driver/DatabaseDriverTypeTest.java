package com.diamonddagger590.mccore.database.driver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseDriverTypeTest {

    @Test
    @DisplayName("Given SQLITE driver type, when getting driver name, then returns sqlite")
    void getDriverName_returnsSqlite_forSqliteType() {
        assertEquals("sqlite", DatabaseDriverType.SQLITE.getDriverName());
    }

    @Test
    @DisplayName("Given valid driver name string, when calling fromString, then returns matching type")
    void fromString_returnsMatchingType_whenValidNameProvided() {
        Optional<DatabaseDriverType> result = DatabaseDriverType.fromString("sqlite");
        assertTrue(result.isPresent());
        assertEquals(DatabaseDriverType.SQLITE, result.get());
    }

    @Test
    @DisplayName("Given valid driver name with different case, when calling fromString, then returns matching type")
    void fromString_returnsMatchingType_whenCaseIsDifferent() {
        Optional<DatabaseDriverType> result = DatabaseDriverType.fromString("SQLITE");
        assertTrue(result.isPresent());
        assertEquals(DatabaseDriverType.SQLITE, result.get());
    }

    @Test
    @DisplayName("Given mixed case driver name, when calling fromString, then returns matching type")
    void fromString_returnsMatchingType_whenMixedCase() {
        Optional<DatabaseDriverType> result = DatabaseDriverType.fromString("SqLiTe");
        assertTrue(result.isPresent());
        assertEquals(DatabaseDriverType.SQLITE, result.get());
    }

    @Test
    @DisplayName("Given invalid driver name, when calling fromString, then returns empty optional")
    void fromString_returnsEmpty_whenInvalidNameProvided() {
        Optional<DatabaseDriverType> result = DatabaseDriverType.fromString("mysql");
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Given empty string, when calling fromString, then returns empty optional")
    void fromString_returnsEmpty_whenEmptyStringProvided() {
        Optional<DatabaseDriverType> result = DatabaseDriverType.fromString("");
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Given values enumeration, when checking, then contains SQLITE")
    void values_containsSqlite() {
        DatabaseDriverType[] values = DatabaseDriverType.values();
        assertEquals(1, values.length);
        assertEquals(DatabaseDriverType.SQLITE, values[0]);
    }
}

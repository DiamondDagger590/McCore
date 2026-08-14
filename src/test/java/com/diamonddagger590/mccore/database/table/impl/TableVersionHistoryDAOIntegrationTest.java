package com.diamonddagger590.mccore.database.table.impl;

import com.diamonddagger590.mccore.database.Database;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link TableVersionHistoryDAO} using a real in-memory SQLite database.
 * Unlike the mock-based tests in {@link TableVersionHistoryDAOTest}, these verify actual SQL
 * execution and data roundtrip correctness.
 */
class TableVersionHistoryDAOIntegrationTest {

    private Connection connection;
    private Database mockDatabase;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        mockDatabase = mock(Database.class);
        when(mockDatabase.tableExists(any(Connection.class), eq("table_history"))).thenReturn(false);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Nested
    @DisplayName("attemptCreateTable")
    class AttemptCreateTable {

        @Test
        @DisplayName("Given no existing table, when attemptCreateTable is called, then creates table and returns true")
        void attemptCreateTable_createsTable_whenTableDoesNotExist() {
            boolean result = TableVersionHistoryDAO.attemptCreateTable(connection, mockDatabase);
            assertTrue(result);
        }

        @Test
        @DisplayName("Given table already exists, when attemptCreateTable is called, then returns false")
        void attemptCreateTable_returnsFalse_whenTableAlreadyExists() {
            TableVersionHistoryDAO.attemptCreateTable(connection, mockDatabase);

            when(mockDatabase.tableExists(any(Connection.class), eq("table_history"))).thenReturn(true);
            boolean result = TableVersionHistoryDAO.attemptCreateTable(connection, mockDatabase);
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("getLatestVersion and setTableVersion roundtrip")
    class VersionRoundtrip {

        @BeforeEach
        void createTable() {
            TableVersionHistoryDAO.attemptCreateTable(connection, mockDatabase);
        }

        @Test
        @DisplayName("Given no version stored, when getLatestVersion is called, then returns 0")
        void getLatestVersion_returnsZero_whenNoVersionStored() {
            int version = TableVersionHistoryDAO.getLatestVersion(connection, "some_table");
            assertEquals(0, version);
        }

        @Test
        @DisplayName("Given a version is set, when getLatestVersion is called, then returns that version")
        void setAndGetVersion_roundTrips() {
            TableVersionHistoryDAO.setTableVersion(connection, "test_table", 3);

            int version = TableVersionHistoryDAO.getLatestVersion(connection, "test_table");
            assertEquals(3, version);
        }

        @Test
        @DisplayName("Given a version is updated, when getLatestVersion is called, then returns updated version")
        void setTableVersion_updatesExistingVersion() {
            TableVersionHistoryDAO.setTableVersion(connection, "test_table", 1);
            TableVersionHistoryDAO.setTableVersion(connection, "test_table", 2);

            int version = TableVersionHistoryDAO.getLatestVersion(connection, "test_table");
            assertEquals(2, version);
        }

        @Test
        @DisplayName("Given multiple tables with versions, when querying each, then returns correct version per table")
        void multipleTablesHaveIndependentVersions() {
            TableVersionHistoryDAO.setTableVersion(connection, "table_a", 5);
            TableVersionHistoryDAO.setTableVersion(connection, "table_b", 10);

            assertEquals(5, TableVersionHistoryDAO.getLatestVersion(connection, "table_a"));
            assertEquals(10, TableVersionHistoryDAO.getLatestVersion(connection, "table_b"));
        }

        @Test
        @DisplayName("Given setTableVersion succeeds, when called, then returns true")
        void setTableVersion_returnsTrue_onSuccess() {
            boolean result = TableVersionHistoryDAO.setTableVersion(connection, "test_table", 1);
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("updateTable")
    class UpdateTable {

        @BeforeEach
        void createTable() {
            TableVersionHistoryDAO.attemptCreateTable(connection, mockDatabase);
        }

        @Test
        @DisplayName("Given table_history has no version for itself, when updateTable is called, then sets version to 1")
        void updateTable_setsVersionToOne_whenNoVersionExists() {
            TableVersionHistoryDAO.updateTable(connection);

            int version = TableVersionHistoryDAO.getLatestVersion(connection, "table_history");
            assertEquals(1, version);
        }

        @Test
        @DisplayName("Given table_history is already at version 1, when updateTable is called, then version remains 1")
        void updateTable_doesNothing_whenAlreadyCurrent() {
            TableVersionHistoryDAO.setTableVersion(connection, "table_history", 1);
            TableVersionHistoryDAO.updateTable(connection);

            int version = TableVersionHistoryDAO.getLatestVersion(connection, "table_history");
            assertEquals(1, version);
        }
    }
}

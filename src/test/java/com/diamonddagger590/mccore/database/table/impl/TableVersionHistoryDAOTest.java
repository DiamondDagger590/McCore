package com.diamonddagger590.mccore.database.table.impl;

import com.diamonddagger590.mccore.database.Database;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableVersionHistoryDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private Database mockDatabase;

    @Nested
    @DisplayName("getLatestVersion")
    class GetLatestVersion {

        @Test
        @DisplayName("Given a table with version 3 stored, When getLatestVersion is called, Then it returns 3")
        void returnsStoredVersion() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getInt("table_version")).thenReturn(3);

            int version = TableVersionHistoryDAO.getLatestVersion(mockConnection, "test_table");

            assertEquals(3, version);
            verify(mockStatement).setString(1, "test_table");
        }

        @Test
        @DisplayName("Given a table with no version stored, When getLatestVersion is called, Then it returns 0")
        void returnsZeroForMissingTable() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            int version = TableVersionHistoryDAO.getLatestVersion(mockConnection, "nonexistent_table");

            assertEquals(0, version);
        }

        @Test
        @DisplayName("Given a SQL exception occurs, When getLatestVersion is called, Then it returns 0")
        void returnsZeroOnSqlException() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Connection failed"));

            int version = TableVersionHistoryDAO.getLatestVersion(mockConnection, "test_table");

            assertEquals(0, version);
        }

        @Test
        @DisplayName("Given multiple rows returned, When getLatestVersion is called, Then it returns the last row's version")
        void returnsLastRowVersion() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getInt("table_version")).thenReturn(1, 5);

            int version = TableVersionHistoryDAO.getLatestVersion(mockConnection, "test_table");

            assertEquals(5, version);
        }
    }

    @Nested
    @DisplayName("setTableVersion")
    class SetTableVersion {

        @Test
        @DisplayName("Given a valid connection, When setTableVersion is called, Then it returns true")
        void returnsTrueOnSuccess() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            boolean result = TableVersionHistoryDAO.setTableVersion(mockConnection, "test_table", 2);

            assertTrue(result);
            verify(mockStatement).setString(1, "test_table");
            verify(mockStatement).setInt(3, 2);
            verify(mockStatement).executeUpdate();
        }

        @Test
        @DisplayName("Given a SQL exception occurs, When setTableVersion is called, Then it returns false")
        void returnsFalseOnSqlException() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Write failed"));

            boolean result = TableVersionHistoryDAO.setTableVersion(mockConnection, "test_table", 1);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given a statement execution failure, When setTableVersion is called, Then it returns false")
        void returnsFalseOnExecuteFailure() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenThrow(new SQLException("Execute failed"));

            boolean result = TableVersionHistoryDAO.setTableVersion(mockConnection, "test_table", 1);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("attemptCreateTable")
    class AttemptCreateTable {

        @Test
        @DisplayName("Given the table already exists, When attemptCreateTable is called, Then it returns false")
        void returnsFalseWhenTableExists() {
            when(mockDatabase.tableExists(mockConnection, "table_history")).thenReturn(true);

            boolean result = TableVersionHistoryDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given the table doesn't exist, When attemptCreateTable is called, Then it creates the table and returns true")
        void returnsTrueWhenTableCreated() throws SQLException {
            when(mockDatabase.tableExists(mockConnection, "table_history")).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            boolean result = TableVersionHistoryDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertTrue(result);
            verify(mockStatement).executeUpdate();
        }

        @Test
        @DisplayName("Given a SQL exception during creation, When attemptCreateTable is called, Then it returns false")
        void returnsFalseOnSqlException() throws SQLException {
            when(mockDatabase.tableExists(mockConnection, "table_history")).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Create failed"));

            boolean result = TableVersionHistoryDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("updateTable")
    class UpdateTable {

        @Test
        @DisplayName("Given the table is at current version, When updateTable is called, Then no updates are performed")
        void noUpdateWhenCurrent() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getInt("table_version")).thenReturn(1);

            TableVersionHistoryDAO.updateTable(mockConnection);

            // getLatestVersion is called but no setTableVersion since version is already current
            verify(mockStatement).setString(1, "table_history");
        }

        @Test
        @DisplayName("Given the table is at version 0, When updateTable is called, Then it updates to version 1")
        void updatesFromVersion0To1() throws SQLException {
            // First call: getLatestVersion returns 0
            PreparedStatement selectStatement = org.mockito.Mockito.mock(PreparedStatement.class);
            ResultSet selectResultSet = org.mockito.Mockito.mock(ResultSet.class);
            when(selectStatement.executeQuery()).thenReturn(selectResultSet);
            when(selectResultSet.next()).thenReturn(false);

            // Second call: setTableVersion
            PreparedStatement updateStatement = org.mockito.Mockito.mock(PreparedStatement.class);

            when(mockConnection.prepareStatement(anyString()))
                    .thenReturn(selectStatement)
                    .thenReturn(updateStatement);

            TableVersionHistoryDAO.updateTable(mockConnection);

            verify(updateStatement).setString(1, "table_history");
            verify(updateStatement).setInt(3, 1);
            verify(updateStatement).executeUpdate();
        }

        @Test
        @DisplayName("Given the table is above current version, When updateTable is called, Then no updates are performed")
        void noUpdateWhenAboveCurrent() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getInt("table_version")).thenReturn(5);

            TableVersionHistoryDAO.updateTable(mockConnection);

            verify(mockStatement).setString(1, "table_history");
        }
    }
}

package com.diamonddagger590.mccore.database.table.impl;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.player.CorePlayer;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MutexDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private Database mockDatabase;

    @Mock
    private CorePlayer mockCorePlayer;

    private static final UUID TEST_UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc");

    @Nested
    @DisplayName("attemptCreateTable")
    class AttemptCreateTable {

        @Test
        @DisplayName("Given the table already exists, When attemptCreateTable is called, Then it returns false")
        void returnsFalseWhenTableExists() {
            when(mockDatabase.tableExists(mockConnection, "player_mutex")).thenReturn(true);

            boolean result = MutexDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given the table doesn't exist, When attemptCreateTable is called, Then it creates the table and returns true")
        void returnsTrueWhenTableCreated() throws SQLException {
            when(mockDatabase.tableExists(mockConnection, "player_mutex")).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            boolean result = MutexDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertTrue(result);
            verify(mockStatement).executeUpdate();
        }

        @Test
        @DisplayName("Given a SQL exception during creation, When attemptCreateTable is called, Then it returns false")
        void returnsFalseOnSqlException() throws SQLException {
            when(mockDatabase.tableExists(mockConnection, "player_mutex")).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Create failed"));

            boolean result = MutexDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("updateTable")
    class UpdateTable {

        @Test
        @DisplayName("Given the table is at current version, When updateTable is called, Then no updates are performed")
        void noUpdateWhenCurrent() throws SQLException {
            // getLatestVersion for "player_mutex" returns 1 (current version)
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getInt("table_version")).thenReturn(1);

            MutexDAO.updateTable(mockConnection);

            verify(mockStatement).setString(1, "player_mutex");
        }

        @Test
        @DisplayName("Given the table is at version 0, When updateTable is called, Then it updates to version 1")
        void updatesFromVersion0To1() throws SQLException {
            // First call: getLatestVersion returns 0 (no version)
            PreparedStatement selectStatement = org.mockito.Mockito.mock(PreparedStatement.class);
            ResultSet selectResultSet = org.mockito.Mockito.mock(ResultSet.class);
            when(selectStatement.executeQuery()).thenReturn(selectResultSet);
            when(selectResultSet.next()).thenReturn(false);

            // Second call: setTableVersion
            PreparedStatement updateStatement = org.mockito.Mockito.mock(PreparedStatement.class);

            when(mockConnection.prepareStatement(anyString()))
                    .thenReturn(selectStatement)
                    .thenReturn(updateStatement);

            MutexDAO.updateTable(mockConnection);

            verify(updateStatement).setString(1, "player_mutex");
            verify(updateStatement).setInt(3, 1);
            verify(updateStatement).executeUpdate();
        }
    }

    @Nested
    @DisplayName("isUserMutexLocked")
    class IsUserMutexLocked {

        @Test
        @DisplayName("Given a locked user exists, When isUserMutexLocked is called, Then it returns true")
        void returnsTrueWhenLocked() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getBoolean("mutex")).thenReturn(true);

            boolean result = MutexDAO.isUserMutexLocked(mockConnection, TEST_UUID);

            assertTrue(result);
            verify(mockStatement).setString(1, TEST_UUID.toString());
        }

        @Test
        @DisplayName("Given an unlocked user exists, When isUserMutexLocked is called, Then it returns false")
        void returnsFalseWhenUnlocked() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getBoolean("mutex")).thenReturn(false);

            boolean result = MutexDAO.isUserMutexLocked(mockConnection, TEST_UUID);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given no user record exists, When isUserMutexLocked is called, Then it returns false")
        void returnsFalseWhenNoRecord() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            boolean result = MutexDAO.isUserMutexLocked(mockConnection, TEST_UUID);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given a SQL exception occurs, When isUserMutexLocked is called, Then it returns false")
        void returnsFalseOnSqlException() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Query failed"));

            boolean result = MutexDAO.isUserMutexLocked(mockConnection, TEST_UUID);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("updateUserMutex with CorePlayer")
    class UpdateUserMutexWithCorePlayer {

        @Test
        @DisplayName("Given a locked CorePlayer, When updateUserMutex is called, Then it returns true")
        void returnsTrueWhenLocked() throws SQLException {
            when(mockCorePlayer.getUUID()).thenReturn(TEST_UUID);
            when(mockCorePlayer.isLocked()).thenReturn(true);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            boolean result = MutexDAO.updateUserMutex(mockConnection, mockCorePlayer);

            assertTrue(result);
            verify(mockStatement).setString(1, TEST_UUID.toString());
            verify(mockStatement).setBoolean(2, true);
            verify(mockStatement).executeUpdate();
        }

        @Test
        @DisplayName("Given an unlocked CorePlayer, When updateUserMutex is called, Then it returns false")
        void returnsFalseWhenUnlocked() throws SQLException {
            when(mockCorePlayer.getUUID()).thenReturn(TEST_UUID);
            when(mockCorePlayer.isLocked()).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            boolean result = MutexDAO.updateUserMutex(mockConnection, mockCorePlayer);

            assertFalse(result);
            verify(mockStatement).setBoolean(2, false);
        }

        @Test
        @DisplayName("Given a SQL exception occurs, When updateUserMutex with CorePlayer is called, Then it returns false")
        void returnsFalseOnSqlException() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Update failed"));

            boolean result = MutexDAO.updateUserMutex(mockConnection, mockCorePlayer);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("updateUserMutex with UUID and boolean")
    class UpdateUserMutexWithUUID {

        @Test
        @DisplayName("Given a UUID and locked=true, When updateUserMutex is called, Then it returns true")
        void returnsTrueWhenLocking() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            boolean result = MutexDAO.updateUserMutex(mockConnection, TEST_UUID, true);

            assertTrue(result);
            verify(mockStatement).setString(1, TEST_UUID.toString());
            verify(mockStatement).setBoolean(2, true);
            verify(mockStatement).executeUpdate();
        }

        @Test
        @DisplayName("Given a UUID and locked=false, When updateUserMutex is called, Then it returns false")
        void returnsFalseWhenUnlocking() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            boolean result = MutexDAO.updateUserMutex(mockConnection, TEST_UUID, false);

            assertFalse(result);
            verify(mockStatement).setBoolean(2, false);
        }

        @Test
        @DisplayName("Given a SQL exception occurs, When updateUserMutex with UUID is called, Then it returns false")
        void returnsFalseOnSqlException() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Update failed"));

            boolean result = MutexDAO.updateUserMutex(mockConnection, TEST_UUID, true);

            assertFalse(result);
        }
    }
}

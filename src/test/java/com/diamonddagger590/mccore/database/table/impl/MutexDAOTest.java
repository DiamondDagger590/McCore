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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
        @DisplayName("Given the table already exists, when attemptCreateTable is called, then it returns false")
        void attemptCreateTable_returnsFalse_whenTableExists() {
            when(mockDatabase.tableExists(mockConnection, "player_mutex")).thenReturn(true);

            boolean result = MutexDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given the table doesn't exist, when attemptCreateTable is called, then it creates the table and returns true")
        void attemptCreateTable_returnsTrue_whenTableCreatedSuccessfully() throws SQLException {
            when(mockDatabase.tableExists(mockConnection, "player_mutex")).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            boolean result = MutexDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertTrue(result);
            verify(mockStatement).executeUpdate();
        }

        @Test
        @DisplayName("Given a SQL exception during creation, when attemptCreateTable is called, then it returns false")
        void attemptCreateTable_returnsFalse_whenSqlExceptionOccurs() throws SQLException {
            when(mockDatabase.tableExists(mockConnection, "player_mutex")).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Create failed"));

            boolean result = MutexDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given executeUpdate throws during table creation, when attemptCreateTable is called, then returns false")
        void attemptCreateTable_returnsFalse_whenExecuteUpdateThrows() throws SQLException {
            when(mockDatabase.tableExists(mockConnection, "player_mutex")).thenReturn(false);
            PreparedStatement failingStatement = mock(PreparedStatement.class);
            when(failingStatement.executeUpdate()).thenThrow(new SQLException("executeUpdate failed"));
            when(mockConnection.prepareStatement(anyString())).thenReturn(failingStatement);

            boolean result = MutexDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("updateTable")
    class UpdateTable {

        @Test
        @DisplayName("Given the table is at current version, when updateTable is called, then no updates are performed")
        void updateTable_performsNoUpdate_whenVersionIsCurrent() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getInt("table_version")).thenReturn(1);

            MutexDAO.updateTable(mockConnection);

            verify(mockStatement).setString(1, "player_mutex");
            verify(mockStatement, never()).executeUpdate();
        }

        @Test
        @DisplayName("Given the table is at version 0, when updateTable is called, then it updates to version 1")
        void updateTable_updatesToVersion1_whenVersionIsZero() throws SQLException {
            PreparedStatement selectStatement = org.mockito.Mockito.mock(PreparedStatement.class);
            ResultSet selectResultSet = org.mockito.Mockito.mock(ResultSet.class);
            when(selectStatement.executeQuery()).thenReturn(selectResultSet);
            when(selectResultSet.next()).thenReturn(false);

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
        @DisplayName("Given a locked user exists, when isUserMutexLocked is called, then it returns true")
        void isUserMutexLocked_returnsTrue_whenUserIsLocked() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getBoolean("mutex")).thenReturn(true);

            boolean result = MutexDAO.isUserMutexLocked(mockConnection, TEST_UUID);

            assertTrue(result);
            verify(mockStatement).setString(1, TEST_UUID.toString());
        }

        @Test
        @DisplayName("Given an unlocked user exists, when isUserMutexLocked is called, then it returns false")
        void isUserMutexLocked_returnsFalse_whenUserIsUnlocked() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getBoolean("mutex")).thenReturn(false);

            boolean result = MutexDAO.isUserMutexLocked(mockConnection, TEST_UUID);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given multiple rows exist, when isUserMutexLocked is called, then it returns the last row's value")
        void isUserMutexLocked_returnsLastRowValue_whenMultipleRowsExist() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getBoolean("mutex")).thenReturn(false, true);

            boolean result = MutexDAO.isUserMutexLocked(mockConnection, TEST_UUID);

            assertTrue(result);
        }

        @Test
        @DisplayName("Given no user record exists, when isUserMutexLocked is called, then it returns false")
        void isUserMutexLocked_returnsFalse_whenNoRecordExists() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            boolean result = MutexDAO.isUserMutexLocked(mockConnection, TEST_UUID);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given a SQL exception occurs, when isUserMutexLocked is called, then it returns false")
        void isUserMutexLocked_returnsFalse_whenSqlExceptionOccurs() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Query failed"));

            boolean result = MutexDAO.isUserMutexLocked(mockConnection, TEST_UUID);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("updateUserMutex with CorePlayer")
    class UpdateUserMutexWithCorePlayer {

        @Test
        @DisplayName("Given a locked CorePlayer, when updateUserMutex is called, then it returns true")
        void updateUserMutex_returnsTrue_whenCorePlayerIsLocked() throws SQLException {
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
        @DisplayName("Given an unlocked CorePlayer, when updateUserMutex is called, then it returns false")
        void updateUserMutex_returnsFalse_whenCorePlayerIsUnlocked() throws SQLException {
            when(mockCorePlayer.getUUID()).thenReturn(TEST_UUID);
            when(mockCorePlayer.isLocked()).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            boolean result = MutexDAO.updateUserMutex(mockConnection, mockCorePlayer);

            assertFalse(result);
            verify(mockStatement).setBoolean(2, false);
        }

        @Test
        @DisplayName("Given a SQL exception occurs, when updateUserMutex with CorePlayer is called, then it returns false")
        void updateUserMutex_returnsFalse_whenSqlExceptionOccurs() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Update failed"));

            boolean result = MutexDAO.updateUserMutex(mockConnection, mockCorePlayer);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("updateUserMutex with UUID and boolean")
    class UpdateUserMutexWithUUID {

        @Test
        @DisplayName("Given a UUID and locked=true, when updateUserMutex is called, then it returns true")
        void updateUserMutex_returnsTrue_whenLockingWithUuid() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            boolean result = MutexDAO.updateUserMutex(mockConnection, TEST_UUID, true);

            assertTrue(result);
            verify(mockStatement).setString(1, TEST_UUID.toString());
            verify(mockStatement).setBoolean(2, true);
            verify(mockStatement).executeUpdate();
        }

        @Test
        @DisplayName("Given a UUID and locked=false, when updateUserMutex is called, then it returns false")
        void updateUserMutex_returnsFalse_whenUnlockingWithUuid() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            boolean result = MutexDAO.updateUserMutex(mockConnection, TEST_UUID, false);

            assertFalse(result);
            verify(mockStatement).setBoolean(2, false);
        }

        @Test
        @DisplayName("Given a SQL exception occurs, when updateUserMutex with UUID is called, then it returns false")
        void updateUserMutex_returnsFalse_whenSqlExceptionOccursWithUuid() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Update failed"));

            boolean result = MutexDAO.updateUserMutex(mockConnection, TEST_UUID, true);

            assertFalse(result);
        }
    }
}

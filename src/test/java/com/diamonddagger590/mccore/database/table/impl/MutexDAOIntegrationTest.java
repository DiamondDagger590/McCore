package com.diamonddagger590.mccore.database.table.impl;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link MutexDAO} using a real in-memory SQLite database.
 * Verifies actual SQL execution and mutex state roundtrip correctness.
 */
class MutexDAOIntegrationTest {

    private Connection connection;
    private Database mockDatabase;

    private static final UUID PLAYER_UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc");
    private static final UUID OTHER_UUID = UUID.fromString("87654321-4321-4321-4321-cba987654321");

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        mockDatabase = mock(Database.class);
        when(mockDatabase.tableExists(any(Connection.class), anyString())).thenReturn(false);

        TableVersionHistoryDAO.attemptCreateTable(connection, mockDatabase);
        MutexDAO.attemptCreateTable(connection, mockDatabase);
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
        void attemptCreateTable_createsTable() throws SQLException {
            Connection freshConn = DriverManager.getConnection("jdbc:sqlite::memory:");
            Database freshDb = mock(Database.class);
            when(freshDb.tableExists(any(Connection.class), anyString())).thenReturn(false);

            boolean result = MutexDAO.attemptCreateTable(freshConn, freshDb);
            assertTrue(result);
            freshConn.close();
        }

        @Test
        @DisplayName("Given table already exists, when attemptCreateTable is called, then returns false")
        void attemptCreateTable_returnsFalse_whenExists() {
            when(mockDatabase.tableExists(any(Connection.class), anyString())).thenReturn(true);
            boolean result = MutexDAO.attemptCreateTable(connection, mockDatabase);
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("isUserMutexLocked and updateUserMutex roundtrip")
    class MutexRoundtrip {

        @Test
        @DisplayName("Given no mutex row exists, when checking lock status, then returns false")
        void isUserMutexLocked_returnsFalse_whenNoRowExists() {
            boolean locked = MutexDAO.isUserMutexLocked(connection, PLAYER_UUID);
            assertFalse(locked);
        }

        @Test
        @DisplayName("Given mutex is set to locked, when checking lock status, then returns true")
        void lockAndCheck_roundTrips() {
            MutexDAO.updateUserMutex(connection, PLAYER_UUID, true);

            boolean locked = MutexDAO.isUserMutexLocked(connection, PLAYER_UUID);
            assertTrue(locked);
        }

        @Test
        @DisplayName("Given mutex is locked then unlocked, when checking lock status, then returns false")
        void unlockAfterLock_roundTrips() {
            MutexDAO.updateUserMutex(connection, PLAYER_UUID, true);
            MutexDAO.updateUserMutex(connection, PLAYER_UUID, false);

            boolean locked = MutexDAO.isUserMutexLocked(connection, PLAYER_UUID);
            assertFalse(locked);
        }

        @Test
        @DisplayName("Given different players have different mutex states, when checking each, then returns correct state")
        void differentPlayersHaveIndependentMutexStates() {
            MutexDAO.updateUserMutex(connection, PLAYER_UUID, true);
            MutexDAO.updateUserMutex(connection, OTHER_UUID, false);

            assertTrue(MutexDAO.isUserMutexLocked(connection, PLAYER_UUID));
            assertFalse(MutexDAO.isUserMutexLocked(connection, OTHER_UUID));
        }

        @Test
        @DisplayName("Given updateUserMutex is called with locked=true, when called, then returns true")
        void updateUserMutex_returnsLockedState_whenTrue() {
            boolean result = MutexDAO.updateUserMutex(connection, PLAYER_UUID, true);
            assertTrue(result);
        }

        @Test
        @DisplayName("Given updateUserMutex is called with locked=false, when called, then returns false")
        void updateUserMutex_returnsLockedState_whenFalse() {
            boolean result = MutexDAO.updateUserMutex(connection, PLAYER_UUID, false);
            assertFalse(result);
        }

        @Test
        @DisplayName("Given a locked CorePlayer, when updateUserMutex with CorePlayer is called, then persists locked state")
        void updateUserMutex_withCorePlayer_persistsLockedState() {
            CorePlayer mockPlayer = mock(CorePlayer.class);
            when(mockPlayer.getUUID()).thenReturn(PLAYER_UUID);
            when(mockPlayer.isLocked()).thenReturn(true);

            boolean result = MutexDAO.updateUserMutex(connection, mockPlayer);
            assertTrue(result);
            assertTrue(MutexDAO.isUserMutexLocked(connection, PLAYER_UUID));
        }

        @Test
        @DisplayName("Given an unlocked CorePlayer, when updateUserMutex with CorePlayer is called, then persists unlocked state")
        void updateUserMutex_withCorePlayer_persistsUnlockedState() {
            CorePlayer mockPlayer = mock(CorePlayer.class);
            when(mockPlayer.getUUID()).thenReturn(PLAYER_UUID);
            when(mockPlayer.isLocked()).thenReturn(false);

            MutexDAO.updateUserMutex(connection, PLAYER_UUID, true);
            boolean result = MutexDAO.updateUserMutex(connection, mockPlayer);
            assertFalse(result);
            assertFalse(MutexDAO.isUserMutexLocked(connection, PLAYER_UUID));
        }
    }

    @Nested
    @DisplayName("updateTable")
    class UpdateTable {

        @Test
        @DisplayName("Given player_mutex has no version, when updateTable is called, then version is set to 1")
        void updateTable_setsVersionToOne_whenNoVersion() {
            MutexDAO.updateTable(connection);

            int version = TableVersionHistoryDAO.getLatestVersion(connection, "player_mutex");
            assertEquals(1, version);
        }

        @Test
        @DisplayName("Given player_mutex is at version 1, when updateTable is called, then version remains 1")
        void updateTable_doesNothing_whenAlreadyCurrent() {
            TableVersionHistoryDAO.setTableVersion(connection, "player_mutex", 1);
            MutexDAO.updateTable(connection);

            int version = TableVersionHistoryDAO.getLatestVersion(connection, "player_mutex");
            assertEquals(1, version);
        }

    }
}

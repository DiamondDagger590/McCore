package com.diamonddagger590.mccore.database.table.impl;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.exception.setting.SettingNotRegisteredException;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.setting.PlayerSettingRegistry;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.util.LinkedNode;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerSettingDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private Database mockDatabase;

    private static final UUID TEST_UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc");

    private enum TestSetting implements PlayerSetting {
        ON,
        OFF;

        private static final NamespacedKey KEY = new NamespacedKey("mccore", "test_setting");

        @Override
        @NotNull
        public NamespacedKey getSettingKey() {
            return KEY;
        }

        @Override
        @NotNull
        public LinkedNode<TestSetting> getFirstSetting() {
            return new LinkedNode<>(ON, new LinkedNode<>(OFF, null));
        }

        @Override
        @NotNull
        public LinkedNode<TestSetting> getNextSetting() {
            return this == ON ? new LinkedNode<>(OFF, null) : new LinkedNode<>(ON, null);
        }

        @Override
        public void onSettingChange(@NotNull CorePlayer player, @NotNull Optional<PlayerSetting> oldSetting) {
        }

        @Override
        @NotNull
        public Optional<TestSetting> fromString(@NotNull String setting) {
            try {
                return Optional.of(TestSetting.valueOf(setting));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
    }

    @Nested
    @DisplayName("attemptCreateTable")
    class AttemptCreateTable {

        @Test
        @DisplayName("Given the table already exists, when attemptCreateTable is called, then returns false")
        void attemptCreateTable_returnsFalse_whenTableExists() {
            when(mockDatabase.tableExists(mockConnection, "player_settings")).thenReturn(true);

            boolean result = PlayerSettingDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given the table doesn't exist, when attemptCreateTable is called, then creates table and index and returns true")
        void attemptCreateTable_returnsTrue_whenTableCreatedSuccessfully() throws SQLException {
            when(mockDatabase.tableExists(mockConnection, "player_settings")).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            boolean result = PlayerSettingDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertTrue(result);
        }

        @Test
        @DisplayName("Given a SQL exception during table creation, when attemptCreateTable is called, then returns false")
        void attemptCreateTable_returnsFalse_whenSqlExceptionOnCreate() throws SQLException {
            when(mockDatabase.tableExists(mockConnection, "player_settings")).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Create failed"));

            boolean result = PlayerSettingDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given a SQL exception during index creation, when attemptCreateTable is called, then returns false")
        void attemptCreateTable_returnsFalse_whenSqlExceptionOnIndex() throws SQLException {
            when(mockDatabase.tableExists(mockConnection, "player_settings")).thenReturn(false);
            PreparedStatement createTableStmt = mock(PreparedStatement.class);
            PreparedStatement indexStmt = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString()))
                    .thenReturn(createTableStmt)
                    .thenThrow(new SQLException("Index failed"));

            boolean result = PlayerSettingDAO.attemptCreateTable(mockConnection, mockDatabase);

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

            PlayerSettingDAO.updateTable(mockConnection);
        }

        @Test
        @DisplayName("Given the table is at version 0, when updateTable is called, then it updates to version 1")
        void updateTable_updatesToVersion1_whenVersionIsZero() throws SQLException {
            PreparedStatement selectStatement = mock(PreparedStatement.class);
            ResultSet selectResultSet = mock(ResultSet.class);
            when(selectStatement.executeQuery()).thenReturn(selectResultSet);
            when(selectResultSet.next()).thenReturn(false);

            PreparedStatement updateStatement = mock(PreparedStatement.class);

            when(mockConnection.prepareStatement(anyString()))
                    .thenReturn(selectStatement)
                    .thenReturn(updateStatement);

            PlayerSettingDAO.updateTable(mockConnection);

            verify(updateStatement).setString(1, "player_settings");
            verify(updateStatement).setTime(eq(2), any(java.sql.Time.class));
            verify(updateStatement).setInt(3, 1);
            verify(updateStatement).executeUpdate();
        }

        @Test
        @DisplayName("Given the table is at a negative version, when updateTable is called, then it does not attempt version 0 migration")
        void updateTable_skipsVersion0Migration_whenVersionIsNegative() throws SQLException {
            try (MockedStatic<TableVersionHistoryDAO> tvhStatic = mockStatic(TableVersionHistoryDAO.class)) {
                tvhStatic.when(() -> TableVersionHistoryDAO.getLatestVersion(any(), anyString())).thenReturn(-1);

                PlayerSettingDAO.updateTable(mockConnection);

                tvhStatic.verify(() -> TableVersionHistoryDAO.setTableVersion(any(), anyString(), eq(1)),
                        org.mockito.Mockito.never());
            }
        }
    }

    @Nested
    @DisplayName("getPlayerSettings")
    class GetPlayerSettings {

        private MockedStatic<CorePlugin> corePluginStatic;

        @BeforeEach
        void setUp() {
            RegistryResetExtension.setupRegistry();

            PlayerSettingRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.PLAYER_SETTING);
            registry.register(TestSetting.ON);

            CorePlugin mockPlugin = mock(CorePlugin.class);
            when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());
            when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("TestLogger"));

            corePluginStatic = mockStatic(CorePlugin.class);
            corePluginStatic.when(CorePlugin::getInstance).thenReturn(mockPlugin);
        }

        @AfterEach
        void tearDown() {
            corePluginStatic.close();
            RegistryResetExtension.resetRegistry();
        }

        @Test
        @DisplayName("Given player has saved settings, when getPlayerSettings is called, then returns settings from database")
        void getPlayerSettings_returnsFromDatabase_whenSettingSaved() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getString("setting_value")).thenReturn("OFF");

            Set<PlayerSetting> settings = PlayerSettingDAO.getPlayerSettings(mockConnection, TEST_UUID);

            assertFalse(settings.isEmpty());
            assertEquals(1, settings.size());
            assertTrue(settings.stream().anyMatch(s -> s.name().equals("OFF")));
        }

        @Test
        @DisplayName("Given player has no saved settings, when getPlayerSettings is called, then returns defaults")
        void getPlayerSettings_returnsDefaults_whenNoSettingSaved() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            Set<PlayerSetting> settings = PlayerSettingDAO.getPlayerSettings(mockConnection, TEST_UUID);

            assertFalse(settings.isEmpty());
            assertEquals(1, settings.size());
            assertTrue(settings.stream().anyMatch(s -> s.name().equals("ON")));
        }

        @Test
        @DisplayName("Given player has invalid saved setting, when getPlayerSettings is called, then returns default")
        void getPlayerSettings_returnsDefault_whenInvalidSettingValue() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getString("setting_value")).thenReturn("INVALID_VALUE");

            Set<PlayerSetting> settings = PlayerSettingDAO.getPlayerSettings(mockConnection, TEST_UUID);

            assertFalse(settings.isEmpty());
            assertEquals(1, settings.size());
            assertTrue(settings.stream().anyMatch(s -> s.name().equals("ON")));
        }

        @Test
        @DisplayName("Given a SQL exception occurs, when getPlayerSettings is called, then returns partial results")
        void getPlayerSettings_returnsPartialResults_whenSqlExceptionOccurs() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Query failed"));

            Set<PlayerSetting> settings = PlayerSettingDAO.getPlayerSettings(mockConnection, TEST_UUID);

            assertNotNull(settings);
            assertTrue(settings.isEmpty());
        }

    }

    @Nested
    @DisplayName("getPlayerSetting")
    class GetPlayerSetting {

        private MockedStatic<CorePlugin> corePluginStatic;

        @BeforeEach
        void setUp() {
            RegistryResetExtension.setupRegistry();

            PlayerSettingRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.PLAYER_SETTING);
            registry.register(TestSetting.ON);

            CorePlugin mockPlugin = mock(CorePlugin.class);
            when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());

            corePluginStatic = mockStatic(CorePlugin.class);
            corePluginStatic.when(CorePlugin::getInstance).thenReturn(mockPlugin);
        }

        @AfterEach
        void tearDown() {
            corePluginStatic.close();
            RegistryResetExtension.resetRegistry();
        }

        @Test
        @DisplayName("Given unregistered setting key, when getPlayerSetting is called, then throws SettingNotRegisteredException")
        void getPlayerSetting_throwsException_whenSettingNotRegistered() {
            NamespacedKey unknownKey = new NamespacedKey("mccore", "unknown_setting");

            assertThrows(SettingNotRegisteredException.class,
                    () -> PlayerSettingDAO.getPlayerSetting(mockConnection, TEST_UUID, unknownKey));
        }

        @Test
        @DisplayName("Given a saved setting value, when getPlayerSetting is called, then returns saved value")
        void getPlayerSetting_returnsSavedValue_whenPresent() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getString("setting_value")).thenReturn("OFF");

            PlayerSetting result = PlayerSettingDAO.getPlayerSetting(mockConnection, TEST_UUID, TestSetting.ON.getSettingKey());

            assertEquals("OFF", result.name());
        }

        @Test
        @DisplayName("Given no saved setting, when getPlayerSetting is called, then returns default")
        void getPlayerSetting_returnsDefault_whenNoSavedValue() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            PlayerSetting result = PlayerSettingDAO.getPlayerSetting(mockConnection, TEST_UUID, TestSetting.ON.getSettingKey());

            assertEquals("ON", result.name());
        }

        @Test
        @DisplayName("Given an invalid saved value, when getPlayerSetting is called, then returns default")
        void getPlayerSetting_returnsDefault_whenInvalidSavedValue() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getString("setting_value")).thenReturn("INVALID");

            PlayerSetting result = PlayerSettingDAO.getPlayerSetting(mockConnection, TEST_UUID, TestSetting.ON.getSettingKey());

            assertEquals("ON", result.name());
        }

        @Test
        @DisplayName("Given a SQL exception, when getPlayerSetting is called, then returns default")
        void getPlayerSetting_returnsDefault_whenSqlExceptionOccurs() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Query failed"));

            PlayerSetting result = PlayerSettingDAO.getPlayerSetting(mockConnection, TEST_UUID, TestSetting.ON.getSettingKey());

            assertEquals("ON", result.name());
        }
    }

    @Nested
    @DisplayName("savePlayerSetting")
    class SavePlayerSetting {

        @Test
        @DisplayName("Given a valid player setting, when savePlayerSetting is called, then returns a prepared statement")
        void savePlayerSetting_returnsPreparedStatement_whenValid() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            PreparedStatement result = PlayerSettingDAO.savePlayerSetting(mockConnection, TEST_UUID, TestSetting.ON);

            assertNotNull(result);
            verify(mockStatement).setString(1, TEST_UUID.toString());
            verify(mockStatement).setString(2, TestSetting.ON.getSettingKey().toString());
            verify(mockStatement).setString(3, "ON");
        }

        @Test
        @DisplayName("Given a SQL exception, when savePlayerSetting is called, then throws RuntimeException")
        void savePlayerSetting_throwsRuntimeException_whenSqlExceptionOccurs() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Save failed"));

            assertThrows(RuntimeException.class,
                    () -> PlayerSettingDAO.savePlayerSetting(mockConnection, TEST_UUID, TestSetting.ON));
        }
    }

    @Nested
    @DisplayName("savePlayerSettings")
    class SavePlayerSettings {

        @Test
        @DisplayName("Given a set of settings, when savePlayerSettings is called, then returns list of prepared statements")
        void savePlayerSettings_returnsList_whenMultipleSettings() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            Set<PlayerSetting> settings = new HashSet<>();
            settings.add(TestSetting.ON);
            settings.add(TestSetting.OFF);

            List<PreparedStatement> results = PlayerSettingDAO.savePlayerSettings(mockConnection, TEST_UUID, settings);

            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("Given an empty set, when savePlayerSettings is called, then returns empty list")
        void savePlayerSettings_returnsEmptyList_whenNoSettings() {
            Set<PlayerSetting> settings = new HashSet<>();

            List<PreparedStatement> results = PlayerSettingDAO.savePlayerSettings(mockConnection, TEST_UUID, settings);

            assertTrue(results.isEmpty());
        }
    }
}

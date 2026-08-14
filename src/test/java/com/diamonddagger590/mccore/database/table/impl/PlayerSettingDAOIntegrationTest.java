package com.diamonddagger590.mccore.database.table.impl;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.Database;
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
import org.mockito.MockedStatic;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link PlayerSettingDAO} using a real in-memory SQLite database.
 * Verifies actual SQL execution and setting save/load roundtrip correctness.
 */
class PlayerSettingDAOIntegrationTest {

    private Connection connection;
    private Database mockDatabase;
    private MockedStatic<CorePlugin> corePluginStatic;

    private static final UUID PLAYER_UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc");
    private static final UUID OTHER_UUID = UUID.fromString("87654321-4321-4321-4321-cba987654321");

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

    private enum AnotherSetting implements PlayerSetting {
        ENABLED,
        DISABLED;

        private static final NamespacedKey KEY = new NamespacedKey("mccore", "another_setting");

        @Override
        @NotNull
        public NamespacedKey getSettingKey() {
            return KEY;
        }

        @Override
        @NotNull
        public LinkedNode<AnotherSetting> getFirstSetting() {
            return new LinkedNode<>(ENABLED, new LinkedNode<>(DISABLED, null));
        }

        @Override
        @NotNull
        public LinkedNode<AnotherSetting> getNextSetting() {
            return this == ENABLED ? new LinkedNode<>(DISABLED, null) : new LinkedNode<>(ENABLED, null);
        }

        @Override
        public void onSettingChange(@NotNull CorePlayer player, @NotNull Optional<PlayerSetting> oldSetting) {
        }

        @Override
        @NotNull
        public Optional<AnotherSetting> fromString(@NotNull String setting) {
            try {
                return Optional.of(AnotherSetting.valueOf(setting));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        mockDatabase = mock(Database.class);
        when(mockDatabase.tableExists(any(Connection.class), anyString())).thenReturn(false);

        TableVersionHistoryDAO.attemptCreateTable(connection, mockDatabase);
        PlayerSettingDAO.attemptCreateTable(connection, mockDatabase);

        RegistryResetExtension.setupRegistry();
        PlayerSettingRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.PLAYER_SETTING);
        registry.register(TestSetting.ON);
        registry.register(AnotherSetting.ENABLED);

        CorePlugin mockPlugin = mock(CorePlugin.class);
        when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("TestLogger"));

        corePluginStatic = mockStatic(CorePlugin.class);
        corePluginStatic.when(CorePlugin::getInstance).thenReturn(mockPlugin);
    }

    @AfterEach
    void tearDown() throws SQLException {
        corePluginStatic.close();
        RegistryResetExtension.resetRegistry();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Nested
    @DisplayName("save and load single setting roundtrip")
    class SingleSettingRoundtrip {

        @Test
        @DisplayName("Given a setting is saved, when getPlayerSetting is called, then returns the saved value")
        void saveAndLoad_roundTrips() throws SQLException {
            PreparedStatement saveStmt = PlayerSettingDAO.savePlayerSetting(connection, PLAYER_UUID, TestSetting.OFF);
            saveStmt.executeUpdate();
            saveStmt.close();

            PlayerSetting loaded = PlayerSettingDAO.getPlayerSetting(connection, PLAYER_UUID, TestSetting.KEY);
            assertEquals("OFF", loaded.name());
        }

        @Test
        @DisplayName("Given no setting is saved, when getPlayerSetting is called, then returns the default")
        void load_returnsDefault_whenNothingSaved() {
            PlayerSetting loaded = PlayerSettingDAO.getPlayerSetting(connection, PLAYER_UUID, TestSetting.KEY);
            assertEquals("ON", loaded.name());
        }

        @Test
        @DisplayName("Given a setting is saved then updated, when getPlayerSetting is called, then returns the updated value")
        void saveUpdate_returnsLatestValue() throws SQLException {
            PreparedStatement stmt1 = PlayerSettingDAO.savePlayerSetting(connection, PLAYER_UUID, TestSetting.OFF);
            stmt1.executeUpdate();
            stmt1.close();

            PreparedStatement stmt2 = PlayerSettingDAO.savePlayerSetting(connection, PLAYER_UUID, TestSetting.ON);
            stmt2.executeUpdate();
            stmt2.close();

            PlayerSetting loaded = PlayerSettingDAO.getPlayerSetting(connection, PLAYER_UUID, TestSetting.KEY);
            assertEquals("ON", loaded.name());
        }
    }

    @Nested
    @DisplayName("save and load multiple settings roundtrip")
    class MultipleSettingsRoundtrip {

        @Test
        @DisplayName("Given multiple settings are saved, when getPlayerSettings is called, then returns all saved values")
        void saveMultipleAndLoadAll_roundTrips() throws SQLException {
            Set<PlayerSetting> toSave = new HashSet<>();
            toSave.add(TestSetting.OFF);
            toSave.add(AnotherSetting.DISABLED);

            List<PreparedStatement> stmts = PlayerSettingDAO.savePlayerSettings(connection, PLAYER_UUID, toSave);
            for (PreparedStatement stmt : stmts) {
                stmt.executeUpdate();
                stmt.close();
            }

            Set<PlayerSetting> loaded = PlayerSettingDAO.getPlayerSettings(connection, PLAYER_UUID);

            assertEquals(2, loaded.size());
            assertTrue(loaded.stream().anyMatch(s -> s.name().equals("OFF")));
            assertTrue(loaded.stream().anyMatch(s -> s.name().equals("DISABLED")));
        }

        @Test
        @DisplayName("Given no settings are saved, when getPlayerSettings is called, then returns defaults for all registered settings")
        void loadAll_returnsDefaults_whenNothingSaved() {
            Set<PlayerSetting> loaded = PlayerSettingDAO.getPlayerSettings(connection, PLAYER_UUID);

            assertEquals(2, loaded.size());
            assertTrue(loaded.stream().anyMatch(s -> s.name().equals("ON")));
            assertTrue(loaded.stream().anyMatch(s -> s.name().equals("ENABLED")));
        }
    }

    @Nested
    @DisplayName("player isolation")
    class PlayerIsolation {

        @Test
        @DisplayName("Given different players have different settings, when loading each, then returns correct values")
        void differentPlayersHaveIndependentSettings() throws SQLException {
            PreparedStatement stmt1 = PlayerSettingDAO.savePlayerSetting(connection, PLAYER_UUID, TestSetting.OFF);
            stmt1.executeUpdate();
            stmt1.close();

            PreparedStatement stmt2 = PlayerSettingDAO.savePlayerSetting(connection, OTHER_UUID, TestSetting.ON);
            stmt2.executeUpdate();
            stmt2.close();

            PlayerSetting player1Setting = PlayerSettingDAO.getPlayerSetting(connection, PLAYER_UUID, TestSetting.KEY);
            PlayerSetting player2Setting = PlayerSettingDAO.getPlayerSetting(connection, OTHER_UUID, TestSetting.KEY);

            assertEquals("OFF", player1Setting.name());
            assertEquals("ON", player2Setting.name());
        }
    }

    @Nested
    @DisplayName("updateTable")
    class UpdateTable {

        @Test
        @DisplayName("Given player_settings has no version, when updateTable is called, then sets version to 1")
        void updateTable_setsVersionToOne() {
            PlayerSettingDAO.updateTable(connection);

            int version = TableVersionHistoryDAO.getLatestVersion(connection, "player_settings");
            assertEquals(1, version);
        }
    }
}

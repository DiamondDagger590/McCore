package com.diamonddagger590.mccore.database.table.impl;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.exception.setting.SettingNotRegisteredException;
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
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerSettingDAOTest {

    private CorePlugin mockPlugin;

    private void stubSettingForRegistry(PlayerSetting setting) {
        LinkedNode<PlayerSetting> node = new LinkedNode<>(setting);
        when(setting.getFirstSetting()).thenAnswer(inv -> node);
    }

    @BeforeEach
    void setUp() throws Exception {
        RegistryResetExtension.setupRegistry();
        mockPlugin = mock(CorePlugin.class);
        when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("TestLogger"));
        // Inject mock plugin into CorePlugin.instance via reflection
        Field instanceField = CorePlugin.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, mockPlugin);
    }

    @AfterEach
    void tearDown() throws Exception {
        Field instanceField = CorePlugin.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
        RegistryResetExtension.resetRegistry();
    }

    @Test
    @DisplayName("Given table does not exist, when attemptCreateTable, then table is created and returns true")
    void attemptCreateTable_returnsTrue_whenTableDoesNotExist() throws SQLException {
        Connection connection = mock(Connection.class);
        Database database = mock(Database.class);
        PreparedStatement createStmt = mock(PreparedStatement.class);
        PreparedStatement indexStmt = mock(PreparedStatement.class);

        when(database.tableExists(connection, "player_settings")).thenReturn(false);
        when(connection.prepareStatement(anyString())).thenReturn(createStmt).thenReturn(indexStmt);

        boolean result = PlayerSettingDAO.attemptCreateTable(connection, database);

        assertTrue(result);
        verify(createStmt).executeUpdate();
        verify(indexStmt).executeUpdate();
    }

    @Test
    @DisplayName("Given table already exists, when attemptCreateTable, then returns false")
    void attemptCreateTable_returnsFalse_whenTableExists() {
        Connection connection = mock(Connection.class);
        Database database = mock(Database.class);

        when(database.tableExists(connection, "player_settings")).thenReturn(true);

        boolean result = PlayerSettingDAO.attemptCreateTable(connection, database);

        assertFalse(result);
    }

    @Test
    @DisplayName("Given SQL exception on create, when attemptCreateTable, then returns false")
    void attemptCreateTable_returnsFalse_whenSQLExceptionOnCreate() throws SQLException {
        Connection connection = mock(Connection.class);
        Database database = mock(Database.class);

        when(database.tableExists(connection, "player_settings")).thenReturn(false);
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("create error"));

        boolean result = PlayerSettingDAO.attemptCreateTable(connection, database);

        assertFalse(result);
    }

    @Test
    @DisplayName("Given SQL exception on index, when attemptCreateTable, then returns false")
    void attemptCreateTable_returnsFalse_whenSQLExceptionOnIndex() throws SQLException {
        Connection connection = mock(Connection.class);
        Database database = mock(Database.class);
        PreparedStatement createStmt = mock(PreparedStatement.class);

        when(database.tableExists(connection, "player_settings")).thenReturn(false);
        when(connection.prepareStatement(anyString()))
                .thenReturn(createStmt)
                .thenThrow(new SQLException("index error"));

        boolean result = PlayerSettingDAO.attemptCreateTable(connection, database);

        assertFalse(result);
    }

    @Test
    @DisplayName("Given version is current, when updateTable, then no update occurs")
    void updateTable_doesNothing_whenVersionIsCurrent() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement versionStmt = mock(PreparedStatement.class);
        ResultSet versionRs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(versionStmt);
        when(versionStmt.executeQuery()).thenReturn(versionRs);
        when(versionRs.next()).thenReturn(true);
        when(versionRs.getInt("version")).thenReturn(1);

        PlayerSettingDAO.updateTable(connection);
    }

    @Test
    @DisplayName("Given version is 0, when updateTable, then version is set to 1")
    void updateTable_setsVersionToOne_whenVersionIsZero() throws SQLException {
        Connection connection = mock(Connection.class);

        PreparedStatement getVersionStmt = mock(PreparedStatement.class);
        ResultSet getVersionRs = mock(ResultSet.class);
        when(getVersionRs.next()).thenReturn(false);
        when(getVersionStmt.executeQuery()).thenReturn(getVersionRs);

        PreparedStatement setVersionStmt = mock(PreparedStatement.class);

        when(connection.prepareStatement(anyString()))
                .thenReturn(getVersionStmt)
                .thenReturn(setVersionStmt);

        PlayerSettingDAO.updateTable(connection);
    }

    @Test
    @DisplayName("Given no settings registered, when getPlayerSettings, then returns empty set")
    void getPlayerSettings_returnsEmptySet_whenNoSettingsRegistered() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);

        Set<PlayerSetting> result = PlayerSettingDAO.getPlayerSettings(connection, UUID.randomUUID());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Given a registered setting with stored value, when getPlayerSettings, then returns parsed setting")
    void getPlayerSettings_returnsParsedSetting_whenStoredValueExists() throws SQLException {
        NamespacedKey settingKey = new NamespacedKey("test", "my_setting");
        PlayerSetting defaultSetting = mock(PlayerSetting.class);
        PlayerSetting parsedSetting = mock(PlayerSetting.class);

        when(defaultSetting.getSettingKey()).thenReturn(settingKey);
        when(defaultSetting.fromString("ON")).thenAnswer(inv -> Optional.of(parsedSetting));

        stubSettingForRegistry(defaultSetting);
        PlayerSettingRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.PLAYER_SETTING);
        registry.register(defaultSetting);

        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getString("setting_value")).thenReturn("ON");

        Set<PlayerSetting> result = PlayerSettingDAO.getPlayerSettings(connection, UUID.randomUUID());

        assertEquals(1, result.size());
        assertTrue(result.contains(parsedSetting));
    }

    @Test
    @DisplayName("Given a registered setting with no stored value, when getPlayerSettings, then returns default setting")
    void getPlayerSettings_returnsDefault_whenNoStoredValue() throws SQLException {
        NamespacedKey settingKey = new NamespacedKey("test", "my_setting");
        PlayerSetting defaultSetting = mock(PlayerSetting.class);

        when(defaultSetting.getSettingKey()).thenReturn(settingKey);

        stubSettingForRegistry(defaultSetting);
        PlayerSettingRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.PLAYER_SETTING);
        registry.register(defaultSetting);

        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        Set<PlayerSetting> result = PlayerSettingDAO.getPlayerSettings(connection, UUID.randomUUID());

        assertEquals(1, result.size());
        assertTrue(result.contains(defaultSetting));
    }

    @Test
    @DisplayName("Given a registered setting with unparseable stored value, when getPlayerSettings, then returns default")
    void getPlayerSettings_returnsDefault_whenStoredValueUnparseable() throws SQLException {
        NamespacedKey settingKey = new NamespacedKey("test", "my_setting");
        PlayerSetting defaultSetting = mock(PlayerSetting.class);

        when(defaultSetting.getSettingKey()).thenReturn(settingKey);
        when(defaultSetting.fromString("INVALID")).thenReturn(Optional.empty());

        stubSettingForRegistry(defaultSetting);
        PlayerSettingRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.PLAYER_SETTING);
        registry.register(defaultSetting);

        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getString("setting_value")).thenReturn("INVALID");

        Set<PlayerSetting> result = PlayerSettingDAO.getPlayerSettings(connection, UUID.randomUUID());

        assertEquals(1, result.size());
        assertTrue(result.contains(defaultSetting));
    }

    @Test
    @DisplayName("Given SQL exception, when getPlayerSettings, then returns partial results")
    void getPlayerSettings_returnsPartialResults_whenSQLException() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("query error"));

        Set<PlayerSetting> result = PlayerSettingDAO.getPlayerSettings(connection, UUID.randomUUID());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Given unregistered setting key, when getPlayerSetting, then throws SettingNotRegisteredException")
    void getPlayerSetting_throwsSettingNotRegistered_whenKeyNotRegistered() {
        Connection connection = mock(Connection.class);
        NamespacedKey settingKey = new NamespacedKey("test", "missing_setting");

        assertThrows(SettingNotRegisteredException.class,
                () -> PlayerSettingDAO.getPlayerSetting(connection, UUID.randomUUID(), settingKey));
    }

    @Test
    @DisplayName("Given registered setting with stored value, when getPlayerSetting, then returns parsed setting")
    void getPlayerSetting_returnsParsedSetting_whenStoredValueExists() throws SQLException {
        NamespacedKey settingKey = new NamespacedKey("test", "my_setting");
        PlayerSetting defaultSetting = mock(PlayerSetting.class);
        PlayerSetting parsedSetting = mock(PlayerSetting.class);

        when(defaultSetting.getSettingKey()).thenReturn(settingKey);
        when(defaultSetting.fromString("ENABLED")).thenAnswer(inv -> Optional.of(parsedSetting));

        stubSettingForRegistry(defaultSetting);
        PlayerSettingRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.PLAYER_SETTING);
        registry.register(defaultSetting);

        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(rs.next()).thenReturn(true);
        when(rs.getString("setting_value")).thenReturn("ENABLED");
        when(stmt.executeQuery()).thenReturn(rs);

        PlayerSetting result = PlayerSettingDAO.getPlayerSetting(connection, UUID.randomUUID(), settingKey);

        assertEquals(parsedSetting, result);
    }

    @Test
    @DisplayName("Given registered setting with no stored value, when getPlayerSetting, then returns default")
    void getPlayerSetting_returnsDefault_whenNoStoredValue() throws SQLException {
        NamespacedKey settingKey = new NamespacedKey("test", "my_setting");
        PlayerSetting defaultSetting = mock(PlayerSetting.class);

        when(defaultSetting.getSettingKey()).thenReturn(settingKey);

        stubSettingForRegistry(defaultSetting);
        PlayerSettingRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.PLAYER_SETTING);
        registry.register(defaultSetting);

        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        PlayerSetting result = PlayerSettingDAO.getPlayerSetting(connection, UUID.randomUUID(), settingKey);

        assertEquals(defaultSetting, result);
    }

    @Test
    @DisplayName("Given SQL exception, when getPlayerSetting, then returns default setting")
    void getPlayerSetting_returnsDefault_whenSQLException() throws SQLException {
        NamespacedKey settingKey = new NamespacedKey("test", "my_setting");
        PlayerSetting defaultSetting = mock(PlayerSetting.class);

        when(defaultSetting.getSettingKey()).thenReturn(settingKey);

        stubSettingForRegistry(defaultSetting);
        PlayerSettingRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.PLAYER_SETTING);
        registry.register(defaultSetting);

        Connection connection = mock(Connection.class);
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("query error"));

        PlayerSetting result = PlayerSettingDAO.getPlayerSetting(connection, UUID.randomUUID(), settingKey);

        assertEquals(defaultSetting, result);
    }

    @Test
    @DisplayName("Given a player setting, when savePlayerSetting, then returns prepared statement with correct bindings")
    void savePlayerSetting_returnsPreparedStatement_whenCalled() throws SQLException {
        NamespacedKey settingKey = new NamespacedKey("test", "my_setting");
        PlayerSetting setting = mock(PlayerSetting.class);
        when(setting.getSettingKey()).thenReturn(settingKey);
        when(setting.name()).thenReturn("ENABLED");

        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);

        UUID playerUUID = UUID.randomUUID();
        PreparedStatement result = PlayerSettingDAO.savePlayerSetting(connection, playerUUID, setting);

        assertNotNull(result);
        verify(stmt).setString(1, playerUUID.toString());
        verify(stmt).setString(2, settingKey.toString());
        verify(stmt).setString(3, "ENABLED");
    }

    @Test
    @DisplayName("Given SQL exception, when savePlayerSetting, then throws RuntimeException")
    void savePlayerSetting_throwsRuntimeException_whenSQLException() throws SQLException {
        PlayerSetting setting = mock(PlayerSetting.class);
        Connection connection = mock(Connection.class);
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("save error"));

        assertThrows(RuntimeException.class,
                () -> PlayerSettingDAO.savePlayerSetting(connection, UUID.randomUUID(), setting));
    }

    @Test
    @DisplayName("Given multiple settings, when savePlayerSettings, then returns list of prepared statements")
    void savePlayerSettings_returnsListOfStatements_whenMultipleSettings() throws SQLException {
        NamespacedKey key1 = new NamespacedKey("test", "setting1");
        NamespacedKey key2 = new NamespacedKey("test", "setting2");

        PlayerSetting setting1 = mock(PlayerSetting.class);
        when(setting1.getSettingKey()).thenReturn(key1);
        when(setting1.name()).thenReturn("ON");

        PlayerSetting setting2 = mock(PlayerSetting.class);
        when(setting2.getSettingKey()).thenReturn(key2);
        when(setting2.name()).thenReturn("OFF");

        Connection connection = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(stmt);

        UUID playerUUID = UUID.randomUUID();
        List<PreparedStatement> result = PlayerSettingDAO.savePlayerSettings(connection, playerUUID, Set.of(setting1, setting2));

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Given empty settings set, when savePlayerSettings, then returns empty list")
    void savePlayerSettings_returnsEmptyList_whenNoSettings() {
        Connection connection = mock(Connection.class);

        List<PreparedStatement> result = PlayerSettingDAO.savePlayerSettings(connection, UUID.randomUUID(), Set.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

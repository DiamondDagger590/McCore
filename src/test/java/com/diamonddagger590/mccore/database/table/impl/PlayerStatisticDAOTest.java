package com.diamonddagger590.mccore.database.table.impl;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.statistic.StatisticEntry;
import com.diamonddagger590.mccore.statistic.StatisticType;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

class PlayerStatisticDAOTest {

    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private Database database;
    private CorePlugin plugin;
    private Logger logger;
    private MockedStatic<CorePlugin> corePluginStatic;

    @BeforeEach
    void setUp() throws SQLException {
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        database = mock(Database.class);
        plugin = mock(CorePlugin.class);
        logger = mock(Logger.class);

        corePluginStatic = mockStatic(CorePlugin.class);
        corePluginStatic.when(CorePlugin::getInstance).thenReturn(plugin);
        when(plugin.getLogger()).thenReturn(logger);
    }

    @AfterEach
    void tearDown() {
        corePluginStatic.close();
    }

    @Nested
    @DisplayName("attemptCreateTable")
    class AttemptCreateTable {

        @Test
        @DisplayName("Given table already exists, when attemptCreateTable, then returns false")
        void returnsFlase_whenTableExists() {
            when(database.tableExists(eq(connection), anyString())).thenReturn(true);

            boolean result = PlayerStatisticDAO.attemptCreateTable(connection, database);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given table does not exist, when attemptCreateTable, then creates table and index and returns true")
        void createsTableAndIndex_whenTableDoesNotExist() throws SQLException {
            when(database.tableExists(eq(connection), anyString())).thenReturn(false);
            PreparedStatement createStmt = mock(PreparedStatement.class);
            PreparedStatement indexStmt = mock(PreparedStatement.class);
            when(connection.prepareStatement(anyString()))
                    .thenReturn(createStmt)
                    .thenReturn(indexStmt);

            boolean result = PlayerStatisticDAO.attemptCreateTable(connection, database);

            assertTrue(result);
            verify(createStmt).executeUpdate();
            verify(indexStmt).executeUpdate();
            verify(createStmt).close();
            verify(indexStmt).close();
        }

        @Test
        @DisplayName("Given CREATE TABLE fails, when attemptCreateTable, then returns false")
        void returnsFalse_whenCreateTableFails() throws SQLException {
            when(database.tableExists(eq(connection), anyString())).thenReturn(false);
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeUpdate()).thenThrow(new SQLException("create failed"));

            boolean result = PlayerStatisticDAO.attemptCreateTable(connection, database);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given CREATE INDEX fails, when attemptCreateTable, then returns false")
        void returnsFalse_whenCreateIndexFails() throws SQLException {
            when(database.tableExists(eq(connection), anyString())).thenReturn(false);
            PreparedStatement createStmt = mock(PreparedStatement.class);
            PreparedStatement indexStmt = mock(PreparedStatement.class);
            when(connection.prepareStatement(anyString()))
                    .thenReturn(createStmt)
                    .thenReturn(indexStmt);
            when(indexStmt.executeUpdate()).thenThrow(new SQLException("index failed"));

            boolean result = PlayerStatisticDAO.attemptCreateTable(connection, database);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("updateTable")
    class UpdateTable {

        @Test
        @DisplayName("Given current version stored, when updateTable, then does nothing")
        void doesNothing_whenVersionIsCurrent() {
            try (MockedStatic<TableVersionHistoryDAO> tvhStatic = mockStatic(TableVersionHistoryDAO.class)) {
                tvhStatic.when(() -> TableVersionHistoryDAO.getLatestVersion(any(), anyString())).thenReturn(1);

                PlayerStatisticDAO.updateTable(connection);

                tvhStatic.verify(() -> TableVersionHistoryDAO.setTableVersion(any(), anyString(), eq(1)),
                        org.mockito.Mockito.never());
            }
        }

        @Test
        @DisplayName("Given version 0 stored, when updateTable, then updates to version 1")
        void updatesToVersion1_whenVersion0Stored() {
            try (MockedStatic<TableVersionHistoryDAO> tvhStatic = mockStatic(TableVersionHistoryDAO.class)) {
                tvhStatic.when(() -> TableVersionHistoryDAO.getLatestVersion(any(), anyString())).thenReturn(0);

                PlayerStatisticDAO.updateTable(connection);

                tvhStatic.verify(() -> TableVersionHistoryDAO.setTableVersion(eq(connection), anyString(), eq(1)));
            }
        }
    }

    @Nested
    @DisplayName("getAllPlayerStatistics")
    class GetAllPlayerStatistics {

        private final UUID playerUUID = UUID.randomUUID();

        @Test
        @DisplayName("Given player has INT statistic, when getAllPlayerStatistics, then returns it correctly")
        void returnsIntStatistic() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString("statistic_key")).thenReturn("test:my_stat");
            when(resultSet.getString("stat_type")).thenReturn("INT");
            when(resultSet.getInt("int_value")).thenReturn(42);

            Map<NamespacedKey, StatisticEntry> result = PlayerStatisticDAO.getAllPlayerStatistics(connection, playerUUID);

            assertEquals(1, result.size());
            NamespacedKey key = NamespacedKey.fromString("test:my_stat");
            assertNotNull(key);
            assertTrue(result.containsKey(key));
            assertEquals(StatisticType.INT, result.get(key).type());
            assertEquals(42, result.get(key).value());
        }

        @Test
        @DisplayName("Given player has LONG statistic, when getAllPlayerStatistics, then returns long value")
        void returnsLongStatistic() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString("statistic_key")).thenReturn("test:long_stat");
            when(resultSet.getString("stat_type")).thenReturn("LONG");
            when(resultSet.getLong("long_value")).thenReturn(999999L);

            Map<NamespacedKey, StatisticEntry> result = PlayerStatisticDAO.getAllPlayerStatistics(connection, playerUUID);

            assertEquals(1, result.size());
            NamespacedKey key = NamespacedKey.fromString("test:long_stat");
            assertEquals(999999L, result.get(key).value());
        }

        @Test
        @DisplayName("Given player has DOUBLE statistic, when getAllPlayerStatistics, then returns double value")
        void returnsDoubleStatistic() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString("statistic_key")).thenReturn("test:double_stat");
            when(resultSet.getString("stat_type")).thenReturn("DOUBLE");
            when(resultSet.getDouble("double_value")).thenReturn(3.14);

            Map<NamespacedKey, StatisticEntry> result = PlayerStatisticDAO.getAllPlayerStatistics(connection, playerUUID);

            assertEquals(1, result.size());
            NamespacedKey key = NamespacedKey.fromString("test:double_stat");
            assertEquals(3.14, result.get(key).value());
        }

        @Test
        @DisplayName("Given player has STRING statistic, when getAllPlayerStatistics, then returns string value")
        void returnsStringStatistic() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString("statistic_key")).thenReturn("test:string_stat");
            when(resultSet.getString("stat_type")).thenReturn("STRING");
            when(resultSet.getString("string_value")).thenReturn("hello world");

            Map<NamespacedKey, StatisticEntry> result = PlayerStatisticDAO.getAllPlayerStatistics(connection, playerUUID);

            assertEquals(1, result.size());
            NamespacedKey key = NamespacedKey.fromString("test:string_stat");
            assertEquals("hello world", result.get(key).value());
        }

        @Test
        @DisplayName("Given player has STRING statistic with null value, when getAllPlayerStatistics, then returns empty string")
        void returnsEmptyString_whenStringValueIsNull() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString("statistic_key")).thenReturn("test:null_string");
            when(resultSet.getString("stat_type")).thenReturn("STRING");
            when(resultSet.getString("string_value")).thenReturn(null);

            Map<NamespacedKey, StatisticEntry> result = PlayerStatisticDAO.getAllPlayerStatistics(connection, playerUUID);

            NamespacedKey key = NamespacedKey.fromString("test:null_string");
            assertEquals("", result.get(key).value());
        }

        @Test
        @DisplayName("Given player has TIMESTAMP statistic, when getAllPlayerStatistics, then returns Instant value")
        void returnsTimestampStatistic() throws SQLException {
            long epochMillis = 1718000000000L;
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString("statistic_key")).thenReturn("test:timestamp_stat");
            when(resultSet.getString("stat_type")).thenReturn("TIMESTAMP");
            when(resultSet.getLong("timestamp_value")).thenReturn(epochMillis);

            Map<NamespacedKey, StatisticEntry> result = PlayerStatisticDAO.getAllPlayerStatistics(connection, playerUUID);

            NamespacedKey key = NamespacedKey.fromString("test:timestamp_stat");
            assertEquals(Instant.ofEpochMilli(epochMillis), result.get(key).value());
        }

        @Test
        @DisplayName("Given player has SET_STRING statistic, when getAllPlayerStatistics, then returns deserialized set")
        void returnsSetStringStatistic() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString("statistic_key")).thenReturn("test:set_stat");
            when(resultSet.getString("stat_type")).thenReturn("SET_STRING");
            when(resultSet.getString("string_value")).thenReturn("[\"alpha\",\"beta\"]");

            Map<NamespacedKey, StatisticEntry> result = PlayerStatisticDAO.getAllPlayerStatistics(connection, playerUUID);

            NamespacedKey key = NamespacedKey.fromString("test:set_stat");
            @SuppressWarnings("unchecked")
            Set<String> set = (Set<String>) result.get(key).value();
            assertTrue(set.contains("alpha"));
            assertTrue(set.contains("beta"));
            assertEquals(2, set.size());
        }

        @Test
        @DisplayName("Given player has SET_STRING with null value, when getAllPlayerStatistics, then returns empty set")
        void returnsEmptySet_whenSetStringValueIsNull() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString("statistic_key")).thenReturn("test:null_set");
            when(resultSet.getString("stat_type")).thenReturn("SET_STRING");
            when(resultSet.getString("string_value")).thenReturn(null);

            Map<NamespacedKey, StatisticEntry> result = PlayerStatisticDAO.getAllPlayerStatistics(connection, playerUUID);

            NamespacedKey key = NamespacedKey.fromString("test:null_set");
            @SuppressWarnings("unchecked")
            Set<String> set = (Set<String>) result.get(key).value();
            assertTrue(set.isEmpty());
        }

        @Test
        @DisplayName("Given invalid key in database, when getAllPlayerStatistics, then skips that entry")
        void skipsInvalidKey() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString("statistic_key")).thenReturn("invalid key no colon");
            when(resultSet.getString("stat_type")).thenReturn("INT");

            Map<NamespacedKey, StatisticEntry> result = PlayerStatisticDAO.getAllPlayerStatistics(connection, playerUUID);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given unknown stat type in database, when getAllPlayerStatistics, then skips that entry")
        void skipsUnknownStatType() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString("statistic_key")).thenReturn("test:my_stat");
            when(resultSet.getString("stat_type")).thenReturn("UNKNOWN_TYPE");

            Map<NamespacedKey, StatisticEntry> result = PlayerStatisticDAO.getAllPlayerStatistics(connection, playerUUID);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given multiple statistics, when getAllPlayerStatistics, then returns all")
        void returnsMultipleStatistics() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, true, false);
            when(resultSet.getString("statistic_key")).thenReturn("test:stat_a", "test:stat_b");
            when(resultSet.getString("stat_type")).thenReturn("INT", "DOUBLE");
            when(resultSet.getInt("int_value")).thenReturn(10);
            when(resultSet.getDouble("double_value")).thenReturn(2.5);

            Map<NamespacedKey, StatisticEntry> result = PlayerStatisticDAO.getAllPlayerStatistics(connection, playerUUID);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Given no statistics for player, when getAllPlayerStatistics, then returns empty map")
        void returnsEmptyMap_whenNoStatistics() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);

            Map<NamespacedKey, StatisticEntry> result = PlayerStatisticDAO.getAllPlayerStatistics(connection, playerUUID);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given SQL exception, when getAllPlayerStatistics, then returns empty map")
        void returnsEmptyMap_whenSQLException() throws SQLException {
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("query failed"));

            Map<NamespacedKey, StatisticEntry> result = PlayerStatisticDAO.getAllPlayerStatistics(connection, playerUUID);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getPlayerStatistic")
    class GetPlayerStatistic {

        private final UUID playerUUID = UUID.randomUUID();
        private final NamespacedKey key = NamespacedKey.fromString("test:single_stat");

        @Test
        @DisplayName("Given statistic exists, when getPlayerStatistic, then returns it")
        void returnsStatistic_whenExists() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getString("stat_type")).thenReturn("INT");
            when(resultSet.getInt("int_value")).thenReturn(77);

            Optional<StatisticEntry> result = PlayerStatisticDAO.getPlayerStatistic(connection, playerUUID, key);

            assertTrue(result.isPresent());
            assertEquals(StatisticType.INT, result.get().type());
            assertEquals(77, result.get().value());
        }

        @Test
        @DisplayName("Given statistic does not exist, when getPlayerStatistic, then returns empty")
        void returnsEmpty_whenNotExists() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);

            Optional<StatisticEntry> result = PlayerStatisticDAO.getPlayerStatistic(connection, playerUUID, key);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given unknown stat type, when getPlayerStatistic, then returns empty")
        void returnsEmpty_whenUnknownType() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getString("stat_type")).thenReturn("BOGUS");

            Optional<StatisticEntry> result = PlayerStatisticDAO.getPlayerStatistic(connection, playerUUID, key);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given SQL exception, when getPlayerStatistic, then returns empty")
        void returnsEmpty_whenSQLException() throws SQLException {
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));

            Optional<StatisticEntry> result = PlayerStatisticDAO.getPlayerStatistic(connection, playerUUID, key);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("savePlayerStatistic")
    class SavePlayerStatistic {

        private final UUID playerUUID = UUID.randomUUID();

        @Test
        @DisplayName("Given INT entry, when savePlayerStatistic, then sets int parameter")
        void setsIntParameter() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            NamespacedKey key = NamespacedKey.fromString("test:int_save");
            StatisticEntry entry = new StatisticEntry(key, StatisticType.INT, 42);

            PreparedStatement result = PlayerStatisticDAO.savePlayerStatistic(connection, playerUUID, entry);

            assertNotNull(result);
            verify(statement).setString(1, playerUUID.toString());
            verify(statement).setString(2, key.toString());
            verify(statement).setString(3, "INT");
            verify(statement).setInt(4, 42);
        }

        @Test
        @DisplayName("Given LONG entry, when savePlayerStatistic, then sets long parameter")
        void setsLongParameter() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            NamespacedKey key = NamespacedKey.fromString("test:long_save");
            StatisticEntry entry = new StatisticEntry(key, StatisticType.LONG, 99999L);

            PlayerStatisticDAO.savePlayerStatistic(connection, playerUUID, entry);

            verify(statement).setLong(5, 99999L);
        }

        @Test
        @DisplayName("Given DOUBLE entry, when savePlayerStatistic, then sets double parameter")
        void setsDoubleParameter() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            NamespacedKey key = NamespacedKey.fromString("test:double_save");
            StatisticEntry entry = new StatisticEntry(key, StatisticType.DOUBLE, 1.23);

            PlayerStatisticDAO.savePlayerStatistic(connection, playerUUID, entry);

            verify(statement).setDouble(6, 1.23);
        }

        @Test
        @DisplayName("Given STRING entry, when savePlayerStatistic, then sets string parameter")
        void setsStringParameter() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            NamespacedKey key = NamespacedKey.fromString("test:string_save");
            StatisticEntry entry = new StatisticEntry(key, StatisticType.STRING, "hello");

            PlayerStatisticDAO.savePlayerStatistic(connection, playerUUID, entry);

            verify(statement).setString(7, "hello");
        }

        @Test
        @DisplayName("Given TIMESTAMP entry, when savePlayerStatistic, then sets epoch millis")
        void setsTimestampParameter() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            NamespacedKey key = NamespacedKey.fromString("test:ts_save");
            Instant now = Instant.ofEpochMilli(1718000000000L);
            StatisticEntry entry = new StatisticEntry(key, StatisticType.TIMESTAMP, now);

            PlayerStatisticDAO.savePlayerStatistic(connection, playerUUID, entry);

            verify(statement).setLong(8, 1718000000000L);
        }

        @Test
        @DisplayName("Given SET_STRING entry, when savePlayerStatistic, then sets serialized string")
        void setsSetStringParameter() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            NamespacedKey key = NamespacedKey.fromString("test:set_save");
            Set<String> set = new LinkedHashSet<>();
            set.add("a");
            set.add("b");
            StatisticEntry entry = new StatisticEntry(key, StatisticType.SET_STRING, set);

            PlayerStatisticDAO.savePlayerStatistic(connection, playerUUID, entry);

            verify(statement).setString(7, "[\"a\",\"b\"]");
        }

        @Test
        @DisplayName("Given SQL exception during preparation, when savePlayerStatistic, then throws RuntimeException")
        void throwsRuntimeException_whenSQLExceptionOccurs() throws SQLException {
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("prepare failed"));
            NamespacedKey key = NamespacedKey.fromString("test:fail_save");
            StatisticEntry entry = new StatisticEntry(key, StatisticType.INT, 1);

            assertThrows(RuntimeException.class, () -> PlayerStatisticDAO.savePlayerStatistic(connection, playerUUID, entry));
        }
    }

    @Nested
    @DisplayName("savePlayerStatistics")
    class SavePlayerStatistics {

        private final UUID playerUUID = UUID.randomUUID();

        @Test
        @DisplayName("Given multiple entries, when savePlayerStatistics, then returns list of prepared statements")
        void returnsListOfStatements() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            NamespacedKey key1 = NamespacedKey.fromString("test:stat1");
            NamespacedKey key2 = NamespacedKey.fromString("test:stat2");
            Map<NamespacedKey, StatisticEntry> entries = Map.of(
                    key1, new StatisticEntry(key1, StatisticType.INT, 1),
                    key2, new StatisticEntry(key2, StatisticType.INT, 2)
            );

            List<PreparedStatement> result = PlayerStatisticDAO.savePlayerStatistics(connection, playerUUID, entries);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Given empty entries, when savePlayerStatistics, then returns empty list")
        void returnsEmptyList_whenNoEntries() {
            List<PreparedStatement> result = PlayerStatisticDAO.savePlayerStatistics(
                    connection, playerUUID, Map.of());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("deletePlayerStatistic")
    class DeletePlayerStatistic {

        private final UUID playerUUID = UUID.randomUUID();
        private final NamespacedKey key = NamespacedKey.fromString("test:delete_stat");

        @Test
        @DisplayName("Given valid parameters, when deletePlayerStatistic, then returns prepared statement with correct bindings")
        void returnsStatement_withCorrectBindings() throws SQLException {
            when(connection.prepareStatement(anyString())).thenReturn(statement);

            PreparedStatement result = PlayerStatisticDAO.deletePlayerStatistic(connection, playerUUID, key);

            assertNotNull(result);
            verify(statement).setString(1, playerUUID.toString());
            verify(statement).setString(2, key.toString());
        }

        @Test
        @DisplayName("Given SQL exception during preparation, when deletePlayerStatistic, then throws RuntimeException")
        void throwsRuntimeException_whenSQLExceptionOccurs() throws SQLException {
            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("delete prepare failed"));

            assertThrows(RuntimeException.class,
                    () -> PlayerStatisticDAO.deletePlayerStatistic(connection, playerUUID, key));
        }
    }
}

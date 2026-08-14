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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
 * Integration tests for {@link PlayerStatisticDAO} using a real in-memory SQLite database.
 * Verifies actual SQL execution and statistic save/load roundtrip for every {@link StatisticType}.
 */
class PlayerStatisticDAOIntegrationTest {

    private Connection connection;
    private Database mockDatabase;
    private MockedStatic<CorePlugin> corePluginStatic;

    private static final UUID PLAYER_UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc");
    private static final UUID OTHER_UUID = UUID.fromString("87654321-4321-4321-4321-cba987654321");

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        mockDatabase = mock(Database.class);
        when(mockDatabase.tableExists(any(Connection.class), anyString())).thenReturn(false);

        TableVersionHistoryDAO.attemptCreateTable(connection, mockDatabase);
        PlayerStatisticDAO.attemptCreateTable(connection, mockDatabase);

        CorePlugin mockPlugin = mock(CorePlugin.class);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("TestLogger"));

        corePluginStatic = mockStatic(CorePlugin.class);
        corePluginStatic.when(CorePlugin::getInstance).thenReturn(mockPlugin);
    }

    @AfterEach
    void tearDown() throws SQLException {
        corePluginStatic.close();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private void executeSave(PreparedStatement stmt) throws SQLException {
        stmt.executeUpdate();
        stmt.close();
    }

    @Nested
    @DisplayName("INT statistic roundtrip")
    class IntRoundtrip {

        private static final NamespacedKey KEY = new NamespacedKey("mccore", "kills");

        @Test
        @DisplayName("Given an INT statistic is saved, when loading it, then returns the correct value")
        void saveAndLoad_intStatistic() throws SQLException {
            StatisticEntry entry = new StatisticEntry(KEY, StatisticType.INT, 42);
            executeSave(PlayerStatisticDAO.savePlayerStatistic(connection, PLAYER_UUID, entry));

            Optional<StatisticEntry> loaded = PlayerStatisticDAO.getPlayerStatistic(connection, PLAYER_UUID, KEY);
            assertTrue(loaded.isPresent());
            assertEquals(StatisticType.INT, loaded.get().type());
            assertEquals(42, (int) loaded.get().value());
        }

        @Test
        @DisplayName("Given an INT statistic is saved then updated, when loading it, then returns the updated value")
        void saveAndUpdate_intStatistic_returnsUpdatedValue() throws SQLException {
            StatisticEntry original = new StatisticEntry(KEY, StatisticType.INT, 42);
            executeSave(PlayerStatisticDAO.savePlayerStatistic(connection, PLAYER_UUID, original));

            StatisticEntry updated = new StatisticEntry(KEY, StatisticType.INT, 99);
            executeSave(PlayerStatisticDAO.savePlayerStatistic(connection, PLAYER_UUID, updated));

            Optional<StatisticEntry> loaded = PlayerStatisticDAO.getPlayerStatistic(connection, PLAYER_UUID, KEY);
            assertTrue(loaded.isPresent());
            assertEquals(99, (int) loaded.get().value());
        }
    }

    @Nested
    @DisplayName("LONG statistic roundtrip")
    class LongRoundtrip {

        private static final NamespacedKey KEY = new NamespacedKey("mccore", "experience");

        @Test
        @DisplayName("Given a LONG statistic is saved, when loading it, then returns the correct value")
        void saveAndLoad_longStatistic() throws SQLException {
            StatisticEntry entry = new StatisticEntry(KEY, StatisticType.LONG, 9999999999L);
            executeSave(PlayerStatisticDAO.savePlayerStatistic(connection, PLAYER_UUID, entry));

            Optional<StatisticEntry> loaded = PlayerStatisticDAO.getPlayerStatistic(connection, PLAYER_UUID, KEY);
            assertTrue(loaded.isPresent());
            assertEquals(StatisticType.LONG, loaded.get().type());
            assertEquals(9999999999L, (long) loaded.get().value());
        }
    }

    @Nested
    @DisplayName("DOUBLE statistic roundtrip")
    class DoubleRoundtrip {

        private static final NamespacedKey KEY = new NamespacedKey("mccore", "accuracy");

        @Test
        @DisplayName("Given a DOUBLE statistic is saved, when loading it, then returns the correct value")
        void saveAndLoad_doubleStatistic() throws SQLException {
            StatisticEntry entry = new StatisticEntry(KEY, StatisticType.DOUBLE, 3.14159);
            executeSave(PlayerStatisticDAO.savePlayerStatistic(connection, PLAYER_UUID, entry));

            Optional<StatisticEntry> loaded = PlayerStatisticDAO.getPlayerStatistic(connection, PLAYER_UUID, KEY);
            assertTrue(loaded.isPresent());
            assertEquals(StatisticType.DOUBLE, loaded.get().type());
            assertEquals(3.14159, (double) loaded.get().value(), 0.0001);
        }
    }

    @Nested
    @DisplayName("STRING statistic roundtrip")
    class StringRoundtrip {

        private static final NamespacedKey KEY = new NamespacedKey("mccore", "nickname");

        @Test
        @DisplayName("Given a STRING statistic is saved, when loading it, then returns the correct value")
        void saveAndLoad_stringStatistic() throws SQLException {
            StatisticEntry entry = new StatisticEntry(KEY, StatisticType.STRING, "TheHero");
            executeSave(PlayerStatisticDAO.savePlayerStatistic(connection, PLAYER_UUID, entry));

            Optional<StatisticEntry> loaded = PlayerStatisticDAO.getPlayerStatistic(connection, PLAYER_UUID, KEY);
            assertTrue(loaded.isPresent());
            assertEquals(StatisticType.STRING, loaded.get().type());
            assertEquals("TheHero", loaded.get().value());
        }

        @Test
        @DisplayName("Given a STRING statistic with special characters, when roundtripped, then preserves value")
        void saveAndLoad_stringWithSpecialChars() throws SQLException {
            StatisticEntry entry = new StatisticEntry(KEY, StatisticType.STRING, "it's a \"test\" with, commas");
            executeSave(PlayerStatisticDAO.savePlayerStatistic(connection, PLAYER_UUID, entry));

            Optional<StatisticEntry> loaded = PlayerStatisticDAO.getPlayerStatistic(connection, PLAYER_UUID, KEY);
            assertTrue(loaded.isPresent());
            assertEquals("it's a \"test\" with, commas", loaded.get().value());
        }
    }

    @Nested
    @DisplayName("TIMESTAMP statistic roundtrip")
    class TimestampRoundtrip {

        private static final NamespacedKey KEY = new NamespacedKey("mccore", "last_login");

        @Test
        @DisplayName("Given a TIMESTAMP statistic is saved, when loading it, then returns the correct instant")
        void saveAndLoad_timestampStatistic() throws SQLException {
            Instant now = Instant.ofEpochMilli(1700000000000L);
            StatisticEntry entry = new StatisticEntry(KEY, StatisticType.TIMESTAMP, now);
            executeSave(PlayerStatisticDAO.savePlayerStatistic(connection, PLAYER_UUID, entry));

            Optional<StatisticEntry> loaded = PlayerStatisticDAO.getPlayerStatistic(connection, PLAYER_UUID, KEY);
            assertTrue(loaded.isPresent());
            assertEquals(StatisticType.TIMESTAMP, loaded.get().type());
            assertEquals(now, loaded.get().value());
        }
    }

    @Nested
    @DisplayName("SET_STRING statistic roundtrip")
    class SetStringRoundtrip {

        private static final NamespacedKey KEY = new NamespacedKey("mccore", "unlocked_skills");

        @Test
        @DisplayName("Given a SET_STRING statistic is saved, when loading it, then returns the correct set")
        void saveAndLoad_setStringStatistic() throws SQLException {
            Set<String> skills = new LinkedHashSet<>();
            skills.add("sword");
            skills.add("archery");
            skills.add("mining");
            StatisticEntry entry = new StatisticEntry(KEY, StatisticType.SET_STRING, skills);
            executeSave(PlayerStatisticDAO.savePlayerStatistic(connection, PLAYER_UUID, entry));

            Optional<StatisticEntry> loaded = PlayerStatisticDAO.getPlayerStatistic(connection, PLAYER_UUID, KEY);
            assertTrue(loaded.isPresent());
            assertEquals(StatisticType.SET_STRING, loaded.get().type());
            @SuppressWarnings("unchecked")
            Set<String> loadedSet = (Set<String>) loaded.get().value();
            assertEquals(skills, loadedSet);
        }

        @Test
        @DisplayName("Given an empty SET_STRING is saved, when loading it, then returns empty set")
        void saveAndLoad_emptySetString() throws SQLException {
            Set<String> emptySet = new LinkedHashSet<>();
            StatisticEntry entry = new StatisticEntry(KEY, StatisticType.SET_STRING, emptySet);
            executeSave(PlayerStatisticDAO.savePlayerStatistic(connection, PLAYER_UUID, entry));

            Optional<StatisticEntry> loaded = PlayerStatisticDAO.getPlayerStatistic(connection, PLAYER_UUID, KEY);
            assertTrue(loaded.isPresent());
            @SuppressWarnings("unchecked")
            Set<String> loadedSet = (Set<String>) loaded.get().value();
            assertTrue(loadedSet.isEmpty());
        }

        @Test
        @DisplayName("Given a SET_STRING with commas and quotes, when roundtripped, then preserves elements")
        void saveAndLoad_setStringWithSpecialChars() throws SQLException {
            Set<String> set = new LinkedHashSet<>();
            set.add("item,with,commas");
            set.add("item \"with\" quotes");
            StatisticEntry entry = new StatisticEntry(KEY, StatisticType.SET_STRING, set);
            executeSave(PlayerStatisticDAO.savePlayerStatistic(connection, PLAYER_UUID, entry));

            Optional<StatisticEntry> loaded = PlayerStatisticDAO.getPlayerStatistic(connection, PLAYER_UUID, KEY);
            assertTrue(loaded.isPresent());
            @SuppressWarnings("unchecked")
            Set<String> loadedSet = (Set<String>) loaded.get().value();
            assertEquals(set, loadedSet);
        }
    }

    @Nested
    @DisplayName("getAllPlayerStatistics")
    class GetAllStatistics {

        @Test
        @DisplayName("Given multiple statistics are saved, when getAllPlayerStatistics is called, then returns all")
        void getAllStatistics_returnsAllSaved() throws SQLException {
            NamespacedKey killsKey = new NamespacedKey("mccore", "kills");
            NamespacedKey xpKey = new NamespacedKey("mccore", "xp");
            NamespacedKey nameKey = new NamespacedKey("mccore", "name");

            executeSave(PlayerStatisticDAO.savePlayerStatistic(connection, PLAYER_UUID,
                    new StatisticEntry(killsKey, StatisticType.INT, 100)));
            executeSave(PlayerStatisticDAO.savePlayerStatistic(connection, PLAYER_UUID,
                    new StatisticEntry(xpKey, StatisticType.LONG, 50000L)));
            executeSave(PlayerStatisticDAO.savePlayerStatistic(connection, PLAYER_UUID,
                    new StatisticEntry(nameKey, StatisticType.STRING, "TestPlayer")));

            Map<NamespacedKey, StatisticEntry> all = PlayerStatisticDAO.getAllPlayerStatistics(connection, PLAYER_UUID);

            assertEquals(3, all.size());
            assertEquals(100, (int) all.get(killsKey).value());
            assertEquals(50000L, (long) all.get(xpKey).value());
            assertEquals("TestPlayer", all.get(nameKey).value());
        }

        @Test
        @DisplayName("Given no statistics exist, when getAllPlayerStatistics is called, then returns empty map")
        void getAllStatistics_returnsEmpty_whenNothingSaved() {
            Map<NamespacedKey, StatisticEntry> all = PlayerStatisticDAO.getAllPlayerStatistics(connection, PLAYER_UUID);
            assertTrue(all.isEmpty());
        }
    }

    @Nested
    @DisplayName("deletePlayerStatistic")
    class DeleteStatistic {

        @Test
        @DisplayName("Given a statistic exists, when deletePlayerStatistic is called, then it is removed")
        void deleteStatistic_removesEntry() throws SQLException {
            NamespacedKey key = new NamespacedKey("mccore", "kills");
            executeSave(PlayerStatisticDAO.savePlayerStatistic(connection, PLAYER_UUID,
                    new StatisticEntry(key, StatisticType.INT, 42)));

            executeSave(PlayerStatisticDAO.deletePlayerStatistic(connection, PLAYER_UUID, key));

            Optional<StatisticEntry> loaded = PlayerStatisticDAO.getPlayerStatistic(connection, PLAYER_UUID, key);
            assertFalse(loaded.isPresent());
        }

        @Test
        @DisplayName("Given a statistic is deleted, when other statistics exist, then they are not affected")
        void deleteStatistic_doesNotAffectOtherStats() throws SQLException {
            NamespacedKey killsKey = new NamespacedKey("mccore", "kills");
            NamespacedKey xpKey = new NamespacedKey("mccore", "xp");

            executeSave(PlayerStatisticDAO.savePlayerStatistic(connection, PLAYER_UUID,
                    new StatisticEntry(killsKey, StatisticType.INT, 42)));
            executeSave(PlayerStatisticDAO.savePlayerStatistic(connection, PLAYER_UUID,
                    new StatisticEntry(xpKey, StatisticType.LONG, 1000L)));

            executeSave(PlayerStatisticDAO.deletePlayerStatistic(connection, PLAYER_UUID, killsKey));

            assertFalse(PlayerStatisticDAO.getPlayerStatistic(connection, PLAYER_UUID, killsKey).isPresent());
            assertTrue(PlayerStatisticDAO.getPlayerStatistic(connection, PLAYER_UUID, xpKey).isPresent());
        }
    }

    @Nested
    @DisplayName("player isolation")
    class PlayerIsolation {

        @Test
        @DisplayName("Given different players have statistics, when loading each, then returns only their own")
        void differentPlayersHaveIsolatedStatistics() throws SQLException {
            NamespacedKey key = new NamespacedKey("mccore", "kills");

            executeSave(PlayerStatisticDAO.savePlayerStatistic(connection, PLAYER_UUID,
                    new StatisticEntry(key, StatisticType.INT, 100)));
            executeSave(PlayerStatisticDAO.savePlayerStatistic(connection, OTHER_UUID,
                    new StatisticEntry(key, StatisticType.INT, 200)));

            Optional<StatisticEntry> player1 = PlayerStatisticDAO.getPlayerStatistic(connection, PLAYER_UUID, key);
            Optional<StatisticEntry> player2 = PlayerStatisticDAO.getPlayerStatistic(connection, OTHER_UUID, key);

            assertTrue(player1.isPresent());
            assertTrue(player2.isPresent());
            assertEquals(100, (int) player1.get().value());
            assertEquals(200, (int) player2.get().value());
        }
    }

    @Nested
    @DisplayName("savePlayerStatistics batch")
    class BatchSave {

        @Test
        @DisplayName("Given multiple entries, when savePlayerStatistics is called, then all entries are saved correctly")
        void batchSave_savesAllEntries() throws SQLException {
            NamespacedKey killsKey = new NamespacedKey("mccore", "kills");
            NamespacedKey xpKey = new NamespacedKey("mccore", "xp");

            Map<NamespacedKey, StatisticEntry> entries = new HashMap<>();
            entries.put(killsKey, new StatisticEntry(killsKey, StatisticType.INT, 50));
            entries.put(xpKey, new StatisticEntry(xpKey, StatisticType.DOUBLE, 99.5));

            List<PreparedStatement> stmts = PlayerStatisticDAO.savePlayerStatistics(connection, PLAYER_UUID, entries);
            for (PreparedStatement stmt : stmts) {
                stmt.executeUpdate();
                stmt.close();
            }

            Map<NamespacedKey, StatisticEntry> loaded = PlayerStatisticDAO.getAllPlayerStatistics(connection, PLAYER_UUID);
            assertEquals(2, loaded.size());
            assertEquals(50, (int) loaded.get(killsKey).value());
            assertEquals(99.5, (double) loaded.get(xpKey).value(), 0.001);
        }
    }

    @Nested
    @DisplayName("updateTable")
    class UpdateTable {

        @Test
        @DisplayName("Given core_player_statistics has no version, when updateTable is called, then sets version to 1")
        void updateTable_setsVersionToOne() {
            PlayerStatisticDAO.updateTable(connection);

            int version = TableVersionHistoryDAO.getLatestVersion(connection, "core_player_statistics");
            assertEquals(1, version);
        }
    }
}

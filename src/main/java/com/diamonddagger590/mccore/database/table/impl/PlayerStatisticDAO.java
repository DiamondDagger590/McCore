package com.diamonddagger590.mccore.database.table.impl;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.statistic.StatisticEntry;
import com.diamonddagger590.mccore.statistic.StatisticType;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * This DAO is in charge of dealing with the saving and loading of player statistics.
 */
public class PlayerStatisticDAO {

    private static final String TABLE_NAME = "core_player_statistics";
    private static final int CURRENT_TABLE_VERSION = 1;

    /**
     * Attempts to create a new table for this DAO provided that the table does not already exist.
     *
     * @param connection The {@link Connection} to use to attempt the creation.
     * @param database   The {@link Database} being used to attempt to create the table.
     * @return {@code true} if a new table was made or {@code false} otherwise.
     */
    public static boolean attemptCreateTable(@NotNull Connection connection, @NotNull Database database) {
        if (database.tableExists(connection, TABLE_NAME)) {
            return false;
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "CREATE TABLE `" + TABLE_NAME + "` (" +
                "`uuid` VARCHAR(36) NOT NULL, " +
                "`statistic_key` VARCHAR(256) NOT NULL, " +
                "`stat_type` VARCHAR(32) NOT NULL, " +
                "`int_value` INTEGER, " +
                "`long_value` BIGINT, " +
                "`double_value` DOUBLE, " +
                "`string_value` TEXT, " +
                "`timestamp_value` BIGINT, " +
                "PRIMARY KEY (`uuid`, `statistic_key`)" +
                ");"
        )) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "CREATE INDEX IF NOT EXISTS idx_core_stats_uuid ON " + TABLE_NAME + " (uuid)"
        )) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Checks to see if there are any version differences from the live version of this SQL table and
     * the current version. If there are any differences, it will iteratively go through and update
     * through each version to ensure the database is safe to run queries on.
     *
     * @param connection The {@link Connection} that will be used to run the changes.
     */
    public static void updateTable(@NotNull Connection connection) {
        int lastStoredVersion = TableVersionHistoryDAO.getLatestVersion(connection, TABLE_NAME);
        if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
            return;
        }
        if (lastStoredVersion == 0) {
            TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
        }
    }

    /**
     * Gets all statistics for a given player.
     *
     * @param connection The {@link Connection} to use.
     * @param playerUUID The {@link UUID} of the player.
     * @return A map of all statistic entries for the player.
     */
    @NotNull
    public static Map<NamespacedKey, StatisticEntry> getAllPlayerStatistics(
            @NotNull Connection connection,
            @NotNull UUID playerUUID
    ) {
        Map<NamespacedKey, StatisticEntry> entries = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT statistic_key, stat_type, int_value, long_value, double_value, " +
                "string_value, timestamp_value FROM " + TABLE_NAME + " WHERE uuid = ?"
        )) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String keyString = rs.getString("statistic_key");
                    NamespacedKey key = NamespacedKey.fromString(keyString);
                    if (key == null) {
                        CorePlugin.getInstance().getLogger().warning(
                                "Skipping statistic with invalid key: " + keyString
                        );
                        continue;
                    }
                    String rawType = rs.getString("stat_type");
                    StatisticType type;
                    try {
                        type = StatisticType.valueOf(rawType);
                    } catch (IllegalArgumentException e) {
                        CorePlugin.getInstance().getLogger().warning(
                                "Skipping statistic with unknown type '" + rawType + "' for key: " + keyString
                        );
                        continue;
                    }
                    Object value = readValueFromResultSet(rs, type);
                    entries.put(key, new StatisticEntry(key, type, value));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    /**
     * Gets a single statistic for a given player.
     *
     * @param connection The {@link Connection} to use.
     * @param playerUUID The {@link UUID} of the player.
     * @param key        The {@link NamespacedKey} of the statistic.
     * @return An {@link Optional} containing the statistic entry, or empty if not found.
     */
    @NotNull
    public static Optional<StatisticEntry> getPlayerStatistic(
            @NotNull Connection connection,
            @NotNull UUID playerUUID,
            @NotNull NamespacedKey key
    ) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT stat_type, int_value, long_value, double_value, " +
                "string_value, timestamp_value FROM " + TABLE_NAME +
                " WHERE uuid = ? AND statistic_key = ?"
        )) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, key.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    String rawType = rs.getString("stat_type");
                    StatisticType type;
                    try {
                        type = StatisticType.valueOf(rawType);
                    } catch (IllegalArgumentException e) {
                        CorePlugin.getInstance().getLogger().warning(
                                "Skipping statistic with unknown type '" + rawType + "' for key: " + key
                        );
                        return Optional.empty();
                    }
                    Object value = readValueFromResultSet(rs, type);
                    return Optional.of(new StatisticEntry(key, type, value));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Saves multiple player statistics. Returns a list of prepared statements
     * to be executed in a transaction.
     *
     * @param connection The {@link Connection} to use.
     * @param playerUUID The {@link UUID} of the player.
     * @param entries    The entries to save.
     * @return A {@link List} of {@link PreparedStatement}s to execute.
     */
    @NotNull
    public static List<PreparedStatement> savePlayerStatistics(
            @NotNull Connection connection,
            @NotNull UUID playerUUID,
            @NotNull Map<NamespacedKey, StatisticEntry> entries
    ) {
        return entries.values().stream()
                .map(entry -> savePlayerStatistic(connection, playerUUID, entry))
                .toList();
    }

    /**
     * Saves a single player statistic. Returns a prepared statement to be executed
     * in a transaction.
     *
     * @param connection The {@link Connection} to use.
     * @param playerUUID The {@link UUID} of the player.
     * @param entry      The entry to save.
     * @return The {@link PreparedStatement} to execute.
     * @throws RuntimeException wrapping {@link SQLException} if statement preparation fails.
     */
    @NotNull
    public static PreparedStatement savePlayerStatistic(
            @NotNull Connection connection,
            @NotNull UUID playerUUID,
            @NotNull StatisticEntry entry
    ) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "REPLACE INTO " + TABLE_NAME +
                    " (uuid, statistic_key, stat_type, int_value, long_value, " +
                    "double_value, string_value, timestamp_value) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            );
            statement.setString(1, playerUUID.toString());
            statement.setString(2, entry.key().toString());
            statement.setString(3, entry.type().name());

            // Null all typed columns, then set the one that matches
            statement.setNull(4, java.sql.Types.INTEGER);
            statement.setNull(5, java.sql.Types.BIGINT);
            statement.setNull(6, java.sql.Types.DOUBLE);
            statement.setNull(7, java.sql.Types.VARCHAR);
            statement.setNull(8, java.sql.Types.BIGINT);

            switch (entry.type()) {
                case INT -> statement.setInt(4, (Integer) entry.value());
                case LONG -> statement.setLong(5, (Long) entry.value());
                case DOUBLE -> statement.setDouble(6, (Double) entry.value());
                case STRING -> statement.setString(7, (String) entry.value());
                case TIMESTAMP -> statement.setLong(8, ((Instant) entry.value()).toEpochMilli());
                case SET_STRING -> {
                    @SuppressWarnings("unchecked")
                    Set<String> set = (Set<String>) entry.value();
                    statement.setString(7, serializeStringSet(set));
                }
            }
            return statement;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes a single player statistic. Returns a prepared statement to be executed
     * in a transaction.
     *
     * @param connection The {@link Connection} to use.
     * @param playerUUID The {@link UUID} of the player.
     * @param key        The {@link NamespacedKey} of the statistic to delete.
     * @return The {@link PreparedStatement} to execute.
     * @throws RuntimeException wrapping {@link SQLException} if statement preparation fails.
     */
    @NotNull
    public static PreparedStatement deletePlayerStatistic(
            @NotNull Connection connection,
            @NotNull UUID playerUUID,
            @NotNull NamespacedKey key
    ) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM " + TABLE_NAME + " WHERE uuid = ? AND statistic_key = ?"
            );
            statement.setString(1, playerUUID.toString());
            statement.setString(2, key.toString());
            return statement;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static Object readValueFromResultSet(
            @NotNull ResultSet rs,
            @NotNull StatisticType type
    ) throws SQLException {
        return switch (type) {
            case INT -> rs.getInt("int_value");
            case LONG -> rs.getLong("long_value");
            case DOUBLE -> rs.getDouble("double_value");
            case STRING -> {
                String value = rs.getString("string_value");
                yield value != null ? value : "";
            }
            case TIMESTAMP -> Instant.ofEpochMilli(rs.getLong("timestamp_value"));
            case SET_STRING -> {
                String value = rs.getString("string_value");
                yield value != null ? deserializeStringSet(value) : new LinkedHashSet<String>();
            }
        };
    }

    @NotNull
    static Set<String> deserializeStringSet(@NotNull String json) {
        Set<String> result = new LinkedHashSet<>();
        if (json.equals("[]") || json.isEmpty()) {
            return result;
        }
        // Remove outer brackets
        String inner = json.substring(1, json.length() - 1);
        // Parse quoted elements properly, handling commas within quoted strings
        int i = 0;
        while (i < inner.length()) {
            // Skip whitespace
            while (i < inner.length() && inner.charAt(i) == ' ') {
                i++;
            }
            if (i >= inner.length()) {
                break;
            }
            if (inner.charAt(i) == '"') {
                // Find the closing quote, respecting escaped quotes
                StringBuilder element = new StringBuilder();
                i++; // skip opening quote
                while (i < inner.length()) {
                    char c = inner.charAt(i);
                    if (c == '\\' && i + 1 < inner.length()) {
                        char next = inner.charAt(i + 1);
                        if (next == '"') {
                            element.append('"');
                        } else if (next == '\\') {
                            element.append('\\');
                        } else {
                            element.append(c);
                            element.append(next);
                        }
                        i += 2;
                    } else if (c == '"') {
                        i++; // skip closing quote
                        break;
                    } else {
                        element.append(c);
                        i++;
                    }
                }
                result.add(element.toString());
                // Skip comma separator
                while (i < inner.length() && (inner.charAt(i) == ',' || inner.charAt(i) == ' ')) {
                    i++;
                }
            } else {
                // Unquoted element (shouldn't happen with our serializer, but handle gracefully)
                int commaIdx = inner.indexOf(',', i);
                if (commaIdx == -1) {
                    result.add(inner.substring(i).trim());
                    break;
                } else {
                    result.add(inner.substring(i, commaIdx).trim());
                    i = commaIdx + 1;
                }
            }
        }
        return result;
    }

    @NotNull
    static String serializeStringSet(@NotNull Set<String> set) {
        StringBuilder sb = new StringBuilder("[");
        var iterator = set.iterator();
        while (iterator.hasNext()) {
            sb.append("\"").append(iterator.next().replace("\\", "\\\\").replace("\"", "\\\"")).append("\"");
            if (iterator.hasNext()) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}

package com.diamonddagger590.mccore.database.table.impl;

import com.diamonddagger590.mccore.statistic.StatisticEntry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object for player statistic persistence.
 */
public final class PlayerStatisticDAO {

    private PlayerStatisticDAO() {
    }

    /**
     * Gets a single player statistic from the database.
     *
     * @param connection The database connection.
     * @param uuid       The player's UUID.
     * @param key        The statistic key.
     * @return An {@link Optional} containing the entry, or empty if not found.
     */
    @NotNull
    public static Optional<StatisticEntry> getPlayerStatistic(@NotNull Connection connection,
                                                              @NotNull UUID uuid,
                                                              @NotNull NamespacedKey key) {
        String sql = "SELECT value FROM player_statistics WHERE uuid = ? AND statistic_key = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, key.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new StatisticEntry(rs.getObject("value")));
                }
            }
        } catch (SQLException ignored) {
        }
        return Optional.empty();
    }

    /**
     * Gets all statistics for a player from the database.
     *
     * @param connection The database connection.
     * @param uuid       The player's UUID.
     * @return A map of statistic entries keyed by their {@link NamespacedKey}.
     */
    @NotNull
    public static Map<NamespacedKey, StatisticEntry> getAllPlayerStatistics(@NotNull Connection connection,
                                                                          @NotNull UUID uuid) {
        Map<NamespacedKey, StatisticEntry> result = new HashMap<>();
        String sql = "SELECT statistic_key, value FROM player_statistics WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String keyStr = rs.getString("statistic_key");
                    String[] parts = keyStr.split(":");
                    if (parts.length == 2) {
                        @SuppressWarnings("deprecation")
                        NamespacedKey key = new NamespacedKey(parts[0], parts[1]);
                        result.put(key, new StatisticEntry(rs.getObject("value")));
                    }
                }
            }
        } catch (SQLException ignored) {
        }
        return result;
    }

    /**
     * Creates {@link PreparedStatement}s for saving player statistics to the database.
     *
     * @param connection The database connection.
     * @param uuid       The player's UUID.
     * @param entries    The statistic entries to save.
     * @return A list of {@link PreparedStatement}s for batch execution.
     */
    @NotNull
    public static List<PreparedStatement> savePlayerStatistics(@NotNull Connection connection,
                                                               @NotNull UUID uuid,
                                                               @NotNull Map<NamespacedKey, StatisticEntry> entries) {
        List<PreparedStatement> statements = new ArrayList<>();
        String sql = "REPLACE INTO player_statistics (uuid, statistic_key, value) VALUES (?, ?, ?)";
        try {
            for (var entry : entries.entrySet()) {
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, uuid.toString());
                stmt.setString(2, entry.getKey().toString());
                stmt.setString(3, entry.getValue().value().toString());
                statements.add(stmt);
            }
        } catch (SQLException ignored) {
        }
        return statements;
    }
}

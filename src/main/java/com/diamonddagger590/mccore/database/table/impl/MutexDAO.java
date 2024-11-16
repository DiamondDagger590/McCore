package com.diamonddagger590.mccore.database.table.impl;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * A database access object that allows for fetching and saving the mutex state of a player
 */
public class MutexDAO {

    private static final String TABLE_NAME = "player_mutex";
    private static final int CURRENT_TABLE_VERSION = 1;

    /**
     * Attempts to create a new table for this DAO provided that the table does not already exist.
     *
     * @param connection The {@link Connection} to use to attempt the creation
     * @param database   The {@link Database} being used to attempt to create the table
     * @return {@code true} if a new table was made or {@code false} otherwise.
     */
    public static boolean attemptCreateTable(@NotNull Connection connection, @NotNull Database database) {
        //Check to see if the table already exists
        if (database.tableExists(connection, TABLE_NAME)) {
            return false;
        }

        /*****
         ** Table Description:
         ** Contains player data that doesn't have another table to be located
         *
         *
         * uuid is the {@link java.util.UUID} of the player being stored
         * mutex is a boolean which will be true if the mutex is locked
         **
         ** Reasoning for structure:
         ** PK is the `uuid` field, as each player only has one uuid
         *****/
        try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "`" +
                "(" +
                "`uuid` varchar(36) NOT NULL," +
                "`mutex` BIT NOT NULL DEFAULT 0," +
                "PRIMARY KEY (`uuid`)" +
                ");")) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Checks to see if there are any version differences from the live version of this SQL table and then current version.
     * <p>
     * If there are any differences, it will iteratively go through and update through each version to ensure the database is
     * safe to run queries on.
     *
     * @param connection The {@link Connection} that will be used to run the changes
     */
    public static void updateTable(@NotNull Connection connection) {
        int lastStoredVersion = TableVersionHistoryDAO.getLatestVersion(connection, TABLE_NAME);
        if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
            return;
        }

        //Adds table to our tracking
        if (lastStoredVersion == 0) {
            TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
            lastStoredVersion = 1;
        }
    }

    public static boolean isUserMutexLocked(@NotNull Connection connection, @NotNull UUID uuid) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT mutex FROM " + TABLE_NAME + " WHERE uuid = ?;")) {
            preparedStatement.setString(1, uuid.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                boolean result = false;
                while (resultSet.next()) {
                    result = resultSet.getBoolean("mutex"); //TODO actually use mutex
                }
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateUserMutex(@NotNull Connection connection, @NotNull CorePlayer corePlayer) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("REPLACE INTO " + TABLE_NAME + " (uuid, mutex) VALUES(?, ?);")) {
            preparedStatement.setString(1, corePlayer.getUUID().toString());
            preparedStatement.setBoolean(2, corePlayer.isLocked());
            preparedStatement.executeUpdate();
            return corePlayer.isLocked();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateUserMutex(@NotNull Connection connection, @NotNull UUID uuid, boolean locked) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("REPLACE INTO " + TABLE_NAME + " (uuid, mutex) VALUES(?, ?);")) {
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setBoolean(2, locked);
            preparedStatement.executeUpdate();
            return locked;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

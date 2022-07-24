package com.diamonddagger590.mccore.database.table.impl;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.DatabaseManager;
import com.diamonddagger590.mccore.database.builder.DatabaseDriver;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A database access object that allows for fetching and saving the mutex state of a player
 */
public class MutexDAO {

    private static final String TABLE_NAME = "player_mutex";
    private static final int CURRENT_TABLE_VERSION = 1;

    /**
     * Attempts to create a new table for this DAO provided that the table does not already exist.
     *
     * @param connection      The {@link Connection} to use to attempt the creation
     * @param databaseManager The {@link DatabaseManager} being used to attempt to create the table
     * @return A {@link CompletableFuture} containing a {@link Boolean} that is {@code true} if a new table was made,
     * or {@code false} otherwise.
     */
    @NotNull
    public static CompletableFuture<Boolean> attemptCreateTable(@NotNull Connection connection, @NotNull DatabaseManager databaseManager) {

        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            //Check to see if the table already exists
            if (databaseManager.getDatabase().tableExists(TABLE_NAME)) {
                completableFuture.complete(false);
                return;
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
            }
            catch (SQLException e) {
                e.printStackTrace();
                completableFuture.completeExceptionally(e);
            }

            completableFuture.complete(true);
        });

        return completableFuture;
    }

    /**
     * Checks to see if there are any version differences from the live version of this SQL table and then current version.
     * <p>
     * If there are any differences, it will iteratively go through and update through each version to ensure the database is
     * safe to run queries on.
     *
     * @param connection The {@link Connection} that will be used to run the changes
     * @return The {@link  CompletableFuture} that is running these changes.
     */
    @NotNull
    public static CompletableFuture<Void> updateTable(@NotNull Connection connection) {

        DatabaseManager databaseManager = CorePlugin.getInstance().getDatabaseManager();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            TableVersionHistoryDAO.getLatestVersion(connection, TABLE_NAME).thenAccept(lastStoredVersion -> {

                if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
                    completableFuture.complete(null);
                    return;
                }

                //Adds table to our tracking
                if (lastStoredVersion == 0) {
                    TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
                    lastStoredVersion = 1;
                }

            });

            completableFuture.complete(null);
        });

        return completableFuture;
    }

    @NotNull
    public static CompletableFuture<Boolean> isUserMutexLocked(@NotNull Connection connection, @NotNull UUID uuid) {

        DatabaseManager databaseManager = CorePlugin.getInstance().getDatabaseManager();
        CompletableFuture<Boolean> mutexFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT mutex FROM " + TABLE_NAME + " WHERE uuid = ?;")) {

                preparedStatement.setString(1, uuid.toString());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    boolean result = false;
                    while(resultSet.next()) {
                        result = resultSet.getBoolean("mutex"); //TODO actually use mutex
                    }

                    mutexFuture.complete(false);
                }
            }
            catch (SQLException e) {
                mutexFuture.completeExceptionally(e);
            }

        });

        return mutexFuture;
    }

    @NotNull
    public static CompletableFuture<Boolean> updateUserMutex(@NotNull Connection connection, @NotNull CorePlayer corePlayer) {

        DatabaseManager databaseManager = CorePlugin.getInstance().getDatabaseManager();
        DatabaseDriver databaseDriver = databaseManager.getDriver();
        CompletableFuture<Boolean> updatedMutexFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            try (PreparedStatement preparedStatement = databaseDriver == DatabaseDriver.H2 ? connection.prepareStatement("INSERT INTO " + TABLE_NAME + " (uuid, mutex)" +
                                                                                                                             "VALUES(?, ?) ON DUPLICATE KEY UPDATE " +
                                                                                                                             "mutex = VALUES(mutex);")
                                                           : connection.prepareStatement("REPLACE INTO " + TABLE_NAME + " (uuid, mutex) VALUES(?, ?);")) {

                preparedStatement.setString(1, corePlayer.getUUID().toString());
                preparedStatement.setBoolean(2, corePlayer.isLocked());

                preparedStatement.executeUpdate();
                updatedMutexFuture.complete(corePlayer.isLocked());
            }
            catch (SQLException e) {
                updatedMutexFuture.completeExceptionally(e);
            }
        });

        return updatedMutexFuture;
    }


}

package com.diamonddagger590.mccore.task.player;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.MutexDAO;
import com.diamonddagger590.mccore.event.player.PlayerUnloadEvent;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.task.core.CoreTask;
import com.diamonddagger590.mccore.task.core.ExpireableCoreTask;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public abstract class PlayerUnloadTask extends ExpireableCoreTask {

    private final CorePlayer corePlayer;
    private final CompletableFuture<Boolean> result;
    private boolean completed;

    public PlayerUnloadTask(@NotNull CorePlugin plugin, @NotNull CorePlayer corePlayer) {
        super(plugin, 0L, 2, 10L);
        this.corePlayer = corePlayer;
        this.result = new CompletableFuture<>();
        completed = false;
        runTask(true);
    }

    private void runUnloadPlayerTask() {
        Database database = getPlugin().getDatabase();
        try (Connection connection = database.getConnection()) {
            //pause the task to prevent future iterations
            pauseTask();

            if (corePlayer.useMutex()) {
                boolean mutexLocked = MutexDAO.isUserMutexLocked(connection, corePlayer.getUUID());
                // If the mutex isn't locked, resume task to continue ticking
                if (!mutexLocked) {
                    resumeTask();
                    startInterval(); // Start the next interval giving time for the mutex to possibly lock
                    return;
                }

                // We are completing the task one way or another, in this case we want to
                // allow externally cancelling to be treated as a failure but not when we do it here
                completed = true;
                cancelTask();

                // Attempt to load the player, if it works, lock their mutex since we are now using it.
                if (unloadPlayer()) {
                    corePlayer.lock();
                    MutexDAO.updateUserMutex(connection, corePlayer);
                    onPlayerUnloadSuccessfully();
                } else {
                    onPlayerUnloadFail();
                }
            } else {
                // We are completing the task one way or another, in this case we want to
                // allow externally cancelling to be treated as a failure but not when we do it here
                completed = true;
                cancelTask();
                if (unloadPlayer()) {
                    onPlayerUnloadSuccessfully();
                } else {
                    onPlayerUnloadFail();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onIntervalComplete() {
        runUnloadPlayerTask();
    }

    @Override
    protected void onCancel() {
        // If we are cancelling and we didn't complete
        if (!completed) {
            onPlayerUnloadFail();
        }
    }

    /**
     * Attempt to load data for the {@link CorePlayer}
     *
     * @return {@code true} if the data was successfully loaded.
     */
    protected abstract boolean unloadPlayer();

    /**
     * A callback that is called whenever the player data loads successfully.
     */
    protected void onPlayerUnloadSuccessfully() {
        result.complete(true);
        // Throw event on main thread
        Bukkit.getScheduler().scheduleSyncDelayedTask(CorePlugin.getInstance(),
                new CoreTask(getPlugin()) {
                    @Override
                    public void run() {
                        Bukkit.getPluginManager().callEvent(new PlayerUnloadEvent(corePlayer));
                    }
                }
        );
    }

    /**
     * A callback that is called whenever the player data fails to load.
     */
    protected void onPlayerUnloadFail() {
        result.complete(false);
    }

    /**
     * Gets a {@link CompletableFuture} that finishes whenever this task is done.
     *
     * @return A {@link CompletableFuture} that finishes whenever this task is done, containing a result
     * of {@code true} if the player was unloaded successfully.
     */
    public CompletableFuture<Boolean> getResult() {
        return result;
    }

    /**
     * Gets the {@link CorePlayer} that is being loaded.
     *
     * @return The {@link CorePlayer} that is being loaded.
     */
    public CorePlayer getCorePlayer() {
        return corePlayer;
    }

    @NotNull
    @Override
    public CorePlugin getPlugin() {
        return (CorePlugin) super.getPlugin();
    }
}

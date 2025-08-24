package com.diamonddagger590.mccore.task.player;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.MutexDAO;
import com.diamonddagger590.mccore.event.player.PlayerLoadEvent;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.CoreManagerKey;
import com.diamonddagger590.mccore.task.core.CoreTask;
import com.diamonddagger590.mccore.task.core.ExpireableCoreTask;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

/**
 * This task will load the player data
 */
public abstract class PlayerLoadTask extends ExpireableCoreTask {

    private final CorePlayer corePlayer;
    private final CompletableFuture<Boolean> result;
    private boolean completed;

    public PlayerLoadTask(@NotNull CorePlugin plugin, @NotNull CorePlayer corePlayer) {
        super(plugin, 0L, 2, 10L);
        this.corePlayer = corePlayer;
        this.result = new CompletableFuture<>();
        completed = false;
        runTask(true);
    }

    private void runLoadPlayerTask() {
        Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(CoreManagerKey.CORE_DATABASE_MANAGER).getDatabase();
        /*
         If the player is already in the player manager, then that means they logged out then back in.
         We need to check for that and don't load their data until they are removed from the manager.
         */
        if (CorePlugin.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(CoreManagerKey.CORE_PLAYER_MANAGER).getPlayer(corePlayer.getUUID()).isPresent()) {
            resumeTask();
            startInterval();
            return;
        }

        //pause the task to prevent future iterations
        pauseTask();
        try (Connection connection = database.getConnection()) {
            if (corePlayer.useMutex()) {
                boolean mutexLocked = MutexDAO.isUserMutexLocked(connection, corePlayer.getUUID());
                // If the mutex is locked, resume task to continue ticking
                if (mutexLocked) {
                    resumeTask();
                    startInterval(); // Start the next interval giving time for the mutex to possibly unlock
                    return;
                }

                // We are completing the task one way or another, in this case we want to
                // allow externally cancelling to be treated as a failure but not when we do it here
                completed = true;
                // If mutex isn't locked, then cancel task
                cancelTask();

                // Attempt to load the player, if it works, lock their mutex since we are now using it.
                if (loadPlayer()) {
                    corePlayer.lock();
                    MutexDAO.updateUserMutex(connection, corePlayer);
                    onPlayerLoadSuccessfully();
                }
                else {
                    onPlayerLoadFail();
                }
            }
            else {
                completed = true;
                cancelTask();
                if (loadPlayer()) {
                    onPlayerLoadSuccessfully();
                }
                else {
                    onPlayerLoadFail();
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onIntervalComplete() {
        runLoadPlayerTask();
    }

    @Override
    protected void onCancel() {
        // If we are cancelling and we didn't complete
        if (!completed) {
            onPlayerLoadFail();
        }
    }

    /**
     * Attempt to load data for the {@link CorePlayer}
     *
     * @return {@code true} if the data was successfully loaded.
     */
    protected abstract boolean loadPlayer();

    /**
     * A callback that is called whenever the player data loads successfully.
     */
    protected void onPlayerLoadSuccessfully() {
        result.complete(true);
        // Throw event on main thread
        Bukkit.getScheduler().scheduleSyncDelayedTask(CorePlugin.getInstance(),
                new CoreTask(getPlugin()) {
                    @Override
                    public void run() {
                        Bukkit.getPluginManager().callEvent(new PlayerLoadEvent(corePlayer));
                    }
                }
        );
    }

    /**
     * A callback that is called whenever the player data fails to load.
     */
    protected void onPlayerLoadFail() {
        result.complete(false);
    }

    /**
     * Gets a {@link CompletableFuture} that finishes whenever this task is done.
     *
     * @return A {@link CompletableFuture} that finishes whenever this task is done, containing a result
     * of {@code true} if the player was loaded successfully.
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

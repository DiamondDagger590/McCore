package com.diamonddagger590.mccore.task.core;

import com.diamonddagger590.mccore.exception.TaskCompletedException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * A generic task that can be used to more easily create runnables for plugins to use.
 */
public abstract class CoreTask implements Runnable {

    private final Plugin plugin;
    protected int bukkitTaskId = -1;
    protected long taskStartTime = -1;
    protected boolean taskRunningAsync;
    protected boolean taskExecuted;

    public CoreTask(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets the {@link Plugin} that created this task.
     *
     * @return The {@link Plugin} that created this task.
     */
    @NotNull
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Gets the bukkit task id of the {@link org.bukkit.scheduler.BukkitRunnable} this task runs.
     *
     * @return {@code -1} if {@link #hasTaskBeenExecuted()} returns {@code false} or the bukkit
     * task id of the {@link org.bukkit.scheduler.BukkitRunnable} ran by this task.
     */
    public int getBukkitTaskId() {
        return bukkitTaskId;
    }

    /**
     * Checks to see if this task is running async.
     *
     * @return {@code true} if this task is running async.
     */
    public boolean isTaskRunningAsync() {
        return taskRunningAsync;
    }

    /**
     * Checks to see if this task has been executed.
     *
     * @return {@code true} if this task has been executed.
     */
    public boolean hasTaskBeenExecuted() {
        return taskExecuted;
    }

    /**
     * Gets the time in milliseconds that this task started.
     *
     * @return The time in milliseconds that this task started, or {@code -1} if
     * {@link #hasTaskBeenExecuted()} returns {@code false}.
     */
    public long getTaskStartTime() {
        return taskStartTime;
    }

    /**
     * Runs this task synchronously.
     * <p>
     * This method will throw a {@link TaskCompletedException} if called while {@link #hasTaskBeenExecuted()} returns
     * {@code true}.
     */
    public void runTask() {
        runTask(false);
    }

    /**
     * Runs this task either syn or async depending on the provided boolean.
     * <p>
     * This method will throw a {@link TaskCompletedException} if called while {@link #hasTaskBeenExecuted()} returns
     * {@code true}.
     *
     * @param runAsync This should be {@code true} if the task should be ran async.
     */
    public void runTask(boolean runAsync) {

        if (taskExecuted) {
            throw new TaskCompletedException("The task with id: " + bukkitTaskId + " has already been executed.");
        }

        if (runAsync) {
            bukkitTaskId = Bukkit.getScheduler().runTaskAsynchronously(plugin, this).getTaskId();
            taskRunningAsync = true;
        }
        else {
            bukkitTaskId = Bukkit.getScheduler().runTask(plugin, this).getTaskId();
            taskRunningAsync = false;
        }

        taskExecuted = true;
        taskStartTime = System.currentTimeMillis();
    }
}

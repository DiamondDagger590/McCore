package com.diamonddagger590.mccore.task.core;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * This task allows for multiple executions of it (different from {@link RepeatableCoreTask} where
 * the task logic will be repeated inside of one runnable).
 * <p>
 * This should be used carefully, as starting another task will result in no longer being able to access
 * the task id and other information relating to the previously ran tasks.
 */
public abstract class MultiExecutionCoreTask extends CoreTask {

    private int executions;

    public MultiExecutionCoreTask(@NotNull Plugin plugin) {
        super(plugin);
    }

    @Override
    public void runTask(boolean runAsync) {
        taskExecuted = false;
        executions++;
        super.runTask(runAsync);
    }

    /**
     * Gets the bukkit task id of the {@link org.bukkit.scheduler.BukkitRunnable} this task runs.
     * <p>
     * Note that this may return different results if called at different points during runtime due
     * to this task being able to be run multiple times.
     *
     * @return {@code -1} if {@link #hasTaskBeenExecuted()} returns {@code false} or the bukkit
     * task id of the {@link org.bukkit.scheduler.BukkitRunnable} ran by this task.
     */
    public int getBukkitTaskId() {
        return bukkitTaskId;
    }

    /**
     * Gets the time in milliseconds that this task started.
     * <p>
     * Note that this may return different results if called at different points during runtime due
     * to this task being able to be run multiple times.
     *
     * @return The time in milliseconds that this task started, or {@code -1} if
     * {@link #hasTaskBeenExecuted()} returns {@code false}.
     */
    public long getTaskStartTime() {
        return taskStartTime;
    }

    /**
     * Gets the amount of times that this task has been executed.
     *
     * @return The amount of times that this task has been executed.
     */
    public int getExecutions() {
        return executions;
    }
}

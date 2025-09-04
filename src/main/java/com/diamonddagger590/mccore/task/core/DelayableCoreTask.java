package com.diamonddagger590.mccore.task.core;

import com.diamonddagger590.mccore.CorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * This task allows for execution to be delayed before starting.
 */
public abstract class DelayableCoreTask extends CoreTask {

    private final long taskDelay;

    /**
     * @param plugin    The {@link Plugin} initializing the task
     * @param taskDelay The delay in seconds before execution whenever {@link #runTask(boolean)} is called.
     */
    public DelayableCoreTask(@NotNull CorePlugin plugin, long taskDelay) {
        super(plugin);
        this.taskDelay = Math.max(0, taskDelay);
    }

    @Override
    public void runTask(boolean runAsync) {
        if (runAsync) {
            bukkitTaskId = Bukkit.getScheduler().runTaskLaterAsynchronously(getPlugin(), this, (taskDelay * 1000) * 20L).getTaskId();
            taskRunningAsync = true;
        } else {
            bukkitTaskId = Bukkit.getScheduler().runTaskLater(getPlugin(), this, (taskDelay * 1000) * 20L).getTaskId();
            taskRunningAsync = false;
        }

        taskExecuted = true;
        taskStartTime = getPlugin().getTimeProvider().now().toEpochMilli();
    }

    /**
     * Gets the number of ticks to delay execution of the task by whenever {@link #runTask(boolean)} is called.
     *
     * @return The positive, zero inclusive number of ticks to delay execution of the task
     * by whenever {@link #runTask(boolean)} is called.
     */
    public long getTaskDelay() {
        return taskDelay;
    }
}

package com.diamonddagger590.mccore.task.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * This task is a {@link RepeatableCoreTask} that allows for cancellation to occur
 * while the task is running, preventing further iterations from triggering.
 */
public abstract class CancellableCoreTask extends RepeatableCoreTask {

    protected boolean cancelled;

    public CancellableCoreTask(@NotNull Plugin plugin, double taskDelay, double taskFrequency) {
        super(plugin, taskDelay, taskFrequency);
    }

    @Override
    public void run() {

        //If task is cancelled, we aren't going to call the super logic.
        if (cancelled) {
            Bukkit.getScheduler().cancelTask(getBukkitTaskId());
            onCancel();
            return;
        }

        super.run();
    }

    /**
     * Marks this task as cancelled for the next tick of this task to check.
     */
    public void cancelTask() {
        cancelled = true;
    }

    /**
     * Checks to see if this task is cancelled.
     *
     * @return {@code true} if this task is cancelled.
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * A callback method that is called whenever this task is cancelled.
     */
    protected abstract void onCancel();
}

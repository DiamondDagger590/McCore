package com.diamonddagger590.mccore.task.core;

import com.diamonddagger590.mccore.CorePlugin;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

/**
 * This task is a {@link RepeatableCoreTask} that allows for cancellation to occur
 * while the task is running, preventing further iterations from triggering.
 */
public abstract class CancelableCoreTask extends RepeatableCoreTask {

    protected volatile boolean cancelled;

    public CancelableCoreTask(@NotNull CorePlugin plugin, double taskDelay, double taskFrequency) {
        super(plugin, taskDelay, taskFrequency);
    }

    @Override
    public void run() {
        //If task is canceled, we aren't going to call the super logic.
        if (cancelled) {
            Bukkit.getScheduler().cancelTask(getBukkitTaskId());
            onCancel();
            return;
        }

        super.run();
    }

    /**
     * Marks this task as canceled for the next tick of this task to check.
     */
    public void cancelTask() {
        cancelled = true;
    }

    /**
     * Checks to see if this task is canceled.
     *
     * @return {@code true} if this task is canceled.
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * A callback method that is called whenever this task is canceled.
     */
    protected abstract void onCancel();
}

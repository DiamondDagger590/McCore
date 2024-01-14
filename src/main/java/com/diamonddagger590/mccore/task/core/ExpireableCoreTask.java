package com.diamonddagger590.mccore.task.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * This task will be expired automatically on two possible conditions.
 * <p>
 * 1) The task executes a maximum amount of intervals
 * 2) The task's duration has reached a maximum length
 */
public abstract class ExpireableCoreTask extends CancellableCoreTask {

    protected long maxTaskDuration;
    protected int maxIntervals;

    public ExpireableCoreTask(@NotNull Plugin plugin, double taskDelay, double taskFrequency, long maxTaskDurationSeconds) {
        super(plugin, taskDelay, taskFrequency);
        this.maxTaskDuration = System.currentTimeMillis() + (maxTaskDurationSeconds * 1000);
        this.maxIntervals = -1;
    }

    public ExpireableCoreTask(@NotNull Plugin plugin, double taskDelay, double taskFrequency, int maxIntervals) {
        super(plugin, taskDelay, taskFrequency);
        this.maxIntervals = Math.max(1, maxIntervals);
        this.maxTaskDuration = -1;
    }

    public ExpireableCoreTask(@NotNull Plugin plugin, double taskDelay, double taskFrequency, long maxTaskDurationSeconds, int maxIntervals) {
        super(plugin, taskDelay, taskFrequency);
        this.maxTaskDuration = System.currentTimeMillis() + (maxTaskDurationSeconds * 1000);
        this.maxIntervals = Math.max(1, maxIntervals);
    }

    /**
     * Gets the timestamp in milliseconds that this task will automatically expire.
     *
     * @return The timestamp in milliseconds that this task will automatically expire or {@code -1} if this
     * condition isn't being used.
     */
    public long getMaxTaskDuration() {
        return maxTaskDuration;
    }

    /**
     * Gets the maximum amount of intervals that this task can run for before expiring.
     *
     * @return The maximum amount of intervals that this task can run for before expiring or {@code -1}
     * if this condition isn't being used.
     */
    public int getMaxIntervals() {
        return maxIntervals;
    }

    @Override
    public void run() {

        //Expire task before passing it back up to check for cancellation state
        long currentTime = System.currentTimeMillis();
        if (maxTaskDuration != -1 && currentTime >= maxTaskDuration) {
            expireTask();
            return;
        }

        super.run();
    }

    /**
     * Expires this task and calls the callback {@link #onTaskExpire()}.
     */
    private void expireTask() {
        Bukkit.getScheduler().cancelTask(getBukkitTaskId());
        onTaskExpire();
    }

    @Override
    protected void startInterval() {
        intervalStartTime = System.currentTimeMillis();
        currentInterval++;

        if (maxIntervals != -1 && currentInterval >= maxIntervals) {
            expireTask();
            return;
        }

        onIntervalStart();
    }

    /**
     * A callback that is called whenever this task expires.
     */
    protected abstract void onTaskExpire();
}

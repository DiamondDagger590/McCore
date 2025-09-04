package com.diamonddagger590.mccore.task.core;

import com.diamonddagger590.mccore.CorePlugin;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

/**
 * This task is a type of multiple execution task where it will run forever
 * until the server is shut down. If behavior of only wanting a task to repeat
 * but eventually expire is desired, please see {@link ExpireableCoreTask}.
 * <p>
 * This task type is driven by the concept of "intervals". Intervals
 * happen every time that this task is able to have a valid execution. There
 * are multiple supporting methods inheritors can override to do specific
 * behavior depending on what states they want to support/handle.
 */
public abstract class RepeatableCoreTask extends MultiExecutionCoreTask {

    protected final double taskDelay;
    protected final double taskFrequency;
    protected int currentInterval = 0;
    protected boolean delayExpired;
    protected long intervalStartTime;
    protected boolean paused;

    public RepeatableCoreTask(@NotNull CorePlugin plugin, double taskDelay, double taskFrequency) {
        super(plugin);
        this.taskDelay = taskDelay;
        this.taskFrequency = taskFrequency;
    }

    @Override
    public void runTask(boolean runAsync) {
        if (runAsync) {
            bukkitTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(getPlugin(), this, 0, 1).getTaskId();
            taskRunningAsync = true;
        } else {
            bukkitTaskId = Bukkit.getScheduler().runTaskTimer(getPlugin(), this, 0, 1).getTaskId();
            taskRunningAsync = false;
        }

        taskExecuted = true;
        taskStartTime = getPlugin().getTimeProvider().now().toEpochMilli();
    }

    @Override
    public void run() {
        if (paused) {
            return;
        }

        long currentTime = getPlugin().getTimeProvider().now().toEpochMilli();
        if (!delayExpired) {
            if (currentTime >= taskStartTime + (taskDelay * 1000)) {
                delayExpired = true;
                onDelayComplete();
                startInterval();
            }
            return;
        }

        if (currentTime >= intervalStartTime + (taskFrequency * 1000)) {
            onIntervalComplete();
            startInterval();
        }
    }

    /**
     * Start a new interval for this task.
     */
    protected void startInterval() {
        intervalStartTime = getPlugin().getTimeProvider().now().toEpochMilli();
        currentInterval++;
        onIntervalStart();
    }

    /**
     * Gets the delay in seconds for this task before the first interval can start.
     *
     * @return The delay in seconds for this task before the first interval can start.
     */
    public double getTaskDelay() {
        return taskDelay;
    }

    /**
     * Gets the frequency of intervals for this task in seconds.
     *
     * @return The frequency of intervals for this task in seconds.
     */
    public double getTaskFrequency() {
        return taskFrequency;
    }

    /**
     * Gets the current interval that this task is on.
     *
     * @return The current interval that this task is on.
     */
    public int getCurrentInterval() {
        return currentInterval;
    }

    /**
     * Checks to see if the delay for this task has expired
     *
     * @return {@code true} if the delay for this task has expired.
     */
    public boolean hasDelayExpired() {
        return delayExpired;
    }

    /**
     * Pause this task and prevent any intervals from occurring until
     * {@link #resumeTask()} is called.
     */
    public void pauseTask() {
        if (!paused) {
            paused = true;
            onIntervalPause();
        }
    }

    /**
     * Resumes this task and allows intervals to start occurring again.
     */
    public void resumeTask() {
        if (paused) {
            paused = false;
            onIntervalResume();
        }
    }

    /**
     * Toggles the current paused state of this task. Meaning if the
     * task is paused, it will be unpaused. If it is unpaused then it will be
     * paused.
     *
     * @return The updated paused state of this task.
     */
    public boolean togglePause() {
        if (paused) {
            resumeTask();
        } else {
            pauseTask();
        }
        return paused;
    }

    /**
     * Checks to see if this task is currently paused or not.
     *
     * @return {@code true} of this task is currently paused.
     */
    public boolean isTaskPaused() {
        return paused;
    }

    /**
     * This method fires whenever the delay is completed. It is expected for this code
     * to fire once and only once and after that {@link #onIntervalComplete()}s will start
     * being called.
     * <p>
     * PLEASE NOTE: this method should not be treated the same as {@link #onIntervalComplete()}.
     */
    protected abstract void onDelayComplete();

    /**
     * This method fires whenever a new interval starts. This is called after {@link #onDelayComplete()}
     * or {@link #onIntervalComplete()}.
     */
    protected abstract void onIntervalStart();

    /**
     * This method is called whenever a full interval is completed. Think of this as the code
     * inside a typical Bukkit runnable.
     */
    protected abstract void onIntervalComplete();

    /**
     * This method is fired whenever this task is set to be paused.
     */
    protected abstract void onIntervalPause();

    /**
     * This method is fired whenever this task is set to be resumed.
     */
    protected abstract void onIntervalResume();

}

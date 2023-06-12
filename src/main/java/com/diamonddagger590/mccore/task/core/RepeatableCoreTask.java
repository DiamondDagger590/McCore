package com.diamonddagger590.mccore.task.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public abstract class RepeatableCoreTask extends MultiExecutionCoreTask {

    protected final double taskDelay;
    protected final double taskFrequency;
    protected int currentInterval = 0;
    protected boolean delayExpired;
    protected long intervalStartTime;
    protected boolean paused;

    public RepeatableCoreTask(@NotNull Plugin plugin, double taskDelay, double taskFrequency) {
        super(plugin);
        this.taskDelay = taskDelay;
        this.taskFrequency = taskFrequency;
    }

    @Override
    public void runTask(boolean runAsync) {

        if(runAsync){
            bukkitTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(getPlugin(), this, 0, 1).getTaskId();
            taskRunningAsync = true;
        }
        else{
            bukkitTaskId = Bukkit.getScheduler().runTaskTimer(getPlugin(), this, 0, 1).getTaskId();
            taskRunningAsync = false;
        }

        taskExecuted = true;
        taskStartTime = System.currentTimeMillis();
    }

    @Override
    public void run() {

        if (paused) {
            return;
        }

        long currentTime = System.currentTimeMillis();

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

    protected void startInterval() {
        intervalStartTime = System.currentTimeMillis();
        currentInterval++;
        onIntervalStart();
    }

    public double getTaskDelay() {
        return taskDelay;
    }

    public double getTaskFrequency() {
        return taskFrequency;
    }

    public int getCurrentInterval(){
        return currentInterval;
    }

    public boolean hasDelayExpired(){
        return delayExpired;
    }

    public void pauseTask() {
        paused = true;
        onIntervalPause();
    }

    public void resumeTask() {
        paused = false;
        onIntervalResume();
    }

    public boolean togglePause() {

        if (paused) {
            resumeTask();
        }
        else {
            pauseTask();
        }

        return paused;
    }

    public boolean isTaskPaused() {
        return paused;
    }

    protected abstract void onDelayComplete();

    protected abstract void onIntervalStart();

    protected abstract void onIntervalComplete();

    protected abstract void onIntervalPause();

    protected abstract void onIntervalResume();

}

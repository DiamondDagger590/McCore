package com.diamonddagger590.mccore.task.core;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.util.TimeProvider;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RepeatableCoreTaskTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-06-15T12:00:00Z");
    private static final int TASK_ID = 55;

    @Mock
    private CorePlugin mockPlugin;
    @Mock
    private BukkitScheduler mockScheduler;
    @Mock
    private BukkitTask mockBukkitTask;

    private MockedStatic<Bukkit> mockedBukkit;
    private AutoCloseable mocks;
    private Clock testClock;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        testClock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        when(mockPlugin.getTimeProvider()).thenReturn(new TimeProvider(testClock));
        when(mockBukkitTask.getTaskId()).thenReturn(TASK_ID);

        mockedBukkit = mockStatic(Bukkit.class);
        mockedBukkit.when(Bukkit::getScheduler).thenReturn(mockScheduler);

        when(mockScheduler.runTaskTimer(any(), any(Runnable.class), eq(0L), eq(1L))).thenReturn(mockBukkitTask);
        when(mockScheduler.runTaskTimerAsynchronously(any(), any(Runnable.class), eq(0L), eq(1L))).thenReturn(mockBukkitTask);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedBukkit.close();
        mocks.close();
    }

    private void advanceClock(Duration offset) {
        testClock = Clock.offset(testClock, offset);
        when(mockPlugin.getTimeProvider()).thenReturn(new TimeProvider(testClock));
    }

    private TestRepeatableTask createTask(double delay, double frequency) {
        return new TestRepeatableTask(mockPlugin, delay, frequency);
    }

    @Test
    @DisplayName("Given a new repeatable task, when checking initial state, then defaults are correct")
    void newTask_hasCorrectDefaults() {
        TestRepeatableTask task = createTask(2.0, 5.0);

        assertEquals(2.0, task.getTaskDelay());
        assertEquals(5.0, task.getTaskFrequency());
        assertEquals(0, task.getCurrentInterval());
        assertFalse(task.hasDelayExpired());
        assertFalse(task.isTaskPaused());
    }

    @Test
    @DisplayName("Given a repeatable task, when running sync, then uses runTaskTimer")
    void runTask_sync_usesRunTaskTimer() {
        TestRepeatableTask task = createTask(1.0, 1.0);

        task.runTask(false);

        verify(mockScheduler).runTaskTimer(eq(mockPlugin), eq(task), eq(0L), eq(1L));
        assertEquals(TASK_ID, task.getBukkitTaskId());
        assertFalse(task.isTaskRunningAsync());
        assertTrue(task.hasTaskBeenExecuted());
    }

    @Test
    @DisplayName("Given a repeatable task, when running async, then uses runTaskTimerAsynchronously")
    void runTask_async_usesRunTaskTimerAsync() {
        TestRepeatableTask task = createTask(1.0, 1.0);

        task.runTask(true);

        verify(mockScheduler).runTaskTimerAsynchronously(eq(mockPlugin), eq(task), eq(0L), eq(1L));
        assertTrue(task.isTaskRunningAsync());
    }

    @Test
    @DisplayName("Given a started task with delay, when run() called before delay expires, then no interval starts")
    void run_beforeDelayExpires_noIntervalStarts() {
        TestRepeatableTask task = createTask(5.0, 1.0);
        task.runTask(false);

        advanceClock(Duration.ofSeconds(3));
        task.run();

        assertFalse(task.hasDelayExpired());
        assertEquals(0, task.getCurrentInterval());
        assertTrue(task.delayCompleteCalls.isEmpty());
    }

    @Test
    @DisplayName("Given a started task, when run() called after delay expires, then delay completes and first interval starts")
    void run_afterDelayExpires_delayCompletesAndIntervalStarts() {
        TestRepeatableTask task = createTask(2.0, 5.0);
        task.runTask(false);

        advanceClock(Duration.ofSeconds(3));
        task.run();

        assertTrue(task.hasDelayExpired());
        assertEquals(1, task.delayCompleteCalls.size());
        assertEquals(1, task.intervalStartCalls.size());
        assertEquals(1, task.getCurrentInterval());
    }

    @Test
    @DisplayName("Given a task past delay, when run() before interval frequency, then no interval completes")
    void run_beforeIntervalFrequency_noIntervalCompletes() {
        TestRepeatableTask task = createTask(0.0, 5.0);
        task.runTask(false);

        // Delay is 0 so first run() should complete delay and start interval 1
        task.run();
        assertTrue(task.hasDelayExpired());
        assertEquals(1, task.getCurrentInterval());

        // Run again before 5 seconds have passed
        advanceClock(Duration.ofSeconds(3));
        task.run();

        assertEquals(0, task.intervalCompleteCalls.size());
        assertEquals(1, task.getCurrentInterval());
    }

    @Test
    @DisplayName("Given a task past delay, when run() after interval frequency, then interval completes and new one starts")
    void run_afterIntervalFrequency_intervalCompletesAndNewStarts() {
        TestRepeatableTask task = createTask(0.0, 2.0);
        task.runTask(false);

        // Complete delay
        task.run();
        assertEquals(1, task.getCurrentInterval());

        // Advance past frequency
        advanceClock(Duration.ofSeconds(3));
        task.run();

        assertEquals(1, task.intervalCompleteCalls.size());
        assertEquals(2, task.getCurrentInterval());
    }

    @Test
    @DisplayName("Given a task, when paused, then run() does nothing")
    void run_whenPaused_doesNothing() {
        TestRepeatableTask task = createTask(0.0, 1.0);
        task.runTask(false);

        task.pauseTask();
        assertTrue(task.isTaskPaused());

        advanceClock(Duration.ofSeconds(10));
        task.run();

        assertFalse(task.hasDelayExpired());
        assertEquals(0, task.getCurrentInterval());
    }

    @Test
    @DisplayName("Given a paused task, when resumed, then run() processes normally")
    void run_afterResume_processesNormally() {
        TestRepeatableTask task = createTask(0.0, 1.0);
        task.runTask(false);

        task.pauseTask();
        task.resumeTask();
        assertFalse(task.isTaskPaused());

        task.run();

        assertTrue(task.hasDelayExpired());
        assertEquals(1, task.getCurrentInterval());
    }

    @Test
    @DisplayName("Given a task, when pauseTask called twice, then onIntervalPause called only once")
    void pauseTask_calledTwice_onlyPausesOnce() {
        TestRepeatableTask task = createTask(1.0, 1.0);

        task.pauseTask();
        task.pauseTask();

        assertEquals(1, task.pauseCalls.size());
    }

    @Test
    @DisplayName("Given a task, when resumeTask called without pausing, then no-op")
    void resumeTask_withoutPause_noOp() {
        TestRepeatableTask task = createTask(1.0, 1.0);

        task.resumeTask();

        assertTrue(task.resumeCalls.isEmpty());
    }

    @Test
    @DisplayName("Given a paused task, when resumeTask called twice, then onIntervalResume called only once")
    void resumeTask_calledTwice_onlyResumesOnce() {
        TestRepeatableTask task = createTask(1.0, 1.0);
        task.pauseTask();

        task.resumeTask();
        task.resumeTask();

        assertEquals(1, task.resumeCalls.size());
    }

    @Test
    @DisplayName("Given a running task, when togglePause, then task becomes paused and returns true")
    void togglePause_fromRunning_pausesAndReturnsTrue() {
        TestRepeatableTask task = createTask(1.0, 1.0);

        boolean result = task.togglePause();

        assertTrue(result);
        assertTrue(task.isTaskPaused());
    }

    @Test
    @DisplayName("Given a paused task, when togglePause, then task resumes and returns false")
    void togglePause_fromPaused_resumesAndReturnsFalse() {
        TestRepeatableTask task = createTask(1.0, 1.0);
        task.pauseTask();

        boolean result = task.togglePause();

        assertFalse(result);
        assertFalse(task.isTaskPaused());
    }

    static class TestRepeatableTask extends RepeatableCoreTask {

        final List<Object> delayCompleteCalls = new ArrayList<>();
        final List<Object> intervalStartCalls = new ArrayList<>();
        final List<Object> intervalCompleteCalls = new ArrayList<>();
        final List<Object> pauseCalls = new ArrayList<>();
        final List<Object> resumeCalls = new ArrayList<>();

        TestRepeatableTask(CorePlugin plugin, double taskDelay, double taskFrequency) {
            super(plugin, taskDelay, taskFrequency);
        }

        @Override
        protected void onDelayComplete() {
            delayCompleteCalls.add(new Object());
        }

        @Override
        protected void onIntervalStart() {
            intervalStartCalls.add(new Object());
        }

        @Override
        protected void onIntervalComplete() {
            intervalCompleteCalls.add(new Object());
        }

        @Override
        protected void onIntervalPause() {
            pauseCalls.add(new Object());
        }

        @Override
        protected void onIntervalResume() {
            resumeCalls.add(new Object());
        }
    }
}

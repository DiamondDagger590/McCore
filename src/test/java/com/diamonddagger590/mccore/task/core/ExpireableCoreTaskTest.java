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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExpireableCoreTaskTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-06-15T12:00:00Z");
    private static final long FIXED_MILLIS = FIXED_INSTANT.toEpochMilli();
    private static final int TASK_ID = 88;

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

    // --- Constructor tests ---

    @Test
    @DisplayName("Given duration-only constructor (2-param), then maxTaskDuration is set and maxIntervals is -1")
    void constructor_durationOnly_setsMaxDurationAndDefaultIntervals() {
        // Constructor: (plugin, taskDelay=2.0, maxDurationSec=10)
        // frequency defaults to 1
        TestExpireableTask task = new TestExpireableTask(mockPlugin, 2.0, 10L);

        // maxTaskDuration = now + (10 * 1000) + (2.0 * 1000) = FIXED_MILLIS + 12000
        assertEquals(FIXED_MILLIS + 12000, task.getMaxTaskDuration());
        assertEquals(-1, task.getMaxIntervals());
    }

    @Test
    @DisplayName("Given duration+frequency constructor (3-param with long), then maxTaskDuration includes delay and duration")
    void constructor_durationWithFrequency_setsMaxDuration() {
        TestExpireableTask task = new TestExpireableTask(mockPlugin, 3.0, 2.0, 15L);

        // maxTaskDuration = now + (15 * 1000) + (3.0 * 1000) = FIXED_MILLIS + 18000
        assertEquals(FIXED_MILLIS + 18000, task.getMaxTaskDuration());
        assertEquals(-1, task.getMaxIntervals());
        assertEquals(2.0, task.getTaskFrequency());
    }

    @Test
    @DisplayName("Given interval-only constructor (3-param with int), then maxIntervals is set and maxTaskDuration is -1")
    void constructor_intervalsOnly_setsMaxIntervalsAndNoMaxDuration() {
        TestExpireableTask task = new TestExpireableTask(mockPlugin, 1.0, 1.0, 5);

        assertEquals(-1, task.getMaxTaskDuration());
        assertEquals(5, task.getMaxIntervals());
    }

    @Test
    @DisplayName("Given interval constructor with value below 1, then maxIntervals is clamped to 1")
    void constructor_intervalsClampedToOne() {
        TestExpireableTask task = new TestExpireableTask(mockPlugin, 1.0, 1.0, 0);
        assertEquals(1, task.getMaxIntervals());

        TestExpireableTask task2 = new TestExpireableTask(mockPlugin, 1.0, 1.0, -5);
        assertEquals(1, task2.getMaxIntervals());
    }

    @Test
    @DisplayName("Given both duration and intervals constructor, then both limits are set")
    void constructor_bothLimits_setsBothValues() {
        TestExpireableTask task = new TestExpireableTask(mockPlugin, 1.0, 2.0, 20L, 10);

        assertEquals(FIXED_MILLIS + 21000, task.getMaxTaskDuration());
        assertEquals(10, task.getMaxIntervals());
    }

    // --- Duration expiry tests ---

    @Test
    @DisplayName("Given a duration-limited task, when run() called before expiry, then parent logic runs")
    void run_beforeDurationExpiry_runsNormally() {
        TestExpireableTask task = new TestExpireableTask(mockPlugin, 0.0, 1.0, 10L);
        task.runTask(false);

        advanceClock(Duration.ofSeconds(5));
        task.run();

        assertTrue(task.hasDelayExpired());
        assertTrue(task.expireCalls.isEmpty());
    }

    @Test
    @DisplayName("Given a duration-limited task, when run() called at expiry time, then task expires")
    void run_atDurationExpiry_expiresTask() {
        TestExpireableTask task = new TestExpireableTask(mockPlugin, 0.0, 1.0, 10L);
        task.runTask(false);

        // maxTaskDuration = FIXED_MILLIS + 10000 + 0 = FIXED_MILLIS + 10000
        advanceClock(Duration.ofSeconds(10));
        task.run();

        assertEquals(1, task.expireCalls.size());
        verify(mockScheduler).cancelTask(TASK_ID);
    }

    @Test
    @DisplayName("Given a duration-limited task, when run() called past expiry, then task expires")
    void run_pastDurationExpiry_expiresTask() {
        TestExpireableTask task = new TestExpireableTask(mockPlugin, 2.0, 1.0, 5L);
        task.runTask(false);

        // maxTaskDuration = FIXED_MILLIS + 5000 + 2000 = FIXED_MILLIS + 7000
        advanceClock(Duration.ofSeconds(8));
        task.run();

        assertEquals(1, task.expireCalls.size());
    }

    @Test
    @DisplayName("Given a duration-limited task that expires, then onCancel is not called (only onTaskExpire)")
    void run_durationExpiry_doesNotCallOnCancel() {
        TestExpireableTask task = new TestExpireableTask(mockPlugin, 0.0, 1.0, 5L);
        task.runTask(false);

        advanceClock(Duration.ofSeconds(6));
        task.run();

        assertEquals(1, task.expireCalls.size());
        assertTrue(task.cancelCalls.isEmpty());
    }

    // --- Interval expiry tests ---

    @Test
    @DisplayName("Given an interval-limited task with maxIntervals=1, then task expires after first interval")
    void startInterval_maxIntervals1_expiresAfterFirstInterval() {
        TestExpireableTask task = new TestExpireableTask(mockPlugin, 0.0, 1.0, 1);
        task.runTask(false);

        // First run: delay completes, startInterval called -> currentInterval becomes 1 >= maxIntervals(1) -> expire
        task.run();

        assertEquals(1, task.expireCalls.size());
        assertEquals(1, task.getCurrentInterval());
        assertTrue(task.intervalStartCalls.isEmpty());
    }

    @Test
    @DisplayName("Given an interval-limited task with maxIntervals=3, then task expires after third interval start")
    void startInterval_maxIntervals3_expiresAfterThirdInterval() {
        TestExpireableTask task = new TestExpireableTask(mockPlugin, 0.0, 1.0, 3);
        task.runTask(false);

        // Delay completes, interval 1 starts
        task.run();
        assertEquals(1, task.getCurrentInterval());
        assertEquals(1, task.intervalStartCalls.size());

        // Interval 1 completes, interval 2 starts
        advanceClock(Duration.ofSeconds(2));
        task.run();
        assertEquals(2, task.getCurrentInterval());
        assertEquals(2, task.intervalStartCalls.size());

        // Interval 2 completes, interval 3 would start but hits max -> expire
        advanceClock(Duration.ofSeconds(2));
        task.run();
        assertEquals(3, task.getCurrentInterval());
        assertEquals(1, task.expireCalls.size());
        // onIntervalStart not called for the expiring interval
        assertEquals(2, task.intervalStartCalls.size());
    }

    // --- Both limits tests ---

    @Test
    @DisplayName("Given both limits, when duration expires first, then task expires by duration")
    void run_durationExpiresFirst_expiresByDuration() {
        // Duration: 5 seconds, Intervals: 100 (won't be reached)
        TestExpireableTask task = new TestExpireableTask(mockPlugin, 0.0, 2.0, 5L, 100);
        task.runTask(false);

        advanceClock(Duration.ofSeconds(6));
        task.run();

        assertEquals(1, task.expireCalls.size());
        assertEquals(0, task.getCurrentInterval());
    }

    @Test
    @DisplayName("Given both limits, when intervals expire first, then task expires by intervals")
    void run_intervalsExpireFirst_expiresByIntervals() {
        // Duration: 100 seconds (won't be reached), Intervals: 1
        TestExpireableTask task = new TestExpireableTask(mockPlugin, 0.0, 0.5, 100L, 1);
        task.runTask(false);

        task.run();

        assertEquals(1, task.expireCalls.size());
        assertEquals(1, task.getCurrentInterval());
    }

    // --- No duration limit test ---

    @Test
    @DisplayName("Given interval-only limited task, when time passes extensively, then task doesn't expire by duration")
    void run_noMaxDuration_doesNotExpireByTime() {
        TestExpireableTask task = new TestExpireableTask(mockPlugin, 0.0, 1.0, 5);
        task.runTask(false);

        // Even after large time advance, should not expire by duration since maxTaskDuration == -1
        advanceClock(Duration.ofDays(365));
        task.run();

        // Should have delay completed and started interval, not expired by duration
        assertTrue(task.hasDelayExpired());
    }

    static class TestExpireableTask extends ExpireableCoreTask {

        final List<Object> expireCalls = new ArrayList<>();
        final List<Object> cancelCalls = new ArrayList<>();
        final List<Object> delayCompleteCalls = new ArrayList<>();
        final List<Object> intervalStartCalls = new ArrayList<>();
        final List<Object> intervalCompleteCalls = new ArrayList<>();

        TestExpireableTask(CorePlugin plugin, double taskDelay, long maxDurationSeconds) {
            super(plugin, taskDelay, maxDurationSeconds);
        }

        TestExpireableTask(CorePlugin plugin, double taskDelay, double taskFrequency, long maxDurationSeconds) {
            super(plugin, taskDelay, taskFrequency, maxDurationSeconds);
        }

        TestExpireableTask(CorePlugin plugin, double taskDelay, double taskFrequency, int maxIntervals) {
            super(plugin, taskDelay, taskFrequency, maxIntervals);
        }

        TestExpireableTask(CorePlugin plugin, double taskDelay, double taskFrequency, long maxDurationSeconds, int maxIntervals) {
            super(plugin, taskDelay, taskFrequency, maxDurationSeconds, maxIntervals);
        }

        @Override
        protected void onTaskExpire() {
            expireCalls.add(new Object());
        }

        @Override
        protected void onCancel() {
            cancelCalls.add(new Object());
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
        }

        @Override
        protected void onIntervalResume() {
        }
    }
}

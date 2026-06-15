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

class CancelableCoreTaskTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-06-15T12:00:00Z");
    private static final int TASK_ID = 77;

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

    private TestCancelableTask createTask(double delay, double frequency) {
        return new TestCancelableTask(mockPlugin, delay, frequency);
    }

    @Test
    @DisplayName("Given a new cancelable task, when checking initial state, then is not cancelled")
    void newTask_isNotCancelled() {
        TestCancelableTask task = createTask(1.0, 1.0);
        assertFalse(task.isCancelled());
    }

    @Test
    @DisplayName("Given a cancelable task, when cancelTask called, then isCancelled returns true")
    void cancelTask_setsFlag() {
        TestCancelableTask task = createTask(1.0, 1.0);

        task.cancelTask();

        assertTrue(task.isCancelled());
    }

    @Test
    @DisplayName("Given a cancelled task, when run() called, then Bukkit scheduler cancels the task and onCancel fires")
    void run_whenCancelled_cancelsAndCallsOnCancel() {
        TestCancelableTask task = createTask(0.0, 1.0);
        task.runTask(false);

        task.cancelTask();
        task.run();

        verify(mockScheduler).cancelTask(TASK_ID);
        assertEquals(1, task.cancelCalls.size());
    }

    @Test
    @DisplayName("Given a cancelled task, when run() called, then parent run() logic is skipped")
    void run_whenCancelled_skipsParentLogic() {
        TestCancelableTask task = createTask(0.0, 1.0);
        task.runTask(false);

        task.cancelTask();

        advanceClock(Duration.ofSeconds(10));
        task.run();

        assertFalse(task.hasDelayExpired());
        assertEquals(0, task.getCurrentInterval());
    }

    @Test
    @DisplayName("Given a non-cancelled task, when run() called, then parent logic executes normally")
    void run_whenNotCancelled_delegatesToParent() {
        TestCancelableTask task = createTask(0.0, 1.0);
        task.runTask(false);

        task.run();

        assertTrue(task.hasDelayExpired());
        assertEquals(1, task.getCurrentInterval());
        assertTrue(task.cancelCalls.isEmpty());
    }

    static class TestCancelableTask extends CancelableCoreTask {

        final List<Object> cancelCalls = new ArrayList<>();
        final List<Object> delayCompleteCalls = new ArrayList<>();
        final List<Object> intervalStartCalls = new ArrayList<>();
        final List<Object> intervalCompleteCalls = new ArrayList<>();

        TestCancelableTask(CorePlugin plugin, double taskDelay, double taskFrequency) {
            super(plugin, taskDelay, taskFrequency);
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

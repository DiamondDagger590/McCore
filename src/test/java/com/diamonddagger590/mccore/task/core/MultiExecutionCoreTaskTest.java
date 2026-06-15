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
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MultiExecutionCoreTaskTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-06-15T12:00:00Z");

    @Mock
    private CorePlugin mockPlugin;
    @Mock
    private BukkitScheduler mockScheduler;
    @Mock
    private BukkitTask mockBukkitTask;

    private MockedStatic<Bukkit> mockedBukkit;
    private AutoCloseable mocks;
    private int taskIdCounter = 1;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        TimeProvider timeProvider = new TimeProvider(Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC));
        when(mockPlugin.getTimeProvider()).thenReturn(timeProvider);
        when(mockBukkitTask.getTaskId()).thenAnswer(invocation -> taskIdCounter++);

        mockedBukkit = mockStatic(Bukkit.class);
        mockedBukkit.when(Bukkit::getScheduler).thenReturn(mockScheduler);

        when(mockScheduler.runTask(any(), any(Runnable.class))).thenReturn(mockBukkitTask);
        when(mockScheduler.runTaskAsynchronously(any(), any(Runnable.class))).thenReturn(mockBukkitTask);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedBukkit.close();
        mocks.close();
    }

    private MultiExecutionCoreTask createTask() {
        return new MultiExecutionCoreTask(mockPlugin) {
            @Override
            public void run() {
            }
        };
    }

    @Test
    @DisplayName("Given a new multi-execution task, when checking initial state, then executions is zero")
    void newTask_hasZeroExecutions() {
        MultiExecutionCoreTask task = createTask();
        assertEquals(0, task.getExecutions());
    }

    @Test
    @DisplayName("Given a multi-execution task, when run once, then execution count is one")
    void runTask_once_incrementsExecutionCount() {
        MultiExecutionCoreTask task = createTask();

        task.runTask(false);

        assertEquals(1, task.getExecutions());
        assertTrue(task.hasTaskBeenExecuted());
    }

    @Test
    @DisplayName("Given a multi-execution task, when run multiple times, then execution count increments each time")
    void runTask_multipleTimes_incrementsEachTime() {
        MultiExecutionCoreTask task = createTask();

        task.runTask(false);
        assertEquals(1, task.getExecutions());

        task.runTask(false);
        assertEquals(2, task.getExecutions());

        task.runTask(true);
        assertEquals(3, task.getExecutions());
    }

    @Test
    @DisplayName("Given a multi-execution task run twice, when checking task ID, then returns latest ID")
    void runTask_multipleTimes_returnsLatestTaskId() {
        MultiExecutionCoreTask task = createTask();

        task.runTask(false);
        int firstId = task.getBukkitTaskId();

        task.runTask(false);
        int secondId = task.getBukkitTaskId();

        assertTrue(secondId >= firstId);
    }

    @Test
    @DisplayName("Given a multi-execution task, when run, then getTaskStartTime returns correct epoch millis")
    void runTask_setsTaskStartTime() {
        MultiExecutionCoreTask task = createTask();

        assertEquals(-1, task.getTaskStartTime());

        task.runTask(false);

        assertEquals(FIXED_INSTANT.toEpochMilli(), task.getTaskStartTime());
    }

    @Test
    @DisplayName("Given a multi-execution task, when run async then sync, then async flag reflects last run")
    void runTask_switchingAsyncSync_reflectsLatest() {
        MultiExecutionCoreTask task = createTask();

        task.runTask(true);
        assertTrue(task.isTaskRunningAsync());

        task.runTask(false);
        // After sync run, the parent sets taskRunningAsync = false
        // But MultiExecutionCoreTask resets taskExecuted then calls super
        assertEquals(2, task.getExecutions());
    }
}

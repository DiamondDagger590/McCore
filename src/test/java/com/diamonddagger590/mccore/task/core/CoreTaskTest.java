package com.diamonddagger590.mccore.task.core;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.exception.TaskCompletedException;
import com.diamonddagger590.mccore.util.TimeProvider;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CoreTaskTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-06-15T12:00:00Z");
    private static final int TASK_ID = 42;

    @Mock
    private CorePlugin mockPlugin;
    @Mock
    private BukkitScheduler mockScheduler;
    @Mock
    private BukkitTask mockBukkitTask;

    private MockedStatic<Bukkit> mockedBukkit;
    private AutoCloseable mocks;
    private TimeProvider timeProvider;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        timeProvider = new TimeProvider(Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC));
        when(mockPlugin.getTimeProvider()).thenReturn(timeProvider);
        when(mockBukkitTask.getTaskId()).thenReturn(TASK_ID);

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

    private CoreTask createTask() {
        return new CoreTask(mockPlugin) {
            @Override
            public void run() {
            }
        };
    }

    @Test
    @DisplayName("Given a new task, when checking initial state, then defaults are set correctly")
    void newTask_hasCorrectDefaults() {
        CoreTask task = createTask();

        assertEquals(mockPlugin, task.getPlugin());
        assertEquals(-1, task.getBukkitTaskId());
        assertEquals(-1, task.getTaskStartTime());
        assertFalse(task.isTaskRunningAsync());
        assertFalse(task.hasTaskBeenExecuted());
    }

    @Test
    @DisplayName("Given a new task, when running synchronously via runTask(), then task is scheduled sync")
    void runTask_noArg_schedulesSync() {
        CoreTask task = createTask();

        task.runTask();

        verify(mockScheduler).runTask(eq(mockPlugin), eq(task));
        assertEquals(TASK_ID, task.getBukkitTaskId());
        assertFalse(task.isTaskRunningAsync());
        assertTrue(task.hasTaskBeenExecuted());
        assertEquals(FIXED_INSTANT.toEpochMilli(), task.getTaskStartTime());
    }

    @Test
    @DisplayName("Given a new task, when running synchronously, then task is scheduled via runTask on scheduler")
    void runTask_sync_schedulesSync() {
        CoreTask task = createTask();

        task.runTask(false);

        verify(mockScheduler).runTask(eq(mockPlugin), eq(task));
        assertEquals(TASK_ID, task.getBukkitTaskId());
        assertFalse(task.isTaskRunningAsync());
        assertTrue(task.hasTaskBeenExecuted());
        assertEquals(FIXED_INSTANT.toEpochMilli(), task.getTaskStartTime());
    }

    @Test
    @DisplayName("Given a new task, when running asynchronously, then task is scheduled async")
    void runTask_async_schedulesAsync() {
        CoreTask task = createTask();

        task.runTask(true);

        verify(mockScheduler).runTaskAsynchronously(eq(mockPlugin), eq(task));
        assertEquals(TASK_ID, task.getBukkitTaskId());
        assertTrue(task.isTaskRunningAsync());
        assertTrue(task.hasTaskBeenExecuted());
        assertEquals(FIXED_INSTANT.toEpochMilli(), task.getTaskStartTime());
    }

    @Test
    @DisplayName("Given an already executed task, when running again, then throws TaskCompletedException")
    void runTask_afterExecution_throwsTaskCompletedException() {
        CoreTask task = createTask();
        task.runTask();

        TaskCompletedException ex = assertThrows(TaskCompletedException.class, () -> task.runTask());
        assertTrue(ex.getMessage().contains(String.valueOf(TASK_ID)));
    }

    @Test
    @DisplayName("Given an already executed task, when running async again, then throws TaskCompletedException")
    void runTask_asyncAfterExecution_throwsTaskCompletedException() {
        CoreTask task = createTask();
        task.runTask(false);

        assertThrows(TaskCompletedException.class, () -> task.runTask(true));
    }
}

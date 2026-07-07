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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DelayableCoreTaskTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-06-15T12:00:00Z");
    private static final int TASK_ID = 99;

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

        when(mockScheduler.runTaskLater(any(), any(Runnable.class), anyLong())).thenReturn(mockBukkitTask);
        when(mockScheduler.runTaskLaterAsynchronously(any(), any(Runnable.class), anyLong())).thenReturn(mockBukkitTask);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockedBukkit.close();
        mocks.close();
    }

    private DelayableCoreTask createTask(long delay) {
        return new DelayableCoreTask(mockPlugin, delay) {
            @Override
            public void run() {
            }
        };
    }

    @Test
    @DisplayName("Given a positive delay, when constructing, then delay is stored correctly")
    void constructor_positiveDelay_storesValue() {
        DelayableCoreTask task = createTask(5);
        assertEquals(5, task.getTaskDelay());
    }

    @Test
    @DisplayName("Given a negative delay, when constructing, then delay is clamped to zero")
    void constructor_negativeDelay_clampsToZero() {
        DelayableCoreTask task = createTask(-10);
        assertEquals(0, task.getTaskDelay());
    }

    @Test
    @DisplayName("Given a zero delay, when constructing, then delay is zero")
    void constructor_zeroDelay_storesZero() {
        DelayableCoreTask task = createTask(0);
        assertEquals(0, task.getTaskDelay());
    }

    @Test
    @DisplayName("Given a delayable task, when running sync, then uses runTaskLater with correct tick delay")
    void runTask_sync_usesRunTaskLaterWithTicks() {
        DelayableCoreTask task = createTask(3);

        task.runTask(false);

        verify(mockScheduler).runTaskLater(eq(mockPlugin), eq(task), eq(60L));
        assertEquals(TASK_ID, task.getBukkitTaskId());
        assertFalse(task.isTaskRunningAsync());
        assertTrue(task.hasTaskBeenExecuted());
        assertEquals(FIXED_INSTANT.toEpochMilli(), task.getTaskStartTime());
    }

    @Test
    @DisplayName("Given a delayable task, when running async, then uses runTaskLaterAsynchronously with correct tick delay")
    void runTask_async_usesRunTaskLaterAsync() {
        DelayableCoreTask task = createTask(5);

        task.runTask(true);

        verify(mockScheduler).runTaskLaterAsynchronously(eq(mockPlugin), eq(task), eq(100L));
        assertEquals(TASK_ID, task.getBukkitTaskId());
        assertTrue(task.isTaskRunningAsync());
        assertTrue(task.hasTaskBeenExecuted());
    }

    @Test
    @DisplayName("Given a delayable task with zero delay, when running, then tick delay is zero")
    void runTask_zeroDelay_zeroTicks() {
        DelayableCoreTask task = createTask(0);

        task.runTask(false);

        verify(mockScheduler).runTaskLater(eq(mockPlugin), eq(task), eq(0L));
    }
}

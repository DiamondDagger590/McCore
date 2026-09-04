package com.diamonddagger590.mccore.task.player;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.DatabaseManager;
import com.diamonddagger590.mccore.database.table.impl.MutexDAO;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.player.PlayerManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.util.TimeProvider;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.isA;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlayerUnloadTaskTest {

    @Mock
    private CorePlugin mockPlugin;

    @Mock
    private CorePlayer mockCorePlayer;

    @Mock
    private Database mockDatabase;

    @Mock
    private Connection mockConnection;

    @Mock
    private BukkitScheduler mockScheduler;

    @Mock
    private BukkitTask mockBukkitTask;

    @Mock
    private PlayerManager<CorePlugin, CorePlayer> mockPlayerManager;

    @Mock
    private DatabaseManager<CorePlugin> mockDatabaseManager;

    private MockedStatic<CorePlugin> corePluginStatic;
    private MockedStatic<Bukkit> bukkitStatic;
    private MockedStatic<MutexDAO> mutexDaoStatic;

    private final TimeProvider timeProvider = new TimeProvider(Clock.fixed(Instant.ofEpochMilli(1000000), ZoneId.of("UTC")));
    private final UUID playerUUID = UUID.randomUUID();

    private boolean unloadPlayerResult;

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();

        lenient().when(mockPlugin.getTimeProvider()).thenReturn(timeProvider);
        lenient().when(mockCorePlayer.getUUID()).thenReturn(playerUUID);
        lenient().when(mockDatabaseManager.getDatabase()).thenReturn(mockDatabase);

        ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        managerRegistry.register(mockPlayerManager);
        managerRegistry.register(mockDatabaseManager);

        corePluginStatic = mockStatic(CorePlugin.class);
        corePluginStatic.when(CorePlugin::getInstance).thenReturn(mockPlugin);
        lenient().when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());

        bukkitStatic = mockStatic(Bukkit.class);
        bukkitStatic.when(Bukkit::getScheduler).thenReturn(mockScheduler);
        lenient().when(mockScheduler.runTaskTimerAsynchronously(any(), any(Runnable.class), anyLong(), anyLong())).thenReturn(mockBukkitTask);
        lenient().when(mockBukkitTask.getTaskId()).thenReturn(42);

        mutexDaoStatic = mockStatic(MutexDAO.class);

        unloadPlayerResult = true;
    }

    @AfterEach
    void tearDown() {
        mutexDaoStatic.close();
        bukkitStatic.close();
        corePluginStatic.close();
        RegistryResetExtension.resetRegistry();
    }

    private PlayerUnloadTask createTask() {
        return new PlayerUnloadTask(mockPlugin, mockCorePlayer) {
            @Override
            protected boolean unloadPlayer() {
                return unloadPlayerResult;
            }

            @Override
            protected void onDelayComplete() {}

            @Override
            protected void onIntervalStart() {}

            @Override
            protected void onIntervalPause() {}

            @Override
            protected void onIntervalResume() {}

            @Override
            protected void onTaskExpire() {}
        };
    }

    @Test
    @DisplayName("Given a new task, when constructed, then getResult returns a non-null CompletableFuture")
    void constructor_resultFutureIsNotNull() {
        PlayerUnloadTask task = createTask();
        assertNotNull(task.getResult());
        assertFalse(task.getResult().isDone());
    }

    @Test
    @DisplayName("Given a new task, when constructed, then getCorePlayer returns the correct player")
    void constructor_corePlayerIsCorrect() {
        PlayerUnloadTask task = createTask();
        assertSame(mockCorePlayer, task.getCorePlayer());
    }

    @Test
    @DisplayName("Given a new task, when constructed, then getPlugin returns the CorePlugin")
    void constructor_pluginIsCorrect() {
        PlayerUnloadTask task = createTask();
        assertSame(mockPlugin, task.getPlugin());
    }

    @Test
    @DisplayName("Constructor does not self-schedule")
    void constructor_doesNotSchedule() {
        createTask();
        verify(mockScheduler, never()).runTaskTimerAsynchronously(any(), any(Runnable.class), anyLong(), anyLong());
    }

    @Test
    @DisplayName("runTask(true) after construction schedules async timer")
    void runTask_schedulesAsyncTimer_whenCalledAfterConstruction() {
        PlayerUnloadTask task = createTask();
        task.runTask(true);
        verify(mockScheduler).runTaskTimerAsynchronously(eq(mockPlugin), any(Runnable.class), eq(0L), eq(1L));
    }

    @Test
    @DisplayName("Given player uses no mutex, when onIntervalComplete and unload succeeds, then result is true")
    void onIntervalComplete_noMutex_unloadSucceeds() throws SQLException {
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockCorePlayer.useMutex()).thenReturn(false);
        lenient().doNothing().when(mockScheduler).cancelTask(anyInt());
        lenient().when(mockScheduler.scheduleSyncDelayedTask(eq(mockPlugin), any(Runnable.class))).thenReturn(1);

        unloadPlayerResult = true;
        PlayerUnloadTask task = createTask();
        task.onIntervalComplete();

        assertTrue(task.getResult().isDone());
        assertTrue(task.getResult().join());
    }

    @Test
    @DisplayName("Given player uses no mutex, when onIntervalComplete and unload fails, then result is false")
    void onIntervalComplete_noMutex_unloadFails() throws SQLException {
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockCorePlayer.useMutex()).thenReturn(false);
        lenient().doNothing().when(mockScheduler).cancelTask(anyInt());

        unloadPlayerResult = false;
        PlayerUnloadTask task = createTask();
        task.onIntervalComplete();

        assertTrue(task.getResult().isDone());
        assertFalse(task.getResult().join());
    }

    @Test
    @DisplayName("Given player uses mutex and mutex is not locked, when onIntervalComplete, then resumes and waits")
    void onIntervalComplete_mutexNotLocked_resumesAndWaits() throws SQLException {
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockCorePlayer.useMutex()).thenReturn(true);
        mutexDaoStatic.when(() -> MutexDAO.isUserMutexLocked(mockConnection, playerUUID)).thenReturn(false);

        PlayerUnloadTask task = createTask();
        task.onIntervalComplete();

        assertFalse(task.getResult().isDone());
    }

    @Test
    @DisplayName("Given player uses mutex and mutex is locked, when onIntervalComplete and unload succeeds, then locks mutex and result is true")
    void onIntervalComplete_mutexLocked_unloadsAndLocksMutex() throws SQLException {
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockCorePlayer.useMutex()).thenReturn(true);
        lenient().doNothing().when(mockScheduler).cancelTask(anyInt());
        lenient().when(mockScheduler.scheduleSyncDelayedTask(eq(mockPlugin), any(Runnable.class))).thenReturn(1);
        mutexDaoStatic.when(() -> MutexDAO.isUserMutexLocked(mockConnection, playerUUID)).thenReturn(true);

        unloadPlayerResult = true;
        PlayerUnloadTask task = createTask();
        task.onIntervalComplete();

        assertTrue(task.getResult().isDone());
        assertTrue(task.getResult().join());
        verify(mockCorePlayer).lock();
        mutexDaoStatic.verify(() -> MutexDAO.updateUserMutex(mockConnection, mockCorePlayer));
    }

    @Test
    @DisplayName("Given player uses mutex and mutex is locked but unload fails, when onIntervalComplete, then result is false")
    void onIntervalComplete_mutexLocked_unloadFails() throws SQLException {
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockCorePlayer.useMutex()).thenReturn(true);
        lenient().doNothing().when(mockScheduler).cancelTask(anyInt());
        mutexDaoStatic.when(() -> MutexDAO.isUserMutexLocked(mockConnection, playerUUID)).thenReturn(true);

        unloadPlayerResult = false;
        PlayerUnloadTask task = createTask();
        task.onIntervalComplete();

        assertTrue(task.getResult().isDone());
        assertFalse(task.getResult().join());
        verify(mockCorePlayer, never()).lock();
    }

    @Test
    @DisplayName("Given task is cancelled externally (not completed), when onCancel, then result is false")
    void onCancel_notCompleted_resultIsFalse() {
        lenient().doNothing().when(mockScheduler).cancelTask(anyInt());

        PlayerUnloadTask task = createTask();
        task.cancelTask();
        task.run();

        assertTrue(task.getResult().isDone());
        assertFalse(task.getResult().join());
    }

    @Test
    @DisplayName("Given a database runtime exception, when onIntervalComplete, then exception propagates")
    void onIntervalComplete_runtimeException_propagates() {
        when(mockDatabase.getConnection()).thenThrow(new RuntimeException(new SQLException("Test exception")));

        PlayerUnloadTask task = createTask();
        // getConnection() wraps SQLException in RuntimeException, and the catch block in
        // runUnloadPlayerTask only catches SQLException, so RuntimeException propagates
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, task::onIntervalComplete);
    }

    @Test
    @DisplayName("Given connection close throws SQLException, when onIntervalComplete, then exception is caught gracefully")
    void onIntervalComplete_connectionCloseThrowsSqlException_caughtGracefully() throws SQLException {
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockCorePlayer.useMutex()).thenReturn(false);
        lenient().doNothing().when(mockScheduler).cancelTask(anyInt());
        lenient().when(mockScheduler.scheduleSyncDelayedTask(eq(mockPlugin), any(Runnable.class))).thenReturn(1);
        org.mockito.Mockito.doThrow(new SQLException("Connection close failed")).when(mockConnection).close();

        unloadPlayerResult = true;
        PlayerUnloadTask task = createTask();
        task.onIntervalComplete();

        assertTrue(task.getResult().isDone());
        assertTrue(task.getResult().join());
    }

    @Test
    @DisplayName("Given task completed successfully, when onCancel is triggered, then result is not overwritten to false")
    void onCancel_afterCompletion_doesNotOverwriteResult() throws SQLException {
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockCorePlayer.useMutex()).thenReturn(false);
        lenient().doNothing().when(mockScheduler).cancelTask(anyInt());
        lenient().when(mockScheduler.scheduleSyncDelayedTask(eq(mockPlugin), any(Runnable.class))).thenReturn(1);

        unloadPlayerResult = true;
        PlayerUnloadTask task = createTask();
        task.onIntervalComplete();

        assertTrue(task.getResult().isDone());
        assertTrue(task.getResult().join());

        task.run();

        assertTrue(task.getResult().join());
    }

    @Test
    @DisplayName("Given successful unload, when scheduleSyncDelayedTask callback executes, then PlayerUnloadEvent is fired")
    void onPlayerUnloadSuccessfully_firesPlayerUnloadEvent() throws SQLException {
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockCorePlayer.useMutex()).thenReturn(false);
        lenient().doNothing().when(mockScheduler).cancelTask(anyInt());

        org.mockito.ArgumentCaptor<Runnable> runnableCaptor = org.mockito.ArgumentCaptor.forClass(Runnable.class);
        when(mockScheduler.scheduleSyncDelayedTask(eq(mockPlugin), runnableCaptor.capture())).thenReturn(1);

        org.bukkit.plugin.PluginManager mockPluginManager = org.mockito.Mockito.mock(org.bukkit.plugin.PluginManager.class);
        bukkitStatic.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);

        unloadPlayerResult = true;
        PlayerUnloadTask task = createTask();
        task.onIntervalComplete();

        assertTrue(task.getResult().isDone());

        Runnable eventFiringRunnable = runnableCaptor.getValue();
        assertNotNull(eventFiringRunnable);
        eventFiringRunnable.run();

        verify(mockPluginManager).callEvent(isA(com.diamonddagger590.mccore.event.player.PlayerUnloadEvent.class));
    }
}

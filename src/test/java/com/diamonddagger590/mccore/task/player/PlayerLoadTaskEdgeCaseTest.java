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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlayerLoadTaskEdgeCaseTest {

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
    private PlayerManager<CorePlugin, CorePlayer> mockPlayerManager;

    @Mock
    private DatabaseManager<CorePlugin> mockDatabaseManager;

    private MockedStatic<CorePlugin> corePluginStatic;
    private MockedStatic<Bukkit> bukkitStatic;
    private MockedStatic<MutexDAO> mutexDaoStatic;

    private final TimeProvider timeProvider = new TimeProvider(Clock.fixed(Instant.ofEpochMilli(1000000), ZoneId.of("UTC")));
    private final UUID playerUUID = UUID.randomUUID();

    private boolean loadPlayerResult;

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

        mutexDaoStatic = mockStatic(MutexDAO.class);

        loadPlayerResult = true;
    }

    @AfterEach
    void tearDown() {
        mutexDaoStatic.close();
        bukkitStatic.close();
        corePluginStatic.close();
        RegistryResetExtension.resetRegistry();
    }

    private PlayerLoadTask createTask() {
        return new PlayerLoadTask(mockPlugin, mockCorePlayer) {
            @Override
            protected boolean loadPlayer() {
                return loadPlayerResult;
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
    @DisplayName("Given task already completed, when onIntervalComplete called again, then early-returns without loading")
    void onIntervalComplete_alreadyCompleted_earlyReturns() throws SQLException {
        when(mockPlayerManager.getPlayer(playerUUID)).thenReturn(Optional.empty());
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockCorePlayer.useMutex()).thenReturn(false);
        lenient().doNothing().when(mockScheduler).cancelTask(anyInt());
        lenient().when(mockScheduler.scheduleSyncDelayedTask(eq(mockPlugin), any(Runnable.class))).thenReturn(1);

        loadPlayerResult = true;
        PlayerLoadTask task = createTask();
        task.onIntervalComplete();

        assertTrue(task.getResult().isDone());
        assertTrue(task.getResult().join());

        task.onIntervalComplete();

        assertTrue(task.getResult().isDone());
    }

    @Test
    @DisplayName("Given task completed successfully, when onCancel is invoked, then result remains true")
    void onCancel_afterSuccessfulCompletion_resultRemainsTrue() throws SQLException {
        when(mockPlayerManager.getPlayer(playerUUID)).thenReturn(Optional.empty());
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockCorePlayer.useMutex()).thenReturn(false);
        lenient().doNothing().when(mockScheduler).cancelTask(anyInt());
        lenient().when(mockScheduler.scheduleSyncDelayedTask(eq(mockPlugin), any(Runnable.class))).thenReturn(1);

        loadPlayerResult = true;
        PlayerLoadTask task = createTask();
        task.onIntervalComplete();

        assertTrue(task.getResult().isDone());
        assertTrue(task.getResult().join());

        task.cancelTask();
        task.run();

        assertTrue(task.getResult().join());
    }

    @Test
    @DisplayName("Given getConnection throws RuntimeException wrapping SQLException, when onIntervalComplete, then exception propagates and result is not completed")
    void onIntervalComplete_sqlExceptionFromGetConnection_resultNotCompleted() {
        when(mockPlayerManager.getPlayer(playerUUID)).thenReturn(Optional.empty());
        when(mockDatabase.getConnection()).thenThrow(new RuntimeException(new SQLException("Connection failed")));

        PlayerLoadTask task = createTask();

        assertThrows(RuntimeException.class, task::onIntervalComplete);
        assertFalse(task.getResult().isDone());
    }

    @Test
    @DisplayName("Given multiple calls to onIntervalComplete with player stored then removed, when called, then loads on second call")
    void onIntervalComplete_playerStoredThenRemoved_loadsOnSecondCall() throws SQLException {
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockCorePlayer.useMutex()).thenReturn(false);
        lenient().doNothing().when(mockScheduler).cancelTask(anyInt());
        lenient().when(mockScheduler.scheduleSyncDelayedTask(eq(mockPlugin), any(Runnable.class))).thenReturn(1);

        when(mockPlayerManager.getPlayer(playerUUID)).thenReturn(Optional.of(mockCorePlayer));

        loadPlayerResult = true;
        PlayerLoadTask task = createTask();
        task.onIntervalComplete();

        assertFalse(task.getResult().isDone());

        when(mockPlayerManager.getPlayer(playerUUID)).thenReturn(Optional.empty());
        task.onIntervalComplete();

        assertTrue(task.getResult().isDone());
        assertTrue(task.getResult().join());
    }
}

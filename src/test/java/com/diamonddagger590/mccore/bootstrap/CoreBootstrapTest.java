package com.diamonddagger590.mccore.bootstrap;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.DatabaseManager;
import com.diamonddagger590.mccore.database.driver.DriverRegistry;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.CoreManagerKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.util.TimeProvider;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CoreBootstrapTest {

    private CorePlugin mockPlugin;
    private PluginManager mockPluginManager;
    private MockedStatic<Bukkit> bukkitMock;

    @BeforeEach
    void setUp() {
        RegistryResetExtension.resetRegistry();

        mockPlugin = mock(CorePlugin.class);
        when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("TestBootstrap"));

        mockPluginManager = mock(PluginManager.class);
        when(mockPluginManager.isPluginEnabled(any(String.class))).thenReturn(false);

        bukkitMock = mockStatic(Bukkit.class);
        bukkitMock.when(Bukkit::getPluginManager).thenReturn(mockPluginManager);
    }

    @AfterEach
    void tearDown() {
        bukkitMock.close();
        RegistryResetExtension.resetRegistry();
    }

    @Test
    void constructor_setsPlugin() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        assertSame(mockPlugin, bootstrap.getPlugin());
    }

    @Test
    void getPlugin_returnsSameInstance() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        assertSame(mockPlugin, bootstrap.getPlugin());
        assertSame(bootstrap.getPlugin(), bootstrap.getPlugin());
    }

    @Test
    void start_testProfile_registersManagerRegistry() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.start(StartupProfile.TEST);

        ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        assertNotNull(managerRegistry);
    }

    @Test
    void start_testProfile_registersPluginHookRegistry() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.start(StartupProfile.TEST);

        assertNotNull(RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK));
    }

    @Test
    void start_testProfile_registersPlayerSettingRegistry() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.start(StartupProfile.TEST);

        assertNotNull(RegistryAccess.registryAccess().registry(RegistryKey.PLAYER_SETTING));
    }

    @Test
    void start_testProfile_registersStatisticRegistry() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.start(StartupProfile.TEST);

        assertNotNull(RegistryAccess.registryAccess().registry(RegistryKey.STATISTIC));
    }

    @Test
    void start_testProfile_registersReloadableContentManager() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.start(StartupProfile.TEST);

        ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        assertTrue(managerRegistry.registered(CoreManagerKey.RELOADABLE_CONTENT));
    }

    @Test
    void start_testProfile_registersChatResponseManager() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.start(StartupProfile.TEST);

        ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        assertTrue(managerRegistry.registered(CoreManagerKey.CHAT_RESPONSE));
    }

    @Test
    void start_testProfile_doesNotRegisterDriverRegistry() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.start(StartupProfile.TEST);

        assertFalse(hasDriverRegistry());
    }

    @Test
    void start_testProfile_registersThreeListeners() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.start(StartupProfile.TEST);

        verify(mockPluginManager, times(3)).registerEvents(any(), eq(mockPlugin));
    }

    @Test
    void start_testProfile_checksPluginHooks() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.start(StartupProfile.TEST);

        verify(mockPluginManager, atLeast(1)).isPluginEnabled(any(String.class));
    }

    @Test
    void start_prodProfile_registersDriverRegistry() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        try {
            bootstrap.start(StartupProfile.PROD);
        } catch (Exception e) {
            // CommandRegistrar creates PaperCommandManager which requires full Paper — expected
        }

        assertTrue(hasDriverRegistry());
    }

    @Test
    void start_prodProfile_registersAllCoreRegistries() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        try {
            bootstrap.start(StartupProfile.PROD);
        } catch (Exception e) {
            // CommandRegistrar requires Paper runtime
        }

        assertNotNull(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER));
        assertNotNull(RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK));
        assertNotNull(RegistryAccess.registryAccess().registry(RegistryKey.PLAYER_SETTING));
        assertNotNull(RegistryAccess.registryAccess().registry(RegistryKey.STATISTIC));
        assertNotNull(RegistryAccess.registryAccess().registry(RegistryKey.DRIVER));
    }

    @Test
    void stop_withDatabaseManager_shutsDownDatabase() {
        RegistryResetExtension.setupRegistry();

        Database mockDatabase = mock(Database.class);
        DatabaseManager<CorePlugin> mockDbManager = new TestDatabaseManager(mockPlugin, mockDatabase);

        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockDbManager);

        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.stop(StartupProfile.PROD);

        verify(mockDatabase).shutdown();
    }

    @Test
    void stop_withoutDatabaseManager_noException() {
        RegistryResetExtension.setupRegistry();

        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.stop(StartupProfile.PROD);
    }

    @Test
    void stop_testProfile_withDatabaseManager_shutsDownDatabase() {
        RegistryResetExtension.setupRegistry();

        Database mockDatabase = mock(Database.class);
        DatabaseManager<CorePlugin> mockDbManager = new TestDatabaseManager(mockPlugin, mockDatabase);

        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockDbManager);

        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.stop(StartupProfile.TEST);

        verify(mockDatabase).shutdown();
    }

    @Test
    void getTimeProvider_returnsNonNull() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        TimeProvider timeProvider = bootstrap.getTimeProvider();
        assertNotNull(timeProvider);
    }

    @Test
    void getTimeProvider_returnsWorkingProvider() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        TimeProvider timeProvider = bootstrap.getTimeProvider();
        assertNotNull(timeProvider.now());
    }

    private boolean hasDriverRegistry() {
        try {
            DriverRegistry reg = RegistryAccess.registryAccess().registry(RegistryKey.DRIVER);
            return reg != null;
        } catch (Exception e) {
            return false;
        }
    }

    static class TestBootstrap extends CoreBootstrap<CorePlugin> {
        TestBootstrap(@NotNull CorePlugin plugin) {
            super(plugin);
        }
    }

    static class TestDatabaseManager extends DatabaseManager<CorePlugin> {
        private final Database database;

        TestDatabaseManager(@NotNull CorePlugin plugin, @NotNull Database database) {
            super(plugin);
            this.database = database;
        }

        @NotNull
        @Override
        public Database getDatabase() {
            return database;
        }
    }
}

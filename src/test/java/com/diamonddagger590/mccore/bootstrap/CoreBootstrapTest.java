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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    @DisplayName("Given a plugin, when constructing bootstrap, then plugin is set")
    void constructor_setsPlugin_whenPluginProvided() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        assertSame(mockPlugin, bootstrap.getPlugin());
    }

    @Test
    @DisplayName("Given a constructed bootstrap, when getPlugin is called multiple times, then returns same instance")
    void getPlugin_returnsSameInstance_whenCalledMultipleTimes() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        assertSame(mockPlugin, bootstrap.getPlugin());
        assertSame(bootstrap.getPlugin(), bootstrap.getPlugin());
    }

    @Test
    @DisplayName("Given test profile, when start is called, then manager registry is registered")
    void start_registersManagerRegistry_whenTestProfile() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.start(StartupProfile.TEST);

        ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        assertNotNull(managerRegistry);
    }

    @Test
    @DisplayName("Given test profile, when start is called, then plugin hook registry is registered")
    void start_registersPluginHookRegistry_whenTestProfile() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.start(StartupProfile.TEST);

        assertNotNull(RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK));
    }

    @Test
    @DisplayName("Given test profile, when start is called, then player setting registry is registered")
    void start_registersPlayerSettingRegistry_whenTestProfile() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.start(StartupProfile.TEST);

        assertNotNull(RegistryAccess.registryAccess().registry(RegistryKey.PLAYER_SETTING));
    }

    @Test
    @DisplayName("Given test profile, when start is called, then statistic registry is registered")
    void start_registersStatisticRegistry_whenTestProfile() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.start(StartupProfile.TEST);

        assertNotNull(RegistryAccess.registryAccess().registry(RegistryKey.STATISTIC));
    }

    @Test
    @DisplayName("Given test profile, when start is called, then reloadable content manager is registered")
    void start_registersReloadableContentManager_whenTestProfile() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.start(StartupProfile.TEST);

        ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        assertTrue(managerRegistry.registered(CoreManagerKey.RELOADABLE_CONTENT));
    }

    @Test
    @DisplayName("Given test profile, when start is called, then chat response manager is registered")
    void start_registersChatResponseManager_whenTestProfile() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.start(StartupProfile.TEST);

        ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        assertTrue(managerRegistry.registered(CoreManagerKey.CHAT_RESPONSE));
    }

    @Test
    @DisplayName("Given test profile, when start is called, then driver registry is not registered")
    void start_doesNotRegisterDriverRegistry_whenTestProfile() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.start(StartupProfile.TEST);

        assertFalse(hasDriverRegistry());
    }

    @Test
    @DisplayName("Given test profile, when start is called, then three listeners are registered")
    void start_registersThreeListeners_whenTestProfile() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.start(StartupProfile.TEST);

        verify(mockPluginManager, times(3)).registerEvents(any(), eq(mockPlugin));
    }

    @Test
    @DisplayName("Given test profile, when start is called, then plugin hooks are checked")
    void start_checksPluginHooks_whenTestProfile() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.start(StartupProfile.TEST);

        verify(mockPluginManager, atLeast(1)).isPluginEnabled(any(String.class));
    }

    @Test
    @DisplayName("Given prod profile, when start is called, then driver registry is registered")
    void start_registersDriverRegistry_whenProdProfile() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        try {
            bootstrap.start(StartupProfile.PROD);
        } catch (Exception e) {
            // CommandRegistrar creates PaperCommandManager which requires full Paper — expected
        }

        assertTrue(hasDriverRegistry());
    }

    @Test
    @DisplayName("Given prod profile, when start is called, then all core registries are registered")
    void start_registersAllCoreRegistries_whenProdProfile() {
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
    @DisplayName("Given a registered database manager, when stop is called with prod profile, then database is shut down")
    void stop_shutsDownDatabase_whenDatabaseManagerRegistered() {
        RegistryResetExtension.setupRegistry();

        Database mockDatabase = mock(Database.class);
        DatabaseManager<CorePlugin> mockDbManager = new TestDatabaseManager(mockPlugin, mockDatabase);

        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockDbManager);

        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.stop(StartupProfile.PROD);

        verify(mockDatabase).shutdown();
    }

    @Test
    @DisplayName("Given no database manager registered, when stop is called with prod profile, then no exception is thrown")
    void stop_doesNotThrow_whenNoDatabaseManagerRegistered() {
        RegistryResetExtension.setupRegistry();

        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        assertDoesNotThrow(() -> bootstrap.stop(StartupProfile.PROD));
    }

    @Test
    @DisplayName("Given a registered database manager, when stop is called with test profile, then database is shut down")
    void stop_shutsDownDatabase_whenTestProfileAndDatabaseManagerRegistered() {
        RegistryResetExtension.setupRegistry();

        Database mockDatabase = mock(Database.class);
        DatabaseManager<CorePlugin> mockDbManager = new TestDatabaseManager(mockPlugin, mockDatabase);

        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockDbManager);

        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        bootstrap.stop(StartupProfile.TEST);

        verify(mockDatabase).shutdown();
    }

    @Test
    @DisplayName("Given a constructed bootstrap, when getTimeProvider is called, then returns non-null provider")
    void getTimeProvider_returnsNonNull_whenCalled() {
        TestBootstrap bootstrap = new TestBootstrap(mockPlugin);
        TimeProvider timeProvider = bootstrap.getTimeProvider();
        assertNotNull(timeProvider);
    }

    @Test
    @DisplayName("Given a constructed bootstrap, when calling now on the time provider, then returns current time")
    void getTimeProvider_returnsCurrentTime_whenNowCalled() {
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

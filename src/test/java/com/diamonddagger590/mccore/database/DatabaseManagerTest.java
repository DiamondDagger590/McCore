package com.diamonddagger590.mccore.database;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.CoreManagerKey;
import com.diamonddagger590.mccore.registry.manager.Manager;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DatabaseManagerTest {

    @Mock
    private CorePlugin mockPlugin;

    @Mock
    private Database mockDatabase;

    private TestDatabaseManager databaseManager;

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();
        databaseManager = new TestDatabaseManager(mockPlugin, mockDatabase);
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
    }

    private ManagerRegistry registry() {
        return RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
    }

    @Test
    @DisplayName("Given a DatabaseManager, when calling getDatabase, then returns the database instance")
    void getDatabase_returnsDatabaseInstance() {
        assertSame(mockDatabase, databaseManager.getDatabase());
    }

    @Test
    @DisplayName("Given a DatabaseManager, when calling plugin, then returns the plugin instance")
    void plugin_returnsPluginInstance() {
        assertSame(mockPlugin, databaseManager.plugin());
    }

    @Test
    @DisplayName("Given a DatabaseManager, when it extends Manager, then it is an instance of Manager")
    void databaseManager_extendsManager() {
        assertTrue(databaseManager instanceof Manager<?>);
    }

    @Test
    @DisplayName("Given a DatabaseManager, when registered in ManagerRegistry, then can be retrieved by CORE_DATABASE_MANAGER key")
    void databaseManager_canBeRegisteredAndRetrievedByKey() {
        registry().register(databaseManager);
        DatabaseManager<?> retrieved = registry().manager(CoreManagerKey.CORE_DATABASE_MANAGER);
        assertNotNull(retrieved);
        assertSame(databaseManager, retrieved);
    }

    @Test
    @DisplayName("Given a DatabaseManager, when registered in ManagerRegistry, then registry reports it as registered")
    void databaseManager_isRegisteredInRegistry() {
        registry().register(databaseManager);
        assertTrue(registry().registered(CoreManagerKey.CORE_DATABASE_MANAGER));
    }

    @Test
    @DisplayName("Given a DatabaseManager subclass, when registered and retrieved, then getDatabase returns expected database")
    void registeredDatabaseManager_getDatabase_returnsExpectedDatabase() {
        registry().register(databaseManager);
        DatabaseManager<?> retrieved = registry().manager(CoreManagerKey.CORE_DATABASE_MANAGER);
        assertSame(mockDatabase, retrieved.getDatabase());
    }

    private static class TestDatabaseManager extends DatabaseManager<CorePlugin> {

        private final Database database;

        TestDatabaseManager(@NotNull CorePlugin plugin, @NotNull Database database) {
            super(plugin);
            this.database = database;
        }

        @Override
        public @NotNull Database getDatabase() {
            return database;
        }
    }
}

package com.diamonddagger590.mccore.bootstrap.registrar;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.bootstrap.BootstrapContext;
import com.diamonddagger590.mccore.bootstrap.StartupProfile;
import com.diamonddagger590.mccore.command.CoreCommandManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandRegistrarTest {

    @Mock
    private CorePlugin mockPlugin;

    private ManagerRegistry managerRegistry;

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();
        managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
    }

    @NotNull
    private BootstrapContext<CorePlugin> context() {
        return new BootstrapContext<>(mockPlugin, StartupProfile.PROD);
    }

    @Test
    @DisplayName("Given a valid context, when register is called, then CoreCommandManager is registered in ManagerRegistry")
    void register_registersCoreCommandManager_whenContextIsValid() {
        try (MockedConstruction<CoreCommandManager> mocked = mockConstruction(CoreCommandManager.class)) {
            new CommandRegistrar<CorePlugin>().register(context());

            assertEquals(1, mocked.constructed().size());
            CoreCommandManager constructed = mocked.constructed().get(0);
            assertTrue(managerRegistry.registered(constructed));
        }
    }

    @Test
    @DisplayName("Given a valid context, when register is called, then CoreCommandManager receives the plugin from context")
    void register_passesPluginToManager_whenContextIsValid() {
        AtomicReference<MockedConstruction.Context> capturedContext = new AtomicReference<>();
        try (MockedConstruction<CoreCommandManager> mocked = mockConstruction(CoreCommandManager.class,
                (mock, ctx) -> capturedContext.set(ctx))) {
            new CommandRegistrar<CorePlugin>().register(context());

            assertEquals(1, mocked.constructed().size());
            assertNotNull(capturedContext.get());
            assertEquals(1, capturedContext.get().arguments().size());
            assertSame(mockPlugin, capturedContext.get().arguments().get(0));
        }
    }

    @Test
    @DisplayName("Given register was already called, when register is called again, then it throws IllegalArgumentException")
    void register_throwsIllegalArgumentException_whenCalledTwice() {
        try (MockedConstruction<CoreCommandManager> mocked = mockConstruction(CoreCommandManager.class)) {
            CommandRegistrar<CorePlugin> registrar = new CommandRegistrar<>();
            registrar.register(context());

            assertThrows(IllegalArgumentException.class, () -> registrar.register(context()));
        }
    }
}

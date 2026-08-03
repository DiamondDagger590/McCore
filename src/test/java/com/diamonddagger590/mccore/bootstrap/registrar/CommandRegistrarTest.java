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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandRegistrarTest {

    private CommandRegistrar<CorePlugin> registrar;

    @Mock
    private CorePlugin mockPlugin;

    @BeforeEach
    void setUp() {
        registrar = new CommandRegistrar<>();
        RegistryResetExtension.setupRegistry();
        when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
    }

    @Test
    @DisplayName("Given a bootstrap context, when registering, then CoreCommandManager is created and registered in ManagerRegistry")
    void register_registersCoreCommandManager() {
        BootstrapContext<CorePlugin> context = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);

        try (MockedConstruction<CoreCommandManager> mocked = mockConstruction(CoreCommandManager.class)) {
            registrar.register(context);

            assertEquals(1, mocked.constructed().size(),
                    "Exactly one CoreCommandManager should be constructed");
            ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
            assertTrue(managerRegistry.registered(mocked.constructed().get(0)),
                    "The constructed CoreCommandManager should be registered in the ManagerRegistry");
        }
    }

    @Test
    @DisplayName("Given a bootstrap context, when registering, then CoreCommandManager is constructed with the context's plugin")
    void register_constructsCoreCommandManager_withContextPlugin() {
        BootstrapContext<CorePlugin> context = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);
        List<List<?>> capturedArgs = new ArrayList<>();

        try (MockedConstruction<CoreCommandManager> mocked = mockConstruction(CoreCommandManager.class,
                (mock, ctx) -> capturedArgs.add(new ArrayList<>(ctx.arguments())))) {
            registrar.register(context);

            assertEquals(1, capturedArgs.size(),
                    "Constructor should be called exactly once");
            assertEquals(1, capturedArgs.get(0).size(),
                    "CoreCommandManager should be constructed with exactly one argument");
            assertSame(mockPlugin, capturedArgs.get(0).get(0),
                    "CoreCommandManager should be constructed with the context's plugin");
        }
    }
}

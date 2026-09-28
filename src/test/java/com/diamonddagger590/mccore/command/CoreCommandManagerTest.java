package com.diamonddagger590.mccore.command;

import com.diamonddagger590.mccore.CorePlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.processors.confirmation.ConfirmationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreCommandManagerTest {

    @Mock
    private CorePlugin mockPlugin;

    private CoreCommandManager manager;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        try (MockedStatic<PaperCommandManager> paperStatic = mockStatic(PaperCommandManager.class);
             MockedConstruction<AnnotationParser> parserConstruction = mockConstruction(AnnotationParser.class)) {

            PaperCommandManager.Builder<CommandSourceStack> mockBuilder = mock(PaperCommandManager.Builder.class);
            PaperCommandManager.CoordinatedBuilder<CommandSourceStack> mockCoordinatedBuilder = mock(PaperCommandManager.CoordinatedBuilder.class);
            PaperCommandManager<CommandSourceStack> mockPaperManager = mock(PaperCommandManager.class);

            paperStatic.when(PaperCommandManager::builder).thenReturn(mockBuilder);
            when(mockBuilder.executionCoordinator(any())).thenReturn(mockCoordinatedBuilder);
            when(mockCoordinatedBuilder.buildOnEnable(any())).thenReturn(mockPaperManager);

            manager = new CoreCommandManager(mockPlugin);
        }
    }

    @Nested
    @DisplayName("getCommandManager")
    class GetCommandManager {

        @Test
        @DisplayName("Given a constructed manager, when getCommandManager is called, then it returns a non-null PaperCommandManager")
        void getCommandManager_returnsNonNull_whenConstructed() {
            assertNotNull(manager.getCommandManager());
        }
    }

    @Nested
    @DisplayName("getAnnotationParser")
    class GetAnnotationParser {

        @Test
        @DisplayName("Given a constructed manager, when getAnnotationParser is called, then it returns a non-null AnnotationParser")
        void getAnnotationParser_returnsNonNull_whenConstructed() {
            assertNotNull(manager.getAnnotationParser());
        }
    }

    @Nested
    @DisplayName("plugin")
    class Plugin {

        @Test
        @DisplayName("Given a constructed manager, when plugin is called, then it returns the plugin passed to constructor")
        void plugin_returnsPluginFromConstructor_whenCalled() {
            assertSame(mockPlugin, manager.plugin());
        }
    }

    @Nested
    @DisplayName("registerConfirmationCommand")
    class RegisterConfirmationCommand {

        @Test
        @DisplayName("Given no confirmation manager registered, when registerConfirmationCommand is called, then it succeeds")
        @SuppressWarnings("unchecked")
        void registerConfirmationCommand_succeeds_whenNotYetRegistered() {
            ConfirmationManager<CommandSourceStack> confirmationManager = mock(ConfirmationManager.class);

            manager.registerConfirmationCommand(confirmationManager);

            assertSame(confirmationManager, manager.getConfirmationManager());
        }

        @Test
        @DisplayName("Given a confirmation manager already registered, when registerConfirmationCommand is called again, then it throws IllegalStateException")
        @SuppressWarnings("unchecked")
        void registerConfirmationCommand_throwsIllegalStateException_whenAlreadyRegistered() {
            ConfirmationManager<CommandSourceStack> first = mock(ConfirmationManager.class);
            ConfirmationManager<CommandSourceStack> second = mock(ConfirmationManager.class);

            manager.registerConfirmationCommand(first);

            assertThrows(IllegalStateException.class, () -> manager.registerConfirmationCommand(second));
        }
    }

    @Nested
    @DisplayName("getConfirmationManager")
    class GetConfirmationManager {

        @Test
        @DisplayName("Given no confirmation manager registered, when getConfirmationManager is called, then it throws IllegalStateException")
        void getConfirmationManager_throwsIllegalStateException_whenNotRegistered() {
            assertThrows(IllegalStateException.class, () -> manager.getConfirmationManager());
        }

        @Test
        @DisplayName("Given a confirmation manager registered, when getConfirmationManager is called, then it returns the registered manager")
        @SuppressWarnings("unchecked")
        void getConfirmationManager_returnsRegisteredManager_whenRegistered() {
            ConfirmationManager<CommandSourceStack> confirmationManager = mock(ConfirmationManager.class);
            manager.registerConfirmationCommand(confirmationManager);

            ConfirmationManager<CommandSourceStack> result = manager.getConfirmationManager();

            assertSame(confirmationManager, result);
        }
    }
}

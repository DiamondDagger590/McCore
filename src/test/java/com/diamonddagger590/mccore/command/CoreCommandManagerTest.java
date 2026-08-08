package com.diamonddagger590.mccore.command;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.manager.Manager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.processors.confirmation.ConfirmationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@SuppressWarnings("unchecked")
class CoreCommandManagerTest {

    private CoreCommandManager manager;
    private PaperCommandManager<CommandSourceStack> mockCommandManager;
    private AnnotationParser<CommandSender> mockAnnotationParser;

    @BeforeEach
    void setUp() throws Exception {
        Unsafe unsafe = getUnsafe();
        manager = (CoreCommandManager) unsafe.allocateInstance(CoreCommandManager.class);

        mockCommandManager = mock(PaperCommandManager.class);
        mockAnnotationParser = mock(AnnotationParser.class);

        putField(Manager.class, "plugin", manager, mock(CorePlugin.class));
        putField(CoreCommandManager.class, "commandManager", manager, mockCommandManager);
        putField(CoreCommandManager.class, "annotationParser", manager, mockAnnotationParser);
    }

    @Nested
    @DisplayName("getCommandManager")
    class GetCommandManager {

        @Test
        @DisplayName("Given a properly initialized manager, when getCommandManager is called, then returns the command manager instance")
        void returnsCommandManagerInstance() {
            assertSame(mockCommandManager, manager.getCommandManager());
        }
    }

    @Nested
    @DisplayName("getAnnotationParser")
    class GetAnnotationParser {

        @Test
        @DisplayName("Given a properly initialized manager, when getAnnotationParser is called, then returns the annotation parser instance")
        void returnsAnnotationParserInstance() {
            assertSame(mockAnnotationParser, manager.getAnnotationParser());
        }
    }

    @Nested
    @DisplayName("registerConfirmationCommand")
    class RegisterConfirmationCommand {

        @Test
        @DisplayName("Given no confirmation manager registered, when registerConfirmationCommand is called, then registers without error")
        void registersSuccessfully_whenNoneRegistered() {
            ConfirmationManager<CommandSourceStack> confirmationManager = mock(ConfirmationManager.class);
            assertDoesNotThrow(() -> manager.registerConfirmationCommand(confirmationManager));
        }

        @Test
        @DisplayName("Given a confirmation manager already registered, when registerConfirmationCommand is called again, then throws IllegalStateException")
        void throwsIllegalState_whenAlreadyRegistered() {
            ConfirmationManager<CommandSourceStack> first = mock(ConfirmationManager.class);
            ConfirmationManager<CommandSourceStack> second = mock(ConfirmationManager.class);
            manager.registerConfirmationCommand(first);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> manager.registerConfirmationCommand(second));
            assertEquals("ConfirmationManager already registered.", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("getConfirmationManager")
    class GetConfirmationManager {

        @Test
        @DisplayName("Given no confirmation manager registered, when getConfirmationManager is called, then throws IllegalStateException")
        void throwsIllegalState_whenNotRegistered() {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> manager.getConfirmationManager());
            assertTrue(ex.getMessage().contains("was not registered"));
        }

        @Test
        @DisplayName("Given a confirmation manager registered, when getConfirmationManager is called, then returns the registered instance")
        void returnsRegisteredInstance() {
            ConfirmationManager<CommandSourceStack> confirmationManager = mock(ConfirmationManager.class);
            manager.registerConfirmationCommand(confirmationManager);

            assertSame(confirmationManager, manager.getConfirmationManager());
        }
    }

    private static Unsafe getUnsafe() throws Exception {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }

    private static void putField(Class<?> clazz, String fieldName, Object target, Object value) throws Exception {
        Unsafe unsafe = getUnsafe();
        Field field = clazz.getDeclaredField(fieldName);
        unsafe.putObject(target, unsafe.objectFieldOffset(field), value);
    }
}

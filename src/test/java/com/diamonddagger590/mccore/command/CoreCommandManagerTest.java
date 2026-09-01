package com.diamonddagger590.mccore.command;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.processors.confirmation.ConfirmationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class CoreCommandManagerTest {

    @Mock
    private CorePlugin mockPlugin;

    @Mock
    private PaperCommandManager<CommandSourceStack> mockCommandManager;

    @Mock
    private AnnotationParser<?> mockAnnotationParser;

    @Mock
    private ConfirmationManager<CommandSourceStack> mockConfirmationManager;

    private CoreCommandManager manager;

    @BeforeEach
    void setUp() throws Exception {
        RegistryResetExtension.setupRegistry();

        Unsafe unsafe = getUnsafe();
        manager = (CoreCommandManager) unsafe.allocateInstance(CoreCommandManager.class);

        setField(CoreCommandManager.class, "commandManager", manager, mockCommandManager);
        setField(CoreCommandManager.class, "annotationParser", manager, mockAnnotationParser);
        setField(CoreCommandManager.class.getSuperclass(), "plugin", manager, mockPlugin);
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
    }

    @Test
    @DisplayName("Given an initialized manager, when getCommandManager is called, then returns the PaperCommandManager")
    void getCommandManager_returnsCommandManager() {
        assertSame(mockCommandManager, manager.getCommandManager());
    }

    @Test
    @DisplayName("Given an initialized manager, when getAnnotationParser is called, then returns the AnnotationParser")
    void getAnnotationParser_returnsAnnotationParser() {
        assertSame(mockAnnotationParser, manager.getAnnotationParser());
    }

    @Test
    @DisplayName("Given an initialized manager, when plugin is called, then returns the CorePlugin")
    void plugin_returnsPlugin() {
        assertSame(mockPlugin, manager.plugin());
    }

    @Test
    @DisplayName("Given no confirmation manager registered, when registerConfirmationCommand is called, then stores the confirmation manager")
    void registerConfirmationCommand_storesManager_whenNoneRegistered() {
        manager.registerConfirmationCommand(mockConfirmationManager);

        assertSame(mockConfirmationManager, manager.getConfirmationManager());
    }

    @Test
    @DisplayName("Given a confirmation manager already registered, when registerConfirmationCommand is called again, then throws IllegalStateException")
    void registerConfirmationCommand_throwsIllegalState_whenAlreadyRegistered() {
        manager.registerConfirmationCommand(mockConfirmationManager);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> manager.registerConfirmationCommand(mockConfirmationManager));
        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Given no confirmation manager registered, when getConfirmationManager is called, then throws IllegalStateException")
    void getConfirmationManager_throwsIllegalState_whenNotRegistered() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> manager.getConfirmationManager());
        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Given a confirmation manager registered, when getConfirmationManager is called, then returns the registered manager")
    void getConfirmationManager_returnsManager_whenRegistered() {
        manager.registerConfirmationCommand(mockConfirmationManager);

        ConfirmationManager<CommandSourceStack> result = manager.getConfirmationManager();

        assertSame(mockConfirmationManager, result);
    }

    @Test
    @DisplayName("Given a second confirmation manager, when registerConfirmationCommand is called after first, then original manager is preserved")
    void registerConfirmationCommand_preservesOriginalManager_whenDoubleRegistrationAttempted() {
        ConfirmationManager<CommandSourceStack> secondManager = org.mockito.Mockito.mock(ConfirmationManager.class);
        manager.registerConfirmationCommand(mockConfirmationManager);

        assertThrows(IllegalStateException.class,
                () -> manager.registerConfirmationCommand(secondManager));

        assertSame(mockConfirmationManager, manager.getConfirmationManager());
    }

    @Test
    @DisplayName("Given a CoreCommandManager, when registered in ManagerRegistry, then can be retrieved by COMMAND key")
    void manager_canBeRegisteredAndRetrievedByKey() {
        ManagerRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        registry.register(manager);

        CoreCommandManager retrieved = registry.manager(ManagerKey.COMMAND);

        assertSame(manager, retrieved);
    }

    @Test
    @DisplayName("Given a CoreCommandManager registered in ManagerRegistry, then registry reports it as registered")
    void manager_isRegisteredInRegistry() {
        ManagerRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        registry.register(manager);

        assertTrue(registry.registered(ManagerKey.COMMAND));
    }

    private static Unsafe getUnsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static void setField(Class<?> clazz, String fieldName, Object target, Object value) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

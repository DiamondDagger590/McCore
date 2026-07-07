package com.diamonddagger590.mccore.registry;

import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry;
import com.diamonddagger590.mccore.testing.InternalResetTestTools;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistryAccessTest {

    @BeforeEach
    void setUp() {
        InternalResetTestTools.resetRegistryAccess("com.diamonddagger590.mccore.registry.RegistryAccess");
    }

    @AfterEach
    void tearDown() {
        InternalResetTestTools.resetRegistryAccess("com.diamonddagger590.mccore.registry.RegistryAccess");
    }

    @Test
    @DisplayName("Given registryAccess called multiple times, when comparing references, then returns same singleton instance")
    void registryAccess_returnsSameSingleton_whenCalledMultipleTimes() {
        RegistryAccess first = RegistryAccess.registryAccess();
        RegistryAccess second = RegistryAccess.registryAccess();
        assertNotNull(first);
        assertSame(first, second);
    }

    @Test
    @DisplayName("Given a new registry, when registering, then registration succeeds")
    void register_succeeds_whenRegistryIsNew() {
        ManagerRegistry registry = new ManagerRegistry();
        RegistryAccess.registryAccess().register(registry);
        assertTrue(RegistryAccess.registryAccess().registered(registry));
    }

    @Test
    @DisplayName("Given an already registered registry, when registering again, then throws IllegalArgumentException")
    void register_throwsIllegalArgument_whenRegistryAlreadyRegistered() {
        RegistryAccess.registryAccess().register(new ManagerRegistry());
        assertThrows(IllegalArgumentException.class, () -> RegistryAccess.registryAccess().register(new ManagerRegistry()));
    }

    @Test
    @DisplayName("Given a registered registry, when checking registered, then returns true")
    void registered_returnsTrue_whenRegistryIsRegistered() {
        PluginHookRegistry hookRegistry = new PluginHookRegistry();
        RegistryAccess.registryAccess().register(hookRegistry);
        assertTrue(RegistryAccess.registryAccess().registered(hookRegistry));
    }

    @Test
    @DisplayName("Given no registered registries, when checking registered, then returns false")
    void registered_returnsFalse_whenRegistryIsNotRegistered() {
        assertFalse(RegistryAccess.registryAccess().registered(new ManagerRegistry()));
    }

    @Test
    @DisplayName("Given a registered registry, when retrieving by key, then returns the correct registry instance")
    void registry_returnsCorrectInstance_whenRegistered() {
        ManagerRegistry managerRegistry = new ManagerRegistry();
        RegistryAccess.registryAccess().register(managerRegistry);
        ManagerRegistry retrieved = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        assertSame(managerRegistry, retrieved);
    }

    @Test
    @DisplayName("Given no registry registered for key, when retrieving by key, then returns null")
    void registry_returnsNull_whenNoRegistryForKey() {
        assertNull(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER));
    }

    @Test
    @DisplayName("Given multiple registries, when retrieving each by key, then returns correct instances")
    void registry_returnsCorrectInstances_whenMultipleRegistered() {
        ManagerRegistry managerRegistry = new ManagerRegistry();
        PluginHookRegistry hookRegistry = new PluginHookRegistry();
        RegistryAccess.registryAccess().register(managerRegistry);
        RegistryAccess.registryAccess().register(hookRegistry);

        assertSame(managerRegistry, RegistryAccess.registryAccess().registry(RegistryKey.MANAGER));
        assertSame(hookRegistry, RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK));
    }

    @Test
    @DisplayName("Given a registered registry, when checking with a different instance of same type, then returns true")
    void registered_returnsTrue_whenDifferentInstanceOfSameTypeChecked() {
        ManagerRegistry first = new ManagerRegistry();
        RegistryAccess.registryAccess().register(first);
        assertTrue(RegistryAccess.registryAccess().registered(new ManagerRegistry()));
    }

    @Test
    @DisplayName("Given only ManagerRegistry registered, when checking PluginHookRegistry registered, then returns false")
    void registered_returnsFalse_whenDifferentTypeChecked() {
        RegistryAccess.registryAccess().register(new ManagerRegistry());
        assertFalse(RegistryAccess.registryAccess().registered(new PluginHookRegistry()));
    }

    @Test
    @DisplayName("Given an exception message for duplicate registration, when thrown, then message contains class name")
    void register_exceptionMessage_containsClassName_whenDuplicate() {
        RegistryAccess.registryAccess().register(new ManagerRegistry());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> RegistryAccess.registryAccess().register(new ManagerRegistry()));
        assertTrue(exception.getMessage().contains("ManagerRegistry"));
    }
}

package com.diamonddagger590.mccore.registry.manager;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManagerRegistryTest {

    private static class TestManagerA extends Manager<CorePlugin> {
        TestManagerA() {
            super(null);
        }
    }

    private static class TestManagerB extends Manager<CorePlugin> {
        TestManagerB() {
            super(null);
        }
    }

    private static class TestManagerASub extends TestManagerA {
    }

    private static final ManagerKey<TestManagerA> KEY_A = ManagerKeyImpl.create(TestManagerA.class);
    private static final ManagerKey<TestManagerB> KEY_B = ManagerKeyImpl.create(TestManagerB.class);

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
    }

    private ManagerRegistry registry() {
        return RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
    }

    @Test
    @DisplayName("Given a new manager, when registering, then registration succeeds")
    void register_succeeds_whenManagerIsNew() {
        TestManagerA manager = new TestManagerA();
        registry().register(manager);
        assertTrue(registry().registered(manager));
    }

    @Test
    @DisplayName("Given an already registered manager, when registering again, then throws IllegalArgumentException")
    void register_throwsIllegalArgument_whenManagerAlreadyRegistered() {
        TestManagerA manager = new TestManagerA();
        registry().register(manager);
        assertThrows(IllegalArgumentException.class, () -> registry().register(new TestManagerA()));
    }

    @Test
    @DisplayName("Given a registered manager, when checking registered by instance, then returns true")
    void registered_returnsTrue_whenManagerIsRegistered() {
        TestManagerA manager = new TestManagerA();
        registry().register(manager);
        assertTrue(registry().registered(manager));
    }

    @Test
    @DisplayName("Given no registered managers, when checking registered by instance, then returns false")
    void registered_returnsFalse_whenManagerIsNotRegistered() {
        assertFalse(registry().registered(new TestManagerA()));
    }

    @Test
    @DisplayName("Given a registered manager, when checking registered by key, then returns true")
    void registered_returnsTrue_whenCheckedByKey() {
        registry().register(new TestManagerA());
        assertTrue(registry().registered(KEY_A));
    }

    @Test
    @DisplayName("Given no registered managers, when checking registered by key, then returns false")
    void registered_returnsFalse_whenCheckedByKeyAndNotRegistered() {
        assertFalse(registry().registered(KEY_A));
    }

    @Test
    @DisplayName("Given a registered manager, when retrieving by key, then returns the correct manager")
    void manager_returnsCorrectInstance_whenRetrievedByKey() {
        TestManagerA manager = new TestManagerA();
        registry().register(manager);
        assertEquals(manager, registry().manager(KEY_A));
    }

    @Test
    @DisplayName("Given multiple registered managers, when retrieving each by key, then returns correct instances")
    void manager_returnsCorrectInstances_whenMultipleRegistered() {
        TestManagerA managerA = new TestManagerA();
        TestManagerB managerB = new TestManagerB();
        registry().register(managerA);
        registry().register(managerB);
        assertEquals(managerA, registry().manager(KEY_A));
        assertEquals(managerB, registry().manager(KEY_B));
    }

    @Test
    @DisplayName("Given a subclass manager registered, when retrieving by parent key, then returns subclass instance")
    void manager_returnsSubclassInstance_whenRetrievedByParentKey() {
        TestManagerASub subManager = new TestManagerASub();
        registry().register(subManager);
        assertEquals(subManager, registry().manager(KEY_A));
    }

    @Test
    @DisplayName("Given a subclass manager registered, when checking registered by parent key, then returns true")
    void registered_returnsTrue_whenSubclassRegisteredAndCheckedByParentKey() {
        TestManagerASub subManager = new TestManagerASub();
        registry().register(subManager);
        assertTrue(registry().registered(KEY_A));
    }

    @Test
    @DisplayName("Given no manager registered for key, when retrieving by key, then returns null")
    void manager_returnsNull_whenNoManagerRegisteredForKey() {
        assertNull(registry().manager(KEY_A));
    }

    @Test
    @DisplayName("Given a different manager registered, when retrieving by non-matching key, then returns null via fallback")
    void manager_returnsNull_whenDifferentManagerRegisteredAndKeyDoesNotMatch() {
        registry().register(new TestManagerB());
        assertNull(registry().manager(KEY_A));
    }

    @Test
    @DisplayName("Given a subclass manager registered, when checking registered by subclass instance, then returns true")
    void registered_returnsTrue_whenSubclassInstanceRegisteredAndCheckedByParentInstance() {
        TestManagerASub subManager = new TestManagerASub();
        registry().register(subManager);
        assertTrue(registry().registered(new TestManagerASub()));
    }
}

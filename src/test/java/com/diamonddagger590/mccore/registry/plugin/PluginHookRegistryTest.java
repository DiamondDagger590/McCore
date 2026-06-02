package com.diamonddagger590.mccore.registry.plugin;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginHookRegistryTest {

    private interface MarkerHookInterface {}

    private static class TestHookA extends PluginHook<CorePlugin> {
        TestHookA() {
            super(null);
        }
    }

    private static class TestHookB extends PluginHook<CorePlugin> implements MarkerHookInterface {
        TestHookB() {
            super(null);
        }
    }

    private static class TestHookC extends PluginHook<CorePlugin> implements MarkerHookInterface {
        TestHookC() {
            super(null);
        }
    }

    private static final PluginHookKey<TestHookA> KEY_A = PluginHookKeyImpl.create(TestHookA.class);
    private static final PluginHookKey<TestHookB> KEY_B = PluginHookKeyImpl.create(TestHookB.class);
    private static final PluginHookKey<TestHookC> KEY_C = PluginHookKeyImpl.create(TestHookC.class);

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
    }

    private PluginHookRegistry registry() {
        return RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK);
    }

    @Test
    @DisplayName("Given a new plugin hook, when registering, then registration succeeds")
    void register_succeeds_whenHookIsNew() {
        TestHookA hook = new TestHookA();
        registry().register(hook);
        assertTrue(registry().registered(hook));
    }

    @Test
    @DisplayName("Given an already registered plugin hook, when registering again, then throws IllegalArgumentException")
    void register_throwsIllegalArgument_whenHookAlreadyRegistered() {
        registry().register(new TestHookA());
        assertThrows(IllegalArgumentException.class, () -> registry().register(new TestHookA()));
    }

    @Test
    @DisplayName("Given a registered hook, when checking registered, then returns true")
    void registered_returnsTrue_whenHookIsRegistered() {
        TestHookA hook = new TestHookA();
        registry().register(hook);
        assertTrue(registry().registered(hook));
    }

    @Test
    @DisplayName("Given no registered hooks, when checking registered, then returns false")
    void registered_returnsFalse_whenHookIsNotRegistered() {
        assertFalse(registry().registered(new TestHookA()));
    }

    @Test
    @DisplayName("Given a registered hook, when retrieving by key, then returns Optional containing the hook")
    void pluginHook_returnsPresent_whenHookIsRegistered() {
        TestHookA hook = new TestHookA();
        registry().register(hook);
        Optional<TestHookA> result = registry().pluginHook(KEY_A);
        assertTrue(result.isPresent());
        assertEquals(hook, result.get());
    }

    @Test
    @DisplayName("Given no registered hook for key, when retrieving by key, then returns empty Optional")
    void pluginHook_returnsEmpty_whenHookIsNotRegistered() {
        Optional<TestHookA> result = registry().pluginHook(KEY_A);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Given multiple hooks implementing same interface, when querying by interface, then returns all matching")
    void pluginHooks_returnsAllMatching_whenFilteredByInterface() {
        TestHookB hookB = new TestHookB();
        TestHookC hookC = new TestHookC();
        registry().register(hookB);
        registry().register(hookC);

        List<MarkerHookInterface> hooks = registry().pluginHooks(MarkerHookInterface.class);
        assertEquals(2, hooks.size());
        assertTrue(hooks.contains(hookB));
        assertTrue(hooks.contains(hookC));
    }

    @Test
    @DisplayName("Given hooks that don't implement the interface, when querying by interface, then returns empty list")
    void pluginHooks_returnsEmptyList_whenNoHooksMatchInterface() {
        registry().register(new TestHookA());
        List<MarkerHookInterface> hooks = registry().pluginHooks(MarkerHookInterface.class);
        assertTrue(hooks.isEmpty());
    }

    @Test
    @DisplayName("Given no registered hooks, when querying by interface, then returns empty list")
    void pluginHooks_returnsEmptyList_whenNoHooksRegistered() {
        List<MarkerHookInterface> hooks = registry().pluginHooks(MarkerHookInterface.class);
        assertTrue(hooks.isEmpty());
    }

    @Test
    @DisplayName("Given mixed hooks, when querying by interface, then only returns matching hooks")
    void pluginHooks_returnsOnlyMatching_whenMixedHooksRegistered() {
        registry().register(new TestHookA());
        TestHookB hookB = new TestHookB();
        registry().register(hookB);

        List<MarkerHookInterface> hooks = registry().pluginHooks(MarkerHookInterface.class);
        assertEquals(1, hooks.size());
        assertEquals(hookB, hooks.get(0));
    }

    @Test
    @DisplayName("Given multiple registered hooks, when retrieving each by key, then returns correct instances")
    void pluginHook_returnsCorrectInstances_whenMultipleRegistered() {
        TestHookA hookA = new TestHookA();
        TestHookB hookB = new TestHookB();
        registry().register(hookA);
        registry().register(hookB);
        assertEquals(hookA, registry().pluginHook(KEY_A).orElse(null));
        assertEquals(hookB, registry().pluginHook(KEY_B).orElse(null));
    }
}

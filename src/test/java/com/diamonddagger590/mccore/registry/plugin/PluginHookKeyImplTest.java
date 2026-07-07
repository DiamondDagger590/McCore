package com.diamonddagger590.mccore.registry.plugin;

import com.diamonddagger590.mccore.CorePlugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginHookKeyImplTest {

    private static class TestHookA extends PluginHook<CorePlugin> {
        TestHookA() {
            super(null);
        }
    }

    private static class TestHookB extends PluginHook<CorePlugin> {
        TestHookB() {
            super(null);
        }
    }

    @Test
    @DisplayName("Given a hook class, when creating a key, then key is not null")
    void create_returnsNonNullKey_whenGivenHookClass() {
        PluginHookKey<TestHookA> key = PluginHookKeyImpl.create(TestHookA.class);
        assertNotNull(key);
    }

    @Test
    @DisplayName("Given a hook class, when calling hookClass, then returns the same class")
    void hookClass_returnsSameClass_whenCalledOnCreatedKey() {
        PluginHookKey<TestHookA> key = PluginHookKeyImpl.create(TestHookA.class);
        assertEquals(TestHookA.class, key.hookClass());
    }

    @Test
    @DisplayName("Given a PluginHookKeyImpl, when calling clazz record accessor, then returns the same class")
    void clazz_returnsSameClass_asHookClass() {
        PluginHookKeyImpl<TestHookA> key = new PluginHookKeyImpl<>(TestHookA.class);
        assertEquals(TestHookA.class, key.clazz());
        assertEquals(key.clazz(), key.hookClass());
    }

    @Test
    @DisplayName("Given two keys with the same class, when comparing, then they are equal")
    void equals_returnsTrue_whenKeysHaveSameClass() {
        PluginHookKey<TestHookA> key1 = PluginHookKeyImpl.create(TestHookA.class);
        PluginHookKey<TestHookA> key2 = PluginHookKeyImpl.create(TestHookA.class);
        assertEquals(key1, key2);
    }

    @Test
    @DisplayName("Given two keys with different classes, when comparing, then they are not equal")
    void equals_returnsFalse_whenKeysHaveDifferentClasses() {
        PluginHookKey<TestHookA> keyA = PluginHookKeyImpl.create(TestHookA.class);
        PluginHookKey<TestHookB> keyB = PluginHookKeyImpl.create(TestHookB.class);
        assertNotEquals(keyA, keyB);
    }

    @Test
    @DisplayName("Given two keys with the same class, when comparing hash codes, then they are equal")
    void hashCode_isSame_whenKeysHaveSameClass() {
        PluginHookKey<TestHookA> key1 = PluginHookKeyImpl.create(TestHookA.class);
        PluginHookKey<TestHookA> key2 = PluginHookKeyImpl.create(TestHookA.class);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    @DisplayName("Given a key, when calling hashCode repeatedly, then it is consistent")
    void hashCode_isConsistent_whenCalledRepeatedly() {
        PluginHookKey<TestHookA> key = PluginHookKeyImpl.create(TestHookA.class);
        assertEquals(key.hashCode(), key.hashCode());
    }

    @Test
    @DisplayName("Given a key, when calling toString, then it contains the class name")
    void toString_containsClassName_whenCalled() {
        PluginHookKey<TestHookA> key = PluginHookKeyImpl.create(TestHookA.class);
        String str = key.toString();
        assertNotNull(str);
        assertTrue(str.contains("TestHookA"));
    }

    @Test
    @DisplayName("Given a key compared to itself, when checking equality, then returns true")
    void equals_returnsTrue_whenComparedToSelf() {
        PluginHookKey<TestHookA> key = PluginHookKeyImpl.create(TestHookA.class);
        assertEquals(key, key);
    }

    @Test
    @DisplayName("Given a key compared to null, when checking equality, then returns false")
    void equals_returnsFalse_whenComparedToNull() {
        PluginHookKey<TestHookA> key = PluginHookKeyImpl.create(TestHookA.class);
        assertNotEquals(null, key);
    }

}

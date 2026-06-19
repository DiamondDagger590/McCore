package com.diamonddagger590.mccore.registry.manager;

import com.diamonddagger590.mccore.CorePlugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagerKeyImplTest {

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

    @Test
    @DisplayName("Given a manager class, when creating a key, then key is not null")
    void create_returnsNonNullKey_whenGivenManagerClass() {
        ManagerKey<TestManagerA> key = ManagerKeyImpl.create(TestManagerA.class);
        assertNotNull(key);
    }

    @Test
    @DisplayName("Given a manager class, when calling managerClass, then returns the same class")
    void managerClass_returnsSameClass_whenCalledOnCreatedKey() {
        ManagerKey<TestManagerA> key = ManagerKeyImpl.create(TestManagerA.class);
        assertEquals(TestManagerA.class, key.managerClass());
    }

    @Test
    @DisplayName("Given a ManagerKeyImpl, when calling manager record accessor, then returns the same class")
    void manager_returnsSameClass_asManagerClass() {
        ManagerKeyImpl<TestManagerA> key = new ManagerKeyImpl<>(TestManagerA.class);
        assertEquals(TestManagerA.class, key.manager());
        assertEquals(key.manager(), key.managerClass());
    }

    @Test
    @DisplayName("Given two keys with the same class, when comparing, then they are equal")
    void equals_returnsTrue_whenKeysHaveSameClass() {
        ManagerKey<TestManagerA> key1 = ManagerKeyImpl.create(TestManagerA.class);
        ManagerKey<TestManagerA> key2 = ManagerKeyImpl.create(TestManagerA.class);
        assertEquals(key1, key2);
    }

    @Test
    @DisplayName("Given two keys with different classes, when comparing, then they are not equal")
    void equals_returnsFalse_whenKeysHaveDifferentClasses() {
        ManagerKey<TestManagerA> keyA = ManagerKeyImpl.create(TestManagerA.class);
        ManagerKey<TestManagerB> keyB = ManagerKeyImpl.create(TestManagerB.class);
        assertNotEquals(keyA, keyB);
    }

    @Test
    @DisplayName("Given two keys with the same class, when comparing hash codes, then they are equal")
    void hashCode_isSame_whenKeysHaveSameClass() {
        ManagerKey<TestManagerA> key1 = ManagerKeyImpl.create(TestManagerA.class);
        ManagerKey<TestManagerA> key2 = ManagerKeyImpl.create(TestManagerA.class);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    @DisplayName("Given two keys with different classes, when comparing hash codes, then they differ")
    void hashCode_differs_whenKeysHaveDifferentClasses() {
        ManagerKey<TestManagerA> keyA = ManagerKeyImpl.create(TestManagerA.class);
        ManagerKey<TestManagerB> keyB = ManagerKeyImpl.create(TestManagerB.class);
        assertNotEquals(keyA.hashCode(), keyB.hashCode());
    }

    @Test
    @DisplayName("Given a key, when calling toString, then it contains the class name")
    void toString_containsClassName_whenCalled() {
        ManagerKey<TestManagerA> key = ManagerKeyImpl.create(TestManagerA.class);
        String str = key.toString();
        assertNotNull(str);
        assertEquals(true, str.contains("TestManagerA"));
    }

    @Test
    @DisplayName("Given a key compared to itself, when checking equality, then returns true")
    void equals_returnsTrue_whenComparedToSelf() {
        ManagerKey<TestManagerA> key = ManagerKeyImpl.create(TestManagerA.class);
        assertEquals(key, key);
    }

    @Test
    @DisplayName("Given a key compared to null, when checking equality, then returns false")
    void equals_returnsFalse_whenComparedToNull() {
        ManagerKey<TestManagerA> key = ManagerKeyImpl.create(TestManagerA.class);
        assertNotEquals(null, key);
    }
}

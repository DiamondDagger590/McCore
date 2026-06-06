package com.diamonddagger590.mccore.mutex;

import com.diamonddagger590.mccore.exception.LockAlreadyHeldException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MutexableTest {

    private static class TestMutexable extends Mutexable {
    }

    private TestMutexable mutexable;

    @BeforeEach
    void setUp() {
        mutexable = new TestMutexable();
    }

    @Test
    @DisplayName("Given a new Mutexable, when checking isLocked, then returns false")
    void isLocked_returnsFalse_whenNewlyConstructed() {
        assertFalse(mutexable.isLocked());
    }

    @Test
    @DisplayName("Given an unlocked Mutexable, when locking, then isLocked returns true")
    void lock_setsLockedTrue_whenUnlocked() {
        mutexable.lock();
        assertTrue(mutexable.isLocked());
    }

    @Test
    @DisplayName("Given a locked Mutexable, when locking again, then throws LockAlreadyHeldException")
    void lock_throwsLockAlreadyHeldException_whenAlreadyLocked() {
        mutexable.lock();
        assertThrows(LockAlreadyHeldException.class, () -> mutexable.lock());
    }

    @Test
    @DisplayName("Given a locked Mutexable, when unlocking, then isLocked returns false")
    void unlock_setsLockedFalse_whenLocked() {
        mutexable.lock();
        mutexable.unlock();
        assertFalse(mutexable.isLocked());
    }

    @Test
    @DisplayName("Given an unlocked Mutexable, when unlocking, then no exception is thrown")
    void unlock_doesNotThrow_whenAlreadyUnlocked() {
        assertDoesNotThrow(() -> mutexable.unlock());
    }

    @Test
    @DisplayName("Given a Mutexable that was locked and unlocked, when locking again, then succeeds")
    void lock_succeeds_afterUnlock() {
        mutexable.lock();
        mutexable.unlock();
        assertDoesNotThrow(() -> mutexable.lock());
        assertTrue(mutexable.isLocked());
    }
}

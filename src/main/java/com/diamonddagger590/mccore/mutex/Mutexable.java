package com.diamonddagger590.mccore.mutex;

import com.diamonddagger590.mccore.exception.LockAlreadyHeldException;

/**
 * This class provides a base implementation for {@link Lockable}.
 */
public abstract class Mutexable implements Lockable {

    private boolean locked;

    public Mutexable() {
        this.locked = false;
    }

    @Override
    public synchronized boolean isLocked() {
        return locked;
    }

    @Override
    public synchronized void lock() {
        if (isLocked()) {
            throw new LockAlreadyHeldException("There was an attempt to acquire an already locked lock.");
        }
        this.locked = true;
    }

    @Override
    public synchronized void unlock() {
        this.locked = false;
    }
}

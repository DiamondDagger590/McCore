package com.diamonddagger590.mccore.mutex;

/**
 * This class provides a base implementation for {@link Lockable}.
 */
public abstract class Mutexable implements Lockable {

    private boolean locked;

    public Mutexable() {
        this.locked = false;
    }

    @Override
    public synchronized boolean setLocked(boolean locked) {

        if(this.locked == locked){
            return false;
        }

        this.locked = locked;

        return true;
    }

    @Override
    public synchronized boolean isLocked() {
        return locked;
    }

    @Override
    public synchronized boolean lock() {
        return Lockable.super.lock();
    }

    @Override
    public synchronized boolean unlock() {
        return Lockable.super.unlock();
    }
}

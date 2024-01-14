package com.diamonddagger590.mccore.mutex;

/**
 * Provides methods for locking and unlocking an object.
 */
public interface Lockable {

    /**
     * Checks to see if this object is locked.
     *
     * @return {@code true} if this object is locked.
     */
    public boolean isLocked();

    /**
     * Locks this object.
     *
     */
    public void lock();

    /**
     * Unlocks this object.
     *
     */
    public void unlock();
}

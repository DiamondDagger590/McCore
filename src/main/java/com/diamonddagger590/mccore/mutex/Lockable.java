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
     * Sets this object's lock state to that of the provided boolean.
     *
     * @param locked The new lock state for the object.
     * @return {@code true} if the lock was successfully updated.
     */
    public boolean setLocked(boolean locked);

    /**
     * Locks this object.
     *
     * @return {@code true} if this object was successfully locked.
     */
    public default boolean lock() {
        return setLocked(true);
    }

    /**
     * Unlocks this object.
     *
     * @return {@code true} if this object was successfully unlocked.
     */
    public default boolean unlock() {
        return setLocked(false);
    }
}

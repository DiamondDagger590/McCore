package com.diamonddagger590.mccore.exception.gui;

import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import org.jetbrains.annotations.NotNull;

/**
 * This exception is thrown whenever a {@link Slot} is added to a {@link Gui} that
 * it isn't allowed to be added to.
 */
public class IllegalSlotAssignmentException extends RuntimeException {

    private final Gui gui;
    private final Slot slot;

    public IllegalSlotAssignmentException(@NotNull Gui gui, @NotNull Slot slot) {
        this.gui = gui;
        this.slot = slot;
    }

    /**
     * Gets the {@link Gui} that can't accept the {@link #getSlot() Slot}.
     *
     * @return The {@link Gui} that can't accept the {@link #getSlot() Slot}.
     */
    @NotNull
    public Gui getGui() {
        return gui;
    }

    /**
     * Gets the {@link Slot} that failed to be added.
     *
     * @return The {@link Slot} that failed to be added.
     */
    @NotNull
    public Slot getSlot() {
        return slot;
    }

    @Override
    public String getMessage() {
        return String.format("GUI %s had slot %s requested to be added. The GUI is not allowed for that slot type.", gui.toString(), slot.toString());
    }
}

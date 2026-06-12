package com.diamonddagger590.mccore.exception.gui;

import com.diamonddagger590.mccore.gui.BaseGui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IllegalSlotAssignmentExceptionTest {

    @Mock
    private BaseGui<?> mockGui;

    @Mock
    private Slot<?> mockSlot;

    @Test
    @DisplayName("Given a gui and slot, When constructing IllegalSlotAssignmentException, Then getGui() returns the gui")
    void getGuiReturnsGui() {
        var exception = new IllegalSlotAssignmentException(mockGui, mockSlot);
        assertEquals(mockGui, exception.getGui());
    }

    @Test
    @DisplayName("Given a gui and slot, When constructing IllegalSlotAssignmentException, Then getSlot() returns the slot")
    void getSlotReturnsSlot() {
        var exception = new IllegalSlotAssignmentException(mockGui, mockSlot);
        assertEquals(mockSlot, exception.getSlot());
    }

    @Test
    @DisplayName("Given an IllegalSlotAssignmentException, When calling getMessage(), Then it contains GUI and slot details")
    void getMessageContainsDetails() {
        when(mockGui.toString()).thenReturn("TestGui");
        when(mockSlot.toString()).thenReturn("TestSlot");

        var exception = new IllegalSlotAssignmentException(mockGui, mockSlot);
        String message = exception.getMessage();

        assertNotNull(message);
        assertTrue(message.contains("TestGui"));
        assertTrue(message.contains("TestSlot"));
        assertTrue(message.contains("not allowed"));
    }

    @Test
    @DisplayName("Given an IllegalSlotAssignmentException, When checking inheritance, Then it extends RuntimeException")
    void extendsRuntimeException() {
        var exception = new IllegalSlotAssignmentException(mockGui, mockSlot);
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Given an IllegalSlotAssignmentException, When calling getCause(), Then it returns null (no cause set)")
    void noCauseSet() {
        var exception = new IllegalSlotAssignmentException(mockGui, mockSlot);
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Given an IllegalSlotAssignmentException, When getMessage() is called, Then it has the expected format")
    void messageFormat() {
        when(mockGui.toString()).thenReturn("MyGui@abc");
        when(mockSlot.toString()).thenReturn("MySlot@def");

        var exception = new IllegalSlotAssignmentException(mockGui, mockSlot);
        assertEquals(
                "GUI MyGui@abc had slot MySlot@def requested to be added. The GUI is not allowed for that slot type.",
                exception.getMessage()
        );
    }
}

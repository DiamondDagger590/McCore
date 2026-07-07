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
    @DisplayName("Given a gui and slot, when constructing IllegalSlotAssignmentException, then getGui() returns the gui")
    void getGui_returnsGui_whenConstructed() {
        var exception = new IllegalSlotAssignmentException(mockGui, mockSlot);
        assertEquals(mockGui, exception.getGui());
    }

    @Test
    @DisplayName("Given a gui and slot, when constructing IllegalSlotAssignmentException, then getSlot() returns the slot")
    void getSlot_returnsSlot_whenConstructed() {
        var exception = new IllegalSlotAssignmentException(mockGui, mockSlot);
        assertEquals(mockSlot, exception.getSlot());
    }

    @Test
    @DisplayName("Given an IllegalSlotAssignmentException, when calling getMessage(), then it contains GUI and slot details")
    void getMessage_containsGuiAndSlotDetails_whenCalled() {
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
    @DisplayName("Given an IllegalSlotAssignmentException, when checking inheritance, then it extends RuntimeException")
    void constructor_createsRuntimeException_whenInstantiated() {
        var exception = new IllegalSlotAssignmentException(mockGui, mockSlot);
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Given an IllegalSlotAssignmentException, when calling getCause(), then it returns null (no cause set)")
    void getCause_returnsNull_whenNoCauseProvided() {
        var exception = new IllegalSlotAssignmentException(mockGui, mockSlot);
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Given an IllegalSlotAssignmentException, when getMessage() is called, then it has the expected format")
    void getMessage_matchesExpectedFormat_whenGuiAndSlotHaveToStrings() {
        when(mockGui.toString()).thenReturn("MyGui@abc");
        when(mockSlot.toString()).thenReturn("MySlot@def");

        var exception = new IllegalSlotAssignmentException(mockGui, mockSlot);
        assertEquals(
                "GUI MyGui@abc had slot MySlot@def requested to be added. The GUI is not allowed for that slot type.",
                exception.getMessage()
        );
    }
}

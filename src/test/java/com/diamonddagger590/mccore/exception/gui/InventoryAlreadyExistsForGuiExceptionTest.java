package com.diamonddagger590.mccore.exception.gui;

import com.diamonddagger590.mccore.gui.BaseGui;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InventoryAlreadyExistsForGuiExceptionTest {

    @Test
    @DisplayName("Given a BaseGui, when constructing exception, then getGui returns the same gui")
    void getGui_returnsSameGui_whenConstructed() {
        BaseGui<?> gui = mock(BaseGui.class);
        InventoryAlreadyExistsForGuiException ex = new InventoryAlreadyExistsForGuiException(gui);
        assertEquals(gui, ex.getGui());
    }

    @Test
    @DisplayName("Given a BaseGui with a UUID, when getting message, then message contains GUI UUID")
    void getMessage_containsGuiUUID_whenGuiHasUUID() {
        BaseGui<?> gui = mock(BaseGui.class);
        UUID guiUUID = UUID.randomUUID();
        when(gui.getUUID()).thenReturn(guiUUID);

        InventoryAlreadyExistsForGuiException ex = new InventoryAlreadyExistsForGuiException(gui);
        String message = ex.getMessage();

        assertNotNull(message);
        assertTrue(message.contains(guiUUID.toString()));
    }

    @Test
    @DisplayName("Given a BaseGui, when getting message, then message mentions refreshGui")
    void getMessage_mentionsRefreshGui_always() {
        BaseGui<?> gui = mock(BaseGui.class);
        UUID guiUUID = UUID.randomUUID();
        when(gui.getUUID()).thenReturn(guiUUID);

        InventoryAlreadyExistsForGuiException ex = new InventoryAlreadyExistsForGuiException(gui);

        assertTrue(ex.getMessage().contains("refreshGui()"));
    }

    @Test
    @DisplayName("Given an InventoryAlreadyExistsForGuiException, when checking type, then it is a RuntimeException")
    void inventoryAlreadyExistsForGuiException_isRuntimeException_always() {
        BaseGui<?> gui = mock(BaseGui.class);
        assertInstanceOf(RuntimeException.class, new InventoryAlreadyExistsForGuiException(gui));
    }
}

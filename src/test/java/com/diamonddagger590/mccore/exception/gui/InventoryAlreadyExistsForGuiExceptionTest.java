package com.diamonddagger590.mccore.exception.gui;

import com.diamonddagger590.mccore.gui.BaseGui;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryAlreadyExistsForGuiExceptionTest {

    @Mock
    private BaseGui<?> mockGui;

    @Test
    @DisplayName("Given a GUI, when constructing, then getGui returns the same GUI")
    void getGui_returnsGui_whenConstructed() {
        InventoryAlreadyExistsForGuiException ex = new InventoryAlreadyExistsForGuiException(mockGui);
        assertSame(mockGui, ex.getGui());
    }

    @Test
    @DisplayName("Given a GUI with a UUID, when getMessage is called, then message contains the UUID")
    void getMessage_containsUuid_whenGuiHasUuid() {
        UUID uuid = UUID.fromString("12345678-1234-1234-1234-123456789abc");
        when(mockGui.getUUID()).thenReturn(uuid);

        InventoryAlreadyExistsForGuiException ex = new InventoryAlreadyExistsForGuiException(mockGui);
        String message = ex.getMessage();

        assertTrue(message.contains("12345678-1234-1234-1234-123456789abc"));
    }

    @Test
    @DisplayName("Given a GUI, when getMessage is called, then message mentions refreshGui")
    void getMessage_mentionsRefreshGui_whenCalled() {
        when(mockGui.getUUID()).thenReturn(UUID.randomUUID());

        InventoryAlreadyExistsForGuiException ex = new InventoryAlreadyExistsForGuiException(mockGui);
        String message = ex.getMessage();

        assertTrue(message.contains("Gui#refreshGui()"));
    }

    @Test
    @DisplayName("Given a GUI, when getMessage is called, then message indicates inventory already exists")
    void getMessage_indicatesInventoryAlreadyExists_whenCalled() {
        when(mockGui.getUUID()).thenReturn(UUID.randomUUID());

        InventoryAlreadyExistsForGuiException ex = new InventoryAlreadyExistsForGuiException(mockGui);
        String message = ex.getMessage();

        assertTrue(message.contains("already has an inventory"));
    }

    @Test
    @DisplayName("Given an InventoryAlreadyExistsForGuiException, when checking type, then it is a RuntimeException")
    void inventoryAlreadyExistsForGuiException_isRuntimeException_always() {
        assertInstanceOf(RuntimeException.class, new InventoryAlreadyExistsForGuiException(mockGui));
    }

    @Test
    @DisplayName("Given two different GUIs, when constructing exceptions, then each returns its own GUI")
    void getGui_returnsDifferentGuis_whenConstructedSeparately(@Mock BaseGui<?> otherGui) {
        InventoryAlreadyExistsForGuiException ex1 = new InventoryAlreadyExistsForGuiException(mockGui);
        InventoryAlreadyExistsForGuiException ex2 = new InventoryAlreadyExistsForGuiException(otherGui);
        assertSame(mockGui, ex1.getGui());
        assertSame(otherGui, ex2.getGui());
    }
}

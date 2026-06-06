package com.diamonddagger590.mccore.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class LockAlreadyHeldExceptionTest {

    @Test
    @DisplayName("Given no arguments, when constructing, then message is null")
    void constructor_noArgs_messageIsNull() {
        LockAlreadyHeldException ex = new LockAlreadyHeldException();
        assertNull(ex.getMessage());
    }

    @Test
    @DisplayName("Given a message, when constructing, then message is stored")
    void constructor_withMessage_storesMessage() {
        LockAlreadyHeldException ex = new LockAlreadyHeldException("Lock is held");
        assertEquals("Lock is held", ex.getMessage());
    }

    @Test
    @DisplayName("LockAlreadyHeldException is a RuntimeException")
    void lockAlreadyHeldException_isRuntimeException() {
        assertInstanceOf(RuntimeException.class, new LockAlreadyHeldException());
    }
}

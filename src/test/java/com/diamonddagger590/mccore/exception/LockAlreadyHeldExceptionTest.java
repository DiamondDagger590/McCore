package com.diamonddagger590.mccore.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class LockAlreadyHeldExceptionTest {

    @Test
    @DisplayName("Given no arguments, when constructing, then message is null")
    void getMessage_returnsNull_whenConstructedWithNoArgs() {
        LockAlreadyHeldException ex = new LockAlreadyHeldException();
        assertNull(ex.getMessage());
    }

    @Test
    @DisplayName("Given a message, when constructing, then message is stored")
    void getMessage_returnsMessage_whenConstructedWithMessage() {
        LockAlreadyHeldException ex = new LockAlreadyHeldException("Lock is held");
        assertEquals("Lock is held", ex.getMessage());
    }

    @Test
    @DisplayName("Given a LockAlreadyHeldException, when checking type, then it is a RuntimeException")
    void lockAlreadyHeldException_isRuntimeException_always() {
        assertInstanceOf(RuntimeException.class, new LockAlreadyHeldException());
    }
}

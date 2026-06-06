package com.diamonddagger590.mccore.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class TaskCompletedExceptionTest {

    @Test
    @DisplayName("Given no arguments, when constructing, then message is null")
    void constructor_noArgs_messageIsNull() {
        TaskCompletedException ex = new TaskCompletedException();
        assertNull(ex.getMessage());
    }

    @Test
    @DisplayName("Given a message, when constructing, then message is stored")
    void constructor_withMessage_storesMessage() {
        TaskCompletedException ex = new TaskCompletedException("Task already ran");
        assertEquals("Task already ran", ex.getMessage());
    }

    @Test
    @DisplayName("TaskCompletedException is a RuntimeException")
    void taskCompletedException_isRuntimeException() {
        assertInstanceOf(RuntimeException.class, new TaskCompletedException());
    }
}

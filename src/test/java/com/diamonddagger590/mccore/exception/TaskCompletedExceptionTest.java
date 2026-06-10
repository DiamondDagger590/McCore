package com.diamonddagger590.mccore.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class TaskCompletedExceptionTest {

    @Test
    @DisplayName("Given no arguments, when constructing, then message is null")
    void getMessage_returnsNull_whenConstructedWithNoArgs() {
        TaskCompletedException ex = new TaskCompletedException();
        assertNull(ex.getMessage());
    }

    @Test
    @DisplayName("Given a message, when constructing, then message is stored")
    void getMessage_returnsMessage_whenConstructedWithMessage() {
        TaskCompletedException ex = new TaskCompletedException("Task already ran");
        assertEquals("Task already ran", ex.getMessage());
    }

    @Test
    @DisplayName("Given a TaskCompletedException, when checking type, then it is a RuntimeException")
    void taskCompletedException_isRuntimeException_always() {
        assertInstanceOf(RuntimeException.class, new TaskCompletedException());
    }
}

package com.diamonddagger590.mccore.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class EvaluationExceptionTest {

    @Test
    @DisplayName("Given no arguments, when constructing, then message is null")
    void constructor_noArgs_messageIsNull() {
        EvaluationException ex = new EvaluationException();
        assertNull(ex.getMessage());
    }

    @Test
    @DisplayName("Given a message, when constructing, then message is stored")
    void constructor_withMessage_storesMessage() {
        EvaluationException ex = new EvaluationException("Variable x not initialized");
        assertEquals("Variable x not initialized", ex.getMessage());
    }

    @Test
    @DisplayName("Given a cause, when constructing, then cause is stored")
    void constructor_withCause_storesCause() {
        RuntimeException cause = new RuntimeException("root cause");
        EvaluationException ex = new EvaluationException(cause);
        assertSame(cause, ex.getCause());
    }

    @Test
    @DisplayName("Given a message and cause, when constructing, then both are stored")
    void constructor_withMessageAndCause_storesBoth() {
        RuntimeException cause = new RuntimeException("root cause");
        EvaluationException ex = new EvaluationException("eval failed", cause);
        assertEquals("eval failed", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    @DisplayName("EvaluationException is a RuntimeException")
    void evaluationException_isRuntimeException() {
        assertInstanceOf(RuntimeException.class, new EvaluationException());
    }
}

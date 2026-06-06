package com.diamonddagger590.mccore.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ParseErrorTest {

    @Test
    @DisplayName("Given a message and position, when constructing, then both are stored correctly")
    void constructor_storesMessageAndPosition() {
        ParseError error = new ParseError("Unexpected token", 5);
        assertEquals("Unexpected token", error.getMessage());
        assertEquals(5, error.getPosition());
    }

    @Test
    @DisplayName("Given position zero, when constructing, then getPosition returns zero")
    void constructor_handlesZeroPosition() {
        ParseError error = new ParseError("Error at start", 0);
        assertEquals(0, error.getPosition());
    }

    @Test
    @DisplayName("Given a negative position, when constructing, then getPosition returns the negative value")
    void constructor_handlesNegativePosition() {
        ParseError error = new ParseError("Error", -1);
        assertEquals(-1, error.getPosition());
    }

    @Test
    @DisplayName("Given only a message, when constructing, then message is stored and position defaults to zero")
    void constructor_withMessageOnly_storesMessage() {
        ParseError error = new ParseError("Parse failure");
        assertEquals("Parse failure", error.getMessage());
        assertEquals(0, error.getPosition());
    }

    @Test
    @DisplayName("Given only a cause, when constructing, then cause is stored")
    void constructor_withCauseOnly_storesCause() {
        RuntimeException cause = new RuntimeException("root cause");
        ParseError error = new ParseError(cause);
        assertSame(cause, error.getCause());
    }

    @Test
    @DisplayName("Given a message and cause, when constructing, then both are stored")
    void constructor_withMessageAndCause_storesBoth() {
        RuntimeException cause = new RuntimeException("root cause");
        ParseError error = new ParseError("Parse failure", cause);
        assertEquals("Parse failure", error.getMessage());
        assertSame(cause, error.getCause());
    }

    @Test
    @DisplayName("ParseError is a RuntimeException")
    void parseError_isRuntimeException() {
        ParseError error = new ParseError("test", 1);
        assertInstanceOf(RuntimeException.class, error);
    }
}

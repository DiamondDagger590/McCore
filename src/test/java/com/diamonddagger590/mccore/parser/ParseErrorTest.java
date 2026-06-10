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
    void getPosition_returnsPosition_whenConstructedWithMessageAndPosition() {
        ParseError error = new ParseError("Unexpected token", 5);
        assertEquals("Unexpected token", error.getMessage());
        assertEquals(5, error.getPosition());
    }

    @Test
    @DisplayName("Given position zero, when constructing, then getPosition returns zero")
    void getPosition_returnsZero_whenConstructedWithZeroPosition() {
        ParseError error = new ParseError("Error at start", 0);
        assertEquals(0, error.getPosition());
    }

    @Test
    @DisplayName("Given a negative position, when constructing, then getPosition returns the negative value")
    void getPosition_returnsNegativeValue_whenConstructedWithNegativePosition() {
        ParseError error = new ParseError("Error", -1);
        assertEquals(-1, error.getPosition());
    }

    @Test
    @DisplayName("Given only a message, when constructing, then message is stored and position defaults to zero")
    void getMessage_returnsMessage_whenConstructedWithMessageOnly() {
        ParseError error = new ParseError("Parse failure");
        assertEquals("Parse failure", error.getMessage());
        assertEquals(0, error.getPosition());
    }

    @Test
    @DisplayName("Given only a cause, when constructing, then cause is stored and position defaults to zero")
    void getCause_returnsCause_whenConstructedWithCauseOnly() {
        RuntimeException cause = new RuntimeException("root cause");
        ParseError error = new ParseError(cause);
        assertSame(cause, error.getCause());
        assertEquals(0, error.getPosition());
    }

    @Test
    @DisplayName("Given a message and cause, when constructing, then both are stored and position defaults to zero")
    void getCause_returnsCause_whenConstructedWithMessageAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        ParseError error = new ParseError("Parse failure", cause);
        assertEquals("Parse failure", error.getMessage());
        assertSame(cause, error.getCause());
        assertEquals(0, error.getPosition());
    }

    @Test
    @DisplayName("Given a ParseError, when checking type, then it is a RuntimeException")
    void parseError_isRuntimeException_always() {
        ParseError error = new ParseError("test", 1);
        assertInstanceOf(RuntimeException.class, error);
    }
}

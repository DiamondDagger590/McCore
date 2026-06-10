package com.diamonddagger590.mccore.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class CoreDatabaseInitializationExceptionTest {

    @Test
    @DisplayName("Given no arguments, when constructing, then message is null")
    void getMessage_returnsNull_whenConstructedWithNoArgs() {
        CoreDatabaseInitializationException ex = new CoreDatabaseInitializationException();
        assertNull(ex.getMessage());
    }

    @Test
    @DisplayName("Given a message, when constructing, then message is stored")
    void getMessage_returnsMessage_whenConstructedWithMessage() {
        CoreDatabaseInitializationException ex = new CoreDatabaseInitializationException("Failed to init DB");
        assertEquals("Failed to init DB", ex.getMessage());
    }

    @Test
    @DisplayName("Given a CoreDatabaseInitializationException, when checking type, then it is a RuntimeException")
    void coreDatabaseInitializationException_isRuntimeException_always() {
        assertInstanceOf(RuntimeException.class, new CoreDatabaseInitializationException());
    }
}

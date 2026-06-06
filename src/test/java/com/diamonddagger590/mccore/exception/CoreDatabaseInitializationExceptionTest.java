package com.diamonddagger590.mccore.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class CoreDatabaseInitializationExceptionTest {

    @Test
    @DisplayName("Given no arguments, when constructing, then message is null")
    void constructor_noArgs_messageIsNull() {
        CoreDatabaseInitializationException ex = new CoreDatabaseInitializationException();
        assertNull(ex.getMessage());
    }

    @Test
    @DisplayName("Given a message, when constructing, then message is stored")
    void constructor_withMessage_storesMessage() {
        CoreDatabaseInitializationException ex = new CoreDatabaseInitializationException("Failed to init DB");
        assertEquals("Failed to init DB", ex.getMessage());
    }

    @Test
    @DisplayName("CoreDatabaseInitializationException is a RuntimeException")
    void coreDatabaseInitializationException_isRuntimeException() {
        assertInstanceOf(RuntimeException.class, new CoreDatabaseInitializationException());
    }
}

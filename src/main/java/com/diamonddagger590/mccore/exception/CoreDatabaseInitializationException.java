package com.diamonddagger590.mccore.exception;

import org.jetbrains.annotations.NotNull;

/**
 * This exception is fired whenever there is an issue initializing the core database.
 */
public class CoreDatabaseInitializationException extends RuntimeException {

    public CoreDatabaseInitializationException() {
        super();
    }

    public CoreDatabaseInitializationException(@NotNull String exceptionMessage) {
        super(exceptionMessage);
    }

}

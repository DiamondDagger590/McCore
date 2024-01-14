package com.diamonddagger590.mccore.exception;

import org.jetbrains.annotations.NotNull;

public class LockAlreadyHeldException extends RuntimeException {

    public LockAlreadyHeldException() {
        super();
    }

    public LockAlreadyHeldException(@NotNull String exceptionMessage) {
        super(exceptionMessage);
    }
}

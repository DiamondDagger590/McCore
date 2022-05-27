package com.diamonddagger590.mccore.exception;

import org.jetbrains.annotations.NotNull;

public class CoreDatabaseInitializationException extends RuntimeException{

    public CoreDatabaseInitializationException(){
        super();
    }

    public CoreDatabaseInitializationException(@NotNull String exceptionMessage){
        super(exceptionMessage);
    }

}

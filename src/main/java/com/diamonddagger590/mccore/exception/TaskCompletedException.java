package com.diamonddagger590.mccore.exception;

/**
 * This error is thrown whenever a {@link com.diamonddagger590.mccore.task.core.CoreTask} is executed whenever it has
 * already been executed once.
 */
public class TaskCompletedException extends RuntimeException {

    public TaskCompletedException(){
        super();
    }

    public TaskCompletedException(String errorMessage){
        super(errorMessage);
    }
}

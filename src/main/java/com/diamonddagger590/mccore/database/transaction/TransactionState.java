package com.diamonddagger590.mccore.database.transaction;

/**
 * Tracks the outcome of a {@link FailSafeTransaction} execution.
 * <p>
 * A transaction starts in {@link #PENDING} and transitions to either
 * {@link #COMMITTED} (all statements succeeded) or {@link #ROLLED_BACK}
 * (a statement failed and the transaction was rolled back).
 */
public enum TransactionState {

    /**
     * The transaction has not yet been executed.
     */
    PENDING,

    /**
     * All statements succeeded and the transaction was committed.
     */
    COMMITTED,

    /**
     * A statement failed and the transaction was rolled back.
     */
    ROLLED_BACK
}

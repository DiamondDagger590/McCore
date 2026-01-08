package com.diamonddagger590.mccore.database.response;

/**
 * Gets the response state of a given {@link GetItemRequest}.
 */
public enum GetItemResponseState {
    /**
     * This request is still pending a response.
     */
    PENDING_RESPONSE,
    /**
     * This request errored out.
     */
    ERRORED,
    /**
     * This request was able to find an item.
     * <p>
     * When this is the state, {@link GetItemRequest#getItem()} will
     * stop returning an empty {@link java.util.Optional}.
     */
    ITEM_FOUND,
    /**
     * The request was unable to find an item.
     */
    ITEM_NOT_FOUND,
}

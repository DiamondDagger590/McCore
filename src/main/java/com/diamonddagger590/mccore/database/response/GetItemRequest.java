package com.diamonddagger590.mccore.database.response;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * This wrapper allows for returning an immediate result on the main
 * thread while not forcing possible database reads to be on the main thread.
 * <p>
 * This is particularly useful when you want to immediately return a result
 * from a {@link com.diamonddagger590.mccore.registry.manager.Manager} that may
 * or may not have something cached. In the case it isn't cached, returning a response
 * immediately helps.
 * <p>
 * While a simple {@link CompletableFuture} could be returned, this allows
 * for a {@link GetItemResponseState} to be included for easier handling of the result
 * or periodic checking.
 * <p>
 * For example, you can continually check if something has been loaded on the main thread
 * every x ticks instead of needing to use the completable future pattern by checking if
 * {@link GetItemResponseState} is {@link GetItemResponseState#PENDING_RESPONSE}.
 * <p>
 * If the standard {@link CompletableFuture} pattern is desired, that is still
 * exposed via {@link #getItemCompletableFuture()} and maintains expected functionality.
 * It is worth noting however that the contents of the completable future may return null
 * if no item was found. Explicit null handling is handled in this class but if you choose
 * to use the completable future pattern, you will need to handle it yourself.
 *
 * @param <T> The item being returned in the response.
 */
public class GetItemRequest<T> {

    private final CompletableFuture<T> completableFuture;
    private volatile GetItemResponseState responseState;
    @Nullable
    private volatile T item;

    public GetItemRequest(@NotNull CompletableFuture<T> completableFuture) {
        this.completableFuture = completableFuture;
        this.responseState = GetItemResponseState.PENDING_RESPONSE;
        completableFuture.thenAccept(t -> {
            if (t != null) {
                this.item = t;
                this.responseState = GetItemResponseState.ITEM_FOUND;
            } else {
                this.responseState = GetItemResponseState.ITEM_NOT_FOUND;
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            this.responseState = GetItemResponseState.ERRORED;
            return null;
        });
    }

    /**
     * Gets an {@link Optional} containing the requested item if {@link #getItemResponseState()} equals
     * {@link GetItemResponseState#ITEM_FOUND}.
     *
     * @return An {@link Optional} containing the requested item only if
     * {@link #getItemResponseState()} equals {@link GetItemResponseState#ITEM_FOUND}.
     * Otherwise, the returned optional will be empty.
     */
    @NotNull
    public Optional<T> getItem() {
        return Optional.ofNullable(item);
    }

    /**
     * Gets the {@link CompletableFuture} that is being used
     * to update this response.
     * <p>
     * This completable future will finish with either the found item
     * or null in the case that the check did not error but did not find anything.
     *
     * @return The {@link CompletableFuture} being used
     * to update this response.
     */
    @NotNull
    public CompletableFuture<T> getItemCompletableFuture() {
        return completableFuture;
    }

    /**
     * Gets the {@link GetItemResponseState} representing the current
     * state of the request.
     *
     * @return The {@link GetItemResponseState} representing the current
     * state of the request.
     */
    @NotNull
    public GetItemResponseState getItemResponseState() {
        return responseState;
    }
}

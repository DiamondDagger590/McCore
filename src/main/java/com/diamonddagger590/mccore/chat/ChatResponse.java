package com.diamonddagger590.mccore.chat;

import org.bukkit.event.player.PlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A chat response allows for code to wait for a player to send a response
 * through a chat message and then to act on that response.
 * <p>
 * A player can only have one response active at once. If another response is added before
 * the previous one consumes a chat message, then {@link #onExpire()} will be called for the prior
 * response.
 */
public abstract class ChatResponse {

    private final UUID chatterUUID;

    public ChatResponse(@NotNull UUID chatterUUID) {
        this.chatterUUID = chatterUUID;
    }

    /**
     * Gets the {@link UUID} of the player whose response is pending.
     *
     * @return The {@link UUID} of the player whose response is pending.
     */
    @NotNull
    public final UUID getChatterUUID() {
        return chatterUUID;
    }

    /**
     * Gets the amount of time in seconds that this response will wait for before
     * expiring and calling {@link #onExpire()}.
     *
     * @return The amount of time in seconds that this response will wait for.
     */
    public abstract long getResponseWaitTime();

    /**
     * Handles responding to a {@link PlayerChatEvent} while this response is active.
     *
     * @param playerChatEvent The event that is being consumed by this response.
     */
    public abstract void onResponse(@NotNull PlayerChatEvent playerChatEvent);

    /**
     * Handles this response expiring, either as a result of the wait time elapsing or another
     * response being added for a player.
     */
    public abstract void onExpire();
}

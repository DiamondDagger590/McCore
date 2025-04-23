package com.diamonddagger590.mccore.chat;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.pair.ImmutablePair;
import com.diamonddagger590.mccore.pair.Pair;
import com.diamonddagger590.mccore.registry.manager.Manager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles managing and expiring {@link ChatResponse}s.
 */
public class ChatResponseManager extends Manager<CorePlugin> {

    private final Map<UUID, Pair<ChatResponse, ChatResponseExpireTask>> pendingResponses = new HashMap<>();

    public ChatResponseManager(@NotNull CorePlugin corePlugin) {
        super(corePlugin);
    }

    /**
     * Checks to see if the provided {@link UUID} has a pending {@link ChatResponse}.
     *
     * @param chatterUUID The {@link UUID} to check.
     * @return {@code true} if the provided {@link UUID} has a pending {@link ChatResponse}.
     */
    public boolean doesChatterHavePendingResponse(@NotNull UUID chatterUUID) {
        return pendingResponses.containsKey(chatterUUID);
    }

    /**
     * Gets an {@link Optional} containing the pending {@link ChatResponse} for the provided {@link UUID}.
     *
     * @param chatterUUID The {@link UUID} to get the response for.
     * @return An {@link Optional} containing the pending {@link ChatResponse} for the provided {@link UUID} or an
     * empty one if no matches were found.
     */
    @NotNull
    public Optional<ChatResponse> getPendingResponse(@NotNull UUID chatterUUID) {
        return Optional.ofNullable(doesChatterHavePendingResponse(chatterUUID) ? pendingResponses.get(chatterUUID).getLeft() : null);
    }

    /**
     * Adds the provided {@link ChatResponse} to be listened to while also starting a {@link ChatResponseExpireTask}
     * to automatically expire the response.
     *
     * @param chatterUUID  The {@link UUID} of the player to get a response from.
     * @param chatResponse The {@link ChatResponse} needing responded to.
     */
    public void addPendingResponse(@NotNull UUID chatterUUID, @NotNull ChatResponse chatResponse) {
        // Cancel existing expire task
        if (doesChatterHavePendingResponse(chatterUUID)) {
            var response = pendingResponses.remove(chatterUUID);
            response.getLeft().onExpire();
            response.getRight().cancelTask();
        }
        var pair = ImmutablePair.of(chatResponse, new ChatResponseExpireTask(plugin(), chatResponse));
        pendingResponses.put(chatterUUID, pair);
        // Add an expire task so if the chatter doesn't respond, then the response is removed
        pair.getRight().runTask();
    }

    /**
     * Expires an existing {@link ChatResponse} for the provided {@link UUID} if there is any.
     *
     * @param chatterUUID The {@link UUID} to expire the corresponding {@link ChatResponse} for.
     */
    public void removePendingResponse(@NotNull UUID chatterUUID) {
        if (pendingResponses.containsKey(chatterUUID)) {
            pendingResponses.remove(chatterUUID).getRight().cancelTask();
        }
    }

    /**
     * Expires the {@link ChatResponse} if it is currently waiting on a response from a player.
     *
     * @param chatResponse The {@link ChatResponse} to expire.
     */
    public void removePendingResponse(@NotNull ChatResponse chatResponse) {
        if (pendingResponses.containsKey(chatResponse.getChatterUUID())
                && pendingResponses.get(chatResponse.getChatterUUID()).getLeft() == chatResponse) {
            pendingResponses.remove(chatResponse.getChatterUUID()).getRight().cancelTask();
        }
    }
}

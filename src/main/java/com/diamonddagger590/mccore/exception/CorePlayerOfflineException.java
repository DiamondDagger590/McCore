package com.diamonddagger590.mccore.exception;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * An exception that gets thrown whenever a {@link CorePlayer} is offline
 * and code was expecting that player to be online.
 */
public class CorePlayerOfflineException extends RuntimeException {

    private final CorePlayer corePlayer;

    public CorePlayerOfflineException(@NotNull CorePlayer corePlayer) {
        this(corePlayer, String.format("CorePlayer with UUID %s is not online and was expected to be.", corePlayer.getUUID()));
    }

    public CorePlayerOfflineException(@NotNull CorePlayer corePlayer, @NotNull String message) {
        super(message);
        this.corePlayer = corePlayer;
    }

    /**
     * Gets the {@link CorePlayer} that was expected to be online
     * @return the {@link CorePlayer} that was expected to be online
     */
    @NotNull
    public CorePlayer getCorePlayer() {
        return corePlayer;
    }
}

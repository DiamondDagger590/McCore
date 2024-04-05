package com.diamonddagger590.mccore.event.player;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called whenever a {@link CorePlayer} is unloaded
 */
public class PlayerUnloadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final CorePlayer corePlayer;

    public PlayerUnloadEvent(@NotNull CorePlayer corePlayer) {
        this.corePlayer = corePlayer;
    }

    /**
     * Gets the {@link CorePlayer} that was unloaded.
     *
     * @return The {@link CorePlayer} that was unloaded.
     */
    public CorePlayer getCorePlayer() {
        return corePlayer;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}

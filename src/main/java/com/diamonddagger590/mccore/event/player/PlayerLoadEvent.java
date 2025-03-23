package com.diamonddagger590.mccore.event.player;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called whenever a {@link CorePlayer} is loaded
 */
public class PlayerLoadEvent extends CorePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    public PlayerLoadEvent(@NotNull CorePlayer corePlayer) {
        super(corePlayer);
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

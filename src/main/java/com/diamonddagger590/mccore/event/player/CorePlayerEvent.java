package com.diamonddagger590.mccore.event.player;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class CorePlayerEvent extends Event {

    private final CorePlayer corePlayer;

    public CorePlayerEvent(@NotNull CorePlayer corePlayer) {
        this.corePlayer = corePlayer;
    }

    @NotNull
    public CorePlayer getCorePlayer() {
        return corePlayer;
    }
}

package com.diamonddagger590.mccore.event.player;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class CorePlayerEvent extends Event {

    private final CorePlayer player;

    public CorePlayerEvent(@NotNull CorePlayer player) {
        this.player = player;
    }

    @NotNull
    public CorePlayer getPlayer() {
        return player;
    }
}

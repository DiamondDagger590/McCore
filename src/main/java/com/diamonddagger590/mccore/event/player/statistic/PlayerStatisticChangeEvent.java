package com.diamonddagger590.mccore.event.player.statistic;

import com.diamonddagger590.mccore.event.player.CorePlayerEvent;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerStatisticChangeEvent extends CorePlayerEvent {
    public PlayerStatisticChangeEvent(@NotNull CorePlayer player) {
        super(player);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return null;
    }
}

package com.diamonddagger590.mccore.event.setting.setting;

import com.diamonddagger590.mccore.event.player.CorePlayerEvent;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PlayerSettingChangeEvent extends CorePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    private final PlayerSetting oldSetting;
    private final PlayerSetting newSetting;

    public PlayerSettingChangeEvent(@NotNull CorePlayer corePlayer, @Nullable PlayerSetting oldSetting, @NotNull PlayerSetting newSetting) {
        super(corePlayer);
        this.oldSetting = oldSetting;
        this.newSetting = newSetting;
    }

    @NotNull
    public Optional<PlayerSetting> getOldSetting() {
        return Optional.ofNullable(oldSetting);
    }

    @NotNull
    public PlayerSetting getNewSetting() {
        return newSetting;
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

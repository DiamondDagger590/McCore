package com.diamonddagger590.mccore.exception.setting;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * This exception is thrown whenever a {@link com.diamonddagger590.mccore.setting.PlayerSetting} is expected
 * to be registered, but it isn't.
 */
public class SettingNotRegisteredException extends RuntimeException {

    private final NamespacedKey settingKey;

    public SettingNotRegisteredException(@NotNull NamespacedKey settingKey) {
        this.settingKey = settingKey;
    }

    /**
     * Gets the {@link NamespacedKey} of the {@link com.diamonddagger590.mccore.setting.PlayerSetting}
     * that caused the error.
     *
     * @return The {@link NamespacedKey} of the {@link com.diamonddagger590.mccore.setting.PlayerSetting}
     * that caused the error.
     */
    @NotNull
    public NamespacedKey getSettingKey() {
        return settingKey;
    }

    @Override
    public String getMessage() {
        return "A player setting with key " + settingKey + " was not found.";
    }
}

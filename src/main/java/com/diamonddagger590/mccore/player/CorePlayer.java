package com.diamonddagger590.mccore.player;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.event.setting.setting.PlayerSettingChangeEvent;
import com.diamonddagger590.mccore.external.common.AfkPluginHook;
import com.diamonddagger590.mccore.mutex.Mutexable;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.statistic.Statistic;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * An abstract class that represents a {@link Player} that contains data
 * used by plugins.
 * <p>
 * This class allows plugins to extend it and rely on the {@link Mutexable} states
 * in order to determine if data is properly loaded or not even in a cross-server environment.
 */
public abstract class CorePlayer extends Mutexable {

    private final UUID uuid;
    private final CorePlugin plugin;
    private final Map<NamespacedKey, PlayerSetting> playerSettings;
    private final Map<NamespacedKey, Statistic> statistics;

    public CorePlayer(@NotNull UUID uuid, @NotNull CorePlugin corePlugin) {
        this.uuid = uuid;
        this.plugin = corePlugin;
        this.playerSettings = new HashMap<>();
        this.statistics = new HashMap<>();
    }

    /**
     * Gets the {@link UUID} of this player.
     *
     * @return The {@link UUID} of this player.
     */
    @NotNull
    public UUID getUUID() {
        return uuid;
    }

    @NotNull
    public CorePlugin getPlugin() {
        return plugin;
    }

    /**
     * Gets this player as a Bukkit {@link Player}.
     *
     * @return An {@link Optional} that either contains the Bukkit {@link Player} or
     * will be empty if {@link Bukkit#getPlayer(UUID)} returns {@code null}.
     */
    @NotNull
    public Optional<Player> getAsBukkitPlayer() {
        return Optional.ofNullable(Bukkit.getPlayer(uuid));
    }

    /**
     * Checks to see if this player should utilize mutex or not.
     *
     * @return {@code true} if mutex should be used.
     */
    public abstract boolean useMutex();

    /**
     * Checks to see if this player is currently afk or not. This will only ever return {@code true}
     * if a supported plugin hook is registered and enabled on the server.
     *
     * @return {@code true} if this player is currently afk.
     */
    public boolean isAfk() {
        for (AfkPluginHook afkPluginHook : RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHooks(AfkPluginHook.class)) {
            if (afkPluginHook.isAfk(this)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the provided {@link PlayerSetting} as the current setting option for that setting type.
     *
     * @param playerSetting The {@link PlayerSetting} to set.
     */
    public void setPlayerSetting(@NotNull PlayerSetting playerSetting) {
        PlayerSetting oldSetting = playerSettings.get(playerSetting.getSettingKey());
        playerSettings.put(playerSetting.getSettingKey(), playerSetting);
        PlayerSettingChangeEvent playerSettingChangeEvent = new PlayerSettingChangeEvent(this, oldSetting, playerSetting);
        Bukkit.getPluginManager().callEvent(playerSettingChangeEvent);
    }

    /**
     * Gets an {@link Optional} containing the {@link PlayerSetting} that belongs to the provided {@link NamespacedKey},
     *
     * @param key The {@link NamespacedKey} to get the {@link PlayerSetting} for.
     * @return An {@link Optional} containing the {@link PlayerSetting} that belongs to the provided {@link NamespacedKey},
     * or empty if there is not a match.
     */
    @NotNull
    public Optional<? extends PlayerSetting> getPlayerSetting(@NotNull NamespacedKey key) {
        return Optional.ofNullable(playerSettings.get(key));
    }

    /**
     * Gets an {@link ImmutableSet} of all {@link PlayerSetting}s for this player.
     *
     * @return An {@link ImmutableSet} of all {@link PlayerSetting}s for this player.
     */
    @NotNull
    public Set<? extends PlayerSetting> getPlayerSettings() {
        return ImmutableSet.copyOf(playerSettings.values());
    }

    public void addStatistic(@NotNull Statistic statistic) {
        statistics.put(statistic.getType().getStatisticKey(), statistic);
    }

    @NotNull
    public Optional<Statistic> getStatistic(@NotNull NamespacedKey key) {
        return Optional.ofNullable(statistics.get(key));
    }

    @NotNull
    public Set<Statistic> getStatistics() {
        return ImmutableSet.copyOf(statistics.values());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + getUUID().hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CorePlayer) {
            CorePlayer corePlayer = (CorePlayer) obj;
            return corePlayer.getUUID().equals(getUUID());
        }
        return false;
    }

    @Override
    public String toString() {
        return "CorePlayer - [uuid=" + this.uuid + "]";
    }
}

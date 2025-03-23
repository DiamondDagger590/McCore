package com.diamonddagger590.mccore.player;

import com.diamonddagger590.mccore.mutex.Mutexable;
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
    private final Map<NamespacedKey, Statistic> statistics;

    public CorePlayer(@NotNull UUID uuid) {
        this.uuid = uuid;
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

    public abstract boolean useMutex();

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

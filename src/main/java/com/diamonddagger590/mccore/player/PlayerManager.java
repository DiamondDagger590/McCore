package com.diamonddagger590.mccore.player;

import com.diamonddagger590.mccore.CorePlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This class tracks all {@link CorePlayer CorePlayers} that are used by this plugin.
 * <p>
 * Implementations of {@link CorePlugin} should also have a corresponding implementation of
 * {@link CorePlayer} which they store in this class.
 */
public class PlayerManager {

    private final CorePlugin corePlugin;
    private final Map<UUID, CorePlayer> playerMap = new ConcurrentHashMap<>();

    public PlayerManager(@NotNull CorePlugin corePlugin) {
        this.corePlugin = corePlugin;
    }

    /**
     * Adds the provided {@link CorePlayer} to the player manager.
     *
     * @param corePlayer The {@link CorePlayer} to add to the manager.
     */
    public void addPlayer(@NotNull CorePlayer corePlayer) {
        playerMap.put(corePlayer.getUUID(), corePlayer);
    }

    /**
     * Removes the {@link CorePlayer} that corresponds to the provided {@link UUID} from the player manager.
     *
     * @param uuid The {@link UUID} who's corresponding {@link CorePlayer} will be removed.
     * @return An {@link Optional} containing the removed {@link CorePlayer} or an empty {@link Optional}
     * if nothing was removed.
     */
    public Optional<CorePlayer> removePlayer(@NotNull UUID uuid) {
        return Optional.ofNullable(playerMap.remove(uuid));
    }

    /**
     * Checks to see if the provided {@link UUID} has a corresponding {@link CorePlayer} that is
     * stored by the player manager.
     *
     * @param uuid The {@link UUID} of the {@link CorePlugin} to check.
     * @return {@code true} if the provided {@link UUID} has a corresponding {@link CorePlugin} stored.
     */
    public boolean hasCorePlayer(@NotNull UUID uuid) {
        return playerMap.containsKey(uuid);
    }

    /**
     * Gets the {@link CorePlayer} that corresponds to the provided {@link UUID}.
     *
     * @param uuid The {@link UUID} used to get the corresponding {@link CorePlayer}.
     * @return An {@link Optional} that will contain the corresponding {@link CorePlayer} or be empty
     * if the {@link UUID} is not stored.
     */
    public Optional<CorePlayer> getPlayer(@NotNull UUID uuid) {
        return Optional.ofNullable(playerMap.get(uuid));
    }

    /**
     * Gets all {@link CorePlayer CorePlayers} stored by this player manager.
     *
     * @return A cloned {@link Set} containing all the {@link CorePlayer CorePlayers} stored
     * by this player manager.
     */
    public Set<CorePlayer> getAllPlayers() {
        return new HashSet<>(playerMap.values());
    }

    /**
     * Gets a {@link Set} containing the {@link Player} version of all stored {@link CorePlayer CorePlayers}.
     *
     * @return A {@link Set} containing the {@link Player} version of all stored {@link CorePlayer CorePlayers}.
     */
    public Set<Player> getAllBukkitPlayers() {
        return playerMap.values().stream()
                   .filter(corePlayer -> corePlayer.getAsBukkitPlayer().isPresent())
                   .map(corePlayer -> corePlayer.getAsBukkitPlayer().get())
                   .collect(Collectors.toSet());
    }
}

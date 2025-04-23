package com.diamonddagger590.mccore.player;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.manager.Manager;
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
public class PlayerManager<C extends CorePlugin, P extends CorePlayer> extends Manager<C> {

    private final Map<UUID, P> playerMap = new ConcurrentHashMap<>();

    public PlayerManager(@NotNull C corePlugin) {
        super(corePlugin);
    }

    /**
     * Adds the provided {@link P} to the player manager.
     *
     * @param corePlayer The {@link P} to add to the manager.
     */
    public void addPlayer(@NotNull P corePlayer) {
        playerMap.put(corePlayer.getUUID(), corePlayer);
    }

    /**
     * Removes the {@link P} that corresponds to the provided {@link UUID} from the player manager.
     *
     * @param uuid The {@link UUID} who's corresponding {@link P} will be removed.
     * @return An {@link Optional} containing the removed {@link P} or an empty {@link Optional}
     * if nothing was removed.
     */
    public Optional<P> removePlayer(@NotNull UUID uuid) {
        return Optional.ofNullable(playerMap.remove(uuid));
    }

    /**
     * Checks to see if the provided {@link UUID} has a corresponding {@link P} that is
     * stored by the player manager.
     *
     * @param uuid The {@link UUID} of the {@link P} to check.
     * @return {@code true} if the provided {@link UUID} has a corresponding {@link P} stored.
     */
    public boolean hasCorePlayer(@NotNull UUID uuid) {
        return playerMap.containsKey(uuid);
    }

    /**
     * Gets the {@link P} that corresponds to the provided {@link UUID}.
     *
     * @param uuid The {@link UUID} used to get the corresponding {@link P}.
     * @return An {@link Optional} that will contain the corresponding {@link P} or be empty
     * if the {@link UUID} is not stored.
     */
    public Optional<P> getPlayer(@NotNull UUID uuid) {
        return Optional.ofNullable(playerMap.get(uuid));
    }

    /**
     * Check to see if the {@link P} associated with the
     * provided {@link UUID} is locked.
     *
     * @param uuid The {@link UUID} of the player to check
     * @return {@code true} if the associated {@link P} is locked.
     */
    public boolean isPlayerLocked(@NotNull UUID uuid) {
        if (playerMap.containsKey(uuid)) {
            return playerMap.get(uuid).isLocked();
        }
        return false;
    }

    /**
     * Gets all {@link P}s stored by this player manager.
     *
     * @return A cloned {@link Set} containing all the {@link P}s stored
     * by this player manager.
     */
    public Set<P> getAllPlayers() {
        return new HashSet<>(playerMap.values());
    }

    /**
     * Gets a {@link Set} containing the {@link Player} version of all stored {@link P}s.
     *
     * @return A {@link Set} containing the {@link Player} version of all stored {@link P}.
     */
    public Set<Player> getAllBukkitPlayers() {
        return playerMap.values().stream()
                .filter(corePlayer -> corePlayer.getAsBukkitPlayer().isPresent())
                .map(corePlayer -> corePlayer.getAsBukkitPlayer().get())
                .collect(Collectors.toSet());
    }
}

package com.diamonddagger590.mccore.gui;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * This class manages {@link Guiv2 Guis} for player's and tracks what guis are currently open.
 */
public class GuiTrackerv2 {

    private final CorePlugin plugin;
    private final Map<UUID, Guiv2> playersWithOpenGuis;
    private final Map<UUID, Set<UUID>> openGuis;

    public GuiTrackerv2(@NotNull CorePlugin plugin) {
        this.plugin = plugin;
        playersWithOpenGuis = new HashMap<>();
        openGuis = new HashMap<>();
    }

    /**
     * Checks to see if the provided {@link CorePlayer} has an open {@link Guiv2}.
     *
     * @param corePlayer The {@link CorePlayer} to check for.
     * @return {@code true} if the provided {@link CorePlayer} has an open {@link Guiv2}.
     */
    public boolean doesPlayerHaveGui(@NotNull CorePlayer corePlayer) {
        return doesPlayerHaveGui(corePlayer.getUUID());
    }

    /**
     * Checks to see if the provided {@link Player} has an open {@link Guiv2}.
     *
     * @param player The {@link Player} to check for.
     * @return {@code true} if the provided {@link Player} has an open {@link Guiv2}.
     */
    public boolean doesPlayerHaveGui(@NotNull Player player) {
        return doesPlayerHaveGui(player.getUniqueId());
    }

    /**
     * Checks to see if the provided {@link UUID} has an open {@link Guiv2}.
     *
     * @param uuid The {@link UUID} to check for.
     * @return {@code true} if the provided {@link UUID} has an open {@link Guiv2}.
     */
    public boolean doesPlayerHaveGui(@NotNull UUID uuid) {
        return playersWithOpenGuis.containsKey(uuid);
    }

    /**
     * Stops tracking any {@link Guiv2 Guis} for the provided {@link CorePlayer}, unregistering
     * any listeners if the gui has no viewers left after the player is removed.
     *
     * @param corePlayer The {@link CorePlayer} to stop tracking.
     */
    public void stopTrackingPlayer(@NotNull CorePlayer corePlayer) {
        stopTrackingPlayer(corePlayer.getUUID());
    }

    /**
     * Stops tracking any {@link Guiv2 Guis} for the provided {@link Player}, unregistering
     * any listeners if the gui has no viewers left after the player is removed.
     *
     * @param player The {@link Player} to stop tracking.
     */
    public void stopTrackingPlayer(@NotNull Player player) {
        stopTrackingPlayer(player.getUniqueId());
    }

    /**
     * Stops tracking any {@link Guiv2 Guis} for the provided {@link UUID}, unregistering
     * any listeners if the gui has no viewers left after the player is removed.
     *
     * @param uuid The {@link UUID} to stop tracking.
     */
    public void stopTrackingPlayer(@NotNull UUID uuid) {
        Guiv2 gui = playersWithOpenGuis.remove(uuid);
        if (gui != null) {
            UUID guiUUID = gui.getUUID();
            openGuis.get(guiUUID).remove(uuid);
            // If no other players are listening to this GUI
            if (openGuis.get(guiUUID).isEmpty()) {
                openGuis.remove(guiUUID);
                gui.unregisterListeners();
            }
        }
    }

    /**
     * Tracks the provided {@link Guiv2} for the provided {@link CorePlayer}, registering any listeners for
     * the gui if it isn't already registered.
     *
     * @param corePlayer The {@link CorePlayer} to track.
     * @param gui        The {@link Guiv2} to track.
     */
    public void trackPlayerGui(@NotNull CorePlayer corePlayer, @NotNull Guiv2 gui) {
        trackPlayerGui(corePlayer.getUUID(), gui);
    }

    /**
     * Tracks the provided {@link Guiv2} for the provided {@link Player}, registering any listeners for
     * the gui if it isn't already registered.
     *
     * @param player The {@link Player} to track.
     * @param gui    The {@link Guiv2} to track.
     */
    public void trackPlayerGui(@NotNull Player player, @NotNull Guiv2 gui) {
        trackPlayerGui(player.getUniqueId(), gui);
    }

    /**
     * Tracks the provided {@link Guiv2} for the provided {@link UUID}, registering any listeners for
     * the gui if it isn't already registered.
     *
     * @param uuid The {@link UUID} to track.
     * @param gui  The {@link Guiv2} to track.
     */
    public void trackPlayerGui(@NotNull UUID uuid, @NotNull Guiv2 gui) {
        // Check if the player is currently being tracked, if so then cancel before tracking them again
        if (playersWithOpenGuis.containsKey(uuid)) {
            stopTrackingPlayer(uuid);
        }
        playersWithOpenGuis.put(uuid, gui);
        if (!openGuis.containsKey(gui.getUUID())) {
            gui.registerListeners();
            Set<UUID> players = new HashSet<>();
            players.add(uuid);
            openGuis.put(gui.getUUID(), players);
        } else {
            openGuis.get(gui.getUUID()).add(uuid);
        }
    }

    /**
     * Gets an {@link Optional} containing the {@link Guiv2} that the provided {@link CorePlayer} is viewing
     * if there is any.
     *
     * @param corePlayer The {@link CorePlayer} to get the {@link Guiv2} for.
     * @return An {@link Optional} containing the {@link Guiv2} that the provided {@link CorePlayer} is viewing
     * if there is any.
     */
    @NotNull
    public Optional<Guiv2> getOpenedGui(@NotNull CorePlayer corePlayer) {
        return getOpenedGui(corePlayer.getUUID());
    }

    /**
     * Gets an {@link Optional} containing the {@link Guiv2} that the provided {@link Player} is viewing
     * if there is any.
     *
     * @param player The {@link Player} to get the {@link Guiv2} for.
     * @return An {@link Optional} containing the {@link Guiv2} that the provided {@link Player} is viewing
     * if there is any.
     */
    @NotNull
    public Optional<Guiv2> getOpenedGui(@NotNull Player player) {
        return Optional.ofNullable(playersWithOpenGuis.get(player.getUniqueId()));
    }

    /**
     * Gets an {@link Optional} containing the {@link Guiv2} that the provided {@link UUID} is viewing
     * if there is any.
     *
     * @param uuid The {@link UUID} to get the {@link Guiv2} for.
     * @return An {@link Optional} containing the {@link Guiv2} that the provided {@link UUID} is viewing
     * if there is any.
     */
    @NotNull
    public Optional<Guiv2> getOpenedGui(@NotNull UUID uuid) {
        return Optional.ofNullable(playersWithOpenGuis.get(uuid));
    }

    /**
     * Refreshes the provided {@link Guiv2} for all players viewing it.
     *
     * @param gui The {@link Guiv2} to refresh.
     */
    public void refreshGui(@NotNull Guiv2 gui) {
        for (UUID playerUUID : openGuis.get(gui.getUUID())) {
            Player player = plugin.getServer().getPlayer(playerUUID);
            if (player != null) {
                player.updateInventory();
            }
        }
    }
}
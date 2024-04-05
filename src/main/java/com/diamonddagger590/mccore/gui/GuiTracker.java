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

public class GuiTracker {

    private final CorePlugin plugin;
    private final Map<UUID, Gui> playersWithOpenGuis;
    private final Map<UUID, Set<UUID>> openGuis;

    public GuiTracker(@NotNull CorePlugin plugin) {
        this.plugin = plugin;
        playersWithOpenGuis = new HashMap<>();
        openGuis = new HashMap<>();
    }

    public boolean doesPlayerHaveGui(@NotNull CorePlayer corePlayer) {
        return doesPlayerHaveGui(corePlayer.getUUID());
    }

    public boolean doesPlayerHaveGui(@NotNull Player player) {
        return playersWithOpenGuis.containsKey(player.getUniqueId());
    }

    public boolean doesPlayerHaveGui(@NotNull UUID uuid) {
        return playersWithOpenGuis.containsKey(uuid);
    }

    public void stopTrackingPlayer(@NotNull CorePlayer corePlayer) {
        stopTrackingPlayer(corePlayer.getUUID());
    }

    public void stopTrackingPlayer(@NotNull Player player) {
        stopTrackingPlayer(player.getUniqueId());
    }

    public void stopTrackingPlayer(@NotNull UUID uuid) {
        Gui gui = playersWithOpenGuis.remove(uuid);
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

    public void trackPlayerGui(@NotNull CorePlayer corePlayer, @NotNull Gui gui) {
        trackPlayerGui(corePlayer.getUUID(), gui);
    }

    public void trackPlayerGui(@NotNull Player player, @NotNull Gui gui) {
        trackPlayerGui(player.getUniqueId(), gui);
    }

    public void trackPlayerGui(@NotNull UUID uuid, @NotNull Gui gui) {
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
        }
        else {
            openGuis.get(gui.getUUID()).add(uuid);
        }
    }

    @NotNull
    public Optional<Gui> getOpenedGui(@NotNull CorePlayer corePlayer) {
        return getOpenedGui(corePlayer.getUUID());
    }

    @NotNull
    public Optional<Gui> getOpenedGui(@NotNull Player player) {
        return Optional.ofNullable(playersWithOpenGuis.get(player.getUniqueId()));
    }

    public Optional<Gui> getOpenedGui(@NotNull UUID uuid) {
        return Optional.ofNullable(playersWithOpenGuis.get(uuid));
    }
}

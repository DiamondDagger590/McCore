package com.diamonddagger590.mccore.gui.slot.setting;

import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.CoreManagerKey;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A slot that displays a {@link PlayerSetting} and allows a player to click through all the different values
 * for the given setting.
 *
 * @param <T> The {@link PlayerSetting} represented by this slot.
 */
public abstract class PlayerSettingSlot<T extends PlayerSetting, P extends CorePlayer> implements Slot<P> {

    protected final P corePlayer;
    protected final Player player;
    private final T setting;

    public PlayerSettingSlot(@NotNull final P corePlayer, @NotNull final T setting) {
        this.corePlayer = corePlayer;
        Optional<Player> playerOptional = corePlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(corePlayer);
        }
        this.player = playerOptional.get();
        this.setting = setting;
    }

    @Override
    public boolean onClick(@NotNull P corePlayer, @NotNull ClickType clickType) {
        Optional<Gui<?>> guiOptional = corePlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(CoreManagerKey.CORE_GUI_MANAGER).getOpenedGui(corePlayer);
        guiOptional.ifPresent(gui -> {
            corePlayer.setPlayerSetting(setting.getNextSetting().getNodeValue());
            gui.refreshGUI();
        });
        return true;
    }

    /**
     * Gets the {@link PlayerSetting} represented by this slot.
     *
     * @return The {@link PlayerSetting} represented by this slot.
     */
    @NotNull
    public T getSetting() {
        return setting;
    }
}

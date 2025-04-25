package com.diamonddagger590.mccore.registry.manager;

import com.diamonddagger590.mccore.gui.GuiManager;
import com.diamonddagger590.mccore.player.PlayerManager;
import org.jetbrains.annotations.ApiStatus;

import static com.diamonddagger590.mccore.registry.manager.ManagerKeyImpl.create;

/**
 * Manager keys which are only intended for usage inside the core with the
 * intent that their implementations will be registered.
 */
public interface CoreManagerKey extends ManagerKey<Manager<?>> {

    @ApiStatus.Internal
    @SuppressWarnings("rawtypes")
    ManagerKey<GuiManager> CORE_GUI_MANAGER = create(GuiManager.class);
    @ApiStatus.Internal
    @SuppressWarnings("rawtypes")
    ManagerKey<PlayerManager> CORE_PLAYER_MANAGER = create(PlayerManager.class);
}

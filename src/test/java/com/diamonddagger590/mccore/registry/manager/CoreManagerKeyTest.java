package com.diamonddagger590.mccore.registry.manager;

import com.diamonddagger590.mccore.database.DatabaseManager;
import com.diamonddagger590.mccore.gui.GuiManager;
import com.diamonddagger590.mccore.player.PlayerManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CoreManagerKeyTest {

    @Test
    @DisplayName("Given CORE_GUI_MANAGER key, when checking manager class, then returns GuiManager class")
    void coreGuiManagerKey_returnsGuiManagerClass() {
        assertNotNull(CoreManagerKey.CORE_GUI_MANAGER);
        assertEquals(GuiManager.class, CoreManagerKey.CORE_GUI_MANAGER.managerClass());
    }

    @Test
    @DisplayName("Given CORE_PLAYER_MANAGER key, when checking manager class, then returns PlayerManager class")
    void corePlayerManagerKey_returnsPlayerManagerClass() {
        assertNotNull(CoreManagerKey.CORE_PLAYER_MANAGER);
        assertEquals(PlayerManager.class, CoreManagerKey.CORE_PLAYER_MANAGER.managerClass());
    }

    @Test
    @DisplayName("Given CORE_DATABASE_MANAGER key, when checking manager class, then returns DatabaseManager class")
    void coreDatabaseManagerKey_returnsDatabaseManagerClass() {
        assertNotNull(CoreManagerKey.CORE_DATABASE_MANAGER);
        assertEquals(DatabaseManager.class, CoreManagerKey.CORE_DATABASE_MANAGER.managerClass());
    }

    @Test
    @DisplayName("Given all core manager keys, when comparing, then all keys are distinct")
    void allCoreManagerKeys_areDistinct() {
        Set<ManagerKey<?>> keys = Set.of(
                CoreManagerKey.CORE_GUI_MANAGER,
                CoreManagerKey.CORE_PLAYER_MANAGER,
                CoreManagerKey.CORE_DATABASE_MANAGER
        );
        assertEquals(3, keys.size());
    }

    @Test
    @DisplayName("Given core manager keys, when comparing different keys, then they are not equal")
    void differentCoreManagerKeys_areNotEqual() {
        assertNotEquals(CoreManagerKey.CORE_GUI_MANAGER, CoreManagerKey.CORE_PLAYER_MANAGER);
        assertNotEquals(CoreManagerKey.CORE_GUI_MANAGER, CoreManagerKey.CORE_DATABASE_MANAGER);
        assertNotEquals(CoreManagerKey.CORE_PLAYER_MANAGER, CoreManagerKey.CORE_DATABASE_MANAGER);
    }
}

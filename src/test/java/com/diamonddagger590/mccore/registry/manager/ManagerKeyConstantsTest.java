package com.diamonddagger590.mccore.registry.manager;

import com.diamonddagger590.mccore.chat.ChatResponseManager;
import com.diamonddagger590.mccore.command.CoreCommandManager;
import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.database.DatabaseManager;
import com.diamonddagger590.mccore.gui.GuiManager;
import com.diamonddagger590.mccore.player.PlayerManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagerKeyConstantsTest {

    // --- ManagerKey constants ---

    @Test
    @DisplayName("Given ManagerKey.COMMAND, when managerClass called, then returns CoreCommandManager class")
    void managerKey_command_returnsCoreCommandManagerClass() {
        assertNotNull(ManagerKey.COMMAND);
        assertEquals(CoreCommandManager.class, ManagerKey.COMMAND.managerClass());
    }

    @Test
    @DisplayName("Given ManagerKey.RELOADABLE_CONTENT, when managerClass called, then returns ReloadableContentManager class")
    void managerKey_reloadableContent_returnsReloadableContentManagerClass() {
        assertNotNull(ManagerKey.RELOADABLE_CONTENT);
        assertEquals(ReloadableContentManager.class, ManagerKey.RELOADABLE_CONTENT.managerClass());
    }

    @Test
    @DisplayName("Given ManagerKey.CHAT_RESPONSE, when managerClass called, then returns ChatResponseManager class")
    void managerKey_chatResponse_returnsChatResponseManagerClass() {
        assertNotNull(ManagerKey.CHAT_RESPONSE);
        assertEquals(ChatResponseManager.class, ManagerKey.CHAT_RESPONSE.managerClass());
    }

    // --- CoreManagerKey constants ---

    @Test
    @DisplayName("Given CoreManagerKey.CORE_GUI_MANAGER, when managerClass called, then returns GuiManager class")
    void coreManagerKey_guiManager_returnsGuiManagerClass() {
        assertNotNull(CoreManagerKey.CORE_GUI_MANAGER);
        assertEquals(GuiManager.class, CoreManagerKey.CORE_GUI_MANAGER.managerClass());
    }

    @Test
    @DisplayName("Given CoreManagerKey.CORE_PLAYER_MANAGER, when managerClass called, then returns PlayerManager class")
    void coreManagerKey_playerManager_returnsPlayerManagerClass() {
        assertNotNull(CoreManagerKey.CORE_PLAYER_MANAGER);
        assertEquals(PlayerManager.class, CoreManagerKey.CORE_PLAYER_MANAGER.managerClass());
    }

    @Test
    @DisplayName("Given CoreManagerKey.CORE_DATABASE_MANAGER, when managerClass called, then returns DatabaseManager class")
    void coreManagerKey_databaseManager_returnsDatabaseManagerClass() {
        assertNotNull(CoreManagerKey.CORE_DATABASE_MANAGER);
        assertEquals(DatabaseManager.class, CoreManagerKey.CORE_DATABASE_MANAGER.managerClass());
    }

    // --- ManagerKeyImpl factory ---

    @Test
    @DisplayName("Given ManagerKeyImpl.create, when called with a class, then returned key holds that class")
    void managerKeyImpl_create_holdsProvidedClass() {
        ManagerKey<GuiManager> key = ManagerKeyImpl.create(GuiManager.class);
        assertNotNull(key);
        assertEquals(GuiManager.class, key.managerClass());
    }

    @Test
    @DisplayName("Given two ManagerKeyImpl instances for the same class, when comparing, then they are equal via record equality")
    void managerKeyImpl_equalityBasedOnClass() {
        ManagerKey<GuiManager> key1 = ManagerKeyImpl.create(GuiManager.class);
        ManagerKey<GuiManager> key2 = ManagerKeyImpl.create(GuiManager.class);
        assertEquals(key1, key2);
    }
}

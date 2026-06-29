package com.diamonddagger590.mccore.registry.manager;

import com.diamonddagger590.mccore.chat.ChatResponseManager;
import com.diamonddagger590.mccore.command.CoreCommandManager;
import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagerKeyTest {

    @Test
    @DisplayName("Given COMMAND key, when managerClass called, then returns CoreCommandManager class")
    void command_managerClass_returnsCoreCommandManagerClass() {
        assertNotNull(ManagerKey.COMMAND);
        assertEquals(CoreCommandManager.class, ManagerKey.COMMAND.managerClass());
    }

    @Test
    @DisplayName("Given RELOADABLE_CONTENT key, when managerClass called, then returns ReloadableContentManager class")
    void reloadableContent_managerClass_returnsReloadableContentManagerClass() {
        assertNotNull(ManagerKey.RELOADABLE_CONTENT);
        assertEquals(ReloadableContentManager.class, ManagerKey.RELOADABLE_CONTENT.managerClass());
    }

    @Test
    @DisplayName("Given CHAT_RESPONSE key, when managerClass called, then returns ChatResponseManager class")
    void chatResponse_managerClass_returnsChatResponseManagerClass() {
        assertNotNull(ManagerKey.CHAT_RESPONSE);
        assertEquals(ChatResponseManager.class, ManagerKey.CHAT_RESPONSE.managerClass());
    }
}

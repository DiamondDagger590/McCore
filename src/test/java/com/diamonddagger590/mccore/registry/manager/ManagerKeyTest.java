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
    @DisplayName("Given the COMMAND key constant, when managerClass called, then returns CoreCommandManager class")
    void managerClass_returnsCoreCommandManagerClass_whenKeyIsCommand() {
        assertNotNull(ManagerKey.COMMAND);
        assertEquals(CoreCommandManager.class, ManagerKey.COMMAND.managerClass());
    }

    @Test
    @DisplayName("Given the RELOADABLE_CONTENT key constant, when managerClass called, then returns ReloadableContentManager class")
    void managerClass_returnsReloadableContentManagerClass_whenKeyIsReloadableContent() {
        assertNotNull(ManagerKey.RELOADABLE_CONTENT);
        assertEquals(ReloadableContentManager.class, ManagerKey.RELOADABLE_CONTENT.managerClass());
    }

    @Test
    @DisplayName("Given the CHAT_RESPONSE key constant, when managerClass called, then returns ChatResponseManager class")
    void managerClass_returnsChatResponseManagerClass_whenKeyIsChatResponse() {
        assertNotNull(ManagerKey.CHAT_RESPONSE);
        assertEquals(ChatResponseManager.class, ManagerKey.CHAT_RESPONSE.managerClass());
    }
}

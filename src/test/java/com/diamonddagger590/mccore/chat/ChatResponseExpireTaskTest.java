package com.diamonddagger590.mccore.chat;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import org.bukkit.event.player.PlayerChatEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.jetbrains.annotations.NotNull;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatResponseExpireTaskTest {

    private TestCorePlugin plugin;

    private static class TestChatResponse extends ChatResponse {

        private boolean expired = false;
        private final long waitTime;

        TestChatResponse(@NotNull UUID chatterUUID, long waitTime) {
            super(chatterUUID);
            this.waitTime = waitTime;
        }

        @Override
        public long getResponseWaitTime() {
            return waitTime;
        }

        @Override
        public void onResponse(@NotNull PlayerChatEvent playerChatEvent) {
        }

        @Override
        public void onExpire() {
            expired = true;
        }

        boolean hasExpired() {
            return expired;
        }
    }

    /**
     * Subclass that exposes {@link ChatResponseExpireTask#onTaskExpire()} for direct testing.
     */
    private static class TestableChatResponseExpireTask extends ChatResponseExpireTask {

        TestableChatResponseExpireTask(@NotNull TestCorePlugin plugin, @NotNull ChatResponse chatResponse) {
            super(plugin, chatResponse);
        }

        void invokeOnTaskExpire() {
            onTaskExpire();
        }
    }

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(TestCorePlugin.class);
        RegistryResetExtension.setupRegistry();
        ChatResponseManager manager = new ChatResponseManager(plugin);
        plugin.registryAccess().registry(RegistryKey.MANAGER).register(manager);
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Given a valid plugin and chat response, when constructing, then succeeds")
    void constructor_succeeds_withValidArgs() {
        UUID uuid = UUID.randomUUID();
        TestChatResponse response = new TestChatResponse(uuid, 30);
        ChatResponseExpireTask task = new ChatResponseExpireTask(plugin, response);
        assertNotNull(task);
    }

    @Test
    @DisplayName("Given a chat response with wait time, when constructing, then max duration is set")
    void constructor_setsMaxDuration_fromResponseWaitTime() {
        UUID uuid = UUID.randomUUID();
        TestChatResponse response = new TestChatResponse(uuid, 60);
        ChatResponseExpireTask task = new ChatResponseExpireTask(plugin, response);
        assertTrue(task.getMaxTaskDuration() > 0);
    }

    @Test
    @DisplayName("Given a pending response, when onTaskExpire is invoked, then response is removed from manager")
    void onTaskExpire_removesPendingResponse_fromManager() {
        UUID uuid = UUID.randomUUID();
        TestChatResponse response = new TestChatResponse(uuid, 30);

        ChatResponseManager manager = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(ManagerKey.CHAT_RESPONSE);

        manager.addPendingResponse(uuid, response);
        assertTrue(manager.doesChatterHavePendingResponse(uuid));

        TestableChatResponseExpireTask task = new TestableChatResponseExpireTask(plugin, response);
        task.invokeOnTaskExpire();

        assertFalse(manager.doesChatterHavePendingResponse(uuid));
    }

    @Test
    @DisplayName("Given no pending response, when onTaskExpire is invoked, then no exception is thrown")
    void onTaskExpire_succeedsGracefully_whenNoPendingResponse() {
        UUID uuid = UUID.randomUUID();
        TestChatResponse response = new TestChatResponse(uuid, 30);
        TestableChatResponseExpireTask task = new TestableChatResponseExpireTask(plugin, response);
        task.invokeOnTaskExpire();
    }
}

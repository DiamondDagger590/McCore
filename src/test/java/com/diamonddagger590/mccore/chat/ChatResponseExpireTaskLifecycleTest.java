package com.diamonddagger590.mccore.chat;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import org.bukkit.event.player.PlayerChatEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ChatResponseExpireTaskLifecycleTest {

    private TestCorePlugin plugin;

    private static class TestChatResponse extends ChatResponse {
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
        }
    }

    private static class FullyTestableChatResponseExpireTask extends ChatResponseExpireTask {

        FullyTestableChatResponseExpireTask(@NotNull TestCorePlugin plugin, @NotNull ChatResponse chatResponse) {
            super(plugin, chatResponse);
        }

        void invokeOnCancel() {
            onCancel();
        }

        void invokeOnDelayComplete() {
            onDelayComplete();
        }

        void invokeOnIntervalStart() {
            onIntervalStart();
        }

        void invokeOnIntervalComplete() {
            onIntervalComplete();
        }

        void invokeOnIntervalPause() {
            onIntervalPause();
        }

        void invokeOnIntervalResume() {
            onIntervalResume();
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
    @DisplayName("Given a ChatResponseExpireTask, when onCancel is invoked, then no exception is thrown")
    void onCancel_doesNotThrow_whenInvoked() {
        FullyTestableChatResponseExpireTask task = createTask();
        assertDoesNotThrow(task::invokeOnCancel);
    }

    @Test
    @DisplayName("Given a ChatResponseExpireTask, when onDelayComplete is invoked, then no exception is thrown")
    void onDelayComplete_doesNotThrow_whenInvoked() {
        FullyTestableChatResponseExpireTask task = createTask();
        assertDoesNotThrow(task::invokeOnDelayComplete);
    }

    @Test
    @DisplayName("Given a ChatResponseExpireTask, when onIntervalStart is invoked, then no exception is thrown")
    void onIntervalStart_doesNotThrow_whenInvoked() {
        FullyTestableChatResponseExpireTask task = createTask();
        assertDoesNotThrow(task::invokeOnIntervalStart);
    }

    @Test
    @DisplayName("Given a ChatResponseExpireTask, when onIntervalComplete is invoked, then no exception is thrown")
    void onIntervalComplete_doesNotThrow_whenInvoked() {
        FullyTestableChatResponseExpireTask task = createTask();
        assertDoesNotThrow(task::invokeOnIntervalComplete);
    }

    @Test
    @DisplayName("Given a ChatResponseExpireTask, when onIntervalPause is invoked, then no exception is thrown")
    void onIntervalPause_doesNotThrow_whenInvoked() {
        FullyTestableChatResponseExpireTask task = createTask();
        assertDoesNotThrow(task::invokeOnIntervalPause);
    }

    @Test
    @DisplayName("Given a ChatResponseExpireTask, when onIntervalResume is invoked, then no exception is thrown")
    void onIntervalResume_doesNotThrow_whenInvoked() {
        FullyTestableChatResponseExpireTask task = createTask();
        assertDoesNotThrow(task::invokeOnIntervalResume);
    }

    private FullyTestableChatResponseExpireTask createTask() {
        UUID uuid = UUID.randomUUID();
        TestChatResponse response = new TestChatResponse(uuid, 30);
        return new FullyTestableChatResponseExpireTask(plugin, response);
    }
}

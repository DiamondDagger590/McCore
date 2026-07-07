package com.diamonddagger590.mccore.chat;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.util.TimeProvider;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ChatResponseManagerTest {

    private static class TestChatResponse extends ChatResponse {

        private final long waitTime;
        private boolean expired = false;

        TestChatResponse(UUID chatterUUID, long waitTime) {
            super(chatterUUID);
            this.waitTime = waitTime;
        }

        @Override
        public long getResponseWaitTime() {
            return waitTime;
        }

        @Override
        public void onResponse(PlayerChatEvent playerChatEvent) {
        }

        @Override
        public void onExpire() {
            expired = true;
        }

        boolean isExpired() {
            return expired;
        }
    }

    private CorePlugin mockPlugin;
    private ChatResponseManager manager;
    private MockedStatic<Bukkit> bukkitMock;

    @BeforeEach
    void setUp() {
        mockPlugin = mock(CorePlugin.class);
        TimeProvider timeProvider = mock(TimeProvider.class);
        when(timeProvider.now()).thenReturn(Instant.ofEpochMilli(1000));
        when(mockPlugin.getTimeProvider()).thenReturn(timeProvider);

        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        BukkitTask task = mock(BukkitTask.class);
        when(task.getTaskId()).thenReturn(1);
        when(scheduler.runTaskTimer(any(), any(Runnable.class), anyLong(), anyLong())).thenReturn(task);
        when(scheduler.runTask(any(), any(Runnable.class))).thenReturn(task);

        bukkitMock = mockStatic(Bukkit.class);
        bukkitMock.when(Bukkit::getScheduler).thenReturn(scheduler);

        Server server = mock(Server.class);
        bukkitMock.when(Bukkit::getServer).thenReturn(server);

        manager = new ChatResponseManager(mockPlugin);
    }

    @AfterEach
    void tearDown() {
        bukkitMock.close();
    }

    @Test
    @DisplayName("Given no pending responses, when checking doesChatterHavePendingResponse, then returns false")
    void doesChatterHavePendingResponse_returnsFalse_whenNoPending() {
        assertFalse(manager.doesChatterHavePendingResponse(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Given a pending response, when checking doesChatterHavePendingResponse, then returns true")
    void doesChatterHavePendingResponse_returnsTrue_afterAdding() {
        UUID uuid = UUID.randomUUID();
        manager.addPendingResponse(uuid, new TestChatResponse(uuid, 30));
        assertTrue(manager.doesChatterHavePendingResponse(uuid));
    }

    @Test
    @DisplayName("Given no pending responses, when getPendingResponse, then returns empty")
    void getPendingResponse_returnsEmpty_whenNoPending() {
        assertTrue(manager.getPendingResponse(UUID.randomUUID()).isEmpty());
    }

    @Test
    @DisplayName("Given a pending response, when getPendingResponse, then returns it")
    void getPendingResponse_returnsResponse_afterAdding() {
        UUID uuid = UUID.randomUUID();
        TestChatResponse response = new TestChatResponse(uuid, 30);
        manager.addPendingResponse(uuid, response);

        Optional<ChatResponse> result = manager.getPendingResponse(uuid);
        assertTrue(result.isPresent());
        assertEquals(response, result.get());
    }

    @Test
    @DisplayName("Given a pending response, when adding a new one for same UUID, then old one is expired")
    void addPendingResponse_expiresOldResponse_whenReplacingForSameUUID() {
        UUID uuid = UUID.randomUUID();
        TestChatResponse first = new TestChatResponse(uuid, 30);
        TestChatResponse second = new TestChatResponse(uuid, 60);

        manager.addPendingResponse(uuid, first);
        manager.addPendingResponse(uuid, second);

        assertTrue(first.isExpired());
        assertEquals(second, manager.getPendingResponse(uuid).orElseThrow());
    }

    @Test
    @DisplayName("Given a pending response, when removePendingResponse by UUID, then it is removed")
    void removePendingResponse_byUUID_removesResponse() {
        UUID uuid = UUID.randomUUID();
        manager.addPendingResponse(uuid, new TestChatResponse(uuid, 30));
        manager.removePendingResponse(uuid);
        assertFalse(manager.doesChatterHavePendingResponse(uuid));
    }

    @Test
    @DisplayName("Given no pending response, when removePendingResponse by UUID, then no error")
    void removePendingResponse_byUUID_noErrorWhenNoPending() {
        UUID uuid = UUID.randomUUID();
        assertDoesNotThrow(() -> manager.removePendingResponse(uuid));
        assertFalse(manager.doesChatterHavePendingResponse(uuid));
    }

    @Test
    @DisplayName("Given a pending response, when removePendingResponse by ChatResponse instance, then it is removed")
    void removePendingResponse_byChatResponse_removesMatchingResponse() {
        UUID uuid = UUID.randomUUID();
        TestChatResponse response = new TestChatResponse(uuid, 30);
        manager.addPendingResponse(uuid, response);
        manager.removePendingResponse(response);
        assertFalse(manager.doesChatterHavePendingResponse(uuid));
    }

    @Test
    @DisplayName("Given a pending response, when removePendingResponse by different ChatResponse instance, then original remains")
    void removePendingResponse_byChatResponse_doesNotRemoveDifferentInstance() {
        UUID uuid = UUID.randomUUID();
        TestChatResponse stored = new TestChatResponse(uuid, 30);
        TestChatResponse other = new TestChatResponse(uuid, 60);

        manager.addPendingResponse(uuid, stored);
        manager.removePendingResponse(other);

        assertTrue(manager.doesChatterHavePendingResponse(uuid));
        assertEquals(stored, manager.getPendingResponse(uuid).orElseThrow());
    }

    @Test
    @DisplayName("Given no pending response, when removePendingResponse by ChatResponse, then no error")
    void removePendingResponse_byChatResponse_noErrorWhenNoPending() {
        UUID uuid = UUID.randomUUID();
        assertDoesNotThrow(() -> manager.removePendingResponse(new TestChatResponse(uuid, 30)));
        assertFalse(manager.doesChatterHavePendingResponse(uuid));
    }

    @Test
    @DisplayName("Given multiple responses for different UUIDs, when removing one, then other remains")
    void removePendingResponse_doesNotAffectOtherUUIDs() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        TestChatResponse response1 = new TestChatResponse(uuid1, 30);
        TestChatResponse response2 = new TestChatResponse(uuid2, 30);

        manager.addPendingResponse(uuid1, response1);
        manager.addPendingResponse(uuid2, response2);
        manager.removePendingResponse(uuid1);

        assertFalse(manager.doesChatterHavePendingResponse(uuid1));
        assertTrue(manager.doesChatterHavePendingResponse(uuid2));
    }
}

package com.diamonddagger590.mccore.chat;

import org.bukkit.event.player.PlayerChatEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ChatResponseTest {

    private static class TestChatResponse extends ChatResponse {

        private final long waitTime;
        private boolean expired = false;
        private PlayerChatEvent lastEvent = null;

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
            lastEvent = playerChatEvent;
        }

        @Override
        public void onExpire() {
            expired = true;
        }

        boolean isExpired() {
            return expired;
        }

        PlayerChatEvent getLastEvent() {
            return lastEvent;
        }
    }

    @Test
    @DisplayName("Given a UUID, when constructing, then getChatterUUID returns it")
    void getChatterUUID_returnsConstructorUUID() {
        UUID uuid = UUID.randomUUID();
        TestChatResponse response = new TestChatResponse(uuid, 30);
        assertEquals(uuid, response.getChatterUUID());
    }

    @Test
    @DisplayName("Given a UUID, when getChatterUUID called multiple times, then returns same instance")
    void getChatterUUID_returnsSameInstance() {
        UUID uuid = UUID.randomUUID();
        TestChatResponse response = new TestChatResponse(uuid, 30);
        assertNotNull(response.getChatterUUID());
        assertEquals(response.getChatterUUID(), response.getChatterUUID());
    }

    @Test
    @DisplayName("Given a wait time, when getResponseWaitTime, then returns configured value")
    void getResponseWaitTime_returnsConfiguredValue() {
        TestChatResponse response = new TestChatResponse(UUID.randomUUID(), 60);
        assertEquals(60, response.getResponseWaitTime());
    }

    @Test
    @DisplayName("Given a response, when onExpire called, then expiration is tracked")
    void onExpire_tracksExpiration() {
        TestChatResponse response = new TestChatResponse(UUID.randomUUID(), 30);
        response.onExpire();
        assertEquals(true, response.isExpired());
    }

    @Test
    @DisplayName("Given two responses with different UUIDs, then they have distinct UUIDs")
    void differentResponses_haveDifferentUUIDs() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        TestChatResponse r1 = new TestChatResponse(uuid1, 30);
        TestChatResponse r2 = new TestChatResponse(uuid2, 30);
        assertEquals(uuid1, r1.getChatterUUID());
        assertEquals(uuid2, r2.getChatterUUID());
    }
}

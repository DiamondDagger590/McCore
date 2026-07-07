package com.diamonddagger590.mccore.event.setting.setting;

import com.diamonddagger590.mccore.event.player.PlayerLoadEvent;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.util.LinkedNode;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerSettingChangeEventTest {

    private static class TestCorePlayer extends CorePlayer {
        TestCorePlayer(UUID uuid) {
            super(uuid, null);
        }

        @Override
        public boolean useMutex() {
            return false;
        }
    }

    private static class TestPlayerSetting implements PlayerSetting {
        private final String settingName;
        private final NamespacedKey key;

        TestPlayerSetting(String settingName) {
            this.settingName = settingName;
            this.key = new NamespacedKey("test", settingName.toLowerCase());
        }

        @Override
        public NamespacedKey getSettingKey() {
            return key;
        }

        @Override
        public LinkedNode<? extends PlayerSetting> getFirstSetting() {
            return new LinkedNode<>(this);
        }

        @Override
        public LinkedNode<? extends PlayerSetting> getNextSetting() {
            return new LinkedNode<>(this);
        }

        @Override
        public void onSettingChange(CorePlayer player, Optional<PlayerSetting> oldSetting) {
        }

        @Override
        public Optional<? extends PlayerSetting> fromString(String setting) {
            return Optional.empty();
        }

        @Override
        public String name() {
            return settingName;
        }
    }

    private final UUID testUUID = UUID.randomUUID();
    private final TestCorePlayer testPlayer = new TestCorePlayer(testUUID);

    @Test
    @DisplayName("Given old and new settings, when creating event, then getCorePlayer returns the player")
    void constructor_getCorePlayer_returnsProvidedPlayer() {
        TestPlayerSetting oldSetting = new TestPlayerSetting("OLD");
        TestPlayerSetting newSetting = new TestPlayerSetting("NEW");
        PlayerSettingChangeEvent event = new PlayerSettingChangeEvent(testPlayer, oldSetting, newSetting);
        assertSame(testPlayer, event.getCorePlayer());
    }

    @Test
    @DisplayName("Given a non-null old setting, when calling getOldSetting, then returns present Optional")
    void getOldSetting_returnsPresent_whenOldSettingIsNotNull() {
        TestPlayerSetting oldSetting = new TestPlayerSetting("OLD");
        TestPlayerSetting newSetting = new TestPlayerSetting("NEW");
        PlayerSettingChangeEvent event = new PlayerSettingChangeEvent(testPlayer, oldSetting, newSetting);
        assertTrue(event.getOldSetting().isPresent());
        assertSame(oldSetting, event.getOldSetting().get());
    }

    @Test
    @DisplayName("Given a null old setting, when calling getOldSetting, then returns empty Optional")
    void getOldSetting_returnsEmpty_whenOldSettingIsNull() {
        TestPlayerSetting newSetting = new TestPlayerSetting("NEW");
        PlayerSettingChangeEvent event = new PlayerSettingChangeEvent(testPlayer, null, newSetting);
        assertTrue(event.getOldSetting().isEmpty());
    }

    @Test
    @DisplayName("Given a new setting, when calling getNewSetting, then returns the same setting")
    void getNewSetting_returnsSameInstance() {
        TestPlayerSetting newSetting = new TestPlayerSetting("NEW");
        PlayerSettingChangeEvent event = new PlayerSettingChangeEvent(testPlayer, null, newSetting);
        assertSame(newSetting, event.getNewSetting());
    }

    @Test
    @DisplayName("Given a PlayerSettingChangeEvent, when calling getHandlers, then returns non-null HandlerList")
    void getHandlers_returnsNonNull() {
        TestPlayerSetting newSetting = new TestPlayerSetting("NEW");
        PlayerSettingChangeEvent event = new PlayerSettingChangeEvent(testPlayer, null, newSetting);
        assertNotNull(event.getHandlers());
    }

    @Test
    @DisplayName("Given a PlayerSettingChangeEvent, when calling getHandlerList, then returns non-null HandlerList")
    void getHandlerList_returnsNonNull() {
        assertNotNull(PlayerSettingChangeEvent.getHandlerList());
    }

    @Test
    @DisplayName("Given a PlayerSettingChangeEvent, when comparing handler lists, then instance and static return same")
    void handlersAndHandlerList_areSameInstance() {
        TestPlayerSetting newSetting = new TestPlayerSetting("NEW");
        PlayerSettingChangeEvent event = new PlayerSettingChangeEvent(testPlayer, null, newSetting);
        assertSame(event.getHandlers(), PlayerSettingChangeEvent.getHandlerList());
    }

    @Test
    @DisplayName("Given PlayerSettingChangeEvent and PlayerLoadEvent, when getting handler lists, then they differ")
    void handlerList_isDifferentFromOtherEventTypes() {
        assertNotSame(PlayerSettingChangeEvent.getHandlerList(), PlayerLoadEvent.getHandlerList());
    }
}

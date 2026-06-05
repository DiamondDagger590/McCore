package com.diamonddagger590.mccore.setting;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.util.LinkedNode;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerSettingRegistryTest {

    @SuppressWarnings("deprecation")
    private static NamespacedKey key(String namespace, String key) {
        return new NamespacedKey(namespace, key);
    }

    private enum TestSetting implements PlayerSetting {

        OPTION_A(key("test", "toggle")),
        OPTION_B(key("test", "toggle"));

        private final NamespacedKey settingKey;
        private final LinkedNode<TestSetting> node;

        TestSetting(NamespacedKey settingKey) {
            this.settingKey = settingKey;
            this.node = new LinkedNode<>(this);
        }

        static {
            OPTION_A.node.setNext(OPTION_B.node);
            OPTION_B.node.setNext(OPTION_A.node);
        }

        @Override
        public @NotNull NamespacedKey getSettingKey() {
            return settingKey;
        }

        @Override
        public @NotNull LinkedNode<TestSetting> getFirstSetting() {
            return OPTION_A.node;
        }

        @Override
        public @NotNull LinkedNode<TestSetting> getNextSetting() {
            return node.getNextNode();
        }

        @Override
        public void onSettingChange(@NotNull CorePlayer player, @NotNull Optional<PlayerSetting> oldSetting) {
        }

        @Override
        public @NotNull Optional<TestSetting> fromString(@NotNull String setting) {
            try {
                return Optional.of(TestSetting.valueOf(setting));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
    }

    private enum OtherSetting implements PlayerSetting {

        VALUE(key("test", "other"));

        private final NamespacedKey settingKey;
        private final LinkedNode<OtherSetting> node;

        OtherSetting(NamespacedKey settingKey) {
            this.settingKey = settingKey;
            this.node = new LinkedNode<>(this);
        }

        @Override
        public @NotNull NamespacedKey getSettingKey() {
            return settingKey;
        }

        @Override
        public @NotNull LinkedNode<OtherSetting> getFirstSetting() {
            return node;
        }

        @Override
        public @NotNull LinkedNode<OtherSetting> getNextSetting() {
            return node;
        }

        @Override
        public void onSettingChange(@NotNull CorePlayer player, @NotNull Optional<PlayerSetting> oldSetting) {
        }

        @Override
        public @NotNull Optional<OtherSetting> fromString(@NotNull String setting) {
            try {
                return Optional.of(OtherSetting.valueOf(setting));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
    }

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
    }

    private PlayerSettingRegistry registry() {
        return RegistryAccess.registryAccess().registry(RegistryKey.PLAYER_SETTING);
    }

    @Test
    @DisplayName("Given a new player setting, when registering, then registration succeeds")
    void register_succeeds_whenSettingIsNew() {
        registry().register(TestSetting.OPTION_A);
        assertTrue(registry().registered(TestSetting.OPTION_A));
    }

    @Test
    @DisplayName("Given a registered setting, when checking registered, then returns true")
    void registered_returnsTrue_whenSettingIsRegistered() {
        registry().register(TestSetting.OPTION_A);
        assertTrue(registry().registered(TestSetting.OPTION_A));
    }

    @Test
    @DisplayName("Given no registered settings, when checking registered, then returns false")
    void registered_returnsFalse_whenSettingIsNotRegistered() {
        assertFalse(registry().registered(TestSetting.OPTION_A));
    }

    @Test
    @DisplayName("Given a registered key, when checking isRegistered by key, then returns true")
    void isRegistered_returnsTrue_whenKeyIsRegistered() {
        registry().register(TestSetting.OPTION_A);
        assertTrue(registry().isRegistered(key("test", "toggle")));
    }

    @Test
    @DisplayName("Given an unregistered key, when checking isRegistered by key, then returns false")
    void isRegistered_returnsFalse_whenKeyIsNotRegistered() {
        assertFalse(registry().isRegistered(key("test", "toggle")));
    }

    @Test
    @DisplayName("Given a registered setting, when getting by key, then returns the setting")
    void getSetting_returnsPresent_whenKeyIsRegistered() {
        registry().register(TestSetting.OPTION_A);
        Optional<PlayerSetting> result = registry().getSetting(key("test", "toggle"));
        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Given no registered setting for key, when getting by key, then returns empty")
    void getSetting_returnsEmpty_whenKeyIsNotRegistered() {
        Optional<PlayerSetting> result = registry().getSetting(key("test", "toggle"));
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Given registered settings, when getting all keys, then returns all registered keys")
    void getSettingKeys_returnsAllKeys_whenSettingsAreRegistered() {
        registry().register(TestSetting.OPTION_A);
        registry().register(OtherSetting.VALUE);
        assertEquals(2, registry().getSettingKeys().size());
        assertTrue(registry().getSettingKeys().contains(key("test", "toggle")));
        assertTrue(registry().getSettingKeys().contains(key("test", "other")));
    }

    @Test
    @DisplayName("Given no registered settings, when getting all keys, then returns empty set")
    void getSettingKeys_returnsEmptySet_whenNoSettingsRegistered() {
        assertTrue(registry().getSettingKeys().isEmpty());
    }

    @Test
    @DisplayName("Given registered settings, when getting all settings, then returns all settings")
    void getSettings_returnsAllSettings_whenSettingsAreRegistered() {
        registry().register(TestSetting.OPTION_A);
        registry().register(OtherSetting.VALUE);
        assertEquals(2, registry().getSettings().size());
    }

    @Test
    @DisplayName("Given no registered settings, when getting all settings, then returns empty set")
    void getSettings_returnsEmptySet_whenNoSettingsRegistered() {
        assertTrue(registry().getSettings().isEmpty());
    }

    @Test
    @DisplayName("Given a setting with linked nodes, when registering, then stores first setting node value")
    void register_storesFirstSettingNodeValue_whenSettingHasLinkedNodes() {
        registry().register(TestSetting.OPTION_B);
        Optional<PlayerSetting> result = registry().getSetting(key("test", "toggle"));
        assertTrue(result.isPresent());
        assertEquals(TestSetting.OPTION_A, result.get());
    }
}

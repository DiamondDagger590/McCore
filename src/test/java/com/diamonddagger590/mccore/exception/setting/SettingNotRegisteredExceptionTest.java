package com.diamonddagger590.mccore.exception.setting;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingNotRegisteredExceptionTest {

    @SuppressWarnings("deprecation")
    private static NamespacedKey key(String namespace, String key) {
        return new NamespacedKey(namespace, key);
    }

    @Test
    @DisplayName("Given a setting key, when constructing, then the key is stored")
    void constructor_storesSettingKey() {
        NamespacedKey key = key("myplugin", "dark_mode");
        SettingNotRegisteredException ex = new SettingNotRegisteredException(key);
        assertEquals(key, ex.getSettingKey());
    }

    @Test
    @DisplayName("Given a setting key, when calling getMessage, then message contains the key")
    void getMessage_containsSettingKey() {
        NamespacedKey key = key("myplugin", "dark_mode");
        SettingNotRegisteredException ex = new SettingNotRegisteredException(key);

        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains(key.toString()));
    }

    @Test
    @DisplayName("SettingNotRegisteredException is a RuntimeException")
    void settingNotRegisteredException_isRuntimeException() {
        assertInstanceOf(RuntimeException.class, new SettingNotRegisteredException(key("test", "setting")));
    }
}

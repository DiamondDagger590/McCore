package com.diamonddagger590.mccore.builder.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemBuilderConfigurationKeysTest {

    private static final String[] ALL_KEYS = {
            ItemBuilderConfigurationKeys.MATERIAL,
            ItemBuilderConfigurationKeys.DATA,
            ItemBuilderConfigurationKeys.NAME,
            ItemBuilderConfigurationKeys.LORE_ROUTE,
            ItemBuilderConfigurationKeys.AMOUNT,
            ItemBuilderConfigurationKeys.MAX_STACK_SIZE,
            ItemBuilderConfigurationKeys.ENCHANTMENTS,
            ItemBuilderConfigurationKeys.CUSTOM_MODEL_DATA,
            ItemBuilderConfigurationKeys.CUSTOM_ITEM,
            ItemBuilderConfigurationKeys.HIDE_TOOLTIP,
            ItemBuilderConfigurationKeys.UNBREAKABLE_ITEM,
            ItemBuilderConfigurationKeys.ITEM_FLAGS,
            ItemBuilderConfigurationKeys.GLOWING,
            ItemBuilderConfigurationKeys.PLAYER,
            ItemBuilderConfigurationKeys.DAMAGE,
            ItemBuilderConfigurationKeys.SKULL,
            ItemBuilderConfigurationKeys.RGB,
            ItemBuilderConfigurationKeys.COLOR,
            ItemBuilderConfigurationKeys.MOB_TYPE,
            ItemBuilderConfigurationKeys.TRIM_PATTERN,
            ItemBuilderConfigurationKeys.TRIM_MATERIAL,
            ItemBuilderConfigurationKeys.POTION_HEADER,
            ItemBuilderConfigurationKeys.POTION_DURATION,
            ItemBuilderConfigurationKeys.POTION_LEVEL,
            ItemBuilderConfigurationKeys.POTION_ICON,
            ItemBuilderConfigurationKeys.POTION_AMBIENT,
            ItemBuilderConfigurationKeys.POTION_PARTICLES,
            ItemBuilderConfigurationKeys.PATTERN_HEADER,
    };

    @Test
    @DisplayName("Given top-level configuration keys, when accessed, then they return simple unprefixed paths")
    void topLevelKeys_returnSimplePaths_whenAccessed() {
        assertEquals("material", ItemBuilderConfigurationKeys.MATERIAL);
        assertEquals("data", ItemBuilderConfigurationKeys.DATA);
        assertEquals("name", ItemBuilderConfigurationKeys.NAME);
        assertEquals("lore", ItemBuilderConfigurationKeys.LORE_ROUTE);
        assertEquals("amount", ItemBuilderConfigurationKeys.AMOUNT);
        assertEquals("max-stack-size", ItemBuilderConfigurationKeys.MAX_STACK_SIZE);
        assertEquals("enchantments", ItemBuilderConfigurationKeys.ENCHANTMENTS);
        assertEquals("custom-model-data", ItemBuilderConfigurationKeys.CUSTOM_MODEL_DATA);
        assertEquals("custom-item", ItemBuilderConfigurationKeys.CUSTOM_ITEM);
        assertEquals("hide-tool-tip", ItemBuilderConfigurationKeys.HIDE_TOOLTIP);
        assertEquals("unbreakable-item", ItemBuilderConfigurationKeys.UNBREAKABLE_ITEM);
        assertEquals("item-flags", ItemBuilderConfigurationKeys.ITEM_FLAGS);
    }

    @Test
    @DisplayName("Given settings-level configuration keys, when accessed, then they are nested under 'settings' prefix")
    void settingsKeys_nestedUnderSettings_whenAccessed() {
        assertEquals("settings.glowing", ItemBuilderConfigurationKeys.GLOWING);
        assertEquals("settings.player", ItemBuilderConfigurationKeys.PLAYER);
        assertEquals("settings.damage", ItemBuilderConfigurationKeys.DAMAGE);
        assertEquals("settings.skull", ItemBuilderConfigurationKeys.SKULL);
        assertEquals("settings.rgb", ItemBuilderConfigurationKeys.RGB);
        assertEquals("settings.color", ItemBuilderConfigurationKeys.COLOR);
    }

    @Test
    @DisplayName("Given mob configuration key, when accessed, then it is nested under 'settings.mob' prefix")
    void mobTypeKey_nestedUnderSettingsMob_whenAccessed() {
        assertEquals("settings.mob.type", ItemBuilderConfigurationKeys.MOB_TYPE);
    }

    @Test
    @DisplayName("Given trim configuration keys, when accessed, then they are nested under 'settings.trim' prefix")
    void trimKeys_nestedUnderSettingsTrim_whenAccessed() {
        assertEquals("settings.trim.pattern", ItemBuilderConfigurationKeys.TRIM_PATTERN);
        assertEquals("settings.trim.material", ItemBuilderConfigurationKeys.TRIM_MATERIAL);
    }

    @Test
    @DisplayName("Given potion header key, when accessed, then it is nested under 'settings' prefix")
    void potionHeader_nestedUnderSettings_whenAccessed() {
        assertEquals("settings.potions", ItemBuilderConfigurationKeys.POTION_HEADER);
    }

    @Test
    @DisplayName("Given potion detail keys, when accessed, then they return relative paths for composition with potion header")
    void potionKeys_returnRelativePaths_whenAccessed() {
        assertEquals("duration", ItemBuilderConfigurationKeys.POTION_DURATION);
        assertEquals("level", ItemBuilderConfigurationKeys.POTION_LEVEL);
        assertEquals("style.icon", ItemBuilderConfigurationKeys.POTION_ICON);
        assertEquals("style.ambient", ItemBuilderConfigurationKeys.POTION_AMBIENT);
        assertEquals("style.particles", ItemBuilderConfigurationKeys.POTION_PARTICLES);
    }

    @Test
    @DisplayName("Given pattern header key, when accessed, then it is nested under 'settings' prefix")
    void patternHeader_nestedUnderSettings_whenAccessed() {
        assertEquals("settings.patterns", ItemBuilderConfigurationKeys.PATTERN_HEADER);
    }

    @Test
    @DisplayName("Given all configuration keys, when checked, then none are null or empty")
    void allKeys_areNonNullAndNonEmpty_whenChecked() {
        for (String key : ALL_KEYS) {
            assertNotNull(key);
            assertFalse(key.isEmpty());
        }
    }

    @Test
    @DisplayName("Given all configuration keys, when checked for format, then all use dot notation without leading or trailing dots")
    void allKeys_useDotNotationCorrectly_whenChecked() {
        for (String key : ALL_KEYS) {
            assertEquals(-1, key.indexOf('/'), "Keys should use dot notation, not slashes: " + key);
            assertFalse(key.startsWith("."), "Keys should not start with dot: " + key);
            assertFalse(key.endsWith("."), "Keys should not end with dot: " + key);
        }
    }

    @Test
    @DisplayName("Given nested configuration keys containing dots, when checked, then each segment between dots is non-empty")
    void nestedKeys_haveNonEmptySegments_whenContainingDots() {
        for (String key : ALL_KEYS) {
            if (key.contains(".")) {
                String[] segments = key.split("\\.");
                for (String segment : segments) {
                    assertTrue(segment.length() > 0, "Empty segment in key: " + key);
                }
            }
        }
    }
}

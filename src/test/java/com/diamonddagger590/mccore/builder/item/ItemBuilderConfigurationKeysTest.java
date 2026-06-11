package com.diamonddagger590.mccore.builder.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ItemBuilderConfigurationKeysTest {

    @Test
    void topLevelKeysAreSimplePaths() {
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
    void settingsKeysAreNestedUnderSettings() {
        assertEquals("settings.glowing", ItemBuilderConfigurationKeys.GLOWING);
        assertEquals("settings.player", ItemBuilderConfigurationKeys.PLAYER);
        assertEquals("settings.damage", ItemBuilderConfigurationKeys.DAMAGE);
        assertEquals("settings.skull", ItemBuilderConfigurationKeys.SKULL);
        assertEquals("settings.rgb", ItemBuilderConfigurationKeys.RGB);
        assertEquals("settings.color", ItemBuilderConfigurationKeys.COLOR);
    }

    @Test
    void mobKeysAreNestedUnderSettingsMob() {
        assertEquals("settings.mob.type", ItemBuilderConfigurationKeys.MOB_TYPE);
    }

    @Test
    void trimKeysAreNestedUnderSettingsTrim() {
        assertEquals("settings.trim.pattern", ItemBuilderConfigurationKeys.TRIM_PATTERN);
        assertEquals("settings.trim.material", ItemBuilderConfigurationKeys.TRIM_MATERIAL);
    }

    @Test
    void potionHeaderIsNestedUnderSettings() {
        assertEquals("settings.potions", ItemBuilderConfigurationKeys.POTION_HEADER);
    }

    @Test
    void potionKeysAreRelativePaths() {
        assertEquals("duration", ItemBuilderConfigurationKeys.POTION_DURATION);
        assertEquals("level", ItemBuilderConfigurationKeys.POTION_LEVEL);
        assertEquals("style.icon", ItemBuilderConfigurationKeys.POTION_ICON);
        assertEquals("style.ambient", ItemBuilderConfigurationKeys.POTION_AMBIENT);
        assertEquals("style.particles", ItemBuilderConfigurationKeys.POTION_PARTICLES);
    }

    @Test
    void patternHeaderIsNestedUnderSettings() {
        assertEquals("settings.patterns", ItemBuilderConfigurationKeys.PATTERN_HEADER);
    }

    @Test
    void allKeysAreNonNullAndNonEmpty() {
        String[] allKeys = {
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
        for (String key : allKeys) {
            assertNotNull(key);
            assertFalse(key.isEmpty());
        }
    }

    @Test
    void nestedKeysUseDotSeparator() {
        for (String key : new String[]{
                ItemBuilderConfigurationKeys.GLOWING,
                ItemBuilderConfigurationKeys.MOB_TYPE,
                ItemBuilderConfigurationKeys.TRIM_PATTERN,
                ItemBuilderConfigurationKeys.POTION_HEADER,
                ItemBuilderConfigurationKeys.PATTERN_HEADER,
        }) {
            assertEquals(-1, key.indexOf('/'), "Keys should use dot notation, not slashes: " + key);
            assertFalse(key.startsWith("."), "Keys should not start with dot: " + key);
            assertFalse(key.endsWith("."), "Keys should not end with dot: " + key);
        }
    }
}

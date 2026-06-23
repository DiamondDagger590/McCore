package com.diamonddagger590.mccore;

import com.diamonddagger590.mccore.bootstrap.StartupProfile;
import com.diamonddagger590.mccore.builder.item.ItemPluginType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

class CorePluginTest {

    private static final String TEST_MODE_PROPERTY = "mccore.testMode";

    @AfterEach
    void clearSystemProperty() {
        System.clearProperty(TEST_MODE_PROPERTY);
    }

    @Test
    @DisplayName("resolveProfile returns PROD when mccore.testMode is not set")
    void resolveProfile_returnsProd_whenPropertyNotSet() {
        System.clearProperty(TEST_MODE_PROPERTY);
        CorePlugin plugin = mock(CorePlugin.class, CALLS_REAL_METHODS);
        assertEquals(StartupProfile.PROD, plugin.resolveProfile());
    }

    @Test
    @DisplayName("resolveProfile returns PROD when mccore.testMode is 'true'")
    void resolveProfile_returnsProd_whenPropertyIsTrue() {
        System.setProperty(TEST_MODE_PROPERTY, "true");
        CorePlugin plugin = mock(CorePlugin.class, CALLS_REAL_METHODS);
        assertEquals(StartupProfile.PROD, plugin.resolveProfile());
    }

    @Test
    @DisplayName("resolveProfile returns TEST when mccore.testMode is 'false'")
    void resolveProfile_returnsTest_whenPropertyIsFalse() {
        System.setProperty(TEST_MODE_PROPERTY, "false");
        CorePlugin plugin = mock(CorePlugin.class, CALLS_REAL_METHODS);
        assertEquals(StartupProfile.TEST, plugin.resolveProfile());
    }

    @Test
    @DisplayName("resolveProfile returns TEST when mccore.testMode is non-boolean string")
    void resolveProfile_returnsTest_whenPropertyIsNonBoolean() {
        System.setProperty(TEST_MODE_PROPERTY, "notABoolean");
        CorePlugin plugin = mock(CorePlugin.class, CALLS_REAL_METHODS);
        assertEquals(StartupProfile.TEST, plugin.resolveProfile());
    }

    @Test
    @DisplayName("resolveProfile returns PROD when mccore.testMode is 'TRUE' (case insensitive)")
    void resolveProfile_returnsProd_whenPropertyIsTrueUpperCase() {
        System.setProperty(TEST_MODE_PROPERTY, "TRUE");
        CorePlugin plugin = mock(CorePlugin.class, CALLS_REAL_METHODS);
        assertEquals(StartupProfile.PROD, plugin.resolveProfile());
    }

    @Test
    @DisplayName("getInstance throws NullPointerException when plugin is not initialized")
    void getInstance_throwsNullPointerException_whenNotInitialized() {
        // Reset the static instance via reflection
        try {
            var field = CorePlugin.class.getDeclaredField("instance");
            field.setAccessible(true);
            Object previous = field.get(null);
            field.set(null, null);
            try {
                assertThrows(NullPointerException.class, CorePlugin::getInstance);
            } finally {
                field.set(null, previous);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("getItemPlugin returns NONE by default")
    void getItemPlugin_returnsNone() {
        CorePlugin plugin = mock(CorePlugin.class, CALLS_REAL_METHODS);
        assertEquals(ItemPluginType.NONE, plugin.getItemPlugin());
    }
}

package com.diamonddagger590.mccore;

import com.diamonddagger590.mccore.bootstrap.StartupProfile;
import com.diamonddagger590.mccore.builder.item.ItemPluginType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

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
    @DisplayName("Given no system property set, when resolveProfile is called, then returns PROD")
    void resolveProfile_returnsProd_whenPropertyNotSet() {
        System.clearProperty(TEST_MODE_PROPERTY);
        CorePlugin plugin = mock(CorePlugin.class, CALLS_REAL_METHODS);
        assertEquals(StartupProfile.PROD, plugin.resolveProfile());
    }

    @Test
    @DisplayName("Given mccore.testMode is 'true', when resolveProfile is called, then returns PROD")
    void resolveProfile_returnsProd_whenPropertyIsTrue() {
        System.setProperty(TEST_MODE_PROPERTY, "true");
        CorePlugin plugin = mock(CorePlugin.class, CALLS_REAL_METHODS);
        assertEquals(StartupProfile.PROD, plugin.resolveProfile());
    }

    @Test
    @DisplayName("Given mccore.testMode is 'false', when resolveProfile is called, then returns TEST")
    void resolveProfile_returnsTest_whenPropertyIsFalse() {
        System.setProperty(TEST_MODE_PROPERTY, "false");
        CorePlugin plugin = mock(CorePlugin.class, CALLS_REAL_METHODS);
        assertEquals(StartupProfile.TEST, plugin.resolveProfile());
    }

    @Test
    @DisplayName("Given mccore.testMode is a non-boolean string, when resolveProfile is called, then returns TEST")
    void resolveProfile_returnsTest_whenPropertyIsNonBoolean() {
        System.setProperty(TEST_MODE_PROPERTY, "notABoolean");
        CorePlugin plugin = mock(CorePlugin.class, CALLS_REAL_METHODS);
        assertEquals(StartupProfile.TEST, plugin.resolveProfile());
    }

    @Test
    @DisplayName("Given mccore.testMode is 'TRUE' (uppercase), when resolveProfile is called, then returns PROD")
    void resolveProfile_returnsProd_whenPropertyIsTrueUpperCase() {
        System.setProperty(TEST_MODE_PROPERTY, "TRUE");
        CorePlugin plugin = mock(CorePlugin.class, CALLS_REAL_METHODS);
        assertEquals(StartupProfile.PROD, plugin.resolveProfile());
    }

    @Test
    @DisplayName("Given plugin is not initialized, when getInstance is called, then throws NullPointerException")
    void getInstance_throwsNullPointerException_whenNotInitialized() {
        try {
            Field field = CorePlugin.class.getDeclaredField("instance");
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
    @DisplayName("Given default implementation, when getItemPlugin is called, then returns NONE")
    void getItemPlugin_returnsNone_whenDefaultImplementation() {
        CorePlugin plugin = mock(CorePlugin.class, CALLS_REAL_METHODS);
        assertEquals(ItemPluginType.NONE, plugin.getItemPlugin());
    }
}

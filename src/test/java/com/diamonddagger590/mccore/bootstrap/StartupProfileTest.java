package com.diamonddagger590.mccore.bootstrap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StartupProfileTest {

    private static final String SYSTEM_PROPERTY_KEY = "mccore.testMode";

    @AfterEach
    void tearDown() {
        System.clearProperty(SYSTEM_PROPERTY_KEY);
    }

    @Test
    @DisplayName("Given PROD profile, when setSystemProperty is called, then system property is set to true")
    void setSystemProperty_setsTrue_whenProd() {
        StartupProfile.PROD.setSystemProperty();
        assertEquals("true", System.getProperty(SYSTEM_PROPERTY_KEY));
    }

    @Test
    @DisplayName("Given TEST profile, when setSystemProperty is called, then system property is set to false")
    void setSystemProperty_setsFalse_whenTest() {
        StartupProfile.TEST.setSystemProperty();
        assertEquals("false", System.getProperty(SYSTEM_PROPERTY_KEY));
    }

    @Test
    @DisplayName("Given TEST profile is set, when PROD replaces it, then system property reflects PROD value")
    void setSystemProperty_overridesPrevious_whenCalledAgain() {
        StartupProfile.TEST.setSystemProperty();
        assertEquals("false", System.getProperty(SYSTEM_PROPERTY_KEY));

        StartupProfile.PROD.setSystemProperty();
        assertEquals("true", System.getProperty(SYSTEM_PROPERTY_KEY));
    }

    @Test
    @DisplayName("Given the enum, when values is called, then both PROD and TEST exist")
    void values_containsBothProfiles() {
        StartupProfile[] values = StartupProfile.values();
        assertEquals(2, values.length);
        assertEquals(StartupProfile.PROD, values[0]);
        assertEquals(StartupProfile.TEST, values[1]);
    }

    @Test
    @DisplayName("Given a valid name string, when valueOf is called, then returns the correct profile")
    void valueOf_returnsCorrectProfile() {
        assertEquals(StartupProfile.PROD, StartupProfile.valueOf("PROD"));
        assertEquals(StartupProfile.TEST, StartupProfile.valueOf("TEST"));
    }

    @Test
    @DisplayName("Given the PROD profile, when name is called, then returns PROD")
    void name_returnsPROD_forProdProfile() {
        assertEquals("PROD", StartupProfile.PROD.name());
    }

    @Test
    @DisplayName("Given the TEST profile, when name is called, then returns TEST")
    void name_returnsTEST_forTestProfile() {
        assertEquals("TEST", StartupProfile.TEST.name());
    }
}

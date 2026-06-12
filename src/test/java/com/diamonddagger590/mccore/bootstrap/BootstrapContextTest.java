package com.diamonddagger590.mccore.bootstrap;

import com.diamonddagger590.mccore.CorePlugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class BootstrapContextTest {

    @Mock
    private CorePlugin mockPlugin;

    @Mock
    private CorePlugin anotherMockPlugin;

    @Test
    @DisplayName("Given a plugin and PROD profile, When constructing BootstrapContext, Then plugin() returns the plugin")
    void pluginAccessor() {
        var context = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);
        assertEquals(mockPlugin, context.plugin());
    }

    @Test
    @DisplayName("Given a plugin and TEST profile, When constructing BootstrapContext, Then startupProfile() returns TEST")
    void startupProfileAccessor() {
        var context = new BootstrapContext<>(mockPlugin, StartupProfile.TEST);
        assertEquals(StartupProfile.TEST, context.startupProfile());
    }

    @Test
    @DisplayName("Given a plugin and PROD profile, When constructing BootstrapContext, Then startupProfile() returns PROD")
    void prodProfileAccessor() {
        var context = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);
        assertEquals(StartupProfile.PROD, context.startupProfile());
    }

    @Test
    @DisplayName("Given two BootstrapContexts with same plugin and profile, When comparing, Then they are equal")
    void equalContexts() {
        var context1 = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);
        var context2 = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);
        assertEquals(context1, context2);
    }

    @Test
    @DisplayName("Given two BootstrapContexts with different profiles, When comparing, Then they are not equal")
    void differentProfiles() {
        var context1 = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);
        var context2 = new BootstrapContext<>(mockPlugin, StartupProfile.TEST);
        assertNotEquals(context1, context2);
    }

    @Test
    @DisplayName("Given two BootstrapContexts with different plugins, When comparing, Then they are not equal")
    void differentPlugins() {
        var context1 = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);
        var context2 = new BootstrapContext<>(anotherMockPlugin, StartupProfile.PROD);
        assertNotEquals(context1, context2);
    }

    @Test
    @DisplayName("Given two equal BootstrapContexts, When computing hashCode, Then they match")
    void hashCodeConsistency() {
        var context1 = new BootstrapContext<>(mockPlugin, StartupProfile.TEST);
        var context2 = new BootstrapContext<>(mockPlugin, StartupProfile.TEST);
        assertEquals(context1.hashCode(), context2.hashCode());
    }

    @Test
    @DisplayName("Given a BootstrapContext, When calling toString, Then it contains the class name")
    void toStringContainsClassName() {
        var context = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);
        String str = context.toString();
        assertNotNull(str);
        assertEquals(true, str.contains("BootstrapContext"));
    }
}

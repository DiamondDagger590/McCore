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
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class BootstrapContextTest {

    @Mock
    private CorePlugin mockPlugin;

    @Mock
    private CorePlugin anotherMockPlugin;

    @Test
    @DisplayName("Given a plugin and PROD profile, when constructing BootstrapContext, then plugin() returns the plugin")
    void plugin_returnsPlugin_whenConstructedWithProdProfile() {
        var context = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);
        assertEquals(mockPlugin, context.plugin());
    }

    @Test
    @DisplayName("Given a plugin and TEST profile, when constructing BootstrapContext, then startupProfile() returns TEST")
    void startupProfile_returnsTest_whenConstructedWithTestProfile() {
        var context = new BootstrapContext<>(mockPlugin, StartupProfile.TEST);
        assertEquals(StartupProfile.TEST, context.startupProfile());
    }

    @Test
    @DisplayName("Given a plugin and PROD profile, when constructing BootstrapContext, then startupProfile() returns PROD")
    void startupProfile_returnsProd_whenConstructedWithProdProfile() {
        var context = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);
        assertEquals(StartupProfile.PROD, context.startupProfile());
    }

    @Test
    @DisplayName("Given two BootstrapContexts with same plugin and profile, when comparing, then they are equal")
    void equals_returnsTrue_whenSamePluginAndProfile() {
        var context1 = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);
        var context2 = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);
        assertEquals(context1, context2);
    }

    @Test
    @DisplayName("Given two BootstrapContexts with different profiles, when comparing, then they are not equal")
    void equals_returnsFalse_whenDifferentProfiles() {
        var context1 = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);
        var context2 = new BootstrapContext<>(mockPlugin, StartupProfile.TEST);
        assertNotEquals(context1, context2);
    }

    @Test
    @DisplayName("Given two BootstrapContexts with different plugins, when comparing, then they are not equal")
    void equals_returnsFalse_whenDifferentPlugins() {
        var context1 = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);
        var context2 = new BootstrapContext<>(anotherMockPlugin, StartupProfile.PROD);
        assertNotEquals(context1, context2);
    }

    @Test
    @DisplayName("Given two equal BootstrapContexts, when computing hashCode, then they match")
    void hashCode_matches_whenContextsAreEqual() {
        var context1 = new BootstrapContext<>(mockPlugin, StartupProfile.TEST);
        var context2 = new BootstrapContext<>(mockPlugin, StartupProfile.TEST);
        assertEquals(context1.hashCode(), context2.hashCode());
    }

    @Test
    @DisplayName("Given a BootstrapContext, when calling toString, then it contains the class name")
    void toString_containsClassName_whenCalled() {
        var context = new BootstrapContext<>(mockPlugin, StartupProfile.PROD);
        String str = context.toString();
        assertNotNull(str);
        assertTrue(str.contains("BootstrapContext"));
    }
}

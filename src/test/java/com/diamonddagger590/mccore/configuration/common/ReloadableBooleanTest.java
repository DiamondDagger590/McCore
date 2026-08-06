package com.diamonddagger590.mccore.configuration.common;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReloadableBooleanTest {

    private YamlDocument yamlDocument;

    @BeforeEach
    void setUp() throws IOException {
        String yaml = "settings:\n  enabled: true\n  disabled: false\n";
        yamlDocument = YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("Given a yaml with a true boolean, when constructed with callback, then content is true")
    void constructor_loadsTrue_whenYamlValueIsTrue() {
        Route route = Route.from("settings", "enabled");
        ReloadableBoolean content = new ReloadableBoolean(yamlDocument, route);
        assertTrue(content.getContent());
    }

    @Test
    @DisplayName("Given a yaml with a false boolean, when constructed with callback, then content is false")
    void constructor_loadsFalse_whenYamlValueIsFalse() {
        Route route = Route.from("settings", "disabled");
        ReloadableBoolean content = new ReloadableBoolean(yamlDocument, route);
        assertFalse(content.getContent());
    }

    @Test
    @DisplayName("Given a missing yaml key, when constructed with callback, then content is false default")
    void constructor_loadsDefault_whenYamlKeyIsMissing() {
        Route route = Route.from("settings", "nonexistent");
        ReloadableBoolean content = new ReloadableBoolean(yamlDocument, route);
        assertFalse(content.getContent());
    }

    @Test
    @DisplayName("Given an explicit content value, when constructed, then uses provided content instead of yaml")
    void constructor_usesProvidedContent_whenExplicitContentGiven() {
        Route route = Route.from("settings", "enabled");
        ReloadableBoolean content = new ReloadableBoolean(yamlDocument, route, false);
        assertFalse(content.getContent());
    }

    @Test
    @DisplayName("Given explicit content, when reloadContent is called, then refreshes from yaml")
    void reloadContent_refreshesFromYaml_whenCalledAfterExplicitContent() {
        Route route = Route.from("settings", "enabled");
        ReloadableBoolean content = new ReloadableBoolean(yamlDocument, route, false);
        assertFalse(content.getContent());

        content.reloadContent();
        assertTrue(content.getContent());
    }

    @Test
    @DisplayName("Given yaml value is updated, when reloadContent is called, then reflects new value")
    void reloadContent_reflectsUpdate_whenYamlModified() {
        Route route = Route.from("settings", "enabled");
        ReloadableBoolean content = new ReloadableBoolean(yamlDocument, route);
        assertTrue(content.getContent());

        yamlDocument.set(route, false);
        content.reloadContent();
        assertFalse(content.getContent());
    }
}

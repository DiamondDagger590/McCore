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

class ReloadableIntegerTest {

    private YamlDocument yamlDocument;

    @BeforeEach
    void setUp() throws IOException {
        String yaml = "settings:\n  count: 42\n  negative: -5\n  zero: 0\n";
        yamlDocument = YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("Given a yaml with an integer, when constructed with callback, then content is loaded")
    void constructor_loadsInteger_whenYamlValueIsPresent() {
        Route route = Route.from("settings", "count");
        ReloadableInteger content = new ReloadableInteger(yamlDocument, route);
        assertEquals(42, content.getContent());
    }

    @Test
    @DisplayName("Given a yaml with a negative integer, when constructed, then content is negative")
    void constructor_loadsNegative_whenYamlValueIsNegative() {
        Route route = Route.from("settings", "negative");
        ReloadableInteger content = new ReloadableInteger(yamlDocument, route);
        assertEquals(-5, content.getContent());
    }

    @Test
    @DisplayName("Given an explicit content value, when constructed, then uses provided content")
    void constructor_usesProvidedContent_whenExplicitContentGiven() {
        Route route = Route.from("settings", "count");
        ReloadableInteger content = new ReloadableInteger(yamlDocument, route, 100);
        assertEquals(100, content.getContent());
    }

    @Test
    @DisplayName("Given explicit content, when reloadContent is called, then refreshes from yaml")
    void reloadContent_refreshesFromYaml_whenCalledAfterExplicitContent() {
        Route route = Route.from("settings", "count");
        ReloadableInteger content = new ReloadableInteger(yamlDocument, route, 0);
        assertEquals(0, content.getContent());

        content.reloadContent();
        assertEquals(42, content.getContent());
    }

    @Test
    @DisplayName("Given a yaml with zero, when constructed with callback, then content is zero")
    void constructor_loadsZero_whenYamlValueIsZero() {
        Route route = Route.from("settings", "zero");
        ReloadableInteger content = new ReloadableInteger(yamlDocument, route);
        assertEquals(0, content.getContent());
    }

    @Test
    @DisplayName("Given a missing yaml key, when constructed with callback, then content is zero default")
    void constructor_loadsDefault_whenYamlKeyIsMissing() {
        Route route = Route.from("settings", "nonexistent");
        ReloadableInteger content = new ReloadableInteger(yamlDocument, route);
        assertEquals(0, content.getContent());
    }

    @Test
    @DisplayName("Given yaml value is updated, when reloadContent is called, then reflects new value")
    void reloadContent_reflectsUpdate_whenYamlModified() {
        Route route = Route.from("settings", "count");
        ReloadableInteger content = new ReloadableInteger(yamlDocument, route);
        assertEquals(42, content.getContent());

        yamlDocument.set(route, 99);
        content.reloadContent();
        assertEquals(99, content.getContent());
    }
}

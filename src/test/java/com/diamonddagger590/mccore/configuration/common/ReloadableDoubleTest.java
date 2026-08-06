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

class ReloadableDoubleTest {

    private YamlDocument yamlDocument;

    @BeforeEach
    void setUp() throws IOException {
        String yaml = "settings:\n  rate: 3.14\n  zero: 0.0\n  negative: -1.5\n";
        yamlDocument = YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("Given a yaml with a double value, when constructed with callback, then content is loaded")
    void constructor_loadsDouble_whenYamlValueIsPresent() {
        Route route = Route.from("settings", "rate");
        ReloadableDouble content = new ReloadableDouble(yamlDocument, route);
        assertEquals(3.14, content.getContent(), 0.001);
    }

    @Test
    @DisplayName("Given a yaml with zero, when constructed with callback, then content is zero")
    void constructor_loadsZero_whenYamlValueIsZero() {
        Route route = Route.from("settings", "zero");
        ReloadableDouble content = new ReloadableDouble(yamlDocument, route);
        assertEquals(0.0, content.getContent(), 0.001);
    }

    @Test
    @DisplayName("Given a yaml with a negative double, when constructed, then content is negative")
    void constructor_loadsNegative_whenYamlValueIsNegative() {
        Route route = Route.from("settings", "negative");
        ReloadableDouble content = new ReloadableDouble(yamlDocument, route);
        assertEquals(-1.5, content.getContent(), 0.001);
    }

    @Test
    @DisplayName("Given a missing yaml key, when constructed with callback, then content is zero default")
    void constructor_loadsDefault_whenYamlKeyIsMissing() {
        Route route = Route.from("settings", "nonexistent");
        ReloadableDouble content = new ReloadableDouble(yamlDocument, route);
        assertEquals(0.0, content.getContent(), 0.001);
    }

    @Test
    @DisplayName("Given an explicit content value, when constructed, then uses provided content")
    void constructor_usesProvidedContent_whenExplicitContentGiven() {
        Route route = Route.from("settings", "rate");
        ReloadableDouble content = new ReloadableDouble(yamlDocument, route, 99.9);
        assertEquals(99.9, content.getContent(), 0.001);
    }

    @Test
    @DisplayName("Given explicit content, when reloadContent is called, then refreshes from yaml")
    void reloadContent_refreshesFromYaml_whenCalledAfterExplicitContent() {
        Route route = Route.from("settings", "rate");
        ReloadableDouble content = new ReloadableDouble(yamlDocument, route, 0.0);
        assertEquals(0.0, content.getContent(), 0.001);

        content.reloadContent();
        assertEquals(3.14, content.getContent(), 0.001);
    }

    @Test
    @DisplayName("Given yaml value is updated, when reloadContent is called, then reflects new value")
    void reloadContent_reflectsUpdate_whenYamlModified() {
        Route route = Route.from("settings", "rate");
        ReloadableDouble content = new ReloadableDouble(yamlDocument, route);
        assertEquals(3.14, content.getContent(), 0.001);

        yamlDocument.set(route, 2.718);
        content.reloadContent();
        assertEquals(2.718, content.getContent(), 0.001);
    }
}

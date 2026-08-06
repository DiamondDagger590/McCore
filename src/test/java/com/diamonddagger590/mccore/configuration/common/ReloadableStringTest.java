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

class ReloadableStringTest {

    private YamlDocument yamlDocument;

    @BeforeEach
    void setUp() throws IOException {
        String yaml = "settings:\n  name: hello\n  empty: \"\"\n";
        yamlDocument = YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("Given a yaml with a string, when constructed with callback, then content is loaded")
    void constructor_loadsString_whenYamlValueIsPresent() {
        Route route = Route.from("settings", "name");
        ReloadableString content = new ReloadableString(yamlDocument, route);
        assertEquals("hello", content.getContent());
    }

    @Test
    @DisplayName("Given a yaml with an empty string, when constructed, then content is empty")
    void constructor_loadsEmptyString_whenYamlValueIsEmpty() {
        Route route = Route.from("settings", "empty");
        ReloadableString content = new ReloadableString(yamlDocument, route);
        assertEquals("", content.getContent());
    }

    @Test
    @DisplayName("Given a missing yaml key, when constructed with callback, then content is null")
    void constructor_loadsNull_whenYamlKeyIsMissing() {
        Route route = Route.from("settings", "nonexistent");
        ReloadableString content = new ReloadableString(yamlDocument, route);
        assertEquals(null, content.getContent());
    }

    @Test
    @DisplayName("Given an explicit content value, when constructed, then uses provided content")
    void constructor_usesProvidedContent_whenExplicitContentGiven() {
        Route route = Route.from("settings", "name");
        ReloadableString content = new ReloadableString(yamlDocument, route, "override");
        assertEquals("override", content.getContent());
    }

    @Test
    @DisplayName("Given explicit content, when reloadContent is called, then refreshes from yaml")
    void reloadContent_refreshesFromYaml_whenCalledAfterExplicitContent() {
        Route route = Route.from("settings", "name");
        ReloadableString content = new ReloadableString(yamlDocument, route, "override");
        assertEquals("override", content.getContent());

        content.reloadContent();
        assertEquals("hello", content.getContent());
    }

    @Test
    @DisplayName("Given yaml value is updated, when reloadContent is called, then reflects new value")
    void reloadContent_reflectsUpdate_whenYamlModified() {
        Route route = Route.from("settings", "name");
        ReloadableString content = new ReloadableString(yamlDocument, route);
        assertEquals("hello", content.getContent());

        yamlDocument.set(route, "world");
        content.reloadContent();
        assertEquals("world", content.getContent());
    }
}

package com.diamonddagger590.mccore.configuration.collection;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReloadableStringListTest {

    private YamlDocument yamlDocument;

    @BeforeEach
    void setUp() throws IOException {
        String yaml = "messages:\n  - hello\n  - world\nempty: []\n";
        yamlDocument = YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("Given a yaml string list, when constructed with callback, then content is loaded")
    void constructor_loadsStringList_whenYamlListIsPresent() {
        Route route = Route.from("messages");
        ReloadableStringList content = new ReloadableStringList(yamlDocument, route);
        assertEquals(2, content.getContent().size());
        assertEquals("hello", content.getContent().get(0));
        assertEquals("world", content.getContent().get(1));
    }

    @Test
    @DisplayName("Given an empty yaml list, when constructed, then content is empty")
    void constructor_loadsEmptyList_whenYamlListIsEmpty() {
        Route route = Route.from("empty");
        ReloadableStringList content = new ReloadableStringList(yamlDocument, route);
        assertTrue(content.getContent().isEmpty());
    }

    @Test
    @DisplayName("Given a missing yaml key, when constructed, then content is empty list")
    void constructor_loadsEmptyList_whenYamlKeyIsMissing() {
        Route route = Route.from("nonexistent");
        ReloadableStringList content = new ReloadableStringList(yamlDocument, route);
        assertTrue(content.getContent().isEmpty());
    }

    @Test
    @DisplayName("Given an explicit content list, when constructed, then uses provided content")
    void constructor_usesProvidedContent_whenExplicitContentGiven() {
        Route route = Route.from("messages");
        List<String> explicit = List.of("custom");
        ReloadableStringList content = new ReloadableStringList(yamlDocument, route, explicit);
        assertEquals(1, content.getContent().size());
        assertEquals("custom", content.getContent().get(0));
    }

    @Test
    @DisplayName("Given explicit content, when reloadContent is called, then refreshes from yaml")
    void reloadContent_refreshesFromYaml_whenCalledAfterExplicitContent() {
        Route route = Route.from("messages");
        ReloadableStringList content = new ReloadableStringList(yamlDocument, route, List.of("stale"));
        assertEquals(1, content.getContent().size());

        content.reloadContent();
        assertEquals(2, content.getContent().size());
        assertEquals("hello", content.getContent().get(0));
    }

    @Test
    @DisplayName("Given yaml is updated, when reloadContent is called, then reflects updated list")
    void reloadContent_reflectsUpdate_whenYamlModified() {
        Route route = Route.from("messages");
        ReloadableStringList content = new ReloadableStringList(yamlDocument, route);
        assertEquals(2, content.getContent().size());

        yamlDocument.set(route, List.of("updated", "list", "here"));
        content.reloadContent();
        assertEquals(3, content.getContent().size());
        assertEquals("updated", content.getContent().get(0));
        assertEquals("list", content.getContent().get(1));
        assertEquals("here", content.getContent().get(2));
    }
}

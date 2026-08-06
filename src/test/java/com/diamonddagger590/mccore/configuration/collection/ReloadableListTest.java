package com.diamonddagger590.mccore.configuration.collection;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReloadableListTest {

    private YamlDocument yamlDocument;

    @BeforeEach
    void setUp() throws IOException {
        String yaml = "items:\n  - apple\n  - banana\n  - cherry\nempty: []\n";
        yamlDocument = YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("Given a yaml string list, when constructed with identity conversion, then content matches yaml values")
    void constructor_loadsStringList_whenIdentityConversionUsed() {
        Route route = Route.from("items");
        ReloadableList<String> content = new ReloadableList<>(yamlDocument, route, ArrayList::new);
        assertEquals(3, content.getContent().size());
        assertEquals("apple", content.getContent().get(0));
        assertEquals("banana", content.getContent().get(1));
        assertEquals("cherry", content.getContent().get(2));
    }

    @Test
    @DisplayName("Given a yaml string list, when constructed with transform conversion, then content is transformed")
    void constructor_appliesTransform_whenConversionFunctionTransforms() {
        Route route = Route.from("items");
        ReloadableList<String> content = new ReloadableList<>(yamlDocument, route,
                strings -> strings.stream().map(String::toUpperCase).toList());
        assertEquals(List.of("APPLE", "BANANA", "CHERRY"), content.getContent());
    }

    @Test
    @DisplayName("Given an empty yaml list, when constructed, then content is empty")
    void constructor_loadsEmptyList_whenYamlListIsEmpty() {
        Route route = Route.from("empty");
        ReloadableList<String> content = new ReloadableList<>(yamlDocument, route, ArrayList::new);
        assertTrue(content.getContent().isEmpty());
    }

    @Test
    @DisplayName("Given a missing yaml key, when constructed, then content is empty list")
    void constructor_loadsEmptyList_whenYamlKeyIsMissing() {
        Route route = Route.from("nonexistent");
        ReloadableList<String> content = new ReloadableList<>(yamlDocument, route, ArrayList::new);
        assertTrue(content.getContent().isEmpty());
    }

    @Test
    @DisplayName("Given an explicit content list, when constructed, then uses provided content")
    void constructor_usesProvidedContent_whenExplicitContentGiven() {
        Route route = Route.from("items");
        List<String> explicit = List.of("x", "y");
        ReloadableList<String> content = new ReloadableList<>(yamlDocument, route, ArrayList::new, explicit);
        assertEquals(List.of("x", "y"), content.getContent());
    }

    @Test
    @DisplayName("Given explicit content, when reloadContent is called, then refreshes from yaml using conversion function")
    void reloadContent_refreshesFromYaml_whenCalledAfterExplicitContent() {
        Route route = Route.from("items");
        ReloadableList<String> content = new ReloadableList<>(yamlDocument, route, ArrayList::new, List.of("stale"));
        assertEquals(1, content.getContent().size());

        content.reloadContent();
        assertEquals(3, content.getContent().size());
        assertEquals("apple", content.getContent().get(0));
    }

    @Test
    @DisplayName("Given yaml is updated, when reloadContent is called, then reflects updated list")
    void reloadContent_reflectsUpdate_whenYamlModified() {
        Route route = Route.from("items");
        ReloadableList<String> content = new ReloadableList<>(yamlDocument, route, ArrayList::new);
        assertEquals(3, content.getContent().size());

        yamlDocument.set(route, List.of("date", "elderberry"));
        content.reloadContent();
        assertEquals(2, content.getContent().size());
        assertEquals("date", content.getContent().get(0));
        assertEquals("elderberry", content.getContent().get(1));
    }

    @Test
    @DisplayName("Given a conversion to integer list, when constructed, then content contains parsed integers")
    void constructor_convertsToIntegers_whenConversionParsesIntegers() throws IOException {
        String yaml = "numbers:\n  - \"1\"\n  - \"2\"\n  - \"3\"\n";
        YamlDocument doc = YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
        Route route = Route.from("numbers");
        ReloadableList<Integer> content = new ReloadableList<>(doc, route,
                strings -> strings.stream().map(Integer::parseInt).toList());
        assertEquals(List.of(1, 2, 3), content.getContent());
    }
}

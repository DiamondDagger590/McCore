package com.diamonddagger590.mccore.configuration.collection;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReloadableSetTest {

    private YamlDocument yamlDocument;

    @BeforeEach
    void setUp() throws IOException {
        String yaml = "worlds:\n  - world\n  - world_nether\n  - world_the_end\nempty: []\n";
        yamlDocument = YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("Given a yaml string list, when constructed with set conversion, then content contains all values as a set")
    void constructor_loadsSet_whenYamlListIsPresent() {
        Route route = Route.from("worlds");
        ReloadableSet<String> content = new ReloadableSet<>(yamlDocument, route, HashSet::new);
        assertEquals(3, content.getContent().size());
        assertTrue(content.getContent().contains("world"));
        assertTrue(content.getContent().contains("world_nether"));
        assertTrue(content.getContent().contains("world_the_end"));
    }

    @Test
    @DisplayName("Given an empty yaml list, when constructed, then content is empty set")
    void constructor_loadsEmptySet_whenYamlListIsEmpty() {
        Route route = Route.from("empty");
        ReloadableSet<String> content = new ReloadableSet<>(yamlDocument, route, HashSet::new);
        assertTrue(content.getContent().isEmpty());
    }

    @Test
    @DisplayName("Given a missing yaml key, when constructed, then content is empty set")
    void constructor_loadsEmptySet_whenYamlKeyIsMissing() {
        Route route = Route.from("nonexistent");
        ReloadableSet<String> content = new ReloadableSet<>(yamlDocument, route, HashSet::new);
        assertTrue(content.getContent().isEmpty());
    }

    @Test
    @DisplayName("Given a transform conversion, when constructed, then content is transformed")
    void constructor_appliesTransform_whenConversionFunctionTransforms() {
        Route route = Route.from("worlds");
        ReloadableSet<String> content = new ReloadableSet<>(yamlDocument, route,
                strings -> new HashSet<>(strings.stream().map(String::toUpperCase).toList()));
        assertTrue(content.getContent().contains("WORLD"));
        assertTrue(content.getContent().contains("WORLD_NETHER"));
        assertTrue(content.getContent().contains("WORLD_THE_END"));
    }

    @Test
    @DisplayName("Given an explicit content set, when constructed, then uses provided content")
    void constructor_usesProvidedContent_whenExplicitContentGiven() {
        Route route = Route.from("worlds");
        Set<String> explicit = Set.of("custom_world");
        ReloadableSet<String> content = new ReloadableSet<>(yamlDocument, route, HashSet::new, explicit);
        assertEquals(1, content.getContent().size());
        assertTrue(content.getContent().contains("custom_world"));
    }

    @Test
    @DisplayName("Given explicit content, when reloadContent is called, then refreshes from yaml")
    void reloadContent_refreshesFromYaml_whenCalledAfterExplicitContent() {
        Route route = Route.from("worlds");
        ReloadableSet<String> content = new ReloadableSet<>(yamlDocument, route, HashSet::new, Set.of("stale"));
        assertEquals(1, content.getContent().size());

        content.reloadContent();
        assertEquals(3, content.getContent().size());
        assertTrue(content.getContent().contains("world"));
    }

    @Test
    @DisplayName("Given yaml is updated, when reloadContent is called, then reflects updated set")
    void reloadContent_reflectsUpdate_whenYamlModified() {
        Route route = Route.from("worlds");
        ReloadableSet<String> content = new ReloadableSet<>(yamlDocument, route, HashSet::new);
        assertEquals(3, content.getContent().size());

        yamlDocument.set(route, List.of("new_world"));
        content.reloadContent();
        assertEquals(1, content.getContent().size());
        assertTrue(content.getContent().contains("new_world"));
    }

    @Test
    @DisplayName("Given duplicate entries in yaml, when constructed as set, then duplicates are removed")
    void constructor_deduplicates_whenYamlContainsDuplicates() throws IOException {
        String yaml = "dupes:\n  - world\n  - world\n  - other\n";
        YamlDocument doc = YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
        Route route = Route.from("dupes");
        ReloadableSet<String> content = new ReloadableSet<>(doc, route, HashSet::new);
        assertEquals(2, content.getContent().size());
        assertTrue(content.getContent().contains("world"));
        assertTrue(content.getContent().contains("other"));
    }
}

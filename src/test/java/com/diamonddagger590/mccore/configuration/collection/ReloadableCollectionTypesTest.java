package com.diamonddagger590.mccore.configuration.collection;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReloadableCollectionTypesTest {

    private static YamlDocument createYaml(String content) throws IOException {
        return YamlDocument.create(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }

    // ── ReloadableStringList ────────────────────────────────────────────────

    @Test
    @DisplayName("Given a YAML with a string list, when constructing ReloadableStringList, then loads all values")
    void reloadableStringList_loadsValues_whenConstructedFromYaml() throws IOException {
        String yaml = "worlds:\n  - world\n  - world_nether\n  - world_the_end";
        YamlDocument doc = createYaml(yaml);
        ReloadableStringList rsl = new ReloadableStringList(doc, Route.from("worlds"));
        assertEquals(3, rsl.getContent().size());
        assertEquals("world", rsl.getContent().get(0));
        assertEquals("world_nether", rsl.getContent().get(1));
        assertEquals("world_the_end", rsl.getContent().get(2));
    }

    @Test
    @DisplayName("Given explicit content, when constructing ReloadableStringList, then uses provided list")
    void reloadableStringList_usesProvidedContent_whenExplicitContentGiven() throws IOException {
        String yaml = "worlds:\n  - world";
        YamlDocument doc = createYaml(yaml);
        List<String> explicit = List.of("a", "b", "c");
        ReloadableStringList rsl = new ReloadableStringList(doc, Route.from("worlds"), explicit);
        assertEquals(3, rsl.getContent().size());
        assertEquals("a", rsl.getContent().get(0));
    }

    @Test
    @DisplayName("Given a ReloadableStringList with explicit content, when reloading, then loads from YAML")
    void reloadableStringList_loadsFromYaml_whenReloaded() throws IOException {
        String yaml = "worlds:\n  - world\n  - world_nether";
        YamlDocument doc = createYaml(yaml);
        List<String> explicit = List.of("override");
        ReloadableStringList rsl = new ReloadableStringList(doc, Route.from("worlds"), explicit);
        assertEquals(1, rsl.getContent().size());
        rsl.reloadContent();
        assertEquals(2, rsl.getContent().size());
        assertEquals("world", rsl.getContent().get(0));
    }

    @Test
    @DisplayName("Given an empty list in YAML, when constructing ReloadableStringList, then loads empty list")
    void reloadableStringList_loadsEmptyList_whenYamlListIsEmpty() throws IOException {
        String yaml = "worlds: []";
        YamlDocument doc = createYaml(yaml);
        ReloadableStringList rsl = new ReloadableStringList(doc, Route.from("worlds"));
        assertTrue(rsl.getContent().isEmpty());
    }

    // ── ReloadableSet ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Given a YAML with a string list, when constructing ReloadableSet, then converts to set")
    void reloadableSet_convertsToSet_whenConstructedFromYaml() throws IOException {
        String yaml = "materials:\n  - STONE\n  - DIRT\n  - STONE";
        YamlDocument doc = createYaml(yaml);
        ReloadableSet<String> rs = new ReloadableSet<>(doc, Route.from("materials"), strings -> new HashSet<>(strings));
        assertEquals(2, rs.getContent().size());
        assertTrue(rs.getContent().contains("STONE"));
        assertTrue(rs.getContent().contains("DIRT"));
    }

    @Test
    @DisplayName("Given explicit content, when constructing ReloadableSet, then uses provided set")
    void reloadableSet_usesProvidedContent_whenExplicitContentGiven() throws IOException {
        String yaml = "materials:\n  - STONE";
        YamlDocument doc = createYaml(yaml);
        Set<String> explicit = Set.of("A", "B");
        ReloadableSet<String> rs = new ReloadableSet<>(doc, Route.from("materials"), strings -> new HashSet<>(strings), explicit);
        assertEquals(2, rs.getContent().size());
        assertTrue(rs.getContent().contains("A"));
        assertTrue(rs.getContent().contains("B"));
    }

    @Test
    @DisplayName("Given a ReloadableSet with explicit content, when reloading, then loads from YAML")
    void reloadableSet_loadsFromYaml_whenReloaded() throws IOException {
        String yaml = "materials:\n  - STONE\n  - DIRT";
        YamlDocument doc = createYaml(yaml);
        Set<String> explicit = Set.of("OVERRIDE");
        ReloadableSet<String> rs = new ReloadableSet<>(doc, Route.from("materials"), strings -> new HashSet<>(strings), explicit);
        assertEquals(1, rs.getContent().size());
        rs.reloadContent();
        assertEquals(2, rs.getContent().size());
        assertTrue(rs.getContent().contains("STONE"));
    }

    @Test
    @DisplayName("Given a conversion function that transforms strings, when constructing ReloadableSet, then applies transformation")
    void reloadableSet_appliesConversion_whenGivenTransformFunction() throws IOException {
        String yaml = "items:\n  - hello\n  - world";
        YamlDocument doc = createYaml(yaml);
        ReloadableSet<String> rs = new ReloadableSet<>(doc, Route.from("items"), strings -> {
            Set<String> result = new HashSet<>();
            for (String s : strings) {
                result.add(s.toUpperCase());
            }
            return result;
        });
        assertTrue(rs.getContent().contains("HELLO"));
        assertTrue(rs.getContent().contains("WORLD"));
    }

    // ── ReloadableList ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Given a YAML with a string list, when constructing ReloadableList, then converts to list")
    void reloadableList_convertsList_whenConstructedFromYaml() throws IOException {
        String yaml = "messages:\n  - hello\n  - world";
        YamlDocument doc = createYaml(yaml);
        ReloadableList<String> rl = new ReloadableList<>(doc, Route.from("messages"), strings -> new ArrayList<>(strings));
        assertEquals(2, rl.getContent().size());
        assertEquals("hello", rl.getContent().get(0));
        assertEquals("world", rl.getContent().get(1));
    }

    @Test
    @DisplayName("Given explicit content, when constructing ReloadableList, then uses provided list")
    void reloadableList_usesProvidedContent_whenExplicitContentGiven() throws IOException {
        String yaml = "messages:\n  - hello";
        YamlDocument doc = createYaml(yaml);
        List<String> explicit = List.of("a", "b");
        ReloadableList<String> rl = new ReloadableList<>(doc, Route.from("messages"), strings -> new ArrayList<>(strings), explicit);
        assertEquals(2, rl.getContent().size());
        assertEquals("a", rl.getContent().get(0));
    }

    @Test
    @DisplayName("Given a ReloadableList with explicit content, when reloading, then loads from YAML")
    void reloadableList_loadsFromYaml_whenReloaded() throws IOException {
        String yaml = "messages:\n  - hello\n  - world";
        YamlDocument doc = createYaml(yaml);
        List<String> explicit = List.of("override");
        ReloadableList<String> rl = new ReloadableList<>(doc, Route.from("messages"), strings -> new ArrayList<>(strings), explicit);
        assertEquals(1, rl.getContent().size());
        rl.reloadContent();
        assertEquals(2, rl.getContent().size());
        assertEquals("hello", rl.getContent().get(0));
    }

    @Test
    @DisplayName("Given a conversion function that transforms strings, when constructing ReloadableList, then applies transformation")
    void reloadableList_appliesConversion_whenGivenTransformFunction() throws IOException {
        String yaml = "values:\n  - 1\n  - 2\n  - 3";
        YamlDocument doc = createYaml(yaml);
        ReloadableList<Integer> rl = new ReloadableList<>(doc, Route.from("values"), strings -> {
            List<Integer> result = new ArrayList<>();
            for (String s : strings) {
                result.add(Integer.parseInt(s));
            }
            return result;
        });
        assertEquals(3, rl.getContent().size());
        assertEquals(1, rl.getContent().get(0));
        assertEquals(2, rl.getContent().get(1));
        assertEquals(3, rl.getContent().get(2));
    }
}

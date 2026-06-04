package com.diamonddagger590.mccore.configuration;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ReloadableContentTest {

    private YamlDocument yamlDocument;

    @BeforeEach
    void setUp() throws IOException {
        String yaml = "settings:\n  name: original\n  count: 5\n  enabled: true\n  rate: 1.5\n";
        yamlDocument = YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("Given a yaml document, when ReloadableContent is created with callback, then content is loaded immediately")
    void constructor_loadsContentImmediately_whenCallbackProvided() {
        Route route = Route.from("settings", "name");
        ReloadableContent<String> content = new ReloadableContent<>(yamlDocument, route, (doc, r) -> doc.getString(r));

        assertEquals("original", content.getContent());
    }

    @Test
    @DisplayName("Given a yaml document, when ReloadableContent is created with explicit content, then uses provided content")
    void constructor_usesProvidedContent_whenExplicitContentGiven() {
        Route route = Route.from("settings", "name");
        ReloadableContent<String> content = new ReloadableContent<>(yamlDocument, route, (doc, r) -> doc.getString(r), "override");

        assertEquals("override", content.getContent());
    }

    @Test
    @DisplayName("Given a ReloadableContent with explicit content, when reloadContent is called, then refreshes from yaml")
    void reloadContent_refreshesFromYaml_whenCalled() {
        Route route = Route.from("settings", "name");
        ReloadableContent<String> content = new ReloadableContent<>(yamlDocument, route, (doc, r) -> doc.getString(r), "override");

        assertEquals("override", content.getContent());
        content.reloadContent();
        assertEquals("original", content.getContent());
    }

    @Test
    @DisplayName("Given a ReloadableContent, when getYamlDocument is called, then returns the yaml document")
    void getYamlDocument_returnsDocument() {
        Route route = Route.from("settings", "name");
        ReloadableContent<String> content = new ReloadableContent<>(yamlDocument, route, (doc, r) -> doc.getString(r));

        assertSame(yamlDocument, content.getYamlDocument());
    }

    @Test
    @DisplayName("Given a ReloadableContent, when getRoute is called, then returns the route")
    void getRoute_returnsRoute() {
        Route route = Route.from("settings", "name");
        ReloadableContent<String> content = new ReloadableContent<>(yamlDocument, route, (doc, r) -> doc.getString(r));

        assertSame(route, content.getRoute());
    }

    @Test
    @DisplayName("Given a ReloadableContent for an integer, when loaded, then returns the integer value")
    void constructor_loadsIntegerContent() {
        Route route = Route.from("settings", "count");
        ReloadableContent<Integer> content = new ReloadableContent<>(yamlDocument, route, (doc, r) -> doc.getInt(r));

        assertEquals(5, content.getContent());
    }

    @Test
    @DisplayName("Given a ReloadableContent for a boolean, when loaded, then returns the boolean value")
    void constructor_loadsBooleanContent() {
        Route route = Route.from("settings", "enabled");
        ReloadableContent<Boolean> content = new ReloadableContent<>(yamlDocument, route, (doc, r) -> doc.getBoolean(r));

        assertEquals(true, content.getContent());
    }

    @Test
    @DisplayName("Given a ReloadableContent for a double, when loaded, then returns the double value")
    void constructor_loadsDoubleContent() {
        Route route = Route.from("settings", "rate");
        ReloadableContent<Double> content = new ReloadableContent<>(yamlDocument, route, (doc, r) -> doc.getDouble(r));

        assertEquals(1.5, content.getContent(), 0.001);
    }

    @Test
    @DisplayName("Given a yaml document is updated, when reloadContent is called, then reflects the update")
    void reloadContent_reflectsUpdate_whenYamlModified() throws IOException {
        Route route = Route.from("settings", "name");
        ReloadableContent<String> content = new ReloadableContent<>(yamlDocument, route, (doc, r) -> doc.getString(r));

        assertEquals("original", content.getContent());

        yamlDocument.set(route, "updated");
        content.reloadContent();

        assertEquals("updated", content.getContent());
    }

    @Test
    @DisplayName("Given a transform callback, when content is loaded, then callback is applied")
    void constructor_appliesTransformCallback() {
        Route route = Route.from("settings", "name");
        BiFunction<YamlDocument, Route, String> toUpper = (doc, r) -> doc.getString(r).toUpperCase();
        ReloadableContent<String> content = new ReloadableContent<>(yamlDocument, route, toUpper);

        assertEquals("ORIGINAL", content.getContent());
    }
}

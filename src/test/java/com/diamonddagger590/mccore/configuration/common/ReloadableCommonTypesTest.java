package com.diamonddagger590.mccore.configuration.common;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReloadableCommonTypesTest {

    private static YamlDocument createYaml(String content) throws IOException {
        return YamlDocument.create(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }

    // ── ReloadableBoolean ───────────────────────────────────────────────────

    @Test
    @DisplayName("Given a YAML with a boolean, when constructing ReloadableBoolean, then loads the value")
    void reloadableBoolean_loadsValue_whenConstructedFromYaml() throws IOException {
        YamlDocument doc = createYaml("enabled: true");
        ReloadableBoolean rb = new ReloadableBoolean(doc, Route.from("enabled"));
        assertTrue(rb.getContent());
    }

    @Test
    @DisplayName("Given a false boolean in YAML, when constructing ReloadableBoolean, then loads false")
    void reloadableBoolean_loadsFalse_whenYamlIsFalse() throws IOException {
        YamlDocument doc = createYaml("enabled: false");
        ReloadableBoolean rb = new ReloadableBoolean(doc, Route.from("enabled"));
        assertFalse(rb.getContent());
    }

    @Test
    @DisplayName("Given explicit content, when constructing ReloadableBoolean, then uses provided value")
    void reloadableBoolean_usesProvidedContent_whenExplicitContentGiven() throws IOException {
        YamlDocument doc = createYaml("enabled: false");
        ReloadableBoolean rb = new ReloadableBoolean(doc, Route.from("enabled"), true);
        assertTrue(rb.getContent());
    }

    @Test
    @DisplayName("Given a ReloadableBoolean with explicit content, when reloading, then loads from YAML")
    void reloadableBoolean_loadsFromYaml_whenReloaded() throws IOException {
        YamlDocument doc = createYaml("enabled: false");
        ReloadableBoolean rb = new ReloadableBoolean(doc, Route.from("enabled"), true);
        assertTrue(rb.getContent());
        rb.reloadContent();
        assertFalse(rb.getContent());
    }

    // ── ReloadableInteger ───────────────────────────────────────────────────

    @Test
    @DisplayName("Given a YAML with an integer, when constructing ReloadableInteger, then loads the value")
    void reloadableInteger_loadsValue_whenConstructedFromYaml() throws IOException {
        YamlDocument doc = createYaml("max-players: 50");
        ReloadableInteger ri = new ReloadableInteger(doc, Route.from("max-players"));
        assertEquals(50, ri.getContent());
    }

    @Test
    @DisplayName("Given explicit content, when constructing ReloadableInteger, then uses provided value")
    void reloadableInteger_usesProvidedContent_whenExplicitContentGiven() throws IOException {
        YamlDocument doc = createYaml("max-players: 50");
        ReloadableInteger ri = new ReloadableInteger(doc, Route.from("max-players"), 100);
        assertEquals(100, ri.getContent());
    }

    @Test
    @DisplayName("Given a ReloadableInteger with explicit content, when reloading, then loads from YAML")
    void reloadableInteger_loadsFromYaml_whenReloaded() throws IOException {
        YamlDocument doc = createYaml("max-players: 50");
        ReloadableInteger ri = new ReloadableInteger(doc, Route.from("max-players"), 100);
        assertEquals(100, ri.getContent());
        ri.reloadContent();
        assertEquals(50, ri.getContent());
    }

    @Test
    @DisplayName("Given zero in YAML, when constructing ReloadableInteger, then loads zero")
    void reloadableInteger_loadsZero_whenYamlIsZero() throws IOException {
        YamlDocument doc = createYaml("count: 0");
        ReloadableInteger ri = new ReloadableInteger(doc, Route.from("count"));
        assertEquals(0, ri.getContent());
    }

    @Test
    @DisplayName("Given negative integer in YAML, when constructing ReloadableInteger, then loads negative value")
    void reloadableInteger_loadsNegative_whenYamlIsNegative() throws IOException {
        YamlDocument doc = createYaml("offset: -5");
        ReloadableInteger ri = new ReloadableInteger(doc, Route.from("offset"));
        assertEquals(-5, ri.getContent());
    }

    @Test
    @DisplayName("Given Integer.MAX_VALUE in YAML, when constructing ReloadableInteger, then loads max value")
    void reloadableInteger_loadsMaxValue_whenYamlIsMaxInt() throws IOException {
        YamlDocument doc = createYaml("value: " + Integer.MAX_VALUE);
        ReloadableInteger ri = new ReloadableInteger(doc, Route.from("value"));
        assertEquals(Integer.MAX_VALUE, ri.getContent());
    }

    @Test
    @DisplayName("Given Integer.MIN_VALUE in YAML, when constructing ReloadableInteger, then loads min value")
    void reloadableInteger_loadsMinValue_whenYamlIsMinInt() throws IOException {
        YamlDocument doc = createYaml("value: " + Integer.MIN_VALUE);
        ReloadableInteger ri = new ReloadableInteger(doc, Route.from("value"));
        assertEquals(Integer.MIN_VALUE, ri.getContent());
    }

    // ── ReloadableDouble ────────────────────────────────────────────────────

    @Test
    @DisplayName("Given a YAML with a double, when constructing ReloadableDouble, then loads the value")
    void reloadableDouble_loadsValue_whenConstructedFromYaml() throws IOException {
        YamlDocument doc = createYaml("multiplier: 1.5");
        ReloadableDouble rd = new ReloadableDouble(doc, Route.from("multiplier"));
        assertEquals(1.5, rd.getContent(), 0.001);
    }

    @Test
    @DisplayName("Given explicit content, when constructing ReloadableDouble, then uses provided value")
    void reloadableDouble_usesProvidedContent_whenExplicitContentGiven() throws IOException {
        YamlDocument doc = createYaml("multiplier: 1.5");
        ReloadableDouble rd = new ReloadableDouble(doc, Route.from("multiplier"), 2.0);
        assertEquals(2.0, rd.getContent(), 0.001);
    }

    @Test
    @DisplayName("Given a ReloadableDouble with explicit content, when reloading, then loads from YAML")
    void reloadableDouble_loadsFromYaml_whenReloaded() throws IOException {
        YamlDocument doc = createYaml("multiplier: 1.5");
        ReloadableDouble rd = new ReloadableDouble(doc, Route.from("multiplier"), 2.0);
        assertEquals(2.0, rd.getContent(), 0.001);
        rd.reloadContent();
        assertEquals(1.5, rd.getContent(), 0.001);
    }

    @Test
    @DisplayName("Given zero in YAML, when constructing ReloadableDouble, then loads zero")
    void reloadableDouble_loadsZero_whenYamlIsZero() throws IOException {
        YamlDocument doc = createYaml("value: 0.0");
        ReloadableDouble rd = new ReloadableDouble(doc, Route.from("value"));
        assertEquals(0.0, rd.getContent(), 0.001);
    }

    @Test
    @DisplayName("Given negative double in YAML, when constructing ReloadableDouble, then loads negative value")
    void reloadableDouble_loadsNegative_whenYamlIsNegative() throws IOException {
        YamlDocument doc = createYaml("value: -3.14");
        ReloadableDouble rd = new ReloadableDouble(doc, Route.from("value"));
        assertEquals(-3.14, rd.getContent(), 0.001);
    }

    // ── ReloadableString ────────────────────────────────────────────────────

    @Test
    @DisplayName("Given a YAML with a string, when constructing ReloadableString, then loads the value")
    void reloadableString_loadsValue_whenConstructedFromYaml() throws IOException {
        YamlDocument doc = createYaml("prefix: \"[McCore]\"");
        ReloadableString rs = new ReloadableString(doc, Route.from("prefix"));
        assertEquals("[McCore]", rs.getContent());
    }

    @Test
    @DisplayName("Given explicit content, when constructing ReloadableString, then uses provided value")
    void reloadableString_usesProvidedContent_whenExplicitContentGiven() throws IOException {
        YamlDocument doc = createYaml("prefix: \"[McCore]\"");
        ReloadableString rs = new ReloadableString(doc, Route.from("prefix"), "override");
        assertEquals("override", rs.getContent());
    }

    @Test
    @DisplayName("Given a ReloadableString with explicit content, when reloading, then loads from YAML")
    void reloadableString_loadsFromYaml_whenReloaded() throws IOException {
        YamlDocument doc = createYaml("prefix: \"[McCore]\"");
        ReloadableString rs = new ReloadableString(doc, Route.from("prefix"), "override");
        assertEquals("override", rs.getContent());
        rs.reloadContent();
        assertEquals("[McCore]", rs.getContent());
    }

    @Test
    @DisplayName("Given an unquoted string in YAML, when constructing ReloadableString, then loads correctly")
    void reloadableString_loadsUnquotedValue_whenConstructedFromYaml() throws IOException {
        YamlDocument doc = createYaml("name: hello");
        ReloadableString rs = new ReloadableString(doc, Route.from("name"));
        assertEquals("hello", rs.getContent());
    }

    @Test
    @DisplayName("Given an empty string in YAML, when constructing ReloadableString, then loads empty string")
    void reloadableString_loadsEmptyString_whenYamlIsEmpty() throws IOException {
        YamlDocument doc = createYaml("value: \"\"");
        ReloadableString rs = new ReloadableString(doc, Route.from("value"));
        assertEquals("", rs.getContent());
    }
}

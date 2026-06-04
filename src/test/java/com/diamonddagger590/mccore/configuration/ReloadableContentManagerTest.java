package com.diamonddagger590.mccore.configuration;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReloadableContentManagerTest {

    private YamlDocument yamlDocument;
    private ReloadableContentManager manager;

    @BeforeEach
    void setUp() throws IOException {
        String yaml = "value: 10\nname: hello\n";
        yamlDocument = YamlDocument.create(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
        manager = new ReloadableContentManager(null);
    }

    @Test
    @DisplayName("Given tracked content, when reloadAllContent is called, then all content is reloaded")
    void reloadAllContent_reloadsAll_whenContentTracked() {
        Route valueRoute = Route.from("value");
        Route nameRoute = Route.from("name");

        ReloadableContent<Integer> intContent = new ReloadableContent<>(yamlDocument, valueRoute, (doc, r) -> doc.getInt(r), 0);
        ReloadableContent<String> strContent = new ReloadableContent<>(yamlDocument, nameRoute, (doc, r) -> doc.getString(r), "stale");

        manager.trackReloadableContent(intContent);
        manager.trackReloadableContent(strContent);

        assertEquals(0, intContent.getContent());
        assertEquals("stale", strContent.getContent());

        manager.reloadAllContent();

        assertEquals(10, intContent.getContent());
        assertEquals("hello", strContent.getContent());
    }

    @Test
    @DisplayName("Given a collection of content, when trackReloadableContent with collection is called, then all are tracked")
    void trackReloadableContent_tracksCollection() {
        Route valueRoute = Route.from("value");
        Route nameRoute = Route.from("name");

        ReloadableContent<Integer> intContent = new ReloadableContent<>(yamlDocument, valueRoute, (doc, r) -> doc.getInt(r), 0);
        ReloadableContent<String> strContent = new ReloadableContent<>(yamlDocument, nameRoute, (doc, r) -> doc.getString(r), "stale");

        manager.trackReloadableContent(List.of(intContent, strContent));
        manager.reloadAllContent();

        assertEquals(10, intContent.getContent());
        assertEquals("hello", strContent.getContent());
    }

    @Test
    @DisplayName("Given no tracked content, when reloadAllContent is called, then completes without error")
    void reloadAllContent_completesSuccessfully_whenNoContentTracked() {
        assertDoesNotThrow(() -> manager.reloadAllContent());
    }

    @Test
    @DisplayName("Given content with a custom reload callback, when reloadAllContent is called, then callback is invoked")
    void reloadAllContent_invokesCallback_forEachTrackedContent() {
        AtomicInteger reloadCount = new AtomicInteger(0);
        Route route = Route.from("value");

        ReloadableContent<Integer> content = new ReloadableContent<>(yamlDocument, route, (doc, r) -> {
            reloadCount.incrementAndGet();
            return doc.getInt(r);
        }, 0);

        manager.trackReloadableContent(content);

        assertEquals(0, reloadCount.get());
        manager.reloadAllContent();
        assertEquals(1, reloadCount.get());
        manager.reloadAllContent();
        assertEquals(2, reloadCount.get());
    }

    @Test
    @DisplayName("Given the same content tracked twice, when reloadAllContent is called, then it is only reloaded once due to Set")
    void trackReloadableContent_deduplicates_whenSameInstanceTrackedTwice() {
        AtomicInteger reloadCount = new AtomicInteger(0);
        Route route = Route.from("value");

        ReloadableContent<Integer> content = new ReloadableContent<>(yamlDocument, route, (doc, r) -> {
            reloadCount.incrementAndGet();
            return doc.getInt(r);
        }, 0);

        manager.trackReloadableContent(content);
        manager.trackReloadableContent(content);

        manager.reloadAllContent();
        assertEquals(1, reloadCount.get());
    }
}

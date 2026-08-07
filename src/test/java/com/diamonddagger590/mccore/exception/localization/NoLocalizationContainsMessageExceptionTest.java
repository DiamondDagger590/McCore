package com.diamonddagger590.mccore.exception.localization;

import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoLocalizationContainsMessageExceptionTest {

    @Test
    @DisplayName("Given a route and checked locales, when constructing, then both are stored")
    void getRoute_returnsRoute_whenConstructedWithRouteAndLocales() {
        Route route = Route.from("messages", "greeting");
        Set<Locale> locales = Set.of(Locale.ENGLISH, Locale.FRENCH);
        NoLocalizationContainsMessageException ex = new NoLocalizationContainsMessageException(route, locales);

        assertEquals(route, ex.getRoute());
        assertEquals(2, ex.getCheckedLocales().size());
        assertTrue(ex.getCheckedLocales().contains(Locale.ENGLISH));
        assertTrue(ex.getCheckedLocales().contains(Locale.FRENCH));
    }

    @Test
    @DisplayName("Given a route and locales, when calling getMessage, then message contains route and locale info")
    void getMessage_containsRouteAndLocaleInfo_whenConstructedWithRouteAndLocales() {
        Route route = Route.from("messages", "test");
        Set<Locale> locales = Set.of(Locale.ENGLISH);
        NoLocalizationContainsMessageException ex = new NoLocalizationContainsMessageException(route, locales);

        String message = ex.getMessage();
        assertNotNull(message);
        assertTrue(message.contains("localization"));
        assertTrue(message.contains(route.toString()));
        assertTrue(message.contains(Locale.ENGLISH.getDisplayName()));
    }

    @Test
    @DisplayName("Given checked locales, when getting them, then returned set is immutable")
    void getCheckedLocales_returnsImmutableSet_whenLocalesAreProvided() {
        Route route = Route.from("test");
        Set<Locale> locales = Set.of(Locale.ENGLISH);
        NoLocalizationContainsMessageException ex = new NoLocalizationContainsMessageException(route, locales);

        Set<Locale> returned = ex.getCheckedLocales();
        assertEquals(1, returned.size());
        assertThrows(UnsupportedOperationException.class, () -> returned.add(Locale.FRENCH));
    }

    @Test
    @DisplayName("Given an empty locale set, when constructing, then getCheckedLocales returns empty set")
    void getCheckedLocales_returnsEmptySet_whenConstructedWithEmptyLocales() {
        Route route = Route.from("messages", "empty");
        NoLocalizationContainsMessageException ex = new NoLocalizationContainsMessageException(route, Set.of());

        assertTrue(ex.getCheckedLocales().isEmpty());
    }

    @Test
    @DisplayName("Given multiple locales, when calling getMessage, then all locale names are joined with commas")
    void getMessage_joinsMultipleLocaleNames_whenMultipleLocalesProvided() {
        Route route = Route.from("messages", "multi");
        Set<Locale> locales = Set.of(Locale.ENGLISH, Locale.FRENCH);
        NoLocalizationContainsMessageException ex = new NoLocalizationContainsMessageException(route, locales);

        String message = ex.getMessage();
        assertNotNull(message);
        assertTrue(message.contains(Locale.ENGLISH.getDisplayName()));
        assertTrue(message.contains(Locale.FRENCH.getDisplayName()));
    }

    @Test
    @DisplayName("Given a NoLocalizationContainsMessageException, when checking type, then it is a RuntimeException")
    void noLocalizationContainsMessageException_isRuntimeException_always() {
        assertInstanceOf(RuntimeException.class,
            new NoLocalizationContainsMessageException(Route.from("test"), Set.of()));
    }
}

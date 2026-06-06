package com.diamonddagger590.mccore.exception.localization;

import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoLocalizationContainsMessageExceptionTest {

    @Test
    @DisplayName("Given a route and checked locales, when constructing, then both are stored")
    void constructor_storesRouteAndLocales() {
        Route route = Route.from("messages", "greeting");
        Set<Locale> locales = Set.of(Locale.ENGLISH, Locale.FRENCH);
        NoLocalizationContainsMessageException ex = new NoLocalizationContainsMessageException(route, locales);

        assertEquals(route, ex.getRoute());
        assertEquals(2, ex.getCheckedLocales().size());
        assertTrue(ex.getCheckedLocales().contains(Locale.ENGLISH));
        assertTrue(ex.getCheckedLocales().contains(Locale.FRENCH));
    }

    @Test
    @DisplayName("Given a route and locales, when calling getMessage, then message contains route info")
    void getMessage_containsRouteInfo() {
        Route route = Route.from("messages", "test");
        Set<Locale> locales = Set.of(Locale.ENGLISH);
        NoLocalizationContainsMessageException ex = new NoLocalizationContainsMessageException(route, locales);

        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("localization"));
    }

    @Test
    @DisplayName("Given checked locales, when getting them, then returned set is immutable")
    void getCheckedLocales_returnsImmutableCopy() {
        Route route = Route.from("test");
        Set<Locale> locales = Set.of(Locale.ENGLISH);
        NoLocalizationContainsMessageException ex = new NoLocalizationContainsMessageException(route, locales);

        Set<Locale> returned = ex.getCheckedLocales();
        assertInstanceOf(RuntimeException.class, ex);
        assertEquals(1, returned.size());
    }

    @Test
    @DisplayName("Given an empty locale set, when constructing, then getCheckedLocales returns empty set")
    void constructor_withEmptyLocales_returnsEmptySet() {
        Route route = Route.from("messages", "empty");
        NoLocalizationContainsMessageException ex = new NoLocalizationContainsMessageException(route, Set.of());

        assertTrue(ex.getCheckedLocales().isEmpty());
    }
}

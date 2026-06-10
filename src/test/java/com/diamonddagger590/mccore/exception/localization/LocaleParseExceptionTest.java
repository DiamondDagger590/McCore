package com.diamonddagger590.mccore.exception.localization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocaleParseExceptionTest {

    @Test
    @DisplayName("Given a parsed locale string, when constructing with default message, then auto-generated message contains the locale")
    void getMessage_containsLocale_whenConstructedWithParsedLocale() {
        LocaleParseException ex = new LocaleParseException("zz_ZZ");
        assertTrue(ex.getMessage().contains("zz_ZZ"));
        assertEquals("zz_ZZ", ex.getParsedLocale());
    }

    @Test
    @DisplayName("Given a parsed locale and custom message, when constructing, then custom message is used")
    void getMessage_returnsCustomMessage_whenConstructedWithParsedLocaleAndMessage() {
        LocaleParseException ex = new LocaleParseException("invalid", "Custom parse error");
        assertEquals("Custom parse error", ex.getMessage());
        assertEquals("invalid", ex.getParsedLocale());
    }

    @Test
    @DisplayName("Given an empty locale string, when constructing, then getParsedLocale returns empty string")
    void getParsedLocale_returnsEmptyString_whenConstructedWithEmptyLocale() {
        LocaleParseException ex = new LocaleParseException("");
        assertEquals("", ex.getParsedLocale());
    }

    @Test
    @DisplayName("Given a LocaleParseException, when checking type, then it is a RuntimeException")
    void localeParseException_isRuntimeException_always() {
        assertInstanceOf(RuntimeException.class, new LocaleParseException("test"));
    }
}

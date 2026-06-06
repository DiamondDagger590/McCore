package com.diamonddagger590.mccore.exception.localization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocaleParseExceptionTest {

    @Test
    @DisplayName("Given a parsed locale string, when constructing with default message, then auto-generated message contains the locale")
    void constructor_withParsedLocale_generatesMessage() {
        LocaleParseException ex = new LocaleParseException("zz_ZZ");
        assertTrue(ex.getMessage().contains("zz_ZZ"));
        assertEquals("zz_ZZ", ex.getParsedLocale());
    }

    @Test
    @DisplayName("Given a parsed locale and custom message, when constructing, then custom message is used")
    void constructor_withParsedLocaleAndMessage_usesCustomMessage() {
        LocaleParseException ex = new LocaleParseException("invalid", "Custom parse error");
        assertEquals("Custom parse error", ex.getMessage());
        assertEquals("invalid", ex.getParsedLocale());
    }

    @Test
    @DisplayName("Given an empty locale string, when constructing, then getParsedLocale returns empty string")
    void constructor_withEmptyLocale_returnsEmptyString() {
        LocaleParseException ex = new LocaleParseException("");
        assertEquals("", ex.getParsedLocale());
    }

    @Test
    @DisplayName("LocaleParseException is a RuntimeException")
    void localeParseException_isRuntimeException() {
        assertInstanceOf(RuntimeException.class, new LocaleParseException("test"));
    }
}

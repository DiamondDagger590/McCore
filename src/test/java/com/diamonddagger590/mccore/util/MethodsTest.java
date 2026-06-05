package com.diamonddagger590.mccore.util;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemFlag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MethodsTest {

    // ── isInt ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given a valid integer string, when checking isInt, then returns true")
    void isInt_returnsTrue_whenStringIsValidInteger() {
        assertTrue(Methods.isInt("42"));
    }

    @Test
    @DisplayName("Given a negative integer string, when checking isInt, then returns true")
    void isInt_returnsTrue_whenStringIsNegativeInteger() {
        assertTrue(Methods.isInt("-7"));
    }

    @Test
    @DisplayName("Given zero, when checking isInt, then returns true")
    void isInt_returnsTrue_whenStringIsZero() {
        assertTrue(Methods.isInt("0"));
    }

    @Test
    @DisplayName("Given a non-numeric string, when checking isInt, then returns false")
    void isInt_returnsFalse_whenStringIsNotNumeric() {
        assertFalse(Methods.isInt("abc"));
    }

    @Test
    @DisplayName("Given a decimal string, when checking isInt, then returns false")
    void isInt_returnsFalse_whenStringIsDecimal() {
        assertFalse(Methods.isInt("3.14"));
    }

    @Test
    @DisplayName("Given an empty string, when checking isInt, then returns false")
    void isInt_returnsFalse_whenStringIsEmpty() {
        assertFalse(Methods.isInt(""));
    }

    @Test
    @DisplayName("Given max integer value, when checking isInt, then returns true")
    void isInt_returnsTrue_whenStringIsMaxInt() {
        assertTrue(Methods.isInt(String.valueOf(Integer.MAX_VALUE)));
    }

    @Test
    @DisplayName("Given a value beyond int range, when checking isInt, then returns false")
    void isInt_returnsFalse_whenStringExceedsIntRange() {
        assertFalse(Methods.isInt("99999999999999"));
    }

    // ── getTimeInSeconds ────────────────────────────────────────────────────

    @Test
    @DisplayName("Given a seconds-only time string, when parsing, then returns correct duration")
    void getTimeInSeconds_returnsDuration_whenGivenSeconds() {
        Duration result = Methods.getTimeInSeconds("30s");
        assertEquals(Duration.ofSeconds(30), result);
    }

    @Test
    @DisplayName("Given a minutes-only time string, when parsing, then returns correct duration")
    void getTimeInSeconds_returnsDuration_whenGivenMinutes() {
        Duration result = Methods.getTimeInSeconds("5m");
        assertEquals(Duration.ofMinutes(5), result);
    }

    @Test
    @DisplayName("Given an hours-only time string, when parsing, then returns correct duration")
    void getTimeInSeconds_returnsDuration_whenGivenHours() {
        Duration result = Methods.getTimeInSeconds("2h");
        assertEquals(Duration.ofHours(2), result);
    }

    @Test
    @DisplayName("Given a days-only time string, when parsing, then returns correct duration")
    void getTimeInSeconds_returnsDuration_whenGivenDays() {
        Duration result = Methods.getTimeInSeconds("3d");
        assertEquals(Duration.ofDays(3), result);
    }

    @Test
    @DisplayName("Given a weeks-only time string, when parsing, then returns correct duration")
    void getTimeInSeconds_returnsDuration_whenGivenWeeks() {
        Duration result = Methods.getTimeInSeconds("2w");
        assertEquals(Duration.ofDays(14), result);
    }

    @Test
    @DisplayName("Given a years-only time string, when parsing, then returns correct duration")
    void getTimeInSeconds_returnsDuration_whenGivenYears() {
        Duration result = Methods.getTimeInSeconds("1y");
        assertEquals(Duration.ofDays(365), result);
    }

    @Test
    @DisplayName("Given a combined time string, when parsing, then returns summed duration")
    void getTimeInSeconds_returnsSummedDuration_whenGivenMultipleUnits() {
        Duration result = Methods.getTimeInSeconds("15s5m1h");
        Duration expected = Duration.ofHours(1).plusMinutes(5).plusSeconds(15);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Given an invalid time unit character, when parsing, then throws IllegalArgumentException")
    void getTimeInSeconds_throwsIllegalArgument_whenGivenInvalidUnit() {
        assertThrows(IllegalArgumentException.class, () -> Methods.getTimeInSeconds("5x"));
    }

    @Test
    @DisplayName("Given zero seconds, when parsing, then returns zero duration")
    void getTimeInSeconds_returnsZero_whenGivenZeroSeconds() {
        Duration result = Methods.getTimeInSeconds("0s");
        assertEquals(Duration.ZERO, result);
    }

    // ── getColor ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given 'aqua', when getting color, then returns Color.AQUA")
    void getColor_returnsAqua_whenGivenAqua() {
        assertEquals(Color.AQUA, Methods.getColor("aqua"));
    }

    @Test
    @DisplayName("Given 'black', when getting color, then returns Color.BLACK")
    void getColor_returnsBlack_whenGivenBlack() {
        assertEquals(Color.BLACK, Methods.getColor("black"));
    }

    @Test
    @DisplayName("Given 'blue', when getting color, then returns Color.BLUE")
    void getColor_returnsBlue_whenGivenBlue() {
        assertEquals(Color.BLUE, Methods.getColor("blue"));
    }

    @Test
    @DisplayName("Given 'fuchsia', when getting color, then returns Color.FUCHSIA")
    void getColor_returnsFuchsia_whenGivenFuchsia() {
        assertEquals(Color.FUCHSIA, Methods.getColor("fuchsia"));
    }

    @Test
    @DisplayName("Given 'gray', when getting color, then returns Color.GRAY")
    void getColor_returnsGray_whenGivenGray() {
        assertEquals(Color.GRAY, Methods.getColor("gray"));
    }

    @Test
    @DisplayName("Given 'green', when getting color, then returns Color.GREEN")
    void getColor_returnsGreen_whenGivenGreen() {
        assertEquals(Color.GREEN, Methods.getColor("green"));
    }

    @Test
    @DisplayName("Given 'lime', when getting color, then returns Color.LIME")
    void getColor_returnsLime_whenGivenLime() {
        assertEquals(Color.LIME, Methods.getColor("lime"));
    }

    @Test
    @DisplayName("Given 'maroon', when getting color, then returns Color.MAROON")
    void getColor_returnsMaroon_whenGivenMaroon() {
        assertEquals(Color.MAROON, Methods.getColor("maroon"));
    }

    @Test
    @DisplayName("Given 'navy', when getting color, then returns Color.NAVY")
    void getColor_returnsNavy_whenGivenNavy() {
        assertEquals(Color.NAVY, Methods.getColor("navy"));
    }

    @Test
    @DisplayName("Given 'olive', when getting color, then returns Color.OLIVE")
    void getColor_returnsOlive_whenGivenOlive() {
        assertEquals(Color.OLIVE, Methods.getColor("olive"));
    }

    @Test
    @DisplayName("Given 'orange', when getting color, then returns Color.ORANGE")
    void getColor_returnsOrange_whenGivenOrange() {
        assertEquals(Color.ORANGE, Methods.getColor("orange"));
    }

    @Test
    @DisplayName("Given 'purple', when getting color, then returns Color.PURPLE")
    void getColor_returnsPurple_whenGivenPurple() {
        assertEquals(Color.PURPLE, Methods.getColor("purple"));
    }

    @Test
    @DisplayName("Given 'red', when getting color, then returns Color.RED")
    void getColor_returnsRed_whenGivenRed() {
        assertEquals(Color.RED, Methods.getColor("red"));
    }

    @Test
    @DisplayName("Given 'silver', when getting color, then returns Color.SILVER")
    void getColor_returnsSilver_whenGivenSilver() {
        assertEquals(Color.SILVER, Methods.getColor("silver"));
    }

    @Test
    @DisplayName("Given 'teal', when getting color, then returns Color.TEAL")
    void getColor_returnsTeal_whenGivenTeal() {
        assertEquals(Color.TEAL, Methods.getColor("teal"));
    }

    @Test
    @DisplayName("Given 'yellow', when getting color, then returns Color.YELLOW")
    void getColor_returnsYellow_whenGivenYellow() {
        assertEquals(Color.YELLOW, Methods.getColor("yellow"));
    }

    @Test
    @DisplayName("Given an unknown color string, when getting color, then returns Color.WHITE")
    void getColor_returnsWhite_whenGivenUnknownColor() {
        assertEquals(Color.WHITE, Methods.getColor("unknown"));
    }

    @Test
    @DisplayName("Given uppercase color name, when getting color, then returns correct color")
    void getColor_returnsCorrectColor_whenGivenUppercase() {
        assertEquals(Color.RED, Methods.getColor("Red"));
    }

    // ── getDyeColor ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given 'orange', when getting dye color, then returns DyeColor.ORANGE")
    void getDyeColor_returnsOrange_whenGivenOrange() {
        assertEquals(DyeColor.ORANGE, Methods.getDyeColor("orange"));
    }

    @Test
    @DisplayName("Given 'magenta', when getting dye color, then returns DyeColor.MAGENTA")
    void getDyeColor_returnsMagenta_whenGivenMagenta() {
        assertEquals(DyeColor.MAGENTA, Methods.getDyeColor("magenta"));
    }

    @Test
    @DisplayName("Given 'fuchsia', when getting dye color, then returns DyeColor.MAGENTA")
    void getDyeColor_returnsMagenta_whenGivenFuchsia() {
        assertEquals(DyeColor.MAGENTA, Methods.getDyeColor("fuchsia"));
    }

    @Test
    @DisplayName("Given 'light_blue', when getting dye color, then returns DyeColor.LIGHT_BLUE")
    void getDyeColor_returnsLightBlue_whenGivenLightBlue() {
        assertEquals(DyeColor.LIGHT_BLUE, Methods.getDyeColor("light_blue"));
    }

    @Test
    @DisplayName("Given 'aqua', when getting dye color, then returns DyeColor.LIGHT_BLUE")
    void getDyeColor_returnsLightBlue_whenGivenAqua() {
        assertEquals(DyeColor.LIGHT_BLUE, Methods.getDyeColor("aqua"));
    }

    @Test
    @DisplayName("Given 'yellow', when getting dye color, then returns DyeColor.YELLOW")
    void getDyeColor_returnsYellow_whenGivenYellow() {
        assertEquals(DyeColor.YELLOW, Methods.getDyeColor("yellow"));
    }

    @Test
    @DisplayName("Given 'lime', when getting dye color, then returns DyeColor.LIME")
    void getDyeColor_returnsLime_whenGivenLime() {
        assertEquals(DyeColor.LIME, Methods.getDyeColor("lime"));
    }

    @Test
    @DisplayName("Given 'pink', when getting dye color, then returns DyeColor.PINK")
    void getDyeColor_returnsPink_whenGivenPink() {
        assertEquals(DyeColor.PINK, Methods.getDyeColor("pink"));
    }

    @Test
    @DisplayName("Given 'gray', when getting dye color, then returns DyeColor.GRAY")
    void getDyeColor_returnsGray_whenGivenGray() {
        assertEquals(DyeColor.GRAY, Methods.getDyeColor("gray"));
    }

    @Test
    @DisplayName("Given 'light_gray', when getting dye color, then returns DyeColor.LIGHT_GRAY")
    void getDyeColor_returnsLightGray_whenGivenLightGray() {
        assertEquals(DyeColor.LIGHT_GRAY, Methods.getDyeColor("light_gray"));
    }

    @Test
    @DisplayName("Given 'silver', when getting dye color, then returns DyeColor.LIGHT_GRAY")
    void getDyeColor_returnsLightGray_whenGivenSilver() {
        assertEquals(DyeColor.LIGHT_GRAY, Methods.getDyeColor("silver"));
    }

    @Test
    @DisplayName("Given 'cyan', when getting dye color, then returns DyeColor.CYAN")
    void getDyeColor_returnsCyan_whenGivenCyan() {
        assertEquals(DyeColor.CYAN, Methods.getDyeColor("cyan"));
    }

    @Test
    @DisplayName("Given 'teal', when getting dye color, then returns DyeColor.CYAN")
    void getDyeColor_returnsCyan_whenGivenTeal() {
        assertEquals(DyeColor.CYAN, Methods.getDyeColor("teal"));
    }

    @Test
    @DisplayName("Given 'purple', when getting dye color, then returns DyeColor.PURPLE")
    void getDyeColor_returnsPurple_whenGivenPurple() {
        assertEquals(DyeColor.PURPLE, Methods.getDyeColor("purple"));
    }

    @Test
    @DisplayName("Given 'blue', when getting dye color, then returns DyeColor.BLUE")
    void getDyeColor_returnsBlue_whenGivenBlue() {
        assertEquals(DyeColor.BLUE, Methods.getDyeColor("blue"));
    }

    @Test
    @DisplayName("Given 'navy', when getting dye color, then returns DyeColor.BLUE")
    void getDyeColor_returnsBlue_whenGivenNavy() {
        assertEquals(DyeColor.BLUE, Methods.getDyeColor("navy"));
    }

    @Test
    @DisplayName("Given 'brown', when getting dye color, then returns DyeColor.BROWN")
    void getDyeColor_returnsBrown_whenGivenBrown() {
        assertEquals(DyeColor.BROWN, Methods.getDyeColor("brown"));
    }

    @Test
    @DisplayName("Given 'green', when getting dye color, then returns DyeColor.GREEN")
    void getDyeColor_returnsGreen_whenGivenGreen() {
        assertEquals(DyeColor.GREEN, Methods.getDyeColor("green"));
    }

    @Test
    @DisplayName("Given 'olive', when getting dye color, then returns DyeColor.GREEN")
    void getDyeColor_returnsGreen_whenGivenOlive() {
        assertEquals(DyeColor.GREEN, Methods.getDyeColor("olive"));
    }

    @Test
    @DisplayName("Given 'red', when getting dye color, then returns DyeColor.RED")
    void getDyeColor_returnsRed_whenGivenRed() {
        assertEquals(DyeColor.RED, Methods.getDyeColor("red"));
    }

    @Test
    @DisplayName("Given 'maroon', when getting dye color, then returns DyeColor.RED")
    void getDyeColor_returnsRed_whenGivenMaroon() {
        assertEquals(DyeColor.RED, Methods.getDyeColor("maroon"));
    }

    @Test
    @DisplayName("Given 'black', when getting dye color, then returns DyeColor.BLACK")
    void getDyeColor_returnsBlack_whenGivenBlack() {
        assertEquals(DyeColor.BLACK, Methods.getDyeColor("black"));
    }

    @Test
    @DisplayName("Given an unknown dye color string, when getting dye color, then returns DyeColor.WHITE")
    void getDyeColor_returnsWhite_whenGivenUnknownColor() {
        assertEquals(DyeColor.WHITE, Methods.getDyeColor("unknown"));
    }

    // ── getFlag ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given a valid flag name, when getting flag, then returns matching ItemFlag")
    void getFlag_returnsFlag_whenGivenValidName() {
        Optional<ItemFlag> result = Methods.getFlag("HIDE_ENCHANTS");
        assertTrue(result.isPresent());
        assertEquals(ItemFlag.HIDE_ENCHANTS, result.get());
    }

    @Test
    @DisplayName("Given a lowercase flag name, when getting flag, then returns matching ItemFlag")
    void getFlag_returnsFlag_whenGivenLowercaseName() {
        Optional<ItemFlag> result = Methods.getFlag("hide_enchants");
        assertTrue(result.isPresent());
        assertEquals(ItemFlag.HIDE_ENCHANTS, result.get());
    }

    @Test
    @DisplayName("Given an invalid flag name, when getting flag, then returns empty Optional")
    void getFlag_returnsEmpty_whenGivenInvalidName() {
        Optional<ItemFlag> result = Methods.getFlag("NOT_A_FLAG");
        assertFalse(result.isPresent());
    }

    // ── getRGB ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given a valid RGB string, when getting color, then returns correct Color")
    void getRGB_returnsColor_whenGivenValidRGBString() {
        Optional<Color> result = Methods.getRGB("255,128,0");
        assertTrue(result.isPresent());
        assertEquals(Color.fromRGB(255, 128, 0), result.get());
    }

    @Test
    @DisplayName("Given an RGB string with too few values, when getting color, then returns empty")
    void getRGB_returnsEmpty_whenGivenTooFewValues() {
        Optional<Color> result = Methods.getRGB("255,128");
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Given an RGB string with too many values, when getting color, then returns empty")
    void getRGB_returnsEmpty_whenGivenTooManyValues() {
        Optional<Color> result = Methods.getRGB("255,128,0,255");
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Given black RGB values, when getting color, then returns black color")
    void getRGB_returnsBlack_whenGivenZeroValues() {
        Optional<Color> result = Methods.getRGB("0,0,0");
        assertTrue(result.isPresent());
        assertEquals(Color.fromRGB(0, 0, 0), result.get());
    }

    @Test
    @DisplayName("Given white RGB values, when getting color, then returns white color")
    void getRGB_returnsWhite_whenGivenMaxValues() {
        Optional<Color> result = Methods.getRGB("255,255,255");
        assertTrue(result.isPresent());
        assertEquals(Color.fromRGB(255, 255, 255), result.get());
    }

    // ── toRoutePath ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given a single path element, when creating route path, then returns that element")
    void toRoutePath_returnsSingleElement_whenGivenOnePath() {
        assertEquals("settings", Methods.toRoutePath("settings"));
    }

    @Test
    @DisplayName("Given multiple path elements, when creating route path, then joins with dots")
    void toRoutePath_joinsPaths_whenGivenMultipleElements() {
        assertEquals("settings.gui.title", Methods.toRoutePath("settings", "gui", "title"));
    }

    @Test
    @DisplayName("Given two path elements, when creating route path, then joins with single dot")
    void toRoutePath_joinsTwoElements_whenGivenTwoPaths() {
        assertEquals("database.host", Methods.toRoutePath("database", "host"));
    }

    // ── lookAt ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given origin and target on same X-Z, when calculating lookAt, then pitch points down")
    void lookAt_setsPitch_whenTargetIsBelow() {
        org.bukkit.Location origin = new org.bukkit.Location(null, 0, 10, 0);
        org.bukkit.Location target = new org.bukkit.Location(null, 0, 0, 0);
        org.bukkit.Location result = Methods.lookAt(origin, target);
        assertTrue(result.getPitch() > 0, "Pitch should be positive when looking down");
    }

    @Test
    @DisplayName("Given origin and target at same height, when calculating lookAt, then pitch is near zero")
    void lookAt_setsPitchNearZero_whenTargetIsSameHeight() {
        org.bukkit.Location origin = new org.bukkit.Location(null, 0, 10, 0);
        org.bukkit.Location target = new org.bukkit.Location(null, 10, 10, 0);
        org.bukkit.Location result = Methods.lookAt(origin, target);
        assertEquals(0.0, result.getPitch(), 0.01);
    }

    @Test
    @DisplayName("Given origin and target, when calculating lookAt, then original location is not modified")
    void lookAt_doesNotModifyOriginal_whenCalled() {
        org.bukkit.Location origin = new org.bukkit.Location(null, 5, 10, 5);
        float originalYaw = origin.getYaw();
        float originalPitch = origin.getPitch();
        org.bukkit.Location target = new org.bukkit.Location(null, 10, 5, 10);
        Methods.lookAt(origin, target);
        assertEquals(originalYaw, origin.getYaw());
        assertEquals(originalPitch, origin.getPitch());
    }

    @Test
    @DisplayName("Given target directly in front on Z-axis, when calculating lookAt, then yaw magnitude is 180")
    void lookAt_setsYaw_whenTargetIsOnNegativeZ() {
        org.bukkit.Location origin = new org.bukkit.Location(null, 0, 0, 0);
        org.bukkit.Location target = new org.bukkit.Location(null, 0, 0, -10);
        org.bukkit.Location result = Methods.lookAt(origin, target);
        assertEquals(180.0, Math.abs(result.getYaw()), 0.01);
    }

    // ── getMinecraftKey ─────────────────────────────────────────────────────

    @SuppressWarnings("deprecation")
    @Test
    @DisplayName("Given a key string, when getting minecraft key, then returns NamespacedKey with minecraft namespace")
    void getMinecraftKey_returnsMinecraftNamespace_whenGivenKey() {
        org.bukkit.NamespacedKey key = Methods.getMinecraftKey("stone");
        assertEquals("minecraft", key.getNamespace());
        assertEquals("stone", key.getKey());
    }

    @SuppressWarnings("deprecation")
    @Test
    @DisplayName("Given an uppercase key, when getting minecraft key, then returns lowercase key")
    void getMinecraftKey_lowercasesKey_whenGivenUppercase() {
        org.bukkit.NamespacedKey key = Methods.getMinecraftKey("STONE");
        assertEquals("stone", key.getKey());
    }
}

package com.diamonddagger590.mccore.util;

import com.diamonddagger590.mccore.testing.CorePluginTestHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.inventory.ItemFlag;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MethodsTest {

    @BeforeAll
    static void setUpPlugin() {
        CorePluginTestHelper.installMinimalInstance();
    }

    @AfterAll
    static void tearDownPlugin() {
        CorePluginTestHelper.uninstallInstance();
    }

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
    @DisplayName("Given an empty string, when parsing time, then returns zero duration")
    void getTimeInSeconds_returnsZero_whenGivenEmptyString() {
        Duration result = Methods.getTimeInSeconds("");
        assertEquals(Duration.ZERO, result);
    }

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

    @Test
    @DisplayName("Given an uppercase unit, when parsing, then parses case-insensitively")
    void getTimeInSeconds_parsesCaseInsensitively_whenGivenUppercaseUnit() {
        assertEquals(Duration.ofHours(24), Methods.getTimeInSeconds("24H"));
        assertEquals(Duration.ofDays(3), Methods.getTimeInSeconds("3D"));
        // Cover the remaining uppercase units so all six of the case-insensitive mappings are exercised.
        assertEquals(Duration.ofDays(14), Methods.getTimeInSeconds("2W"));
        assertEquals(Duration.ofDays(365), Methods.getTimeInSeconds("1Y"));
        assertEquals(Duration.ofSeconds(30), Methods.getTimeInSeconds("30S"));
    }

    @Test
    @DisplayName("Given a mixed-case combined string, when parsing, then sums all units")
    void getTimeInSeconds_sumsUnits_whenGivenMixedCase() {
        Duration result = Methods.getTimeInSeconds("1D12H30M");
        assertEquals(Duration.ofDays(1).plusHours(12).plusMinutes(30), result);
    }

    @Test
    @DisplayName("Given a bare number with no unit, when parsing, then treats it as seconds")
    void getTimeInSeconds_treatsBareNumberAsSeconds_whenNoUnitGiven() {
        assertEquals(Duration.ofSeconds(86400), Methods.getTimeInSeconds("86400"));
    }

    @Test
    @DisplayName("Given trailing digits after a unit, when parsing, then treats the trailing digits as seconds")
    void getTimeInSeconds_treatsTrailingDigitsAsSeconds_whenDigitsFollowUnit() {
        Duration result = Methods.getTimeInSeconds("1h30");
        assertEquals(Duration.ofHours(1).plusSeconds(30), result);
    }

    @Test
    @DisplayName("Given whitespace between units, when parsing, then ignores the whitespace")
    void getTimeInSeconds_ignoresWhitespace_whenPresentBetweenUnits() {
        Duration result = Methods.getTimeInSeconds("1d 12h");
        assertEquals(Duration.ofDays(1).plusHours(12), result);
    }

    @Test
    @DisplayName("Given a whitespace-only string, when parsing, then returns zero duration")
    void getTimeInSeconds_returnsZero_whenGivenWhitespaceOnly() {
        assertEquals(Duration.ZERO, Methods.getTimeInSeconds("   "));
    }

    @Test
    @DisplayName("Given a unit with no preceding number, when parsing, then throws IllegalArgumentException")
    void getTimeInSeconds_throwsIllegalArgument_whenUnitHasNoNumber() {
        assertThrows(IllegalArgumentException.class, () -> Methods.getTimeInSeconds("h"));
    }

    @Test
    @DisplayName("Given a unit immediately after whitespace with no number, when parsing, then throws IllegalArgumentException")
    void getTimeInSeconds_throwsIllegalArgument_whenUnitFollowsWhitespaceWithNoNumber() {
        // "1h" flushes numberBuilder; the whitespace-skip leaves 'm' facing an empty numberBuilder.
        assertThrows(IllegalArgumentException.class, () -> Methods.getTimeInSeconds("1h m"));
    }

    // ── getProgressBarAsString ────────────────────────────────────────────

    @Test
    @DisplayName("Given negative progress, when getting progress bar string, then throws IllegalArgumentException")
    void getProgressBarAsString_throwsIllegalArgument_whenProgressIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> Methods.getProgressBarAsString(-0.1, 10));
    }

    @Test
    @DisplayName("Given progress above 1.0, when getting progress bar string, then throws IllegalArgumentException")
    void getProgressBarAsString_throwsIllegalArgument_whenProgressAboveOne() {
        assertThrows(IllegalArgumentException.class, () -> Methods.getProgressBarAsString(1.1, 10));
    }

    @Test
    @DisplayName("Given zero progress, when getting progress bar string, then contains all red bars")
    void getProgressBarAsString_containsAllRedBars_whenProgressIsZero() {
        String result = Methods.getProgressBarAsString(0.0, 10);
        assertNotNull(result);
        assertTrue(result.contains("<green></green>"), "Should have empty green section");
        assertTrue(result.contains("<color:#ff1418>" + "|".repeat(10) + "</color>"), "Should have 10 red bars");
    }

    @Test
    @DisplayName("Given full progress, when getting progress bar string, then contains all green bars")
    void getProgressBarAsString_containsAllGreenBars_whenProgressIsFull() {
        String result = Methods.getProgressBarAsString(1.0, 10);
        assertNotNull(result);
        assertTrue(result.contains("<green>" + "|".repeat(10) + "</green>"), "Should have 10 green bars");
    }

    @Test
    @DisplayName("Given partial progress with remainder <= 0.25, when getting progress bar string, then includes yellow-green transition color")
    void getProgressBarAsString_includesYellowGreenColor_whenRemainderIsLow() {
        String result = Methods.getProgressBarAsString(0.1, 4);
        assertNotNull(result);
        assertTrue(result.contains("<color:#c9ff29>|</color>"), "Should have yellow-green transition bar");
    }

    @Test
    @DisplayName("Given partial progress with remainder <= 0.50, when getting progress bar string, then includes gold transition color")
    void getProgressBarAsString_includesGoldColor_whenRemainderIsMediumLow() {
        String result = Methods.getProgressBarAsString(0.4, 4);
        assertNotNull(result);
        assertTrue(result.contains("<color:#ffcb21>|</color>"), "Should have gold transition bar");
    }

    @Test
    @DisplayName("Given partial progress with remainder <= 0.75, when getting progress bar string, then includes orange transition color")
    void getProgressBarAsString_includesOrangeColor_whenRemainderIsMediumHigh() {
        String result = Methods.getProgressBarAsString(0.6, 4);
        assertNotNull(result);
        assertTrue(result.contains("<color:#ff822e>|</color>"), "Should have orange transition bar");
    }

    @Test
    @DisplayName("Given partial progress with remainder > 0.75, when getting progress bar string, then includes red-orange transition color")
    void getProgressBarAsString_includesRedOrangeColor_whenRemainderIsHigh() {
        String result = Methods.getProgressBarAsString(0.8, 4);
        assertNotNull(result);
        assertTrue(result.contains("<color:#ff6417>|</color>"), "Should have red-orange transition bar");
    }

    @Test
    @DisplayName("Given half progress, when getting progress bar string, then has green and red sections")
    void getProgressBarAsString_hasMixedColors_whenProgressIsHalf() {
        String result = Methods.getProgressBarAsString(0.5, 10);
        assertNotNull(result);
        assertTrue(result.contains("<green>"), "Should have green section");
        assertTrue(result.contains("<color:#ff1418>"), "Should have red section");
    }

    // ── getProgressBar ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Given negative progress, when getting progress bar, then throws IllegalArgumentException")
    void getProgressBar_throwsIllegalArgument_whenProgressIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> Methods.getProgressBar(-0.1, 10));
    }

    @Test
    @DisplayName("Given progress above 1.0, when getting progress bar, then throws IllegalArgumentException")
    void getProgressBar_throwsIllegalArgument_whenProgressAboveOne() {
        assertThrows(IllegalArgumentException.class, () -> Methods.getProgressBar(1.1, 10));
    }

    @Test
    @DisplayName("Given zero progress, when getting progress bar, then returns Component containing only pipe characters")
    void getProgressBar_returnsComponentWithBars_whenProgressIsZero() {
        Component result = Methods.getProgressBar(0.0, 10);
        String plain = PlainTextComponentSerializer.plainText().serialize(result);
        assertEquals("|".repeat(10), plain, "Plain text should contain exactly 10 pipe characters");
    }

    @Test
    @DisplayName("Given full progress, when getting progress bar, then returns Component containing only pipe characters")
    void getProgressBar_returnsComponentWithBars_whenProgressIsFull() {
        Component result = Methods.getProgressBar(1.0, 10);
        String plain = PlainTextComponentSerializer.plainText().serialize(result);
        assertEquals("|".repeat(10), plain, "Plain text should contain exactly 10 pipe characters");
    }

    @Test
    @DisplayName("Given partial progress with low remainder, when getting progress bar, then returns Component with correct bar count")
    void getProgressBar_returnsComponentWithBars_whenRemainderIsLow() {
        Component result = Methods.getProgressBar(0.1, 4);
        String plain = PlainTextComponentSerializer.plainText().serialize(result);
        assertEquals("|".repeat(4), plain, "Plain text should contain exactly 4 pipe characters");
    }

    @Test
    @DisplayName("Given partial progress with medium-low remainder, when getting progress bar, then returns Component with correct bar count")
    void getProgressBar_returnsComponentWithBars_whenRemainderIsMediumLow() {
        Component result = Methods.getProgressBar(0.4, 4);
        String plain = PlainTextComponentSerializer.plainText().serialize(result);
        assertEquals("|".repeat(4), plain, "Plain text should contain exactly 4 pipe characters");
    }

    @Test
    @DisplayName("Given partial progress with medium-high remainder, when getting progress bar, then returns Component with correct bar count")
    void getProgressBar_returnsComponentWithBars_whenRemainderIsMediumHigh() {
        Component result = Methods.getProgressBar(0.6, 4);
        String plain = PlainTextComponentSerializer.plainText().serialize(result);
        assertEquals("|".repeat(4), plain, "Plain text should contain exactly 4 pipe characters");
    }

    @Test
    @DisplayName("Given partial progress with high remainder, when getting progress bar, then returns Component with correct bar count")
    void getProgressBar_returnsComponentWithBars_whenRemainderIsHigh() {
        Component result = Methods.getProgressBar(0.8, 4);
        String plain = PlainTextComponentSerializer.plainText().serialize(result);
        assertEquals("|".repeat(4), plain, "Plain text should contain exactly 4 pipe characters");
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
    @DisplayName("Given non-numeric RGB values, when getting color, then throws NumberFormatException")
    void getRGB_throwsNumberFormatException_whenGivenNonNumericValues() {
        assertThrows(NumberFormatException.class, () -> Methods.getRGB("red,green,blue"));
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

    @Test
    @DisplayName("Given target to the left (negative X), when calculating lookAt, then yaw reflects leftward direction")
    void lookAt_setsYaw_whenTargetIsOnNegativeX() {
        org.bukkit.Location origin = new org.bukkit.Location(null, 0, 0, 0);
        org.bukkit.Location target = new org.bukkit.Location(null, -10, 0, 0);
        org.bukkit.Location result = Methods.lookAt(origin, target);
        assertEquals(270.0, Math.abs(result.getYaw()), 0.01);
    }

    @Test
    @DisplayName("Given target with negative dx and negative dz, when calculating lookAt, then yaw is computed correctly")
    void lookAt_computesYaw_whenBothDxAndDzAreNegative() {
        org.bukkit.Location origin = new org.bukkit.Location(null, 10, 10, 10);
        org.bukkit.Location target = new org.bukkit.Location(null, 0, 10, 0);
        org.bukkit.Location result = Methods.lookAt(origin, target);
        assertTrue(Float.isFinite(result.getYaw()), "Yaw should be finite for negative dx and dz");
    }

    @Test
    @DisplayName("Given target directly ahead on positive Z with dx=0, when calculating lookAt, then yaw is 0")
    void lookAt_setsYawZero_whenTargetIsDirectlyAheadOnPositiveZ() {
        org.bukkit.Location origin = new org.bukkit.Location(null, 0, 10, 0);
        org.bukkit.Location target = new org.bukkit.Location(null, 0, 10, 10);
        org.bukkit.Location result = Methods.lookAt(origin, target);
        assertEquals(0.0, result.getYaw(), 0.01);
    }

    @Test
    @DisplayName("Given target above the origin, when calculating lookAt, then pitch is negative")
    void lookAt_setsNegativePitch_whenTargetIsAbove() {
        org.bukkit.Location origin = new org.bukkit.Location(null, 0, 0, 0);
        org.bukkit.Location target = new org.bukkit.Location(null, 10, 10, 0);
        org.bukkit.Location result = Methods.lookAt(origin, target);
        assertTrue(result.getPitch() < 0, "Pitch should be negative when looking up");
    }

    @Test
    @DisplayName("Given target at same position, when calculating lookAt, then yaw and pitch are not infinite")
    void lookAt_returnsNonInfiniteValues_whenTargetIsSamePosition() {
        org.bukkit.Location origin = new org.bukkit.Location(null, 5, 5, 5);
        org.bukkit.Location target = new org.bukkit.Location(null, 5, 5, 5);
        org.bukkit.Location result = Methods.lookAt(origin, target);
        assertFalse(Float.isInfinite(result.getYaw()), "Yaw should not be infinite");
        assertFalse(Float.isInfinite(result.getPitch()), "Pitch should not be infinite");
    }

    @Test
    @DisplayName("Given target on positive Z with same X, when calculating lookAt, then yaw is zero and pitch is near zero")
    void lookAt_setsYawToZero_whenTargetIsOnPositiveZWithSameX() {
        org.bukkit.Location origin = new org.bukkit.Location(null, 0, 0, 0);
        org.bukkit.Location target = new org.bukkit.Location(null, 0, 0, 10);
        org.bukkit.Location result = Methods.lookAt(origin, target);
        assertEquals(0.0, result.getYaw(), 0.01);
        assertEquals(0.0, result.getPitch(), 0.01);
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

    @Test
    @DisplayName("Given a location with a world, when serializing, then returns semicolon-delimited string")
    void serializeLocation_returnsDelimitedString_whenGivenValidLocation() {
        World mockWorld = mock(World.class);
        UUID worldUuid = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        when(mockWorld.getUID()).thenReturn(worldUuid);

        Location location = new Location(mockWorld, 100.5, 64.0, -200.75);
        String serialized = Methods.serializeLocation(location);

        assertEquals("100.5;64.0;-200.75;aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", serialized);
    }

    @Test
    @DisplayName("Given a location at origin, when serializing, then returns zeros with world UUID")
    void serializeLocation_returnsZeros_whenLocationIsOrigin() {
        World mockWorld = mock(World.class);
        UUID worldUuid = UUID.randomUUID();
        when(mockWorld.getUID()).thenReturn(worldUuid);

        Location location = new Location(mockWorld, 0, 0, 0);
        String serialized = Methods.serializeLocation(location);

        assertTrue(serialized.startsWith("0.0;0.0;0.0;"));
        assertTrue(serialized.endsWith(worldUuid.toString()));
    }

    @Test
    @DisplayName("Given a valid serialized location with existing world, when deserializing, then returns location")
    void deserializeLocation_returnsLocation_whenWorldExists() {
        UUID worldUuid = UUID.randomUUID();
        World mockWorld = mock(World.class);

        try (MockedStatic<Bukkit> bukkit = org.mockito.Mockito.mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld(worldUuid.toString())).thenReturn(mockWorld);

            Optional<Location> result = Methods.deserializeLocation("10;20;30;" + worldUuid);

            assertTrue(result.isPresent());
            Location loc = result.get();
            assertEquals(10, loc.getBlockX());
            assertEquals(20, loc.getBlockY());
            assertEquals(30, loc.getBlockZ());
        }
    }

    @Test
    @DisplayName("Given a valid serialized location with unknown world, when deserializing, then returns empty")
    void deserializeLocation_returnsEmpty_whenWorldDoesNotExist() {
        UUID worldUuid = UUID.randomUUID();

        try (MockedStatic<Bukkit> bukkit = org.mockito.Mockito.mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld(worldUuid.toString())).thenReturn(null);

            Optional<Location> result = Methods.deserializeLocation("10;20;30;" + worldUuid);

            assertTrue(result.isEmpty());
        }
    }

    @Test
    @DisplayName("Given an invalid serialized location with too few parts, when deserializing, then throws IllegalArgumentException")
    void deserializeLocation_throwsIllegalArgument_whenTooFewParts() {
        assertThrows(IllegalArgumentException.class, () -> Methods.deserializeLocation("10;20"));
    }

    @Test
    @DisplayName("Given an invalid serialized location with too many parts, when deserializing, then throws IllegalArgumentException")
    void deserializeLocation_throwsIllegalArgument_whenTooManyParts() {
        assertThrows(IllegalArgumentException.class, () -> Methods.deserializeLocation("10;20;30;world;extra"));
    }

    @Test
    @DisplayName("Given a serialized location from serializeLocation, when deserializing, then throws NumberFormatException because serializeLocation uses doubles but deserializeLocation uses Integer.parseInt")
    void deserializeLocation_throwsNumberFormatException_whenSerializedBySerializeLocation() {
        World mockWorld = mock(World.class);
        UUID worldUuid = UUID.randomUUID();
        when(mockWorld.getUID()).thenReturn(worldUuid);

        Location location = new Location(mockWorld, 100, 64, -200);
        String serialized = Methods.serializeLocation(location);

        assertThrows(NumberFormatException.class, () -> Methods.deserializeLocation(serialized));
    }

    @Test
    @DisplayName("Given target with negative dx, when calculating lookAt, then yaw is set correctly")
    void lookAt_setsYaw_whenTargetHasNegativeDx() {
        Location origin = new Location(null, 10, 0, 0);
        Location target = new Location(null, -10, 0, 0);
        Location result = Methods.lookAt(origin, target);
        // dx < 0, so yaw starts at 1.5 * PI
        assertTrue(Math.abs(result.getYaw()) > 0, "Yaw should be non-zero when target is behind on X");
    }

    @Test
    @DisplayName("Given target with positive dx, when calculating lookAt, then yaw is set correctly")
    void lookAt_setsYaw_whenTargetHasPositiveDx() {
        Location origin = new Location(null, 0, 0, 0);
        Location target = new Location(null, 10, 0, 5);
        Location result = Methods.lookAt(origin, target);
        assertTrue(Math.abs(result.getYaw()) > 0, "Yaw should be non-zero when target has positive dx");
    }

    @Test
    @DisplayName("Given target above origin, when calculating lookAt, then pitch is negative (looking up)")
    void lookAt_setsPitchNegative_whenTargetIsAbove() {
        Location origin = new Location(null, 0, 0, 0);
        Location target = new Location(null, 10, 10, 0);
        Location result = Methods.lookAt(origin, target);
        assertTrue(result.getPitch() < 0, "Pitch should be negative when looking up");
    }

    @Test
    @DisplayName("Given no path elements, when creating route path, then returns empty string")
    void toRoutePath_returnsEmpty_whenGivenNoPaths() {
        assertEquals("", Methods.toRoutePath());
    }

    @Test
    @DisplayName("Given four path elements, when creating route path, then joins all with dots")
    void toRoutePath_joinsAll_whenGivenFourElements() {
        assertEquals("a.b.c.d", Methods.toRoutePath("a", "b", "c", "d"));
    }
}

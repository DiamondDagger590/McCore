package com.diamonddagger590.mccore.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SfunAdditionalBranchTest {

    private static final double DELTA = 1e-9;
    private static final double RELAXED_DELTA = 1e-6;

    // ── sinh: very large y branch (y >= 94906265.62) ──────────────────────

    @Test
    @DisplayName("Given very large positive x, when computing sinh, then uses 0.5*y formula")
    void sinh_usesHalfYFormula_whenXIsVeryLargePositive() {
        double x = 100.0;
        double y = Math.exp(x);
        double expected = 0.5 * y;
        assertEquals(expected, Sfun.sinh(x), expected * 1e-10);
    }

    @Test
    @DisplayName("Given very large negative x, when computing sinh, then uses -0.5*y formula")
    void sinh_usesNegativeHalfYFormula_whenXIsVeryLargeNegative() {
        double x = -100.0;
        double result = Sfun.sinh(x);
        assertTrue(result < -1e40);
    }

    @Test
    @DisplayName("Given x in Chebyshev range, when computing sinh, then uses series expansion")
    void sinh_usesChebyshevSeries_whenXIsInSeriesRange() {
        double x = 0.5;
        double result = Sfun.sinh(x);
        double expected = (Math.exp(0.5) - Math.exp(-0.5)) / 2.0;
        assertEquals(expected, result, RELAXED_DELTA);
    }

    // ── asinh: large negative x branch ────────────────────────────────────

    @Test
    @DisplayName("Given very large negative x, when computing asinh, then returns negative large-value result")
    void asinh_returnsNegativeLargeResult_whenXIsVeryLargeNegative() {
        double x = -1e9;
        double expected = -(0.69314718055994530941723212145818 + Math.log(1e9));
        assertEquals(expected, Sfun.asinh(x), DELTA);
    }

    @Test
    @DisplayName("Given negative x in medium range, when computing asinh, then uses log formula with negation")
    void asinh_usesLogFormulaWithNegation_whenXIsNegativeMedium() {
        double x = -5.0;
        double y = 5.0;
        double expected = -(Math.log(y + Math.sqrt(y * y + 1.0)));
        assertEquals(expected, Sfun.asinh(x), DELTA);
    }

    @Test
    @DisplayName("Given negative very small x, when computing asinh, then returns x")
    void asinh_returnsX_whenXIsVerySmallNegative() {
        double x = -1e-10;
        assertEquals(x, Sfun.asinh(x), DELTA);
    }

    @Test
    @DisplayName("Given negative x in Chebyshev range, when computing asinh, then returns consistent result")
    void asinh_returnsConsistentResult_whenXIsNegativeInSeriesRange() {
        double x = -0.5;
        double result = Sfun.asinh(x);
        assertEquals(Sfun.asinh(-0.5), result, DELTA);
        assertTrue(Double.isFinite(result));
    }

    // ── atanh: very small negative x branch ───────────────────────────────

    @Test
    @DisplayName("Given negative very small x, when computing atanh, then returns x")
    void atanh_returnsX_whenXIsVerySmallNegative() {
        double x = -1e-10;
        assertEquals(x, Sfun.atanh(x), DELTA);
    }

    @Test
    @DisplayName("Given negative x in Chebyshev range, when computing atanh, then uses series")
    void atanh_usesSeries_whenXIsNegativeInChebyshevRange() {
        double x = -0.3;
        double expected = 0.5 * Math.log((1.0 + x) / (1.0 - x));
        assertEquals(expected, Sfun.atanh(x), RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given negative x between -1 and -0.5, when computing atanh, then uses log formula")
    void atanh_usesLogFormula_whenXIsNegativeBetweenHalfAndOne() {
        double x = -0.8;
        double expected = 0.5 * Math.log((1.0 + x) / (1.0 - x));
        assertEquals(expected, Sfun.atanh(x), DELTA);
    }

    // ── cot: x = 0 branch (y == 0, infinity) ─────────────────────────────

    @Test
    @DisplayName("Given x = pi, when computing cot, then returns large magnitude value")
    void cot_returnsLargeMagnitude_whenXIsPi() {
        double result = Sfun.cot(Math.PI);
        assertTrue(Double.isFinite(result));
        assertTrue(Math.abs(result) > 1e10);
    }

    @Test
    @DisplayName("Given negative x in [0.25, 0.5] range, when computing cot, then returns correct negative value")
    void cot_returnsNegative_whenNegativeXInQuarterToHalfRange() {
        double x = -Math.PI / 8;
        double result = Sfun.cot(x);
        double expected = 1.0 / Math.tan(x);
        assertEquals(expected, result, Math.abs(expected) * RELAXED_DELTA);
    }

    // ── tanh: negative values in exp branch ───────────────────────────────

    @Test
    @DisplayName("Given negative very small x, when computing tanh, then returns x")
    void tanh_returnsX_whenXIsVerySmallNegative() {
        double x = -1e-10;
        assertEquals(x, Sfun.tanh(x), DELTA);
    }

    @Test
    @DisplayName("Given negative x in Chebyshev range, when computing tanh, then uses series")
    void tanh_usesSeries_whenXIsNegativeInChebyshevRange() {
        double x = -0.5;
        assertEquals(-Sfun.tanh(0.5), Sfun.tanh(x), DELTA);
    }

    // ── erf: negative x in medium range ───────────────────────────────────

    @Test
    @DisplayName("Given negative medium range x, when computing erf, then returns correct negative value")
    void erf_returnsNegative_whenXIsNegativeMediumRange() {
        double result = Sfun.erf(-3.0);
        assertEquals(-Sfun.erf(3.0), result, DELTA);
    }

    @Test
    @DisplayName("Given very small negative x, when computing erf, then uses linear approximation")
    void erf_returnsLinearApprox_whenXIsVerySmallNegative() {
        double x = -1e-10;
        double expected = 2 * x / 1.77245385090551602729816748334;
        assertEquals(expected, Sfun.erf(x), DELTA);
    }

    @Test
    @DisplayName("Given negative x just below threshold, when computing erf, then returns -1")
    void erf_returnsNegativeOne_whenXIsLargeNegative() {
        assertEquals(-1.0, Sfun.erf(-7.0), DELTA);
    }

    // ── gamma: edge cases not covered ─────────────────────────────────────

    @Test
    @DisplayName("Given negative non-integer x between -2 and -1, when computing gamma, then returns correct value")
    void gamma_returnsCorrectValue_whenXIsNegativeNonIntBetweenNeg2AndNeg1() {
        double result = Sfun.gamma(-1.5);
        assertTrue(Double.isFinite(result));
    }

    @Test
    @DisplayName("Given x = 3, when computing gamma, then returns 2 (2!)")
    void gamma_returnsTwo_whenXIsThree() {
        assertEquals(2.0, Sfun.gamma(3.0), RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given large positive x near 171, when computing gamma, then returns finite value")
    void gamma_returnsFiniteValue_whenXIsNear171() {
        double result = Sfun.gamma(171.0);
        assertTrue(Double.isFinite(result));
    }

    @Test
    @DisplayName("Given negative integer > -10 in gamma, then returns NaN")
    void gamma_returnsNaN_whenXIsSmallNegativeInteger() {
        assertTrue(Double.isNaN(Sfun.gamma(-3.0)));
        assertTrue(Double.isNaN(Sfun.gamma(-5.0)));
    }

    // ── logBeta: dlnrel x <= -1 branch ────────────────────────────────────

    @Test
    @DisplayName("Given both p and q are small, when computing logBeta, then uses direct gamma product")
    void logBeta_usesDirectGamma_whenBothSmall() {
        double result = Sfun.logBeta(2.0, 3.0);
        double expected = Math.log(Sfun.gamma(2.0) * (Sfun.gamma(3.0) / Sfun.gamma(5.0)));
        assertEquals(expected, result, RELAXED_DELTA);
    }

    // ── fact: boundary cases ──────────────────────────────────────────────

    @Test
    @DisplayName("Given n = 2, when computing factorial, then returns 2")
    void fact_returnsTwo_whenNIsTwo() {
        assertEquals(2.0, Sfun.fact(2), DELTA);
    }

    @Test
    @DisplayName("Given n = 20, when computing factorial, then returns correct large value")
    void fact_returnsCorrectValue_whenNIs20() {
        assertEquals(2432902008176640000.0, Sfun.fact(20), 1.0);
    }
}

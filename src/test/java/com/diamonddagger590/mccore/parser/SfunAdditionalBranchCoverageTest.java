package com.diamonddagger590.mccore.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SfunAdditionalBranchCoverageTest {

    private static final double DELTA = 1e-9;

    @Test
    @DisplayName("Given x = 0.0, when computing cot, then returns positive infinity (y == 0.0 branch)")
    void cot_returnsPositiveInfinity_whenXIsZero() {
        assertEquals(Double.POSITIVE_INFINITY, Sfun.cot(0.0));
    }

    @Test
    @DisplayName("Given x = pi/4, when computing cot, then x != 0 and sign branch is exercised")
    void cot_exercisesSignBranch_whenXIsNonZero() {
        double result = Sfun.cot(Math.PI / 4);
        assertEquals(1.0, result, 1e-6);
    }

    @Test
    @DisplayName("Given negative integer -11, when computing gamma, then returns NaN (|x| > 10 negative integer branch)")
    void gamma_returnsNaN_whenXIsLargeNegativeInteger() {
        assertTrue(Double.isNaN(Sfun.gamma(-11.0)));
    }

    @Test
    @DisplayName("Given x = -170.7, when computing gamma, then returns 0 (underflow branch)")
    void gamma_returnsZero_whenXIsVeryLargeNegative() {
        assertEquals(0.0, Sfun.gamma(-170.7), DELTA);
    }

    @Test
    @DisplayName("Given x = 172, when computing gamma, then returns positive infinity (overflow branch)")
    void gamma_returnsInfinity_whenXExceedsOverflowThreshold() {
        assertEquals(Double.POSITIVE_INFINITY, Sfun.gamma(172.0));
    }

    @Test
    @DisplayName("Given large negative integer -11, when computing logGamma, then returns NaN (sinpiy == 0 branch)")
    void logGamma_returnsNaN_whenXIsLargeNegativeInteger() {
        assertTrue(Double.isNaN(Sfun.logGamma(-11.0)));
    }

    @Test
    @DisplayName("Given very small p and q, when computing logBeta, then exercises dlnrel path")
    void logBeta_exercisesDlnrelPath_whenBothArgsVerySmall() {
        double result = Sfun.logBeta(0.001, 0.001);
        assertEquals(7.6009008, result, 1e-4);
    }

    @Test
    @DisplayName("Given negative n, when computing fact, then returns NaN")
    void fact_returnsNaN_whenNIsNegative() {
        assertTrue(Double.isNaN(Sfun.fact(-1)));
    }

    @Test
    @DisplayName("Given n > 170, when computing fact, then returns positive infinity")
    void fact_returnsInfinity_whenNExceeds170() {
        assertEquals(Double.POSITIVE_INFINITY, Sfun.fact(171));
    }

    @Test
    @DisplayName("Given n = 0, when computing fact, then returns 1")
    void fact_returnsOne_whenNIsZero() {
        assertEquals(1.0, Sfun.fact(0), DELTA);
    }
}

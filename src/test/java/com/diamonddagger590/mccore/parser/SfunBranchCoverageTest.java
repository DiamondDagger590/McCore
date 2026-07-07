package com.diamonddagger590.mccore.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SfunBranchCoverageTest {

    private static final double DELTA = 1e-9;
    private static final double RELAXED_DELTA = 1e-6;

    @Test
    @DisplayName("Given positive infinity, when computing cosh, then returns positive infinity")
    void cosh_returnsPositiveInfinity_whenInputIsPositiveInfinity() {
        assertEquals(Double.POSITIVE_INFINITY, Sfun.cosh(Double.POSITIVE_INFINITY));
    }

    @Test
    @DisplayName("Given negative infinity, when computing cosh, then returns positive infinity")
    void cosh_returnsPositiveInfinity_whenInputIsNegativeInfinity() {
        assertEquals(Double.POSITIVE_INFINITY, Sfun.cosh(Double.NEGATIVE_INFINITY));
    }

    @Test
    @DisplayName("Given very large x, when computing cosh, then uses large-value formula without 1/y term")
    void cosh_usesLargeValueFormula_whenYExceedsThreshold() {
        double x = 710.0;
        double result = Sfun.cosh(x);
        assertTrue(Double.isInfinite(result) || result > 1e300);
    }

    @Test
    @DisplayName("Given x just above threshold, when computing cosh, then uses 0.5*y formula")
    void cosh_usesHalfY_whenExpAbsXExceedsThreshold() {
        double x = 100.0;
        double y = Math.exp(x);
        double expected = 0.5 * y;
        assertEquals(expected, Sfun.cosh(x), expected * 1e-10);
    }

    @Test
    @DisplayName("Given negative x between -4 and -1, when computing erfc, then hits y<=4 negative branch")
    void erfc_hitsNegativeYLe4Branch_whenXIsNegativeBetweenOneAndFour() {
        double result = Sfun.erfc(-2.0);
        assertTrue(result > 1.0 && result < 2.0, "erfc(-2) should be between 1 and 2, was " + result);
    }

    @Test
    @DisplayName("Given x = -3.5, when computing erfc, then returns value near 2")
    void erfc_returnsNearTwo_whenXIsNegative3Point5() {
        double result = Sfun.erfc(-3.5);
        assertTrue(result > 1.999 && result <= 2.0, "erfc(-3.5) should be very close to 2, was " + result);
    }

    @Test
    @DisplayName("Given negative x with |x| > 4 but |x| < 6.01, when computing erfc, then hits y>4 negative branch")
    void erfc_hitsNegativeYGt4Branch_whenAbsXExceedsFourButBelowThreshold() {
        double result = Sfun.erfc(-5.0);
        assertTrue(result > 1.99999 && result <= 2.0, "erfc(-5) should be extremely close to 2, was " + result);
    }

    @Test
    @DisplayName("Given erfc + erf = 1 for negative x, when computing both, then sum is 1")
    void erfc_complementsErf_whenXIsNegative() {
        double x = -2.0;
        assertEquals(1.0, Sfun.erf(x) + Sfun.erfc(x), DELTA);
    }

    @Test
    @DisplayName("Given very small positive x, when computing erfc, then uses linear approximation")
    void erfc_usesLinearApprox_whenXIsVerySmallPositive() {
        double result = Sfun.erfc(1e-10);
        assertEquals(0.999999999887162, result, DELTA);
    }

    @Test
    @DisplayName("Given very small negative x, when computing erfc, then uses linear approximation")
    void erfc_usesLinearApprox_whenXIsVerySmallNegative() {
        double result = Sfun.erfc(-1e-10);
        assertEquals(1.000000000112838, result, DELTA);
    }

    @Test
    @DisplayName("Given negative x between 0 and -1, when computing erfc, then hits y<1 branch with negative")
    void erfc_hitsYLessThanOneBranch_whenXIsSmallNegative() {
        double result = Sfun.erfc(-0.5);
        assertTrue(result > 1.0 && result < 2.0);
        assertEquals(1.0, Sfun.erf(-0.5) + Sfun.erfc(-0.5), DELTA);
    }

    @Test
    @DisplayName("Given x < 10, when computing r9lgmc, then returns NaN")
    void r9lgmc_returnsNaN_whenXLessThan10() {
        assertTrue(Double.isNaN(Sfun.r9lgmc(5.0)));
    }

    @Test
    @DisplayName("Given x = 9.99, when computing r9lgmc, then returns NaN")
    void r9lgmc_returnsNaN_whenXIsJustBelowTen() {
        assertTrue(Double.isNaN(Sfun.r9lgmc(9.99)));
    }

    @Test
    @DisplayName("Given x = 10, when computing r9lgmc, then returns valid correction term")
    void r9lgmc_returnsValidCorrection_whenXIsTen() {
        double result = Sfun.r9lgmc(10.0);
        assertTrue(Double.isFinite(result));
        assertTrue(result > 0);
    }

    @Test
    @DisplayName("Given x = 100, when computing r9lgmc, then returns small positive correction")
    void r9lgmc_returnsSmallCorrection_whenXIs100() {
        double result = Sfun.r9lgmc(100.0);
        assertTrue(result > 0 && result < 0.01);
    }

    @Test
    @DisplayName("Given x in mid-range, when computing r9lgmc, then returns small correction term")
    void r9lgmc_returnsSmallCorrection_whenXInMidRange() {
        double result = Sfun.r9lgmc(1e8);
        assertEquals(8.333333333333334e-10, result, DELTA);
    }

    @Test
    @DisplayName("Given very large x, when computing r9lgmc, then returns 0 (underflow)")
    void r9lgmc_returnsZero_whenXIsVeryLarge() {
        assertEquals(0.0, Sfun.r9lgmc(1e12), DELTA);
    }

    @Test
    @DisplayName("Given x slightly above 1.39118e+11, when computing r9lgmc, then returns 0")
    void r9lgmc_returnsZero_whenXExceedsUpperThreshold() {
        assertEquals(0.0, Sfun.r9lgmc(2e11), DELTA);
    }

    @Test
    @DisplayName("Given very small x, when computing cot, then returns large value close to 1/x")
    void cot_returnsLargeValue_whenXIsVerySmall() {
        double x = 1e-10;
        double result = Sfun.cot(x);
        assertTrue(result > 1e9, "cot(1e-10) should be very large, was " + result);
    }

    @Test
    @DisplayName("Given x in [0.25, 0.5] range, when computing cot, then uses double recursion formula")
    void cot_usesDoubleRecursion_whenYInQuarterToHalfRange() {
        double x = Math.PI / 8;
        double result = Sfun.cot(x);
        double expected = 1.0 / Math.tan(x);
        assertEquals(expected, result, Math.abs(expected) * RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given x in [0.5, 1] range, when computing cot, then uses triple recursion formula")
    void cot_usesTripleRecursion_whenYInHalfToOneRange() {
        double x = Math.PI / 3;
        double result = Sfun.cot(x);
        double expected = 1.0 / Math.tan(x);
        assertEquals(expected, result, Math.abs(expected) * RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given negative very small x, when computing cot, then returns negative large value")
    void cot_returnsNegativeLarge_whenXIsVerySmallNegative() {
        double x = -1e-10;
        double result = Sfun.cot(x);
        assertTrue(result < -1e9, "cot(-1e-10) should be very large negative, was " + result);
    }

    @Test
    @DisplayName("Given both p and q >= 10, when computing logBeta, then exercises dlnrel")
    void logBeta_exercisesDlnrel_whenBothArgsLarge() {
        assertEquals(-13.736229227036555, Sfun.logBeta(10.0, 10.0), RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given p small and q >= 10, when computing logBeta, then exercises dlnrel via mixed formula")
    void logBeta_exercisesDlnrelMixed_whenPSmallQlarge() {
        assertEquals(-2.70805020110221, Sfun.logBeta(1.0, 15.0), RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given p and q both large, when computing logBeta, then exercises r9lgmc mid-range")
    void logBeta_exercisesR9lgmcAllBranches_whenBothVeryLarge() {
        assertEquals(-76.52272335335051, Sfun.logBeta(50.0, 60.0), RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given small positive x near 0, when computing gamma, then returns large value")
    void gamma_returnsLargeValue_whenXIsSmallPositive() {
        double result = Sfun.gamma(0.001);
        assertTrue(result > 900);
    }

    @Test
    @DisplayName("Given x very close to 0 from positive side, when computing gamma, then returns infinity")
    void gamma_returnsInfinity_whenXIsExtremelySmalPositive() {
        double result = Sfun.gamma(1e-310);
        assertEquals(Double.POSITIVE_INFINITY, result);
    }

    @Test
    @DisplayName("Given negative non-integer between -1 and 0, when computing gamma, then returns negative")
    void gamma_returnsNegative_whenXIsBetweenNeg1AndZero() {
        double result = Sfun.gamma(-0.5);
        assertTrue(result < 0);
        assertEquals(-2 * Math.sqrt(Math.PI), result, RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given x = 2, when computing gamma, then returns 1 (1!)")
    void gamma_returnsOne_whenXIsTwo() {
        assertEquals(1.0, Sfun.gamma(2.0), RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given large negative non-integer, when computing gamma, then exercises large |x| negative branch")
    void gamma_exercisesLargeNegativeBranch_whenXIsLargeNegativeNonInt() {
        assertEquals(-1.4499543939077312e-65, Sfun.gamma(-50.5), 1e-75);
    }

    @Test
    @DisplayName("Given large positive x, when computing logGamma, then uses Stirling via r9lgmc")
    void logGamma_usesStirlingsFormula_whenXIsLargePositive() {
        assertEquals(359.1342053695754, Sfun.logGamma(100.0), RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given large negative non-integer, when computing logGamma, then exercises negative large branch")
    void logGamma_exercisesNegativeLargeBranch_whenXIsLargeNegativeNonInt() {
        assertEquals(-149.29649894115252, Sfun.logGamma(-50.5), RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given negative integer > -10, when computing logGamma, then returns NaN")
    void logGamma_returnsNaN_whenXIsNegativeIntegerSmall() {
        assertTrue(Double.isNaN(Sfun.logGamma(-5.0)));
    }

    @Test
    @DisplayName("Given large negative integer, when computing logGamma, then returns NaN")
    void logGamma_returnsNaN_whenXIsLargeNegativeInteger() {
        assertTrue(Double.isNaN(Sfun.logGamma(-20.0)));
    }

    @Test
    @DisplayName("Given x = 2, when computing logGamma, then returns 0")
    void logGamma_returnsZero_whenXIsTwo() {
        assertEquals(0.0, Sfun.logGamma(2.0), DELTA);
    }

    @Test
    @DisplayName("Given csevl is package-private, when called with known input, then returns deterministic result")
    void csevl_returnsDeterministic_whenCalledDirectly() {
        assertEquals(0.25, Sfun.csevl(0.0, new double[]{1.0, 0.5, 0.25}), DELTA);
    }

    @Test
    @DisplayName("Given single-element coef array, when computing csevl, then returns half the coefficient")
    void csevl_returnsHalfCoef_whenSingleElement() {
        assertEquals(0.5, Sfun.csevl(0.0, new double[]{1.0}), DELTA);
    }
}

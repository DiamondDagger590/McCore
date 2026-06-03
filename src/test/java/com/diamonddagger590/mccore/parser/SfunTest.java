package com.diamonddagger590.mccore.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SfunTest {

    private static final double DELTA = 1e-9;
    private static final double RELAXED_DELTA = 1e-6;
    @Test
    @DisplayName("Given x = 1, when computing acosh, then returns 0")
    void acosh_returnsZero_whenXIsOne() {
        assertEquals(0.0, Sfun.acosh(1.0), DELTA);
    }

    @Test
    @DisplayName("Given x = 2, when computing acosh, then returns ln(2 + sqrt(3))")
    void acosh_returnsCorrectValue_whenXIsTwo() {
        assertEquals(Math.log(2.0 + Math.sqrt(3.0)), Sfun.acosh(2.0), DELTA);
    }

    @Test
    @DisplayName("Given x < 1, when computing acosh, then returns NaN")
    void acosh_returnsNaN_whenXLessThanOne() {
        assertTrue(Double.isNaN(Sfun.acosh(0.5)));
    }

    @Test
    @DisplayName("Given NaN input, when computing acosh, then returns NaN")
    void acosh_returnsNaN_whenInputIsNaN() {
        assertTrue(Double.isNaN(Sfun.acosh(Double.NaN)));
    }

    @Test
    @DisplayName("Given very large x, when computing acosh, then uses large-value formula")
    void acosh_returnsCorrectValue_whenXIsVeryLarge() {
        double x = 1e9;
        double expected = 0.69314718055994530941723212145818 + Math.log(x);
        assertEquals(expected, Sfun.acosh(x), DELTA);
    }
    @Test
    @DisplayName("Given x = 0, when computing asinh, then returns 0")
    void asinh_returnsZero_whenXIsZero() {
        assertEquals(0.0, Sfun.asinh(0.0), DELTA);
    }

    @Test
    @DisplayName("Given x = 1, when computing asinh, then returns ln(1 + sqrt(2))")
    void asinh_returnsCorrectValue_whenXIsOne() {
        assertEquals(Math.log(1.0 + Math.sqrt(2.0)), Sfun.asinh(1.0), RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given negative x, when computing asinh, then returns negative result")
    void asinh_returnsNegative_whenXIsNegative() {
        assertTrue(Sfun.asinh(-2.0) < 0);
        assertEquals(-Sfun.asinh(2.0), Sfun.asinh(-2.0), DELTA);
    }

    @Test
    @DisplayName("Given NaN, when computing asinh, then returns NaN")
    void asinh_returnsNaN_whenInputIsNaN() {
        assertTrue(Double.isNaN(Sfun.asinh(Double.NaN)));
    }

    @Test
    @DisplayName("Given very small x, when computing asinh, then returns x itself")
    void asinh_returnsX_whenXIsVerySmall() {
        double x = 1e-10;
        assertEquals(x, Sfun.asinh(x), DELTA);
    }

    @Test
    @DisplayName("Given very large x, when computing asinh, then uses large-value formula")
    void asinh_returnsCorrectValue_whenXIsVeryLarge() {
        double x = 1e9;
        double expected = 0.69314718055994530941723212145818 + Math.log(x);
        assertEquals(expected, Sfun.asinh(x), DELTA);
    }
    @Test
    @DisplayName("Given x = 0, when computing atanh, then returns 0")
    void atanh_returnsZero_whenXIsZero() {
        assertEquals(0.0, Sfun.atanh(0.0), DELTA);
    }

    @Test
    @DisplayName("Given x = 0.5, when computing atanh, then returns correct value")
    void atanh_returnsCorrectValue_whenXIsHalf() {
        double expected = 0.5 * Math.log(3.0);
        assertEquals(expected, Sfun.atanh(0.5), RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given x = 1, when computing atanh, then returns positive infinity")
    void atanh_returnsInfinity_whenXIsOne() {
        assertEquals(Double.POSITIVE_INFINITY, Sfun.atanh(1.0));
    }

    @Test
    @DisplayName("Given x = -1, when computing atanh, then returns negative infinity")
    void atanh_returnsNegativeInfinity_whenXIsNegativeOne() {
        assertEquals(Double.NEGATIVE_INFINITY, Sfun.atanh(-1.0));
    }

    @Test
    @DisplayName("Given |x| > 1, when computing atanh, then returns NaN")
    void atanh_returnsNaN_whenAbsXGreaterThanOne() {
        assertTrue(Double.isNaN(Sfun.atanh(1.5)));
        assertTrue(Double.isNaN(Sfun.atanh(-1.5)));
    }

    @Test
    @DisplayName("Given NaN, when computing atanh, then returns NaN")
    void atanh_returnsNaN_whenInputIsNaN() {
        assertTrue(Double.isNaN(Sfun.atanh(Double.NaN)));
    }

    @Test
    @DisplayName("Given very small x, when computing atanh, then returns x")
    void atanh_returnsX_whenXIsVerySmall() {
        double x = 1e-10;
        assertEquals(x, Sfun.atanh(x), DELTA);
    }

    @Test
    @DisplayName("Given x = 0.8, when computing atanh, then uses log formula branch")
    void atanh_returnsCorrectValue_whenXBetweenHalfAndOne() {
        double x = 0.8;
        double expected = 0.5 * Math.log((1.0 + x) / (1.0 - x));
        assertEquals(expected, Sfun.atanh(x), DELTA);
    }
    @Test
    @DisplayName("Given x = 0, when computing cosh, then returns 1")
    void cosh_returnsOne_whenXIsZero() {
        assertEquals(1.0, Sfun.cosh(0.0), DELTA);
    }

    @Test
    @DisplayName("Given x = 1, when computing cosh, then returns (e + 1/e) / 2")
    void cosh_returnsCorrectValue_whenXIsOne() {
        double expected = (Math.E + 1.0 / Math.E) / 2.0;
        assertEquals(expected, Sfun.cosh(1.0), DELTA);
    }

    @Test
    @DisplayName("Given cosh is even, when computing for x and -x, then results are equal")
    void cosh_isEvenFunction_whenComparingPositiveAndNegative() {
        assertEquals(Sfun.cosh(3.0), Sfun.cosh(-3.0), DELTA);
    }

    @Test
    @DisplayName("Given NaN, when computing cosh, then returns NaN")
    void cosh_returnsNaN_whenInputIsNaN() {
        assertTrue(Double.isNaN(Sfun.cosh(Double.NaN)));
    }
    @Test
    @DisplayName("Given x = 0, when computing sinh, then returns 0")
    void sinh_returnsZero_whenXIsZero() {
        assertEquals(0.0, Sfun.sinh(0.0), DELTA);
    }

    @Test
    @DisplayName("Given x = 1, when computing sinh, then returns (e - 1/e) / 2")
    void sinh_returnsCorrectValue_whenXIsOne() {
        double expected = (Math.E - 1.0 / Math.E) / 2.0;
        assertEquals(expected, Sfun.sinh(1.0), DELTA);
    }

    @Test
    @DisplayName("Given sinh is odd, when computing for x and -x, then results are negated")
    void sinh_isOddFunction_whenComparingPositiveAndNegative() {
        assertEquals(-Sfun.sinh(3.0), Sfun.sinh(-3.0), DELTA);
    }

    @Test
    @DisplayName("Given NaN, when computing sinh, then returns NaN")
    void sinh_returnsNaN_whenInputIsNaN() {
        assertTrue(Double.isNaN(Sfun.sinh(Double.NaN)));
    }

    @Test
    @DisplayName("Given very small x, when computing sinh, then returns x")
    void sinh_returnsX_whenXIsVerySmall() {
        double x = 1e-10;
        assertEquals(x, Sfun.sinh(x), DELTA);
    }

    @Test
    @DisplayName("Given positive infinity, when computing sinh, then returns positive infinity")
    void sinh_returnsPositiveInfinity_whenInputIsPositiveInfinity() {
        assertEquals(Double.POSITIVE_INFINITY, Sfun.sinh(Double.POSITIVE_INFINITY));
    }

    @Test
    @DisplayName("Given negative infinity, when computing sinh, then returns negative infinity")
    void sinh_returnsNegativeInfinity_whenInputIsNegativeInfinity() {
        assertEquals(Double.NEGATIVE_INFINITY, Sfun.sinh(Double.NEGATIVE_INFINITY));
    }

    @Test
    @DisplayName("Given large x, when computing sinh, then uses large-value formula")
    void sinh_returnsCorrectValue_whenXIsLarge() {
        double x = 100.0;
        double y = Math.exp(x);
        double expected = 0.5 * (y - 1.0 / y);
        assertEquals(expected, Sfun.sinh(x), expected * 1e-10);
    }
    @Test
    @DisplayName("Given x = 0, when computing tanh, then returns 0")
    void tanh_returnsZero_whenXIsZero() {
        assertEquals(0.0, Sfun.tanh(0.0), DELTA);
    }

    @Test
    @DisplayName("Given large positive x, when computing tanh, then approaches 1")
    void tanh_approachesOne_whenXIsLargePositive() {
        assertEquals(1.0, Sfun.tanh(100.0), DELTA);
    }

    @Test
    @DisplayName("Given large negative x, when computing tanh, then approaches -1")
    void tanh_approachesNegativeOne_whenXIsLargeNegative() {
        assertEquals(-1.0, Sfun.tanh(-100.0), DELTA);
    }

    @Test
    @DisplayName("Given NaN, when computing tanh, then returns NaN")
    void tanh_returnsNaN_whenInputIsNaN() {
        assertTrue(Double.isNaN(Sfun.tanh(Double.NaN)));
    }

    @Test
    @DisplayName("Given very small x, when computing tanh, then returns x")
    void tanh_returnsX_whenXIsVerySmall() {
        double x = 1e-10;
        assertEquals(x, Sfun.tanh(x), DELTA);
    }

    @Test
    @DisplayName("Given x in medium range, when computing tanh, then uses exp formula")
    void tanh_returnsCorrectValue_whenXIsInMediumRange() {
        double x = 3.0;
        double y = Math.exp(x);
        double expected = (y - 1.0 / y) / (y + 1.0 / y);
        assertEquals(expected, Sfun.tanh(x), DELTA);
    }
    @Test
    @DisplayName("Given x = pi/4, when computing cot, then returns approximately 1")
    void cot_returnsOne_whenXIsPiOverFour() {
        assertEquals(1.0, Sfun.cot(Math.PI / 4), RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given x = pi/2, when computing cot, then returns approximately 0")
    void cot_returnsZero_whenXIsPiOverTwo() {
        assertEquals(0.0, Sfun.cot(Math.PI / 2), RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given very large x, when computing cot, then returns NaN")
    void cot_returnsNaN_whenXIsVeryLarge() {
        assertTrue(Double.isNaN(Sfun.cot(1e16)));
    }

    @Test
    @DisplayName("Given negative x, when computing cot, then returns negative cot for cot(-pi/4)")
    void cot_returnsNegative_whenXIsNegativePiOverFour() {
        assertEquals(-1.0, Sfun.cot(-Math.PI / 4), RELAXED_DELTA);
    }
    @Test
    @DisplayName("Given x = 0, when computing erf, then returns 0")
    void erf_returnsZero_whenXIsZero() {
        assertEquals(0.0, Sfun.erf(0.0), DELTA);
    }

    @Test
    @DisplayName("Given large positive x, when computing erf, then approaches 1")
    void erf_approachesOne_whenXIsLargePositive() {
        assertEquals(1.0, Sfun.erf(10.0), DELTA);
    }

    @Test
    @DisplayName("Given large negative x, when computing erf, then approaches -1")
    void erf_approachesNegativeOne_whenXIsLargeNegative() {
        assertEquals(-1.0, Sfun.erf(-10.0), DELTA);
    }

    @Test
    @DisplayName("Given erf is odd, when computing for x and -x, then results are negated")
    void erf_isOddFunction_whenComparingPositiveAndNegative() {
        assertEquals(-Sfun.erf(0.5), Sfun.erf(-0.5), DELTA);
    }

    @Test
    @DisplayName("Given very small x, when computing erf, then returns 2x/sqrt(pi)")
    void erf_returnsLinearApprox_whenXIsVerySmall() {
        double x = 1e-10;
        double expected = 2.0 * x / 1.77245385090551602729816748334;
        assertEquals(expected, Sfun.erf(x), DELTA);
    }

    @Test
    @DisplayName("Given x in medium range, when computing erf, then uses erfc complement")
    void erf_returnsCorrectValue_whenXIsInMediumRange() {
        double result = Sfun.erf(2.0);
        assertTrue(result > 0.99 && result < 1.0);
    }
    @Test
    @DisplayName("Given x = 0, when computing erfc, then returns 1")
    void erfc_returnsOne_whenXIsZero() {
        assertEquals(1.0, Sfun.erfc(0.0), DELTA);
    }

    @Test
    @DisplayName("Given large positive x, when computing erfc, then approaches 0")
    void erfc_approachesZero_whenXIsLargePositive() {
        assertEquals(0.0, Sfun.erfc(10.0), DELTA);
    }

    @Test
    @DisplayName("Given very negative x, when computing erfc, then returns 2")
    void erfc_returnsTwo_whenXIsVeryNegative() {
        assertEquals(2.0, Sfun.erfc(-10.0), DELTA);
    }

    @Test
    @DisplayName("Given erf + erfc = 1, when computing both, then sum is 1")
    void erfc_complementsErf_whenSummed() {
        double x = 1.5;
        assertEquals(1.0, Sfun.erf(x) + Sfun.erfc(x), DELTA);
    }

    @Test
    @DisplayName("Given x between 1 and 4, when computing erfc, then uses erfc2 series")
    void erfc_returnsCorrectValue_whenXBetweenOneAndFour() {
        double result = Sfun.erfc(2.0);
        assertTrue(result > 0.0 && result < 0.01);
    }

    @Test
    @DisplayName("Given x > 4, when computing erfc, then uses erfcc series")
    void erfc_returnsCorrectValue_whenXGreaterThanFour() {
        double result = Sfun.erfc(5.0);
        assertTrue(result >= 0.0 && result < 1e-10);
    }
    @Test
    @DisplayName("Given n = 0, when computing factorial, then returns 1")
    void fact_returnsOne_whenNIsZero() {
        assertEquals(1.0, Sfun.fact(0), DELTA);
    }

    @Test
    @DisplayName("Given n = 1, when computing factorial, then returns 1")
    void fact_returnsOne_whenNIsOne() {
        assertEquals(1.0, Sfun.fact(1), DELTA);
    }

    @Test
    @DisplayName("Given n = 5, when computing factorial, then returns 120")
    void fact_returns120_whenNIs5() {
        assertEquals(120.0, Sfun.fact(5), DELTA);
    }

    @Test
    @DisplayName("Given n = 10, when computing factorial, then returns 3628800")
    void fact_returns3628800_whenNIs10() {
        assertEquals(3628800.0, Sfun.fact(10), DELTA);
    }

    @Test
    @DisplayName("Given negative n, when computing factorial, then returns NaN")
    void fact_returnsNaN_whenNIsNegative() {
        assertTrue(Double.isNaN(Sfun.fact(-1)));
    }

    @Test
    @DisplayName("Given n > 170, when computing factorial, then returns positive infinity")
    void fact_returnsInfinity_whenNExceeds170() {
        assertEquals(Double.POSITIVE_INFINITY, Sfun.fact(171));
    }

    @Test
    @DisplayName("Given n = 170, when computing factorial, then returns finite value")
    void fact_returnsFiniteValue_whenNIs170() {
        assertTrue(Double.isFinite(Sfun.fact(170)));
    }
    @Test
    @DisplayName("Given x = 1, when computing gamma, then returns 1 (0! = 1)")
    void gamma_returnsOne_whenXIsOne() {
        assertEquals(1.0, Sfun.gamma(1.0), RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given x = 5, when computing gamma, then returns 24 (4!)")
    void gamma_returns24_whenXIs5() {
        assertEquals(24.0, Sfun.gamma(5.0), RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given x = 0.5, when computing gamma, then returns sqrt(pi)")
    void gamma_returnsSqrtPi_whenXIsHalf() {
        assertEquals(Math.sqrt(Math.PI), Sfun.gamma(0.5), RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given x = 0, when computing gamma, then returns NaN (pole)")
    void gamma_returnsNaN_whenXIsZero() {
        assertTrue(Double.isNaN(Sfun.gamma(0.0)));
    }

    @Test
    @DisplayName("Given negative integer, when computing gamma, then returns NaN")
    void gamma_returnsNaN_whenXIsNegativeInteger() {
        assertTrue(Double.isNaN(Sfun.gamma(-1.0)));
        assertTrue(Double.isNaN(Sfun.gamma(-2.0)));
    }

    @Test
    @DisplayName("Given x > 171.614, when computing gamma, then returns positive infinity")
    void gamma_returnsInfinity_whenXIsVeryLarge() {
        assertEquals(Double.POSITIVE_INFINITY, Sfun.gamma(172.0));
    }

    @Test
    @DisplayName("Given x < -170.56, when computing gamma, then returns 0 (underflow)")
    void gamma_returnsZero_whenXIsVeryNegative() {
        assertEquals(0.0, Sfun.gamma(-171.0), DELTA);
    }

    @Test
    @DisplayName("Given x = 10, when computing gamma, then returns 9!")
    void gamma_returns9Factorial_whenXIs10() {
        assertEquals(362880.0, Sfun.gamma(10.0), RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given negative non-integer with |x| > 10, when computing gamma, then returns finite value")
    void gamma_returnsFiniteValue_whenXIsNegativeNonIntegerLarge() {
        double result = Sfun.gamma(-10.5);
        assertTrue(Double.isFinite(result));
    }
    @Test
    @DisplayName("Given x = 1, when computing log10, then returns 0")
    void log10_returnsZero_whenXIsOne() {
        assertEquals(0.0, Sfun.log10(1.0), DELTA);
    }

    @Test
    @DisplayName("Given x = 10, when computing log10, then returns 1")
    void log10_returnsOne_whenXIsTen() {
        assertEquals(1.0, Sfun.log10(10.0), DELTA);
    }

    @Test
    @DisplayName("Given x = 100, when computing log10, then returns 2")
    void log10_returnsTwo_whenXIs100() {
        assertEquals(2.0, Sfun.log10(100.0), DELTA);
    }

    @Test
    @DisplayName("Given x = 0.001, when computing log10, then returns -3")
    void log10_returnsNegativeThree_whenXIsOneThousandth() {
        assertEquals(-3.0, Sfun.log10(0.001), DELTA);
    }
    @Test
    @DisplayName("Given x = 1, when computing logGamma, then returns 0")
    void logGamma_returnsZero_whenXIsOne() {
        assertEquals(0.0, Sfun.logGamma(1.0), DELTA);
    }

    @Test
    @DisplayName("Given x = 5, when computing logGamma, then returns ln(24)")
    void logGamma_returnsLn24_whenXIs5() {
        assertEquals(Math.log(24.0), Sfun.logGamma(5.0), RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given negative integer, when computing logGamma, then returns NaN")
    void logGamma_returnsNaN_whenXIsNegativeInteger() {
        assertTrue(Double.isNaN(Sfun.logGamma(-2.0)));
    }

    @Test
    @DisplayName("Given x > 10, when computing logGamma, then uses Stirling approximation")
    void logGamma_returnsCorrectValue_whenXIsLarge() {
        double result = Sfun.logGamma(15.0);
        double expected = Math.log(Sfun.gamma(15.0));
        assertEquals(expected, result, RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given negative non-integer x with |x| > 10, when computing logGamma, then returns finite value")
    void logGamma_returnsFiniteValue_whenXIsLargeNegativeNonInteger() {
        double result = Sfun.logGamma(-10.5);
        assertTrue(Double.isFinite(result));
    }
    @Test
    @DisplayName("Given a = 1 and b = 1, when computing logBeta, then returns 0")
    void logBeta_returnsZero_whenBothAreOne() {
        assertEquals(0.0, Sfun.logBeta(1.0, 1.0), RELAXED_DELTA);
    }

    @Test
    @DisplayName("Given a <= 0, when computing logBeta, then returns NaN")
    void logBeta_returnsNaN_whenAIsNonPositive() {
        assertTrue(Double.isNaN(Sfun.logBeta(0.0, 1.0)));
        assertTrue(Double.isNaN(Sfun.logBeta(-1.0, 1.0)));
    }

    @Test
    @DisplayName("Given b <= 0, when computing logBeta, then returns NaN")
    void logBeta_returnsNaN_whenBIsNonPositive() {
        assertTrue(Double.isNaN(Sfun.logBeta(1.0, 0.0)));
    }

    @Test
    @DisplayName("Given large a and b, when computing logBeta, then uses large-value formula")
    void logBeta_returnsCorrectValue_whenBothAreLarge() {
        double result = Sfun.logBeta(15.0, 20.0);
        assertTrue(Double.isFinite(result));
        assertTrue(result < 0);
    }

    @Test
    @DisplayName("Given small a and large b, when computing logBeta, then uses mixed formula")
    void logBeta_returnsCorrectValue_whenAIsSmallAndBIsLarge() {
        double result = Sfun.logBeta(2.0, 15.0);
        assertTrue(Double.isFinite(result));
    }

    @Test
    @DisplayName("Given Beta symmetry, when computing logBeta(a,b) and logBeta(b,a), then results are equal")
    void logBeta_isSymmetric_whenArgumentsAreSwapped() {
        assertEquals(Sfun.logBeta(3.0, 5.0), Sfun.logBeta(5.0, 3.0), DELTA);
    }
}

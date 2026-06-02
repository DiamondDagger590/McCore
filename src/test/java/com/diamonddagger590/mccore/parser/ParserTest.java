package com.diamonddagger590.mccore.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParserTest {

    private static final double DELTA = 1e-9;

    @Test
    @DisplayName("Given addition expression, when evaluating, then returns correct sum")
    void getValue_returnsSum_whenExpressionIsAddition() {
        assertEquals(5.0, new Parser("2+3").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given subtraction expression, when evaluating, then returns correct difference")
    void getValue_returnsDifference_whenExpressionIsSubtraction() {
        assertEquals(6.0, new Parser("10-4").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given multiplication expression, when evaluating, then returns correct product")
    void getValue_returnsProduct_whenExpressionIsMultiplication() {
        assertEquals(21.0, new Parser("3*7").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given division expression, when evaluating, then returns correct quotient")
    void getValue_returnsQuotient_whenExpressionIsDivision() {
        assertEquals(5.0, new Parser("20/4").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given modulo expression, when evaluating, then returns correct remainder")
    void getValue_returnsRemainder_whenExpressionIsModulo() {
        assertEquals(1.0, new Parser("10%3").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given exponentiation expression, when evaluating, then returns correct power")
    void getValue_returnsPower_whenExpressionIsExponentiation() {
        assertEquals(1024.0, new Parser("2^10").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given mixed addition and multiplication, when evaluating, then multiplication takes precedence")
    void getValue_appliesMultiplicationFirst_whenMixedWithAddition() {
        assertEquals(14.0, new Parser("2+3*4").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given mixed subtraction and division, when evaluating, then division takes precedence")
    void getValue_appliesDivisionFirst_whenMixedWithSubtraction() {
        assertEquals(7.0, new Parser("10-6/2").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given parenthesized sub-expression, when evaluating, then parentheses override precedence")
    void getValue_overridesPrecedence_whenParenthesesPresent() {
        assertEquals(20.0, new Parser("(2+3)*4").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given nested parentheses, when evaluating, then inner parentheses evaluate first")
    void getValue_evaluatesInnerFirst_whenParenthesesAreNested() {
        assertEquals(15.0, new Parser("((2+3)*(4-1))").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given unary negation, when evaluating, then returns negative value")
    void getValue_returnsNegative_whenUnaryNegationApplied() {
        assertEquals(-5.0, new Parser("-5").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given unary negation in multiplication, when evaluating, then negation applies correctly")
    void getValue_appliesNegation_whenUsedInMultiplication() {
        assertEquals(-6.0, new Parser("3*-2").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given right-associative exponentiation, when evaluating, then evaluates right-to-left")
    void getValue_evaluatesRightToLeft_whenExponentiationChained() {
        assertEquals(512.0, new Parser("2^3^2").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given decimal numbers, when evaluating, then handles fractional values correctly")
    void getValue_handlesFractions_whenDecimalNumbersUsed() {
        assertEquals(4.0, new Parser("1.5+2.5").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given scientific notation with positive exponent, when evaluating, then parses correctly")
    void getValue_parsesScientificNotation_whenPositiveExponent() {
        assertEquals(1500.0, new Parser("1e3+500").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given scientific notation with negative exponent, when evaluating, then parses correctly")
    void getValue_parsesScientificNotation_whenNegativeExponent() {
        assertEquals(0.05, new Parser("5e-2").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given built-in constant pi, when evaluating, then returns Math.PI")
    void getValue_returnsMathPI_whenExpressionIsPi() {
        assertEquals(Math.PI, new Parser("pi").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given built-in constant e, when evaluating, then returns Math.E")
    void getValue_returnsMathE_whenExpressionIsE() {
        assertEquals(Math.E, new Parser("e").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given pi in a multiplication, when evaluating, then applies arithmetic to constant")
    void getValue_multipliesConstant_whenPiUsedInExpression() {
        assertEquals(2 * Math.PI, new Parser("2*pi").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given sin(0), when evaluating, then returns zero")
    void getValue_returnsZero_whenSinOfZero() {
        assertEquals(0.0, new Parser("sin(0)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given cos(0), when evaluating, then returns one")
    void getValue_returnsOne_whenCosOfZero() {
        assertEquals(1.0, new Parser("cos(0)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given abs(-5), when evaluating, then returns positive value")
    void getValue_returnsPositive_whenAbsOfNegative() {
        assertEquals(5.0, new Parser("abs(-5)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given sqrt(16), when evaluating, then returns correct square root")
    void getValue_returnsSquareRoot_whenSqrtCalled() {
        assertEquals(4.0, new Parser("sqrt(16)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given exp(0), when evaluating, then returns one")
    void getValue_returnsOne_whenExpOfZero() {
        assertEquals(1.0, new Parser("exp(0)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given ln(1), when evaluating, then returns zero")
    void getValue_returnsZero_whenLnOfOne() {
        assertEquals(0.0, new Parser("ln(1)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given log(100), when evaluating, then returns two")
    void getValue_returnsTwo_whenLogOfHundred() {
        assertEquals(2.0, new Parser("log(100)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given sin(pi), when evaluating, then returns approximately zero")
    void getValue_returnsApproximatelyZero_whenSinOfPi() {
        assertEquals(0.0, new Parser("sin(pi)").getValue(), 1e-6);
    }

    @Test
    @DisplayName("Given cos(pi), when evaluating, then returns negative one")
    void getValue_returnsNegativeOne_whenCosOfPi() {
        assertEquals(-1.0, new Parser("cos(pi)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given tan(0), when evaluating, then returns zero")
    void getValue_returnsZero_whenTanOfZero() {
        assertEquals(0.0, new Parser("tan(0)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given asin(1), when evaluating, then returns pi/2")
    void getValue_returnsPiOverTwo_whenAsinOfOne() {
        assertEquals(Math.PI / 2, new Parser("asin(1)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given acos(1), when evaluating, then returns zero")
    void getValue_returnsZero_whenAcosOfOne() {
        assertEquals(0.0, new Parser("acos(1)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given atan(0), when evaluating, then returns zero")
    void getValue_returnsZero_whenAtanOfZero() {
        assertEquals(0.0, new Parser("atan(0)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given sinh(0), when evaluating, then returns zero")
    void getValue_returnsZero_whenSinhOfZero() {
        assertEquals(0.0, new Parser("sinh(0)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given cosh(0), when evaluating, then returns one")
    void getValue_returnsOne_whenCoshOfZero() {
        assertEquals(1.0, new Parser("cosh(0)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given tanh(0), when evaluating, then returns zero")
    void getValue_returnsZero_whenTanhOfZero() {
        assertEquals(0.0, new Parser("tanh(0)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given nested functions, when evaluating, then inner function evaluates first")
    void getValue_evaluatesInnerFunctionFirst_whenFunctionsAreNested() {
        assertEquals(0.0, new Parser("abs(sin(0))").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given a variable set to 5, when evaluating x+1, then returns 6")
    void getValue_returnsSum_whenVariableIsSet() {
        Parser parser = new Parser("x+1");
        parser.setVariable("x", 5.0);
        assertEquals(6.0, parser.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given multiple variables, when evaluating compound expression, then substitutes all")
    void getValue_substitutesAllVariables_whenMultipleVariablesSet() {
        Parser parser = new Parser("x*y+z");
        parser.setVariable("x", 2.0);
        parser.setVariable("y", 3.0);
        parser.setVariable("z", 4.0);
        assertEquals(10.0, parser.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given uninitialized variable with error=false, when evaluating, then defaults to zero")
    void getValue_defaultsToZero_whenVariableUninitializedAndErrorDisabled() {
        assertEquals(1.0, new Parser("x+1", false).getValue(), DELTA);
    }

    @Test
    @DisplayName("Given uninitialized variable with error=true, when evaluating, then throws EvaluationException")
    void getValue_throwsEvaluationException_whenVariableUninitializedAndErrorEnabled() {
        Parser parser = new Parser("x+1", true);
        assertThrows(EvaluationException.class, parser::getValue);
    }

    @Test
    @DisplayName("Given a variable reassigned to a new value, when evaluating, then uses the new value")
    void getValue_usesNewValue_whenVariableReassigned() {
        Parser parser = new Parser("x*2");
        parser.setVariable("x", 3.0);
        assertEquals(6.0, parser.getValue(), DELTA);
        parser.setVariable("x", 10.0);
        assertEquals(20.0, parser.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given a constant followed by a variable, when parsing, then detects implicit multiplication")
    void getValue_appliesImplicitMultiplication_whenConstantPrecedesVariable() {
        Parser parser = new Parser("2x");
        parser.setVariable("x", 5.0);
        assertEquals(10.0, parser.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given a constant followed by a function, when parsing, then detects implicit multiplication")
    void getValue_appliesImplicitMultiplication_whenConstantPrecedesFunction() {
        assertEquals(4.0, new Parser("2sqrt(4)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given expression with three variables, when getting parsed variables, then returns all three")
    void getParsedVariables_returnsAllVariables_whenExpressionContainsThree() {
        HashSet<String> vars = new Parser("x+y*z").getParsedVariables();
        assertEquals(3, vars.size());
        assertTrue(vars.contains("x"));
        assertTrue(vars.contains("y"));
        assertTrue(vars.contains("z"));
    }

    @Test
    @DisplayName("Given expression with sin and cos, when getting parsed functions, then returns both")
    void getParsedFunctions_returnsBoth_whenExpressionUsesSinAndCos() {
        HashSet<String> funcs = new Parser("sin(x)+cos(y)").getParsedFunctions();
        assertEquals(2, funcs.size());
        assertTrue(funcs.contains("sin"));
        assertTrue(funcs.contains("cos"));
    }

    @Test
    @DisplayName("Given a valid expression, when getting expression string, then returns non-empty string")
    void getExpression_returnsNonEmpty_whenExpressionIsValid() {
        assertFalse(new Parser("2+3").getExpression().isEmpty());
    }

    @Test
    @DisplayName("Given input with spaces, when getting input string, then spaces are stripped")
    void getInputString_stripsSpaces_whenInputContainsWhitespace() {
        assertEquals("2+3", new Parser("2 + 3").getInputString());
    }

    @Test
    @DisplayName("Given a complex mixed expression, when evaluating, then returns correct result")
    void getValue_returnsCorrectResult_whenExpressionIsMixedAndComplex() {
        assertEquals(27.0, new Parser("(3+2)^2 - sqrt(16) * 2 + abs(-10)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given division by zero, when evaluating, then returns positive infinity")
    void getValue_returnsInfinity_whenDividingByZero() {
        assertEquals(Double.POSITIVE_INFINITY, new Parser("1/0").getValue());
    }

    @Test
    @DisplayName("Given modulo expression 17%5, when evaluating, then returns 2")
    void getValue_returnsRemainder_whenModuloApplied() {
        assertEquals(2.0, new Parser("17%5").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given already-evaluated parser, when evaluating again, then returns same result")
    void getValue_returnsSameResult_whenCalledMultipleTimes() {
        Parser parser = new Parser("2+3");
        assertEquals(5.0, parser.getValue(), DELTA);
        assertEquals(5.0, parser.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given already-parsed tree, when getting tree again, then returns same instance")
    void getTree_returnsSameInstance_whenCalledMultipleTimes() {
        Parser parser = new Parser("2+3");
        ExpressionNode tree1 = parser.getTree();
        ExpressionNode tree2 = parser.getTree();
        assertEquals(tree1, tree2);
    }

    @Test
    @DisplayName("Given unmatched open parenthesis, when evaluating, then throws ParseError")
    void getValue_throwsParseError_whenOpenParenthesisUnmatched() {
        assertThrows(ParseError.class, () -> new Parser("(2+3").getValue());
    }

    @Test
    @DisplayName("Given function without parenthesis, when evaluating, then throws ParseError")
    void getValue_throwsParseError_whenFunctionMissingParenthesis() {
        assertThrows(ParseError.class, () -> new Parser("sin").getValue());
    }

    @Test
    @DisplayName("Given empty expression, when evaluating, then throws ParseError")
    void getValue_throwsParseError_whenExpressionIsEmpty() {
        assertThrows(ParseError.class, () -> new Parser("").getValue());
    }

    @Test
    @DisplayName("Given log2(8), when evaluating, then returns 3")
    void getValue_returnsThree_whenLog2Of8() {
        assertEquals(3.0, new Parser("log2(8)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given exp(1), when evaluating, then returns Math.E")
    void getValue_returnsMathE_whenExpOfOne() {
        assertEquals(Math.E, new Parser("exp(1)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given long variable names with underscores, when evaluating, then resolves correctly")
    void getValue_resolvesCorrectly_whenVariableNamesAreLongWithUnderscores() {
        Parser parser = new Parser("level * base_damage + armor_bonus");
        parser.setVariable("level", 10.0);
        parser.setVariable("base_damage", 5.0);
        parser.setVariable("armor_bonus", 3.0);
        assertEquals(53.0, parser.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given chained additions, when evaluating, then sums all terms")
    void getValue_sumsAllTerms_whenAdditionsAreChained() {
        assertEquals(15.0, new Parser("1+2+3+4+5").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given chained multiplications, when evaluating, then multiplies all factors")
    void getValue_multipliesAllFactors_whenMultiplicationsAreChained() {
        assertEquals(24.0, new Parser("2*3*4").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given mixed operators, when evaluating, then applies correct precedence throughout")
    void getValue_appliesCorrectPrecedence_whenOperatorsAreMixed() {
        assertEquals(12.0, new Parser("2+3*4-6/2+1").getValue(), DELTA);
    }
}

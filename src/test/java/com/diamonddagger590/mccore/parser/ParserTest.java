package com.diamonddagger590.mccore.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParserTest {

    private static final double DELTA = 1e-9;

    @ParameterizedTest
    @CsvSource({
        "2+3, 5.0",
        "10-4, 6.0",
        "3*7, 21.0",
        "20/4, 5.0",
        "10%3, 1.0",
        "2^10, 1024.0"
    })
    void basicArithmeticOperators(String expression, double expected) {
        Parser parser = new Parser(expression);
        assertEquals(expected, parser.getValue(), DELTA);
    }

    @Test
    void operatorPrecedenceMultiplicationBeforeAddition() {
        Parser parser = new Parser("2+3*4");
        assertEquals(14.0, parser.getValue(), DELTA);
    }

    @Test
    void operatorPrecedenceDivisionBeforeSubtraction() {
        Parser parser = new Parser("10-6/2");
        assertEquals(7.0, parser.getValue(), DELTA);
    }

    @Test
    void parenthesesOverridePrecedence() {
        Parser parser = new Parser("(2+3)*4");
        assertEquals(20.0, parser.getValue(), DELTA);
    }

    @Test
    void nestedParentheses() {
        Parser parser = new Parser("((2+3)*(4-1))");
        assertEquals(15.0, parser.getValue(), DELTA);
    }

    @Test
    void unaryNegation() {
        Parser parser = new Parser("-5");
        assertEquals(-5.0, parser.getValue(), DELTA);
    }

    @Test
    void unaryNegationInExpression() {
        Parser parser = new Parser("3*-2");
        assertEquals(-6.0, parser.getValue(), DELTA);
    }

    @Test
    void exponentiation() {
        Parser parser = new Parser("2^3^2");
        assertEquals(512.0, parser.getValue(), DELTA);
    }

    @Test
    void decimalNumbers() {
        Parser parser = new Parser("1.5+2.5");
        assertEquals(4.0, parser.getValue(), DELTA);
    }

    @Test
    void scientificNotation() {
        Parser parser = new Parser("1e3+500");
        assertEquals(1500.0, parser.getValue(), DELTA);
    }

    @Test
    void scientificNotationNegativeExponent() {
        Parser parser = new Parser("5e-2");
        assertEquals(0.05, parser.getValue(), DELTA);
    }

    @Test
    void builtInConstantPi() {
        Parser parser = new Parser("pi");
        assertEquals(Math.PI, parser.getValue(), DELTA);
    }

    @Test
    void builtInConstantE() {
        Parser parser = new Parser("e");
        assertEquals(Math.E, parser.getValue(), DELTA);
    }

    @Test
    void piInExpression() {
        Parser parser = new Parser("2*pi");
        assertEquals(2 * Math.PI, parser.getValue(), DELTA);
    }

    @ParameterizedTest
    @CsvSource({
        "sin(0), 0.0",
        "cos(0), 1.0",
        "abs(-5), 5.0",
        "sqrt(16), 4.0",
        "exp(0), 1.0",
        "ln(1), 0.0",
        "log(100), 2.0",
    })
    void builtInFunctions(String expression, double expected) {
        Parser parser = new Parser(expression);
        assertEquals(expected, parser.getValue(), DELTA);
    }

    @Test
    void sinOfPiIsZero() {
        Parser parser = new Parser("sin(pi)");
        assertEquals(0.0, parser.getValue(), 1e-6);
    }

    @Test
    void cosOfPiIsNegativeOne() {
        Parser parser = new Parser("cos(pi)");
        assertEquals(-1.0, parser.getValue(), DELTA);
    }

    @Test
    void tanOfZero() {
        Parser parser = new Parser("tan(0)");
        assertEquals(0.0, parser.getValue(), DELTA);
    }

    @Test
    void asinAndAcos() {
        Parser parser1 = new Parser("asin(1)");
        assertEquals(Math.PI / 2, parser1.getValue(), DELTA);

        Parser parser2 = new Parser("acos(1)");
        assertEquals(0.0, parser2.getValue(), DELTA);
    }

    @Test
    void atanOfZero() {
        Parser parser = new Parser("atan(0)");
        assertEquals(0.0, parser.getValue(), DELTA);
    }

    @Test
    void sinhCoshTanh() {
        Parser sinhParser = new Parser("sinh(0)");
        assertEquals(0.0, sinhParser.getValue(), DELTA);

        Parser coshParser = new Parser("cosh(0)");
        assertEquals(1.0, coshParser.getValue(), DELTA);

        Parser tanhParser = new Parser("tanh(0)");
        assertEquals(0.0, tanhParser.getValue(), DELTA);
    }

    @Test
    void nestedFunctions() {
        Parser parser = new Parser("abs(sin(0))");
        assertEquals(0.0, parser.getValue(), DELTA);
    }

    @Test
    void variableSubstitution() {
        Parser parser = new Parser("x+1");
        parser.setVariable("x", 5.0);
        assertEquals(6.0, parser.getValue(), DELTA);
    }

    @Test
    void multipleVariables() {
        Parser parser = new Parser("x*y+z");
        parser.setVariable("x", 2.0);
        parser.setVariable("y", 3.0);
        parser.setVariable("z", 4.0);
        assertEquals(10.0, parser.getValue(), DELTA);
    }

    @Test
    void variableDefaultsToZeroWhenErrorIsFalse() {
        Parser parser = new Parser("x+1", false);
        assertEquals(1.0, parser.getValue(), DELTA);
    }

    @Test
    void uninitializedVariableThrowsWhenErrorIsTrue() {
        Parser parser = new Parser("x+1", true);
        assertThrows(EvaluationException.class, parser::getValue);
    }

    @Test
    void variableReassignment() {
        Parser parser = new Parser("x*2");
        parser.setVariable("x", 3.0);
        assertEquals(6.0, parser.getValue(), DELTA);

        parser.setVariable("x", 10.0);
        assertEquals(20.0, parser.getValue(), DELTA);
    }

    @Test
    void implicitMultiplicationConstantTimesVariable() {
        Parser parser = new Parser("2x");
        parser.setVariable("x", 5.0);
        assertEquals(10.0, parser.getValue(), DELTA);
    }

    @Test
    void implicitMultiplicationConstantTimesFunction() {
        Parser parser = new Parser("2sqrt(4)");
        assertEquals(4.0, parser.getValue(), DELTA);
    }

    @Test
    void getParsedVariablesReturnsAllVariables() {
        Parser parser = new Parser("x+y*z");
        HashSet<String> vars = parser.getParsedVariables();
        assertEquals(3, vars.size());
        assertTrue(vars.contains("x"));
        assertTrue(vars.contains("y"));
        assertTrue(vars.contains("z"));
    }

    @Test
    void getParsedFunctionsReturnsUsedFunctions() {
        Parser parser = new Parser("sin(x)+cos(y)");
        HashSet<String> funcs = parser.getParsedFunctions();
        assertEquals(2, funcs.size());
        assertTrue(funcs.contains("sin"));
        assertTrue(funcs.contains("cos"));
    }

    @Test
    void getExpressionReturnsNormalizedString() {
        Parser parser = new Parser("2+3");
        String expression = parser.getExpression();
        assertFalse(expression.isEmpty());
    }

    @Test
    void getInputStringStripsInvalidCharacters() {
        Parser parser = new Parser("2 + 3");
        assertEquals("2+3", parser.getInputString());
    }

    @Test
    void complexExpression() {
        Parser parser = new Parser("(3+2)^2 - sqrt(16) * 2 + abs(-10)");
        assertEquals(27.0, parser.getValue(), DELTA);
    }

    @Test
    void divisionByZeroReturnsInfinity() {
        Parser parser = new Parser("1/0");
        assertEquals(Double.POSITIVE_INFINITY, parser.getValue());
    }

    @Test
    void moduloOperator() {
        Parser parser = new Parser("17%5");
        assertEquals(2.0, parser.getValue(), DELTA);
    }

    @Test
    void multipleGetValueReturnsSameResult() {
        Parser parser = new Parser("2+3");
        assertEquals(5.0, parser.getValue(), DELTA);
        assertEquals(5.0, parser.getValue(), DELTA);
    }

    @Test
    void getTreeReturnsSameTreeOnMultipleCalls() {
        Parser parser = new Parser("2+3");
        ExpressionNode tree1 = parser.getTree();
        ExpressionNode tree2 = parser.getTree();
        assertEquals(tree1, tree2);
    }

    @Test
    void unmatchedOpenParenthesisThrowsParseError() {
        Parser parser = new Parser("(2+3");
        assertThrows(ParseError.class, parser::getValue);
    }

    @Test
    void functionMissingParenthesisThrowsParseError() {
        Parser parser = new Parser("sin 5");
        assertThrows(ParseError.class, parser::getValue);
    }

    @Test
    void emptyExpressionThrowsParseError() {
        Parser parser = new Parser("");
        assertThrows(ParseError.class, parser::getValue);
    }

    @Test
    void log2Function() {
        Parser parser = new Parser("log2(8)");
        assertEquals(3.0, parser.getValue(), DELTA);
    }

    @Test
    void expFunction() {
        Parser parser = new Parser("exp(1)");
        assertEquals(Math.E, parser.getValue(), DELTA);
    }

    @Test
    void longVariableNames() {
        Parser parser = new Parser("level * base_damage + armor_bonus");
        parser.setVariable("level", 10.0);
        parser.setVariable("base_damage", 5.0);
        parser.setVariable("armor_bonus", 3.0);
        assertEquals(53.0, parser.getValue(), DELTA);
    }

    @Test
    void chainedAddition() {
        Parser parser = new Parser("1+2+3+4+5");
        assertEquals(15.0, parser.getValue(), DELTA);
    }

    @Test
    void chainedMultiplication() {
        Parser parser = new Parser("2*3*4");
        assertEquals(24.0, parser.getValue(), DELTA);
    }

    @Test
    void mixedOperatorsComplexPrecedence() {
        Parser parser = new Parser("2+3*4-6/2+1");
        assertEquals(12.0, parser.getValue(), DELTA);
    }
}

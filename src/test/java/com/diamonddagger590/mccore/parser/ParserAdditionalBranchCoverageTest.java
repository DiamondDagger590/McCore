package com.diamonddagger590.mccore.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserAdditionalBranchCoverageTest {

    private static final double DELTA = 1e-9;

    @Test
    @DisplayName("Given variable starting with underscore, when evaluating, then isText recognizes underscore as first char")
    void getValue_recognizesUnderscore_whenVariableStartsWithUnderscore() {
        Parser parser = new Parser("_myVar + 1");
        parser.setVariable("_myVar", 5.0);
        assertEquals(6.0, parser.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given variable that is only underscore, when evaluating, then resolves correctly")
    void getValue_recognizesUnderscore_whenVariableIsSingleUnderscore() {
        Parser parser = new Parser("_ * 2");
        parser.setVariable("_", 3.0);
        assertEquals(6.0, parser.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given just a number, when getInputString called, then returns the number")
    void getInputString_returnsNumber_whenInputIsSimpleNumber() {
        assertEquals("42", new Parser("42").getInputString());
    }

    @Test
    @DisplayName("Given built-in constant pi, when evaluating, then returns Math.PI")
    void getValue_returnsMathPI_whenPiUsed() {
        assertEquals(Math.PI, new Parser("pi").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given built-in constant e, when evaluating, then returns Math.E")
    void getValue_returnsMathE_whenEUsed() {
        assertEquals(Math.E, new Parser("e").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given unrecognized characters in input, when convertInput strips them, then expression evaluates without them")
    void getValue_stripsUnrecognizedCharacters_whenInputContainsInvalidChars() {
        assertEquals(23.0, new Parser("2@3").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given expression with implicit multiplication, when evaluating, then handles correctly")
    void getValue_handlesImplicitMultiplication_whenConstantFollowedByVariable() {
        Parser parser = new Parser("2x");
        parser.setVariable("x", 5.0);
        assertEquals(10.0, parser.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given expression with implicit multiplication before function, when evaluating, then handles correctly")
    void getValue_handlesImplicitMultiplication_whenConstantFollowedByFunction() {
        assertEquals(2.0 * Math.sin(1.0), new Parser("2sin(1)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given error=true and unset variable, when evaluating via Parser, then throws EvaluationException")
    void getValue_throwsEvaluationException_whenErrorModeAndVariableUnset() {
        Parser parser = new Parser("x + 1", true);
        assertThrows(EvaluationException.class, parser::getValue);
    }

    @Test
    @DisplayName("Given error=true and variable is set, when evaluating via Parser, then returns correct value")
    void getValue_returnsCorrectValue_whenErrorModeAndVariableIsSet() {
        Parser parser = new Parser("x + 1", true);
        parser.setVariable("x", 4.0);
        assertEquals(5.0, parser.getValue(), DELTA);
    }
}

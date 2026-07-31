package com.diamonddagger590.mccore.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParserAdditionalBranchTest {

    private static final double DELTA = 1e-9;

    // ── convertInput: character filtering branches ────────────────────────

    @Test
    @DisplayName("Given input with special characters, when parsing, then strips unsupported chars")
    void getValue_stripsUnsupportedChars_whenInputHasSpecialCharacters() {
        assertEquals("2+3", new Parser("2 + 3!@#$").getInputString());
        assertEquals(5.0, new Parser("2+3").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given input with mixed valid and invalid characters, when getting input string, then only valid chars remain")
    void getInputString_containsOnlyValidChars_whenInputHasMixedCharacters() {
        String result = new Parser("a&b").getInputString();
        assertEquals("ab", result);
    }

    // ── detectImplicitMult: number followed by bracket (no implicit mult) ─

    @Test
    @DisplayName("Given number followed by open bracket, when parsing, then no implicit multiplication occurs")
    void getValue_noImplicitMult_whenNumberFollowedByBracket() {
        assertThrows(ParseError.class, () -> new Parser("2(3+4)").getValue());
    }

    @Test
    @DisplayName("Given number followed by another number, when parsing, then treats as single number")
    void getValue_concatenatesDigits_whenNumberFollowedByNumber() {
        assertEquals(23.0, new Parser("23").getValue(), DELTA);
    }

    // ── parse: exponential notation edge cases ────────────────────────────

    @Test
    @DisplayName("Given number with e followed by end of input, when parsing, then throws ParseError")
    void getValue_throwsParseError_whenNumberFollowedByConstantE() {
        assertThrows(ParseError.class, () -> new Parser("2e").getValue());
    }

    @Test
    @DisplayName("Given number like 1e-0, when parsing, then evaluates as 1.0")
    void getValue_parsesZeroExponent_whenExponentIsNegativeZero() {
        assertEquals(1.0, new Parser("1e-0").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given expression with multiple implicit multiplications, when evaluating, then all apply")
    void getValue_appliesMultipleImplicitMults_whenChainedConstantsAndVariables() {
        Parser p = new Parser("2x+3y");
        p.setVariable("x", 5.0);
        p.setVariable("y", 10.0);
        assertEquals(40.0, p.getValue(), DELTA);
    }

    // ── getTree: caching branch ───────────────────────────────────────────

    @Test
    @DisplayName("Given parser already parsed, when getTree called again, then returns cached tree")
    void getTree_returnsCachedTree_whenCalledMultipleTimes() {
        Parser parser = new Parser("2+3");
        ExpressionNode tree1 = parser.getTree();
        ExpressionNode tree2 = parser.getTree();
        assertTrue(tree1 == tree2);
    }

    // ── getParsedFunctions and getParsedVariables caching ─────────────────

    @Test
    @DisplayName("Given expression with no functions, when getting parsed functions, then returns empty set")
    void getParsedFunctions_returnsEmpty_whenExpressionHasNoFunctions() {
        assertTrue(new Parser("2+3").getParsedFunctions().isEmpty());
    }

    @Test
    @DisplayName("Given expression with no variables, when getting parsed variables, then returns empty set")
    void getParsedVariables_returnsEmpty_whenExpressionHasNoVariables() {
        assertTrue(new Parser("2+3").getParsedVariables().isEmpty());
    }

    @Test
    @DisplayName("Given expression with single function, when getting parsed functions, then contains that function")
    void getParsedFunctions_containsFunction_whenExpressionHasOneFunction() {
        var funcs = new Parser("abs(x)").getParsedFunctions();
        assertEquals(1, funcs.size());
        assertTrue(funcs.contains("abs"));
    }

    // ── modulo chains in H() ──────────────────────────────────────────────

    @Test
    @DisplayName("Given chained modulo operations, when evaluating, then applies left to right")
    void getValue_appliesLeftToRight_whenModuloChained() {
        assertEquals(1.0, new Parser("10%3%2").getValue(), DELTA);
    }

    // ── division chains in G() ────────────────────────────────────────────

    @Test
    @DisplayName("Given chained division operations, when evaluating, then applies left to right")
    void getValue_appliesLeftToRight_whenDivisionChained() {
        assertEquals(5.0, new Parser("100/10/2").getValue(), DELTA);
    }

    // ── subtraction chains in S() ─────────────────────────────────────────

    @Test
    @DisplayName("Given chained subtraction operations, when evaluating, then applies left to right")
    void getValue_appliesLeftToRight_whenSubtractionChained() {
        assertEquals(3.0, new Parser("10-5-2").getValue(), DELTA);
    }

    // ── ConstantNode for built-in pi/e via parser ─────────────────────────

    @Test
    @DisplayName("Given pi constant in expression, when evaluating, then uses Math.PI")
    void getValue_usesMathPI_whenPiUsedInExpression() {
        assertEquals(Math.PI * 2, new Parser("2*pi").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given e constant multiplied, when evaluating, then uses Math.E")
    void getValue_usesMathE_whenEUsedInExpression() {
        assertEquals(Math.E * 3, new Parser("3*e").getValue(), DELTA);
    }

    // ── OperatorNode toString additional branches ─────────────────────────

    @Test
    @DisplayName("Given division with left subtraction child, when converting to string, then adds brackets")
    void toString_addsBrackets_whenDivisionWithSubtractionLeft() {
        OperatorNode left = new OperatorNode(new ConstantNode(5.0), new ConstantNode(2.0), '-');
        OperatorNode node = new OperatorNode(left, new ConstantNode(3.0), '/');
        assertEquals("(5-2)/3", node.toString());
    }

    @Test
    @DisplayName("Given multiplication with right modulo child, when converting to string, then adds brackets")
    void toString_addsBrackets_whenMultiplicationWithModuloRight() {
        OperatorNode right = new OperatorNode(new ConstantNode(7.0), new ConstantNode(3.0), '%');
        OperatorNode node = new OperatorNode(new ConstantNode(2.0), right, '*');
        assertEquals("2*(7%3)", node.toString());
    }

    @Test
    @DisplayName("Given exponentiation with function children, when converting to string, then wraps both")
    void toString_addsBrackets_whenExponentiationWithFunctionChildren() {
        FunctionNode neg1 = new FunctionNode(new ConstantNode(2.0), 0);
        FunctionNode neg2 = new FunctionNode(new ConstantNode(3.0), 0);
        OperatorNode node = new OperatorNode(neg1, neg2, '^');
        assertEquals("(-2)^(-3)", node.toString());
    }

    @Test
    @DisplayName("Given modulo with function children, when converting to string, then wraps both")
    void toString_addsBrackets_whenModuloWithFunctionChildren() {
        FunctionNode neg = new FunctionNode(new ConstantNode(5.0), 0);
        OperatorNode child = new OperatorNode(new ConstantNode(7.0), new ConstantNode(2.0), '+');
        OperatorNode node = new OperatorNode(child, neg, '%');
        assertEquals("(7+2)%(-5)", node.toString());
    }
}

package com.diamonddagger590.mccore.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParserBranchCoverageTest {

    private static final double DELTA = 1e-9;

    @Test
    @DisplayName("Given expression with trailing characters, when getting tree, then throws ParseError")
    void getTree_throwsParseError_whenExpressionHasTrailingTokens() {
        assertThrows(ParseError.class, () -> new Parser("2+3)").getTree());
    }

    @Test
    @DisplayName("Given expression with extra closing bracket, when evaluating, then throws ParseError")
    void getValue_throwsParseError_whenExtraClosingBracket() {
        assertThrows(ParseError.class, () -> new Parser("(2+3))").getValue());
    }

    @Test
    @DisplayName("Given function name without parentheses, when evaluating, then throws ParseError")
    void getValue_throwsParseError_whenFunctionMissingOpenBracket() {
        assertThrows(ParseError.class, () -> new Parser("sin").getValue());
    }

    @Test
    @DisplayName("Given function call missing close bracket, when evaluating, then throws ParseError")
    void getValue_throwsParseError_whenFunctionMissingCloseBracket() {
        assertThrows(ParseError.class, () -> new Parser("sin(3").getValue());
    }

    @Test
    @DisplayName("Given nested function missing inner close bracket, when evaluating, then throws ParseError")
    void getValue_throwsParseError_whenNestedFunctionMissingCloseBracket() {
        assertThrows(ParseError.class, () -> new Parser("abs(sin(3)").getValue());
    }

    @Test
    @DisplayName("Given open bracket without close, when evaluating, then throws ParseError")
    void getValue_throwsParseError_whenMissingCloseBracketInGroup() {
        assertThrows(ParseError.class, () -> new Parser("(2+3").getValue());
    }

    @Test
    @DisplayName("Given deeply nested missing close bracket, when evaluating, then throws ParseError")
    void getValue_throwsParseError_whenDeeplyNestedMissingCloseBracket() {
        assertThrows(ParseError.class, () -> new Parser("((2+3)*4").getValue());
    }

    @Test
    @DisplayName("Given number with uppercase E exponent, when evaluating, then parses correctly")
    void getValue_parsesUppercaseExponent_whenExponentialNotationUsed() {
        assertEquals(1500.0, new Parser("1.5E3").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given number with positive exponent, when evaluating, then parses correctly")
    void getValue_parsesPositiveExponent_whenExplicitPlusNotUsed() {
        assertEquals(200.0, new Parser("2e2").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given number with negative exponent, when evaluating, then parses correctly")
    void getValue_parsesNegativeExponent_whenExponentialNotationUsed() {
        assertEquals(0.002, new Parser("2e-3").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given number with multi-digit exponent, when evaluating, then parses correctly")
    void getValue_parsesMultiDigitExponent_whenLargeExponent() {
        assertEquals(1e10, new Parser("1e10").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given decimal with exponent, when evaluating, then parses correctly")
    void getValue_parsesDecimalWithExponent_whenCombined() {
        assertEquals(3.14e2, new Parser("3.14e2").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given unsupported function name, when constructing FunctionNode, then throws IllegalArgumentException")
    void functionNode_throwsIllegalArgument_whenFunctionNameUnsupported() {
        assertThrows(IllegalArgumentException.class,
            () -> new FunctionNode(new ConstantNode(1.0), "notafunction"));
    }

    @Test
    @DisplayName("Given valid function name string, when constructing FunctionNode, then evaluates correctly")
    void functionNode_evaluatesCorrectly_whenConstructedWithStringName() {
        FunctionNode node = new FunctionNode(new ConstantNode(0.0), "sin");
        assertEquals(0.0, node.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given negation function, when converting to string with operator child, then wraps in brackets")
    void functionNode_toString_wrapsBrackets_whenNegationWithOperatorChild() {
        OperatorNode child = new OperatorNode(new ConstantNode(1.0), new ConstantNode(2.0), '+');
        FunctionNode neg = new FunctionNode(child, 0);
        assertEquals("-(1+2)", neg.toString());
    }

    @Test
    @DisplayName("Given negation function, when converting to string with constant child, then omits brackets")
    void functionNode_toString_omitsBrackets_whenNegationWithConstantChild() {
        FunctionNode neg = new FunctionNode(new ConstantNode(5.0), 0);
        assertEquals("-5", neg.toString());
    }

    @Test
    @DisplayName("Given negation function, when converting to string with variable child, then omits brackets")
    void functionNode_toString_omitsBrackets_whenNegationWithVariableChild() {
        FunctionNode neg = new FunctionNode(new VariableNode("x", false), 0);
        assertEquals("-x", neg.toString());
    }

    @Test
    @DisplayName("Given negation of negation, when converting to string, then wraps inner in brackets")
    void functionNode_toString_wrapsBrackets_whenNegationOfNegation() {
        FunctionNode inner = new FunctionNode(new ConstantNode(5.0), 0);
        FunctionNode outer = new FunctionNode(inner, 0);
        assertEquals("-(-5)", outer.toString());
    }

    @Test
    @DisplayName("Given non-negation function, when converting to string, then uses function name with brackets")
    void functionNode_toString_usesFunctionName_whenNotNegation() {
        FunctionNode node = new FunctionNode(new ConstantNode(1.0), "abs");
        assertEquals("abs(1)", node.toString());
    }

    @Test
    @DisplayName("Given a FunctionNode, when cloned, then clone is independent")
    void functionNode_clone_createsIndependentCopy() {
        VariableNode x = new VariableNode("x", false);
        x.setVariable("x", -5.0);
        FunctionNode original = new FunctionNode(x, "abs");
        FunctionNode cloned = (FunctionNode) original.clone();

        assertEquals(5.0, original.getValue(), DELTA);
        assertEquals(5.0, cloned.getValue(), DELTA);
        x.setVariable("x", -3.0);
        assertEquals(3.0, original.getValue(), DELTA);
        assertEquals(5.0, cloned.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given a FunctionNode, when getting type, then returns FUNCTION_NODE")
    void functionNode_getType_returnsFunctionNode() {
        FunctionNode node = new FunctionNode(new ConstantNode(1.0), "sin");
        assertEquals(ExpressionNode.FUNCTION_NODE, node.getType());
    }

    @Test
    @DisplayName("Given a FunctionNode, when getting subtype, then returns function name")
    void functionNode_getSubtype_returnsFunctionName() {
        FunctionNode node = new FunctionNode(new ConstantNode(1.0), "cos");
        assertEquals("cos", node.getSubtype());
    }

    @Test
    @DisplayName("Given a FunctionNode with constant child, when getting depth, then returns 2")
    void functionNode_getDepth_returnsTwo_whenChildIsConstant() {
        FunctionNode node = new FunctionNode(new ConstantNode(1.0), "sin");
        assertEquals(2, node.getDepth());
    }

    @Test
    @DisplayName("Given a FunctionNode with constant child, when counting, then returns 2")
    void functionNode_count_returnsTwo_whenChildIsConstant() {
        FunctionNode node = new FunctionNode(new ConstantNode(1.0), "sin");
        assertEquals(2, node.count());
    }

    @Test
    @DisplayName("Given a FunctionNode, when getting children, then returns array with child")
    void functionNode_getChildrenNodes_returnsChildArray() {
        ConstantNode child = new ConstantNode(1.0);
        FunctionNode node = new FunctionNode(child, "sin");
        ExpressionNode[] children = node.getChildrenNodes();
        assertEquals(1, children.length);
        assertEquals(child, children[0]);
    }

    @Test
    @DisplayName("Given a FunctionNode with variable child, when setting variable, then propagates")
    void functionNode_setVariable_propagatesToChild() {
        VariableNode x = new VariableNode("x", false);
        FunctionNode node = new FunctionNode(x, "abs");
        node.setVariable("x", -7.0);
        assertEquals(7.0, node.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given asinh(1), when evaluating via parser, then returns correct value")
    void getValue_returnsCorrectValue_whenAsinhCalled() {
        double expected = Sfun.asinh(1.0);
        assertEquals(expected, new Parser("asinh(1)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given acosh(2), when evaluating via parser, then returns correct value")
    void getValue_returnsCorrectValue_whenAcoshCalled() {
        double expected = Sfun.acosh(2.0);
        assertEquals(expected, new Parser("acosh(2)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given atanh(0.5), when evaluating via parser, then returns correct value")
    void getValue_returnsCorrectValue_whenAtanhCalled() {
        double expected = Sfun.atanh(0.5);
        assertEquals(expected, new Parser("atanh(0.5)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given erf(1), when evaluating via parser, then returns correct value")
    void getValue_returnsCorrectValue_whenErfCalled() {
        double expected = Sfun.erf(1.0);
        assertEquals(expected, new Parser("erf(1)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given erfc(1), when evaluating via parser, then returns correct value")
    void getValue_returnsCorrectValue_whenErfcCalled() {
        double expected = Sfun.erfc(1.0);
        assertEquals(expected, new Parser("erfc(1)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given gamma(5), when evaluating via parser, then returns 24 (4!)")
    void getValue_returns24_whenGammaOf5() {
        assertEquals(24.0, new Parser("gamma(5)").getValue(), 1e-6);
    }

    @Test
    @DisplayName("Given cot(1), when evaluating via parser, then returns correct value")
    void getValue_returnsCorrectValue_whenCotCalled() {
        double expected = Sfun.cot(1.0);
        assertEquals(expected, new Parser("cot(1)").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given a ConstantNode, when getting type, then returns CONSTANT_NODE")
    void constantNode_getType_returnsConstantNode() {
        assertEquals(ExpressionNode.CONSTANT_NODE, new ConstantNode(1.0).getType());
    }

    @Test
    @DisplayName("Given an integer ConstantNode, when getting subtype, then returns integer string")
    void constantNode_getSubtype_returnsIntegerString_whenValueIsInteger() {
        assertEquals("1", new ConstantNode(1.0).getSubtype());
    }

    @Test
    @DisplayName("Given a decimal ConstantNode, when getting subtype, then returns decimal string")
    void constantNode_getSubtype_returnsDecimalString_whenValueIsDecimal() {
        assertEquals("3.14", new ConstantNode(3.14).getSubtype());
    }

    @Test
    @DisplayName("Given a ConstantNode, when getting depth, then returns 1")
    void constantNode_getDepth_returnsOne() {
        assertEquals(1, new ConstantNode(1.0).getDepth());
    }

    @Test
    @DisplayName("Given a ConstantNode, when counting, then returns 1")
    void constantNode_count_returnsOne() {
        assertEquals(1, new ConstantNode(1.0).count());
    }

    @Test
    @DisplayName("Given a ConstantNode, when getting children, then returns empty array")
    void constantNode_getChildrenNodes_returnsEmptyArray() {
        assertEquals(0, new ConstantNode(1.0).getChildrenNodes().length);
    }

    @Test
    @DisplayName("Given a ConstantNode, when cloned, then returns equal value")
    void constantNode_clone_returnsEqualValue() {
        ConstantNode original = new ConstantNode(42.0);
        ConstantNode cloned = (ConstantNode) original.clone();
        assertEquals(42.0, cloned.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given a ConstantNode, when converting to string, then returns number string")
    void constantNode_toString_returnsNumberString() {
        assertTrue(new ConstantNode(3.14).toString().startsWith("3.14"));
    }

    @Test
    @DisplayName("Given a VariableNode, when getting type, then returns VARIABLE_NODE")
    void variableNode_getType_returnsVariableNode() {
        assertEquals(ExpressionNode.VARIABLE_NODE, new VariableNode("x", false).getType());
    }

    @Test
    @DisplayName("Given a VariableNode, when getting subtype, then returns variable name")
    void variableNode_getSubtype_returnsName() {
        assertEquals("x", new VariableNode("x", false).getSubtype());
    }

    @Test
    @DisplayName("Given a VariableNode, when getting depth, then returns 1")
    void variableNode_getDepth_returnsOne() {
        assertEquals(1, new VariableNode("x", false).getDepth());
    }

    @Test
    @DisplayName("Given a VariableNode, when counting, then returns 1")
    void variableNode_count_returnsOne() {
        assertEquals(1, new VariableNode("x", false).count());
    }

    @Test
    @DisplayName("Given a VariableNode, when getting children, then returns empty array")
    void variableNode_getChildrenNodes_returnsEmptyArray() {
        assertEquals(0, new VariableNode("x", false).getChildrenNodes().length);
    }

    @Test
    @DisplayName("Given a VariableNode, when cloned, then clone is independent")
    void variableNode_clone_createsIndependentCopy() {
        VariableNode original = new VariableNode("x", false);
        original.setVariable("x", 5.0);
        VariableNode cloned = (VariableNode) original.clone();
        assertEquals(5.0, cloned.getValue(), DELTA);
        original.setVariable("x", 10.0);
        assertEquals(5.0, cloned.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given a VariableNode, when converting to string, then returns variable name")
    void variableNode_toString_returnsName() {
        assertEquals("x", new VariableNode("x", false).toString());
    }

    @Test
    @DisplayName("Given a VariableNode with error=true and unset, when getting value, then throws EvaluationException")
    void variableNode_getValue_throwsException_whenErrorEnabledAndUnset() {
        VariableNode node = new VariableNode("x", true);
        assertThrows(EvaluationException.class, node::getValue);
    }

    @Test
    @DisplayName("Given a VariableNode with non-matching name, when setting variable, then value unchanged")
    void variableNode_setVariable_doesNothing_whenNameDoesNotMatch() {
        VariableNode node = new VariableNode("x", false);
        node.setVariable("y", 5.0);
        assertEquals(0.0, node.getValue(), DELTA);
    }
}

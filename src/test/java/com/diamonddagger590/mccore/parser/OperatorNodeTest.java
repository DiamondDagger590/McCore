package com.diamonddagger590.mccore.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class OperatorNodeTest {

    private static final double DELTA = 1e-9;

    private ConstantNode c(double val) {
        return new ConstantNode(val);
    }
    @Test
    @DisplayName("Given addition operator, when evaluating, then returns sum of children")
    void getValue_returnsSum_whenOperatorIsAddition() {
        OperatorNode node = new OperatorNode(c(3.0), c(4.0), '+');
        assertEquals(7.0, node.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given subtraction operator, when evaluating, then returns difference of children")
    void getValue_returnsDifference_whenOperatorIsSubtraction() {
        OperatorNode node = new OperatorNode(c(10.0), c(4.0), '-');
        assertEquals(6.0, node.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given multiplication operator, when evaluating, then returns product of children")
    void getValue_returnsProduct_whenOperatorIsMultiplication() {
        OperatorNode node = new OperatorNode(c(3.0), c(7.0), '*');
        assertEquals(21.0, node.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given division operator, when evaluating, then returns quotient of children")
    void getValue_returnsQuotient_whenOperatorIsDivision() {
        OperatorNode node = new OperatorNode(c(20.0), c(4.0), '/');
        assertEquals(5.0, node.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given modulo operator, when evaluating, then returns remainder of children")
    void getValue_returnsRemainder_whenOperatorIsModulo() {
        OperatorNode node = new OperatorNode(c(10.0), c(3.0), '%');
        assertEquals(1.0, node.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given exponentiation operator, when evaluating, then returns power of children")
    void getValue_returnsPower_whenOperatorIsExponentiation() {
        OperatorNode node = new OperatorNode(c(2.0), c(10.0), '^');
        assertEquals(1024.0, node.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given division by zero, when evaluating, then returns infinity")
    void getValue_returnsInfinity_whenDividingByZero() {
        OperatorNode node = new OperatorNode(c(1.0), c(0.0), '/');
        assertEquals(Double.POSITIVE_INFINITY, node.getValue());
    }
    @Test
    @DisplayName("Given an OperatorNode, when getting type, then returns OPERATOR_NODE")
    void getType_returnsOperatorNode_always() {
        assertEquals(ExpressionNode.OPERATOR_NODE, new OperatorNode(c(1.0), c(2.0), '+').getType());
    }
    @Test
    @DisplayName("Given an OperatorNode, when getting subtype, then returns operator character")
    void getSubtype_returnsOperatorChar_always() {
        assertEquals("+", new OperatorNode(c(1.0), c(2.0), '+').getSubtype());
        assertEquals("-", new OperatorNode(c(1.0), c(2.0), '-').getSubtype());
        assertEquals("*", new OperatorNode(c(1.0), c(2.0), '*').getSubtype());
        assertEquals("/", new OperatorNode(c(1.0), c(2.0), '/').getSubtype());
        assertEquals("%", new OperatorNode(c(1.0), c(2.0), '%').getSubtype());
        assertEquals("^", new OperatorNode(c(1.0), c(2.0), '^').getSubtype());
    }
    @Test
    @DisplayName("Given two constant children, when getting depth, then returns 2")
    void getDepth_returnsTwo_whenChildrenAreConstants() {
        OperatorNode node = new OperatorNode(c(1.0), c(2.0), '+');
        assertEquals(2, node.getDepth());
    }

    @Test
    @DisplayName("Given nested operators, when getting depth, then returns correct depth")
    void getDepth_returnsCorrectDepth_whenNested() {
        OperatorNode inner = new OperatorNode(c(1.0), c(2.0), '+');
        OperatorNode outer = new OperatorNode(inner, c(3.0), '*');
        assertEquals(3, outer.getDepth());
    }
    @Test
    @DisplayName("Given two constant children, when counting, then returns 3")
    void count_returnsThree_whenChildrenAreConstants() {
        OperatorNode node = new OperatorNode(c(1.0), c(2.0), '+');
        assertEquals(3, node.count());
    }

    @Test
    @DisplayName("Given nested operators, when counting, then returns total node count")
    void count_returnsTotalNodes_whenNested() {
        OperatorNode inner = new OperatorNode(c(1.0), c(2.0), '+');
        OperatorNode outer = new OperatorNode(inner, c(3.0), '*');
        assertEquals(5, outer.count());
    }
    @Test
    @DisplayName("Given an OperatorNode, when getting children, then returns array of left and right")
    void getChildrenNodes_returnsLeftAndRight_always() {
        ConstantNode left = c(1.0);
        ConstantNode right = c(2.0);
        OperatorNode node = new OperatorNode(left, right, '+');
        ExpressionNode[] children = node.getChildrenNodes();
        assertEquals(2, children.length);
        assertEquals(left, children[0]);
        assertEquals(right, children[1]);
    }
    @Test
    @DisplayName("Given operator with variable children, when setting variable, then propagates to both")
    void setVariable_propagatesToBothChildren_always() {
        VariableNode x = new VariableNode("x", false);
        VariableNode y = new VariableNode("x", false);
        OperatorNode node = new OperatorNode(x, y, '+');
        node.setVariable("x", 5.0);
        assertEquals(10.0, node.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given operator with mixed children, when setting variable, then only matching child updates")
    void setVariable_updatesOnlyMatchingChild_whenMixed() {
        VariableNode x = new VariableNode("x", false);
        ConstantNode five = c(5.0);
        OperatorNode node = new OperatorNode(x, five, '*');
        node.setVariable("x", 3.0);
        assertEquals(15.0, node.getValue(), DELTA);
    }
    @Test
    @DisplayName("Given an OperatorNode, when cloned, then clone is independent")
    void clone_createsIndependentCopy_always() {
        VariableNode x = new VariableNode("x", false);
        x.setVariable("x", 5.0);
        OperatorNode original = new OperatorNode(x, c(2.0), '*');
        OperatorNode cloned = (OperatorNode) original.clone();

        assertNotSame(original, cloned);
        assertEquals(original.getValue(), cloned.getValue(), DELTA);

        x.setVariable("x", 99.0);
        assertEquals(198.0, original.getValue(), DELTA);
        assertEquals(10.0, cloned.getValue(), DELTA);
    }
    @Test
    @DisplayName("Given simple addition of constants, when converting to string, then no brackets")
    void toString_omitsBrackets_whenChildrenAreConstants() {
        OperatorNode node = new OperatorNode(c(3.0), c(4.0), '+');
        assertEquals("3+4", node.toString());
    }

    @Test
    @DisplayName("Given multiplication with addition child, when converting to string, then adds brackets")
    void toString_addsBrackets_whenPrecedenceRequires() {
        OperatorNode inner = new OperatorNode(c(1.0), c(2.0), '+');
        OperatorNode outer = new OperatorNode(inner, c(3.0), '*');
        assertEquals("(1+2)*3", outer.toString());
    }

    @Test
    @DisplayName("Given subtraction on right with addition, when converting to string, then adds right brackets")
    void toString_addsBrackets_forRightChildSubtraction() {
        OperatorNode rightChild = new OperatorNode(c(2.0), c(3.0), '+');
        OperatorNode node = new OperatorNode(c(10.0), rightChild, '-');
        assertEquals("10-(2+3)", node.toString());
    }

    @Test
    @DisplayName("Given addition of additions, when converting to string, then no brackets needed")
    void toString_omitsBrackets_whenSamePrecedence() {
        OperatorNode left = new OperatorNode(c(1.0), c(2.0), '+');
        OperatorNode node = new OperatorNode(left, c(3.0), '+');
        assertEquals("1+2+3", node.toString());
    }

    @Test
    @DisplayName("Given exponentiation with operator children, when converting to string, then wraps both")
    void toString_addsBrackets_forExponentiationChildren() {
        OperatorNode left = new OperatorNode(c(1.0), c(2.0), '+');
        OperatorNode right = new OperatorNode(c(3.0), c(4.0), '*');
        OperatorNode node = new OperatorNode(left, right, '^');
        assertEquals("(1+2)^(3*4)", node.toString());
    }

    @Test
    @DisplayName("Given negation child in operator, when converting to string, then wraps negation in brackets")
    void toString_addsBrackets_forNegationFunctionChild() {
        FunctionNode neg = new FunctionNode(c(5.0), 0);
        OperatorNode node = new OperatorNode(neg, c(3.0), '*');
        assertEquals("(-5)*3", node.toString());
    }

    @Test
    @DisplayName("Given division with multiplication left child, when converting to string, then no left brackets")
    void toString_omitsLeftBrackets_whenDivisionWithMultiplicationLeft() {
        OperatorNode left = new OperatorNode(c(2.0), c(3.0), '*');
        OperatorNode node = new OperatorNode(left, c(4.0), '/');
        assertEquals("2*3/4", node.toString());
    }

    @Test
    @DisplayName("Given division with division right child, when converting to string, then adds right brackets")
    void toString_addsBrackets_whenDivisionWithDivisionRight() {
        OperatorNode right = new OperatorNode(c(4.0), c(2.0), '/');
        OperatorNode node = new OperatorNode(c(8.0), right, '/');
        assertEquals("8/(4/2)", node.toString());
    }

    @Test
    @DisplayName("Given modulo with operator child, when converting to string, then adds brackets")
    void toString_addsBrackets_forModuloChildren() {
        OperatorNode left = new OperatorNode(c(1.0), c(2.0), '+');
        OperatorNode node = new OperatorNode(left, c(3.0), '%');
        assertEquals("(1+2)%3", node.toString());
    }

    // ── needBrackets branch coverage: VARIABLE_NODE child ──────────────────

    @Test
    @DisplayName("Given addition with variable children, when converting to string, then omits brackets")
    void toString_omitsBrackets_whenChildrenAreVariables() {
        VariableNode x = new VariableNode("x", false);
        VariableNode y = new VariableNode("y", false);
        OperatorNode node = new OperatorNode(x, y, '+');
        assertEquals("x+y", node.toString());
    }

    // ── needBrackets branch coverage: FUNCTION_NODE non-negation child ─────

    @Test
    @DisplayName("Given multiplication with non-negation function child, when converting to string, then omits brackets")
    void toString_omitsBrackets_whenFunctionChildIsNotNegation() {
        FunctionNode sinNode = new FunctionNode(c(1.0), 1);
        OperatorNode node = new OperatorNode(sinNode, c(2.0), '*');
        assertEquals("sin(1)*2", node.toString());
    }

    @Test
    @DisplayName("Given addition with non-negation function child on right, when converting to string, then omits brackets")
    void toString_omitsBrackets_whenRightChildIsFunctionNotNegation() {
        FunctionNode cosNode = new FunctionNode(c(3.14), 2);
        OperatorNode node = new OperatorNode(c(1.0), cosNode, '+');
        assertEquals("1+cos(3.14)", node.toString());
    }

    // ── needBrackets branch coverage: '-' operator with left add/sub child ─

    @Test
    @DisplayName("Given subtraction with left child being addition, when converting to string, then omits left brackets")
    void toString_omitsLeftBrackets_whenSubtractionWithAdditionLeft() {
        OperatorNode left = new OperatorNode(c(1.0), c(2.0), '+');
        OperatorNode node = new OperatorNode(left, c(3.0), '-');
        assertEquals("1+2-3", node.toString());
    }

    @Test
    @DisplayName("Given subtraction with left child being subtraction, when converting to string, then omits left brackets")
    void toString_omitsLeftBrackets_whenSubtractionWithSubtractionLeft() {
        OperatorNode left = new OperatorNode(c(5.0), c(2.0), '-');
        OperatorNode node = new OperatorNode(left, c(1.0), '-');
        assertEquals("5-2-1", node.toString());
    }

    @Test
    @DisplayName("Given subtraction with right child being subtraction, when converting to string, then adds right brackets")
    void toString_addsBrackets_whenSubtractionWithSubtractionRight() {
        OperatorNode right = new OperatorNode(c(3.0), c(1.0), '-');
        OperatorNode node = new OperatorNode(c(10.0), right, '-');
        assertEquals("10-(3-1)", node.toString());
    }

    // ── needBrackets branch coverage: '-' operator with non-add/sub child ──

    @Test
    @DisplayName("Given subtraction with right child being multiplication, when converting to string, then omits right brackets")
    void toString_omitsRightBrackets_whenSubtractionWithMultiplicationRight() {
        OperatorNode right = new OperatorNode(c(2.0), c(3.0), '*');
        OperatorNode node = new OperatorNode(c(10.0), right, '-');
        assertEquals("10-2*3", node.toString());
    }

    // ── needBrackets branch coverage: '*' operator with '%' child ──────────

    @Test
    @DisplayName("Given multiplication with left modulo child, when converting to string, then adds brackets")
    void toString_addsBrackets_whenMultiplicationWithModuloLeft() {
        OperatorNode left = new OperatorNode(c(7.0), c(3.0), '%');
        OperatorNode node = new OperatorNode(left, c(2.0), '*');
        assertEquals("(7%3)*2", node.toString());
    }

    // ── needBrackets branch coverage: '*' operator with '*' or '/' child ───

    @Test
    @DisplayName("Given multiplication with left multiplication child, when converting to string, then omits brackets")
    void toString_omitsBrackets_whenMultiplicationWithMultiplicationLeft() {
        OperatorNode left = new OperatorNode(c(2.0), c(3.0), '*');
        OperatorNode node = new OperatorNode(left, c(4.0), '*');
        assertEquals("2*3*4", node.toString());
    }

    @Test
    @DisplayName("Given multiplication with right division child, when converting to string, then omits brackets")
    void toString_omitsBrackets_whenMultiplicationWithDivisionRight() {
        OperatorNode right = new OperatorNode(c(6.0), c(2.0), '/');
        OperatorNode node = new OperatorNode(c(3.0), right, '*');
        assertEquals("3*6/2", node.toString());
    }

    // ── needBrackets branch coverage: '/' operator with left add child ─────

    @Test
    @DisplayName("Given division with left addition child, when converting to string, then adds brackets")
    void toString_addsBrackets_whenDivisionWithAdditionLeft() {
        OperatorNode left = new OperatorNode(c(1.0), c(2.0), '+');
        OperatorNode node = new OperatorNode(left, c(3.0), '/');
        assertEquals("(1+2)/3", node.toString());
    }

    // ── needBrackets branch coverage: '^' with variable children ───────────

    @Test
    @DisplayName("Given exponentiation with variable children, when converting to string, then omits brackets")
    void toString_omitsBrackets_whenExponentiationWithVariableChildren() {
        VariableNode x = new VariableNode("x", false);
        VariableNode y = new VariableNode("y", false);
        OperatorNode node = new OperatorNode(x, y, '^');
        assertEquals("x^y", node.toString());
    }

    // ── needBrackets branch coverage: '%' with constant children ───────────

    @Test
    @DisplayName("Given modulo with constant children, when converting to string, then omits brackets")
    void toString_omitsBrackets_whenModuloWithConstantChildren() {
        OperatorNode node = new OperatorNode(c(10.0), c(3.0), '%');
        assertEquals("10%3", node.toString());
    }

    // ── needBrackets branch coverage: '/' with right multiplication child ──

    @Test
    @DisplayName("Given division with right multiplication child, when converting to string, then adds brackets")
    void toString_addsBrackets_whenDivisionWithMultiplicationRight() {
        OperatorNode right = new OperatorNode(c(2.0), c(3.0), '*');
        OperatorNode node = new OperatorNode(c(12.0), right, '/');
        assertEquals("12/(2*3)", node.toString());
    }

    // ── needBrackets branch coverage: '+' with operator children ───────────

    @Test
    @DisplayName("Given addition with multiplication children, when converting to string, then omits all brackets")
    void toString_omitsBrackets_whenAdditionWithMultiplicationChildren() {
        OperatorNode left = new OperatorNode(c(2.0), c(3.0), '*');
        OperatorNode right = new OperatorNode(c(4.0), c(5.0), '*');
        OperatorNode node = new OperatorNode(left, right, '+');
        assertEquals("2*3+4*5", node.toString());
    }

    // ── needBrackets branch coverage: '*' with subtraction child ───────────

    @Test
    @DisplayName("Given multiplication with right subtraction child, when converting to string, then adds brackets")
    void toString_addsBrackets_whenMultiplicationWithSubtractionRight() {
        OperatorNode right = new OperatorNode(c(5.0), c(1.0), '-');
        OperatorNode node = new OperatorNode(c(3.0), right, '*');
        assertEquals("3*(5-1)", node.toString());
    }

    // ── needBrackets branch coverage: negation function on right ────────────

    @Test
    @DisplayName("Given addition with negation function on right, when converting to string, then adds brackets for negation")
    void toString_addsBrackets_whenNegationFunctionOnRight() {
        FunctionNode neg = new FunctionNode(c(3.0), 0);
        OperatorNode node = new OperatorNode(c(5.0), neg, '+');
        assertEquals("5+(-3)", node.toString());
    }

    // ── needBrackets branch coverage: '^' with operator children ───────────

    @Test
    @DisplayName("Given exponentiation with modulo left child, when converting to string, then adds brackets")
    void toString_addsBrackets_whenExponentiationWithModuloLeft() {
        OperatorNode left = new OperatorNode(c(10.0), c(3.0), '%');
        OperatorNode node = new OperatorNode(left, c(2.0), '^');
        assertEquals("(10%3)^2", node.toString());
    }

    // ── needBrackets branch coverage: '/' with left '/' child ──────────────

    @Test
    @DisplayName("Given division with left division child, when converting to string, then adds brackets for left")
    void toString_addsBrackets_whenDivisionWithDivisionLeft() {
        OperatorNode left = new OperatorNode(c(8.0), c(2.0), '/');
        OperatorNode node = new OperatorNode(left, c(4.0), '/');
        assertEquals("(8/2)/4", node.toString());
    }

    // ── getValue default switch branch ────────────────────────────────────

    @Test
    @DisplayName("Given an unsupported operator character, when evaluating, then returns 0")
    void getValue_returnsZero_whenOperatorIsUnsupported() {
        OperatorNode node = new OperatorNode(c(3.0), c(4.0), '!');
        assertEquals(0.0, node.getValue(), DELTA);
    }

    // ── needBrackets default switch branch ────────────────────────────────

    @Test
    @DisplayName("Given an unsupported operator with operator children, when converting to string, then adds brackets")
    void toString_addsBrackets_whenOperatorIsUnsupported() {
        OperatorNode left = new OperatorNode(c(1.0), c(2.0), '+');
        OperatorNode right = new OperatorNode(c(3.0), c(4.0), '*');
        OperatorNode node = new OperatorNode(left, right, '!');
        assertEquals("(1+2)!(3*4)", node.toString());
    }
}

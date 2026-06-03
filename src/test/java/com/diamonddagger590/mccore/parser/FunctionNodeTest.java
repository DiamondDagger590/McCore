package com.diamonddagger590.mccore.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FunctionNodeTest {

    private static final double DELTA = 1e-9;

    private ConstantNode c(double val) {
        return new ConstantNode(val);
    }
    @Test
    @DisplayName("Given valid function name, when creating FunctionNode, then succeeds")
    void constructor_succeeds_whenFunctionNameIsValid() {
        FunctionNode node = new FunctionNode(c(1.0), "sin");
        assertEquals(Math.sin(1.0), node.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given invalid function name, when creating FunctionNode, then throws IllegalArgumentException")
    void constructor_throwsException_whenFunctionNameInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new FunctionNode(c(1.0), "bogus"));
    }
    @Test
    @DisplayName("Given negation function (index 0), when evaluating, then returns negated value")
    void getValue_returnsNegated_whenFunctionIsNegation() {
        assertEquals(-5.0, new FunctionNode(c(5.0), 0).getValue(), DELTA);
    }

    @Test
    @DisplayName("Given sin function, when evaluating, then returns Math.sin")
    void getValue_returnsSin_whenFunctionIsSin() {
        assertEquals(Math.sin(1.0), new FunctionNode(c(1.0), "sin").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given cos function, when evaluating, then returns Math.cos")
    void getValue_returnsCos_whenFunctionIsCos() {
        assertEquals(Math.cos(1.0), new FunctionNode(c(1.0), "cos").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given tan function, when evaluating, then returns Math.tan")
    void getValue_returnsTan_whenFunctionIsTan() {
        assertEquals(Math.tan(1.0), new FunctionNode(c(1.0), "tan").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given asin function, when evaluating, then returns Math.asin")
    void getValue_returnsAsin_whenFunctionIsAsin() {
        assertEquals(Math.asin(0.5), new FunctionNode(c(0.5), "asin").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given acos function, when evaluating, then returns Math.acos")
    void getValue_returnsAcos_whenFunctionIsAcos() {
        assertEquals(Math.acos(0.5), new FunctionNode(c(0.5), "acos").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given atan function, when evaluating, then returns Math.atan")
    void getValue_returnsAtan_whenFunctionIsAtan() {
        assertEquals(Math.atan(1.0), new FunctionNode(c(1.0), "atan").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given sinh function, when evaluating, then returns Sfun.sinh")
    void getValue_returnsSinh_whenFunctionIsSinh() {
        assertEquals(Sfun.sinh(1.0), new FunctionNode(c(1.0), "sinh").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given cosh function, when evaluating, then returns Sfun.cosh")
    void getValue_returnsCosh_whenFunctionIsCosh() {
        assertEquals(Sfun.cosh(1.0), new FunctionNode(c(1.0), "cosh").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given tanh function, when evaluating, then returns Sfun.tanh")
    void getValue_returnsTanh_whenFunctionIsTanh() {
        assertEquals(Sfun.tanh(0.5), new FunctionNode(c(0.5), "tanh").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given asinh function, when evaluating, then returns Sfun.asinh")
    void getValue_returnsAsinh_whenFunctionIsAsinh() {
        assertEquals(Sfun.asinh(1.0), new FunctionNode(c(1.0), "asinh").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given acosh function, when evaluating, then returns Sfun.acosh")
    void getValue_returnsAcosh_whenFunctionIsAcosh() {
        assertEquals(Sfun.acosh(2.0), new FunctionNode(c(2.0), "acosh").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given atanh function, when evaluating, then returns Sfun.atanh")
    void getValue_returnsAtanh_whenFunctionIsAtanh() {
        assertEquals(Sfun.atanh(0.5), new FunctionNode(c(0.5), "atanh").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given ln function, when evaluating, then returns Math.log")
    void getValue_returnsLn_whenFunctionIsLn() {
        assertEquals(Math.log(2.0), new FunctionNode(c(2.0), "ln").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given log function, when evaluating, then returns log base 10")
    void getValue_returnsLog10_whenFunctionIsLog() {
        assertEquals(Math.log(100.0) * 0.43429448190325182765, new FunctionNode(c(100.0), "log").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given abs function, when evaluating, then returns Math.abs")
    void getValue_returnsAbs_whenFunctionIsAbs() {
        assertEquals(5.0, new FunctionNode(c(-5.0), "abs").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given rand function, when evaluating, then returns value in [0, child)")
    void getValue_returnsValueInRange_whenFunctionIsRand() {
        double result = new FunctionNode(c(10.0), "rand").getValue();
        assertTrue(result >= 0.0 && result < 10.0);
    }

    @Test
    @DisplayName("Given sqrt function, when evaluating, then returns Math.sqrt")
    void getValue_returnsSqrt_whenFunctionIsSqrt() {
        assertEquals(4.0, new FunctionNode(c(16.0), "sqrt").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given erf function, when evaluating, then returns Sfun.erf")
    void getValue_returnsErf_whenFunctionIsErf() {
        assertEquals(Sfun.erf(1.0), new FunctionNode(c(1.0), "erf").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given erfc function, when evaluating, then returns Sfun.erfc")
    void getValue_returnsErfc_whenFunctionIsErfc() {
        assertEquals(Sfun.erfc(1.0), new FunctionNode(c(1.0), "erfc").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given gamma function, when evaluating, then returns Sfun.gamma")
    void getValue_returnsGamma_whenFunctionIsGamma() {
        assertEquals(Sfun.gamma(5.0), new FunctionNode(c(5.0), "gamma").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given exp function, when evaluating, then returns Math.exp")
    void getValue_returnsExp_whenFunctionIsExp() {
        assertEquals(Math.exp(2.0), new FunctionNode(c(2.0), "exp").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given cot function, when evaluating, then returns Sfun.cot")
    void getValue_returnsCot_whenFunctionIsCot() {
        assertEquals(Sfun.cot(1.0), new FunctionNode(c(1.0), "cot").getValue(), DELTA);
    }

    @Test
    @DisplayName("Given log2 function, when evaluating, then returns log base 2")
    void getValue_returnsLog2_whenFunctionIsLog2() {
        assertEquals(3.0, new FunctionNode(c(8.0), "log2").getValue(), DELTA);
    }
    @Test
    @DisplayName("Given a FunctionNode, when getting type, then returns FUNCTION_NODE")
    void getType_returnsFunctionNode_always() {
        assertEquals(ExpressionNode.FUNCTION_NODE, new FunctionNode(c(1.0), "sin").getType());
    }
    @Test
    @DisplayName("Given a FunctionNode, when getting subtype, then returns function name")
    void getSubtype_returnsFunctionName_always() {
        assertEquals("sin", new FunctionNode(c(1.0), "sin").getSubtype());
        assertEquals("-", new FunctionNode(c(1.0), 0).getSubtype());
    }
    @Test
    @DisplayName("Given function with constant child, when getting depth, then returns 2")
    void getDepth_returnsTwo_whenChildIsConstant() {
        assertEquals(2, new FunctionNode(c(1.0), "sin").getDepth());
    }

    @Test
    @DisplayName("Given nested functions, when getting depth, then returns correct depth")
    void getDepth_returnsCorrectDepth_whenNested() {
        FunctionNode inner = new FunctionNode(c(1.0), "sin");
        FunctionNode outer = new FunctionNode(inner, "abs");
        assertEquals(3, outer.getDepth());
    }

    @Test
    @DisplayName("Given function with constant child, when counting, then returns 2")
    void count_returnsTwo_whenChildIsConstant() {
        assertEquals(2, new FunctionNode(c(1.0), "sin").count());
    }
    @Test
    @DisplayName("Given a FunctionNode, when getting children, then returns array with child")
    void getChildrenNodes_returnsSingleChild_always() {
        ConstantNode child = c(1.0);
        FunctionNode node = new FunctionNode(child, "sin");
        ExpressionNode[] children = node.getChildrenNodes();
        assertEquals(1, children.length);
        assertEquals(child, children[0]);
    }
    @Test
    @DisplayName("Given function with variable child, when setting variable, then propagates")
    void setVariable_propagatesToChild_whenVariableMatches() {
        VariableNode x = new VariableNode("x", false);
        FunctionNode node = new FunctionNode(x, "abs");
        node.setVariable("x", -7.0);
        assertEquals(7.0, node.getValue(), DELTA);
    }
    @Test
    @DisplayName("Given a FunctionNode, when cloned, then clone is independent")
    void clone_createsIndependentCopy_always() {
        VariableNode x = new VariableNode("x", false);
        x.setVariable("x", 4.0);
        FunctionNode original = new FunctionNode(x, "sqrt");
        FunctionNode cloned = (FunctionNode) original.clone();
        assertNotSame(original, cloned);
        assertEquals(2.0, cloned.getValue(), DELTA);

        x.setVariable("x", 16.0);
        assertEquals(4.0, original.getValue(), DELTA);
        assertEquals(2.0, cloned.getValue(), DELTA);
    }
    @Test
    @DisplayName("Given non-negation function, when converting to string, then returns function(child)")
    void toString_returnsFunctionFormat_whenNotNegation() {
        assertEquals("sin(5)", new FunctionNode(c(5.0), "sin").toString());
        assertEquals("abs(3)", new FunctionNode(c(3.0), "abs").toString());
    }

    @Test
    @DisplayName("Given negation of constant, when converting to string, then returns -value without parens")
    void toString_returnsNegationWithoutParens_whenChildIsConstant() {
        assertEquals("-5", new FunctionNode(c(5.0), 0).toString());
    }

    @Test
    @DisplayName("Given negation of variable, when converting to string, then returns -name without parens")
    void toString_returnsNegationWithoutParens_whenChildIsVariable() {
        assertEquals("-x", new FunctionNode(new VariableNode("x", false), 0).toString());
    }

    @Test
    @DisplayName("Given negation of operator, when converting to string, then adds parens")
    void toString_addsParens_whenNegatingOperator() {
        OperatorNode add = new OperatorNode(c(1.0), c(2.0), '+');
        assertEquals("-(1+2)", new FunctionNode(add, 0).toString());
    }

    @Test
    @DisplayName("Given negation of non-negation function, when converting to string, then no parens")
    void toString_omitsParens_whenNegatingNonNegationFunction() {
        FunctionNode sin = new FunctionNode(c(1.0), "sin");
        assertEquals("-sin(1)", new FunctionNode(sin, 0).toString());
    }

    @Test
    @DisplayName("Given negation of negation, when converting to string, then adds parens")
    void toString_addsParens_whenDoubleNegation() {
        FunctionNode inner = new FunctionNode(c(5.0), 0);
        assertEquals("-(-5)", new FunctionNode(inner, 0).toString());
    }
}

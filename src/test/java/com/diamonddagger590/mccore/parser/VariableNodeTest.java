package com.diamonddagger590.mccore.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VariableNodeTest {

    private static final double DELTA = 1e-9;

    // ── getValue ──

    @Test
    @DisplayName("Given uninitialized variable with error=false, when getting value, then returns 0")
    void getValue_returnsZero_whenUninitializedAndErrorDisabled() {
        VariableNode node = new VariableNode("x", false);
        assertEquals(0.0, node.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given uninitialized variable with error=true, when getting value, then throws EvaluationException")
    void getValue_throwsException_whenUninitializedAndErrorEnabled() {
        VariableNode node = new VariableNode("x", true);
        EvaluationException ex = assertThrows(EvaluationException.class, node::getValue);
        assertEquals("Variable 'x' was not initialized.", ex.getMessage());
    }

    @Test
    @DisplayName("Given initialized variable, when getting value, then returns set value")
    void getValue_returnsSetValue_whenVariableInitialized() {
        VariableNode node = new VariableNode("x", true);
        node.setVariable("x", 42.0);
        assertEquals(42.0, node.getValue(), DELTA);
    }

    // ── setVariable ──

    @Test
    @DisplayName("Given matching name, when setting variable, then value is updated")
    void setVariable_updatesValue_whenNameMatches() {
        VariableNode node = new VariableNode("x", false);
        node.setVariable("x", 7.5);
        assertEquals(7.5, node.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given non-matching name, when setting variable, then value is unchanged")
    void setVariable_doesNotUpdate_whenNameDoesNotMatch() {
        VariableNode node = new VariableNode("x", false);
        node.setVariable("y", 7.5);
        assertEquals(0.0, node.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given variable set with error=true, when resetting value, then no longer throws")
    void setVariable_clearsErrorFlag_whenValueSet() {
        VariableNode node = new VariableNode("x", true);
        assertThrows(EvaluationException.class, node::getValue);
        node.setVariable("x", 5.0);
        assertEquals(5.0, node.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given variable reassigned, when getting value, then returns latest value")
    void setVariable_updatesToLatestValue_whenReassigned() {
        VariableNode node = new VariableNode("x", false);
        node.setVariable("x", 1.0);
        node.setVariable("x", 99.0);
        assertEquals(99.0, node.getValue(), DELTA);
    }

    // ── getType ──

    @Test
    @DisplayName("Given a VariableNode, when getting type, then returns VARIABLE_NODE")
    void getType_returnsVariableNode_always() {
        assertEquals(ExpressionNode.VARIABLE_NODE, new VariableNode("x", false).getType());
    }

    // ── getSubtype ──

    @Test
    @DisplayName("Given a VariableNode, when getting subtype, then returns variable name")
    void getSubtype_returnsVariableName_always() {
        assertEquals("x", new VariableNode("x", false).getSubtype());
        assertEquals("myVar", new VariableNode("myVar", false).getSubtype());
    }

    // ── getDepth / count ──

    @Test
    @DisplayName("Given a VariableNode, when getting depth, then returns 1")
    void getDepth_returnsOne_always() {
        assertEquals(1, new VariableNode("x", false).getDepth());
    }

    @Test
    @DisplayName("Given a VariableNode, when counting nodes, then returns 1")
    void count_returnsOne_always() {
        assertEquals(1, new VariableNode("x", false).count());
    }

    // ── getChildrenNodes ──

    @Test
    @DisplayName("Given a VariableNode, when getting children, then returns empty array")
    void getChildrenNodes_returnsEmpty_always() {
        assertEquals(0, new VariableNode("x", false).getChildrenNodes().length);
    }

    // ── clone ──

    @Test
    @DisplayName("Given an initialized VariableNode, when cloned, then clone has same value and name")
    void clone_preservesValueAndName_whenCloned() {
        VariableNode original = new VariableNode("x", false);
        original.setVariable("x", 10.0);
        VariableNode cloned = (VariableNode) original.clone();
        assertNotSame(original, cloned);
        assertEquals(original.getValue(), cloned.getValue(), DELTA);
        assertEquals("x", cloned.getSubtype());
    }

    @Test
    @DisplayName("Given a cloned node, when original is modified, then clone is unaffected")
    void clone_isIndependent_whenOriginalModified() {
        VariableNode original = new VariableNode("x", false);
        original.setVariable("x", 10.0);
        VariableNode cloned = (VariableNode) original.clone();
        original.setVariable("x", 99.0);
        assertEquals(10.0, cloned.getValue(), DELTA);
    }

    // ── toString ──

    @Test
    @DisplayName("Given a VariableNode, when converting to string, then returns variable name")
    void toString_returnsVariableName_always() {
        assertEquals("x", new VariableNode("x", false).toString());
        assertEquals("level", new VariableNode("level", false).toString());
    }
}

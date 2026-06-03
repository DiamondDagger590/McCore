package com.diamonddagger590.mccore.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConstantNodeTest {

    private static final double DELTA = 1e-9;

    // ── Constructor (double) ──

    @Test
    @DisplayName("Given a numeric value, when creating ConstantNode, then getValue returns that value")
    void constructor_storesValue_whenCreatedWithDouble() {
        ConstantNode node = new ConstantNode(42.5);
        assertEquals(42.5, node.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given a numeric value, when creating ConstantNode, then name is null")
    void constructor_setsNameNull_whenCreatedWithDouble() {
        ConstantNode node = new ConstantNode(42.5);
        assertEquals("42.5", node.toString());
    }

    @Test
    @DisplayName("Given zero, when creating ConstantNode, then getValue returns zero")
    void constructor_storesZero_whenCreatedWithZero() {
        ConstantNode node = new ConstantNode(0.0);
        assertEquals(0.0, node.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given negative value, when creating ConstantNode, then getValue returns negative")
    void constructor_storesNegative_whenCreatedWithNegative() {
        ConstantNode node = new ConstantNode(-7.3);
        assertEquals(-7.3, node.getValue(), DELTA);
    }

    // ── Constructor (String) ──

    @Test
    @DisplayName("Given 'pi', when creating ConstantNode, then getValue returns Math.PI")
    void constructor_returnsPi_whenCreatedWithPiString() {
        ConstantNode node = new ConstantNode("pi");
        assertEquals(Math.PI, node.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given 'e', when creating ConstantNode, then getValue returns Math.E")
    void constructor_returnsE_whenCreatedWithEString() {
        ConstantNode node = new ConstantNode("e");
        assertEquals(Math.E, node.getValue(), DELTA);
    }

    @Test
    @DisplayName("Given unrecognized constant name, when creating ConstantNode, then throws IllegalArgumentException")
    void constructor_throwsException_whenConstantNameUnrecognized() {
        assertThrows(IllegalArgumentException.class, () -> new ConstantNode("tau"));
    }

    // ── Constructor (int position) ──

    @Test
    @DisplayName("Given position 0, when creating ConstantNode, then represents pi")
    void constructor_representsPi_whenPositionIsZero() {
        ConstantNode node = new ConstantNode(0);
        assertEquals(Math.PI, node.getValue(), DELTA);
        assertEquals("pi", node.toString());
    }

    @Test
    @DisplayName("Given position 1, when creating ConstantNode, then represents e")
    void constructor_representsE_whenPositionIsOne() {
        ConstantNode node = new ConstantNode(1);
        assertEquals(Math.E, node.getValue(), DELTA);
        assertEquals("e", node.toString());
    }

    // ── getType ──

    @Test
    @DisplayName("Given any ConstantNode, when getting type, then returns CONSTANT_NODE")
    void getType_returnsConstantNode_always() {
        assertEquals(ExpressionNode.CONSTANT_NODE, new ConstantNode(5.0).getType());
        assertEquals(ExpressionNode.CONSTANT_NODE, new ConstantNode("pi").getType());
    }

    // ── getSubtype ──

    @Test
    @DisplayName("Given integer constant, when getting subtype, then returns integer string")
    void getSubtype_returnsIntegerString_whenValueIsWholeNumber() {
        assertEquals("5", new ConstantNode(5.0).getSubtype());
    }

    @Test
    @DisplayName("Given fractional constant, when getting subtype, then returns double string")
    void getSubtype_returnsDoubleString_whenValueIsFractional() {
        assertEquals("5.5", new ConstantNode(5.5).getSubtype());
    }

    @Test
    @DisplayName("Given zero, when getting subtype, then returns '0'")
    void getSubtype_returnsZeroString_whenValueIsZero() {
        assertEquals("0", new ConstantNode(0.0).getSubtype());
    }

    // ── getDepth / count ──

    @Test
    @DisplayName("Given a ConstantNode, when getting depth, then returns 1")
    void getDepth_returnsOne_always() {
        assertEquals(1, new ConstantNode(5.0).getDepth());
    }

    @Test
    @DisplayName("Given a ConstantNode, when counting nodes, then returns 1")
    void count_returnsOne_always() {
        assertEquals(1, new ConstantNode(5.0).count());
    }

    // ── getChildrenNodes ──

    @Test
    @DisplayName("Given a ConstantNode, when getting children, then returns empty array")
    void getChildrenNodes_returnsEmpty_always() {
        ExpressionNode[] children = new ConstantNode(5.0).getChildrenNodes();
        assertEquals(0, children.length);
    }

    // ── setVariable ──

    @Test
    @DisplayName("Given a ConstantNode, when setting a variable, then value is unchanged")
    void setVariable_doesNothing_always() {
        ConstantNode node = new ConstantNode(5.0);
        node.setVariable("x", 10.0);
        assertEquals(5.0, node.getValue(), DELTA);
    }

    // ── clone ──

    @Test
    @DisplayName("Given a ConstantNode, when cloned, then returns a different instance with same value")
    void clone_returnsSameValue_butDifferentInstance() {
        ConstantNode original = new ConstantNode(42.0);
        ConstantNode cloned = (ConstantNode) original.clone();
        assertNotSame(original, cloned);
        assertEquals(original.getValue(), cloned.getValue(), DELTA);
    }

    // ── toString ──

    @Test
    @DisplayName("Given named constant, when converting to string, then returns name")
    void toString_returnsName_whenNamedConstant() {
        assertEquals("pi", new ConstantNode("pi").toString());
        assertEquals("e", new ConstantNode("e").toString());
    }

    @Test
    @DisplayName("Given unnamed constant, when converting to string, then returns numeric string")
    void toString_returnsNumericString_whenUnnamedConstant() {
        assertEquals("10", new ConstantNode(10.0).toString());
        assertEquals("3.14", new ConstantNode(3.14).toString());
    }
}

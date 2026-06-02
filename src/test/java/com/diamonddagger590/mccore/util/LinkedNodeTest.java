package com.diamonddagger590.mccore.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkedNodeTest {

    @Test
    @DisplayName("Given a single-arg constructor, when getting value, then returns the stored value")
    void getNodeValue_returnsStoredValue_whenSingleArgConstructorUsed() {
        LinkedNode<String> node = new LinkedNode<>("hello");
        assertEquals("hello", node.getNodeValue());
    }

    @Test
    @DisplayName("Given a single node with no next, when checking hasNext, then returns false")
    void hasNext_returnsFalse_whenNodeHasNoNext() {
        LinkedNode<Integer> node = new LinkedNode<>(42);
        assertFalse(node.hasNext());
    }

    @Test
    @DisplayName("Given a single node with no next, when getting next node, then throws NullPointerException")
    void getNextNode_throwsNullPointerException_whenNoNextNodeExists() {
        LinkedNode<String> node = new LinkedNode<>("only");
        assertThrows(NullPointerException.class, node::getNextNode);
    }

    @Test
    @DisplayName("Given two-arg constructor with next node, when getting next, then returns the linked node")
    void getNextNode_returnsLinkedNode_whenTwoArgConstructorUsed() {
        LinkedNode<String> second = new LinkedNode<>("second");
        LinkedNode<String> first = new LinkedNode<>("first", second);

        assertTrue(first.hasNext());
        assertEquals(second, first.getNextNode());
        assertEquals("second", first.getNextNode().getNodeValue());
    }

    @Test
    @DisplayName("Given a node with no next, when calling setNext, then hasNext returns true and getNextNode returns the set node")
    void setNext_linksNodes_whenCalledOnUnlinkedNode() {
        LinkedNode<Integer> first = new LinkedNode<>(1);
        LinkedNode<Integer> second = new LinkedNode<>(2);

        assertFalse(first.hasNext());
        first.setNext(second);
        assertTrue(first.hasNext());
        assertEquals(second, first.getNextNode());
    }

    @Test
    @DisplayName("Given a chain of three nodes, when traversing, then visits all nodes in order")
    void getNextNode_traversesChain_whenThreeNodesLinked() {
        LinkedNode<String> third = new LinkedNode<>("c");
        LinkedNode<String> second = new LinkedNode<>("b", third);
        LinkedNode<String> first = new LinkedNode<>("a", second);

        assertEquals("a", first.getNodeValue());
        assertEquals("b", first.getNextNode().getNodeValue());
        assertEquals("c", first.getNextNode().getNextNode().getNodeValue());
        assertFalse(first.getNextNode().getNextNode().hasNext());
    }

    @Test
    @DisplayName("Given a node with an existing next, when setNext is called with a different node, then overrides the previous link")
    void setNext_overridesPreviousLink_whenCalledAgain() {
        LinkedNode<Integer> first = new LinkedNode<>(1);
        LinkedNode<Integer> oldNext = new LinkedNode<>(2);
        LinkedNode<Integer> newNext = new LinkedNode<>(3);

        first.setNext(oldNext);
        assertEquals(2, first.getNextNode().getNodeValue());

        first.setNext(newNext);
        assertEquals(3, first.getNextNode().getNodeValue());
    }

    @Test
    @DisplayName("Given nodes of different generic types, when getting values, then returns correct typed values")
    void getNodeValue_returnsCorrectType_whenDifferentGenericTypesUsed() {
        LinkedNode<Double> doubleNode = new LinkedNode<>(3.14);
        assertEquals(3.14, doubleNode.getNodeValue());

        LinkedNode<Boolean> boolNode = new LinkedNode<>(true);
        assertEquals(true, boolNode.getNodeValue());
    }

    @Test
    @DisplayName("Given a linked chain of three nodes, when summing all values via traversal, then returns correct total")
    void traversal_sumsAllValues_whenChainIsTraversed() {
        LinkedNode<Integer> node3 = new LinkedNode<>(30);
        LinkedNode<Integer> node2 = new LinkedNode<>(20, node3);
        LinkedNode<Integer> node1 = new LinkedNode<>(10, node2);

        int sum = 0;
        LinkedNode<Integer> current = node1;
        sum += current.getNodeValue();
        while (current.hasNext()) {
            current = current.getNextNode();
            sum += current.getNodeValue();
        }
        assertEquals(60, sum);
    }

    @Test
    @DisplayName("Given a node with no next, when NullPointerException is thrown, then message contains the node value")
    void getNextNode_includesNodeValueInMessage_whenExceptionThrown() {
        LinkedNode<String> node = new LinkedNode<>("myValue");
        NullPointerException exception = assertThrows(NullPointerException.class, node::getNextNode);
        assertTrue(exception.getMessage().contains("myValue"));
    }
}

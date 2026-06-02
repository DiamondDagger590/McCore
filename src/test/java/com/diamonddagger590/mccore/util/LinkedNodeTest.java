package com.diamonddagger590.mccore.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkedNodeTest {

    @Test
    void singleNodeHoldsValue() {
        LinkedNode<String> node = new LinkedNode<>("hello");
        assertEquals("hello", node.getNodeValue());
    }

    @Test
    void singleNodeHasNoNext() {
        LinkedNode<Integer> node = new LinkedNode<>(42);
        assertFalse(node.hasNext());
    }

    @Test
    void getNextNodeOnSingleNodeThrowsNullPointerException() {
        LinkedNode<String> node = new LinkedNode<>("only");
        assertThrows(NullPointerException.class, node::getNextNode);
    }

    @Test
    void constructorWithNextNodeLinksCorrectly() {
        LinkedNode<String> second = new LinkedNode<>("second");
        LinkedNode<String> first = new LinkedNode<>("first", second);

        assertTrue(first.hasNext());
        assertEquals(second, first.getNextNode());
        assertEquals("second", first.getNextNode().getNodeValue());
    }

    @Test
    void setNextLinksNodes() {
        LinkedNode<Integer> first = new LinkedNode<>(1);
        LinkedNode<Integer> second = new LinkedNode<>(2);

        assertFalse(first.hasNext());
        first.setNext(second);
        assertTrue(first.hasNext());
        assertEquals(second, first.getNextNode());
    }

    @Test
    void chainOfThreeNodes() {
        LinkedNode<String> third = new LinkedNode<>("c");
        LinkedNode<String> second = new LinkedNode<>("b", third);
        LinkedNode<String> first = new LinkedNode<>("a", second);

        assertEquals("a", first.getNodeValue());
        assertEquals("b", first.getNextNode().getNodeValue());
        assertEquals("c", first.getNextNode().getNextNode().getNodeValue());
        assertFalse(first.getNextNode().getNextNode().hasNext());
    }

    @Test
    void setNextOverridesPreviousLink() {
        LinkedNode<Integer> first = new LinkedNode<>(1);
        LinkedNode<Integer> oldNext = new LinkedNode<>(2);
        LinkedNode<Integer> newNext = new LinkedNode<>(3);

        first.setNext(oldNext);
        assertEquals(2, first.getNextNode().getNodeValue());

        first.setNext(newNext);
        assertEquals(3, first.getNextNode().getNodeValue());
    }

    @Test
    void nodeWithDifferentTypes() {
        LinkedNode<Double> node = new LinkedNode<>(3.14);
        assertEquals(3.14, node.getNodeValue());

        LinkedNode<Boolean> boolNode = new LinkedNode<>(true);
        assertEquals(true, boolNode.getNodeValue());
    }

    @Test
    void traverseLinkedChain() {
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
    void nullPointerExceptionMessageContainsNodeValue() {
        LinkedNode<String> node = new LinkedNode<>("myValue");
        NullPointerException exception = assertThrows(NullPointerException.class, node::getNextNode);
        assertTrue(exception.getMessage().contains("myValue"));
    }
}

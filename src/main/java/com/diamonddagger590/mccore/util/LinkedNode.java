package com.diamonddagger590.mccore.util;

import org.jetbrains.annotations.NotNull;

/**
 * A linked node allows for linking data together in a sequential manner.
 *
 * @param <E> The data type stored inside this node.
 */
public class LinkedNode<E> {

    private final E nodeValue;
    private LinkedNode<E> nextNode;

    public LinkedNode(@NotNull E nodeValue) {
        this.nodeValue = nodeValue;
    }

    public LinkedNode(@NotNull E nodeValue, @NotNull LinkedNode<E> nextNode) {
        this.nodeValue = nodeValue;
        this.nextNode = nextNode;
    }

    /**
     * Gets the value stored in this node.
     *
     * @return The value stored in this node.
     */
    @NotNull
    public E getNodeValue() {
        return nodeValue;
    }

    /**
     * Sets the node that is the next in the linked chain.
     *
     * @param nextNode The node that is next in the linked chain.
     */
    public void setNext(@NotNull LinkedNode<E> nextNode) {
        this.nextNode = nextNode;
    }

    /**
     * Checks to see if there is a next node in the linked chain.
     *
     * @return {@code true} if there is a next node in the linked chain.
     */
    public boolean hasNext() {
        return nextNode != null;
    }

    /**
     * Gets the next node in the linked chain.
     *
     * @return The next node in the linked chain.
     * @throws NullPointerException if there is not a next node in the linked chain.
     */
    @NotNull
    public LinkedNode<E> getNextNode() {
        if (nextNode == null) {
            throw new NullPointerException(String.format("A next node was not for a LinkedNode with the following contents: %s", nodeValue));
        }
        return nextNode;
    }
}

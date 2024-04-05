package com.diamonddagger590.mccore.util;

import org.jetbrains.annotations.NotNull;

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

    @NotNull
    public E getNodeValue() {
        return nodeValue;
    }

    public void setNext(@NotNull LinkedNode<E> nextNode) {
        this.nextNode = nextNode;
    }

    @NotNull
    public LinkedNode<E> getNextNode() {
        if (nextNode == null) {
            throw new NullPointerException(String.format("A next node was not for a LinkedNode with the following contents: %s", nodeValue));
        }
        return nextNode;
    }
}

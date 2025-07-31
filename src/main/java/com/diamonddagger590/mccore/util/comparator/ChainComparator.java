package com.diamonddagger590.mccore.util.comparator;


import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

/**
 * https://medium.com/@lonell.liburd/chaining-comparators-and-sorting-in-java-498b8e1e34a8
 * @param <E>
 */
public class ChainComparator<E> implements Comparator<E> {

    private List<Comparator<E>> comparatorList;

    @SafeVarargs
    public ChainComparator(@NotNull Comparator<E>... comparators) {
        this.comparatorList = List.of(comparators);
    }

    @Override
    public int compare(E p1, E p2) {
        int result;
        for(Comparator<E> comparator : comparatorList) {
            if ((result = comparator.compare(p1, p2)) != 0) {
                return result;
            }
        }
        return 0;
    }
}

package com.diamonddagger590.mccore.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class ChainFilter<E> implements Filter<E> {

    private final List<Filter<E>> filterList;

    @SafeVarargs
    public ChainFilter(@NotNull Filter<E>... filters) {
        this.filterList = List.of(filters);
    }

    @Override
    public Collection<E> filter(Collection<E> list) {
        for (Filter<E> filter : filterList) {
            list = filter.filter(list);
        }
        return list;
    }
}

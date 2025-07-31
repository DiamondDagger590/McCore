package com.diamonddagger590.mccore.util.filter;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * An chain filter allows for multiple {@link Filter}s to feed their
 * outputs as the input into the next filter.
 * <p>
 * Doing this allows for multiple standalone filters to be created
 * and then combined into one singular filter to modify a collection.
 *
 * @param <E> The type of object being filtered.
 */
public class ChainFilter<E> implements Filter<E> {

    private final List<Filter<E>> filterList;

    @SafeVarargs
    public ChainFilter(@NotNull Filter<E>... filters) {
        this.filterList = List.of(filters);
    }

    @NotNull
    @Override
    public Collection<E> filter(@NotNull Collection<E> list) {
        for (Filter<E> filter : filterList) {
            list = filter.filter(list);
        }
        return list;
    }
}

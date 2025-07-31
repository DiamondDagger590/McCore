package com.diamonddagger590.mccore.util.filter;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A filter will filter out specific results from a provided collection based
 * on varying criteria.
 *
 * @param <E> The type of data being filtered.
 */
public interface Filter<E> {

    /**
     * Filters the provided {@link Collection} and returns a modified collection
     * that's filtered based on the criteria this filter represents.
     *
     * @param collection The collection to filter.
     * @return A filtered {@link Collection}.
     */
    @NotNull
    Collection<E> filter(@NotNull Collection<E> collection);
}

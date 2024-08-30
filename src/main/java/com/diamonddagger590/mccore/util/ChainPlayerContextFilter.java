package com.diamonddagger590.mccore.util;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * This filter allows for chaining multiple {@link PlayerContextFilter}s together to have their results
 * feed into each other into one combined filtered output.
 * @param <E> The object type being filtered.
 */
public class ChainPlayerContextFilter<E> implements PlayerContextFilter<E> {

    private final List<PlayerContextFilter<E>> filterList;

    @SafeVarargs
    public ChainPlayerContextFilter(@NotNull PlayerContextFilter<E>... filters) {
        this.filterList = List.of(filters);
    }

    @NotNull
    @Override
    public Collection<E> filter(@NotNull CorePlayer corePlayer, @NotNull Collection<E> list) {
        for (PlayerContextFilter<E> filter : filterList) {
            list = filter.filter(corePlayer, list);
        }
        return list;
    }
}

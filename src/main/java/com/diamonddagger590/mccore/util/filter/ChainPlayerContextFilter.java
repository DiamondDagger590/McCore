package com.diamonddagger590.mccore.util.filter;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * This filter allows for chaining multiple {@link PlayerContextFilter}s together to have their results
 * feed into each other into one combined filtered output.
 * @param <E> The object type being filtered.
 */
public class ChainPlayerContextFilter<E, P extends CorePlayer> implements PlayerContextFilter<E, P> {

    private final List<PlayerContextFilter<E, P>> filterList;

    @SafeVarargs
    public ChainPlayerContextFilter(@NotNull PlayerContextFilter<E, P>... filters) {
        this.filterList = List.of(filters);
    }

    @NotNull
    @Override
    public Collection<E> filter(@NotNull P corePlayer, @NotNull Collection<E> list) {
        for (PlayerContextFilter<E, P> filter : filterList) {
            list = filter.filter(corePlayer, list);
        }
        return list;
    }
}

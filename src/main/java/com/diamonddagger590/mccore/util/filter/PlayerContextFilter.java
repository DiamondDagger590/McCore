package com.diamonddagger590.mccore.util.filter;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A filter that contains a context of a {@link CorePlayer} to be used when filtering a collection.
 *
 * @param <E> The object type being filtered.
 */
public interface PlayerContextFilter<E> {

    /**
     * Filters the provided collection based on some related context from the provided {@link CorePlayer}.
     *
     * @param corePlayer The {@link CorePlayer} to use as a reference when filtering.
     * @param list       The collection to filter.
     * @return A filtered {@link Collection}.
     */
    @NotNull
    public Collection<E> filter(@NotNull CorePlayer corePlayer, @NotNull Collection<E> list);
}

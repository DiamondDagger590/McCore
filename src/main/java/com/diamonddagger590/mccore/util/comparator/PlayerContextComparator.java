package com.diamonddagger590.mccore.util.comparator;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

/**
 * A player context comparator allows for generating a comparator on a specific data
 * type for a specific player. By wrapping it in this way, it allows for the "player context"
 * to be passed into the comparator which is useful for things like comparing names in the player's
 * locale.
 *
 * @param <E> The type of object being compared.
 * @param <P> The {@link CorePlayer} type that's being passed in as context.
 */
public interface PlayerContextComparator<E, P extends CorePlayer> {

    /**
     * Gets a {@link Comparator} comparing {@link E} for the provided
     * {@link P} player.
     *
     * @param corePlayer The player to get a comparator for.
     * @return A {@link Comparator} comparing {@link E}.
     */
    @NotNull
    Comparator<E> getComparator(@NotNull P corePlayer);
}

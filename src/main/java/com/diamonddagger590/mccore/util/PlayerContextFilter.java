package com.diamonddagger590.mccore.util;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface PlayerContextFilter<E> {

    public Collection<E> filter(@NotNull CorePlayer corePlayer, @NotNull Collection<E> list);
}

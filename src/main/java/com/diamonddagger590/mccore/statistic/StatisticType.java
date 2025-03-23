package com.diamonddagger590.mccore.statistic;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class StatisticType {

    @NotNull
    public abstract NamespacedKey getStatisticKey();

    @NotNull
    public abstract Statistic getStatistic(@NotNull UUID uuid, float value);
}

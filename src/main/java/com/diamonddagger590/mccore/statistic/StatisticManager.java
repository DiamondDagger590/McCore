package com.diamonddagger590.mccore.statistic;

import com.diamonddagger590.mccore.CorePlugin;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StatisticManager {

    private final CorePlugin plugin;
    private final Map<NamespacedKey, StatisticType> statisticTypes;

    public StatisticManager(@NotNull CorePlugin plugin) {
        this.plugin = plugin;
        this.statisticTypes = new HashMap<>();
    }

    public void registerStatisticType(@NotNull StatisticType statisticType) {
        statisticTypes.put(statisticType.getStatisticKey(), statisticType);
    }

    public boolean isStatisticTypeRegistered(@NotNull NamespacedKey key) {
        return statisticTypes.containsKey(key);
    }

    public void unregisterStatisticType(@NotNull NamespacedKey key) {
        statisticTypes.remove(key);
    }

    @NotNull
    public Optional<StatisticType> getStatisticType(@NotNull NamespacedKey key) {
        return Optional.ofNullable(statisticTypes.get(key));
    }
}

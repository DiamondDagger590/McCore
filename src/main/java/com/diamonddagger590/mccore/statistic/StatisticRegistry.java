package com.diamonddagger590.mccore.statistic;

import com.diamonddagger590.mccore.registry.Registry;
import com.google.common.collect.ImmutableSet;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A registry for all {@link Statistic} definitions. Statistics are registered once during
 * plugin bootstrap and are not unregistered during runtime.
 */
public final class StatisticRegistry implements Registry<Statistic> {

    private final Map<NamespacedKey, Statistic> statistics;

    public StatisticRegistry() {
        this.statistics = new HashMap<>();
    }

    @Override
    public void register(@NotNull Statistic statistic) {
        if (statistics.containsKey(statistic.getStatisticKey())) {
            throw new IllegalArgumentException(
                    "Statistic already registered: " + statistic.getStatisticKey()
            );
        }
        statistics.put(statistic.getStatisticKey(), statistic);
    }

    @Override
    public boolean registered(@NotNull Statistic statistic) {
        return statistics.containsKey(statistic.getStatisticKey());
    }

    /**
     * Gets a {@link Statistic} by its {@link NamespacedKey}.
     *
     * @param key The key to look up.
     * @return An {@link Optional} containing the statistic, or empty if not registered.
     */
    @NotNull
    public Optional<Statistic> getStatistic(@NotNull NamespacedKey key) {
        return Optional.ofNullable(statistics.get(key));
    }

    /**
     * Gets all registered {@link Statistic} definitions.
     *
     * @return An {@link ImmutableSet} of all registered statistics.
     */
    @NotNull
    public Set<Statistic> getRegisteredStatistics() {
        return ImmutableSet.copyOf(statistics.values());
    }

    /**
     * Gets all registered {@link NamespacedKey}s.
     *
     * @return An {@link ImmutableSet} of all registered statistic keys.
     */
    @NotNull
    public Set<NamespacedKey> getRegisteredStatisticKeys() {
        return ImmutableSet.copyOf(statistics.keySet());
    }
}

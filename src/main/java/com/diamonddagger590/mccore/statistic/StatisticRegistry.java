package com.diamonddagger590.mccore.statistic;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link Registry} that holds all registered {@link Statistic} instances,
 * keyed by their {@link NamespacedKey}.
 */
public class StatisticRegistry implements Registry<Statistic> {

    private final Map<NamespacedKey, Statistic> statistics = new LinkedHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(@NotNull Statistic statistic) {
        statistics.put(statistic.getStatisticKey(), statistic);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean registered(@NotNull Statistic statistic) {
        return statistics.containsKey(statistic.getStatisticKey());
    }

    /**
     * Gets a {@link Statistic} by its {@link NamespacedKey}.
     *
     * @param key The key to look up.
     * @return An {@link Optional} containing the statistic, or empty if not found.
     */
    @NotNull
    public Optional<Statistic> getStatistic(@NotNull NamespacedKey key) {
        return Optional.ofNullable(statistics.get(key));
    }

    /**
     * Gets all registered statistics.
     *
     * @return An unmodifiable collection of all registered statistics.
     */
    @NotNull
    public Collection<Statistic> getRegisteredStatistics() {
        return Collections.unmodifiableCollection(statistics.values());
    }
}

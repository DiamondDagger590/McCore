package com.diamonddagger590.mccore.exception.statistic;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when a {@link com.diamonddagger590.mccore.statistic.Statistic} is expected
 * to be registered but is not found in the {@link com.diamonddagger590.mccore.statistic.StatisticRegistry}.
 */
public class StatisticNotRegisteredException extends RuntimeException {

    private final NamespacedKey statisticKey;

    public StatisticNotRegisteredException(@NotNull NamespacedKey statisticKey) {
        this.statisticKey = statisticKey;
    }

    /**
     * Gets the {@link NamespacedKey} of the unregistered statistic.
     *
     * @return The {@link NamespacedKey} that was not found.
     */
    @NotNull
    public NamespacedKey getStatisticKey() {
        return statisticKey;
    }

    @Override
    public String getMessage() {
        return "A statistic with key " + statisticKey + " was not found in the StatisticRegistry.";
    }
}

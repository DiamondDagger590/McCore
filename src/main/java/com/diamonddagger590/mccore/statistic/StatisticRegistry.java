package com.diamonddagger590.mccore.statistic;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.diamonddagger590.mccore.statistic.StatisticType.*;

/**
 * A registry for all {@link Statistic} definitions. Statistics are registered once during
 * plugin bootstrap and are not unregistered during runtime.
 */
public final class StatisticRegistry implements Registry<Statistic> {

    private final Map<NamespacedKey, Statistic> statistics;

    public StatisticRegistry() {
        this.statistics = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if a statistic with the same {@link NamespacedKey} is already registered.
     */
    @Override
    public void register(@NotNull Statistic statistic) {
        if (statistics.containsKey(statistic.getStatisticKey())) {
            throw new IllegalArgumentException(
                    "Statistic already registered: " + statistic.getStatisticKey()
            );
        }
        validateDefaultValue(statistic);
        statistics.put(statistic.getStatisticKey(), statistic);
    }

    /**
     * Validates that a {@link Statistic}'s default value is type-compatible with its
     * {@link StatisticType}. This catches mismatches at registration time rather than
     * at runtime when the value is first used.
     *
     * @param statistic The statistic to validate.
     * @throws IllegalArgumentException if the default value is not compatible with the statistic type.
     */
    private void validateDefaultValue(@NotNull Statistic statistic) {
        Object defaultValue = statistic.getDefaultValue();
        StatisticType type = statistic.getStatisticType();
        boolean valid = switch (type) {
            case INT -> defaultValue instanceof Integer;
            case LONG -> defaultValue instanceof Long;
            case DOUBLE -> defaultValue instanceof Double;
            case STRING -> defaultValue instanceof String;
            case TIMESTAMP -> defaultValue instanceof Instant;
            case SET_STRING -> defaultValue instanceof Set<?> s
                    && (s.isEmpty() || s.stream().allMatch(e -> e instanceof String));
        };
        if (!valid) {
            throw new IllegalArgumentException(
                    "Statistic '" + statistic.getStatisticKey() + "' has default value of type "
                    + defaultValue.getClass().getSimpleName() + " which is incompatible with StatisticType." + type
            );
        }
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
     * @return An unmodifiable {@link Set} of all registered statistics.
     */
    @NotNull
    public Set<Statistic> getRegisteredStatistics() {
        return Set.copyOf(statistics.values());
    }

    /**
     * Gets all registered {@link NamespacedKey}s.
     *
     * @return An unmodifiable {@link Set} of all registered statistic keys.
     */
    @NotNull
    public Set<NamespacedKey> getRegisteredStatisticKeys() {
        return Set.copyOf(statistics.keySet());
    }
}

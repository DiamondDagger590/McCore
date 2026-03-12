package com.diamonddagger590.mccore.event.statistic;

import com.diamonddagger590.mccore.event.player.CorePlayerEvent;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.statistic.Statistic;
import com.diamonddagger590.mccore.statistic.StatisticType;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Set;

/**
 * Fired before a statistic value is modified. Cancellable — listeners can prevent
 * the change or adjust the new value via {@link #setNewValue(Object)}.
 */
public class StatisticModifyEvent extends CorePlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final NamespacedKey statisticKey;
    private final Statistic statistic;
    private final Object oldValue;
    private Object newValue;
    private final ModificationType modificationType;
    private boolean cancelled;

    public StatisticModifyEvent(
            @NotNull CorePlayer corePlayer,
            @NotNull NamespacedKey statisticKey,
            @NotNull Statistic statistic,
            @NotNull Object oldValue,
            @NotNull Object newValue,
            @NotNull ModificationType modificationType
    ) {
        super(corePlayer);
        this.statisticKey = statisticKey;
        this.statistic = statistic;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.modificationType = modificationType;
        this.cancelled = false;
    }

    /**
     * Gets the {@link NamespacedKey} identifying the statistic being modified.
     *
     * @return The statistic key.
     */
    @NotNull
    public NamespacedKey getStatisticKey() {
        return statisticKey;
    }

    /**
     * Gets the {@link Statistic} definition being modified.
     *
     * @return The statistic definition.
     */
    @NotNull
    public Statistic getStatistic() {
        return statistic;
    }

    /**
     * Gets the value of the statistic before the proposed modification.
     *
     * @return The current value.
     */
    @NotNull
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * Gets the proposed new value. This may differ from the original proposed
     * value if a listener called {@link #setNewValue(Object)}.
     *
     * @return The proposed new value.
     */
    @NotNull
    public Object getNewValue() {
        return newValue;
    }

    /**
     * Adjusts the new value that will be applied. Listeners can use this to
     * apply multipliers or clamp values.
     * <p>
     * The provided value must be type-compatible with the statistic's
     * {@link StatisticType}. Passing an incompatible type will throw an
     * {@link IllegalArgumentException}.
     *
     * @param newValue The adjusted value.
     * @throws IllegalArgumentException if the value type is incompatible with the statistic's type.
     */
    public void setNewValue(@NotNull Object newValue) {
        validateValueType(newValue, statistic.getStatisticType());
        this.newValue = newValue;
    }

    /**
     * Gets the type of modification being performed.
     *
     * @return The {@link ModificationType}.
     */
    @NotNull
    public ModificationType getModificationType() {
        return modificationType;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Validates that the given value is type-compatible with the specified {@link StatisticType}.
     *
     * @param value The value to validate.
     * @param type  The expected statistic type.
     * @throws IllegalArgumentException if the value type does not match.
     */
    private static void validateValueType(@NotNull Object value, @NotNull StatisticType type) {
        boolean valid = switch (type) {
            case INT -> value instanceof Integer;
            case LONG -> value instanceof Long;
            case DOUBLE -> value instanceof Double;
            case STRING -> value instanceof String;
            case TIMESTAMP -> value instanceof Instant;
            case SET_STRING -> value instanceof Set<?>;
        };
        if (!valid) {
            throw new IllegalArgumentException(
                    "Expected value compatible with " + type + " but got " + value.getClass().getSimpleName()
            );
        }
    }
}

package com.diamonddagger590.mccore.event.statistic;

import com.diamonddagger590.mccore.event.player.CorePlayerEvent;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.statistic.Statistic;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired after a statistic value has been successfully modified. Not cancellable.
 * Used by reactive systems (e.g., future achievements) to check thresholds.
 * <p>
 * This event only fires if the pre-event ({@link StatisticModifyEvent}) was not cancelled
 * and the value actually changed.
 */
public class PostStatisticModifyEvent extends CorePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    private final NamespacedKey statisticKey;
    private final Statistic statistic;
    private final Object oldValue;
    private final Object newValue;
    private final ModificationType modificationType;

    public PostStatisticModifyEvent(
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
    }

    @NotNull
    public NamespacedKey getStatisticKey() {
        return statisticKey;
    }

    @NotNull
    public Statistic getStatistic() {
        return statistic;
    }

    @NotNull
    public Object getOldValue() {
        return oldValue;
    }

    @NotNull
    public Object getNewValue() {
        return newValue;
    }

    @NotNull
    public ModificationType getModificationType() {
        return modificationType;
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
}

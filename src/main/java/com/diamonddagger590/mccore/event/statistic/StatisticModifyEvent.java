package com.diamonddagger590.mccore.event.statistic;

import com.diamonddagger590.mccore.event.player.CorePlayerEvent;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.statistic.Statistic;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

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

    /**
     * Adjusts the new value that will be applied. Listeners can use this to
     * apply multipliers or clamp values.
     *
     * @param newValue The adjusted value.
     */
    public void setNewValue(@NotNull Object newValue) {
        this.newValue = newValue;
    }

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
}

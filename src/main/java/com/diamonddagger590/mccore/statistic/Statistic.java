package com.diamonddagger590.mccore.statistic;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class Statistic {

    private final StatisticType type;
    private final UUID uuid;
    private float value;

    public Statistic(@NotNull StatisticType statisticType, @NotNull UUID player, final float value) {
        this.type = statisticType;
        this.uuid = player;
        this.value = Math.max(0, value);
    }

    @NotNull
    public StatisticType getType() {
        return type;
    }

    @NotNull
    public UUID getPlayerUUID() {
        return uuid;
    }

    public float getValue() {
        return value;
    }

    public void setValue(final float value) {
        if (value != this.value) {
            this.value = Math.max(0, value);
        }
    }

    public void incrementValue(int value) {
        setValue(getValue() + Math.max(0, value));
    }

    public void decrementValue(int value) {
        setValue(getValue() - Math.min(0, value));
    }

    public void incrementValue(final float value) {
        this.value = Math.max(0, this.value + value);
    }
}

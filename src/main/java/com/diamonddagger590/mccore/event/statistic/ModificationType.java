package com.diamonddagger590.mccore.event.statistic;

/**
 * Describes the type of modification being made to a statistic.
 */
public enum ModificationType {

    /**
     * A direct value replacement via {@link com.diamonddagger590.mccore.statistic.PlayerStatisticData#setValue}.
     */
    SET,

    /**
     * A numeric addition (positive or negative) to the current value.
     */
    INCREMENT,

    /**
     * An element being added to a {@link com.diamonddagger590.mccore.statistic.StatisticType#SET_STRING} statistic.
     */
    ADD_TO_SET,

    /**
     * An element being removed from a {@link com.diamonddagger590.mccore.statistic.StatisticType#SET_STRING} statistic.
     */
    REMOVE_FROM_SET,

    /**
     * A conditional set that only applies if the new value is greater than the current value.
     */
    SET_MAX,

    /**
     * A conditional set that only applies if no value is currently stored.
     */
    SET_IF_ABSENT
}

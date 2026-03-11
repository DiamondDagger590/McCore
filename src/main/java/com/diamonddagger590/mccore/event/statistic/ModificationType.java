package com.diamonddagger590.mccore.event.statistic;

/**
 * Describes the type of modification being made to a statistic.
 */
public enum ModificationType {
    SET,
    INCREMENT,
    ADD_TO_SET,
    REMOVE_FROM_SET,
    SET_MAX,
    SET_IF_ABSENT
}

package com.diamonddagger590.mccore.statistic;

public enum StatisticType {
    INT,
    LONG,
    DOUBLE,
    STRING,
    TIMESTAMP,
    SET_STRING;

    /**
     * Returns {@code true} if this type is numeric ({@link #INT}, {@link #LONG}, or {@link #DOUBLE}).
     *
     * @return {@code true} if this type is numeric.
     */
    public boolean isNumeric() {
        return this == INT || this == LONG || this == DOUBLE;
    }
}

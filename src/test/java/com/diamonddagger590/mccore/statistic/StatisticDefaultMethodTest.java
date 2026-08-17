package com.diamonddagger590.mccore.statistic;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatisticDefaultMethodTest {

    @Test
    @DisplayName("Given a Statistic with default getMaxSetSize, when called, then returns -1 for unlimited")
    void getMaxSetSize_returnsNegativeOne_whenDefaultImplementation() {
        Statistic stat = new TestStatistic();
        assertEquals(-1, stat.getMaxSetSize());
    }

    @Test
    @DisplayName("Given a Statistic that overrides getMaxSetSize, when called, then returns the overridden value")
    void getMaxSetSize_returnsOverriddenValue_whenOverridden() {
        Statistic stat = new LimitedSetStatistic();
        assertEquals(10, stat.getMaxSetSize());
    }

    @SuppressWarnings("deprecation")
    private static class TestStatistic implements Statistic {

        @NotNull
        @Override
        public NamespacedKey getStatisticKey() {
            return new NamespacedKey("test", "default_stat");
        }

        @NotNull
        @Override
        public StatisticType getStatisticType() {
            return StatisticType.INT;
        }

        @NotNull
        @Override
        public Object getDefaultValue() {
            return 0;
        }

        @NotNull
        @Override
        public String getDisplayName() {
            return "Test Statistic";
        }

        @NotNull
        @Override
        public String getDescription() {
            return "A test statistic for default method coverage";
        }
    }

    @SuppressWarnings("deprecation")
    private static class LimitedSetStatistic implements Statistic {

        @NotNull
        @Override
        public NamespacedKey getStatisticKey() {
            return new NamespacedKey("test", "limited_stat");
        }

        @NotNull
        @Override
        public StatisticType getStatisticType() {
            return StatisticType.SET_STRING;
        }

        @NotNull
        @Override
        public Object getDefaultValue() {
            return java.util.Set.of();
        }

        @NotNull
        @Override
        public String getDisplayName() {
            return "Limited Set Statistic";
        }

        @NotNull
        @Override
        public String getDescription() {
            return "A test statistic with a max set size";
        }

        @Override
        public int getMaxSetSize() {
            return 10;
        }
    }
}

package com.diamonddagger590.mccore.statistic;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleStatisticTest {

    @SuppressWarnings("deprecation")
    private static NamespacedKey key(String namespace, String key) {
        return new NamespacedKey(namespace, key);
    }

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("Given all fields, when using full constructor, then all accessors return provided values")
        void fullConstructor_populatesAllFields() {
            NamespacedKey key = key("test", "kills");
            SimpleStatistic stat = new SimpleStatistic(key, StatisticType.INT, 0, "Kills", "Total kills", 10);
            assertEquals(key, stat.getStatisticKey());
            assertEquals(StatisticType.INT, stat.getStatisticType());
            assertEquals(0, stat.getDefaultValue());
            assertEquals("Kills", stat.getDisplayName());
            assertEquals("Total kills", stat.getDescription());
            assertEquals(10, stat.getMaxSetSize());
        }

        @Test
        @DisplayName("Given convenience constructor, when called, then maxSetSize defaults to -1")
        void convenienceConstructor_defaultsMaxSetSizeToNegativeOne() {
            NamespacedKey key = key("test", "deaths");
            SimpleStatistic stat = new SimpleStatistic(key, StatisticType.LONG, 0L, "Deaths", "Total deaths");
            assertEquals(-1, stat.getMaxSetSize());
        }

        @Test
        @DisplayName("Given a DOUBLE statistic, when accessing fields, then returns correct type and value")
        void constructorWithDoubleType_returnsCorrectFields() {
            NamespacedKey key = key("test", "accuracy");
            SimpleStatistic stat = new SimpleStatistic(key, StatisticType.DOUBLE, 0.0, "Accuracy", "Hit accuracy");
            assertEquals(StatisticType.DOUBLE, stat.getStatisticType());
            assertEquals(0.0, stat.getDefaultValue());
        }

        @Test
        @DisplayName("Given a STRING statistic, when accessing fields, then returns correct type and value")
        void constructorWithStringType_returnsCorrectFields() {
            NamespacedKey key = key("test", "title");
            SimpleStatistic stat = new SimpleStatistic(key, StatisticType.STRING, "Novice", "Title", "Player title");
            assertEquals(StatisticType.STRING, stat.getStatisticType());
            assertEquals("Novice", stat.getDefaultValue());
        }

        @Test
        @DisplayName("Given a TIMESTAMP statistic, when accessing fields, then returns correct type and value")
        void constructorWithTimestampType_returnsCorrectFields() {
            NamespacedKey key = key("test", "last_login");
            Instant now = Instant.ofEpochSecond(1_000_000);
            SimpleStatistic stat = new SimpleStatistic(key, StatisticType.TIMESTAMP, now, "Last Login", "Last login time");
            assertEquals(StatisticType.TIMESTAMP, stat.getStatisticType());
            assertEquals(now, stat.getDefaultValue());
        }

        @Test
        @DisplayName("Given a SET_STRING statistic with maxSetSize, when accessing fields, then returns correct values")
        void constructorWithSetStringType_returnsCorrectFields() {
            NamespacedKey key = key("test", "titles");
            Set<String> defaults = Set.of("Novice");
            SimpleStatistic stat = new SimpleStatistic(key, StatisticType.SET_STRING, defaults, "Titles", "Earned titles", 50);
            assertEquals(StatisticType.SET_STRING, stat.getStatisticType());
            assertEquals(defaults, stat.getDefaultValue());
            assertEquals(50, stat.getMaxSetSize());
        }
    }

    @Nested
    @DisplayName("Equals and hashCode tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Given two statistics with same fields, when compared, then they are equal")
        void equals_returnsTrue_whenAllFieldsMatch() {
            NamespacedKey key = key("test", "xp");
            SimpleStatistic stat1 = new SimpleStatistic(key, StatisticType.DOUBLE, 0.0, "XP", "Experience");
            SimpleStatistic stat2 = new SimpleStatistic(key, StatisticType.DOUBLE, 0.0, "XP", "Experience");
            assertEquals(stat1, stat2);
            assertEquals(stat1.hashCode(), stat2.hashCode());
        }

        @Test
        @DisplayName("Given two statistics with different keys, when compared, then they are not equal")
        void equals_returnsFalse_whenKeysDiffer() {
            SimpleStatistic stat1 = new SimpleStatistic(key("test", "a"), StatisticType.INT, 0, "A", "Stat A");
            SimpleStatistic stat2 = new SimpleStatistic(key("test", "b"), StatisticType.INT, 0, "B", "Stat B");
            assertNotEquals(stat1, stat2);
        }

        @Test
        @DisplayName("Given a statistic compared to itself, when equals called, then returns true")
        void equals_returnsTrue_whenComparedToSelf() {
            SimpleStatistic stat = new SimpleStatistic(key("test", "xp"), StatisticType.INT, 0, "XP", "Experience");
            assertEquals(stat, stat);
        }

        @Test
        @DisplayName("Given a statistic compared to null, when equals called, then returns false")
        void equals_returnsFalse_whenComparedToNull() {
            SimpleStatistic stat = new SimpleStatistic(key("test", "xp"), StatisticType.INT, 0, "XP", "Experience");
            assertFalse(stat.equals(null));
        }

        @Test
        @DisplayName("Given a statistic compared to a non-SimpleStatistic, when equals called, then returns false")
        void equals_returnsFalse_whenComparedToDifferentType() {
            SimpleStatistic stat = new SimpleStatistic(key("test", "xp"), StatisticType.INT, 0, "XP", "Experience");
            assertFalse(stat.equals("not a statistic"));
        }

        @Test
        @DisplayName("Given two statistics with same key but different types, when compared, then they are not equal")
        void equals_returnsFalse_whenStatisticTypeDiffers() {
            NamespacedKey key = key("test", "value");
            SimpleStatistic stat1 = new SimpleStatistic(key, StatisticType.INT, 0, "Value", "A value");
            SimpleStatistic stat2 = new SimpleStatistic(key, StatisticType.LONG, 0L, "Value", "A value");
            assertNotEquals(stat1, stat2);
        }

        @Test
        @DisplayName("Given two statistics with same key but different maxSetSize, when compared, then they are not equal")
        void equals_returnsFalse_whenMaxSetSizeDiffers() {
            NamespacedKey key = key("test", "tags");
            SimpleStatistic stat1 = new SimpleStatistic(key, StatisticType.SET_STRING, Set.of(), "Tags", "Tags", 10);
            SimpleStatistic stat2 = new SimpleStatistic(key, StatisticType.SET_STRING, Set.of(), "Tags", "Tags", 20);
            assertNotEquals(stat1, stat2);
        }
    }

    @Nested
    @DisplayName("toString tests")
    class ToStringTests {

        @Test
        @DisplayName("Given a statistic, when toString is called, then it contains the key and type info")
        void toString_containsKeyAndTypeInfo() {
            NamespacedKey key = key("test", "kills");
            SimpleStatistic stat = new SimpleStatistic(key, StatisticType.INT, 0, "Kills", "Total kills");
            String str = stat.toString();
            assertTrue(str.contains("kills"));
            assertTrue(str.contains("INT"));
        }
    }

    @Nested
    @DisplayName("Record accessor tests")
    class RecordAccessorTests {

        @Test
        @DisplayName("Given a statistic, when record component accessors called, then return same as getter methods")
        void recordAccessors_matchGetterMethods() {
            NamespacedKey key = key("test", "kills");
            SimpleStatistic stat = new SimpleStatistic(key, StatisticType.INT, 0, "Kills", "Total kills", 5);
            assertEquals(stat.getStatisticKey(), stat.statisticKey());
            assertEquals(stat.getStatisticType(), stat.statisticType());
            assertEquals(stat.getDefaultValue(), stat.defaultValue());
            assertEquals(stat.getDisplayName(), stat.displayName());
            assertEquals(stat.getDescription(), stat.description());
            assertEquals(stat.getMaxSetSize(), stat.maxSetSize());
        }
    }

    @Nested
    @DisplayName("Statistic interface default method tests")
    class StatisticDefaultMethodTests {

        @Test
        @DisplayName("Given a Statistic implementation without getMaxSetSize override, when called, then returns -1")
        void getMaxSetSize_returnsNegativeOne_whenNotOverridden() {
            Statistic minimal = new Statistic() {
                @Override
                public @NotNull NamespacedKey getStatisticKey() {
                    return key("test", "minimal");
                }

                @Override
                public @NotNull StatisticType getStatisticType() {
                    return StatisticType.INT;
                }

                @Override
                public @NotNull Object getDefaultValue() {
                    return 0;
                }

                @Override
                public @NotNull String getDisplayName() {
                    return "Minimal";
                }

                @Override
                public @NotNull String getDescription() {
                    return "A minimal statistic";
                }
            };
            assertEquals(-1, minimal.getMaxSetSize());
        }
    }
}

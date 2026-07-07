package com.diamonddagger590.mccore.event.statistic;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.statistic.SimpleStatistic;
import com.diamonddagger590.mccore.statistic.Statistic;
import com.diamonddagger590.mccore.statistic.StatisticType;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatisticModifyEventTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();

    @SuppressWarnings("deprecation")
    private static NamespacedKey key(String key) {
        return new NamespacedKey("test", key);
    }

    private static CorePlayer testPlayer() {
        return new CorePlayer(PLAYER_UUID, null) {
            @Override
            public boolean useMutex() {
                return false;
            }
        };
    }

    private static Statistic intStat() {
        return new SimpleStatistic(key("int"), StatisticType.INT, 0, "Int", "Int stat");
    }

    private static Statistic longStat() {
        return new SimpleStatistic(key("long"), StatisticType.LONG, 0L, "Long", "Long stat");
    }

    private static Statistic doubleStat() {
        return new SimpleStatistic(key("double"), StatisticType.DOUBLE, 0.0, "Double", "Double stat");
    }

    private static Statistic stringStat() {
        return new SimpleStatistic(key("string"), StatisticType.STRING, "", "String", "String stat");
    }

    private static Statistic timestampStat() {
        return new SimpleStatistic(key("timestamp"), StatisticType.TIMESTAMP, Instant.EPOCH, "Timestamp", "Timestamp stat");
    }

    private static Statistic setStringStat() {
        return new SimpleStatistic(key("set"), StatisticType.SET_STRING, new LinkedHashSet<String>(), "Set", "Set stat");
    }

    // ── Constructor & Getters ─────────────────────────────────────────

    @Test
    @DisplayName("Given valid arguments, when constructing, then all getters return expected values")
    void constructor_setsAllFields() {
        CorePlayer player = testPlayer();
        Statistic stat = intStat();
        NamespacedKey statKey = key("int");

        StatisticModifyEvent event = new StatisticModifyEvent(player, statKey, stat, 0, 42, ModificationType.SET);

        assertSame(player, event.getCorePlayer());
        assertEquals(statKey, event.getStatisticKey());
        assertSame(stat, event.getStatistic());
        assertEquals(0, event.getOldValue());
        assertEquals(42, event.getNewValue());
        assertEquals(ModificationType.SET, event.getModificationType());
    }

    @Test
    @DisplayName("Given a new event, when checking cancelled, then defaults to false")
    void isCancelled_defaultsFalse() {
        StatisticModifyEvent event = new StatisticModifyEvent(
                testPlayer(), key("int"), intStat(), 0, 1, ModificationType.INCREMENT);
        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("Given an event, when setting cancelled to true, then isCancelled returns true")
    void setCancelled_togglesCancelledState() {
        StatisticModifyEvent event = new StatisticModifyEvent(
                testPlayer(), key("int"), intStat(), 0, 1, ModificationType.INCREMENT);
        event.setCancelled(true);
        assertTrue(event.isCancelled());
        event.setCancelled(false);
        assertFalse(event.isCancelled());
    }

    // ── setNewValue ───────────────────────────────────────────────────

    @Test
    @DisplayName("Given an INT event, when setting new value to a valid Integer, then value updates")
    void setNewValue_acceptsValidInteger() {
        StatisticModifyEvent event = new StatisticModifyEvent(
                testPlayer(), key("int"), intStat(), 0, 1, ModificationType.SET);
        event.setNewValue(99);
        assertEquals(99, event.getNewValue());
    }

    @Test
    @DisplayName("Given an INT event, when setting new value to a String, then throws IllegalArgumentException")
    void setNewValue_rejectsIncompatibleType() {
        StatisticModifyEvent event = new StatisticModifyEvent(
                testPlayer(), key("int"), intStat(), 0, 1, ModificationType.SET);
        assertThrows(IllegalArgumentException.class, () -> event.setNewValue("not an int"));
    }

    // ── validateValueType via constructor ─────────────────────────────

    @Test
    @DisplayName("Given INT type, when constructing with Integer value, then succeeds")
    void validateValueType_acceptsInteger_forIntType() {
        new StatisticModifyEvent(testPlayer(), key("int"), intStat(), 0, 42, ModificationType.SET);
    }

    @Test
    @DisplayName("Given INT type, when constructing with Long value, then throws IllegalArgumentException")
    void validateValueType_rejectsLong_forIntType() {
        assertThrows(IllegalArgumentException.class,
                () -> new StatisticModifyEvent(testPlayer(), key("int"), intStat(), 0, 42L, ModificationType.SET));
    }

    @Test
    @DisplayName("Given LONG type, when constructing with Long value, then succeeds")
    void validateValueType_acceptsLong_forLongType() {
        new StatisticModifyEvent(testPlayer(), key("long"), longStat(), 0L, 100L, ModificationType.SET);
    }

    @Test
    @DisplayName("Given LONG type, when constructing with Integer value, then throws IllegalArgumentException")
    void validateValueType_rejectsInteger_forLongType() {
        assertThrows(IllegalArgumentException.class,
                () -> new StatisticModifyEvent(testPlayer(), key("long"), longStat(), 0L, 100, ModificationType.SET));
    }

    @Test
    @DisplayName("Given DOUBLE type, when constructing with Double value, then succeeds")
    void validateValueType_acceptsDouble_forDoubleType() {
        new StatisticModifyEvent(testPlayer(), key("double"), doubleStat(), 0.0, 3.14, ModificationType.SET);
    }

    @Test
    @DisplayName("Given DOUBLE type, when constructing with Float value, then throws IllegalArgumentException")
    void validateValueType_rejectsFloat_forDoubleType() {
        assertThrows(IllegalArgumentException.class,
                () -> new StatisticModifyEvent(testPlayer(), key("double"), doubleStat(), 0.0, 3.14f, ModificationType.SET));
    }

    @Test
    @DisplayName("Given STRING type, when constructing with String value, then succeeds")
    void validateValueType_acceptsString_forStringType() {
        new StatisticModifyEvent(testPlayer(), key("string"), stringStat(), "", "hello", ModificationType.SET);
    }

    @Test
    @DisplayName("Given STRING type, when constructing with Integer value, then throws IllegalArgumentException")
    void validateValueType_rejectsInteger_forStringType() {
        assertThrows(IllegalArgumentException.class,
                () -> new StatisticModifyEvent(testPlayer(), key("string"), stringStat(), "", 42, ModificationType.SET));
    }

    @Test
    @DisplayName("Given TIMESTAMP type, when constructing with Instant value, then succeeds")
    void validateValueType_acceptsInstant_forTimestampType() {
        new StatisticModifyEvent(testPlayer(), key("timestamp"), timestampStat(), Instant.EPOCH, Instant.now(), ModificationType.SET);
    }

    @Test
    @DisplayName("Given TIMESTAMP type, when constructing with Long value, then throws IllegalArgumentException")
    void validateValueType_rejectsLong_forTimestampType() {
        assertThrows(IllegalArgumentException.class,
                () -> new StatisticModifyEvent(testPlayer(), key("timestamp"), timestampStat(), Instant.EPOCH, 123L, ModificationType.SET));
    }

    @Test
    @DisplayName("Given SET_STRING type, when constructing with Set<String> value, then succeeds")
    void validateValueType_acceptsSetString_forSetStringType() {
        new StatisticModifyEvent(testPlayer(), key("set"), setStringStat(),
                new LinkedHashSet<>(), Set.of("a", "b"), ModificationType.SET);
    }

    @Test
    @DisplayName("Given SET_STRING type, when constructing with empty Set, then succeeds")
    void validateValueType_acceptsEmptySet_forSetStringType() {
        new StatisticModifyEvent(testPlayer(), key("set"), setStringStat(),
                new LinkedHashSet<>(), Set.of(), ModificationType.SET);
    }

    @Test
    @DisplayName("Given SET_STRING type, when constructing with String value, then throws IllegalArgumentException")
    void validateValueType_rejectsString_forSetStringType() {
        assertThrows(IllegalArgumentException.class,
                () -> new StatisticModifyEvent(testPlayer(), key("set"), setStringStat(),
                        new LinkedHashSet<>(), "not a set", ModificationType.SET));
    }

    // ── Handler list ──────────────────────────────────────────────────

    @Test
    @DisplayName("Given an event, when getting handler list, then returns non-null handler list")
    void getHandlers_returnsNonNull() {
        StatisticModifyEvent event = new StatisticModifyEvent(
                testPlayer(), key("int"), intStat(), 0, 1, ModificationType.SET);
        assertSame(StatisticModifyEvent.getHandlerList(), event.getHandlers());
    }

    // ── Exception message content ─────────────────────────────────────

    @Test
    @DisplayName("Given type mismatch, when constructing, then exception message contains type and class info")
    void validateValueType_exceptionMessage_containsTypeInfo() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new StatisticModifyEvent(testPlayer(), key("int"), intStat(), 0, "bad", ModificationType.SET));
        assertTrue(ex.getMessage().contains("INT"));
        assertTrue(ex.getMessage().contains("String"));
    }

    // ── All ModificationType values ───────────────────────────────────

    static Stream<Arguments> allModificationTypes() {
        return Stream.of(ModificationType.values()).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("allModificationTypes")
    @DisplayName("Given each ModificationType, when constructing event, then getModificationType returns it")
    void constructor_acceptsAllModificationTypes(ModificationType type) {
        StatisticModifyEvent event = new StatisticModifyEvent(
                testPlayer(), key("int"), intStat(), 0, 1, type);
        assertEquals(type, event.getModificationType());
    }
}

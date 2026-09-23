package com.diamonddagger590.mccore.statistic;

import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatisticRegistryTest {

    @SuppressWarnings("deprecation")
    private static NamespacedKey key(String namespace, String key) {
        return new NamespacedKey(namespace, key);
    }

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
    }

    private StatisticRegistry registry() {
        return RegistryAccess.registryAccess().registry(RegistryKey.STATISTIC);
    }

    @Test
    @DisplayName("Given a new statistic, when registering, then the registry reports it as registered")
    void register_succeeds_whenKeyIsNew() {
        Statistic stat = new SimpleStatistic(key("test", "kills"), StatisticType.INT, 0, "Kills", "Total kills");
        registry().register(stat);
        assertTrue(registry().registered(stat));
    }

    @Test
    @DisplayName("Given an already-registered statistic, when registering the same key again, then throws IllegalArgumentException")
    void register_throwsIllegalArgument_whenKeyIsDuplicate() {
        Statistic stat = new SimpleStatistic(key("test", "kills"), StatisticType.INT, 0, "Kills", "Total kills");
        registry().register(stat);
        assertThrows(IllegalArgumentException.class, () -> registry().register(stat));
    }

    @Test
    @DisplayName("Given a statistic that was never registered, when checking registered, then returns false")
    void registered_returnsFalse_whenStatisticNotRegistered() {
        Statistic stat = new SimpleStatistic(key("test", "unknown"), StatisticType.INT, 0, "Unknown", "Unknown stat");
        assertFalse(registry().registered(stat));
    }

    @Test
    @DisplayName("Given a registered statistic, when getting by key, then returns the statistic")
    void getStatistic_returnsStatistic_whenKeyIsRegistered() {
        NamespacedKey key = key("test", "kills");
        Statistic stat = new SimpleStatistic(key, StatisticType.INT, 0, "Kills", "Total kills");
        registry().register(stat);
        assertTrue(registry().getStatistic(key).isPresent());
        assertEquals(stat, registry().getStatistic(key).get());
    }

    @Test
    @DisplayName("Given no registered statistic for a key, when getting by that key, then returns empty Optional")
    void getStatistic_returnsEmpty_whenKeyIsNotRegistered() {
        assertFalse(registry().getStatistic(key("test", "none")).isPresent());
    }

    @Test
    @DisplayName("Given two registered statistics, when getting all registered statistics, then returns both")
    void getRegisteredStatistics_returnsAll_whenMultipleRegistered() {
        Statistic stat1 = new SimpleStatistic(key("test", "a"), StatisticType.INT, 0, "A", "Stat A");
        Statistic stat2 = new SimpleStatistic(key("test", "b"), StatisticType.LONG, 0L, "B", "Stat B");
        registry().register(stat1);
        registry().register(stat2);
        assertEquals(2, registry().getRegisteredStatistics().size());
    }

    @Test
    @DisplayName("Given an INT statistic with a String default, when registering, then throws IllegalArgumentException")
    void register_throwsIllegalArgument_whenIntStatHasStringDefault() {
        assertThrows(IllegalArgumentException.class, () ->
                registry().register(new SimpleStatistic(key("test", "bad"), StatisticType.INT, "wrong", "Bad", "Mismatched default")));
    }

    @Test
    @DisplayName("Given an INT statistic with a Long default, when registering, then throws IllegalArgumentException")
    void register_throwsIllegalArgument_whenIntStatHasLongDefault() {
        assertThrows(IllegalArgumentException.class, () ->
                registry().register(new SimpleStatistic(key("test", "bad"), StatisticType.INT, 5L, "Bad", "Long for INT")));
    }

    @Test
    @DisplayName("Given a SET_STRING statistic with an empty Set default, when registering, then succeeds")
    void register_succeeds_whenSetStringStatHasEmptySetDefault() {
        Set<String> defaultSet = new LinkedHashSet<>();
        Statistic stat = new SimpleStatistic(key("test", "set"), StatisticType.SET_STRING, defaultSet, "Set", "A set stat");
        registry().register(stat);
        assertTrue(registry().registered(stat));
    }

    @Test
    @DisplayName("Given two registered statistics, when getting all keys, then returns both keys")
    void getRegisteredStatisticKeys_returnsAllKeys_whenMultipleRegistered() {
        Statistic stat1 = new SimpleStatistic(key("test", "a"), StatisticType.INT, 0, "A", "Stat A");
        Statistic stat2 = new SimpleStatistic(key("other", "b"), StatisticType.LONG, 0L, "B", "Stat B");
        registry().register(stat1);
        registry().register(stat2);
        assertEquals(2, registry().getRegisteredStatisticKeys().size());
        assertTrue(registry().getRegisteredStatisticKeys().contains(key("test", "a")));
        assertTrue(registry().getRegisteredStatisticKeys().contains(key("other", "b")));
    }

    @Test
    @DisplayName("Given a DOUBLE statistic with a Double default, when registering, then succeeds")
    void register_succeeds_whenDoubleStatHasDoubleDefault() {
        Statistic stat = new SimpleStatistic(key("test", "ratio"), StatisticType.DOUBLE, 0.5, "Ratio", "A ratio");
        registry().register(stat);
        assertTrue(registry().registered(stat));
    }

    @Test
    @DisplayName("Given a DOUBLE statistic with an Integer default, when registering, then throws IllegalArgumentException")
    void register_throwsIllegalArgument_whenDoubleStatHasIntegerDefault() {
        assertThrows(IllegalArgumentException.class, () ->
                registry().register(new SimpleStatistic(key("test", "bad"), StatisticType.DOUBLE, 5, "Bad", "Int for DOUBLE")));
    }

    @Test
    @DisplayName("Given a DOUBLE statistic with a String default, when registering, then throws IllegalArgumentException")
    void register_throwsIllegalArgument_whenDoubleStatHasStringDefault() {
        assertThrows(IllegalArgumentException.class, () ->
                registry().register(new SimpleStatistic(key("test", "bad"), StatisticType.DOUBLE, "wrong", "Bad", "String for DOUBLE")));
    }

    @Test
    @DisplayName("Given a STRING statistic with a String default, when registering, then succeeds")
    void register_succeeds_whenStringStatHasStringDefault() {
        Statistic stat = new SimpleStatistic(key("test", "name"), StatisticType.STRING, "default", "Name", "A name");
        registry().register(stat);
        assertTrue(registry().registered(stat));
    }

    @Test
    @DisplayName("Given a STRING statistic with an Integer default, when registering, then throws IllegalArgumentException")
    void register_throwsIllegalArgument_whenStringStatHasIntegerDefault() {
        assertThrows(IllegalArgumentException.class, () ->
                registry().register(new SimpleStatistic(key("test", "bad"), StatisticType.STRING, 42, "Bad", "Int for STRING")));
    }

    @Test
    @DisplayName("Given a TIMESTAMP statistic with an Instant default, when registering, then succeeds")
    void register_succeeds_whenTimestampStatHasInstantDefault() {
        Statistic stat = new SimpleStatistic(key("test", "last_login"), StatisticType.TIMESTAMP, Instant.EPOCH, "Last Login", "Last login time");
        registry().register(stat);
        assertTrue(registry().registered(stat));
    }

    @Test
    @DisplayName("Given a TIMESTAMP statistic with a String default, when registering, then throws IllegalArgumentException")
    void register_throwsIllegalArgument_whenTimestampStatHasStringDefault() {
        assertThrows(IllegalArgumentException.class, () ->
                registry().register(new SimpleStatistic(key("test", "bad"), StatisticType.TIMESTAMP, "2024-01-01", "Bad", "String for TIMESTAMP")));
    }

    @Test
    @DisplayName("Given a TIMESTAMP statistic with a Long default, when registering, then throws IllegalArgumentException")
    void register_throwsIllegalArgument_whenTimestampStatHasLongDefault() {
        assertThrows(IllegalArgumentException.class, () ->
                registry().register(new SimpleStatistic(key("test", "bad"), StatisticType.TIMESTAMP, 1000L, "Bad", "Long for TIMESTAMP")));
    }

    @Test
    @DisplayName("Given a SET_STRING statistic with a non-empty valid Set default, when registering, then succeeds")
    void register_succeeds_whenSetStringStatHasNonEmptyValidDefault() {
        Set<String> defaultSet = new LinkedHashSet<>();
        defaultSet.add("value1");
        defaultSet.add("value2");
        Statistic stat = new SimpleStatistic(key("test", "tags"), StatisticType.SET_STRING, defaultSet, "Tags", "Player tags");
        registry().register(stat);
        assertTrue(registry().registered(stat));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    @DisplayName("Given a SET_STRING statistic with a Set containing non-String elements, when registering, then throws IllegalArgumentException")
    void register_throwsIllegalArgument_whenSetStringStatHasNonStringElements() {
        Set rawSet = new LinkedHashSet<>();
        rawSet.add(1);
        rawSet.add(2);
        assertThrows(IllegalArgumentException.class, () ->
                registry().register(new SimpleStatistic(key("test", "bad"), StatisticType.SET_STRING, rawSet, "Bad", "Non-string set")));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    @DisplayName("Given a SET_STRING statistic with a Set containing mixed String and non-String elements, when registering, then throws IllegalArgumentException")
    void register_throwsIllegalArgument_whenSetStringStatHasMixedTypeElements() {
        Set rawSet = new LinkedHashSet<>();
        rawSet.add("valid");
        rawSet.add(42);
        assertThrows(IllegalArgumentException.class, () ->
                registry().register(new SimpleStatistic(key("test", "bad"), StatisticType.SET_STRING, rawSet, "Bad", "Mixed-type set")));
    }

    @Test
    @DisplayName("Given a SET_STRING statistic with a non-Set default, when registering, then throws IllegalArgumentException")
    void register_throwsIllegalArgument_whenSetStringStatHasNonSetDefault() {
        assertThrows(IllegalArgumentException.class, () ->
                registry().register(new SimpleStatistic(key("test", "bad"), StatisticType.SET_STRING, "wrong", "Bad", "String for SET_STRING")));
    }

    @Test
    @DisplayName("Given a LONG statistic with a Long default, when registering, then succeeds")
    void register_succeeds_whenLongStatHasLongDefault() {
        Statistic stat = new SimpleStatistic(key("test", "distance"), StatisticType.LONG, 0L, "Distance", "Total distance");
        registry().register(stat);
        assertTrue(registry().registered(stat));
    }

    @Test
    @DisplayName("Given a LONG statistic with a Double default, when registering, then throws IllegalArgumentException")
    void register_throwsIllegalArgument_whenLongStatHasDoubleDefault() {
        assertThrows(IllegalArgumentException.class, () ->
                registry().register(new SimpleStatistic(key("test", "bad"), StatisticType.LONG, 1.5, "Bad", "Double for LONG")));
    }
}

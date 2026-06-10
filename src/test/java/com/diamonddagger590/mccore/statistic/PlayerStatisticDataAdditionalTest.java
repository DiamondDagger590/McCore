package com.diamonddagger590.mccore.statistic;

import com.diamonddagger590.mccore.event.statistic.ModificationType;
import com.diamonddagger590.mccore.event.statistic.PostStatisticModifyEvent;
import com.diamonddagger590.mccore.event.statistic.StatisticModifyEvent;
import com.diamonddagger590.mccore.exception.statistic.StatisticNotRegisteredException;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerStatisticDataAdditionalTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();

    @SuppressWarnings("deprecation")
    private static NamespacedKey key(String namespace, String key) {
        return new NamespacedKey(namespace, key);
    }

    private static final NamespacedKey INT_KEY = key("test", "int_stat");
    private static final NamespacedKey LONG_KEY = key("test", "long_stat");
    private static final NamespacedKey DOUBLE_KEY = key("test", "double_stat");
    private static final NamespacedKey STRING_KEY = key("test", "string_stat");
    private static final NamespacedKey TIMESTAMP_KEY = key("test", "timestamp_stat");
    private static final NamespacedKey SET_KEY = key("test", "set_stat");

    private final List<Event> firedEvents = Collections.synchronizedList(new ArrayList<>());
    private CorePlayer mockPlayer;
    private PlayerStatisticData data;

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();
        StatisticRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.STATISTIC);

        registry.register(new SimpleStatistic(INT_KEY, StatisticType.INT, 0, "Int", "Int stat"));
        registry.register(new SimpleStatistic(LONG_KEY, StatisticType.LONG, 0L, "Long", "Long stat"));
        registry.register(new SimpleStatistic(DOUBLE_KEY, StatisticType.DOUBLE, 0.0, "Double", "Double stat"));
        registry.register(new SimpleStatistic(STRING_KEY, StatisticType.STRING, "", "String", "String stat"));
        registry.register(new SimpleStatistic(TIMESTAMP_KEY, StatisticType.TIMESTAMP, Instant.EPOCH, "Timestamp", "Timestamp stat"));
        registry.register(new SimpleStatistic(SET_KEY, StatisticType.SET_STRING, new LinkedHashSet<String>(), "Set", "Set stat", 3));

        mockPlayer = new TestCorePlayer(PLAYER_UUID);
        firedEvents.clear();
        data = new PlayerStatisticData(
                PLAYER_UUID,
                event -> firedEvents.add(event),
                id -> mockPlayer
        );
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
    }

    // --- getUUID ---

    @Test
    @DisplayName("Given a PlayerStatisticData, when calling getUUID, then returns the UUID passed to constructor")
    void getUUID_returnsConstructorUUID() {
        assertEquals(PLAYER_UUID, data.getUUID());
    }

    // --- getValue (raw Object accessor) ---

    @Test
    @DisplayName("Given no stored value and a registered stat, when calling getValue, then returns the default value")
    void getValue_returnsDefault_whenNoStoredValue() {
        assertTrue(data.getValue(INT_KEY).isPresent());
        assertEquals(0, data.getValue(INT_KEY).get());
    }

    @Test
    @DisplayName("Given a stored value, when calling getValue, then returns that stored value")
    void getValue_returnsStoredValue_afterSet() {
        data.setValue(INT_KEY, 42);
        assertEquals(42, data.getValue(INT_KEY).get());
    }

    @Test
    @DisplayName("Given an unregistered key, when calling getValue, then returns empty")
    void getValue_returnsEmpty_forUnregisteredKey() {
        assertTrue(data.getValue(key("test", "nonexistent")).isEmpty());
    }

    @Test
    @DisplayName("Given a string stat with stored value, when calling getValue, then returns the string")
    void getValue_returnsString_whenStringStatStored() {
        data.setValue(STRING_KEY, "hello");
        assertEquals("hello", data.getValue(STRING_KEY).get());
    }

    @Test
    @DisplayName("Given a timestamp stat with stored value, when calling getValue, then returns the Instant")
    void getValue_returnsInstant_whenTimestampStatStored() {
        Instant now = Instant.now();
        data.setValue(TIMESTAMP_KEY, now);
        assertEquals(now, data.getValue(TIMESTAMP_KEY).get());
    }

    // --- bulkIncrementLong ---

    @Test
    @DisplayName("Given a long stat, when calling bulkIncrementLong, then it delegates to incrementLong")
    void bulkIncrementLong_delegatesToIncrementLong() {
        data.bulkIncrementLong(LONG_KEY, 50L);
        assertEquals(50L, data.getLongValue(LONG_KEY).orElse(0L));
    }

    @Test
    @DisplayName("Given a long stat, when calling bulkIncrementLong multiple times, then values accumulate")
    void bulkIncrementLong_accumulates_whenCalledMultipleTimes() {
        data.bulkIncrementLong(LONG_KEY, 10L);
        data.bulkIncrementLong(LONG_KEY, 20L);
        assertEquals(30L, data.getLongValue(LONG_KEY).orElse(0L));
    }

    @Test
    @DisplayName("Given a long stat, when calling bulkIncrementLong, then fires pre and post events with INCREMENT type")
    void bulkIncrementLong_firesEvents_withIncrementModificationType() {
        data.bulkIncrementLong(LONG_KEY, 5L);
        assertEquals(2, firedEvents.size());
        assertTrue(firedEvents.get(0) instanceof StatisticModifyEvent);
        StatisticModifyEvent preEvent = (StatisticModifyEvent) firedEvents.get(0);
        assertEquals(ModificationType.INCREMENT, preEvent.getModificationType());
        assertTrue(firedEvents.get(1) instanceof PostStatisticModifyEvent);
    }

    // --- getOrCreateSet with plain HashSet (non-LinkedHashSet) ---

    @Test
    @DisplayName("Given a set stat populated with a plain HashSet via entries, when adding to the set, then works correctly")
    void addToSet_worksCorrectly_whenPopulatedWithPlainHashSet() {
        Set<String> plainSet = new HashSet<>();
        plainSet.add("existing");
        data.populateFromEntries(Map.of(SET_KEY, new StatisticEntry(SET_KEY, StatisticType.SET_STRING, plainSet)));
        assertTrue(data.addToSet(SET_KEY, "new_element"));
        Set<String> result = data.getSetValue(SET_KEY).get();
        assertTrue(result.contains("existing"));
        assertTrue(result.contains("new_element"));
    }

    @Test
    @DisplayName("Given a set stat populated with a plain HashSet, when removing from the set, then works correctly")
    void removeFromSet_worksCorrectly_whenPopulatedWithPlainHashSet() {
        Set<String> plainSet = new HashSet<>();
        plainSet.add("target");
        data.populateFromEntries(Map.of(SET_KEY, new StatisticEntry(SET_KEY, StatisticType.SET_STRING, plainSet)));
        assertTrue(data.removeFromSet(SET_KEY, "target"));
        assertFalse(data.getSetValue(SET_KEY).get().contains("target"));
    }

    // --- Increment cancellation ---

    @Test
    @DisplayName("Given a cancelling event dispatcher, when incrementing int, then value does not change")
    void incrementInt_doesNotChange_whenCancelled() {
        data = new PlayerStatisticData(
                PLAYER_UUID,
                event -> {
                    if (event instanceof StatisticModifyEvent sme) {
                        sme.setCancelled(true);
                    }
                },
                id -> mockPlayer
        );
        data.incrementInt(INT_KEY, 10);
        assertEquals(0, data.getIntValue(INT_KEY).orElse(0));
        assertFalse(data.isDirty());
    }

    @Test
    @DisplayName("Given a cancelling event dispatcher, when incrementing double, then value does not change")
    void incrementDouble_doesNotChange_whenCancelled() {
        data = new PlayerStatisticData(
                PLAYER_UUID,
                event -> {
                    if (event instanceof StatisticModifyEvent sme) {
                        sme.setCancelled(true);
                    }
                },
                id -> mockPlayer
        );
        data.incrementDouble(DOUBLE_KEY, 5.0);
        assertEquals(0.0, data.getDoubleValue(DOUBLE_KEY).orElse(0.0), 0.001);
        assertFalse(data.isDirty());
    }

    @Test
    @DisplayName("Given a cancelling event dispatcher, when incrementing long, then value does not change")
    void incrementLong_doesNotChange_whenCancelled() {
        data = new PlayerStatisticData(
                PLAYER_UUID,
                event -> {
                    if (event instanceof StatisticModifyEvent sme) {
                        sme.setCancelled(true);
                    }
                },
                id -> mockPlayer
        );
        data.incrementLong(LONG_KEY, 100L);
        assertEquals(0L, data.getLongValue(LONG_KEY).orElse(0L));
        assertFalse(data.isDirty());
    }

    // --- setMax cancellation ---

    @Test
    @DisplayName("Given a cancelling dispatcher, when setting max long, then value does not change")
    void setMaxLong_doesNotChange_whenCancelled() {
        data = new PlayerStatisticData(
                PLAYER_UUID,
                event -> {
                    if (event instanceof StatisticModifyEvent sme) {
                        sme.setCancelled(true);
                    }
                },
                id -> mockPlayer
        );
        data.setMaxLong(LONG_KEY, 100L);
        assertEquals(0L, data.getLongValue(LONG_KEY).orElse(0L));
    }

    @Test
    @DisplayName("Given a cancelling dispatcher, when setting max int, then value does not change")
    void setMaxInt_doesNotChange_whenCancelled() {
        data = new PlayerStatisticData(
                PLAYER_UUID,
                event -> {
                    if (event instanceof StatisticModifyEvent sme) {
                        sme.setCancelled(true);
                    }
                },
                id -> mockPlayer
        );
        data.setMaxInt(INT_KEY, 100);
        assertEquals(0, data.getIntValue(INT_KEY).orElse(0));
    }

    @Test
    @DisplayName("Given a cancelling dispatcher, when setting max double, then value does not change")
    void setMaxDouble_doesNotChange_whenCancelled() {
        data = new PlayerStatisticData(
                PLAYER_UUID,
                event -> {
                    if (event instanceof StatisticModifyEvent sme) {
                        sme.setCancelled(true);
                    }
                },
                id -> mockPlayer
        );
        data.setMaxDouble(DOUBLE_KEY, 100.0);
        assertEquals(0.0, data.getDoubleValue(DOUBLE_KEY).orElse(0.0), 0.001);
    }

    // --- setTimestampIfAbsent cancellation ---

    @Test
    @DisplayName("Given a cancelling dispatcher, when setting timestamp if absent, then value does not change")
    void setTimestampIfAbsent_doesNotChange_whenCancelled() {
        data = new PlayerStatisticData(
                PLAYER_UUID,
                event -> {
                    if (event instanceof StatisticModifyEvent sme) {
                        sme.setCancelled(true);
                    }
                },
                id -> mockPlayer
        );
        data.setTimestampIfAbsent(TIMESTAMP_KEY, Instant.now());
        assertEquals(Instant.EPOCH, data.getTimestampValue(TIMESTAMP_KEY).orElse(Instant.EPOCH));
    }

    // --- addToSet cancellation ---

    @Test
    @DisplayName("Given a cancelling dispatcher, when adding to set, then returns false and set is unchanged")
    void addToSet_returnsFalse_whenCancelled() {
        data = new PlayerStatisticData(
                PLAYER_UUID,
                event -> {
                    if (event instanceof StatisticModifyEvent sme) {
                        sme.setCancelled(true);
                    }
                },
                id -> mockPlayer
        );
        assertFalse(data.addToSet(SET_KEY, "value"));
        assertTrue(data.getSetValue(SET_KEY).get().isEmpty());
    }

    // --- removeFromSet cancellation ---

    @Test
    @DisplayName("Given a set with an element and a cancelling dispatcher, when removing, then element remains")
    void removeFromSet_doesNotRemove_whenCancelled() {
        data.addToSet(SET_KEY, "keep_me");
        data = new PlayerStatisticData(
                PLAYER_UUID,
                event -> {
                    if (event instanceof StatisticModifyEvent sme) {
                        sme.setCancelled(true);
                    }
                },
                id -> mockPlayer
        );
        data.populateFromEntries(Map.of(SET_KEY, new StatisticEntry(SET_KEY, StatisticType.SET_STRING, new LinkedHashSet<>(Set.of("keep_me")))));
        assertFalse(data.removeFromSet(SET_KEY, "keep_me"));
        assertTrue(data.getSetValue(SET_KEY).get().contains("keep_me"));
    }

    // --- getModifiedEntries with null value ---

    @Test
    @DisplayName("Given a dirty key whose value was cleared, when getting modified entries, then that key is skipped")
    void getModifiedEntries_skipsKey_whenValueIsNull() {
        data.setValue(INT_KEY, 42);
        assertTrue(data.isDirty());
        data.populateFromEntries(Map.of());
        data.setValue(INT_KEY, 99);
        Map<NamespacedKey, StatisticEntry> modified = data.getModifiedEntries();
        assertTrue(modified.containsKey(INT_KEY));
        assertEquals(99, modified.get(INT_KEY).getAsInt());
    }

    // --- Increment throws for unregistered key ---

    @Test
    @DisplayName("Given an unregistered key, when incrementing long, then throws StatisticNotRegisteredException")
    void incrementLong_throws_forUnregisteredKey() {
        assertThrows(StatisticNotRegisteredException.class,
                () -> data.incrementLong(key("test", "nonexistent"), 1L));
    }

    @Test
    @DisplayName("Given an unregistered key, when incrementing int, then throws StatisticNotRegisteredException")
    void incrementInt_throws_forUnregisteredKey() {
        assertThrows(StatisticNotRegisteredException.class,
                () -> data.incrementInt(key("test", "nonexistent"), 1));
    }

    @Test
    @DisplayName("Given an unregistered key, when incrementing double, then throws StatisticNotRegisteredException")
    void incrementDouble_throws_forUnregisteredKey() {
        assertThrows(StatisticNotRegisteredException.class,
                () -> data.incrementDouble(key("test", "nonexistent"), 1.0));
    }

    @Test
    @DisplayName("Given an unregistered key, when setting max long, then throws StatisticNotRegisteredException")
    void setMaxLong_throws_forUnregisteredKey() {
        assertThrows(StatisticNotRegisteredException.class,
                () -> data.setMaxLong(key("test", "nonexistent"), 1L));
    }

    @Test
    @DisplayName("Given an unregistered key, when setting max int, then throws StatisticNotRegisteredException")
    void setMaxInt_throws_forUnregisteredKey() {
        assertThrows(StatisticNotRegisteredException.class,
                () -> data.setMaxInt(key("test", "nonexistent"), 1));
    }

    @Test
    @DisplayName("Given an unregistered key, when setting max double, then throws StatisticNotRegisteredException")
    void setMaxDouble_throws_forUnregisteredKey() {
        assertThrows(StatisticNotRegisteredException.class,
                () -> data.setMaxDouble(key("test", "nonexistent"), 1.0));
    }

    @Test
    @DisplayName("Given an unregistered key, when setting timestamp if absent, then throws StatisticNotRegisteredException")
    void setTimestampIfAbsent_throws_forUnregisteredKey() {
        assertThrows(StatisticNotRegisteredException.class,
                () -> data.setTimestampIfAbsent(key("test", "nonexistent"), Instant.now()));
    }

    @Test
    @DisplayName("Given an unregistered key, when adding to set, then throws StatisticNotRegisteredException")
    void addToSet_throws_forUnregisteredKey() {
        assertThrows(StatisticNotRegisteredException.class,
                () -> data.addToSet(key("test", "nonexistent"), "value"));
    }

    @Test
    @DisplayName("Given an unregistered key, when removing from set, then throws StatisticNotRegisteredException")
    void removeFromSet_throws_forUnregisteredKey() {
        assertThrows(StatisticNotRegisteredException.class,
                () -> data.removeFromSet(key("test", "nonexistent"), "value"));
    }

    // --- Test helper ---

    private static class TestCorePlayer extends CorePlayer {

        TestCorePlayer(UUID uuid) {
            super(uuid, null);
        }

        @Override
        public boolean useMutex() {
            return false;
        }
    }
}

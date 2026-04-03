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
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerStatisticDataTest {

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

        // Create a minimal mock CorePlayer
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

    // ── Typed Getters ──────────────────────────────────────────────────

    @Test
    void getIntValueReturnsDefaultWhenNoStoredValue() {
        assertEquals(0, data.getIntValue(INT_KEY).orElse(-1));
    }

    @Test
    void getIntValueReturnsStoredValueAfterLoad() {
        data.populateFromEntries(Map.of(INT_KEY, new StatisticEntry(INT_KEY, StatisticType.INT, 42)));
        assertEquals(42, data.getIntValue(INT_KEY).orElse(-1));
    }

    @Test
    void getIntValueReturnsEmptyForUnregisteredKey() {
        assertTrue(data.getIntValue(key("test", "unregistered")).isEmpty());
    }

    @Test
    void getLongValueReturnsDefault() {
        assertEquals(0L, data.getLongValue(LONG_KEY).orElse(-1L));
    }

    @Test
    void getDoubleValueReturnsDefault() {
        assertEquals(0.0, data.getDoubleValue(DOUBLE_KEY).orElse(-1.0));
    }

    @Test
    void getStringValueReturnsDefault() {
        assertEquals("", data.getStringValue(STRING_KEY).orElse(null));
    }

    @Test
    void getTimestampValueReturnsDefault() {
        assertEquals(Instant.EPOCH, data.getTimestampValue(TIMESTAMP_KEY).orElse(null));
    }

    @Test
    void getSetValueReturnsEmptyDefault() {
        assertTrue(data.getSetValue(SET_KEY).isPresent());
        assertTrue(data.getSetValue(SET_KEY).get().isEmpty());
    }

    // ── setValue ────────────────────────────────────────────────────────

    @Test
    void setValueStoresAndMarksDirty() {
        data.setValue(INT_KEY, 99);
        assertEquals(99, data.getIntValue(INT_KEY).orElse(-1));
        assertTrue(data.isDirty());
    }

    @Test
    void setValueFiresPreAndPostEvents() {
        data.setValue(INT_KEY, 10);
        assertEquals(2, firedEvents.size());
        assertTrue(firedEvents.get(0) instanceof StatisticModifyEvent);
        assertTrue(firedEvents.get(1) instanceof PostStatisticModifyEvent);
    }

    @Test
    void setValueCancellationPreventsChange() {
        data = new PlayerStatisticData(
                PLAYER_UUID,
                event -> {
                    firedEvents.add(event);
                    if (event instanceof StatisticModifyEvent sme) {
                        sme.setCancelled(true);
                    }
                },
                id -> mockPlayer
        );
        data.setValue(INT_KEY, 99);
        assertEquals(0, data.getIntValue(INT_KEY).orElse(0));
        assertFalse(data.isDirty());
    }

    @Test
    void setValueEventAdjustsAppliedValue() {
        data = new PlayerStatisticData(
                PLAYER_UUID,
                event -> {
                    if (event instanceof StatisticModifyEvent sme) {
                        sme.setNewValue(42);
                    }
                },
                id -> mockPlayer
        );
        data.setValue(INT_KEY, 99);
        assertEquals(42, data.getIntValue(INT_KEY).orElse(-1));
    }

    @Test
    void setValueThrowsForUnregisteredKey() {
        assertThrows(StatisticNotRegisteredException.class,
                () -> data.setValue(key("test", "nope"), 1));
    }

    // ── Increment operations ───────────────────────────────────────────

    @Test
    void incrementLongAddsDelta() {
        data.incrementLong(LONG_KEY, 5L);
        assertEquals(5L, data.getLongValue(LONG_KEY).orElse(0L));
        data.incrementLong(LONG_KEY, 3L);
        assertEquals(8L, data.getLongValue(LONG_KEY).orElse(0L));
    }

    @Test
    void incrementIntAddsDelta() {
        data.incrementInt(INT_KEY, 10);
        assertEquals(10, data.getIntValue(INT_KEY).orElse(0));
    }

    @Test
    void incrementDoubleAddsDelta() {
        data.incrementDouble(DOUBLE_KEY, 1.5);
        assertEquals(1.5, data.getDoubleValue(DOUBLE_KEY).orElse(0.0), 0.001);
    }

    @Test
    void incrementFiresEvents() {
        data.incrementLong(LONG_KEY, 1L);
        assertEquals(2, firedEvents.size());
        assertTrue(firedEvents.get(0) instanceof StatisticModifyEvent);
        StatisticModifyEvent preEvent = (StatisticModifyEvent) firedEvents.get(0);
        assertEquals(ModificationType.INCREMENT, preEvent.getModificationType());
    }

    // ── Conditional mutators ───────────────────────────────────────────

    @Test
    void setMaxLongUpdatesWhenNewIsGreater() {
        data.setMaxLong(LONG_KEY, 10L);
        assertEquals(10L, data.getLongValue(LONG_KEY).orElse(0L));
        data.setMaxLong(LONG_KEY, 20L);
        assertEquals(20L, data.getLongValue(LONG_KEY).orElse(0L));
    }

    @Test
    void setMaxLongDoesNotUpdateWhenNewIsSmaller() {
        data.setMaxLong(LONG_KEY, 20L);
        firedEvents.clear();
        data.setMaxLong(LONG_KEY, 10L);
        assertEquals(20L, data.getLongValue(LONG_KEY).orElse(0L));
        assertTrue(firedEvents.isEmpty());
    }

    @Test
    void setMaxIntWorks() {
        data.setMaxInt(INT_KEY, 5);
        assertEquals(5, data.getIntValue(INT_KEY).orElse(0));
        data.setMaxInt(INT_KEY, 3);
        assertEquals(5, data.getIntValue(INT_KEY).orElse(0));
    }

    @Test
    void setMaxDoubleWorks() {
        data.setMaxDouble(DOUBLE_KEY, 5.5);
        assertEquals(5.5, data.getDoubleValue(DOUBLE_KEY).orElse(0.0), 0.001);
        data.setMaxDouble(DOUBLE_KEY, 3.0);
        assertEquals(5.5, data.getDoubleValue(DOUBLE_KEY).orElse(0.0), 0.001);
    }

    @Test
    void setTimestampIfAbsentSetsOnFirstCall() {
        Instant now = Instant.now();
        data.setTimestampIfAbsent(TIMESTAMP_KEY, now);
        assertEquals(now, data.getTimestampValue(TIMESTAMP_KEY).orElse(null));
    }

    @Test
    void setTimestampIfAbsentDoesNotOverwrite() {
        Instant first = Instant.ofEpochMilli(1000);
        Instant second = Instant.ofEpochMilli(2000);
        data.setTimestampIfAbsent(TIMESTAMP_KEY, first);
        firedEvents.clear();
        data.setTimestampIfAbsent(TIMESTAMP_KEY, second);
        assertEquals(first, data.getTimestampValue(TIMESTAMP_KEY).orElse(null));
        assertTrue(firedEvents.isEmpty());
    }

    // ── Set operations ─────────────────────────────────────────────────

    @Test
    void addToSetAddsElement() {
        assertTrue(data.addToSet(SET_KEY, "a"));
        assertTrue(data.getSetValue(SET_KEY).get().contains("a"));
    }

    @Test
    void addToSetReturnsFalseForDuplicate() {
        data.addToSet(SET_KEY, "a");
        firedEvents.clear();
        assertFalse(data.addToSet(SET_KEY, "a"));
        assertTrue(firedEvents.isEmpty());
    }

    @Test
    void removeFromSetRemovesElement() {
        data.addToSet(SET_KEY, "a");
        assertTrue(data.removeFromSet(SET_KEY, "a"));
        assertFalse(data.getSetValue(SET_KEY).get().contains("a"));
    }

    @Test
    void removeFromSetReturnsFalseForAbsent() {
        firedEvents.clear();
        assertFalse(data.removeFromSet(SET_KEY, "nonexistent"));
        assertTrue(firedEvents.isEmpty());
    }

    @Test
    void addToSetEnforcesMaxSetSizeEvictsOldest() {
        data.addToSet(SET_KEY, "a");
        data.addToSet(SET_KEY, "b");
        data.addToSet(SET_KEY, "c");
        // At max size (3), adding "d" should evict "a"
        data.addToSet(SET_KEY, "d");
        Set<String> result = data.getSetValue(SET_KEY).get();
        assertEquals(3, result.size());
        assertFalse(result.contains("a"));
        assertTrue(result.contains("b"));
        assertTrue(result.contains("c"));
        assertTrue(result.contains("d"));
    }

    // ── Dirty tracking ─────────────────────────────────────────────────

    @Test
    void isDirtyReturnsFalseAfterConstruction() {
        assertFalse(data.isDirty());
    }

    @Test
    void isDirtyReturnsFalseAfterLoadFromDatabase() {
        data.populateFromEntries(Map.of(INT_KEY, new StatisticEntry(INT_KEY, StatisticType.INT, 42)));
        assertFalse(data.isDirty());
    }

    @Test
    void isDirtyReturnsTrueAfterMutation() {
        data.setValue(INT_KEY, 1);
        assertTrue(data.isDirty());
    }

    @Test
    void getModifiedEntriesReturnsOnlyMutatedKeys() {
        data.populateFromEntries(Map.of(
                INT_KEY, new StatisticEntry(INT_KEY, StatisticType.INT, 10),
                LONG_KEY, new StatisticEntry(LONG_KEY, StatisticType.LONG, 20L)
        ));
        data.incrementInt(INT_KEY, 1);
        Map<NamespacedKey, StatisticEntry> modified = data.getModifiedEntries();
        assertEquals(1, modified.size());
        assertTrue(modified.containsKey(INT_KEY));
        assertEquals(11, modified.get(INT_KEY).getAsInt());
    }

    @Test
    void markCleanClearsDirtyState() {
        data.setValue(INT_KEY, 99);
        assertTrue(data.isDirty());
        data.markClean(data.getModifiedEntries().keySet());
        assertFalse(data.isDirty());
    }

    @Test
    void markCleanOnlyRemovesSavedKeys() {
        data.setValue(INT_KEY, 99);
        data.setValue(LONG_KEY, 200L);
        assertTrue(data.isDirty());
        // Simulate: save only INT_KEY, LONG_KEY was dirtied after the snapshot
        data.markClean(Set.of(INT_KEY));
        assertTrue(data.isDirty());
        assertEquals(1, data.getModifiedEntries().size());
        assertTrue(data.getModifiedEntries().containsKey(LONG_KEY));
    }

    // ── populateFromEntries ───────────────────────────────────────────────

    @Test
    void populateFromEntriesPopulatesValues() {
        data.populateFromEntries(Map.of(
                INT_KEY, new StatisticEntry(INT_KEY, StatisticType.INT, 42),
                LONG_KEY, new StatisticEntry(LONG_KEY, StatisticType.LONG, 100L)
        ));
        assertEquals(42, data.getIntValue(INT_KEY).orElse(-1));
        assertEquals(100L, data.getLongValue(LONG_KEY).orElse(-1L));
    }

    @Test
    void populateFromEntriesClearsPreviousValues() {
        data.setValue(STRING_KEY, "old");
        data.populateFromEntries(Map.of(INT_KEY, new StatisticEntry(INT_KEY, StatisticType.INT, 1)));
        assertEquals("", data.getStringValue(STRING_KEY).orElse(""));
    }

    // ── Thread safety ──────────────────────────────────────────────────

    @Test
    void concurrentIncrementLongProducesCorrectTotal() throws InterruptedException {
        int threadCount = 10;
        int incrementsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    data.incrementLong(LONG_KEY, 1L);
                }
                latch.countDown();
            });
        }
        latch.await();
        executor.shutdown();

        assertEquals(threadCount * incrementsPerThread, data.getLongValue(LONG_KEY).orElse(0L));
    }

    // ── Test helper: minimal CorePlayer stub ───────────────────────────

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

package com.diamonddagger590.mccore.statistic;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerStatisticDataSetCoverageTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();

    @SuppressWarnings("deprecation")
    private static NamespacedKey key(String namespace, String key) {
        return new NamespacedKey(namespace, key);
    }

    private static final NamespacedKey UNLIMITED_SET_KEY = key("test", "unlimited_set");
    private static final NamespacedKey INT_KEY = key("test", "int_stat");

    private final List<Event> firedEvents = Collections.synchronizedList(new ArrayList<>());
    private CorePlayer mockPlayer;
    private PlayerStatisticData data;

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();
        StatisticRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.STATISTIC);

        registry.register(new SimpleStatistic(UNLIMITED_SET_KEY, StatisticType.SET_STRING,
                new LinkedHashSet<String>(), "Unlimited Set", "Set with no size limit"));
        registry.register(new SimpleStatistic(INT_KEY, StatisticType.INT, 0, "Int", "Int stat"));

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

    @Nested
    @DisplayName("addToSet with unlimited max size")
    class AddToSetUnlimitedTests {

        @Test
        @DisplayName("Given a set statistic with unlimited max size, when adding elements, then no eviction occurs")
        void addToSet_doesNotEvict_whenMaxSizeIsUnlimited() {
            for (int i = 0; i < 10; i++) {
                assertTrue(data.addToSet(UNLIMITED_SET_KEY, "element_" + i));
            }
            Set<String> result = data.getSetValue(UNLIMITED_SET_KEY).get();
            assertEquals(10, result.size());
            for (int i = 0; i < 10; i++) {
                assertTrue(result.contains("element_" + i));
            }
        }

        @Test
        @DisplayName("Given a set statistic with unlimited max size, when adding a duplicate, then returns false")
        void addToSet_returnsFalse_whenDuplicateAndUnlimited() {
            data.addToSet(UNLIMITED_SET_KEY, "element");
            firedEvents.clear();
            assertFalse(data.addToSet(UNLIMITED_SET_KEY, "element"));
            assertTrue(firedEvents.isEmpty());
        }

        @Test
        @DisplayName("Given a set statistic with unlimited max size, when adding many elements, then all are preserved")
        void addToSet_preservesAllElements_whenUnlimitedAndLargeSet() {
            for (int i = 0; i < 50; i++) {
                data.addToSet(UNLIMITED_SET_KEY, "item_" + i);
            }
            Set<String> result = data.getSetValue(UNLIMITED_SET_KEY).get();
            assertEquals(50, result.size());
        }
    }

    @Nested
    @DisplayName("getModifiedEntries edge cases")
    class GetModifiedEntriesEdgeCases {

        @Test
        @DisplayName("Given a dirty key with non-null value whose statistic was unregistered, when getting modified entries, then excludes the key")
        void getModifiedEntries_excludesKey_whenValuePresentButStatisticUnregistered() {
            data.setValue(INT_KEY, 42);
            assertTrue(data.isDirty());

            RegistryResetExtension.resetRegistry();
            RegistryResetExtension.setupRegistry();

            Map<NamespacedKey, StatisticEntry> modified = data.getModifiedEntries();
            assertTrue(modified.isEmpty());
        }

        @Test
        @DisplayName("Given dirty keys with mixed registration status, when getting modified entries, then includes only registered keys")
        void getModifiedEntries_includesOnlyRegistered_whenMixedRegistration() {
            NamespacedKey tempKey = key("test", "temp_stat");
            StatisticRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.STATISTIC);
            registry.register(new SimpleStatistic(tempKey, StatisticType.INT, 0, "Temp", "Temp stat"));

            data.setValue(INT_KEY, 10);
            data.setValue(tempKey, 20);
            assertTrue(data.isDirty());

            RegistryResetExtension.resetRegistry();
            RegistryResetExtension.setupRegistry();
            StatisticRegistry newRegistry = RegistryAccess.registryAccess().registry(RegistryKey.STATISTIC);
            newRegistry.register(new SimpleStatistic(INT_KEY, StatisticType.INT, 0, "Int", "Int stat"));

            Map<NamespacedKey, StatisticEntry> modified = data.getModifiedEntries();
            assertEquals(1, modified.size());
            assertTrue(modified.containsKey(INT_KEY));
            assertFalse(modified.containsKey(tempKey));
        }
    }

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

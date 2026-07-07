package com.diamonddagger590.mccore.event.statistic;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.statistic.SimpleStatistic;
import com.diamonddagger590.mccore.statistic.Statistic;
import com.diamonddagger590.mccore.statistic.StatisticType;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class PostStatisticModifyEventTest {

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

    @Test
    @DisplayName("Given valid arguments, when constructing, then getCorePlayer returns the player")
    void getCorePlayer_returnsExpectedPlayer_whenConstructed() {
        CorePlayer player = testPlayer();
        PostStatisticModifyEvent event = new PostStatisticModifyEvent(
                player, key("int"), intStat(), 0, 42, ModificationType.SET);
        assertSame(player, event.getCorePlayer());
    }

    @Test
    @DisplayName("Given valid arguments, when constructing, then getStatisticKey returns the key")
    void getStatisticKey_returnsExpectedKey_whenConstructed() {
        NamespacedKey statKey = key("int");
        PostStatisticModifyEvent event = new PostStatisticModifyEvent(
                testPlayer(), statKey, intStat(), 0, 42, ModificationType.SET);
        assertEquals(statKey, event.getStatisticKey());
    }

    @Test
    @DisplayName("Given valid arguments, when constructing, then getStatistic returns the statistic definition")
    void getStatistic_returnsExpectedStatistic_whenConstructed() {
        Statistic stat = intStat();
        PostStatisticModifyEvent event = new PostStatisticModifyEvent(
                testPlayer(), key("int"), stat, 0, 42, ModificationType.SET);
        assertSame(stat, event.getStatistic());
    }

    @Test
    @DisplayName("Given valid arguments, when constructing, then getOldValue returns the old value")
    void getOldValue_returnsExpectedOldValue_whenConstructed() {
        PostStatisticModifyEvent event = new PostStatisticModifyEvent(
                testPlayer(), key("int"), intStat(), 10, 42, ModificationType.SET);
        assertEquals(10, event.getOldValue());
    }

    @Test
    @DisplayName("Given valid arguments, when constructing, then getNewValue returns the new value")
    void getNewValue_returnsExpectedNewValue_whenConstructed() {
        PostStatisticModifyEvent event = new PostStatisticModifyEvent(
                testPlayer(), key("int"), intStat(), 0, 42, ModificationType.SET);
        assertEquals(42, event.getNewValue());
    }

    @Test
    @DisplayName("Given valid arguments, when constructing, then getModificationType returns the type")
    void getModificationType_returnsExpectedType_whenConstructed() {
        PostStatisticModifyEvent event = new PostStatisticModifyEvent(
                testPlayer(), key("int"), intStat(), 0, 42, ModificationType.INCREMENT);
        assertEquals(ModificationType.INCREMENT, event.getModificationType());
    }

    @Test
    @DisplayName("Given an event, when getting handler list, then static and instance lists match")
    void getHandlers_matchesStaticHandlerList_whenCalled() {
        PostStatisticModifyEvent event = new PostStatisticModifyEvent(
                testPlayer(), key("int"), intStat(), 0, 42, ModificationType.SET);
        assertSame(PostStatisticModifyEvent.getHandlerList(), event.getHandlers());
    }

    @Test
    @DisplayName("Given different modification types, when constructing events, then each returns correct type")
    void constructor_preservesAllModificationTypes_whenEachTypeUsed() {
        for (ModificationType type : ModificationType.values()) {
            PostStatisticModifyEvent event = new PostStatisticModifyEvent(
                    testPlayer(), key("int"), intStat(), 0, 1, type);
            assertEquals(type, event.getModificationType());
        }
    }

    @Test
    @DisplayName("Given Instant values, when constructing TIMESTAMP event, then values are preserved")
    void constructor_preservesInstantValues_whenTimestampType() {
        Instant oldVal = Instant.parse("2024-01-01T00:00:00Z");
        Instant newVal = Instant.parse("2024-06-15T12:30:00Z");
        Statistic stat = new SimpleStatistic(key("ts"), StatisticType.TIMESTAMP, Instant.EPOCH, "TS", "Timestamp");
        PostStatisticModifyEvent event = new PostStatisticModifyEvent(
                testPlayer(), key("ts"), stat, oldVal, newVal, ModificationType.SET);
        assertEquals(oldVal, event.getOldValue());
        assertEquals(newVal, event.getNewValue());
    }
}

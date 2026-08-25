package com.diamonddagger590.mccore.statistic;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.event.statistic.StatisticModifyEvent;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.player.PlayerManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.CoreManagerKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link PlayerStatisticData} production constructor, which wires event
 * dispatching through Bukkit's PluginManager and resolves the CorePlayer via
 * the ManagerRegistry/PlayerManager chain. All existing tests use the test
 * constructor with injectable dependencies — these tests cover the 3 production
 * constructor lambdas and the defensive null-value branch in getModifiedEntries.
 */
class PlayerStatisticDataProductionConstructorTest {

    @SuppressWarnings("deprecation")
    private static NamespacedKey key(String namespace, String key) {
        return new NamespacedKey(namespace, key);
    }

    private static final NamespacedKey INT_KEY = key("test", "prod_int");

    private ServerMock server;
    private TestCorePlugin plugin;
    private PlayerManager<TestCorePlugin, TestCorePlayer> playerManager;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestCorePlugin.class);
        RegistryResetExtension.setupRegistry();

        StatisticRegistry statRegistry = RegistryAccess.registryAccess().registry(RegistryKey.STATISTIC);
        statRegistry.register(new SimpleStatistic(INT_KEY, StatisticType.INT, 0, "Int", "Int stat"));

        playerManager = new PlayerManager<>(plugin);
        ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        managerRegistry.register(playerManager);
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Production constructor dispatches events through Bukkit PluginManager")
    void productionConstructor_dispatchesEventViaBukkitPluginManager() {
        Player bukkitPlayer = server.addPlayer();
        UUID uuid = bukkitPlayer.getUniqueId();
        TestCorePlayer corePlayer = new TestCorePlayer(uuid, plugin);
        playerManager.addPlayer(corePlayer);

        AtomicReference<StatisticModifyEvent> captured = new AtomicReference<>();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onStatModify(StatisticModifyEvent event) {
                captured.set(event);
            }
        }, plugin);

        PlayerStatisticData data = new PlayerStatisticData(uuid);
        data.setValue(INT_KEY, 42);

        assertNotNull(captured.get(), "StatisticModifyEvent should have been dispatched through Bukkit");
        assertEquals(INT_KEY, captured.get().getStatisticKey());
        assertEquals(42, data.getIntValue(INT_KEY).orElse(-1));
    }

    @Test
    @DisplayName("Production constructor resolves CorePlayer from PlayerManager via RegistryAccess chain")
    void productionConstructor_resolvesPlayerFromPlayerManager() {
        Player bukkitPlayer = server.addPlayer();
        UUID uuid = bukkitPlayer.getUniqueId();
        TestCorePlayer corePlayer = new TestCorePlayer(uuid, plugin);
        playerManager.addPlayer(corePlayer);

        PlayerStatisticData data = new PlayerStatisticData(uuid);

        AtomicReference<CorePlayer> resolvedPlayer = new AtomicReference<>();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onStatModify(StatisticModifyEvent event) {
                resolvedPlayer.set(event.getCorePlayer());
            }
        }, plugin);

        data.setValue(INT_KEY, 10);

        assertInstanceOf(TestCorePlayer.class, resolvedPlayer.get());
        assertEquals(uuid, resolvedPlayer.get().getUUID());
    }

    @Test
    @DisplayName("Production constructor throws IllegalStateException when player not in PlayerManager")
    void productionConstructor_throwsIllegalStateException_whenPlayerNotInManager() {
        UUID unknownUuid = UUID.randomUUID();
        PlayerStatisticData data = new PlayerStatisticData(unknownUuid);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> data.setValue(INT_KEY, 99));
        assertTrue(ex.getMessage().contains(unknownUuid.toString()));
        assertTrue(ex.getMessage().contains("is not online"));
    }

    @Test
    @DisplayName("getModifiedEntries skips dirty key whose value is null (defensive branch)")
    void getModifiedEntries_skipsDirtyKeyWithNullValue() throws Exception {
        Player bukkitPlayer = server.addPlayer();
        UUID uuid = bukkitPlayer.getUniqueId();
        TestCorePlayer corePlayer = new TestCorePlayer(uuid, plugin);
        playerManager.addPlayer(corePlayer);

        PlayerStatisticData data = new PlayerStatisticData(uuid);
        data.setValue(INT_KEY, 5);
        assertTrue(data.isDirty());

        Field valuesField = PlayerStatisticData.class.getDeclaredField("values");
        valuesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        var values = (java.util.concurrent.ConcurrentHashMap<NamespacedKey, Object>) valuesField.get(data);
        values.remove(INT_KEY);

        Map<NamespacedKey, StatisticEntry> modified = data.getModifiedEntries();
        assertFalse(modified.containsKey(INT_KEY));
        assertTrue(modified.isEmpty());
    }

    private static class TestCorePlayer extends CorePlayer {

        TestCorePlayer(@NotNull UUID uuid, @NotNull CorePlugin plugin) {
            super(uuid, plugin);
        }

        @Override
        public boolean useMutex() {
            return false;
        }
    }
}

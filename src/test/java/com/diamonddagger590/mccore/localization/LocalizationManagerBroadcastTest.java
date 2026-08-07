package com.diamonddagger590.mccore.localization;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.player.PlayerManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import com.diamonddagger590.mccore.util.LinkedNode;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocalizationManagerBroadcastTest {

    private static class TestCorePlayer extends CorePlayer {
        private final Player bukkitPlayer;

        TestCorePlayer(@NotNull UUID uuid, @NotNull CorePlugin plugin, Player bukkitPlayer) {
            super(uuid, plugin);
            this.bukkitPlayer = bukkitPlayer;
        }

        @Override
        public boolean useMutex() {
            return false;
        }

        @Override
        @NotNull
        public Optional<Player> getAsBukkitPlayer() {
            return Optional.ofNullable(bukkitPlayer);
        }
    }

    private static class TestLocalizationManager extends LocalizationManager<CorePlugin, TestCorePlayer> {

        private final LinkedNode<Locale> defaultChain;

        TestLocalizationManager(@NotNull CorePlugin plugin, @NotNull LinkedNode<Locale> chain) {
            super(plugin);
            this.defaultChain = chain;
        }

        @Override
        @NotNull
        protected ReloadableContent<LinkedNode<Locale>> generateLocaleChain() {
            if (defaultChain == null) {
                LinkedNode<Locale> sentinel = new LinkedNode<>(Locale.ENGLISH);
                LinkedNode<Locale> englishNode = new LinkedNode<>(Locale.ENGLISH, sentinel);
                return new ReloadableContent<>(mock(YamlDocument.class), Route.from("locale"), (doc, route) -> englishNode, englishNode);
            }
            return new ReloadableContent<>(
                    mock(YamlDocument.class),
                    Route.from("locale"),
                    (doc, route) -> defaultChain,
                    defaultChain
            );
        }
    }

    private ServerMock server;
    private TestCorePlugin plugin;
    private TestLocalizationManager localizationManager;
    private YamlDocument englishDoc;
    private Route testRoute;
    private PlayerManager<CorePlugin, TestCorePlayer> playerManager;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestCorePlugin.class);
        RegistryResetExtension.setupRegistry();

        ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        managerRegistry.register(new ReloadableContentManager(plugin));

        LinkedNode<Locale> sentinel = new LinkedNode<>(Locale.ENGLISH);
        LinkedNode<Locale> chain = new LinkedNode<>(Locale.ENGLISH, sentinel);
        localizationManager = new TestLocalizationManager(plugin, chain);

        englishDoc = mock(YamlDocument.class);
        testRoute = Route.from("messages", "broadcast_test");

        Localization localization = mock(Localization.class);
        when(localization.getLocale()).thenReturn(Locale.ENGLISH);
        when(localization.getConfigurationFile()).thenReturn(englishDoc);
        localizationManager.registerLanguageFile(localization);

        playerManager = new PlayerManager<>(plugin);
        managerRegistry.register(playerManager);
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
        MockBukkit.unmock();
    }

    private String componentToPlain(@NotNull Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Nested
    @DisplayName("broadcastMessage(Route)")
    class BroadcastMessageRoute {

        @Test
        @DisplayName("Loaded player receives message via their locale chain")
        void loadedPlayerReceivesLocalizedMessage() {
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Server announcement");

            PlayerMock bukkitPlayer = server.addPlayer();
            TestCorePlayer corePlayer = new TestCorePlayer(bukkitPlayer.getUniqueId(), plugin, bukkitPlayer);
            playerManager.addPlayer(corePlayer);

            localizationManager.broadcastMessage(testRoute);

            String msg = bukkitPlayer.nextMessage();
            assertNotNull(msg, "Player should have received a message");
            assertEquals("Server announcement", msg);
        }

        @Test
        @DisplayName("Unloaded player receives message via default locale")
        void unloadedPlayerReceivesDefaultMessage() {
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Default broadcast");

            PlayerMock bukkitPlayer = server.addPlayer();

            localizationManager.broadcastMessage(testRoute);

            String msg = bukkitPlayer.nextMessage();
            assertNotNull(msg, "Unloaded player should still receive a message");
            assertEquals("Default broadcast", msg);
        }

        @Test
        @DisplayName("Console receives message via default locale")
        void consoleReceivesDefaultMessage() {
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Console broadcast");

            localizationManager.broadcastMessage(testRoute);

            String msg = server.getConsoleSender().nextMessage();
            assertNotNull(msg, "Console should have received a message");
            assertEquals("Console broadcast", msg);
        }

        @Test
        @DisplayName("Mix of loaded and unloaded players all receive messages")
        void mixedPlayersBothReceiveMessages() {
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Mixed broadcast");

            PlayerMock loadedBukkitPlayer = server.addPlayer();
            TestCorePlayer loadedCorePlayer = new TestCorePlayer(loadedBukkitPlayer.getUniqueId(), plugin, loadedBukkitPlayer);
            playerManager.addPlayer(loadedCorePlayer);

            PlayerMock unloadedBukkitPlayer = server.addPlayer();

            localizationManager.broadcastMessage(testRoute);

            String loadedMsg = loadedBukkitPlayer.nextMessage();
            String unloadedMsg = unloadedBukkitPlayer.nextMessage();
            assertNotNull(loadedMsg, "Loaded player should receive message");
            assertNotNull(unloadedMsg, "Unloaded player should receive message");
            assertEquals("Mixed broadcast", loadedMsg);
            assertEquals("Mixed broadcast", unloadedMsg);
        }

        @Test
        @DisplayName("No online players sends only to console")
        void noOnlinePlayersSendsToConsole() {
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Console only");

            localizationManager.broadcastMessage(testRoute);

            String msg = server.getConsoleSender().nextMessage();
            assertNotNull(msg, "Console should receive message even with no online players");
            assertEquals("Console only", msg);
        }
    }

    @Nested
    @DisplayName("broadcastMessage(Route, Map)")
    class BroadcastMessageRouteWithPlaceholders {

        @Test
        @DisplayName("Loaded player receives message with placeholders substituted")
        void loadedPlayerReceivesPlaceholderMessage() {
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Hello <name>, you have <count> items");

            PlayerMock bukkitPlayer = server.addPlayer();
            TestCorePlayer corePlayer = new TestCorePlayer(bukkitPlayer.getUniqueId(), plugin, bukkitPlayer);
            playerManager.addPlayer(corePlayer);

            localizationManager.broadcastMessage(testRoute, Map.of("name", "Steve", "count", "5"));

            Component msg = bukkitPlayer.nextComponentMessage();
            assertNotNull(msg, "Player should have received a message");
            assertEquals("Hello Steve, you have 5 items", componentToPlain(msg));
        }

        @Test
        @DisplayName("Unloaded player receives message with placeholders substituted via default locale")
        void unloadedPlayerReceivesPlaceholderMessage() {
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Welcome <player>");

            PlayerMock bukkitPlayer = server.addPlayer();

            localizationManager.broadcastMessage(testRoute, Map.of("player", "Alex"));

            Component msg = bukkitPlayer.nextComponentMessage();
            assertNotNull(msg, "Unloaded player should receive message");
            assertEquals("Welcome Alex", componentToPlain(msg));
        }

        @Test
        @DisplayName("Console receives message with placeholders substituted")
        void consoleReceivesPlaceholderMessage() {
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Event: <event>");

            localizationManager.broadcastMessage(testRoute, Map.of("event", "Raid"));

            Component msg = server.getConsoleSender().nextComponentMessage();
            assertNotNull(msg, "Console should receive message");
            assertEquals("Event: Raid", componentToPlain(msg));
        }

        @Test
        @DisplayName("Empty placeholders map sends message without substitution")
        void emptyPlaceholdersPassesThrough() {
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("No placeholders here");

            PlayerMock bukkitPlayer = server.addPlayer();
            TestCorePlayer corePlayer = new TestCorePlayer(bukkitPlayer.getUniqueId(), plugin, bukkitPlayer);
            playerManager.addPlayer(corePlayer);

            localizationManager.broadcastMessage(testRoute, Map.of());

            Component msg = bukkitPlayer.nextComponentMessage();
            assertNotNull(msg, "Player should receive message");
            assertEquals("No placeholders here", componentToPlain(msg));
        }

        @Test
        @DisplayName("Mix of loaded and unloaded players both receive placeholder messages")
        void mixedPlayersReceivePlaceholderMessages() {
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Score: <score>");

            PlayerMock loadedBukkitPlayer = server.addPlayer();
            TestCorePlayer corePlayer = new TestCorePlayer(loadedBukkitPlayer.getUniqueId(), plugin, loadedBukkitPlayer);
            playerManager.addPlayer(corePlayer);

            PlayerMock unloadedBukkitPlayer = server.addPlayer();

            localizationManager.broadcastMessage(testRoute, Map.of("score", "100"));

            Component loadedMsg = loadedBukkitPlayer.nextComponentMessage();
            Component unloadedMsg = unloadedBukkitPlayer.nextComponentMessage();
            assertNotNull(loadedMsg, "Loaded player should receive message");
            assertNotNull(unloadedMsg, "Unloaded player should receive message");
            assertEquals("Score: 100", componentToPlain(loadedMsg));
            assertEquals("Score: 100", componentToPlain(unloadedMsg));
        }
    }
}

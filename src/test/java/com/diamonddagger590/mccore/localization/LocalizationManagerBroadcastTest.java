package com.diamonddagger590.mccore.localization;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.external.papi.CorePapiHook;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.player.PlayerManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.CoreManagerKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry;
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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocalizationManagerBroadcastTest {

    private static class TestCorePlayer extends CorePlayer {
        TestCorePlayer(@NotNull UUID uuid, @NotNull CorePlugin plugin) {
            super(uuid, plugin);
        }

        @Override
        public boolean useMutex() {
            return false;
        }
    }

    private static class TestLocalizationManager extends LocalizationManager<TestCorePlugin, TestCorePlayer> {

        private final LinkedNode<Locale> defaultChain;

        TestLocalizationManager(@NotNull TestCorePlugin plugin, @NotNull LinkedNode<Locale> chain) {
            super(plugin);
            this.defaultChain = chain;
        }

        @Override
        @NotNull
        protected ReloadableContent<LinkedNode<Locale>> generateLocaleChain() {
            if (defaultChain == null) {
                LinkedNode<Locale> sentinel = new LinkedNode<>(Locale.ENGLISH);
                LinkedNode<Locale> englishNode = new LinkedNode<>(Locale.ENGLISH, sentinel);
                return new ReloadableContent<>(mock(YamlDocument.class), Route.from("locale"),
                        (doc, route) -> englishNode, englishNode);
            }
            return new ReloadableContent<>(mock(YamlDocument.class), Route.from("locale"),
                    (doc, route) -> defaultChain, defaultChain);
        }
    }

    private ServerMock server;
    private TestCorePlugin plugin;
    private TestLocalizationManager localizationManager;
    private PlayerManager<TestCorePlugin, TestCorePlayer> playerManager;
    private YamlDocument englishDoc;
    private Route testRoute;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestCorePlugin.class);
        RegistryResetExtension.setupRegistry();

        ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        managerRegistry.register(new ReloadableContentManager(plugin));

        playerManager = new PlayerManager<>(plugin);
        managerRegistry.register(playerManager);

        LinkedNode<Locale> sentinel = new LinkedNode<>(Locale.ENGLISH);
        LinkedNode<Locale> chain = new LinkedNode<>(Locale.ENGLISH, sentinel);
        localizationManager = new TestLocalizationManager(plugin, chain);

        englishDoc = mock(YamlDocument.class);
        testRoute = Route.from("messages", "broadcast_test");
        registerEnglishDoc();
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
        MockBukkit.unmock();
    }

    private void registerEnglishDoc() {
        Localization localization = mock(Localization.class);
        when(localization.getLocale()).thenReturn(Locale.ENGLISH);
        when(localization.getConfigurationFile()).thenReturn(englishDoc);
        localizationManager.registerLanguageFile(localization);
    }

    private static String plainText(@NotNull Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Nested
    @DisplayName("broadcastMessage(Route)")
    class BroadcastMessageRoute {

        @Test
        @DisplayName("Given no online players, when broadcasting, then completes without error")
        void broadcastMessage_noOnlinePlayers_completesWithoutError() {
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Server announcement");

            assertDoesNotThrow(() -> localizationManager.broadcastMessage(testRoute));
        }

        @Test
        @DisplayName("Given a loaded player online, when broadcasting, then player receives the player-localized message")
        void broadcastMessage_loadedPlayer_receivesMessage() {
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Hello everyone");

            PlayerMock bukkitPlayer = server.addPlayer();
            TestCorePlayer corePlayer = new TestCorePlayer(bukkitPlayer.getUniqueId(), plugin);
            playerManager.addPlayer(corePlayer);

            localizationManager.broadcastMessage(testRoute);

            Component received = bukkitPlayer.nextComponentMessage();
            assertNotNull(received);
            assertEquals("Hello everyone", plainText(received));
        }

        @Test
        @DisplayName("Given an unloaded player online, when broadcasting, then player receives the default locale message")
        void broadcastMessage_unloadedPlayer_receivesDefaultMessage() {
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Default broadcast");

            PlayerMock unloadedPlayer = server.addPlayer();

            localizationManager.broadcastMessage(testRoute);

            Component received = unloadedPlayer.nextComponentMessage();
            assertNotNull(received);
            assertEquals("Default broadcast", plainText(received));
        }

        @Test
        @DisplayName("Given both loaded and unloaded players, when broadcasting, then each receives the message")
        void broadcastMessage_mixedPlayers_eachReceivesMessage() {
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Mixed broadcast");

            PlayerMock loadedBukkit = server.addPlayer();
            TestCorePlayer corePlayer = new TestCorePlayer(loadedBukkit.getUniqueId(), plugin);
            playerManager.addPlayer(corePlayer);

            PlayerMock unloadedBukkit = server.addPlayer();

            localizationManager.broadcastMessage(testRoute);

            Component loadedReceived = loadedBukkit.nextComponentMessage();
            assertNotNull(loadedReceived);
            assertEquals("Mixed broadcast", plainText(loadedReceived));

            Component unloadedReceived = unloadedBukkit.nextComponentMessage();
            assertNotNull(unloadedReceived);
            assertEquals("Mixed broadcast", plainText(unloadedReceived));
        }
    }

    @Nested
    @DisplayName("broadcastMessage(Route, Map)")
    class BroadcastMessageRouteWithPlaceholders {

        @Test
        @DisplayName("Given no online players with placeholders, when broadcasting, then completes without error")
        void broadcastMessage_noOnlinePlayers_completesWithoutError() {
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Hello <name>");

            assertDoesNotThrow(() ->
                    localizationManager.broadcastMessage(testRoute, Map.of("name", "World")));
        }

        @Test
        @DisplayName("Given a loaded player with placeholders, when broadcasting, then player receives the substituted message")
        void broadcastMessage_loadedPlayer_receivesComponentWithPlaceholders() {
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Welcome <player_name>");

            PlayerMock bukkitPlayer = server.addPlayer();
            TestCorePlayer corePlayer = new TestCorePlayer(bukkitPlayer.getUniqueId(), plugin);
            playerManager.addPlayer(corePlayer);

            localizationManager.broadcastMessage(testRoute, Map.of("player_name", "Steve"));

            Component received = bukkitPlayer.nextComponentMessage();
            assertNotNull(received);
            assertEquals("Welcome Steve", plainText(received));
        }

        @Test
        @DisplayName("Given an unloaded player with placeholders, when broadcasting, then player receives the substituted message")
        void broadcastMessage_unloadedPlayer_receivesSubstitutedMessage() {
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Announcement: <info>");

            PlayerMock unloadedPlayer = server.addPlayer();

            localizationManager.broadcastMessage(testRoute, Map.of("info", "server restart"));

            Component received = unloadedPlayer.nextComponentMessage();
            assertNotNull(received);
            assertEquals("Announcement: server restart", plainText(received));
        }

        @Test
        @DisplayName("Given both loaded and unloaded players with placeholders, when broadcasting, then each receives the substituted message")
        void broadcastMessage_mixedPlayers_eachReceivesSubstitutedMessage() {
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Event: <event>");

            PlayerMock loadedBukkit = server.addPlayer();
            TestCorePlayer corePlayer = new TestCorePlayer(loadedBukkit.getUniqueId(), plugin);
            playerManager.addPlayer(corePlayer);

            PlayerMock unloadedBukkit = server.addPlayer();

            localizationManager.broadcastMessage(testRoute, Map.of("event", "PvP Tournament"));

            Component loadedReceived = loadedBukkit.nextComponentMessage();
            assertNotNull(loadedReceived);
            assertEquals("Event: PvP Tournament", plainText(loadedReceived));

            Component unloadedReceived = unloadedBukkit.nextComponentMessage();
            assertNotNull(unloadedReceived);
            assertEquals("Event: PvP Tournament", plainText(unloadedReceived));
        }

        @Test
        @DisplayName("Given an empty placeholders map, when broadcasting, then message is sent with original text")
        void broadcastMessage_emptyPlaceholders_sendsOriginalText() {
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("No placeholders here");

            PlayerMock bukkitPlayer = server.addPlayer();
            TestCorePlayer corePlayer = new TestCorePlayer(bukkitPlayer.getUniqueId(), plugin);
            playerManager.addPlayer(corePlayer);

            localizationManager.broadcastMessage(testRoute, Map.of());

            Component received = bukkitPlayer.nextComponentMessage();
            assertNotNull(received);
            assertEquals("No placeholders here", plainText(received));
        }
    }

    @Nested
    @DisplayName("PAPI integration branches")
    class PapiIntegration {

        private void registerMockPapiHook() throws Exception {
            CorePapiHook mockPapi = mock(CorePapiHook.class);
            when(mockPapi.translateMessage(any(), anyString())).thenAnswer(
                    invocation -> "PAPI:" + invocation.getArgument(1, String.class));

            PluginHookRegistry hookRegistry = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK);
            Field hooksField = PluginHookRegistry.class.getDeclaredField("hooks");
            hooksField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Class<?>, Object> hooks = (Map<Class<?>, Object>) hooksField.get(hookRegistry);
            hooks.put(CorePapiHook.class, mockPapi);
        }

        @Test
        @DisplayName("Given PAPI hook is registered and player is online, when getLocalizedMessage(player, route), then PAPI translates the message")
        void getLocalizedMessage_withPapi_translatesMessage() throws Exception {
            registerMockPapiHook();

            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Hello %player_name%");

            Player bukkitPlayer = server.addPlayer();
            TestCorePlayer corePlayer = new TestCorePlayer(bukkitPlayer.getUniqueId(), plugin);

            String result = localizationManager.getLocalizedMessage(corePlayer, testRoute);
            assertEquals("PAPI:Hello %player_name%", result);
        }

        @Test
        @DisplayName("Given PAPI hook is registered and player is online, when getLocalizedMessages(player, route), then PAPI translates each line")
        void getLocalizedMessages_withPapi_translatesEachLine() throws Exception {
            registerMockPapiHook();

            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getStringList(testRoute)).thenReturn(List.of("Line %one%", "Line %two%"));

            Player bukkitPlayer = server.addPlayer();
            TestCorePlayer corePlayer = new TestCorePlayer(bukkitPlayer.getUniqueId(), plugin);

            List<String> result = localizationManager.getLocalizedMessages(corePlayer, testRoute);
            assertEquals(2, result.size());
            assertEquals("PAPI:Line %one%", result.get(0));
            assertEquals("PAPI:Line %two%", result.get(1));
        }

        @Test
        @DisplayName("Given PAPI hook is registered but player is offline, when getLocalizedMessage(player, route), then PAPI is skipped")
        void getLocalizedMessage_withPapi_offlinePlayer_skipsPapi() throws Exception {
            registerMockPapiHook();

            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Hello %player_name%");

            TestCorePlayer offlinePlayer = new TestCorePlayer(UUID.randomUUID(), plugin);

            String result = localizationManager.getLocalizedMessage(offlinePlayer, testRoute);
            assertEquals("Hello %player_name%", result);
        }

        @Test
        @DisplayName("Given PAPI hook is registered but player is offline, when getLocalizedMessages(player, route), then PAPI is skipped for each line")
        void getLocalizedMessages_withPapi_offlinePlayer_skipsPapi() throws Exception {
            registerMockPapiHook();

            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getStringList(testRoute)).thenReturn(List.of("Line %one%", "Line %two%"));

            TestCorePlayer offlinePlayer = new TestCorePlayer(UUID.randomUUID(), plugin);

            List<String> result = localizationManager.getLocalizedMessages(offlinePlayer, testRoute);
            assertEquals(2, result.size());
            assertEquals("Line %one%", result.get(0));
            assertEquals("Line %two%", result.get(1));
        }
    }

    @Nested
    @DisplayName("getLocalizedMessages(Audience, Route) additional branches")
    class GetLocalizedMessagesAudienceBranch {

        @Test
        @DisplayName("Given audience is a Player not in PlayerManager, when getLocalizedMessages(audience, route), then uses default locale")
        void getLocalizedMessages_audiencePlayerNotLoaded_usesDefault() {
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getStringList(testRoute)).thenReturn(List.of("Default list msg"));

            Player bukkitPlayer = server.addPlayer();

            List<String> result = localizationManager.getLocalizedMessages(bukkitPlayer, testRoute);
            assertEquals(1, result.size());
            assertEquals("Default list msg", result.get(0));
        }
    }
}

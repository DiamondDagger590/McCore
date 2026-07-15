package com.diamonddagger590.mccore.localization;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.external.papi.CorePapiHook;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.player.PlayerManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import com.diamonddagger590.mccore.util.LinkedNode;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    private static class BroadcastTestLocalizationManager extends LocalizationManager<CorePlugin, TestCorePlayer> {

        private final LinkedNode<Locale> defaultChain;

        BroadcastTestLocalizationManager(@NotNull CorePlugin plugin, @NotNull LinkedNode<Locale> chain) {
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
            return new ReloadableContent<>(mock(YamlDocument.class), Route.from("locale"), (doc, route) -> defaultChain, defaultChain);
        }
    }

    private ServerMock server;
    private TestCorePlugin plugin;
    private BroadcastTestLocalizationManager localizationManager;
    private YamlDocument englishDoc;
    private Route testRoute;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestCorePlugin.class);
        RegistryResetExtension.setupRegistry();

        ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(plugin);
        managerRegistry.register(reloadableContentManager);

        LinkedNode<Locale> sentinel = new LinkedNode<>(Locale.ENGLISH);
        LinkedNode<Locale> chain = new LinkedNode<>(Locale.ENGLISH, sentinel);
        localizationManager = new BroadcastTestLocalizationManager(plugin, chain);

        englishDoc = mock(YamlDocument.class);
        testRoute = Route.from("messages", "broadcast");
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

    private PlayerManager<CorePlugin, TestCorePlayer> registerPlayerManager() {
        PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(plugin);
        ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        managerRegistry.register(playerManager);
        return playerManager;
    }

    private void registerPapiHook(@NotNull CorePapiHook papiHook) throws Exception {
        PluginHookRegistry hookRegistry = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK);
        Field hooksField = PluginHookRegistry.class.getDeclaredField("hooks");
        hooksField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Class<?>, Object> hooks = (Map<Class<?>, Object>) hooksField.get(hookRegistry);
        hooks.put(CorePapiHook.class, papiHook);
    }

    // --- broadcastMessage ---

    @Test
    @DisplayName("Given online players with loaded CorePlayers, when broadcastMessage called, then all players receive localized message")
    void broadcastMessage_sendsToAllOnlineLoadedPlayers() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Hello everyone!");

        PlayerManager<CorePlugin, TestCorePlayer> playerManager = registerPlayerManager();

        PlayerMock player1 = server.addPlayer();
        PlayerMock player2 = server.addPlayer();
        playerManager.addPlayer(new TestCorePlayer(player1.getUniqueId(), plugin, player1));
        playerManager.addPlayer(new TestCorePlayer(player2.getUniqueId(), plugin, player2));

        localizationManager.broadcastMessage(testRoute);

        player1.assertSaid("Hello everyone!");
        player2.assertSaid("Hello everyone!");
    }

    @Test
    @DisplayName("Given online players without loaded CorePlayers, when broadcastMessage called, then players receive message using default locale")
    void broadcastMessage_usesDefaultLocale_whenPlayersNotLoaded() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Default message");

        registerPlayerManager();

        PlayerMock player1 = server.addPlayer();

        localizationManager.broadcastMessage(testRoute);

        player1.assertSaid("Default message");
    }

    @Test
    @DisplayName("Given mix of loaded and unloaded players, when broadcastMessage called, then all receive appropriate messages")
    void broadcastMessage_handlesMixOfLoadedAndUnloadedPlayers() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Broadcast msg");

        PlayerManager<CorePlugin, TestCorePlayer> playerManager = registerPlayerManager();

        PlayerMock loadedPlayer = server.addPlayer();
        PlayerMock unloadedPlayer = server.addPlayer();
        playerManager.addPlayer(new TestCorePlayer(loadedPlayer.getUniqueId(), plugin, loadedPlayer));

        localizationManager.broadcastMessage(testRoute);

        loadedPlayer.assertSaid("Broadcast msg");
        unloadedPlayer.assertSaid("Broadcast msg");
    }

    @Test
    @DisplayName("Given no online players, when broadcastMessage called, then console still receives the message")
    void broadcastMessage_sendsToConsole_whenNoPlayersOnline() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Console message");

        registerPlayerManager();

        assertDoesNotThrow(() -> localizationManager.broadcastMessage(testRoute));
    }

    // --- PAPI integration in getLocalizedMessage(player, route) ---

    @Test
    @DisplayName("Given PAPI hook registered and player present, when getLocalizedMessage(player, route) called, then PAPI translates the message")
    void getLocalizedMessage_player_appliesPapiTranslation_whenHookRegistered() throws Exception {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Hello %player_name%");

        CorePapiHook mockPapiHook = mock(CorePapiHook.class);
        when(mockPapiHook.translateMessage(any(), anyString())).thenReturn("Hello TestPlayer");
        registerPapiHook(mockPapiHook);

        PlayerMock bukkitPlayer = server.addPlayer();
        TestCorePlayer corePlayer = new TestCorePlayer(bukkitPlayer.getUniqueId(), plugin, bukkitPlayer);

        String result = localizationManager.getLocalizedMessage(corePlayer, testRoute);
        assertEquals("Hello TestPlayer", result);
    }

    @Test
    @DisplayName("Given PAPI hook registered but player has no Bukkit player, when getLocalizedMessage called, then PAPI is skipped")
    void getLocalizedMessage_player_skipsPapi_whenNoBukkitPlayer() throws Exception {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Hello %player_name%");

        CorePapiHook mockPapiHook = mock(CorePapiHook.class);
        registerPapiHook(mockPapiHook);

        TestCorePlayer corePlayer = new TestCorePlayer(UUID.randomUUID(), plugin, null);

        String result = localizationManager.getLocalizedMessage(corePlayer, testRoute);
        assertEquals("Hello %player_name%", result);
    }

    @Test
    @DisplayName("Given no PAPI hook registered, when getLocalizedMessage(player, route) called, then returns raw message")
    void getLocalizedMessage_player_returnsRawMessage_whenNoPapiHook() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Hello %player_name%");

        PlayerMock bukkitPlayer = server.addPlayer();
        TestCorePlayer corePlayer = new TestCorePlayer(bukkitPlayer.getUniqueId(), plugin, bukkitPlayer);

        String result = localizationManager.getLocalizedMessage(corePlayer, testRoute);
        assertEquals("Hello %player_name%", result);
    }

    // --- PAPI integration in getLocalizedMessages(player, route) ---

    @Test
    @DisplayName("Given PAPI hook registered and player present, when getLocalizedMessages(player, route) called, then PAPI translates each line")
    void getLocalizedMessages_player_appliesPapiTranslation_whenHookRegistered() throws Exception {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getStringList(testRoute)).thenReturn(List.of("Line1 %player_name%", "Line2 %player_name%"));

        CorePapiHook mockPapiHook = mock(CorePapiHook.class);
        when(mockPapiHook.translateMessage(any(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1, String.class).replace("%player_name%", "TestPlayer"));
        registerPapiHook(mockPapiHook);

        PlayerMock bukkitPlayer = server.addPlayer();
        TestCorePlayer corePlayer = new TestCorePlayer(bukkitPlayer.getUniqueId(), plugin, bukkitPlayer);

        List<String> result = localizationManager.getLocalizedMessages(corePlayer, testRoute);
        assertEquals(2, result.size());
        assertEquals("Line1 TestPlayer", result.get(0));
        assertEquals("Line2 TestPlayer", result.get(1));
    }

    @Test
    @DisplayName("Given PAPI hook registered but no Bukkit player, when getLocalizedMessages(player, route) called, then returns raw messages")
    void getLocalizedMessages_player_skipsPapi_whenNoBukkitPlayer() throws Exception {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getStringList(testRoute)).thenReturn(List.of("Line1 %player_name%", "Line2 %player_name%"));

        CorePapiHook mockPapiHook = mock(CorePapiHook.class);
        registerPapiHook(mockPapiHook);

        TestCorePlayer corePlayer = new TestCorePlayer(UUID.randomUUID(), plugin, null);

        List<String> result = localizationManager.getLocalizedMessages(corePlayer, testRoute);
        assertEquals(2, result.size());
        assertEquals("Line1 %player_name%", result.get(0));
        assertEquals("Line2 %player_name%", result.get(1));
    }

    // --- postProcessResolvedString default implementation ---

    @Test
    @DisplayName("Given default postProcessResolvedString, when getLocalizedMessage(route) called, then returns unmodified message")
    void postProcessResolvedString_defaultIdentity_returnsUnmodifiedMessage() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Unchanged message");

        String result = localizationManager.getLocalizedMessage(testRoute);
        assertEquals("Unchanged message", result);
    }

    @Test
    @DisplayName("Given default postProcessResolvedString, when getLocalizedMessage(player, route) called, then returns unmodified message")
    void postProcessResolvedString_defaultIdentity_returnsUnmodifiedMessage_playerOverload() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("<primary>Special text");

        PlayerMock bukkitPlayer = server.addPlayer();
        TestCorePlayer corePlayer = new TestCorePlayer(bukkitPlayer.getUniqueId(), plugin, bukkitPlayer);

        String result = localizationManager.getLocalizedMessage(corePlayer, testRoute);
        assertEquals("<primary>Special text", result);
    }
}

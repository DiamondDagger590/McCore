package com.diamonddagger590.mccore.localization;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.exception.localization.NoLocalizationContainsMessageException;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.player.PlayerManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.CoreManagerKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry;
import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.setting.PlayerSettingRegistry;
import com.diamonddagger590.mccore.statistic.StatisticRegistry;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.util.LinkedNode;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.mockito.ArgumentCaptor;

class LocalizationManagerTest {

    private static class TestCorePlayer extends CorePlayer {
        private final Player bukkitPlayer;

        TestCorePlayer(UUID uuid, CorePlugin plugin, Player bukkitPlayer) {
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
        private String postProcessResult = null;

        TestLocalizationManager(CorePlugin plugin, LinkedNode<Locale> chain) {
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

        void setPostProcessResult(String result) {
            this.postProcessResult = result;
        }

        @Override
        @NotNull
        protected String postProcessResolvedString(@NotNull String raw) {
            return postProcessResult != null ? postProcessResult : raw;
        }
    }

    private CorePlugin mockPlugin;
    private TestLocalizationManager localizationManager;
    private YamlDocument englishDoc;
    private Route testRoute;

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();

        mockPlugin = mock(CorePlugin.class);
        when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());
        when(mockPlugin.getMiniMessage()).thenReturn(MiniMessage.miniMessage());

        ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mockPlugin);
        managerRegistry.register(reloadableContentManager);

        LinkedNode<Locale> sentinel = new LinkedNode<>(Locale.ENGLISH);
        LinkedNode<Locale> chain = new LinkedNode<>(Locale.ENGLISH, sentinel);
        localizationManager = new TestLocalizationManager(mockPlugin, chain);

        englishDoc = mock(YamlDocument.class);
        testRoute = Route.from("messages", "greeting");
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
    }

    private void registerEnglishDoc() {
        Localization localization = mock(Localization.class);
        when(localization.getLocale()).thenReturn(Locale.ENGLISH);
        when(localization.getConfigurationFile()).thenReturn(englishDoc);
        localizationManager.registerLanguageFile(localization);
    }

    private TestCorePlayer createPlayerWithLocale(Locale locale) {
        UUID uuid = UUID.randomUUID();
        Player bukkitPlayer = mock(Player.class);
        when(bukkitPlayer.locale()).thenReturn(locale);
        return new TestCorePlayer(uuid, mockPlugin, bukkitPlayer);
    }

    private TestCorePlayer createPlayerWithoutBukkit() {
        UUID uuid = UUID.randomUUID();
        return new TestCorePlayer(uuid, mockPlugin, null);
    }

    // --- registerLanguageFile ---

    @Test
    @DisplayName("Given a localization, when registering, then messages from that locale are resolvable")
    void registerLanguageFile_makesMessagesResolvable() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Hello");

        String result = localizationManager.getLocalizedMessage(testRoute);
        assertEquals("Hello", result);
    }

    @Test
    @DisplayName("Given multiple docs for same locale, when registering, then both are searched")
    void registerLanguageFile_multipleDocsForSameLocale() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(false);

        YamlDocument secondDoc = mock(YamlDocument.class);
        Route otherRoute = Route.from("messages", "farewell");
        when(secondDoc.contains(otherRoute)).thenReturn(true);
        when(secondDoc.getString(otherRoute)).thenReturn("Goodbye");

        Localization secondLocalization = mock(Localization.class);
        when(secondLocalization.getLocale()).thenReturn(Locale.ENGLISH);
        when(secondLocalization.getConfigurationFile()).thenReturn(secondDoc);
        localizationManager.registerLanguageFile(secondLocalization);

        assertEquals("Goodbye", localizationManager.getLocalizedMessage(otherRoute));
    }

    // --- getLocalizedMessage(Route) ---

    @Test
    @DisplayName("Given English locale has message, when getLocalizedMessage(route), then returns it")
    void getLocalizedMessage_route_returnsEnglishMessage() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Test message");

        assertEquals("Test message", localizationManager.getLocalizedMessage(testRoute));
    }

    @Test
    @DisplayName("Given no locale contains route, when getLocalizedMessage(route), then throws")
    void getLocalizedMessage_route_throwsWhenMissing() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(false);

        assertThrows(NoLocalizationContainsMessageException.class,
                () -> localizationManager.getLocalizedMessage(testRoute));
    }

    @Test
    @DisplayName("Given no localizations registered, when getLocalizedMessage(route), then throws")
    void getLocalizedMessage_route_throwsWhenNoLocalizations() {
        assertThrows(NoLocalizationContainsMessageException.class,
                () -> localizationManager.getLocalizedMessage(testRoute));
    }

    // --- getLocalizedMessage(CorePlayer, Route) ---

    @Test
    @DisplayName("Given player's client locale has message, when getLocalizedMessage(player, route), then returns from client locale")
    void getLocalizedMessage_player_usesClientLocale() {
        YamlDocument frenchDoc = mock(YamlDocument.class);
        when(frenchDoc.contains(testRoute)).thenReturn(true);
        when(frenchDoc.getString(testRoute)).thenReturn("Bonjour");

        Localization frenchLocalization = mock(Localization.class);
        when(frenchLocalization.getLocale()).thenReturn(Locale.FRENCH);
        when(frenchLocalization.getConfigurationFile()).thenReturn(frenchDoc);
        localizationManager.registerLanguageFile(frenchLocalization);

        TestCorePlayer player = createPlayerWithLocale(Locale.FRENCH);

        assertEquals("Bonjour", localizationManager.getLocalizedMessage(player, testRoute));
    }

    @Test
    @DisplayName("Given player's client locale missing message, when getLocalizedMessage(player, route), then falls back to chain")
    void getLocalizedMessage_player_fallsBackToChain() {
        YamlDocument frenchDoc = mock(YamlDocument.class);
        when(frenchDoc.contains(testRoute)).thenReturn(false);

        Localization frenchLocalization = mock(Localization.class);
        when(frenchLocalization.getLocale()).thenReturn(Locale.FRENCH);
        when(frenchLocalization.getConfigurationFile()).thenReturn(frenchDoc);
        localizationManager.registerLanguageFile(frenchLocalization);

        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Hello fallback");

        TestCorePlayer player = createPlayerWithLocale(Locale.FRENCH);

        assertEquals("Hello fallback", localizationManager.getLocalizedMessage(player, testRoute));
    }

    @Test
    @DisplayName("Given player without Bukkit player, when getLocalizedMessage(player, route), then uses chain locale")
    void getLocalizedMessage_player_noBukkitPlayer_usesChainLocale() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Default message");

        TestCorePlayer player = createPlayerWithoutBukkit();

        assertEquals("Default message", localizationManager.getLocalizedMessage(player, testRoute));
    }

    @Test
    @DisplayName("Given duplicate locale in chain, when getLocalizedMessage, then skips duplicates")
    void getLocalizedMessage_player_skipsDuplicateLocalesInChain() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("English");

        TestCorePlayer player = createPlayerWithLocale(Locale.ENGLISH);

        assertEquals("English", localizationManager.getLocalizedMessage(player, testRoute));
    }

    @Test
    @DisplayName("Given no locale in chain has message, when getLocalizedMessage(player, route), then throws")
    void getLocalizedMessage_player_throwsWhenMissing() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(false);

        TestCorePlayer player = createPlayerWithLocale(Locale.ENGLISH);

        assertThrows(NoLocalizationContainsMessageException.class,
                () -> localizationManager.getLocalizedMessage(player, testRoute));
    }

    // --- getLocalizedMessage with placeholders ---

    @Test
    @DisplayName("Given a message with placeholders, when getLocalizedMessage(route, placeholders), then substitutes them")
    void getLocalizedMessage_routeWithPlaceholders_substitutes() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Hello <name>, you have <count> items");

        String result = localizationManager.getLocalizedMessage(testRoute, Map.of("name", "Steve", "count", "5"));
        assertEquals("Hello Steve, you have 5 items", result);
    }

    @Test
    @DisplayName("Given a message with placeholders, when getLocalizedMessage(player, route, placeholders), then substitutes them")
    void getLocalizedMessage_playerRouteWithPlaceholders_substitutes() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Welcome <player>");

        TestCorePlayer player = createPlayerWithLocale(Locale.ENGLISH);
        String result = localizationManager.getLocalizedMessage(player, testRoute, Map.of("player", "Alex"));
        assertEquals("Welcome Alex", result);
    }

    // --- getLocalizedMessage(Audience, Route) ---

    @Test
    @DisplayName("Given audience is a Player with a stored CorePlayer, when getLocalizedMessage(audience, route), then uses player's locale chain")
    void getLocalizedMessage_audience_usesPlayerLocaleChain() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Player message");

        UUID uuid = UUID.randomUUID();
        Player bukkitPlayer = mock(Player.class);
        when(bukkitPlayer.getUniqueId()).thenReturn(uuid);
        when(bukkitPlayer.locale()).thenReturn(Locale.ENGLISH);

        TestCorePlayer corePlayer = new TestCorePlayer(uuid, mockPlugin, bukkitPlayer);

        PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
        playerManager.addPlayer(corePlayer);

        ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        managerRegistry.register(playerManager);

        assertEquals("Player message", localizationManager.getLocalizedMessage(bukkitPlayer, testRoute));
    }

    @Test
    @DisplayName("Given audience is a Player without stored CorePlayer, when getLocalizedMessage(audience, route), then uses default locale")
    void getLocalizedMessage_audience_noStoredCorePlayer_usesDefault() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Default message");

        UUID uuid = UUID.randomUUID();
        Player bukkitPlayer = mock(Player.class);
        when(bukkitPlayer.getUniqueId()).thenReturn(uuid);

        PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
        ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        managerRegistry.register(playerManager);

        assertEquals("Default message", localizationManager.getLocalizedMessage(bukkitPlayer, testRoute));
    }

    @Test
    @DisplayName("Given audience is a Player with placeholders, when getLocalizedMessage(audience, route, placeholders), then substitutes")
    void getLocalizedMessage_audience_withPlaceholders() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Hello <name>");

        UUID uuid = UUID.randomUUID();
        Player bukkitPlayer = mock(Player.class);
        when(bukkitPlayer.getUniqueId()).thenReturn(uuid);

        PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
        ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        managerRegistry.register(playerManager);

        assertEquals("Hello World", localizationManager.getLocalizedMessage(bukkitPlayer, testRoute, Map.of("name", "World")));
    }

    // --- getLocalizedMessages (list) ---

    @Test
    @DisplayName("Given a string list route, when getLocalizedMessages(route), then returns all strings")
    void getLocalizedMessages_route_returnsStringList() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getStringList(testRoute)).thenReturn(List.of("Line 1", "Line 2", "Line 3"));

        List<String> result = localizationManager.getLocalizedMessages(testRoute);
        assertEquals(3, result.size());
        assertEquals("Line 1", result.get(0));
        assertEquals("Line 3", result.get(2));
    }

    @Test
    @DisplayName("Given no locale contains route for list, when getLocalizedMessages(route), then throws")
    void getLocalizedMessages_route_throwsWhenMissing() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(false);

        assertThrows(NoLocalizationContainsMessageException.class,
                () -> localizationManager.getLocalizedMessages(testRoute));
    }

    @Test
    @DisplayName("Given player and string list route, when getLocalizedMessages(player, route), then returns all strings")
    void getLocalizedMessages_player_returnsStringList() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getStringList(testRoute)).thenReturn(List.of("Hello", "World"));

        TestCorePlayer player = createPlayerWithLocale(Locale.ENGLISH);

        List<String> result = localizationManager.getLocalizedMessages(player, testRoute);
        assertEquals(2, result.size());
        assertEquals("Hello", result.get(0));
    }

    @Test
    @DisplayName("Given player with no matching locale, when getLocalizedMessages(player, route), then throws")
    void getLocalizedMessages_player_throwsWhenMissing() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(false);

        TestCorePlayer player = createPlayerWithLocale(Locale.ENGLISH);

        assertThrows(NoLocalizationContainsMessageException.class,
                () -> localizationManager.getLocalizedMessages(player, testRoute));
    }

    @Test
    @DisplayName("Given audience is Player, when getLocalizedMessages(audience, route), then resolves via player manager")
    void getLocalizedMessages_audience_resolvesViaPlayerManager() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getStringList(testRoute)).thenReturn(List.of("Message 1"));

        UUID uuid = UUID.randomUUID();
        Player bukkitPlayer = mock(Player.class);
        when(bukkitPlayer.getUniqueId()).thenReturn(uuid);
        when(bukkitPlayer.locale()).thenReturn(Locale.ENGLISH);

        TestCorePlayer corePlayer = new TestCorePlayer(uuid, mockPlugin, bukkitPlayer);
        PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
        playerManager.addPlayer(corePlayer);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

        List<String> result = localizationManager.getLocalizedMessages(bukkitPlayer, testRoute);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Given audience is not a Player, when getLocalizedMessages(audience, route), then uses default locale")
    void getLocalizedMessages_audience_notPlayer_usesDefault() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getStringList(testRoute)).thenReturn(List.of("Console msg"));

        PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

        net.kyori.adventure.audience.Audience nonPlayerAudience = mock(net.kyori.adventure.audience.Audience.class);
        List<String> result = localizationManager.getLocalizedMessages(nonPlayerAudience, testRoute);
        assertEquals(1, result.size());
        assertEquals("Console msg", result.get(0));
    }

    // --- getLocalizedMessages with placeholders (player/audience) ---

    @Test
    @DisplayName("Given player and list route with placeholders, when getLocalizedMessages is not a list variant, then placeholders are applied via getLocalizedMessage")
    void getLocalizedMessages_playerWithPlaceholders_notAvailable() {
        // LocalizationManager does not have a getLocalizedMessages(player, route, map) overload
        // that returns List<String> with string placeholders — placeholder substitution on lists
        // is done via the Component variants (getLocalizedMessageAsComponents).
        // This test documents that the string-list path for player applies postProcess only.
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getStringList(testRoute)).thenReturn(List.of("Item <name>", "Count <count>"));

        TestCorePlayer player = createPlayerWithLocale(Locale.ENGLISH);
        List<String> result = localizationManager.getLocalizedMessages(player, testRoute);
        assertEquals(2, result.size());
        assertEquals("Item <name>", result.get(0));
        assertEquals("Count <count>", result.get(1));
    }

    // --- doesAnyLocaleContainRoute ---

    @Test
    @DisplayName("Given locale has route, when doesAnyLocaleContainRoute, then returns true")
    void doesAnyLocaleContainRoute_returnsTrue_whenPresent() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);

        TestCorePlayer player = createPlayerWithLocale(Locale.ENGLISH);
        assertTrue(localizationManager.doesAnyLocaleContainRoute(player, testRoute));
    }

    @Test
    @DisplayName("Given no locale has route, when doesAnyLocaleContainRoute, then returns false")
    void doesAnyLocaleContainRoute_returnsFalse_whenAbsent() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(false);

        TestCorePlayer player = createPlayerWithLocale(Locale.ENGLISH);
        assertFalse(localizationManager.doesAnyLocaleContainRoute(player, testRoute));
    }

    @Test
    @DisplayName("Given no localization registered for locale, when doesAnyLocaleContainRoute, then returns false")
    void doesAnyLocaleContainRoute_returnsFalse_whenLocaleNotRegistered() {
        TestCorePlayer player = createPlayerWithLocale(Locale.JAPANESE);
        assertFalse(localizationManager.doesAnyLocaleContainRoute(player, testRoute));
    }

    // --- getLocalizedSection ---

    @Test
    @DisplayName("Given section exists for route, when getLocalizedSection(player, route), then returns it")
    void getLocalizedSection_player_returnsSection() {
        registerEnglishDoc();
        Section mockSection = mock(Section.class);
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getSection(testRoute)).thenReturn(mockSection);

        TestCorePlayer player = createPlayerWithLocale(Locale.ENGLISH);
        assertEquals(mockSection, localizationManager.getLocalizedSection(player, testRoute));
    }

    @Test
    @DisplayName("Given no section exists for route, when getLocalizedSection(player, route), then throws")
    void getLocalizedSection_player_throwsWhenMissing() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(false);

        TestCorePlayer player = createPlayerWithLocale(Locale.ENGLISH);
        assertThrows(NoLocalizationContainsMessageException.class,
                () -> localizationManager.getLocalizedSection(player, testRoute));
    }

    @Test
    @DisplayName("Given section exists for route, when getLocalizedSection(route), then returns it")
    void getLocalizedSection_route_returnsSection() {
        registerEnglishDoc();
        Section mockSection = mock(Section.class);
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getSection(testRoute)).thenReturn(mockSection);

        assertEquals(mockSection, localizationManager.getLocalizedSection(testRoute));
    }

    @Test
    @DisplayName("Given no section exists for route, when getLocalizedSection(route), then throws")
    void getLocalizedSection_route_throwsWhenMissing() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(false);

        assertThrows(NoLocalizationContainsMessageException.class,
                () -> localizationManager.getLocalizedSection(testRoute));
    }

    @Test
    @DisplayName("Given no localizations at all, when getLocalizedSection(route), then throws")
    void getLocalizedSection_route_throwsWhenNoLocalizations() {
        assertThrows(NoLocalizationContainsMessageException.class,
                () -> localizationManager.getLocalizedSection(testRoute));
    }

    // --- postProcessResolvedString ---

    @Test
    @DisplayName("Given postProcess override, when resolving message, then post-processing is applied")
    void postProcessResolvedString_isApplied() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("raw message");

        localizationManager.setPostProcessResult("processed message");

        assertEquals("processed message", localizationManager.getLocalizedMessage(testRoute));
    }

    @Test
    @DisplayName("Given postProcess override, when resolving list messages, then post-processing is applied to each")
    void postProcessResolvedString_isAppliedToListMessages() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getStringList(testRoute)).thenReturn(List.of("line 1", "line 2"));

        localizationManager.setPostProcessResult("processed");

        List<String> result = localizationManager.getLocalizedMessages(testRoute);
        assertEquals(2, result.size());
        assertEquals("processed", result.get(0));
        assertEquals("processed", result.get(1));
    }

    // --- getLocaleChain ---

    @Test
    @DisplayName("Given player with client locale, when getLocaleChain, then client locale is first")
    void getLocaleChain_clientLocaleFirst() {
        TestCorePlayer player = createPlayerWithLocale(Locale.FRENCH);
        LinkedNode<Locale> chain = localizationManager.getLocaleChain(player);

        assertEquals(Locale.FRENCH, chain.getNodeValue());
        assertTrue(chain.hasNext());
    }

    @Test
    @DisplayName("Given player without Bukkit player, when getLocaleChain, then uses default chain")
    void getLocaleChain_noBukkitPlayer_usesDefault() {
        TestCorePlayer player = createPlayerWithoutBukkit();
        LinkedNode<Locale> chain = localizationManager.getLocaleChain(player);

        assertEquals(Locale.ENGLISH, chain.getNodeValue());
    }

    // --- getLocalizedMessageAsComponent ---

    @Test
    @DisplayName("Given a message, when getLocalizedMessageAsComponent(route), then returns non-null Component")
    void getLocalizedMessageAsComponent_returnsComponent() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Hello world");

        assertNotNull(localizationManager.getLocalizedMessageAsComponent(testRoute));
    }

    @Test
    @DisplayName("Given a player and message, when getLocalizedMessageAsComponent(player, route), then returns component")
    void getLocalizedMessageAsComponent_player_returnsComponent() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Hello");

        TestCorePlayer player = createPlayerWithLocale(Locale.ENGLISH);
        assertNotNull(localizationManager.getLocalizedMessageAsComponent(player, testRoute));
    }

    @Test
    @DisplayName("Given placeholders, when getLocalizedMessageAsComponent(route, placeholders), then returns component")
    void getLocalizedMessageAsComponent_withPlaceholders_returnsComponent() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Hello <name>");

        assertNotNull(localizationManager.getLocalizedMessageAsComponent(testRoute, Map.of("name", "World")));
    }

    @Test
    @DisplayName("Given player and placeholders, when getLocalizedMessageAsComponent(player, route, placeholders), then returns component")
    void getLocalizedMessageAsComponent_playerWithPlaceholders_returnsComponent() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Hi <name>");

        TestCorePlayer player = createPlayerWithLocale(Locale.ENGLISH);
        assertNotNull(localizationManager.getLocalizedMessageAsComponent(player, testRoute, Map.of("name", "Steve")));
    }

    @Test
    @DisplayName("Given audience, when getLocalizedMessageAsComponent(audience, route), then returns component")
    void getLocalizedMessageAsComponent_audience_returnsComponent() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Hi");

        PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

        net.kyori.adventure.audience.Audience audience = mock(net.kyori.adventure.audience.Audience.class);
        assertNotNull(localizationManager.getLocalizedMessageAsComponent(audience, testRoute));
    }

    @Test
    @DisplayName("Given audience and placeholders, when getLocalizedMessageAsComponent(audience, route, placeholders), then returns component")
    void getLocalizedMessageAsComponent_audienceWithPlaceholders_returnsComponent() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Hi <name>");

        PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

        net.kyori.adventure.audience.Audience audience = mock(net.kyori.adventure.audience.Audience.class);
        assertNotNull(localizationManager.getLocalizedMessageAsComponent(audience, testRoute, Map.of("name", "World")));
    }

    // --- getLocalizedMessageAsComponents (list) ---

    @Test
    @DisplayName("Given a list route, when getLocalizedMessageAsComponents(route), then returns list of Components")
    void getLocalizedMessageAsComponents_route_returnsList() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getStringList(testRoute)).thenReturn(List.of("Line 1", "Line 2"));

        List<?> result = localizationManager.getLocalizedMessageAsComponents(testRoute);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Given a player and list route, when getLocalizedMessageAsComponents(player, route), then returns list")
    void getLocalizedMessageAsComponents_player_returnsList() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getStringList(testRoute)).thenReturn(List.of("A", "B"));

        TestCorePlayer player = createPlayerWithLocale(Locale.ENGLISH);
        assertEquals(2, localizationManager.getLocalizedMessageAsComponents(player, testRoute).size());
    }

    @Test
    @DisplayName("Given audience and list route, when getLocalizedMessageAsComponents(audience, route), then returns list")
    void getLocalizedMessageAsComponents_audience_returnsList() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getStringList(testRoute)).thenReturn(List.of("X"));

        PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

        net.kyori.adventure.audience.Audience audience = mock(net.kyori.adventure.audience.Audience.class);
        assertEquals(1, localizationManager.getLocalizedMessageAsComponents(audience, testRoute).size());
    }

    @Test
    @DisplayName("Given a list route with placeholders, when getLocalizedMessageAsComponents(route, placeholders), then returns list")
    void getLocalizedMessageAsComponents_routeWithPlaceholders_returnsList() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getStringList(testRoute)).thenReturn(List.of("Hello <name>"));

        assertEquals(1, localizationManager.getLocalizedMessageAsComponents(testRoute, Map.of("name", "Steve")).size());
    }

    @Test
    @DisplayName("Given player and list route with placeholders, when getLocalizedMessageAsComponents(player, route, placeholders), then returns list")
    void getLocalizedMessageAsComponents_playerWithPlaceholders_returnsList() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getStringList(testRoute)).thenReturn(List.of("Hi <name>"));

        TestCorePlayer player = createPlayerWithLocale(Locale.ENGLISH);
        assertEquals(1, localizationManager.getLocalizedMessageAsComponents(player, testRoute, Map.of("name", "X")).size());
    }

    @Test
    @DisplayName("Given audience and list route with placeholders, when getLocalizedMessageAsComponents(audience, route, placeholders), then returns list")
    void getLocalizedMessageAsComponents_audienceWithPlaceholders_returnsList() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getStringList(testRoute)).thenReturn(List.of("A <name>"));

        PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

        net.kyori.adventure.audience.Audience audience = mock(net.kyori.adventure.audience.Audience.class);
        assertEquals(1, localizationManager.getLocalizedMessageAsComponents(audience, testRoute, Map.of("name", "Y")).size());
    }

    // --- Locale chain priority ---

    @Test
    @DisplayName("Given client locale and chain locale both have message, when resolving, then client locale wins")
    void localeChain_clientLocaleHasPriority() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("English");

        YamlDocument germanDoc = mock(YamlDocument.class);
        when(germanDoc.contains(testRoute)).thenReturn(true);
        when(germanDoc.getString(testRoute)).thenReturn("Deutsch");

        Localization germanLocalization = mock(Localization.class);
        when(germanLocalization.getLocale()).thenReturn(Locale.GERMAN);
        when(germanLocalization.getConfigurationFile()).thenReturn(germanDoc);
        localizationManager.registerLanguageFile(germanLocalization);

        TestCorePlayer player = createPlayerWithLocale(Locale.GERMAN);

        assertEquals("Deutsch", localizationManager.getLocalizedMessage(player, testRoute));
    }

    // --- Unsupported locale in chain ---

    @Test
    @DisplayName("Given client locale is unsupported, when resolving, then falls through to English")
    void localeChain_unsupportedClientLocale_fallsToEnglish() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("English fallback");

        TestCorePlayer player = createPlayerWithLocale(Locale.KOREAN);

        assertEquals("English fallback", localizationManager.getLocalizedMessage(player, testRoute));
    }

    // --- broadcastMessage(Route) ---

    @Test
    @DisplayName("broadcastMessage sends localized message to loaded player and console")
    void broadcastMessage_route_sendsToLoadedPlayerAndConsole() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Broadcast hello");

        PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

        UUID uuid = UUID.randomUUID();
        Player bukkitPlayer = mock(Player.class);
        when(bukkitPlayer.getUniqueId()).thenReturn(uuid);
        when(bukkitPlayer.locale()).thenReturn(Locale.ENGLISH);
        TestCorePlayer corePlayer = new TestCorePlayer(uuid, mockPlugin, bukkitPlayer);
        playerManager.addPlayer(corePlayer);

        ConsoleCommandSender consoleSender = mock(ConsoleCommandSender.class);

        try (MockedStatic<Bukkit> bukkitStatic = mockStatic(Bukkit.class)) {
            bukkitStatic.when(Bukkit::getOnlinePlayers).thenReturn((Collection) List.of(bukkitPlayer));
            bukkitStatic.when(Bukkit::getConsoleSender).thenReturn(consoleSender);

            localizationManager.broadcastMessage(testRoute);

            verify(bukkitPlayer).sendMessage("Broadcast hello");
            verify(consoleSender).sendMessage("Broadcast hello");
        }
    }

    @Test
    @DisplayName("broadcastMessage sends default locale message to unloaded player")
    void broadcastMessage_route_sendsDefaultToUnloadedPlayer() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Default broadcast");

        PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

        UUID uuid = UUID.randomUUID();
        Player bukkitPlayer = mock(Player.class);
        when(bukkitPlayer.getUniqueId()).thenReturn(uuid);
        when(bukkitPlayer.locale()).thenReturn(Locale.ENGLISH);

        ConsoleCommandSender consoleSender = mock(ConsoleCommandSender.class);

        try (MockedStatic<Bukkit> bukkitStatic = mockStatic(Bukkit.class)) {
            bukkitStatic.when(Bukkit::getOnlinePlayers).thenReturn((Collection) List.of(bukkitPlayer));
            bukkitStatic.when(Bukkit::getConsoleSender).thenReturn(consoleSender);

            localizationManager.broadcastMessage(testRoute);

            verify(bukkitPlayer).sendMessage("Default broadcast");
            verify(consoleSender).sendMessage("Default broadcast");
        }
    }

    @Test
    @DisplayName("broadcastMessage with no online players only sends to console")
    void broadcastMessage_route_noPlayers_sendsOnlyToConsole() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Console only");

        PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

        ConsoleCommandSender consoleSender = mock(ConsoleCommandSender.class);

        try (MockedStatic<Bukkit> bukkitStatic = mockStatic(Bukkit.class)) {
            bukkitStatic.when(Bukkit::getOnlinePlayers).thenReturn((Collection) List.of());
            bukkitStatic.when(Bukkit::getConsoleSender).thenReturn(consoleSender);

            localizationManager.broadcastMessage(testRoute);

            verify(consoleSender).sendMessage("Console only");
        }
    }

    @Test
    @DisplayName("broadcastMessage sends to both loaded and unloaded players in a single call")
    void broadcastMessage_route_sendsToMixedLoadedAndUnloadedPlayers() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Mixed broadcast");

        PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

        UUID loadedUuid = UUID.randomUUID();
        Player loadedBukkitPlayer = mock(Player.class);
        when(loadedBukkitPlayer.getUniqueId()).thenReturn(loadedUuid);
        when(loadedBukkitPlayer.locale()).thenReturn(Locale.ENGLISH);
        TestCorePlayer loadedCorePlayer = new TestCorePlayer(loadedUuid, mockPlugin, loadedBukkitPlayer);
        playerManager.addPlayer(loadedCorePlayer);

        UUID unloadedUuid = UUID.randomUUID();
        Player unloadedBukkitPlayer = mock(Player.class);
        when(unloadedBukkitPlayer.getUniqueId()).thenReturn(unloadedUuid);
        when(unloadedBukkitPlayer.locale()).thenReturn(Locale.ENGLISH);

        ConsoleCommandSender consoleSender = mock(ConsoleCommandSender.class);

        try (MockedStatic<Bukkit> bukkitStatic = mockStatic(Bukkit.class)) {
            bukkitStatic.when(Bukkit::getOnlinePlayers).thenReturn((Collection) List.of(loadedBukkitPlayer, unloadedBukkitPlayer));
            bukkitStatic.when(Bukkit::getConsoleSender).thenReturn(consoleSender);

            localizationManager.broadcastMessage(testRoute);

            verify(loadedBukkitPlayer).sendMessage("Mixed broadcast");
            verify(unloadedBukkitPlayer).sendMessage("Mixed broadcast");
            verify(consoleSender).sendMessage("Mixed broadcast");
        }
    }

    // --- broadcastMessage(Route, Map<String, String>) ---

    @Test
    @DisplayName("broadcastMessage with placeholders sends resolved message to loaded player and console")
    void broadcastMessage_routeWithPlaceholders_sendsToLoadedPlayerAndConsole() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Hello <name>");

        PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

        UUID uuid = UUID.randomUUID();
        Player bukkitPlayer = mock(Player.class);
        when(bukkitPlayer.getUniqueId()).thenReturn(uuid);
        when(bukkitPlayer.locale()).thenReturn(Locale.ENGLISH);
        TestCorePlayer corePlayer = new TestCorePlayer(uuid, mockPlugin, bukkitPlayer);
        playerManager.addPlayer(corePlayer);

        ConsoleCommandSender consoleSender = mock(ConsoleCommandSender.class);

        try (MockedStatic<Bukkit> bukkitStatic = mockStatic(Bukkit.class)) {
            bukkitStatic.when(Bukkit::getOnlinePlayers).thenReturn((Collection) List.of(bukkitPlayer));
            bukkitStatic.when(Bukkit::getConsoleSender).thenReturn(consoleSender);

            localizationManager.broadcastMessage(testRoute, Map.of("name", "Steve"));

            ArgumentCaptor<Component> playerCaptor = ArgumentCaptor.forClass(Component.class);
            verify(bukkitPlayer).sendMessage(playerCaptor.capture());
            assertEquals("Hello Steve", PlainTextComponentSerializer.plainText().serialize(playerCaptor.getValue()));

            ArgumentCaptor<Component> consoleCaptor = ArgumentCaptor.forClass(Component.class);
            verify(consoleSender).sendMessage(consoleCaptor.capture());
            assertEquals("Hello Steve", PlainTextComponentSerializer.plainText().serialize(consoleCaptor.getValue()));
        }
    }

    @Test
    @DisplayName("broadcastMessage with placeholders sends default locale to unloaded player")
    void broadcastMessage_routeWithPlaceholders_sendsDefaultToUnloadedPlayer() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Hi <name>");

        PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

        UUID uuid = UUID.randomUUID();
        Player bukkitPlayer = mock(Player.class);
        when(bukkitPlayer.getUniqueId()).thenReturn(uuid);

        ConsoleCommandSender consoleSender = mock(ConsoleCommandSender.class);

        try (MockedStatic<Bukkit> bukkitStatic = mockStatic(Bukkit.class)) {
            bukkitStatic.when(Bukkit::getOnlinePlayers).thenReturn((Collection) List.of(bukkitPlayer));
            bukkitStatic.when(Bukkit::getConsoleSender).thenReturn(consoleSender);

            localizationManager.broadcastMessage(testRoute, Map.of("name", "Alex"));

            ArgumentCaptor<Component> playerCaptor = ArgumentCaptor.forClass(Component.class);
            verify(bukkitPlayer).sendMessage(playerCaptor.capture());
            assertEquals("Hi Alex", PlainTextComponentSerializer.plainText().serialize(playerCaptor.getValue()));

            ArgumentCaptor<Component> consoleCaptor = ArgumentCaptor.forClass(Component.class);
            verify(consoleSender).sendMessage(consoleCaptor.capture());
            assertEquals("Hi Alex", PlainTextComponentSerializer.plainText().serialize(consoleCaptor.getValue()));
        }
    }

    @Test
    @DisplayName("broadcastMessage with placeholders and no online players only sends to console")
    void broadcastMessage_routeWithPlaceholders_noPlayers_sendsOnlyToConsole() {
        registerEnglishDoc();
        when(englishDoc.contains(testRoute)).thenReturn(true);
        when(englishDoc.getString(testRoute)).thenReturn("Msg <name>");

        PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

        ConsoleCommandSender consoleSender = mock(ConsoleCommandSender.class);

        try (MockedStatic<Bukkit> bukkitStatic = mockStatic(Bukkit.class)) {
            bukkitStatic.when(Bukkit::getOnlinePlayers).thenReturn((Collection) List.of());
            bukkitStatic.when(Bukkit::getConsoleSender).thenReturn(consoleSender);

            localizationManager.broadcastMessage(testRoute, Map.of("name", "Nobody"));

            ArgumentCaptor<Component> consoleCaptor = ArgumentCaptor.forClass(Component.class);
            verify(consoleSender).sendMessage(consoleCaptor.capture());
            assertEquals("Msg Nobody", PlainTextComponentSerializer.plainText().serialize(consoleCaptor.getValue()));
        }
    }
}

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
import com.diamonddagger590.mccore.registry.plugin.CorePluginHookKey;
import com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.util.LinkedNode;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalizationManagerBroadcastTest {

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

        TestLocalizationManager(CorePlugin plugin, LinkedNode<Locale> chain) {
            super(plugin);
        }

        @Override
        @NotNull
        protected ReloadableContent<LinkedNode<Locale>> generateLocaleChain() {
            LinkedNode<Locale> sentinel = new LinkedNode<>(Locale.ENGLISH);
            LinkedNode<Locale> englishNode = new LinkedNode<>(Locale.ENGLISH, sentinel);
            return new ReloadableContent<>(mock(YamlDocument.class), Route.from("locale"), (doc, route) -> englishNode, englishNode);
        }
    }

    private CorePlugin mockPlugin;
    private TestLocalizationManager localizationManager;
    private YamlDocument englishDoc;
    private Route testRoute;
    private MockedStatic<Bukkit> bukkitStatic;

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

        bukkitStatic = mockStatic(Bukkit.class);
    }

    @AfterEach
    void tearDown() {
        bukkitStatic.close();
        RegistryResetExtension.resetRegistry();
    }

    private void registerEnglishDoc() {
        Localization localization = mock(Localization.class);
        when(localization.getLocale()).thenReturn(Locale.ENGLISH);
        when(localization.getConfigurationFile()).thenReturn(englishDoc);
        localizationManager.registerLanguageFile(localization);
    }

    @Nested
    @DisplayName("broadcastMessage(Route)")
    class BroadcastMessage {

        @Test
        @DisplayName("Given loaded online player, when broadcastMessage, then sends player-localized message")
        void broadcastMessage_loadedPlayer_sendsLocalizedMessage() {
            registerEnglishDoc();
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Hello all");

            UUID uuid = UUID.randomUUID();
            Player bukkitPlayer = mock(Player.class);
            when(bukkitPlayer.getUniqueId()).thenReturn(uuid);
            when(bukkitPlayer.locale()).thenReturn(Locale.ENGLISH);

            TestCorePlayer corePlayer = new TestCorePlayer(uuid, mockPlugin, bukkitPlayer);
            PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
            playerManager.addPlayer(corePlayer);
            RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

            @SuppressWarnings("unchecked")
            Collection<Player> onlinePlayers = (Collection<Player>) (Collection<?>) List.of(bukkitPlayer);
            bukkitStatic.when(Bukkit::getOnlinePlayers).thenReturn(onlinePlayers);
            ConsoleCommandSender consoleSender = mock(ConsoleCommandSender.class);
            bukkitStatic.when(Bukkit::getConsoleSender).thenReturn(consoleSender);

            localizationManager.broadcastMessage(testRoute);

            verify(bukkitPlayer).sendMessage("Hello all");
            verify(consoleSender).sendMessage("Hello all");
        }

        @Test
        @DisplayName("Given unloaded online player, when broadcastMessage, then sends default-locale message")
        void broadcastMessage_unloadedPlayer_sendsDefaultMessage() {
            registerEnglishDoc();
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Default broadcast");

            UUID uuid = UUID.randomUUID();
            Player bukkitPlayer = mock(Player.class);
            when(bukkitPlayer.getUniqueId()).thenReturn(uuid);

            PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
            RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

            @SuppressWarnings("unchecked")
            Collection<Player> onlinePlayers = (Collection<Player>) (Collection<?>) List.of(bukkitPlayer);
            bukkitStatic.when(Bukkit::getOnlinePlayers).thenReturn(onlinePlayers);
            ConsoleCommandSender consoleSender = mock(ConsoleCommandSender.class);
            bukkitStatic.when(Bukkit::getConsoleSender).thenReturn(consoleSender);

            localizationManager.broadcastMessage(testRoute);

            verify(bukkitPlayer).sendMessage("Default broadcast");
            verify(consoleSender).sendMessage("Default broadcast");
        }

        @Test
        @DisplayName("Given no online players, when broadcastMessage, then only console receives message")
        void broadcastMessage_noOnlinePlayers_onlyConsoleSent() {
            registerEnglishDoc();
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Console only");

            PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
            RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

            @SuppressWarnings("unchecked")
            Collection<Player> onlinePlayers = (Collection<Player>) (Collection<?>) List.of();
            bukkitStatic.when(Bukkit::getOnlinePlayers).thenReturn(onlinePlayers);
            ConsoleCommandSender consoleSender = mock(ConsoleCommandSender.class);
            bukkitStatic.when(Bukkit::getConsoleSender).thenReturn(consoleSender);

            localizationManager.broadcastMessage(testRoute);

            verify(consoleSender).sendMessage("Console only");
        }
    }

    @Nested
    @DisplayName("broadcastMessage(Route, Map)")
    class BroadcastMessageWithPlaceholders {

        @Test
        @DisplayName("Given loaded online player, when broadcastMessage with placeholders, then sends localized component")
        void broadcastMessage_loadedPlayer_sendsComponentWithPlaceholders() {
            registerEnglishDoc();
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Hello <name>");

            UUID uuid = UUID.randomUUID();
            Player bukkitPlayer = mock(Player.class);
            when(bukkitPlayer.getUniqueId()).thenReturn(uuid);
            when(bukkitPlayer.locale()).thenReturn(Locale.ENGLISH);

            TestCorePlayer corePlayer = new TestCorePlayer(uuid, mockPlugin, bukkitPlayer);
            PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
            playerManager.addPlayer(corePlayer);
            RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

            @SuppressWarnings("unchecked")
            Collection<Player> onlinePlayers = (Collection<Player>) (Collection<?>) List.of(bukkitPlayer);
            bukkitStatic.when(Bukkit::getOnlinePlayers).thenReturn(onlinePlayers);
            ConsoleCommandSender consoleSender = mock(ConsoleCommandSender.class);
            bukkitStatic.when(Bukkit::getConsoleSender).thenReturn(consoleSender);

            localizationManager.broadcastMessage(testRoute, Map.of("name", "World"));

            verify(bukkitPlayer).sendMessage(any(net.kyori.adventure.text.Component.class));
            verify(consoleSender).sendMessage(any(net.kyori.adventure.text.Component.class));
        }

        @Test
        @DisplayName("Given unloaded online player, when broadcastMessage with placeholders, then sends default component")
        void broadcastMessage_unloadedPlayer_sendsDefaultComponent() {
            registerEnglishDoc();
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Hi <name>");

            UUID uuid = UUID.randomUUID();
            Player bukkitPlayer = mock(Player.class);
            when(bukkitPlayer.getUniqueId()).thenReturn(uuid);

            PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
            RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

            @SuppressWarnings("unchecked")
            Collection<Player> onlinePlayers = (Collection<Player>) (Collection<?>) List.of(bukkitPlayer);
            bukkitStatic.when(Bukkit::getOnlinePlayers).thenReturn(onlinePlayers);
            ConsoleCommandSender consoleSender = mock(ConsoleCommandSender.class);
            bukkitStatic.when(Bukkit::getConsoleSender).thenReturn(consoleSender);

            localizationManager.broadcastMessage(testRoute, Map.of("name", "Steve"));

            verify(bukkitPlayer).sendMessage(any(net.kyori.adventure.text.Component.class));
            verify(consoleSender).sendMessage(any(net.kyori.adventure.text.Component.class));
        }

        @Test
        @DisplayName("Given no online players, when broadcastMessage with placeholders, then only console receives component")
        void broadcastMessage_noOnlinePlayers_onlyConsoleSentComponent() {
            registerEnglishDoc();
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Hello <name>");

            PlayerManager<CorePlugin, TestCorePlayer> playerManager = new PlayerManager<>(mockPlugin);
            RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(playerManager);

            @SuppressWarnings("unchecked")
            Collection<Player> onlinePlayers = (Collection<Player>) (Collection<?>) List.of();
            bukkitStatic.when(Bukkit::getOnlinePlayers).thenReturn(onlinePlayers);
            ConsoleCommandSender consoleSender = mock(ConsoleCommandSender.class);
            bukkitStatic.when(Bukkit::getConsoleSender).thenReturn(consoleSender);

            localizationManager.broadcastMessage(testRoute, Map.of("name", "World"));

            verify(consoleSender).sendMessage(any(net.kyori.adventure.text.Component.class));
        }
    }

    @Nested
    @DisplayName("PAPI integration branches")
    class PapiIntegration {

        @Test
        @DisplayName("Given PAPI hook present and player online, when getLocalizedMessage(player, route), then PAPI translates message")
        void getLocalizedMessage_papiPresent_translatesMessage() {
            registerEnglishDoc();
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Balance: %vault_eco_balance%");

            UUID uuid = UUID.randomUUID();
            Player bukkitPlayer = mock(Player.class);
            when(bukkitPlayer.getUniqueId()).thenReturn(uuid);
            when(bukkitPlayer.locale()).thenReturn(Locale.ENGLISH);

            TestCorePlayer corePlayer = new TestCorePlayer(uuid, mockPlugin, bukkitPlayer);

            CorePapiHook mockPapiHook = mock(CorePapiHook.class);
            when(mockPapiHook.translateMessage(eq(bukkitPlayer), eq("Balance: %vault_eco_balance%")))
                    .thenReturn("Balance: 1000");

            PluginHookRegistry hookRegistry = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK);
            hookRegistry.register(mockPapiHook);

            String result = localizationManager.getLocalizedMessage(corePlayer, testRoute);
            assertEquals("Balance: 1000", result);
        }

        @Test
        @DisplayName("Given PAPI hook present but player offline, when getLocalizedMessage(player, route), then skips PAPI")
        void getLocalizedMessage_papiPresentButPlayerOffline_skipsPapi() {
            registerEnglishDoc();
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getString(testRoute)).thenReturn("Balance: %vault_eco_balance%");

            TestCorePlayer offlinePlayer = new TestCorePlayer(UUID.randomUUID(), mockPlugin, null);

            CorePapiHook mockPapiHook = mock(CorePapiHook.class);
            PluginHookRegistry hookRegistry = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK);
            hookRegistry.register(mockPapiHook);

            String result = localizationManager.getLocalizedMessage(offlinePlayer, testRoute);
            assertEquals("Balance: %vault_eco_balance%", result);
        }

        @Test
        @DisplayName("Given PAPI hook present and player online, when getLocalizedMessages(player, route), then PAPI translates each line")
        void getLocalizedMessages_papiPresent_translatesEachLine() {
            registerEnglishDoc();
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getStringList(testRoute)).thenReturn(List.of("Line: %placeholder1%", "Line: %placeholder2%"));

            UUID uuid = UUID.randomUUID();
            Player bukkitPlayer = mock(Player.class);
            when(bukkitPlayer.getUniqueId()).thenReturn(uuid);
            when(bukkitPlayer.locale()).thenReturn(Locale.ENGLISH);

            TestCorePlayer corePlayer = new TestCorePlayer(uuid, mockPlugin, bukkitPlayer);

            CorePapiHook mockPapiHook = mock(CorePapiHook.class);
            when(mockPapiHook.translateMessage(eq(bukkitPlayer), eq("Line: %placeholder1%")))
                    .thenReturn("Line: Value1");
            when(mockPapiHook.translateMessage(eq(bukkitPlayer), eq("Line: %placeholder2%")))
                    .thenReturn("Line: Value2");

            PluginHookRegistry hookRegistry = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK);
            hookRegistry.register(mockPapiHook);

            List<String> result = localizationManager.getLocalizedMessages(corePlayer, testRoute);
            assertEquals(2, result.size());
            assertEquals("Line: Value1", result.get(0));
            assertEquals("Line: Value2", result.get(1));
        }

        @Test
        @DisplayName("Given PAPI hook absent, when getLocalizedMessages(player, route), then returns raw strings")
        void getLocalizedMessages_papiAbsent_returnsRawStrings() {
            registerEnglishDoc();
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getStringList(testRoute)).thenReturn(List.of("Line: %placeholder%"));

            UUID uuid = UUID.randomUUID();
            Player bukkitPlayer = mock(Player.class);
            when(bukkitPlayer.getUniqueId()).thenReturn(uuid);
            when(bukkitPlayer.locale()).thenReturn(Locale.ENGLISH);

            TestCorePlayer corePlayer = new TestCorePlayer(uuid, mockPlugin, bukkitPlayer);

            List<String> result = localizationManager.getLocalizedMessages(corePlayer, testRoute);
            assertEquals(1, result.size());
            assertEquals("Line: %placeholder%", result.get(0));
        }

        @Test
        @DisplayName("Given PAPI hook present but player offline, when getLocalizedMessages(player, route), then skips PAPI")
        void getLocalizedMessages_papiPresentButOffline_skipsPapi() {
            registerEnglishDoc();
            when(englishDoc.contains(testRoute)).thenReturn(true);
            when(englishDoc.getStringList(testRoute)).thenReturn(List.of("Raw %placeholder%"));

            TestCorePlayer offlinePlayer = new TestCorePlayer(UUID.randomUUID(), mockPlugin, null);

            CorePapiHook mockPapiHook = mock(CorePapiHook.class);
            PluginHookRegistry hookRegistry = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK);
            hookRegistry.register(mockPapiHook);

            List<String> result = localizationManager.getLocalizedMessages(offlinePlayer, testRoute);
            assertEquals(1, result.size());
            assertEquals("Raw %placeholder%", result.get(0));
        }
    }
}

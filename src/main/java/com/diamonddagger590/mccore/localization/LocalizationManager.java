package com.diamonddagger590.mccore.localization;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.exception.localization.NoLocalizationContainsMessageException;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.player.PlayerManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.CoreManagerKey;
import com.diamonddagger590.mccore.registry.manager.Manager;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import com.diamonddagger590.mccore.registry.plugin.CorePluginHookKey;
import com.diamonddagger590.mccore.util.LinkedNode;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Any messages sent for McRPG should pass through here in order to be translated.
 * <p>
 * Players are able to have multiple locals available to them, called a "locale chain". Locale chains
 * allows searching for translations while having multiple fallback {@link Locale}s supported as it's expected
 * various languages have varying degrees of coverage.
 * <p>
 * A locale chain starts with the player's client {@link Locale}, then followed by the server's default locale, then finally followed by {@link Locale#ENGLISH}. The expectation
 * is that english will be a resilient fallback source of truth. If for some reason the entire locale chain is missing a
 * translation, then {@link NoLocalizationContainsMessageException} will be thrown.
 * <p>
 * Third party plugins can add their own configuration files to be included for localization by using {@link #registerLanguageFile(Localization)}.
 * <p>
 * Subclasses may override {@link #postProcessResolvedString(String)} to apply custom string transformations
 * (such as palette placeholder replacement) to every resolved locale string before it is returned or
 * parsed by MiniMessage. The default implementation is a no-op identity transform.
 */
public abstract class LocalizationManager<P extends CorePlugin, T extends CorePlayer> extends Manager<P> {

    protected final Map<Locale, List<YamlDocument>> localizations;
    protected final ReloadableContent<LinkedNode<Locale>> localeChain;

    public LocalizationManager(P plugin) {
        super(plugin);
        this.localizations = new HashMap<>();
        this.localeChain = generateLocaleChain();
        plugin.registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.RELOADABLE_CONTENT).trackReloadableContent(localeChain);
    }

    /**
     * Generates the default locale chain for this plugin.
     *
     * @return A {@link ReloadableContent} containing the default locale chain for this plugin.
     */
    @NotNull
    protected abstract ReloadableContent<LinkedNode<Locale>> generateLocaleChain();

    /**
     * Post-processes a resolved locale string before it is returned or parsed by MiniMessage.
     * The default implementation is an identity transform — the raw string is returned unchanged.
     * <p>
     * Subclasses override this to apply plugin-specific transformations such as palette
     * placeholder replacement (e.g. {@code <primary>} → {@code <color:#D4A76A>}).
     * This hook is applied in every {@code getLocalizedMessage} and {@code getLocalizedMessages}
     * overload, ensuring all resolution paths — direct string return, Component deserialization,
     * and list variants — receive the transformation before the string reaches MiniMessage.
     *
     * @param raw The resolved locale string.
     * @return The post-processed string.
     */
    @NotNull
    protected String postProcessResolvedString(@NotNull String raw) {
        return raw;
    }

    /**
     * Gets a localized {@link Component} using the provided {@link Route} to find a translated message.
     *
     * @param corePlayer The {@link T} to localize for.
     * @param route      The {@link Route} to check for a translated message.
     * @return A localized {@link Component} using the provided {@link Route} to find a translated message.
     * @throws NoLocalizationContainsMessageException If there is no localization in the player's locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public Component getLocalizedMessageAsComponent(@NotNull T corePlayer, @NotNull Route route) {
        return plugin().getMiniMessage().deserialize(getLocalizedMessage(corePlayer, route)).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Gets a localized {@link Component} using the provided {@link Route} to find a translated message. If the
     * provided {@link Audience} is an instance of a {@link Player}, then it will use that player's locale chain to
     * get the message. Otherwise, the default locale chain is used.
     *
     * @param audience The {@link Audience} to localize for.
     * @param route    The {@link Route} to check for a translated message.
     * @return A localized {@link Component} using the provided {@link Route} to find a translated message.
     * @throws NoLocalizationContainsMessageException If there is no localization in the player's locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public Component getLocalizedMessageAsComponent(@NotNull Audience audience, @NotNull Route route) {
        return plugin().getMiniMessage().deserialize(getLocalizedMessage(audience, route)).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Gets a localized {@link Component} using the provided {@link Route} to find a translated message.
     *
     * @param route The {@link Route} to check for a translated message.
     * @return A localized {@link Component} using the provided {@link Route} to find a translated message.
     * @throws NoLocalizationContainsMessageException If there is no localization in the default locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public Component getLocalizedMessageAsComponent(@NotNull Route route) {
        return plugin().getMiniMessage().deserialize(getLocalizedMessage(route)).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Gets a localized {@link Component} using the provided {@link Route} to find a translated message.
     *
     * @param corePlayer   The {@link T} to localize for.
     * @param route        The {@link Route} to check for a translated message.
     * @param placeholders The placeholders to replace in the message.
     * @return A localized {@link Component} using the provided {@link Route} to find a translated message.
     * @throws NoLocalizationContainsMessageException If there is no localization in the player's locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public Component getLocalizedMessageAsComponent(@NotNull T corePlayer, @NotNull Route route, @NotNull Map<String, String> placeholders) {
        return plugin().getMiniMessage().deserialize(getLocalizedMessage(corePlayer, route), getPlaceholders(placeholders)).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Gets a localized {@link Component} using the provided {@link Route} to find a translated message. If the
     * provided {@link Audience} is an instance of a {@link Player}, then it will use that player's locale chain to
     * get the message. Otherwise, the default locale chain is used.
     *
     * @param audience     The {@link Audience} to localize for.
     * @param route        The {@link Route} to check for a translated message.
     * @param placeholders The placeholders to replace in the message.
     * @return A localized {@link Component} using the provided {@link Route} to find a translated message.
     * @throws NoLocalizationContainsMessageException If there is no localization in the audience's locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public Component getLocalizedMessageAsComponent(@NotNull Audience audience, @NotNull Route route, @NotNull Map<String, String> placeholders) {
        return plugin().getMiniMessage().deserialize(getLocalizedMessage(audience, route), getPlaceholders(placeholders)).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Gets a localized {@link Component} using the provided {@link Route} to find a translated message.
     *
     * @param route        The {@link Route} to check for a translated message.
     * @param placeholders The placeholders to replace in the message.
     * @return A localized {@link Component} using the provided {@link Route} to find a translated message.
     * @throws NoLocalizationContainsMessageException If there is no localization in the default locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public Component getLocalizedMessageAsComponent(@NotNull Route route, @NotNull Map<String, String> placeholders) {
        return plugin().getMiniMessage().deserialize(getLocalizedMessage(route), getPlaceholders(placeholders)).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Gets a {@link List} of localized {@link Component}s using the provided {@link Route}
     * to find a translated messages.
     *
     * @param player The {@link T} to localize for.
     * @param route  The {@link Route} to check for a translated message.
     * @return A localized {@link Component} using the provided {@link Route} to find a translated message.
     * @throws NoLocalizationContainsMessageException If there is no localization in the player's locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public List<Component> getLocalizedMessageAsComponents(@NotNull T player, @NotNull Route route) {
        return getLocalizedMessages(player, route).stream()
                .map(message -> plugin().getMiniMessage().deserialize(message).decoration(TextDecoration.ITALIC, false))
                .collect(Collectors.toList());
    }

    /**
     * Gets a {@link List} of localized {@link Component}s using the provided {@link Route}
     * to find a translated messages.
     *
     * @param audience The {@link Audience} to localize for.
     * @param route    The {@link Route} to check for a translated message.
     * @return A localized {@link Component} using the provided {@link Route} to find a translated message.
     * @throws NoLocalizationContainsMessageException If there is no localization in the audience's locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public List<Component> getLocalizedMessageAsComponents(@NotNull Audience audience, @NotNull Route route) {
        return getLocalizedMessages(audience, route).stream()
                .map(message -> plugin().getMiniMessage().deserialize(message).decoration(TextDecoration.ITALIC, false))
                .collect(Collectors.toList());
    }

    /**
     * Gets a {@link List} of localized {@link Component}s using the provided {@link Route}
     * to find a translated messages.
     *
     * @param route The {@link Route} to check for a translated message.
     * @return A localized {@link Component} using the provided {@link Route} to find a translated message.
     * @throws NoLocalizationContainsMessageException If there is no localization in the default locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public List<Component> getLocalizedMessageAsComponents(@NotNull Route route) {
        return getLocalizedMessages(route).stream()
                .map(message -> plugin().getMiniMessage().deserialize(message).decoration(TextDecoration.ITALIC, false))
                .collect(Collectors.toList());
    }

    /**
     * Gets a {@link List} of localized {@link Component}s using the provided {@link Route}
     * to find a translated messages.
     *
     * @param player       The {@link T} to localize for.
     * @param route        The {@link Route} to check for a translated message.
     * @param placeholders The placeholders to replace in the message.
     * @return A localized {@link Component} using the provided {@link Route} to find a translated message.
     * @throws NoLocalizationContainsMessageException If there is no localization in the player's locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public List<Component> getLocalizedMessageAsComponents(@NotNull T player, @NotNull Route route, @NotNull Map<String, String> placeholders) {
        return getLocalizedMessages(player, route).stream()
                .map(message -> plugin().getMiniMessage().deserialize(message, getPlaceholders(placeholders)).decoration(TextDecoration.ITALIC, false))
                .collect(Collectors.toList());
    }

    /**
     * Gets a {@link List} of localized {@link Component}s using the provided {@link Route}
     * to find a translated messages.
     *
     * @param audience     The {@link Audience} to localize for.
     * @param route        The {@link Route} to check for a translated message.
     * @param placeholders The placeholders to replace in the message.
     * @return A localized {@link Component} using the provided {@link Route} to find a translated message.
     * @throws NoLocalizationContainsMessageException If there is no localization in the audience's locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public List<Component> getLocalizedMessageAsComponents(@NotNull Audience audience, @NotNull Route route, @NotNull Map<String, String> placeholders) {
        return getLocalizedMessages(audience, route).stream()
                .map(message -> plugin().getMiniMessage().deserialize(message, getPlaceholders(placeholders)).decoration(TextDecoration.ITALIC, false))
                .collect(Collectors.toList());
    }

    /**
     * Gets a {@link List} of localized {@link Component}s using the provided {@link Route}
     * to find a translated messages.
     *
     * @param route        The {@link Route} to check for a translated message.
     * @param placeholders The placeholders to replace in the message.
     * @return A localized {@link Component} using the provided {@link Route} to find a translated message.
     * @throws NoLocalizationContainsMessageException If there is no localization in the default locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public List<Component> getLocalizedMessageAsComponents(@NotNull Route route, @NotNull Map<String, String> placeholders) {
        return getLocalizedMessages(route).stream()
                .map(message -> plugin().getMiniMessage().deserialize(message, getPlaceholders(placeholders)).decoration(TextDecoration.ITALIC, false))
                .collect(Collectors.toList());
    }

    /**
     * Gets a localized message using the provided {@link Route} to find a translated message.
     *
     * @param player The {@link T} to localize for.
     * @param route  The {@link Route} to check for a translated message.
     * @return A localized message using the provided {@link Route} to find a translated message.
     * @throws NoLocalizationContainsMessageException If there is no localization in the player's locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public String getLocalizedMessage(@NotNull T player, @NotNull Route route) {
        LinkedNode<Locale> locales = getLocaleChain(player);
        Set<Locale> processedLocales = new HashSet<>();
        while (locales.hasNext()) {
            Locale locale = locales.getNodeValue();
            locales = locales.getNextNode();
            // We don't want to process locales twice
            if (processedLocales.contains(locale)) {
                continue;
            }
            // Mark that it has now been processed
            processedLocales.add(locale);
            // If we support this localization
            if (localizations.containsKey(locale)) {
                List<YamlDocument> documents = localizations.get(locale);
                // Check all registered configurations for the message
                for (YamlDocument yamlDocument : documents) {
                    if (yamlDocument.contains(route)) {
                        var papiHookOptional = plugin().registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHook(CorePluginHookKey.CORE_PAPI);
                        var playerOptional = player.getAsBukkitPlayer();
                        String message = yamlDocument.getString(route);
                        if (papiHookOptional.isPresent() && playerOptional.isPresent()) {
                            message = papiHookOptional.get().translateMessage(playerOptional.get(), message);
                        }
                        return postProcessResolvedString(message);
                    }
                }
            }
        }
        // If we reach here, then that means no languages support the message which shouldn't be true.
        // English should always be supported.
        throw new NoLocalizationContainsMessageException(route, processedLocales);
    }

    /**
     * Checks to see if any locale in the player's locale chain contains the provided {@link Route}.
     *
     * @param player The {@link T} to localize for.
     * @param route  The {@link Route} to check for a translated message.
     * @return {@code true} if any locale in the player's locale chain contains the provided {@link Route}.
     */
    public boolean doesAnyLocaleContainRoute(@NotNull T player, @NotNull Route route) {
        LinkedNode<Locale> locales = getLocaleChain(player);
        Set<Locale> processedLocales = new HashSet<>();
        while (locales.hasNext()) {
            Locale locale = locales.getNodeValue();
            locales = locales.getNextNode();
            // We don't want to process locales twice
            if (processedLocales.contains(locale)) {
                continue;
            }
            // Mark that it has now been processed
            processedLocales.add(locale);
            // If we support this localization
            if (localizations.containsKey(locale)) {
                List<YamlDocument> documents = localizations.get(locale);
                // Check all registered configurations for the message
                for (YamlDocument yamlDocument : documents) {
                    if (yamlDocument.contains(route)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Gets a localized message using the provided {@link Route} to find a translated message. If the
     * provided {@link Audience} is an instance of a {@link Player}, then it will use that player's locale chain to
     * get the message. Otherwise, the default locale chain is used.
     *
     * @param audience The {@link Audience} to localize for.
     * @param route    The {@link Route} to check for a translated message.
     * @return A localized message using the provided {@link Route} to find a translated message.
     * @throws NoLocalizationContainsMessageException If there is no localization in the player's locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public String getLocalizedMessage(@NotNull Audience audience, @NotNull Route route) {
        if (audience instanceof Player player) {
            PlayerManager<?, T> playerManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(CoreManagerKey.CORE_PLAYER_MANAGER);
            var playerOptional = playerManager.getPlayer(player.getUniqueId());
            if (playerOptional.isPresent()) {
                return getLocalizedMessage(playerOptional.get(), route);
            }
        }
        return getLocalizedMessage(route);
    }

    /**
     * Gets a localized message using the provided {@link Route} to find a translated message
     * with {@link Locale#ENGLISH} as the locale.
     *
     * @param route The {@link Route} to check for a translated message.
     * @return A localized message using the provided {@link Route} to find a translated message.
     * @throws NoLocalizationContainsMessageException If there is no localization in the default locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public String getLocalizedMessage(@NotNull Route route) {
        Locale locale = Locale.ENGLISH;
        if (localizations.containsKey(locale)) {
            List<YamlDocument> documents = localizations.get(locale);
            // Check all registered configurations for the message
            for (YamlDocument yamlDocument : documents) {
                if (yamlDocument.contains(route)) {
                    return postProcessResolvedString(yamlDocument.getString(route));
                }
            }
        }
        // If we reach here, then that means no languages support the message which shouldn't be true.
        // English should always be supported.
        throw new NoLocalizationContainsMessageException(route, Set.of(locale));
    }

    /**
     * Gets a localized message using the provided {@link Route}, then substitutes {@code <key>}
     * placeholders with the values from the provided map. This uses the same {@code <key>}
     * placeholder convention as {@link #getLocalizedMessageAsComponent(CorePlayer, Route, Map)} so that
     * locale YAML authors do not need to know whether a string will be resolved as a
     * {@link Component} or a plain {@link String}.
     *
     * @param player       The {@link T} to localize for.
     * @param route        The {@link Route} to check for a translated message.
     * @param placeholders Map of placeholder keys (without angle brackets) to replacement values.
     * @return The localized message with all matching placeholders substituted.
     * @throws NoLocalizationContainsMessageException If there is no localization in the player's locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public String getLocalizedMessage(@NotNull T player, @NotNull Route route,
                                      @NotNull Map<String, String> placeholders) {
        return applyStringPlaceholders(getLocalizedMessage(player, route), placeholders);
    }

    /**
     * Gets a localized message for the provided {@link Audience}, then substitutes {@code <key>}
     * placeholders with the values from the provided map.
     *
     * @param audience     The {@link Audience} to localize for.
     * @param route        The {@link Route} to check for a translated message.
     * @param placeholders Map of placeholder keys (without angle brackets) to replacement values.
     * @return The localized message with all matching placeholders substituted.
     * @throws NoLocalizationContainsMessageException If there is no localization in the audience's locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public String getLocalizedMessage(@NotNull Audience audience, @NotNull Route route,
                                      @NotNull Map<String, String> placeholders) {
        return applyStringPlaceholders(getLocalizedMessage(audience, route), placeholders);
    }

    /**
     * Gets a localized message using {@link Locale#ENGLISH} as the locale, then substitutes
     * {@code <key>} placeholders with the values from the provided map.
     *
     * @param route        The {@link Route} to check for a translated message.
     * @param placeholders Map of placeholder keys (without angle brackets) to replacement values.
     * @return The localized message with all matching placeholders substituted.
     * @throws NoLocalizationContainsMessageException If there is no localization in the default locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public String getLocalizedMessage(@NotNull Route route, @NotNull Map<String, String> placeholders) {
        return applyStringPlaceholders(getLocalizedMessage(route), placeholders);
    }

    /**
     * Substitutes {@code <key>} tokens in the given template using the provided map.
     * Uses the same angle-bracket convention as MiniMessage so that locale YAML files
     * need only one placeholder syntax regardless of how the string is resolved.
     *
     * @param template     the raw localized string
     * @param placeholders map of token key to replacement value
     * @return the string with all matching tokens replaced
     */
    @NotNull
    private static String applyStringPlaceholders(@NotNull String template,
                                                  @NotNull Map<String, String> placeholders) {
        String result = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("<" + entry.getKey() + ">", entry.getValue());
        }
        return result;
    }

    /**
     * Gets a {@link List} of localized messages from the provided {@link Route} assuming the route
     * maps to a string list.
     *
     * @param player The {@link CorePlayer} to use for localization.
     * @param route  The route containing the messages to localize.
     * @return A {@link List} of localized messages from the provided {@link Route} assuming
     * the route maps to a string list.
     * @throws NoLocalizationContainsMessageException If there is no localization in the player's locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public List<String> getLocalizedMessages(@NotNull T player, @NotNull Route route) {
        LinkedNode<Locale> locales = getLocaleChain(player);
        Set<Locale> processedLocales = new HashSet<>();
        while (locales.hasNext()) {
            Locale locale = locales.getNodeValue();
            locales = locales.getNextNode();
            // We don't want to process locales twice
            if (processedLocales.contains(locale)) {
                continue;
            }
            // Mark that it has now been processed
            processedLocales.add(locale);
            // If we support this localization
            if (localizations.containsKey(locale)) {
                List<YamlDocument> documents = localizations.get(locale);
                // Check all registered configurations for the message
                for (YamlDocument yamlDocument : documents) {
                    if (yamlDocument.contains(route)) {
                        var papiHookOptional = plugin().registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHook(CorePluginHookKey.CORE_PAPI);
                        var playerOptional = player.getAsBukkitPlayer();
                        List<String> message = yamlDocument.getStringList(route);
                        List<String> returnList = new ArrayList<>();
                        if (papiHookOptional.isPresent() && playerOptional.isPresent()) {
                            for (String line : message) {
                                returnList.add(postProcessResolvedString(papiHookOptional.get().translateMessage(playerOptional.get(), line)));
                            }
                        } else {
                            for (String line : message) {
                                returnList.add(postProcessResolvedString(line));
                            }
                        }
                        return returnList;
                    }
                }
            }
        }
        // If we reach here, then that means no languages support the message which shouldn't be true.
        // English should always be supported.
        throw new NoLocalizationContainsMessageException(route, processedLocales);
    }

    /**
     * Gets a {@link List} of localized messages from the provided {@link Route} assuming the route
     * maps to a string list.
     *
     * @param audience The {@link Audience} to use for localization.
     * @param route    The route containing the messages to localize.
     * @return A {@link List} of localized messages from the provided {@link Route} assuming
     * the route maps to a string list.
     * @throws NoLocalizationContainsMessageException If there is no localization in the audience's locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public List<String> getLocalizedMessages(@NotNull Audience audience, @NotNull Route route) {
        if (audience instanceof Player player) {
            PlayerManager<?, T> playerManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(CoreManagerKey.CORE_PLAYER_MANAGER);
            var playerOptional = playerManager.getPlayer(player.getUniqueId());
            if (playerOptional.isPresent()) {
                return getLocalizedMessages(playerOptional.get(), route);
            }
        }
        return getLocalizedMessages(route);
    }

    /**
     * Gets a {@link List} of localized messages from the provided {@link Route} assuming the route
     * maps to a string list.
     *
     * @param route The route containing the messages to localize.
     * @return A {@link List} of localized messages from the provided {@link Route} assuming
     * the route maps to a string list.
     * @throws NoLocalizationContainsMessageException If there is no localization in the default locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public List<String> getLocalizedMessages(@NotNull Route route) {
        Locale locale = Locale.ENGLISH;
        if (localizations.containsKey(locale)) {
            List<YamlDocument> documents = localizations.get(locale);
            // Check all registered configurations for the message
            for (YamlDocument yamlDocument : documents) {
                if (yamlDocument.contains(route)) {
                    return yamlDocument.getStringList(route).stream()
                            .map(this::postProcessResolvedString)
                            .collect(Collectors.toList());
                }
            }
        }
        // If we reach here, then that means no languages support the message which shouldn't be true.
        // English should always be supported.
        throw new NoLocalizationContainsMessageException(route, Set.of(locale));
    }

    /**
     * Broadcasts a localized message to all online players and the console. Each online player
     * receives the message resolved against their own locale chain if they are loaded, or the
     * default locale chain otherwise. The console always receives the default-locale variant.
     *
     * @param route The {@link Route} to check for a translated message.
     * @throws NoLocalizationContainsMessageException If no localization in a recipient's locale
     *                                                chain contains the provided {@link Route}.
     */
    public void broadcastMessage(@NotNull Route route) {
        PlayerManager<?, T> playerManager = (PlayerManager<?, T>) RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(CoreManagerKey.CORE_PLAYER_MANAGER);
        for (Player player : Bukkit.getOnlinePlayers()) {
            // If the player is loaded, attempt to use their locale chain
            var playerOptional = playerManager.getPlayer(player.getUniqueId());
            if (playerOptional.isPresent()) {
                T corePlayer = playerOptional.get();
                player.sendMessage(getLocalizedMessage(corePlayer, route));
            }
            // If the player isn't loaded, use default locale chain
            else {
                player.sendMessage(getLocalizedMessage(route));
            }
        }
        // Send to console using default locale chain
        Bukkit.getConsoleSender().sendMessage(getLocalizedMessage(route));
    }

    /**
     * Broadcasts a localized message with placeholders to all online players and the console.
     * Each online player receives the message resolved against their own locale chain if they
     * are loaded, or the default locale chain otherwise. Placeholders are substituted after
     * locale resolution. The console always receives the default-locale variant.
     *
     * @param route        The {@link Route} to check for a translated message.
     * @param placeholders The placeholders to replace in the message.
     * @throws NoLocalizationContainsMessageException If no localization in a recipient's locale
     *                                                chain contains the provided {@link Route}.
     */
    public void broadcastMessage(@NotNull Route route, @NotNull Map<String, String> placeholders) {
        PlayerManager<?, T> playerManager = (PlayerManager<?, T>) RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(CoreManagerKey.CORE_PLAYER_MANAGER);
        for (Player player : Bukkit.getOnlinePlayers()) {
            var playerOptional = playerManager.getPlayer(player.getUniqueId());
            if (playerOptional.isPresent()) {
                T corePlayer = playerOptional.get();
                player.sendMessage(getLocalizedMessageAsComponent(corePlayer, route, placeholders));
            }
            else {
                player.sendMessage(getLocalizedMessageAsComponent(route, placeholders));
            }
        }
        Bukkit.getConsoleSender().sendMessage(getLocalizedMessageAsComponent(route, placeholders));
    }

    /**
     * Gets a {@link Section} based on the provided {@link Route} using the
     * provided player's locale chain.
     *
     * @param player The {@link T} to get the locale chain for.
     * @param route  The {@link Route} to use.
     * @return A {@link Section} based on the provided {@link Route} using
     * the provided player's locale chain.
     * @throws NoLocalizationContainsMessageException If no {@link Locale} in the player's
     *                                                locale chain contains the provided {@link Route}.
     */
    @NotNull
    public Section getLocalizedSection(@NotNull T player, @NotNull Route route) {
        LinkedNode<Locale> locales = getLocaleChain(player);
        Set<Locale> processedLocales = new HashSet<>();
        while (locales.hasNext()) {
            Locale locale = locales.getNodeValue();
            locales = locales.getNextNode();
            // We don't want to process locales twice
            if (processedLocales.contains(locale)) {
                continue;
            }
            // Mark that it has now been processed
            processedLocales.add(locale);
            // If we support this localization
            if (localizations.containsKey(locale)) {
                List<YamlDocument> documents = localizations.get(locale);
                // Check all registered configurations for the message
                for (YamlDocument yamlDocument : documents) {
                    if (yamlDocument.contains(route)) {
                        return yamlDocument.getSection(route);
                    }
                }
            }
        }
        // If we reach here, then that means no languages support the message which shouldn't be true.
        // English should always be supported.
        throw new NoLocalizationContainsMessageException(route, processedLocales);
    }

    @NotNull
    public Section getLocalizedSection(@NotNull Route route) {
        Locale locale = Locale.ENGLISH;
        if (localizations.containsKey(locale)) {
            List<YamlDocument> documents = localizations.get(locale);
            // Check all registered configurations for the message
            for (YamlDocument yamlDocument : documents) {
                if (yamlDocument.contains(route)) {
                    return yamlDocument.getSection(route);
                }
            }
        }
        // If we reach here, then that means no languages support the message which shouldn't be true.
        // English should always be supported.
        throw new NoLocalizationContainsMessageException(route, Set.of(locale));
    }

    /**
     * Gets the "locale chain" for the provided {@link T}.
     *
     * @param corePlayer The {@link T} to get the locale chain for.
     * @return The "locale chain" for the provided player. It's expected that there will be an end {@link LinkedNode} where
     * {@link LinkedNode#hasNext()} returns {@code false}.
     */
    @NotNull
    public LinkedNode<Locale> getLocaleChain(@NotNull T corePlayer) {
        var clientLocaleOptional = getClientLocale(corePlayer);
        if (clientLocaleOptional.isPresent()) {
            LinkedNode<Locale> clientLocaleNode = new LinkedNode<>(clientLocaleOptional.get());
            clientLocaleNode.setNext(localeChain.getContent());
            return clientLocaleNode;
        } else {
            return localeChain.getContent();
        }
    }

    /**
     * Registers the provided {@link Localization} to be supported by McRPG.
     *
     * @param localization The {@link Localization} to add to McRPG.
     */
    public void registerLanguageFile(@NotNull Localization localization) {
        localizations.computeIfAbsent(localization.getLocale(), k -> new ArrayList<>()).add(localization.getConfigurationFile());
    }

    /**
     * Gets the {@link Locale} from the provided {@link T}'s client.
     *
     * @param corePlayer The {@link T} to get the client locale from.
     * @return An {@link Optional} containing the {@link T}'s client {@link Locale}
     * if possible.
     */
    @NotNull
    private Optional<Locale> getClientLocale(@NotNull T corePlayer) {
        return corePlayer.getAsBukkitPlayer().map(Player::locale);
    }

    /**
     * Converts the stored placeholders into ones that {@link net.kyori.adventure.Adventure} can accept.
     *
     * @return An array of {@link TagResolver.Single}s.
     */
    @NotNull
    private TagResolver.Single[] getPlaceholders(@NotNull Map<String, String> placeHolders) {
        TagResolver.Single[] placeholderArray = new TagResolver.Single[placeHolders.size()];
        int index = 0;
        for (String key : placeHolders.keySet()) {
            String value = placeHolders.get(key);
            TagResolver.Single placeholder = Placeholder.parsed(key, value);
            placeholderArray[index++] = placeholder;
        }
        return placeholderArray;
    }
}

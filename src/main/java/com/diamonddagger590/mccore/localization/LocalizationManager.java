package com.diamonddagger590.mccore.localization;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.exception.localization.NoLocalizationContainsMessageException;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.Manager;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import com.diamonddagger590.mccore.registry.plugin.CorePluginHookKey;
import com.diamonddagger590.mccore.util.LinkedNode;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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

    @NotNull
    protected abstract ReloadableContent<LinkedNode<Locale>> generateLocaleChain();

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
        return plugin().getMiniMessage().deserialize(getLocalizedMessage(corePlayer, route));
    }

    /**
     * Gets a localized {@link Component} using the provided {@link Route} to find a translated message.
     *
     * @param player       The {@link T} to localize for.
     * @param route        The {@link Route} to check for a translated message.
     * @param placeholders The placeholders to replace in the message.
     * @return A localized {@link Component} using the provided {@link Route} to find a translated message.
     * @throws NoLocalizationContainsMessageException If there is no localization in the player's locale
     *                                                chain that supports the provided route.
     */
    @NotNull
    public Component getLocalizedMessageAsComponent(@NotNull T player, @NotNull Route route, @NotNull Map<String, String> placeholders) {
        return plugin().getMiniMessage().deserialize(getLocalizedMessage(player, route), getPlaceholders(placeholders));
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
                        return message;
                    }
                }
            }
        }
        // If we reach here, then that means no languages support the message which shouldn't be true.
        // English should always be supported.
        throw new NoLocalizationContainsMessageException(route, processedLocales);
    }

    /**
     * Gets a localized message using the provided {@link Route} to find a translated message
     * with {@link Locale#ENGLISH} as the locale.
     *
     * @param route The {@link Route} to check for a translated message.
     * @return A localized message using the provided {@link Route} to find a translated message.
     * @throws NoLocalizationContainsMessageException If there is no localization in the player's locale
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
                    return yamlDocument.getString(route);
                }
            }
        }
        // If we reach here, then that means no languages support the message which shouldn't be true.
        // English should always be supported.
        throw new NoLocalizationContainsMessageException(route, Set.of(locale));
    }

    @NotNull
    public List<String> getLocalizedMessages(@NotNull T player, @NotNull Route route) {
        LinkedNode<Locale> locales = getLocaleChain(player);
        Set<Locale> processedLocales = new HashSet<>();
        while (locales.hasNext()) {
            Locale locale = locales.getNodeValue();
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
                                returnList.add(papiHookOptional.get().translateMessage(playerOptional.get(), line));
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

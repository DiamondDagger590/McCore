package com.diamonddagger590.mccore.localization;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * A localization that provides a configuration file in the form of a {@link YamlDocument}
 * for a specific {@link Locale}.
 */
public interface Localization {

    /**
     * Gets the {@link Locale} supported by this localization.
     * @return The {@link Locale} supported by this localization.
     */
    @NotNull
    Locale getLocale();

    /**
     * Gets the {@link YamlDocument} containing the configuration for this
     * localization.
     * @return The {@link YamlDocument} containing the configuration for this
     * localization.
     */
    @NotNull
    YamlDocument getConfigurationFile();
}

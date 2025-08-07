package com.diamonddagger590.mccore.configuration.common;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;

/**
 * A helper implementation of reloadable content that gets a string
 * from a config.
 */
public class ReloadableString extends ReloadableContent<String> {

    public ReloadableString(@NotNull YamlDocument yamlDocument, @NotNull Route route) {
        super(yamlDocument, route, (Section::getString));
    }

    public ReloadableString(@NotNull YamlDocument yamlDocument, @NotNull Route route, @NotNull String content) {
        super(yamlDocument, route, (Section::getString), content);
    }
}

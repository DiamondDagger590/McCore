package com.diamonddagger590.mccore.configuration.common;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;

/**
 * A helper implementation of reloadable content that gets a boolean
 * from a config.
 */
public class ReloadableBoolean extends ReloadableContent<Boolean> {

    public ReloadableBoolean(@NotNull YamlDocument yamlDocument, @NotNull Route route) {
        super(yamlDocument, route, (Section::getBoolean));
    }

    public ReloadableBoolean(@NotNull YamlDocument yamlDocument, @NotNull Route route, @NotNull Boolean content) {
        super(yamlDocument, route, (Section::getBoolean), content);
    }
}

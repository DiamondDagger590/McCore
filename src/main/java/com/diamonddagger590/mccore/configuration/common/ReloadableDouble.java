package com.diamonddagger590.mccore.configuration.common;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;

/**
 * A helper implementation of reloadable content that gets a double
 * from a config.
 */
public class ReloadableDouble extends ReloadableContent<Double> {

    public ReloadableDouble(@NotNull YamlDocument yamlDocument, @NotNull Route route) {
        super(yamlDocument, route, (Section::getDouble));
    }

    public ReloadableDouble(@NotNull YamlDocument yamlDocument, @NotNull Route route, @NotNull Double content) {
        super(yamlDocument, route, (Section::getDouble), content);
    }
}

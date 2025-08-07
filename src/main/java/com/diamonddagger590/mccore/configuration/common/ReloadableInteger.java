package com.diamonddagger590.mccore.configuration.common;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;

/**
 * A helper implementation of reloadable content that gets an integer
 * from a config.
 */
public class ReloadableInteger extends ReloadableContent<Integer> {

    public ReloadableInteger(@NotNull YamlDocument yamlDocument, @NotNull Route route) {
        super(yamlDocument, route, (Section::getInt));
    }

    public ReloadableInteger(@NotNull YamlDocument yamlDocument, @NotNull Route route, @NotNull Integer content) {
        super(yamlDocument, route, (Section::getInt), content);
    }
}

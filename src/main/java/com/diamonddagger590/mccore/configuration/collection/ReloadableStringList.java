package com.diamonddagger590.mccore.configuration.collection;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A helper implementation of reloadable content that gets a string list
 * from a config.
 */
public class ReloadableStringList extends ReloadableContent<List<String>> {

    public ReloadableStringList(@NotNull YamlDocument yamlDocument, @NotNull Route route) {
        super(yamlDocument, route, (Section::getStringList));
    }

    public ReloadableStringList(@NotNull YamlDocument yamlDocument, @NotNull Route route, @NotNull List<String> content) {
        super(yamlDocument, route, (Section::getStringList), content);
    }
}

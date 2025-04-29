package com.diamonddagger590.mccore.configuration.parser;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.parser.Parser;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;

/**
 * A type of {@link ReloadableContent} that contains a {@link Parser}
 * to be used for equations.
 */
public class ReloadableParser extends ReloadableContent<Parser> {

    public ReloadableParser(@NotNull YamlDocument yamlDocument, @NotNull Route route) {
        super(yamlDocument, route, (yamlDocument1, route1) -> new Parser(yamlDocument1.getString(route1)));
    }
}

package com.diamonddagger590.mccore.configuration;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ReloadableList<T> extends ReloadableContent<List<T>> {

    private final Function<List<String>, List<T>> listConverstionFunction;

    public ReloadableList(@NotNull YamlDocument yamlDocument, @NotNull Route route, @NotNull Function<List<String>, List<T>> listConverstionFunction) {
        super(yamlDocument, route, ((yamlDocument1, route1) -> listConverstionFunction.apply(yamlDocument1.getStringList(route1))));
        this.listConverstionFunction = listConverstionFunction;
    }

    public ReloadableList(@NotNull YamlDocument yamlDocument, @NotNull Route route, @NotNull Function<List<String>, List<T>> listConverstionFunction, @NotNull List<T> content) {
        super(yamlDocument, route, ((yamlDocument1, route1) -> listConverstionFunction.apply(yamlDocument1.getStringList(route1))), content);
        this.listConverstionFunction = listConverstionFunction;
    }

    @Override
    List<T> getDefaultContent() {
        return new ArrayList<>();
    }
}

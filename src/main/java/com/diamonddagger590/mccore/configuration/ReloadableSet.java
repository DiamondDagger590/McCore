package com.diamonddagger590.mccore.configuration;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ReloadableSet<T> extends ReloadableContent<Set<T>> {

    private final Function<List<String>, Set<T>> setConverstionFunction;

    public ReloadableSet(@NotNull YamlDocument yamlDocument, @NotNull Route route, @NotNull Function<List<String>, Set<T>> setConverstionFunction) {
        super(yamlDocument, route, ((yamlDocument1, route1) -> setConverstionFunction.apply(yamlDocument1.getStringList(route1))));
        this.setConverstionFunction = setConverstionFunction;
    }

    public ReloadableSet(@NotNull YamlDocument yamlDocument, @NotNull Route route, @NotNull Function<List<String>, Set<T>> setConverstionFunction, @NotNull Set<T> content) {
        super(yamlDocument, route, ((yamlDocument1, route1) -> setConverstionFunction.apply(yamlDocument1.getStringList(route1))), content);
        this.setConverstionFunction = setConverstionFunction;
    }

    @Override
    Set<T> getDefaultContent() {
        return new HashSet<>();
    }
}

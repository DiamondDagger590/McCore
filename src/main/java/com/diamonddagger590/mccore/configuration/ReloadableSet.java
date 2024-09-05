package com.diamonddagger590.mccore.configuration;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * A reloadable set is a special type of {@link ReloadableContent} that stores a {@link Set} of an object.
 * <p>
 * This allows for things like automatic reloading of a set of {@link org.bukkit.Material}s from a string list.
 *
 * @param <T> The type of object to be stored in the set.
 */
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
}

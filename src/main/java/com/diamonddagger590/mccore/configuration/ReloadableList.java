package com.diamonddagger590.mccore.configuration;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

/**
 * A reloadable list is a special type of {@link ReloadableContent} that stores a {@link List} of an object.
 * <p>
 * This allows for things like automatic reloading of a list of {@link org.bukkit.Material}s from a string list.
 *
 * @param <T> The type of object to be stored in the list.
 */
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
}

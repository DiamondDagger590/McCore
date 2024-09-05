package com.diamonddagger590.mccore.configuration;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/**
 * This class represents a wrapper around a type of content that needs to support being reloaded from a
 * configuration file.
 * <p>
 * Any reloadable content should be registered with the {@link ReloadableContentRegistry} in order to automatically
 * be reloaded whenever {@link ReloadableContentRegistry#reloadAllContent()} is called.
 *
 * @param <T> The type of object that needs to be stored.
 */
public abstract class ReloadableContent<T> {

    protected YamlDocument yamlDocument;
    protected Route route;
    protected BiFunction<YamlDocument, Route, T> reloadCallback;
    protected T content;

    public ReloadableContent(@NotNull YamlDocument yamlDocument, @NotNull Route route, @NotNull BiFunction<YamlDocument, Route, T> reloadCallback) {
        this.yamlDocument = yamlDocument;
        this.route = route;
        this.reloadCallback = reloadCallback;
        reloadContent();
    }

    public ReloadableContent(@NotNull YamlDocument yamlDocument, @NotNull Route route, @NotNull BiFunction<YamlDocument, Route, T> reloadCallback, @NotNull T content) {
        this.yamlDocument = yamlDocument;
        this.route = route;
        this.reloadCallback = reloadCallback;
        this.content = content;
    }

    /**
     * Gets the current content stored.
     *
     * @return The current content stored.
     */
    public T getContent() {
        return content;
    }

    /**
     * Reloads this content from the {@link #getYamlDocument() YamlDocument}.
     */
    public void reloadContent() {
        content = reloadCallback.apply(getYamlDocument(), getRoute());
    }

    /**
     * Gets the {@link YamlDocument} that this should pull content from when reloading.
     *
     * @return The {@link YamlDocument} that this should pull content from when reloading.
     */
    @NotNull
    public YamlDocument getYamlDocument() {
        return yamlDocument;
    }

    /**
     * Gets the {@link Route} to use when getting content from the {@link #getYamlDocument() YamlDocument}.
     *
     * @return The {@link Route} to use when getting content from the {@link #getYamlDocument() YamlDocument}.
     */
    @NotNull
    public Route getRoute() {
        return route;
    }
}

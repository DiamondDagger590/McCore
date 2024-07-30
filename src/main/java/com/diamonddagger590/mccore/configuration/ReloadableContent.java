package com.diamonddagger590.mccore.configuration;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public abstract class ReloadableContent<T> {

    protected YamlDocument yamlDocument;
    protected Route route;
    protected BiFunction<YamlDocument, Route, T> reloadCallback;
    protected T content;

    public ReloadableContent(@NotNull YamlDocument yamlDocument, @NotNull Route route, @NotNull BiFunction<YamlDocument, Route, T> reloadCallback) {
        this.yamlDocument = yamlDocument;
        this.route = route;
        this.reloadCallback = reloadCallback;
        this.content = getDefaultContent();
    }

    public ReloadableContent(@NotNull YamlDocument yamlDocument, @NotNull Route route, @NotNull BiFunction<YamlDocument, Route, T> reloadCallback, @NotNull T content) {
        this.yamlDocument = yamlDocument;
        this.route = route;
        this.reloadCallback = reloadCallback;
        this.content = content;
    }

    public T getContent() {
        return content;
    }

    abstract T getDefaultContent();

    public void reloadContent() {
        content = reloadCallback.apply(yamlDocument, route);
    }
}

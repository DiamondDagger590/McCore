package com.diamonddagger590.mccore.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ReloadableContentRegistry {

    private final Set<ReloadableContent<?>> reloadableContent;

    public ReloadableContentRegistry() {
        this.reloadableContent = new HashSet<>();
    }

    public void trackReloadableContent(@NotNull ReloadableContent<?> reloadableContent) {
        this.reloadableContent.add(reloadableContent);
    }

    public void trackReloadableContent(@NotNull Collection<ReloadableContent<?>> reloadableContent) {
        this.reloadableContent.addAll(reloadableContent);
    }

    public void reloadAllContent() {
        reloadableContent.forEach(ReloadableContent::reloadContent);
    }
}

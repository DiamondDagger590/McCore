package com.diamonddagger590.mccore.bootstrap;

import org.jetbrains.annotations.NotNull;

/**
 * A profile which is used in {@link CoreBootstrap}s in order
 * to determine what plugin features should or shouldn't be loaded
 * depending on what the context of the runtime is.
 */
public enum StartupProfile {
    PROD("true"),
    TEST("false");

    private final String key;
    private final String value;

    StartupProfile(@NotNull String value) {
        this.key = "mccore.testMode";
        this.value = value;
    }

    /**
     * Sets this startup profile to the corresponding system property so it can
     * be read from.
     */
    public void setSystemProperty() {
        System.setProperty(key, value);
    }
}

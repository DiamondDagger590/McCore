package com.diamonddagger590.mccore.config;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.properties.PropertyInitializer;

public abstract class BaseMainConfig implements SettingsHolder {

    @Comment("test")
    public static final Property<Boolean> B_STATS_ENABLED = PropertyInitializer.newProperty("bstats-enabled", true);
}

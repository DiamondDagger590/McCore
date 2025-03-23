package com.diamonddagger590.mccore.builder.item;

import dev.dejvokep.boostedyaml.route.Route;

/**
 * All the configuration keys needed to go from {@link dev.dejvokep.boostedyaml.block.implementation.Section}
 * to a {@link com.diamonddagger590.mccore.builder.item.impl.ItemBuilder}.
 * <p>
 * These keys are used under a {@link dev.dejvokep.boostedyaml.block.implementation.Section}, not from
 * the root of a {@link dev.dejvokep.boostedyaml.YamlDocument}.
 */
public class ItemBuilderConfigurationKeys {

    // Headers
    private static final Route SETTINGS_HEADER = Route.fromString("settings");
    private static final Route MOB_HEADER = Route.addTo(SETTINGS_HEADER, "mob");
    private static final Route TRIM_HEADER = Route.addTo(SETTINGS_HEADER, "trim");
    public static final Route POTION_HEADER = Route.addTo(SETTINGS_HEADER, "potions");
    private static final Route POTION_STYLE_HEADER = Route.from("style");
    public static final Route PATTERN_HEADER = Route.addTo(SETTINGS_HEADER, "patterns");

    public static final Route MATERIAL = Route.fromString("material");
    public static final Route DATA = Route.fromString("data");
    public static final Route NAME = Route.fromString("name");
    public static final Route LORE_ROUTE = Route.fromString("lore");
    public static final Route AMOUNT = Route.fromString("amount");
    public static final Route ENCHANTMENTS = Route.fromString("enchantments");
    public static final Route CUSTOM_MODEL_DATA = Route.fromString("custom-model-data");
    public static final Route HIDE_TOOLTIP = Route.fromString("hide-tool-tip");
    public static final Route UNBREAKABLE_ITEM = Route.fromString("unbreakable-item");

    // Settings
    public static final Route GLOWING = Route.addTo(SETTINGS_HEADER, "glowing");
    public static final Route PLAYER = Route.addTo(SETTINGS_HEADER, "player");
    public static final Route DAMAGE = Route.addTo(SETTINGS_HEADER, "damage");
    public static final Route SKULL = Route.addTo(SETTINGS_HEADER, "skull");
    public static final Route RGB = Route.addTo(SETTINGS_HEADER, "rgb");
    public static final Route COLOR = Route.addTo(SETTINGS_HEADER, "color");
    public static final Route MOB_TYPE = Route.addTo(MOB_HEADER, "type");
    public static final Route TRIM_PATTERN = Route.addTo(TRIM_HEADER, "pattern");
    public static final Route TRIM_MATERIAL = Route.addTo(TRIM_HEADER, "material");

    // Potion
    public static final Route POTION_DURATION = Route.fromString("duration");
    public static final Route POTION_LEVEL = Route.fromString("level");
    public static final Route POTION_ICON = Route.addTo(POTION_STYLE_HEADER, "icon");
    public static final Route POTION_AMBIENT = Route.addTo(POTION_STYLE_HEADER, "ambient");
    public static final Route POTION_PARTICLES = Route.addTo(POTION_STYLE_HEADER, "particles");
}

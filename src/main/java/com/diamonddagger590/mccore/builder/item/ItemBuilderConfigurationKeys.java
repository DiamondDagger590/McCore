package com.diamonddagger590.mccore.builder.item;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;

/**
 * All the configuration keys needed to go from {@link dev.dejvokep.boostedyaml.block.implementation.Section}
 * to a {@link com.diamonddagger590.mccore.builder.item.impl.ItemBuilder}.
 * <p>
 * These keys are used under a {@link dev.dejvokep.boostedyaml.block.implementation.Section}, not from
 * the root of a {@link dev.dejvokep.boostedyaml.YamlDocument}.
 */
public class ItemBuilderConfigurationKeys {

    // Headers
    private static final String SETTINGS_HEADER = toRoutePath("settings");
    private static final String MOB_HEADER = toRoutePath(SETTINGS_HEADER, "mob");
    private static final String TRIM_HEADER = toRoutePath(SETTINGS_HEADER, "trim");
    public static final String POTION_HEADER = toRoutePath(SETTINGS_HEADER, "potions");
    private static final String POTION_STYLE_HEADER = toRoutePath("style");
    public static final String PATTERN_HEADER = toRoutePath(SETTINGS_HEADER, "patterns");

    public static final String MATERIAL = toRoutePath("material");
    public static final String DATA = toRoutePath("data");
    public static final String NAME = toRoutePath("name");
    public static final String LORE_ROUTE = toRoutePath("lore");
    public static final String AMOUNT = toRoutePath("amount");
    public static final String MAX_STACK_SIZE = toRoutePath("max-stack-size");
    public static final String ENCHANTMENTS = toRoutePath("enchantments");
    public static final String CUSTOM_MODEL_DATA = toRoutePath("custom-model-data");
    public static final String CUSTOM_ITEM = toRoutePath("custom-item");
    public static final String HIDE_TOOLTIP = toRoutePath("hide-tool-tip");
    public static final String UNBREAKABLE_ITEM = toRoutePath("unbreakable-item");
    public static final String ITEM_FLAGS = toRoutePath("item-flags");

    // Settings
    public static final String GLOWING = toRoutePath(SETTINGS_HEADER, "glowing");
    public static final String PLAYER = toRoutePath(SETTINGS_HEADER, "player");
    public static final String DAMAGE = toRoutePath(SETTINGS_HEADER, "damage");
    public static final String SKULL = toRoutePath(SETTINGS_HEADER, "skull");
    public static final String RGB = toRoutePath(SETTINGS_HEADER, "rgb");
    public static final String COLOR = toRoutePath(SETTINGS_HEADER, "color");
    public static final String MOB_TYPE = toRoutePath(MOB_HEADER, "type");
    public static final String TRIM_PATTERN = toRoutePath(TRIM_HEADER, "pattern");
    public static final String TRIM_MATERIAL = toRoutePath(TRIM_HEADER, "material");

    // Potion
    public static final String POTION_DURATION = toRoutePath("duration");
    public static final String POTION_LEVEL = toRoutePath("level");
    public static final String POTION_ICON = toRoutePath(POTION_STYLE_HEADER, "icon");
    public static final String POTION_AMBIENT = toRoutePath(POTION_STYLE_HEADER, "ambient");
    public static final String POTION_PARTICLES = toRoutePath(POTION_STYLE_HEADER, "particles");
}

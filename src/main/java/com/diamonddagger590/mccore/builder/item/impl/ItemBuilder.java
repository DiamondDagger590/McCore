package com.diamonddagger590.mccore.builder.item.impl;

import com.diamonddagger590.mccore.builder.item.BaseItemBuilder;
import com.diamonddagger590.mccore.builder.item.ItemBuilderConfigurationKeys;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.diamonddagger590.mccore.util.Methods.getEntityType;
import static com.diamonddagger590.mccore.util.Methods.getPotionEffect;

/**
 * The default generic builder for {@link ItemStack}s.
 */
public class ItemBuilder extends BaseItemBuilder<ItemBuilder> {

    protected ItemBuilder(@NotNull final ItemStack itemStack) {
        super(itemStack);
    }

    protected ItemBuilder(@NotNull final String value) {
        super(value);
    }

    /**
     * Returns a {@link PotionBuilder} using the provided {@link ItemType}.
     *
     * @param itemType The {@link ItemType} to use.
     * @param amount   The amount of the item.
     * @return A new {@link PotionBuilder}.
     */
    @NotNull
    public static PotionBuilder potion(@NotNull final ItemType itemType, final int amount) {
        return new PotionBuilder(itemType.createItemStack(Math.max(amount, 1)));
    }

    /**
     * Returns a {@link PotionBuilder} using the provided {@link ItemType}.
     *
     * @param itemType The {@link ItemType} to use.
     * @return A new {@link PotionBuilder}.
     */
    @NotNull
    public static PotionBuilder potion(@NotNull final ItemType itemType) {
        return potion(itemType, 1);
    }

    /**
     * Returns a {@link SkullBuilder} using the provided {@link ItemType}.
     *
     * @param itemType The {@link ItemType} to use.
     * @param amount   The amount of the item.
     * @return A new {@link SkullBuilder}.
     */
    @NotNull
    public static SkullBuilder skull(@NotNull final ItemType itemType, final int amount) {
        return new SkullBuilder(itemType.createItemStack(Math.max(amount, 1)));
    }

    /**
     * Returns a {@link SkullBuilder} using the provided {@link ItemType}.
     *
     * @param itemType The {@link ItemType} to use.
     * @return A new {@link SkullBuilder}.
     */
    @NotNull
    public static SkullBuilder skull(@NotNull final ItemType itemType) {
        return skull(itemType, 1);
    }

    /**
     * Returns a {@link PatternBuilder} using the provided {@link ItemType}.
     *
     * @param itemType The {@link ItemType} to use.
     * @param amount   The amount of the item.
     * @return A new {@link PatternBuilder}.
     */
    @NotNull
    public static PatternBuilder pattern(@NotNull final ItemType itemType, final int amount) {
        return new PatternBuilder(itemType.createItemStack(Math.max(amount, 1)));
    }

    /**
     * Returns a {@link PatternBuilder} using the provided {@link ItemType}.
     *
     * @param itemType The {@link ItemType} to use.
     * @return A new {@link PatternBuilder}.
     */
    @NotNull
    public static PatternBuilder pattern(@NotNull final ItemType itemType) {
        return pattern(itemType, 1);
    }

    /**
     * Returns an {@link ItemBuilder} using the provided {@link ItemType}.
     *
     * @param itemType The {@link ItemType} to use.
     * @param amount   The amount of the item.
     * @return A new {@link ItemBuilder}.
     */
    @NotNull
    public static ItemBuilder from(@NotNull final ItemType itemType, final int amount) {
        return new ItemBuilder(itemType.createItemStack(Math.max(amount, 1)));
    }

    /**
     * Returns an {@link ItemBuilder} using the provided {@link ItemStack}.
     *
     * @param itemStack The {@link ItemStack} to modify.
     * @return A new {@link ItemBuilder}.
     */
    @NotNull
    public static ItemBuilder from(@NotNull final ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }

    /**
     * Returns an {@link ItemBuilder} using the provided {@link ItemType}.
     *
     * @param itemType The {@link ItemType} to use.
     * @return A new {@link ItemBuilder}.
     */
    @NotNull
    public static ItemBuilder from(@NotNull final ItemType itemType) {
        return from(itemType, 1);
    }

    /**
     * Returns an {@link ItemBuilder} using the provided custom item string.
     *
     * @param customItem The string of the custom item.
     * @return A new {@link ItemBuilder}.
     */
    @NotNull
    public static ItemBuilder from(@NotNull final String customItem) {
        return new ItemBuilder(customItem);
    }

    /**
     * Converts the provided {@link Section} into an {@link ItemBuilder} using predefined
     * keys found in {@link ItemBuilderConfigurationKeys} to pull data from.
     *
     * @param itemSection The {@link Section} containing configuration data.
     * @return A new {@link ItemBuilder}.
     */
    @NotNull
    public static ItemBuilder from(@NotNull Section itemSection) {
        final String base64 = itemSection.getString(ItemBuilderConfigurationKeys.DATA, "");
        final ItemBuilder itemBuilder = ItemBuilder.from(itemSection.getString(ItemBuilderConfigurationKeys.MATERIAL, "stone"));
        if (base64 != null && !base64.isEmpty()) {
            itemBuilder.withBase64(base64);
        }
        return from(itemSection, itemBuilder.asItemStack());
    }

    /**
     * Converts the provided {@link Section} into an {@link ItemBuilder} using predefined
     * keys found in {@link ItemBuilderConfigurationKeys} to pull data from.
     *
     * @param itemSection      The {@link Section} containing configuration data.
     * @param initialItemStack The initial underlying itemstack to apply the {@link Section} to.
     * @return A new {@link ItemBuilder}.
     */
    @NotNull
    public static ItemBuilder from(@NotNull Section itemSection, @NotNull ItemStack initialItemStack) {
        return from(itemSection, new ItemBuilder(initialItemStack));
    }

    /**
     * Converts the provided {@link Section} into an {@link ItemBuilder} using predefined
     * keys found in {@link ItemBuilderConfigurationKeys} to pull data from.
     *
     * @param itemSection      The {@link Section} containing configuration data.
     * @param itemBuilder The initial underlying {@link ItemBuilder} to apply the {@link Section} to.
     * @return A new {@link ItemBuilder}.
     */
    @NotNull
    public static ItemBuilder from(@NotNull Section itemSection, @NotNull ItemBuilder itemBuilder) {
        // We need to set max stack size before we set the amount
        if (itemSection.contains(ItemBuilderConfigurationKeys.MAX_STACK_SIZE)) {
            itemBuilder.setMaxStackSize(itemSection.getInt(ItemBuilderConfigurationKeys.MAX_STACK_SIZE));
        }
        itemBuilder.setDisplayName(itemSection.getString(ItemBuilderConfigurationKeys.NAME, ""))
                .withDisplayLore(itemSection.getStringList(ItemBuilderConfigurationKeys.LORE_ROUTE))
                .setAmount(itemSection.getInt(ItemBuilderConfigurationKeys.AMOUNT, 1));
        itemBuilder.setCustomModelData(itemSection.getInt(ItemBuilderConfigurationKeys.CUSTOM_MODEL_DATA, -1));
        if (itemSection.contains(ItemBuilderConfigurationKeys.CUSTOM_ITEM)) {
            itemBuilder.withCustomItem(itemSection.getString(ItemBuilderConfigurationKeys.CUSTOM_ITEM));
        }
        if (itemSection.getBoolean(ItemBuilderConfigurationKeys.HIDE_TOOLTIP, false)) {
            itemBuilder.hideToolTip();
        }
        itemBuilder.setUnbreakable(itemSection.getBoolean(ItemBuilderConfigurationKeys.UNBREAKABLE_ITEM, false));

        // Enchantments
        itemBuilder.setEnchantGlint(itemSection.getBoolean(ItemBuilderConfigurationKeys.GLOWING, false));
        final Section enchantments = itemSection.getSection(ItemBuilderConfigurationKeys.ENCHANTMENTS);
        if (enchantments != null) {
            for (final String enchantment : enchantments.getRoutesAsStrings(false)) {
                final int level = enchantments.getInt(enchantment);
                itemBuilder.addEnchantment(enchantment, level);
            }
        }

        itemSection.getStringList(ItemBuilderConfigurationKeys.ITEM_FLAGS).stream().map(ItemFlag::valueOf).forEach(itemBuilder::addItemFlag);

        final String player = itemSection.getString(ItemBuilderConfigurationKeys.PLAYER, "");
        if (player != null && !player.isEmpty()) {
            final SkullBuilder skullBuilder = itemBuilder.asSkullBuilder();
            skullBuilder.withName(player).build();
        }
        itemBuilder.setItemDamage(itemSection.getInt(ItemBuilderConfigurationKeys.DAMAGE, 0));
        itemBuilder.withSkull(itemSection.getString(ItemBuilderConfigurationKeys.SKULL, ""));

        // Color
        final String rgb = itemSection.getString(ItemBuilderConfigurationKeys.RGB, "");
        final String color = itemSection.getString(ItemBuilderConfigurationKeys.COLOR, "");
        itemBuilder.setColor(!color.isEmpty() ? color : !rgb.isEmpty() ? rgb : "");

        // Spawner
        final Optional<EntityType> mobType = getEntityType(itemSection.getString(ItemBuilderConfigurationKeys.MOB_TYPE, ""));
        mobType.ifPresent(entityType -> itemBuilder.asSpawnerBuilder().withEntityType(entityType).build());

        itemBuilder.setTrim(itemSection.getString(ItemBuilderConfigurationKeys.TRIM_PATTERN, ""), itemSection.getString(ItemBuilderConfigurationKeys.TRIM_MATERIAL, ""));

        final Section potions = itemSection.getSection(ItemBuilderConfigurationKeys.POTION_HEADER);
        if (potions != null) {
            for (final String potion : potions.getRoutesAsStrings(false)) {
                Route potionRoute = Route.fromString(potion);
                final Optional<PotionEffectType> type = getPotionEffect(potion);
                if (type.isPresent()) {
                    final int duration = potions.getInt(Route.addTo(potionRoute, ItemBuilderConfigurationKeys.POTION_DURATION), 60);
                    final int level = potions.getInt(Route.addTo(potionRoute, ItemBuilderConfigurationKeys.POTION_LEVEL), 1);
                    final boolean icon = potions.getBoolean(Route.addTo(potionRoute, ItemBuilderConfigurationKeys.POTION_ICON), false);
                    final boolean ambient = potions.getBoolean(Route.addTo(potionRoute, ItemBuilderConfigurationKeys.POTION_AMBIENT), false);
                    final boolean particles = potions.getBoolean(Route.addTo(potionRoute, ItemBuilderConfigurationKeys.POTION_PARTICLES), false);
                    final PotionBuilder potionBuilder = itemBuilder.asPotionBuilder();
                    potionBuilder.withPotionEffect(type.get(), duration, level, ambient, particles, icon).build();
                }
            }
        }

        final Section patterns = itemSection.getSection(ItemBuilderConfigurationKeys.PATTERN_HEADER);
        if (patterns != null) {
            for (final String pattern : patterns.getRoutesAsStrings(false)) {
                final String patternColor = patterns.getString(pattern, "white");
                final PatternBuilder patternBuilder = itemBuilder.asPatternBuilder();
                patternBuilder.addPattern(pattern, patternColor);
                patternBuilder.build();
            }
        }
        return itemBuilder.build();
    }
}

package com.diamonddagger590.mccore.builder.item;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.builder.item.impl.PatternBuilder;
import com.diamonddagger590.mccore.builder.item.impl.PotionBuilder;
import com.diamonddagger590.mccore.builder.item.impl.SkullBuilder;
import com.diamonddagger590.mccore.builder.item.impl.SpawnerBuilder;
import com.diamonddagger590.mccore.builder.item.impl.fireworks.FireworkBuilder;
import com.diamonddagger590.mccore.builder.item.impl.fireworks.FireworkStarBuilder;
import com.diamonddagger590.mccore.exception.builder.item.InvalidItemBuilderException;
import com.diamonddagger590.mccore.external.headdatabase.CoreHeadDatabaseHook;
import com.diamonddagger590.mccore.external.papi.CorePapiHook;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.plugin.CorePluginHookKey;
import com.google.common.collect.ImmutableMultimap;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import io.papermc.paper.datacomponent.item.ItemArmorTrim;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.MapItemColor;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.PatternReplacementResult;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.diamonddagger590.mccore.util.Methods.fromBase64;
import static com.diamonddagger590.mccore.util.Methods.getColor;
import static com.diamonddagger590.mccore.util.Methods.getDyeColor;
import static com.diamonddagger590.mccore.util.Methods.getEnchantment;
import static com.diamonddagger590.mccore.util.Methods.getFlag;
import static com.diamonddagger590.mccore.util.Methods.getRGB;
import static com.diamonddagger590.mccore.util.Methods.getTrimMaterial;
import static com.diamonddagger590.mccore.util.Methods.getTrimPattern;

/**
 * Forked from <a href="https://github.com/ryderbelserion/Fusion/blob/main/paper/src/main/java/com/ryderbelserion/fusion/paper/builder/items/modern/BaseItemBuilder.java">Fusion</a>.
 * Ty Ryder ^~^
 * <p>
 * This is the base level builder for {@link ItemStack}s with most of the code required
 * for constructing them. Any items requiring more specific handling (such as potions) should
 * implement their own specific sub builder to handle those types (such as {@link PotionBuilder}).
 * <p>
 * Subsequent calls to {@link #asItemStack()} will return new copies of the same item, meaning == checks
 * will fail, but {@link ItemStack#equals(Object)} checks should pass.
 * <p>
 * Changing the underlying {@link ItemStack} with a method such as {@link #withCustomItem(String)} will
 * reset any data applied directly to the item via methods such as {@link #setUnbreakable(boolean)}.
 */
public class BaseItemBuilder<B extends BaseItemBuilder<B>> {

    private static final EnumSet<Material> BANNERS = EnumSet.of(
            Material.WHITE_BANNER, Material.ORANGE_BANNER, Material.MAGENTA_BANNER, Material.LIGHT_BLUE_BANNER, Material.YELLOW_BANNER,
            Material.LIME_BANNER, Material.PINK_BANNER, Material.GRAY_BANNER, Material.LIGHT_GRAY_BANNER, Material.CYAN_BANNER,
            Material.PURPLE_BANNER, Material.BLUE_BANNER, Material.BROWN_BANNER, Material.GREEN_BANNER, Material.RED_BANNER,
            Material.BLACK_BANNER,
            Material.WHITE_WALL_BANNER, Material.ORANGE_WALL_BANNER, Material.MAGENTA_WALL_BANNER, Material.LIGHT_BLUE_WALL_BANNER, Material.YELLOW_WALL_BANNER,
            Material.LIME_WALL_BANNER, Material.PINK_WALL_BANNER, Material.GRAY_WALL_BANNER, Material.LIGHT_GRAY_WALL_BANNER, Material.CYAN_WALL_BANNER,
            Material.PURPLE_WALL_BANNER, Material.BLUE_WALL_BANNER, Material.BROWN_WALL_BANNER, Material.GREEN_WALL_BANNER, Material.RED_WALL_BANNER,
            Material.BLACK_WALL_BANNER
    );

    private static final EnumSet<Material> LEATHER_ARMOR = EnumSet.of(
            Material.LEATHER_HELMET,
            Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS,
            Material.LEATHER_BOOTS,
            Material.LEATHER_HORSE_ARMOR
    );

    private static final EnumSet<Material> POTIONS = EnumSet.of(
            Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION
    );

    protected CorePlugin corePlugin = CorePlugin.getInstance();
    protected MiniMessage miniMessage = corePlugin.getMiniMessage();

    private Map<String, String> placeholders = new HashMap<>();
    private final List<ItemFlag> itemFlags = new ArrayList<>();
    private List<String> lore = new ArrayList<>();
    private List<Component> loreAsComponent = new ArrayList<>();
    @Nullable
    private String displayName = null;
    @Nullable
    private Component displayNameComponent = null;
    @Nullable
    private String customItem;
    private ItemStack itemStack;
    private boolean staticItemName = true;
    private boolean applyAudienceSkullTexture = true;

    public BaseItemBuilder(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
        if (itemStack.hasData(DataComponentTypes.CUSTOM_NAME)) {
            this.displayNameComponent = itemStack.getData(DataComponentTypes.CUSTOM_NAME);
        } else if (itemStack.hasData(DataComponentTypes.ITEM_NAME)) {
            this.displayNameComponent = itemStack.getData(DataComponentTypes.ITEM_NAME);
        }
        if (this.itemStack.hasItemMeta() && this.itemStack.getItemMeta().hasLore()) {
            this.loreAsComponent = itemStack.lore();
        }
    }

    public BaseItemBuilder(@NotNull String item) {
        withCustomItem(item);
    }

    /**
     * Gets an {@link ItemStack} from this builder using the provided {@link Audience} to
     * use when replacing PlaceholderAPI placeholders.
     * <p>
     * Subsequent calls to this method will return new item stacks which means == checks will
     * not pass, but {@link ItemStack#equals(Object)} checks should.
     *
     * @param audience The {@link Audience} to use when replacing PlaceholderAPI placeholders.
     * @return An {@link ItemStack} from this builder.
     */
    @NotNull
    public ItemStack asItemStack(@Nullable final Audience audience) {
        if (this.displayName != null) {
            this.itemStack.setData(this.staticItemName ? DataComponentTypes.ITEM_NAME : DataComponentTypes.CUSTOM_NAME, parseString(displayName, audience));
        } else if (this.displayNameComponent != null) {
            Component displayComponent = parseComponent(displayNameComponent);
            displayComponent.decoration(TextDecoration.ITALIC, false);
            this.itemStack.setData(DataComponentTypes.ITEM_NAME, displayComponent);
            this.itemStack.setData(DataComponentTypes.CUSTOM_NAME, displayComponent);
        }
        if (!this.lore.isEmpty()) {
            this.itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(lore.stream().map(loreLine -> parseString(loreLine, audience)).toList()));
        }
        if (!this.loreAsComponent.isEmpty()) {
            this.itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(loreAsComponent.stream().map(this::parseComponent).toList()));
        }
        if (!this.itemFlags.isEmpty()) { // temporary for now.
            this.itemStack.editMeta(itemMeta -> this.itemFlags.forEach(flag -> {
                itemMeta.addItemFlags(flag);
                if (flag == ItemFlag.HIDE_ATTRIBUTES) {
                    itemMeta.setAttributeModifiers(ImmutableMultimap.of());
                }
            }));
        }
        // Check to see if we should apply the skull texture of the player this item is being created for
        if (applyAudienceSkullTexture && isPlayerHead() && audience != null) {
            this.asSkullBuilder().withAudience(audience).build();
        }
        build();
        return this.itemStack.clone();
    }

    /**
     * Gets an {@link ItemStack} from this builder.
     * <p>
     * Subsequent calls to this method will return new item stacks which means == checks will
     * not pass, but {@link ItemStack#equals(Object)} checks should.
     *
     * @return An {@link ItemStack} from this builder.
     */
    @NotNull
    public ItemStack asItemStack() {
        return this.asItemStack(null);
    }

    /**
     * Gets the custom item id for this item builder. This is
     * used to integrate with custom item plugins like Nexo.
     *
     * @return An {@link Optional} containing the custom item id
     * for this item builder if present.
     */
    @NotNull
    public Optional<String> getCustomItem() {
        return Optional.ofNullable(this.customItem);
    }

    /**
     * Builds any item specific builders into the underlying {@link ItemStack}.
     *
     * @return This builder.
     */
    @NotNull
    public B build() {
        return (B) this;
    }

    /**
     * If there is no underlying {@link ItemStack}, then will create a new one from the provided
     * {@link ItemType}.
     *
     * @param type   The {@link ItemType} to create an {@link ItemStack} from.
     * @param amount The amount of the created {@link ItemStack}.
     * @return This builder.
     */
    @NotNull
    public B withType(@NotNull final ItemType type, final int amount) {
        if (this.itemStack == null) {
            this.itemStack = type.createItemStack(amount);
        }
        return (B) this;
    }

    /**
     * If there is no underlying {@link ItemStack}, then will create a new one from the provided
     * {@link ItemType} with an amount of 1.
     *
     * @param type The {@link ItemType} to create an {@link ItemStack} from.
     * @return This builder.
     */
    @NotNull
    public B withType(@NotNull final ItemType type) {
        return this.withType(type, 1);
    }

    /**
     * Changes the underlying {@link ItemStack} to be built from a custom item from a plugin like
     * {@link ItemPluginType#NEXO} based on what plugin is currently supported via {@link CorePlugin#getItemPlugin()}.
     * <p>
     * There is a best effort to convert the provided string into an {@link ItemStack} using whatever
     * custom item plugin is currently supported.
     *
     * @param item The item to get as a custom item.
     * @return This builder.
     */
    @NotNull
    public B withCustomItem(@NotNull final String item) {
        this.customItem = item;
        this.itemStack = corePlugin.getItemPlugin().getCustomItem(item);
        return (B) this;
    }

    /**
     * Changes the underlying {@link ItemStack} to be built from the base64 conversion of the provided string.
     *
     * @param base64 The base64 representation of the item.
     * @return This builder.
     */
    @NotNull
    public B withBase64(@NotNull final String base64) {
        if (base64.isEmpty()) {
            return (B) this;
        }
        this.itemStack = fromBase64(base64);
        return (B) this;
    }

    /**
     * Attempts to change the underlying {@link ItemStack} by using HeadDatabase. If
     * HeadDatabase is not enabled, then no mutation will be done.
     *
     * @param skull The HeadDatabase head to get.
     * @return This builder.
     */
    public B withSkull(@NotNull final String skull) {
        var headDatabaseHookOptional = corePlugin.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHook(CorePluginHookKey.CORE_HEAD_DATABASE);
        if (skull.isEmpty() || headDatabaseHookOptional.isEmpty()) return (B) this;
        CoreHeadDatabaseHook coreHeadDatabaseHook = headDatabaseHookOptional.get();
        if (coreHeadDatabaseHook.isItem(skull)) {
            this.itemStack = coreHeadDatabaseHook.item(skull).orElse(ItemType.STONE.createItemStack(1));
            applyAudienceSkullTexture = false;
        } else {
            this.itemStack = ItemType.PLAYER_HEAD.createItemStack();
        }
        return (B) this;
    }

    /**
     * Adds a placeholder to be replaced in {@link Component}s for the item's name and lore.
     *
     * @param placeholder The placeholder tag to be replaced.
     * @param value       The value to replace the placeholder with.
     * @return This builder.
     */
    @NotNull
    public B addPlaceholder(@NotNull String placeholder, @NotNull String value) {
        this.placeholders.put(placeholder, value);
        return (B) this;
    }

    /**
     * Sets placeholders to be replaced in {@link Component}s for the item's name and lore.
     *
     * @param placeholders The placeholders to use.
     * @return This builder.
     */
    @NotNull
    public B setPlaceholders(@NotNull Map<String, String> placeholders) {
        this.placeholders = placeholders;
        return (B) this;
    }

    /**
     * Adds the provided placeholders be replaced in {@link Component}s for the item's name and lore.
     *
     * @param placeholders The placeholders to add.
     * @return This builder.
     */
    @NotNull
    public B addPlaceholders(@NotNull Map<String, String> placeholders) {
        this.placeholders.putAll(placeholders);
        return (B) this;
    }

    /**
     * Checks to see if the provided placeholder is in this builder.
     *
     * @param placeholder The placeholder to check.
     * @return {@code true} if the provided placeholder is in this builder.
     */
    public boolean hasPlaceholder(@NotNull String placeholder) {
        return this.placeholders.containsKey(placeholder);
    }

    /**
     * Removes the provided placeholder from this builder.
     *
     * @param placeholder The placeholder to remove.
     * @return This builder.
     */
    @NotNull
    public B removePlaceholder(@NotNull String placeholder) {
        this.placeholders.remove(placeholder);
        return (B) this;
    }

    /**
     * Adds the {@link Enchantment} matching the provided string to the underlying {@link ItemStack}.
     *
     * @param enchant The enchantment to add.
     * @param level   The level of the enchantment
     * @return This builder.
     */
    @NotNull
    public B addEnchantment(@NotNull final String enchant, final int level) {
        getEnchantment(enchant).ifPresent(value -> addEnchantment(value, level));
        return (B) this;
    }

    /**
     * Adds the {@link Enchantment} to the underlying {@link ItemStack}.
     *
     * @param enchant The {@link Enchantment} to add.
     * @param level   The level of the enchantment
     * @return This builder.
     */
    @NotNull
    public B addEnchantment(@NotNull final Enchantment enchant, final int level) {
        final ItemEnchantments.Builder builder = ItemEnchantments.itemEnchantments();

        if (isEnchantedBook() && this.itemStack.hasData(DataComponentTypes.STORED_ENCHANTMENTS)) {
            final ItemEnchantments enchantments = this.itemStack.getData(DataComponentTypes.STORED_ENCHANTMENTS);
            if (enchantments != null) {
                builder.addAll(enchantments.enchantments());
            }
        } else if (this.itemStack.hasData(DataComponentTypes.ENCHANTMENTS)) {
            final ItemEnchantments enchantments = this.itemStack.getData(DataComponentTypes.ENCHANTMENTS);
            if (enchantments != null) {
                builder.addAll(enchantments.enchantments());
            }
        }

        builder.add(enchant, level);
        this.itemStack.setData(isEnchantedBook() ? DataComponentTypes.STORED_ENCHANTMENTS : DataComponentTypes.ENCHANTMENTS, builder.build());
        return (B) this;
    }

    /**
     * Removes the {@link Enchantment} matching the provided string from the underlying {@link ItemStack}.
     *
     * @param enchant The enchantment to remove.
     * @return This builder.
     */
    @NotNull
    public B removeEnchantment(@NotNull final String enchant) {
        getEnchantment(enchant).ifPresent(this::removeEnchantment);
        return (B) this;
    }

    /**
     * Removes the {@link Enchantment} from the underlying {@link ItemStack}.
     *
     * @param enchant The {@link Enchantment} to remove.
     * @return This builder.
     */
    @NotNull
    public B removeEnchantment(@NotNull final Enchantment enchant) {
        this.itemStack.removeEnchantment(enchant);
        return (B) this;
    }

    /**
     * Sets whether the item being built will have an enchantment glint or not.
     *
     * @param enchantGlintOverride If the item should have an enchantment glint.
     * @return This builder.
     */
    @NotNull
    public B setEnchantGlint(final boolean enchantGlintOverride) {
        if (enchantGlintOverride && !this.itemStack.hasData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE)) {
            this.itemStack.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, enchantGlintOverride);
            return (B) this;
        } else if (!enchantGlintOverride && this.itemStack.hasData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE)) {
            this.itemStack.unsetData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        }
        return (B) this;
    }

    /**
     * Removes the enchantment glint from the underlying {@link ItemStack}.
     *
     * @return This builder.
     */
    @NotNull
    public B removeEnchantGlint() {
        if (this.itemStack.hasData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE)) {
            this.itemStack.unsetData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        }
        return (B) this;
    }

    /**
     * Sets the amount of the underlying {@link ItemStack}.
     *
     * @param amount The amount of the item.
     * @return This builder.
     */
    @NotNull
    public B setAmount(final int amount) {
        this.itemStack.setAmount(Math.max(amount, 1));
        return (B) this;
    }

    /**
     * Sets the max stack size of the underlying {@link ItemStack}.
     *
     * @param maxStackSize The max stack size of the item.
     * @return This builder.
     */
    @NotNull
    public B setMaxStackSize(final int maxStackSize) {
        this.itemStack.setData(DataComponentTypes.MAX_STACK_SIZE, Math.max(1, maxStackSize));
        return (B) this;
    }

    /**
     * Sets the display name of the item being built.
     *
     * @param displayName    The display name to use when building an item.
     * @param staticItemName If the name should be immutable through anvils.
     * @return This builder.
     */
    @NotNull
    public B setDisplayName(@Nullable final String displayName, final boolean staticItemName) {
        if (displayNameComponent != null) {
            this.displayNameComponent = null;
        }
        this.displayName = displayName;
        this.staticItemName = staticItemName;
        return (B) this;
    }

    /**
     * Sets the display name of the item being built.
     *
     * @param displayName The display name to use when building an item.
     * @return This builder.
     */
    @NotNull
    public B setDisplayName(@Nullable final String displayName) {
        return setDisplayName(displayName, false);
    }

    /**
     * Gets the display name from the underlying {@link ItemStack}.
     *
     * @return The display name from the underlying {@link ItemStack}.
     */
    @NotNull
    public String getPlainName() {
        Component component = Component.empty();
        if (this.itemStack.hasData(DataComponentTypes.ITEM_NAME)) {
            final Component item_name = this.itemStack.getData(DataComponentTypes.ITEM_NAME);
            if (item_name != null) {
                component = item_name;
            }
        } else if (this.itemStack.hasData(DataComponentTypes.CUSTOM_NAME)) {
            final Component custom_name = this.itemStack.getData(DataComponentTypes.CUSTOM_NAME);
            if (custom_name != null) {
                component = custom_name;
            }
        }
        return PlainTextComponentSerializer.plainText().serializeOr(component, "");
    }

    /**
     * Sets the lore to be used for the item being built.
     *
     * @param displayLore The lore to be used.
     * @return This builder.
     */
    @NotNull
    public B withDisplayLore(@NotNull final List<String> displayLore) {
        if (!loreAsComponent.isEmpty()) {
            throw new IllegalStateException("Can not set lore when there was already one created");
        }
        this.lore = displayLore;
        return (B) this;
    }

    /**
     * Adds to the lore to be used for the item being built.
     *
     * @param displayLore The line of lore to be added.
     * @return This builder.
     */
    @NotNull
    public B addDisplayLore(@NotNull final String displayLore) {
        if (displayLore.isEmpty()) return (B) this;
        this.lore.add(displayLore);
        return (B) this;
    }

    /**
     * Adds the provided {@link List} of strings
     * to the lore to be used for the item being built.
     *
     * @param displayLore The {@link List} of lore to be added.
     * @return This builder.
     */
    @NotNull
    public B addDisplayLore(@NotNull final List<String> displayLore) {
        if (displayLore.isEmpty()) return (B) this;
        this.lore.addAll(displayLore);
        return (B) this;
    }

    /**
     * Gets the lore from the underlying {@link ItemStack}.
     *
     * @return The lore from the underlying {@link ItemStack}.
     */
    @NotNull
    public List<String> getPlainLore() {
        final List<String> plainLore = new ArrayList<>();
        if (this.itemStack.hasData(DataComponentTypes.LORE)) {
            final ItemLore lore = this.itemStack.getData(DataComponentTypes.LORE);
            if (lore != null) {
                lore.lines().forEach(line -> plainLore.add(PlainTextComponentSerializer.plainText().serialize(line)));
            }
        }
        return plainLore;
    }

    /**
     * Sets it so the underlying {@link ItemStack} has its tooltip hidden.
     *
     * @return This builder.
     */
    @NotNull
    public B hideToolTip() {
        if (!this.itemStack.hasData(DataComponentTypes.TOOLTIP_DISPLAY)) {
            this.itemStack.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build());
        }
        return (B) this;
    }

    /**
     * Sets it so the underlying {@link ItemStack} has its tooltip shown.
     *
     * @return This builder.
     */
    @NotNull
    public B showToolTip() {
        if (this.itemStack.hasData(DataComponentTypes.TOOLTIP_DISPLAY)) {
            this.itemStack.unsetData(DataComponentTypes.TOOLTIP_DISPLAY);
        }
        return (B) this;
    }

    /**
     * Adds the provided {@link ItemFlag}s to be used for the item being built.
     *
     * @param itemFlag The {@link ItemFlag}s to add.
     * @return This builder.
     */
    @NotNull
    public B addItemFlag(final ItemFlag itemFlag) {
        this.itemFlags.add(itemFlag);
        return (B) this;
    }

    /**
     * Adds the {@link ItemFlag} matching the provided string to be used for the item
     * being built.
     *
     * @param flag The string representation of an {@link ItemFlag} to add.
     * @return This builder.
     */
    @NotNull
    public B addItemFlag(final String flag) {
        getFlag(flag).ifPresent(this::addItemFlag);
        return (B) this;
    }

    /**
     * Adds {@link ItemFlag}s matching the provided strings to be used for the item being
     * built.
     *
     * @param flags The string representations of an {@link ItemFlag} to add.
     * @return This builder.
     */
    @NotNull
    public B addItemFlags(final List<String> flags) {
        flags.forEach(this::addItemFlag);
        return (B) this;
    }

    /**
     * Removes {@link ItemFlag}s matching the provided strings to be used for the item being
     * built.
     *
     * @param flags The string representations of an {@link ItemFlag} to remove.
     * @return This builder.
     */
    @NotNull
    public B removeItemFlags(final List<String> flags) {
        flags.forEach(this::removeItemFlag);
        return (B) this;
    }

    /**
     * Removes the {@link ItemFlag} matching the provided string to be used for the item
     * being built.
     *
     * @param flag The string representation of an {@link ItemFlag} to remove.
     * @return This builder.
     */
    @NotNull
    public B removeItemFlag(final String flag) {
        Optional<ItemFlag> itemFlagOptional = getFlag(flag);
        if (itemFlagOptional.isPresent()) {
            ItemFlag itemFlag = itemFlagOptional.get();
            this.itemStack.editMeta(itemMeta -> itemMeta.removeItemFlags(itemFlag));
            this.itemFlags.remove(itemFlag);
        }
        return (B) this;
    }

    /**
     * Sets the underlying {@link ItemStack} to be unbreakable or not.
     *
     * @param isUnbreakable If the item should be unbreakable.
     * @return This builder.
     */
    @NotNull
    public B setUnbreakable(final boolean isUnbreakable) {
        if (isUnbreakable && !this.itemStack.hasData(DataComponentTypes.UNBREAKABLE)) {
            this.itemStack.setData(DataComponentTypes.UNBREAKABLE);
            return (B) this;
        }
        if (this.itemStack.hasData(DataComponentTypes.UNBREAKABLE)) {
            this.itemStack.unsetData(DataComponentTypes.UNBREAKABLE);
        }
        return (B) this;
    }

    /**
     * Sets the custom model data for the underlying {@link ItemStack}.
     *
     * @param customModelData The custom model data for the underlying item.
     * @return This builder.
     */
    @NotNull
    public B setCustomModelData(final int customModelData) {
        if (customModelData == -1) return (B) this;
        this.itemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addFloat(customModelData).build());
        return (B) this;
    }

    /**
     * Sets the item mode for the underlying {@link ItemStack} using the default minecraft namespace.
     *
     * @param itemModel The item model for the underlying item.
     * @return This builder.
     */
    @NotNull
    public B setItemModel(@NotNull final String itemModel) {
        if (itemModel.isEmpty()) return (B) this;
        this.itemStack.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft(itemModel));
        return (B) this;
    }

    /**
     * Sets the item model for the underlying {@link ItemStack} using the provided namespace.
     *
     * @param namespace The namespace of the item model.
     * @param itemModel The item model for the underlying item.
     * @return This builder.
     */
    @NotNull
    public B setItemModel(@NotNull final String namespace, @NotNull final String itemModel) {
        if (itemModel.isEmpty()) return (B) this;
        this.itemStack.setData(DataComponentTypes.ITEM_MODEL, new NamespacedKey(namespace, itemModel));
        return (B) this;
    }

    /**
     * Sets the trim for the underlying {@link ItemStack}.
     *
     * @param pattern  The string representation of a {@link TrimPattern}.
     * @param material The string representation of a {@link TrimMaterial}.
     * @return This builder.
     */
    @NotNull
    public B setTrim(@NotNull final String pattern, @NotNull final String material) {
        if (pattern.isEmpty() || material.isEmpty()) return (B) this;
        Optional<TrimMaterial> trimMaterial = getTrimMaterial(material);
        Optional<TrimPattern> trimPattern = getTrimPattern(pattern);
        if (trimPattern.isEmpty() || trimMaterial.isEmpty()) {
            return (B) this;
        }
        return setTrim(trimPattern.get(), trimMaterial.get());
    }

    /**
     * Sets the trim for the underlying {@link ItemStack}.
     *
     * @param trimPattern  The {@link TrimPattern} to use.
     * @param trimMaterial The {@link TrimMaterial} to use.
     * @return This builder.
     */
    @NotNull
    public B setTrim(@NotNull final TrimPattern trimPattern, @NotNull final TrimMaterial trimMaterial) {
        final ItemArmorTrim.Builder builder = ItemArmorTrim
                .itemArmorTrim(new ArmorTrim(trimMaterial, trimPattern));
        this.itemStack.setData(DataComponentTypes.TRIM, builder.build());
        return (B) this;
    }

    /**
     * Sets the {@link Color} of the underlying {@link ItemStack} if the item can be colored.
     *
     * @param value The color to use. Can be RGB or {@link Color}.
     * @return This builder.
     */
    @NotNull
    public B setColor(@NotNull final String value) {
        final Color color = value.contains(",") ? getRGB(value).orElse(Color.WHITE) : getColor(value);
        if (isMap()) {
            this.itemStack.setData(DataComponentTypes.MAP_COLOR, MapItemColor.mapItemColor().color(color).build());
        } else if (isLeatherArmor() || isPotion()) {
            this.itemStack.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor().color(color).build());
        } else if (isShield()) {
            final DyeColor dyeColor = getDyeColor(value);
            this.itemStack.setData(DataComponentTypes.BASE_COLOR, dyeColor);
        }

        return (B) this;
    }

    /**
     * Sets the item damage of the underlying {@link ItemStack}.
     *
     * @param damage The damage to set.
     * @return This builder.
     */
    public B setItemDamage(final int damage) {
        this.itemStack.setData(DataComponentTypes.DAMAGE, Math.min(damage, getType().getMaxDurability()));
        return (B) this;
    }

    /**
     * Creates a new {@link ItemStack} based on the provided {@link Audience} and sets it into the
     * provided {@link Inventory} at the provided slot.
     *
     * @param audience  The {@link Audience} to use for placeholders when constructing the item.
     * @param inventory The {@link Inventory} to set the item to.
     * @param slot      The slot to set the item to.
     */
    public void setItemToInventory(@Nullable final Audience audience, @NotNull final Inventory inventory, final int slot) {
        inventory.setItem(slot, asItemStack(audience));
    }

    /**
     * Creates a new {@link ItemStack} sets it into the provided {@link Inventory} at the provided slot.
     *
     * @param inventory The {@link Inventory} to set the item to.
     * @param slot      The slot to set the item to.
     */
    public void setItemToInventory(@NotNull final Inventory inventory, final int slot) {
        setItemToInventory(null, inventory, slot);
    }

    /**
     * Creates a new {@link ItemStack} based on the provided {@link Audience} and adds it into the
     * provided {@link Inventory}.
     *
     * @param audience  The {@link Audience} to use for placeholders when constructing the item.
     * @param inventory The {@link Inventory} to add the item to.
     */
    public void addItemToInventory(@Nullable final Audience audience, @NotNull final Inventory inventory) {
        inventory.addItem(asItemStack(audience));
    }

    /**
     * Creates a new {@link ItemStack} and adds it into the provided {@link Inventory}.
     *
     * @param inventory The {@link Inventory} to add the item to.
     */
    public void addItemToInventory(@NotNull final Inventory inventory) {
        addItemToInventory(null, inventory);
    }

    /**
     * Checks to see if the underlying {@link ItemStack} can be dyed.
     *
     * @return {@code true} if the underlying {@link ItemStack} can be dyed.
     */
    public final boolean isDyeable() {
        return isTippedArrow() || isShield() || isLeatherArmor() || isMap();
    }

    /**
     * Checks to see if the underlying {@link ItemStack} is a player head.
     *
     * @return {@code true} if the underlying {@link ItemStack} is a player head.
     */
    public final boolean isPlayerHead() {
        return getType().equals(Material.PLAYER_HEAD);
    }

    /**
     * Checks to see if the underlying {@link ItemStack} is a firework star
     *
     * @return {@code true} if the underlying {@link ItemStack} is a firework star.
     */
    public final boolean isFireworkStar() {
        return getType().equals(Material.FIREWORK_STAR);
    }

    /**
     * Checks to see if the underlying {@link ItemStack} is a tipped arrow.
     *
     * @return {@code true} if the underlying {@link ItemStack} is a tipped arrow.
     */
    public final boolean isTippedArrow() {
        return getType().equals(Material.TIPPED_ARROW);
    }

    /**
     * Checks to see if the underlying {@link ItemStack} is a firework.
     *
     * @return {@code true} if the underlying {@link ItemStack} is a firework.
     */
    public final boolean isFirework() {
        return getType().equals(Material.FIREWORK_ROCKET);
    }

    /**
     * Checks to see if the underlying {@link ItemStack} is a spawner.
     *
     * @return {@code true} if the underlying {@link ItemStack} is a spawner.
     */
    public final boolean isSpawner() {
        return getType().equals(Material.SPAWNER);
    }

    /**
     * Checks to see if the underlying {@link ItemStack} is a shield.
     *
     * @return {@code true} if the underlying {@link ItemStack} is a shield.
     */
    public final boolean isShield() {
        return getType().equals(Material.SHIELD);
    }

    /**
     * Checks to see if the underlying {@link ItemStack} is edible.
     *
     * @return {@code true} if the underlying {@link ItemStack} is edible.
     */
    public final boolean isEdible() {
        return getType().isEdible();
    }

    /**
     * Checks to see if the underlying {@link ItemStack} is leather armor.
     *
     * @return {@code true} if the underlying {@link ItemStack} is leather armor.
     */
    public final boolean isLeatherArmor() {
        return LEATHER_ARMOR.contains(getType());
    }

    /**
     * Checks to see if the underlying {@link ItemStack} is a potion.
     *
     * @return {@code true} if the underlying {@link ItemStack} is a potion.
     */
    public final boolean isPotion() {
        return POTIONS.contains(getType());
    }

    /**
     * Checks to see if the underlying {@link ItemStack} is a banner.
     *
     * @return {@code true} if the underlying {@link ItemStack} is a banner.
     */
    public final boolean isBanner() {
        return BANNERS.contains(getType());
    }

    /**
     * Checks to see if the underlying {@link ItemStack} is an enchanted book.
     *
     * @return {@code true} if the underlying {@link ItemStack} is an enchanted book..
     */
    public final boolean isEnchantedBook() {
        return getType().equals(Material.ENCHANTED_BOOK);
    }

    /**
     * Checks to see if the underlying {@link ItemStack} is a map.
     *
     * @return {@code true} if the underlying {@link ItemStack} is a map.
     */
    public final boolean isMap() {
        return getType().equals(Material.FILLED_MAP);
    }

    /**
     * Gets a {@link FireworkBuilder} using the underlying {@link ItemStack}.
     *
     * @return A {@link FireworkBuilder}.
     * @throws InvalidItemBuilderException If {@link #isFirework()} is {@code false}.
     */
    @NotNull
    public FireworkBuilder asFireworkBuilder() {
        if (!isFirework()) {
            throw new InvalidItemBuilderException(this, "This item type is not a firework rocket.");
        }
        return new FireworkBuilder(this.itemStack);
    }

    /**
     * Gets a {@link FireworkStarBuilder} using the underlying {@link ItemStack}.
     *
     * @return A {@link FireworkStarBuilder}.
     * @throws InvalidItemBuilderException If {@link #isFireworkStar()} is {@code false}.
     */
    @NotNull
    public FireworkStarBuilder asFireworkStarBuilder() {
        if (!isFireworkStar()) {
            throw new InvalidItemBuilderException(this, "This item type is not a firework star.");
        }
        return new FireworkStarBuilder(this.itemStack);
    }

    /**
     * Gets a {@link PatternBuilder} using the underlying {@link ItemStack}.
     *
     * @return A {@link PatternBuilder}.
     * @throws InvalidItemBuilderException If {@link #isShield()} or {@link #isBanner()} are {@code false}.
     */
    @NotNull
    public PatternBuilder asPatternBuilder() {
        if (isShield() || isBanner()) {
            return new PatternBuilder(this.itemStack);
        }
        throw new InvalidItemBuilderException(this, "This item type is not a shield/banner.");
    }

    /**
     * Gets a {@link SkullBuilder} using the underlying {@link ItemStack}.
     *
     * @return A {@link SkullBuilder}.
     * @throws InvalidItemBuilderException If {@link #isPlayerHead()} is {@code false}.
     */
    @NotNull
    public SkullBuilder asSkullBuilder() {
        if (!isPlayerHead()) {
            throw new InvalidItemBuilderException(this, "This item type is not a skull.");
        }
        return new SkullBuilder(this.itemStack);
    }

    /**
     * Gets a {@link PotionBuilder} using the underlying {@link ItemStack}.
     *
     * @return A {@link PotionBuilder}.
     * @throws InvalidItemBuilderException If {@link #isPotion()} is {@code false}.
     */
    @NotNull
    public PotionBuilder asPotionBuilder() {
        if (!isPotion()) {
            throw new InvalidItemBuilderException(this, "This item type is not a potion.");
        }
        return new PotionBuilder(this.itemStack);
    }

    /**
     * Gets a {@link SpawnerBuilder} using the underlying {@link ItemStack}.
     *
     * @return A {@link SpawnerBuilder}.
     * @throws InvalidItemBuilderException If {@link #isSpawner()} is {@code false}.
     */
    @NotNull
    public SpawnerBuilder asSpawnerBuilder() {
        if (!isSpawner()) {
            throw new InvalidItemBuilderException(this, "This item type is not a spawner.");
        }
        return new SpawnerBuilder(this.itemStack);
    }

    /**
     * Parses the provided string using Placeholder API if available with the provided {@link Audience}
     * as the target for placeholders.
     *
     * @param message  The message to be parsed.
     * @param audience The {@link Audience} to use as the target for placeholders.
     * @return A parsed {@link Component}.
     */
    @NotNull
    protected Component parseString(@NotNull String message, @Nullable Audience audience) {
        var papiHookOptional = corePlugin.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHook(CorePluginHookKey.CORE_PAPI);
        return miniMessage.deserialize(message, getPlaceHolders(papiHookOptional.orElse(null), audience)).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Parses the provided {@link Component} using Placeholder API if available with the provided {@link Audience}
     * as the target for placeholders.
     *
     * @param message The message to be parsed.
     * @return A parsed {@link Component}.
     */
    @NotNull
    protected Component parseComponent(@NotNull Component message) {
        for (TextReplacementConfig config : getPlaceholdersAsConfig()) {
            message = message.replaceText(config);
        }
        message.decoration(TextDecoration.ITALIC, false);
        return message;
    }

    /**
     * Converts the stored placeholders into ones that {@link net.kyori.adventure.Adventure} can accept.
     *
     * @return An array of {@link TagResolver.Single}s.
     */
    @NotNull
    protected TagResolver[] getPlaceHolders(@Nullable CorePapiHook papiHook, @Nullable Audience audience) {
        TagResolver[] placeholderArray = new TagResolver.Single[placeholders.size() + (papiHook != null && audience instanceof Player ? 1 : 0)];
        int index = 0;
        for (String key : placeholders.keySet()) {
            String value = placeholders.get(key);
            TagResolver.Single placeholder = Placeholder.parsed(key, value);
            placeholderArray[index++] = placeholder;
        }
        if (papiHook != null && audience instanceof Player player) {
            placeholderArray[index] = papiHook.getTagResolver(player);
        }
        return placeholderArray;
    }

    /**
     * Converts the stored placeholders into ones that {@link net.kyori.adventure.Adventure} can accept.
     *
     * @return An array of {@link TagResolver.Single}s.
     */
    @NotNull
    protected List<TextReplacementConfig> getPlaceholdersAsConfig() {
        List<TextReplacementConfig> configs = new ArrayList<>();
        for (String key : placeholders.keySet()) {
            TextReplacementConfig.Builder textReplacementConfig = TextReplacementConfig.builder();
            String value = placeholders.get(key);
            textReplacementConfig.condition(getReplacementCondition());
            textReplacementConfig.matchLiteral("<" + key + ">");
            textReplacementConfig.replacement(value);
            configs.add(textReplacementConfig.build());
        }
        return configs;
    }

    /**
     * Gets the {@link Material} of the underlying {@link ItemStack}.
     *
     * @return The {@link Material} of the underlying {@link ItemStack}.
     */
    @NotNull
    public final Material getType() {
        return this.itemStack.getType();
    }

    /**
     * Sets the underlying {@link ItemStack} for this builder.
     *
     * @param itemStack The new {@link ItemStack} to use for this builder.
     */
    @NotNull
    public final B setItemStack(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
        return (B) this;
    }

    /**
     * Returns the underlying {@link ItemStack} being mutated by this builder.
     *
     * @return The underlying {@link ItemStack} being mutated by this builder.
     */
    @NotNull
    protected final ItemStack getItemStack() {
        return this.itemStack;
    }

    @NotNull
    protected final TextReplacementConfig.Condition getReplacementCondition() {
        return (result, matchCount, replaced) -> PatternReplacementResult.REPLACE;
    }
}

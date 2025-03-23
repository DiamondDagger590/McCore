package com.diamonddagger590.mccore.builder.item.impl;

import com.diamonddagger590.mccore.builder.item.BaseItemBuilder;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import static com.diamonddagger590.mccore.util.Methods.getColor;

/**
 * An item builder implementation that is used to build potions.
 */
public class PotionBuilder extends BaseItemBuilder<PotionBuilder> {

    private final PotionContents.Builder builder;

    public PotionBuilder(@NotNull final ItemStack itemStack) {
        super(itemStack);

        this.builder = PotionContents.potionContents();
    }

    /**
     * Creates a {@link PotionEffect} to add to this builder.
     * @param potionEffectType The {@link PotionEffectType} to use.
     * @param duration The duration of the effect.
     * @param amplifier The amplifier of the effect.
     * @param isAmbient Should the particles of the effect be less visible.
     * @param isParticles Should the effect give off particles.
     * @param hasIcon Should the effect use an icon.
     * @return This builder.
     */
    @NotNull
    public PotionBuilder withPotionEffect(final PotionEffectType potionEffectType, final int duration, final int amplifier, final boolean isAmbient, final boolean isParticles, final boolean hasIcon) {
        this.builder.addCustomEffect(new PotionEffect(potionEffectType, duration, amplifier).withAmbient(isAmbient).withParticles(isParticles).withIcon(hasIcon));
        return this;
    }

    /**
     * Creates a {@link PotionEffect} to add to this builder.
     * @param potionEffectType The {@link PotionEffectType} to use.
     * @param duration The duration of the effect.
     * @param amplifier The amplifier of the effect.
     * @return This builder.
     */
    @NotNull
    public PotionBuilder withPotionEffect(final PotionEffectType potionEffectType, final int duration, final int amplifier) {
        return withPotionEffect(potionEffectType, duration, amplifier, true, true, true);
    }

    /**
     * Creates a {@link PotionEffect} to add to this builder.
     * @param potionType The {@link PotionType} to use.
     * @return This builder.
     */
    @NotNull
    public PotionBuilder withPotionType(final PotionType potionType) {
        this.builder.potion(potionType);
        return this;
    }

    /**
     * Sets the suffix to the translation key of the potion item.
     * @param customName The custom name of this effect.
     * @return This builder.
     */
    @NotNull
    public PotionBuilder withCustomName(final String customName) {
        this.builder.customName(customName);
        return this;
    }

    @NotNull
    @Override
    public PotionBuilder setColor(@NotNull final String value) {
        this.builder.customColor(getColor(value));
        return this;
    }

    @NotNull
    @Override
    public PotionBuilder build() {
        getItemStack().setData(DataComponentTypes.POTION_CONTENTS, this.builder.build());
        return this;
    }
}

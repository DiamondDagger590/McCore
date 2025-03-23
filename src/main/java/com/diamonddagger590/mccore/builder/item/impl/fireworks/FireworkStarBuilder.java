package com.diamonddagger590.mccore.builder.item.impl.fireworks;

import com.diamonddagger590.mccore.builder.item.BaseItemBuilder;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A supporting item builder for {@link FireworkBuilder} that focuses
 * on building {@link FireworkEffect}s.
 */
public class FireworkStarBuilder extends BaseItemBuilder<FireworkStarBuilder> {

    private final FireworkEffect.Builder builder;

    public FireworkStarBuilder(@NotNull final ItemStack itemStack) {
        super(itemStack);
        this.builder = FireworkEffect.builder();
    }

    /**
     * Sets the underlying {@link FireworkEffect} to flicker or not.
     *
     * @param flicker If the effect should flicker or not.
     * @return This builder.
     */
    @NotNull
    public FireworkStarBuilder flicker(final boolean flicker) {
        this.builder.flicker(flicker);
        return this;
    }

    /**
     * Sets the underlying {@link FireworkEffect} to have a trail or not.
     *
     * @param trail If the effect should have a trail or not.
     * @return This builder.
     */
    @NotNull
    public FireworkStarBuilder trail(final boolean trail) {
        this.builder.trail(trail);
        return this;
    }

    /**
     * Sets the {@link Color} to use for the underlying {@link FireworkEffect}.
     *
     * @param color The {@link Color} to use.
     * @return This builder.
     */
    @NotNull
    public FireworkStarBuilder withColor(final Color color) {
        this.builder.withColor(color);
        return this;
    }

    /**
     * Sets the {@link Color}s to use for the underlying {@link FireworkEffect}.
     *
     * @param colors The {@link Color}s to use.
     * @return This builder.
     */
    @NotNull
    public FireworkStarBuilder withColor(@NotNull final Color... colors) {
        this.builder.withColor(colors);
        return this;
    }

    /**
     * Sets the {@link Color}s to use for the underlying {@link FireworkEffect}.
     *
     * @param colors The {@link Color}s to use.
     * @return This builder.
     */
    @NotNull
    public FireworkStarBuilder withColor(@NotNull final List<Color> colors) {
        this.builder.withColor(colors);
        return this;
    }

    /**
     * Sets the {@link Color} to use for fading on the underlying {@link FireworkEffect}.
     *
     * @param color The {@link Color} to use.
     * @return This builder.
     */
    @NotNull
    public FireworkStarBuilder withFade(final Color color) {
        this.builder.withFade(color);
        return this;
    }

    /**
     * Sets the {@link Color}s to use for fading on the underlying {@link FireworkEffect}.
     *
     * @param colors The {@link Color}s to use.
     * @return This builder.
     */
    @NotNull
    public FireworkStarBuilder withFade(@NotNull final Color... colors) {
        this.builder.withFade(colors);
        return this;
    }

    /**
     * Sets the {@link Color}s to use for fading on the underlying {@link FireworkEffect}.
     *
     * @param colors The {@link Color}s to use.
     * @return This builder.
     */
    @NotNull
    public FireworkStarBuilder withFade(@NotNull final List<Color> colors) {
        this.builder.withFade(colors);
        return this;
    }

    /**
     * Sets the {@link FireworkEffect.Type} for the underlying {@link FireworkEffect}.
     *
     * @param type The {@link FireworkEffect.Type} to use.
     * @return This builder.
     */
    @NotNull
    public FireworkStarBuilder with(final FireworkEffect.Type type) {
        this.builder.with(type);
        return this;
    }

    /**
     * Gets the underlying {@link FireworkEffect.Builder}.
     *
     * @return The underlying {@link FireworkEffect.Builder}.
     */
    @NotNull
    public FireworkEffect.Builder getBuilder() {
        return this.builder;
    }

    @NotNull
    @Override
    public FireworkStarBuilder build() {
        getItemStack().setData(DataComponentTypes.FIREWORK_EXPLOSION, this.builder.build());
        return this;
    }
}

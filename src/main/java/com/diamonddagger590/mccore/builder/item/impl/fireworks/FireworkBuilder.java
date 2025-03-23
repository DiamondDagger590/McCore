package com.diamonddagger590.mccore.builder.item.impl.fireworks;

import com.diamonddagger590.mccore.builder.item.BaseItemBuilder;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Fireworks;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * An item builder that builds fire work items.
 */
public class FireworkBuilder extends BaseItemBuilder<FireworkBuilder> {

    private final Fireworks.Builder builder;

    public FireworkBuilder(@NotNull final ItemStack itemStack) {
        super(itemStack);
        this.builder = Fireworks.fireworks();
    }

    /**
     * Adds the provided {@link FireworkEffect} to this builder.
     *
     * @param effect The {@link FireworkEffect} to add.
     * @return This builder.
     */
    @NotNull
    public FireworkBuilder addEffect(@NotNull final FireworkEffect effect) {
        this.builder.addEffect(effect);
        return this;
    }

    /**
     * Creates a new {@link FireworkEffect} to add to this builder.
     *
     * @param flicker    If the effect should flicker when it explodes.
     * @param trail      If the firework should have a trail on its way up.
     * @param type       The {@link FireworkEffect.Type} to use for the effect.
     * @param colors     A {@link List} of {@link Color}s to use for the effect.
     * @param fadeColors A {@link List} of {@link Color}s to use for the fade out of the effect.
     * @return This builder.
     */
    @NotNull
    public FireworkBuilder addEffect(final boolean flicker, final boolean trail, final FireworkEffect.Type type, @Nullable final List<Color> colors, @Nullable final List<Color> fadeColors) {
        final FireworkStarBuilder builder = new FireworkStarBuilder(getItemStack());
        builder.flicker(flicker);
        builder.trail(trail);
        builder.with(type);

        if (colors != null) {
            builder.withColor(colors);
        }
        if (fadeColors != null) {
            builder.withFade(fadeColors);
        }
        return addEffect(builder.getBuilder().build());
    }

    /**
     * Sets the flight duration of the firework being built.
     *
     * @param duration The duration to use.
     * @return This builder.
     */
    @NotNull
    public FireworkBuilder withDuration(final int duration) {
        this.builder.flightDuration(duration);
        return this;
    }

    @NotNull
    @Override
    public FireworkBuilder build() {
        getItemStack().setData(DataComponentTypes.FIREWORKS, this.builder.build());
        return this;
    }
}

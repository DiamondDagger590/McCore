package com.diamonddagger590.mccore.builder.item.impl;

import com.diamonddagger590.mccore.builder.item.BaseItemBuilder;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BannerPatternLayers;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.diamonddagger590.mccore.util.Methods.getDyeColor;
import static com.diamonddagger590.mccore.util.Methods.getPatternType;

/**
 * An item builder implementation that is used to build {@link Pattern}s for items.
 */
public class PatternBuilder extends BaseItemBuilder<PatternBuilder> {

    private final BannerPatternLayers.Builder builder;

    public PatternBuilder(@NotNull final ItemStack itemStack) {
        super(itemStack);
        this.builder = BannerPatternLayers.bannerPatternLayers();
    }

    /**
     * Adds the provided {@link Pattern} to this builder.
     * @param pattern The {@link Pattern} to add.
     * @return This builder.
     */
    @NotNull
    public PatternBuilder addPattern(@NotNull final Pattern pattern) {
        this.builder.add(pattern);
        return this;
    }

    /**
     * Creates a {@link Pattern} from the provided pattern string and dye to add to this builder.
     * @param pattern The string representation of a {@link Pattern}.
     * @param dye The string representation of a {@link DyeColor}.
     * @return This builder.
     */
    @NotNull
    public PatternBuilder addPattern(@NotNull final String pattern, @NotNull final String dye) {
        Optional<PatternType> type = getPatternType(pattern.toLowerCase());
        if (type.isEmpty()) {
            return this;
        }
        final DyeColor color = getDyeColor(dye);
        return addPattern(new Pattern(color, type.get()));
    }

    @NotNull
    @Override
    public PatternBuilder build() {
        getItemStack().setData(DataComponentTypes.BANNER_PATTERNS, this.builder.build());
        return this;
    }
}

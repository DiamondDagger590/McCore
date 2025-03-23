package com.diamonddagger590.mccore.exception.builder.item;

import com.diamonddagger590.mccore.builder.item.BaseItemBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * This exception is thrown whenever a {@link BaseItemBuilder} implementation
 * is attempted to be obtained from an item builder of which the underlying {@link org.bukkit.inventory.ItemStack}
 * doesn't match.
 */
public class InvalidItemBuilderException extends RuntimeException {

    private final BaseItemBuilder<?> builder;

    public InvalidItemBuilderException(@NotNull BaseItemBuilder<?> builder, @NotNull String message) {
        super(message);
        this.builder = builder;
    }

    /**
     * Gets the {@link BaseItemBuilder} that caused this error.
     *
     * @return The {@link BaseItemBuilder} that caused this error.
     */
    @NotNull
    public BaseItemBuilder<?> getBuilder() {
        return builder;
    }
}

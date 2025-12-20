package com.diamonddagger590.mccore.external.nexo;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.external.common.CustomBlockHook;
import com.diamonddagger590.mccore.external.common.CustomItemHook;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

/**
 * The hook needed to support <a href="https://polymart.org/resource/nexo.6901">Nexo</a> for this plugin.
 */
public class CoreNexoHook extends PluginHook<CorePlugin> implements CustomItemHook, CustomBlockHook {

    public CoreNexoHook(@NotNull CorePlugin corePlugin) {
        super(corePlugin);
    }

    /**
     * Gets the Nexo {@link ItemStack} representation from the provided item.
     *
     * @param item The item to get the Nexo {@link ItemStack} from.
     * @return An {@link Optional} containing the Nexo {@link ItemStack} representation
     * from the provided item.
     */
    @NotNull
    @Override
    public Optional<ItemStack> item(@NotNull final String item) {
        ItemBuilder itemBuilder = NexoItems.itemFromId(item);
        if (itemBuilder == null) {
            return Optional.empty();
        }
        return Optional.of(itemBuilder.build());
    }

    @NotNull
    @Override
    public Optional<Set<String>> itemModels(@NotNull ItemStack itemStack) {
        String itemId = NexoItems.idFromItem(itemStack);
        return Optional.ofNullable(itemId == null ? null : Set.of(itemId));
    }

    /**
     * Checks to see if the provided item is a valid Nexo item.
     *
     * @param item The item to check.
     * @return {@code true} if the provided item is a valid Nexo item.
     */
    @Override
    public boolean isItem(@NotNull final String item) {
        return NexoItems.exists(item);
    }

    @Override
    public boolean isItem(@NotNull ItemStack itemStack) {
        return NexoItems.exists(itemStack);
    }

    @Override
    public boolean isItemOfType(@NotNull ItemStack itemStack, @NotNull String itemName) {
        String nexoItem = NexoItems.idFromItem(itemStack);
        return nexoItem != null && nexoItem.equalsIgnoreCase(itemName);
    }

    /**
     * Checks to see if the provided {@link Block} is currently a Nexo block.
     *
     * @param block The block to validate.
     * @return {@code true} if the provided {@link Block} is currently a Nexo block.
     */
    @Override
    public boolean isCustomBlock(@NotNull Block block) {
        return NexoBlocks.isCustomBlock(block);
    }

    @Override
    public boolean isCustomBlock(@NotNull String customBlock) {
        return NexoBlocks.isCustomBlock(customBlock);
    }

    /**
     * Checks to see if the provided {@link Block} currently has the itemId provided.
     *
     * @param block           The block to validate.
     * @param customBlockType The itemId to check.
     * @return {@code true} if the provided {@link Block} currently has the itemId provided.
     */
    @Override
    public boolean isCustomBlockOfType(@NotNull Block block, @NotNull String customBlockType) {
        var customMechanic = NexoBlocks.customBlockMechanic(block.getLocation());
        return isCustomBlock(block) && customMechanic != null && customMechanic.getItemID().equalsIgnoreCase(customBlockType);
    }

    @NotNull
    @Override
    public Optional<Set<String>> blockModels(@NotNull Block block) {
        CustomBlockMechanic customBlockMechanic = NexoBlocks.customBlockMechanic(block.getLocation());
        if (customBlockMechanic == null) {
            return Optional.empty();
        }
        return Optional.of(Set.of(customBlockMechanic.getItemID()));
    }

    @Override
    public void placeCustomBlock(@NotNull Location location, @NotNull String blockId) {
        if (!isCustomBlock(blockId)) {
            throw new IllegalArgumentException("Block " + blockId + " is not a valid Nexo block.");
        }
        NexoBlocks.place(blockId, location);
    }
}

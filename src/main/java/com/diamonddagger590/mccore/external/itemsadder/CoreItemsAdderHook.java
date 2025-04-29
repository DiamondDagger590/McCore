package com.diamonddagger590.mccore.external.itemsadder;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.external.CustomBlockHook;
import com.diamonddagger590.mccore.external.CustomItemHook;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

/**
 * The hook needed to support
 * <a href="https://www.spigotmc.org/resources/%E2%9C%A8itemsadder%E2%AD%90emotes-mobs-items-armors-hud-gui-emojis-blocks-wings-hats-liquids.73355/">ItemsAdder</a>
 * for this plugin.
 */
public class CoreItemsAdderHook extends PluginHook<CorePlugin> implements CustomItemHook, CustomBlockHook {

    public CoreItemsAdderHook(@NotNull CorePlugin plugin) {
        super(plugin);
    }

    /**
     * Gets the ItemsAdder {@link ItemStack} representation from the provided item.
     *
     * @param item The item to get the ItemsAdder {@link ItemStack} from.
     * @return An {@link Optional} containing the ItemsAdder {@link ItemStack} representation
     * from the provided item.
     */
    @NotNull
    @Override
    public Optional<ItemStack> item(@NotNull final String item) {
        CustomStack customStack = CustomStack.getInstance(item);
        if (customStack == null) {
            return Optional.empty();
        }
        return Optional.of(customStack.getItemStack());
    }

    @NotNull
    @Override
    public Optional<Set<String>> itemModels(@NotNull ItemStack itemStack) {
        CustomStack customStack = CustomStack.byItemStack(itemStack);
        if (customStack == null) {
            return Optional.empty();
        }
        return Optional.of(Set.of(customStack.getModelPath()));
    }

    /**
     * Checks to see if the provided item is a valid ItemsAdder item.
     *
     * @param item The item to check.
     * @return {@code true} if the provided item is a valid ItemsAdder item.
     */
    @Override
    public boolean isItem(@NotNull final String item) {
        return CustomStack.isInRegistry(item);
    }

    @Override
    public boolean isItem(@NotNull ItemStack itemStack) {
        return CustomStack.byItemStack(itemStack) != null;
    }

    @Override
    public boolean isItemOfType(@NotNull ItemStack itemStack, @NotNull String itemName) {
        return CustomStack.byItemStack(itemStack) != null && CustomStack.byItemStack(itemStack).getModelPath().equalsIgnoreCase(itemName);
    }

    @Override
    public boolean isCustomBlock(@NotNull Block block) {
        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
        return customBlock != null;
    }

    @Override
    public boolean isCustomBlock(@NotNull String customBlock) {
        return false;
    }

    @Override
    public boolean isCustomBlockOfType(@NotNull Block block, @NotNull String customBlockType) {
        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
        return customBlock != null && customBlock.getModelPath().equalsIgnoreCase(customBlockType);
    }

    @NotNull
    @Override
    public Optional<Set<String>> blockModels(@NotNull Block block) {
        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
        if (customBlock == null) {
            return Optional.empty();
        }
        return Optional.of(Set.of(customBlock.getModelPath()));
    }
}

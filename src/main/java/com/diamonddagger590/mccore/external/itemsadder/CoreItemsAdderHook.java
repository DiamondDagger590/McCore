package com.diamonddagger590.mccore.external.itemsadder;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.external.common.CustomBlockHook;
import com.diamonddagger590.mccore.external.common.CustomItemHook;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import com.diamonddagger590.mccore.util.item.CustomBlockWrapper;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.SoundGroup;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Override
    public void placeCustomBlock(@NotNull Location location, @NotNull String blockId) {
        if (!isCustomBlock(blockId)) {
            throw new IllegalArgumentException("Block " + blockId + " is not a valid ItemsAdder block.");
        }
        CustomBlock.place(blockId, location);
    }

    @NotNull
    @Override
    public List<ItemStack> drops(@NotNull Block block, @NotNull ItemStack itemToBreakWith, @Nullable Entity entityBreaking) {
        if (isCustomBlock(block)) {
            return CustomBlock.getLoot(block, itemToBreakWith, true);
        } else {
            return List.copyOf(block.getDrops(itemToBreakWith, entityBreaking));
        }
    }

    @Override
    public void playBlockDropEffects(@NotNull Block block) {
        if (isCustomBlock(block)) {
            CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
            customBlock.playBreakEffect();
            customBlock.playBreakSound();
            customBlock.playBreakParticles();
            return;
        }
        World world = block.getWorld();
        BlockData data = block.getBlockData();
        SoundGroup soundGroup = block.getBlockSoundGroup();
        world.playSound(block.getLocation(), soundGroup.getBreakSound(), soundGroup.getVolume(), soundGroup.getPitch());
        world.spawnParticle(
                Particle.BLOCK,
                block.getLocation().clone().add(0.5, 0.5, 0.5),
                20,
                0.25, 0.25, 0.25,
                0.05,
                data);
    }

    @Override
    public void removeBlock(@NotNull Block block) {
        if (isCustomBlock(block)) {
            CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
            if(!customBlock.remove()) {
                throw new IllegalStateException("Failed to remove ItemsAdder block " + customBlock.getModelPath() + " at block " + block.getLocation());
            }
        } else {
            block.setType(Material.AIR);
        }
    }

    /**
     * Gets a player-friendly name for the block represented by the provided {@link CustomBlockWrapper}.
     * <p>
     * For ItemsAdder custom blocks, the item name is resolved via {@link CustomStack#itemName()},
     * falling back to {@link CustomStack#getDisplayName()} (with legacy color codes stripped).
     * If neither yields a name, the block ID is formatted into
     * title case (e.g. {@code "my_namespace:cool_block"} → {@code "Cool Block"}).
     * For vanilla materials, the material name is similarly title-cased.
     *
     * @param customBlockWrapper The {@link CustomBlockWrapper} to get the name of.
     * @return The player-friendly name for the block.
     */
    @Override
    @NotNull
    public String blockName(@NotNull CustomBlockWrapper customBlockWrapper) {
        if (customBlockWrapper.customBlock().isPresent()) {
            String blockId = customBlockWrapper.customBlock().get();
            CustomStack customStack = CustomStack.getInstance(blockId);
            if (customStack != null) {
                // itemName() returns the modern Adventure Component (ITEM_NAME)
                Component nameComponent = customStack.itemName();
                if (nameComponent != null) {
                    String name = PlainTextComponentSerializer.plainText().serialize(nameComponent);
                    if (!name.isEmpty()) {
                        return name;
                    }
                }
                // Fall back to legacy string display name, stripping any § color codes
                String legacyName = customStack.getDisplayName();
                if (legacyName != null && !legacyName.isEmpty()) {
                    return legacyName.replaceAll("§[0-9a-fklmnorA-FKLMNOR]", "");
                }
            }
            return formatBlockId(blockId);
        }
        return formatMaterial(customBlockWrapper.material().orElseThrow());
    }

    /**
     * Formats a namespaced block ID into a title-cased display string.
     * The namespace is stripped and underscores are replaced with spaces.
     * For example, {@code "my_namespace:cool_block"} becomes {@code "Cool Block"}.
     *
     * @param blockId The namespaced block ID to format.
     * @return A title-cased display string.
     */
    @NotNull
    private static String formatBlockId(@NotNull String blockId) {
        String key = blockId.contains(":") ? blockId.substring(blockId.indexOf(':') + 1) : blockId;
        return Arrays.stream(key.split("_"))
            .filter(word -> !word.isEmpty())
            .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }

    /**
     * Formats a {@link Material} name into a title-cased display string.
     * For example, {@link Material#OAK_LOG} becomes {@code "Oak Log"}.
     *
     * @param material The {@link Material} to format.
     * @return A title-cased display string.
     */
    @NotNull
    private static String formatMaterial(@NotNull Material material) {
        return Arrays.stream(material.name().split("_"))
            .filter(word -> !word.isEmpty())
            .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }
}

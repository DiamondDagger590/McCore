package com.diamonddagger590.mccore.external.nexo;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.external.common.CustomBlockHook;
import com.diamonddagger590.mccore.external.common.CustomItemHook;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import com.diamonddagger590.mccore.util.item.CustomBlockWrapper;
import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import com.nexomc.nexo.mechanics.breakable.Breakable;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.nexomc.nexo.utils.blocksounds.BlockSounds;
import com.nexomc.nexo.utils.drops.Loot;
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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    @NotNull
    @Override
    public List<ItemStack> drops(@NotNull Block block, @NotNull ItemStack itemToBreakWith, @Nullable Entity entityBreaking) {
        if (isCustomBlock(block)) {
            CustomBlockMechanic customBlockMechanic = NexoBlocks.customBlockMechanic(block.getLocation());
            if (customBlockMechanic != null && entityBreaking instanceof Player player) {
                Breakable breakable = customBlockMechanic.getBreakable();
                List<Loot> lootDrops = breakable.getDrop().lootToDrop(player);
                return lootDrops.stream().map(Loot::getItemStack).toList();
            } else {
                return List.of();
            }
        } else {
            return List.copyOf(block.getDrops(itemToBreakWith, entityBreaking));
        }
    }

    @Override
    public void removeBlock(@NotNull Block block) {
        if (isCustomBlock(block)) {
            if (!NexoBlocks.remove(block.getLocation())) {
                var customMechanic = NexoBlocks.customBlockMechanic(block.getLocation());
                throw new IllegalStateException("Failed to remove Nexo block " + customMechanic.getItemID() + " at block " + block.getLocation());
            }
        } else {
            block.setType(Material.AIR);
        }
    }

    @Override
    public void playBlockDropEffects(@NotNull Block block) {
        if (isCustomBlock(block)) {
            var customMechanic = NexoBlocks.customBlockMechanic(block.getLocation());
            if (customMechanic != null && customMechanic.hasBlockSounds()) {
                BlockSounds blockSounds = customMechanic.getBlockSounds();
                assert(blockSounds != null);
                block.getWorld().playSound(block.getLocation(), blockSounds.getBreakSound(), blockSounds.getBreakVolume(), blockSounds.getBreakPitch());
                return;
            }
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

    /**
     * Gets a player-friendly name for the block represented by the provided {@link CustomBlockWrapper}.
     * <p>
     * For Nexo custom blocks, the item name is resolved via {@link ItemBuilder#getItemName()}
     * when set, falling back to {@link ItemBuilder#getCustomName()}. If neither yields a name,
     * the block ID is formatted into
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
            if (isCustomBlock(blockId)) {
                ItemBuilder nexoItem = NexoItems.itemFromId(blockId);
                if (nexoItem != null) {
                    // Prefer itemName (ITEM_NAME / static non-italic name), fall back to customName (CUSTOM_NAME)
                    Component nameComponent = Boolean.TRUE.equals(nexoItem.hasItemName())
                        ? nexoItem.getItemName()
                        : nexoItem.getCustomName();
                    if (nameComponent != null) {
                        String name = PlainTextComponentSerializer.plainText().serialize(nameComponent);
                        if (!name.isEmpty()) {
                            return name;
                        }
                    }
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

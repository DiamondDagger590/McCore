package com.diamonddagger590.mccore.external.headdatabase;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.external.common.CustomItemHook;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The hook needed to support <a href="https://www.spigotmc.org/resources/head-database.14280/">HeadDatabase</a>
 * for this plugin.
 */
public class CoreHeadDatabaseHook extends PluginHook<CorePlugin> implements CustomItemHook {

    private final HeadDatabaseAPI headDatabaseAPI;

    public CoreHeadDatabaseHook(@NotNull final CorePlugin plugin) {
        super(plugin);
        this.headDatabaseAPI = new HeadDatabaseAPI();
    }

    /**
     * Checks to see if the provided item is a HeadDatabase head.
     *
     * @param item The item to check.
     * @return {@code true} if the provided item is a HeadDatabase head.
     */
    @Override
    public boolean isItem(@NotNull final String item) {
        return headDatabaseAPI.isHead(item);
    }

    @Override
    public boolean isItem(@NotNull ItemStack itemStack) {
        return headDatabaseAPI.getItemID(itemStack) != null;
    }

    @Override
    public boolean isItemOfType(@NotNull ItemStack itemStack, @NotNull String itemName) {
        return headDatabaseAPI.getItemID(itemStack).equalsIgnoreCase(itemName);
    }

    /**
     * Gets the HeadDatabase {@link ItemStack} representation from the provided item.
     *
     * @param item The item to get the HeadDatabase {@link ItemStack} from.
     * @return An {@link Optional} containing the HeadDatabase {@link ItemStack} representation
     * from the provided item.
     */
    @NotNull
    @Override
    public Optional<ItemStack> item(@NotNull final String item) {
        return Optional.ofNullable(headDatabaseAPI.getItemHead(item));
    }

    @NotNull
    @Override
    public Optional<Set<String>> itemModels(@NotNull ItemStack itemStack) {
        String itemID = headDatabaseAPI.getItemID(itemStack);
        return Optional.ofNullable(itemID == null ? null : Set.of(itemID));
    }

    /**
     * Gets a player-friendly name for the item represented by the provided {@link CustomItemWrapper}.
     * <p>
     * For HeadDatabase custom items, the head is fetched via its ID and the display name is
     * extracted from the item meta. If no name is found, the head ID is formatted into
     * title case. For vanilla materials, the material name is similarly title-cased.
     *
     * @param customItemWrapper The {@link CustomItemWrapper} to get the name of.
     * @return The player-friendly name for the item.
     */
    @Override
    @NotNull
    public String itemName(@NotNull CustomItemWrapper customItemWrapper) {
        if (customItemWrapper.customItem().isPresent()) {
            String headId = customItemWrapper.customItem().get();
            ItemStack head = headDatabaseAPI.getItemHead(headId);
            if (head != null && head.hasItemMeta()) {
                ItemMeta meta = head.getItemMeta();
                if (meta.hasDisplayName()) {
                    String name = PlainTextComponentSerializer.plainText().serialize(meta.displayName());
                    if (!name.isEmpty()) {
                        return name;
                    }
                }
            }
            return formatItemId(headId);
        }
        return formatMaterial(customItemWrapper.material().orElseThrow());
    }

    /**
     * Formats a namespaced item ID into a title-cased display string.
     * The namespace is stripped and underscores are replaced with spaces.
     *
     * @param itemId The namespaced item ID to format.
     * @return A title-cased display string.
     */
    @NotNull
    private static String formatItemId(@NotNull String itemId) {
        String key = itemId.contains(":") ? itemId.substring(itemId.indexOf(':') + 1) : itemId;
        return Arrays.stream(key.split("_"))
            .filter(word -> !word.isEmpty())
            .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
            .collect(Collectors.joining(" "));
    }

    /**
     * Formats a {@link Material} name into a title-cased display string.
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

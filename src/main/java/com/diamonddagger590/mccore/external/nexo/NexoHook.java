package com.diamonddagger590.mccore.external.nexo;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * The hook needed to support <a href="https://polymart.org/resource/nexo.6901">Nexo</a> for this plugin.
 */
public class NexoHook extends PluginHook {

    public NexoHook(@NotNull CorePlugin corePlugin) {
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
    public Optional<ItemStack> getNexoItem(@NotNull final String item) {
        ItemBuilder itemBuilder = NexoItems.itemFromId(item);
        if (itemBuilder == null) {
            return Optional.empty();
        }
        return Optional.of(itemBuilder.build());
    }

    /**
     * Checks to see if the provided item is a valid Nexo item.
     *
     * @param item The item to check.
     * @return {@code true} if the provided item is a valid Nexo item.
     */
    public boolean doesNexoItemExist(@NotNull final String item) {
        return NexoItems.exists(item);
    }
}

package com.diamonddagger590.mccore.external.mythicmobs;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.external.common.CustomEntityHook;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * The hook needed to support
 * <a href="https://www.spigotmc.org/resources/%E2%9A%94-mythicmobs-free-version-%E2%96%BAthe-1-custom-mob-creator%E2%97%84.5702/">MythicMobs</a>
 * for this plugin.
 */
public class CoreMythicMobsHook extends PluginHook<CorePlugin> implements CustomEntityHook {

    public CoreMythicMobsHook(@NotNull CorePlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean isCustomEntity(@NotNull UUID uuid) {
        return MythicBukkit.inst().getMobManager().isActiveMob(uuid);
    }

    @Override
    public boolean isCustomEntity(@NotNull String customEntity) {
        return MythicBukkit.inst().getMobManager().getMythicMob(customEntity).isPresent();
    }

    @Override
    public boolean isCustomEntityOfType(@NotNull UUID uuid, @NotNull String customEntityType) {
        var activeMobOptional = MythicBukkit.inst().getMobManager().getActiveMob(uuid);
        return activeMobOptional.isPresent() && activeMobOptional.get().getMobType().equalsIgnoreCase(customEntityType);
    }

    @NotNull
    @Override
    public Optional<Set<String>> entityModels(@NotNull Entity entity) {
        var activeMobOptional = MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId());
        return activeMobOptional.map(activeMob -> Set.of(activeMob.getMobType()));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Resolves the display name configured on the MythicMobs mob type definition.
     * Falls back to the raw mob type ID if no mob type is found for the custom entity identifier.
     */
    @NotNull
    @Override
    public String entityName(@NotNull CustomEntityWrapper customEntityWrapper) {
        return customEntityWrapper.customEntity()
                .flatMap(id -> MythicBukkit.inst().getMobManager().getMythicMob(id))
                .map(mob -> mob.getDisplayName().get())
                .orElseGet(() -> customEntityWrapper.customEntity().orElse("Unknown"));
    }
}

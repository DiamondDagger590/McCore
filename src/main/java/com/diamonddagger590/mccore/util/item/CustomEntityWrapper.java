package com.diamonddagger590.mccore.util.item;

import com.diamonddagger590.mccore.external.common.CustomEntityHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.Methods;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A wrapper around {@link Entity Entities}, allowing a {@link EntityType}
 * and a custom entity model (used by model plugins) to be used interchangeably.
 * <p>
 * This is handy when a plugin wants to provide configuration options for server
 * owners where there doesn't need to be a distinct difference between vanilla and
 * custom entities.
 * <p>
 * It's expected that a wrapper will either have a entity type or a custom entity model but
 * never both or neither.
 */
public class CustomEntityWrapper {

    @Nullable
    private final EntityType entityType;
    @Nullable
    private final String customEntity;

    public CustomEntityWrapper(@NotNull EntityType entityType) {
        this.entityType = entityType;
        this.customEntity = null;
    }

    public CustomEntityWrapper(@NotNull String customEntity) {
        EntityType entityType = lookupVanillaEntityType(customEntity);
        if (entityType != null) {
            this.entityType = entityType;
            this.customEntity = null;
        }
        else {
            this.entityType = null;
            this.customEntity = customEntity;
        }
    }

    public CustomEntityWrapper(@NotNull Entity entity) {
        List<CustomEntityHook> pluginHooks = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHooks(CustomEntityHook.class);
        String customEntityResult = null;
        for (CustomEntityHook hook : pluginHooks) {
            if (hook.isCustomEntity(entity)) {
                var entityModelsOptional = hook.entityModels(entity);
                if (entityModelsOptional.isPresent() && !entityModelsOptional.get().isEmpty()) {
                    customEntityResult =  entityModelsOptional.get().iterator().next();
                    break;
                }
            }
        }
        this.entityType = customEntityResult == null ? entity.getType() : null;
        this.customEntity = customEntityResult;
    }

    /**
     * Checks whether this wrapper represents a vanilla entity (resolved via the Paper registry).
     *
     * @return {@code true} if this wrapper holds an {@link EntityType}, meaning the entity
     *         was recognized as a vanilla Minecraft entity.
     */
    public boolean isVanilla() {
        return entityType != null;
    }

    /**
     * Checks whether this wrapper represents a custom entity (from a model plugin).
     *
     * @return {@code true} if this wrapper holds a custom entity identifier, meaning the entity
     *         was not recognized as a vanilla Minecraft entity.
     */
    public boolean isCustom() {
        return customEntity != null;
    }

    /**
     * Checks whether the given identifier resolves to a vanilla entity in the Paper entity type registry.
     *
     * @param id The entity identifier to check (e.g. {@code "zombie"}, {@code "creeper"}).
     * @return {@code true} if the identifier is recognized as a vanilla entity type.
     */
    public static boolean isVanillaEntity(@NotNull String id) {
        return lookupVanillaEntityType(id) != null;
    }

    /**
     * Looks up an entity identifier in the Paper entity type registry.
     *
     * @param id The entity identifier to look up (e.g. {@code "zombie"}, {@code "creeper"}).
     * @return The {@link EntityType} if the identifier is a vanilla entity, or {@code null} if not found.
     */
    @Nullable
    private static EntityType lookupVanillaEntityType(@NotNull String id) {
        return io.papermc.paper.registry.RegistryAccess.registryAccess()
                .getRegistry(io.papermc.paper.registry.RegistryKey.ENTITY_TYPE)
                .get(Methods.getMinecraftKey(id));
    }

    /**
     * Gets an {@link Optional} containing the {@link EntityType} represented
     * by this wrapper.
     *
     * @return An {@link Optional} containing the {@link EntityType} represented
     * by this wrapper. If this optional is empty, it can be assumed that
     * {@link #customEntity()} will not return an empty optional.
     */
    @NotNull
    public Optional<EntityType> entityType() {
        return Optional.ofNullable(entityType);
    }

    /**
     * Gets an {@link Optional} containing the custom entity model represented
     * by this wrapper.
     *
     * @return An {@link Optional} containing the custom entity model represented
     * by this wrapper. If this optional is empty, it can be assumed that
     * {@link #entityType()} will not return an empty optional.
     */
    @NotNull
    public Optional<String> customEntity() {
        return Optional.ofNullable(customEntity);
    }

    /**
     * Returns a player-friendly display name for this entity as a MiniMessage string.
     * <p>
     * For vanilla entity types this returns a MiniMessage {@code <lang:key>} tag (e.g.
     * {@code <lang:entity.minecraft.zombie>}), which the Minecraft client resolves to the
     * entity's localized name in the player's own language.
     * <p>
     * For custom entities the name is resolved via the registered {@link CustomEntityHook}, falling
     * back to {@code "Unknown"} if no hook recognises the custom entity identifier.
     *
     * @return A MiniMessage string representing the display name of this entity.
     */
    @NotNull
    public String entityName() {
        if (customEntity != null) {
            List<CustomEntityHook> pluginHooks = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHooks(CustomEntityHook.class);
            for (CustomEntityHook hook : pluginHooks) {
                if (hook.isCustomEntity(customEntity)) {
                    return hook.entityName(this);
                }
            }
            return "Unknown";
        } else {
            return "<lang:" + entityType.translationKey() + ">";
        }
    }

    /**
     * Checks to see if the provided {@link EntityType} equals this wrapper.
     *
     * @param entityType The entity type to check.
     * @return {@code true} of the provided {@link EntityType} equals this wrapper.
     */
    public boolean equals(@NotNull EntityType entityType) {
        return this.entityType != null && this.entityType.equals(entityType);
    }

    /**
     * Checks to see if the provided {@link Entity} equals this
     * wrapper.
     *
     * @param entity The {@link Entity} to check.
     * @return {@code true} if the provided {@link Entity} equals
     * this wrapper.
     */
    public boolean equals(@NotNull Entity entity) {
        if (customEntity != null) {
            List<CustomEntityHook> customEntityHooks = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHooks(CustomEntityHook.class);
            for (CustomEntityHook customEntityHook : customEntityHooks) {
                if (customEntityHook.isCustomEntityOfType(entity, customEntity)) {
                    return true;
                }
            }
            return false;
        } else {
            return entityType == entity.getType();
        }
    }

    /**
     * Checks to see if the provided custom entity equals
     * this wrapper.
     *
     * @param customEntity The custom entity to check.
     * @return {@code true} if the provided custom entity equals
     * this wrapper.
     */
    public boolean equals(@NotNull String customEntity) {
        return this.customEntity != null && this.customEntity.equals(customEntity);
    }

    /**
     * Checks to see if the provided wrapper equals
     * this wrapper.
     *
     * @param customEntity The wrapper to check.
     * @return {@code true} if the provided wrapper equals
     * this wrapper.
     */
    public boolean equals(@NotNull CustomEntityWrapper customEntity) {
        if (this.customEntity != null) {
            return this.customEntity.equals(customEntity.customEntity);
        }
        return this.entityType == customEntity.entityType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomEntityWrapper other)) return false;

        if (this.entityType != null && other.entityType != null) {
            return this.entityType == other.entityType;
        }

        if (this.customEntity != null && other.customEntity != null) {
            return this.customEntity.equals(other.customEntity);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return entityType != null ? entityType.hashCode() : customEntity.hashCode();
    }

    /**
     * Gets an {@link Optional} containing all the entity models that the provided {@link Entity}
     * currently has on it.
     *
     * @param entity The {@link Entity} to get custom models from.
     * @return An {@link Optional} containing all the entity models that the item models that the
     * provided {@link Entity} currently has on it.
     */
    @NotNull
    public static Optional<Set<String>> customModels(@NotNull Entity entity) {
        List<CustomEntityHook> pluginHooks = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHooks(CustomEntityHook.class);
        Set<String> customModels = new HashSet<>();
        for (CustomEntityHook hook : pluginHooks) {
            hook.entityModels(entity).ifPresent(customModels::addAll);
        }
        return Optional.of(customModels);
    }
}

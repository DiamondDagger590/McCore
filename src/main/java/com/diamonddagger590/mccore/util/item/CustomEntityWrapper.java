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
import java.util.Locale;
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
        EntityType entityType = io.papermc.paper.registry.RegistryAccess.registryAccess().getRegistry(io.papermc.paper.registry.RegistryKey.ENTITY_TYPE).get(Methods.getMinecraftKey(customEntity));
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
        return this.customEntity != null && this.customEntity.equalsIgnoreCase(customEntity);
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
            return this.customEntity.equalsIgnoreCase(customEntity.customEntity);
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
            return this.customEntity.equalsIgnoreCase(other.customEntity);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return entityType != null ? entityType.hashCode() : customEntity.toLowerCase(Locale.ROOT).hashCode();
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

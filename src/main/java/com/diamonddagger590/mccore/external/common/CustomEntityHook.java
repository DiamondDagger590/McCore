package com.diamonddagger590.mccore.external.common;

import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A type of {@link com.diamonddagger590.mccore.registry.plugin.PluginHook} which supports custom entity models.
 */
public interface CustomEntityHook {

    /**
     * Checks to see if the provided {@link Entity} has a custom model on it.
     *
     * @param entity The block to check.
     * @return {@code true} if the provided {@link Entity} has a custom model on it.
     */
    default boolean isCustomEntity(@NotNull Entity entity) {
        return isCustomEntity(entity.getUniqueId());
    }

    /**
     * Checks to see if the {@link Entity} belonging to the provided {@link UUID} has a custom model on it.
     *
     * @param uuid The uuid to check.
     * @return {@code true} if the {@link Entity} belonging to the provided {@link UUID} has a custom model on it.
     */
    boolean isCustomEntity(@NotNull UUID uuid);

    /**
     * Checks to see if the provided entity model is a valid custom model.
     *
     * @param customEntity The model name to check.
     * @return {@code true} if the provided entity model is a valid custom model.
     */
    boolean isCustomEntity(@NotNull String customEntity);

    /**
     * Checks to see if the provided {@link CustomEntityWrapper} is an instance of a custom entity.
     *
     * @param customEntityWrapper The wrapper to check.
     * @return {@code true} if the provided {@link CustomEntityWrapper} is an instance of a custom entity.
     */
    default boolean isCustomEntity(@NotNull CustomEntityWrapper customEntityWrapper) {
        return customEntityWrapper.customEntity().map(this::isCustomEntity).orElse(false);
    }

    /**
     * Checks to see if the provided {@link Entity} has the specified model on it.
     *
     * @param entity           The entity to check.
     * @param customEntityType The custom model to check.
     * @return {@code true} if the provided {@link Entity} has the specified model on it.
     */
    default boolean isCustomEntityOfType(@NotNull Entity entity, @NotNull String customEntityType) {
        return isCustomEntityOfType(entity.getUniqueId(), customEntityType);
    }

    /**
     * Checks to see if the {@link Entity} belonging to the provided {@link UUID} has the specified model on it.
     *
     * @param uuid             The uuid to check.
     * @param customEntityType The custom model to check.
     * @return {@code true} if the {@link Entity} belonging to the provided {@link UUID} has the specified model on it.
     */
    boolean isCustomEntityOfType(@NotNull UUID uuid, @NotNull String customEntityType);

    /**
     * Gets an {@link Optional} containing all the models that the provided {@link Entity} has on it.
     *
     * @param entity The entity to get the models from.
     * @return An {@link Optional} containing all the models that the provided {@link Entity} has on it. This
     * optional will be empty if there are no models on the entity.
     */
    @NotNull
    Optional<Set<String>> entityModels(@NotNull Entity entity);

    /**
     * Returns a player-friendly display name for the custom entity represented by the provided
     * {@link CustomEntityWrapper} as a MiniMessage string.
     *
     * @param customEntityWrapper The {@link CustomEntityWrapper} to get the name of.
     * @return A MiniMessage string representing the display name of this entity.
     */
    @NotNull
    String entityName(@NotNull CustomEntityWrapper customEntityWrapper);
}

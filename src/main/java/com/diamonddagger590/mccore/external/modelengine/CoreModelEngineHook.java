package com.diamonddagger590.mccore.external.modelengine;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.external.common.CustomEntityHook;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * The hook needed to support
 * <a href="hhttps://mythiccraft.io/index.php?resources/model-engine%E2%80%94ultimate-entity-model-manager-1-19-4-1-21-4.1213/">Model Engine</a>
 * for this plugin.
 */
public class CoreModelEngineHook extends PluginHook<CorePlugin> implements CustomEntityHook {

    public CoreModelEngineHook(@NotNull CorePlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean isCustomEntity(@NotNull UUID entityUUID) {
        return ModelEngineAPI.isModeledEntity(entityUUID);
    }

    @Override
    public boolean isCustomEntity(@NotNull String customEntity) {
        return ModelEngineAPI.getBlueprint(customEntity) != null;
    }

    @Override
    public boolean isCustomEntityOfType(@NotNull UUID entityUUID, @NotNull String customEntityType) {
        if (isCustomEntity(entityUUID)) {
            ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(entityUUID);
            for (ActiveModel model : modeledEntity.getModels().values()) {
                if (model.getBlueprint().getName().equalsIgnoreCase(customEntityType)) {
                    return true;
                }
            }
        }
        return false;
    }

    @NotNull
    @Override
    public Optional<Set<String>> entityModels(@NotNull Entity entity) {
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(entity.getUniqueId());
        if (modeledEntity == null) {
            return Optional.empty();
        }
        return Optional.of(modeledEntity.getModels().keySet());
    }
}

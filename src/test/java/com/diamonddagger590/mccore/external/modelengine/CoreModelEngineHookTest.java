package com.diamonddagger590.mccore.external.modelengine;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class CoreModelEngineHookTest {

    private CoreModelEngineHook hook;
    private CorePlugin mockPlugin;
    private MockedStatic<ModelEngineAPI> modelEngineAPIMock;

    @BeforeEach
    void setUp() {
        mockPlugin = mock(CorePlugin.class);
        hook = new CoreModelEngineHook(mockPlugin);
        modelEngineAPIMock = mockStatic(ModelEngineAPI.class);
    }

    @AfterEach
    void tearDown() {
        modelEngineAPIMock.close();
    }

    @Nested
    @DisplayName("isCustomEntity(UUID)")
    class IsCustomEntityByUuid {

        @Test
        @DisplayName("Given a modeled entity UUID, when checked, then returns true")
        void returnsTrue_whenUuidIsModeledEntity() {
            UUID uuid = UUID.randomUUID();
            modelEngineAPIMock.when(() -> ModelEngineAPI.isModeledEntity(uuid)).thenReturn(true);

            assertTrue(hook.isCustomEntity(uuid));
        }

        @Test
        @DisplayName("Given a non-modeled entity UUID, when checked, then returns false")
        void returnsFalse_whenUuidIsNotModeledEntity() {
            UUID uuid = UUID.randomUUID();
            modelEngineAPIMock.when(() -> ModelEngineAPI.isModeledEntity(uuid)).thenReturn(false);

            assertFalse(hook.isCustomEntity(uuid));
        }
    }

    @Nested
    @DisplayName("isCustomEntity(String)")
    class IsCustomEntityByName {

        @Test
        @DisplayName("Given a known blueprint name, when checked, then returns true")
        void returnsTrue_whenBlueprintExists() {
            ModelBlueprint blueprint = mock(ModelBlueprint.class);
            modelEngineAPIMock.when(() -> ModelEngineAPI.getBlueprint("dragon_model")).thenReturn(blueprint);

            assertTrue(hook.isCustomEntity("dragon_model"));
        }

        @Test
        @DisplayName("Given an unknown blueprint name, when checked, then returns false")
        void returnsFalse_whenBlueprintDoesNotExist() {
            modelEngineAPIMock.when(() -> ModelEngineAPI.getBlueprint("nonexistent")).thenReturn(null);

            assertFalse(hook.isCustomEntity("nonexistent"));
        }
    }

    @Nested
    @DisplayName("isCustomEntityOfType(UUID, String)")
    class IsCustomEntityOfType {

        @Test
        @DisplayName("Given an entity with a matching model, when checked, then returns true")
        void returnsTrue_whenEntityHasMatchingModel() {
            UUID uuid = UUID.randomUUID();
            modelEngineAPIMock.when(() -> ModelEngineAPI.isModeledEntity(uuid)).thenReturn(true);

            ModeledEntity modeledEntity = mock(ModeledEntity.class);
            modelEngineAPIMock.when(() -> ModelEngineAPI.getModeledEntity(uuid)).thenReturn(modeledEntity);

            ActiveModel activeModel = mock(ActiveModel.class);
            ModelBlueprint blueprint = mock(ModelBlueprint.class);
            when(activeModel.getBlueprint()).thenReturn(blueprint);
            when(blueprint.getName()).thenReturn("dragon_model");
            when(modeledEntity.getModels()).thenReturn(Map.of("dragon_model", activeModel));

            assertTrue(hook.isCustomEntityOfType(uuid, "dragon_model"));
        }

        @Test
        @DisplayName("Given an entity with a matching model (case-insensitive), when checked, then returns true")
        void returnsTrue_whenModelMatchesCaseInsensitively() {
            UUID uuid = UUID.randomUUID();
            modelEngineAPIMock.when(() -> ModelEngineAPI.isModeledEntity(uuid)).thenReturn(true);

            ModeledEntity modeledEntity = mock(ModeledEntity.class);
            modelEngineAPIMock.when(() -> ModelEngineAPI.getModeledEntity(uuid)).thenReturn(modeledEntity);

            ActiveModel activeModel = mock(ActiveModel.class);
            ModelBlueprint blueprint = mock(ModelBlueprint.class);
            when(activeModel.getBlueprint()).thenReturn(blueprint);
            when(blueprint.getName()).thenReturn("Dragon_Model");
            when(modeledEntity.getModels()).thenReturn(Map.of("dragon_model", activeModel));

            assertTrue(hook.isCustomEntityOfType(uuid, "dragon_model"));
        }

        @Test
        @DisplayName("Given an entity without the specified model, when checked, then returns false")
        void returnsFalse_whenEntityDoesNotHaveModel() {
            UUID uuid = UUID.randomUUID();
            modelEngineAPIMock.when(() -> ModelEngineAPI.isModeledEntity(uuid)).thenReturn(true);

            ModeledEntity modeledEntity = mock(ModeledEntity.class);
            modelEngineAPIMock.when(() -> ModelEngineAPI.getModeledEntity(uuid)).thenReturn(modeledEntity);

            ActiveModel activeModel = mock(ActiveModel.class);
            ModelBlueprint blueprint = mock(ModelBlueprint.class);
            when(activeModel.getBlueprint()).thenReturn(blueprint);
            when(blueprint.getName()).thenReturn("wolf_model");
            when(modeledEntity.getModels()).thenReturn(Map.of("wolf_model", activeModel));

            assertFalse(hook.isCustomEntityOfType(uuid, "dragon_model"));
        }

        @Test
        @DisplayName("Given a non-modeled entity UUID, when checked, then returns false")
        void returnsFalse_whenEntityIsNotModeled() {
            UUID uuid = UUID.randomUUID();
            modelEngineAPIMock.when(() -> ModelEngineAPI.isModeledEntity(uuid)).thenReturn(false);

            assertFalse(hook.isCustomEntityOfType(uuid, "dragon_model"));
        }

        @Test
        @DisplayName("Given an entity with multiple models and one matches, when checked, then returns true")
        void returnsTrue_whenOneOfMultipleModelsMatches() {
            UUID uuid = UUID.randomUUID();
            modelEngineAPIMock.when(() -> ModelEngineAPI.isModeledEntity(uuid)).thenReturn(true);

            ModeledEntity modeledEntity = mock(ModeledEntity.class);
            modelEngineAPIMock.when(() -> ModelEngineAPI.getModeledEntity(uuid)).thenReturn(modeledEntity);

            ActiveModel model1 = mock(ActiveModel.class);
            ModelBlueprint blueprint1 = mock(ModelBlueprint.class);
            when(model1.getBlueprint()).thenReturn(blueprint1);
            when(blueprint1.getName()).thenReturn("wolf_model");

            ActiveModel model2 = mock(ActiveModel.class);
            ModelBlueprint blueprint2 = mock(ModelBlueprint.class);
            when(model2.getBlueprint()).thenReturn(blueprint2);
            when(blueprint2.getName()).thenReturn("dragon_model");

            when(modeledEntity.getModels()).thenReturn(Map.of("wolf_model", model1, "dragon_model", model2));

            assertTrue(hook.isCustomEntityOfType(uuid, "dragon_model"));
        }
    }

    @Nested
    @DisplayName("entityModels(Entity)")
    class EntityModels {

        @Test
        @DisplayName("Given a modeled entity, when getting models, then returns the model key set")
        void returnsModelKeys_whenEntityIsModeled() {
            Entity entity = mock(Entity.class);
            UUID uuid = UUID.randomUUID();
            when(entity.getUniqueId()).thenReturn(uuid);

            ModeledEntity modeledEntity = mock(ModeledEntity.class);
            modelEngineAPIMock.when(() -> ModelEngineAPI.getModeledEntity(uuid)).thenReturn(modeledEntity);
            when(modeledEntity.getModels()).thenReturn(Map.of(
                "dragon_model", mock(ActiveModel.class),
                "wolf_model", mock(ActiveModel.class)
            ));

            Optional<Set<String>> result = hook.entityModels(entity);

            assertTrue(result.isPresent());
            assertEquals(Set.of("dragon_model", "wolf_model"), result.get());
        }

        @Test
        @DisplayName("Given a non-modeled entity, when getting models, then returns empty")
        void returnsEmpty_whenEntityIsNotModeled() {
            Entity entity = mock(Entity.class);
            UUID uuid = UUID.randomUUID();
            when(entity.getUniqueId()).thenReturn(uuid);

            modelEngineAPIMock.when(() -> ModelEngineAPI.getModeledEntity(uuid)).thenReturn(null);

            Optional<Set<String>> result = hook.entityModels(entity);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("entityName(CustomEntityWrapper)")
    class EntityName {

        @Test
        @DisplayName("Given a wrapper with a custom entity, when getting name, then returns the entity id")
        void returnsEntityId_whenWrapperHasCustomEntity() {
            CustomEntityWrapper wrapper = mock(CustomEntityWrapper.class);
            when(wrapper.customEntity()).thenReturn(Optional.of("dragon_model"));

            assertEquals("dragon_model", hook.entityName(wrapper));
        }

        @Test
        @DisplayName("Given a wrapper without a custom entity, when getting name, then returns Unknown")
        void returnsUnknown_whenWrapperHasNoCustomEntity() {
            CustomEntityWrapper wrapper = mock(CustomEntityWrapper.class);
            when(wrapper.customEntity()).thenReturn(Optional.empty());

            assertEquals("Unknown", hook.entityName(wrapper));
        }
    }

    @Test
    @DisplayName("Hook returns the plugin it was created with")
    void plugin_returnsProvidedPlugin() {
        assertEquals(mockPlugin, hook.plugin());
    }
}

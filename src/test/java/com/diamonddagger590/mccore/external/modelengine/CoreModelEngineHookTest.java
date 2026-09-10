package com.diamonddagger590.mccore.external.modelengine;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import org.bukkit.entity.Entity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreModelEngineHookTest {

    @Mock
    private CorePlugin plugin;

    private MockedStatic<ModelEngineAPI> modelEngineApi;
    private CoreModelEngineHook hook;

    @BeforeEach
    void setUp() {
        modelEngineApi = mockStatic(ModelEngineAPI.class);
        hook = new CoreModelEngineHook(plugin);
    }

    @AfterEach
    void tearDown() {
        modelEngineApi.close();
    }

    @Test
    @DisplayName("Given a CoreModelEngineHook, when constructed, then plugin is accessible")
    void constructor_storesPlugin() {
        assertNotNull(hook.plugin());
    }

    @Nested
    @DisplayName("isCustomEntity(UUID)")
    class IsCustomEntityByUUID {

        @Test
        @DisplayName("Given a modeled entity UUID, when isCustomEntity is called, then returns true")
        void returnsTrue_whenEntityIsModeled() {
            UUID uuid = UUID.randomUUID();
            modelEngineApi.when(() -> ModelEngineAPI.isModeledEntity(uuid)).thenReturn(true);

            assertTrue(hook.isCustomEntity(uuid));
        }

        @Test
        @DisplayName("Given a non-modeled entity UUID, when isCustomEntity is called, then returns false")
        void returnsFalse_whenEntityIsNotModeled() {
            UUID uuid = UUID.randomUUID();
            modelEngineApi.when(() -> ModelEngineAPI.isModeledEntity(uuid)).thenReturn(false);

            assertFalse(hook.isCustomEntity(uuid));
        }
    }

    @Nested
    @DisplayName("isCustomEntity(String)")
    class IsCustomEntityByName {

        @Test
        @DisplayName("Given a valid blueprint name, when isCustomEntity is called, then returns true")
        void returnsTrue_whenBlueprintExists() {
            ModelBlueprint blueprint = mock(ModelBlueprint.class);
            modelEngineApi.when(() -> ModelEngineAPI.getBlueprint("dragon")).thenReturn(blueprint);

            assertTrue(hook.isCustomEntity("dragon"));
        }

        @Test
        @DisplayName("Given an invalid blueprint name, when isCustomEntity is called, then returns false")
        void returnsFalse_whenBlueprintDoesNotExist() {
            modelEngineApi.when(() -> ModelEngineAPI.getBlueprint("nonexistent")).thenReturn(null);

            assertFalse(hook.isCustomEntity("nonexistent"));
        }
    }

    @Nested
    @DisplayName("isCustomEntityOfType(UUID, String)")
    class IsCustomEntityOfType {

        @Test
        @DisplayName("Given a modeled entity with matching blueprint, when isCustomEntityOfType is called, then returns true")
        void returnsTrue_whenEntityHasMatchingModel() {
            UUID uuid = UUID.randomUUID();
            modelEngineApi.when(() -> ModelEngineAPI.isModeledEntity(uuid)).thenReturn(true);

            ModelBlueprint blueprint = mock(ModelBlueprint.class);
            when(blueprint.getName()).thenReturn("dragon");

            ActiveModel activeModel = mock(ActiveModel.class);
            when(activeModel.getBlueprint()).thenReturn(blueprint);

            ModeledEntity modeledEntity = mock(ModeledEntity.class);
            when(modeledEntity.getModels()).thenReturn(Map.of("dragon", activeModel));

            modelEngineApi.when(() -> ModelEngineAPI.getModeledEntity(uuid)).thenReturn(modeledEntity);

            assertTrue(hook.isCustomEntityOfType(uuid, "dragon"));
        }

        @Test
        @DisplayName("Given a modeled entity with matching blueprint (case-insensitive), when isCustomEntityOfType is called, then returns true")
        void returnsTrue_whenEntityHasMatchingModelCaseInsensitive() {
            UUID uuid = UUID.randomUUID();
            modelEngineApi.when(() -> ModelEngineAPI.isModeledEntity(uuid)).thenReturn(true);

            ModelBlueprint blueprint = mock(ModelBlueprint.class);
            when(blueprint.getName()).thenReturn("Dragon");

            ActiveModel activeModel = mock(ActiveModel.class);
            when(activeModel.getBlueprint()).thenReturn(blueprint);

            ModeledEntity modeledEntity = mock(ModeledEntity.class);
            when(modeledEntity.getModels()).thenReturn(Map.of("dragon", activeModel));

            modelEngineApi.when(() -> ModelEngineAPI.getModeledEntity(uuid)).thenReturn(modeledEntity);

            assertTrue(hook.isCustomEntityOfType(uuid, "DRAGON"));
        }

        @Test
        @DisplayName("Given a modeled entity with multiple models where second matches, when isCustomEntityOfType is called, then returns true")
        void returnsTrue_whenSecondModelMatches() {
            UUID uuid = UUID.randomUUID();
            modelEngineApi.when(() -> ModelEngineAPI.isModeledEntity(uuid)).thenReturn(true);

            ModelBlueprint blueprint1 = mock(ModelBlueprint.class);
            when(blueprint1.getName()).thenReturn("skeleton");
            ActiveModel activeModel1 = mock(ActiveModel.class);
            when(activeModel1.getBlueprint()).thenReturn(blueprint1);

            ModelBlueprint blueprint2 = mock(ModelBlueprint.class);
            when(blueprint2.getName()).thenReturn("dragon");
            ActiveModel activeModel2 = mock(ActiveModel.class);
            when(activeModel2.getBlueprint()).thenReturn(blueprint2);

            ModeledEntity modeledEntity = mock(ModeledEntity.class);
            when(modeledEntity.getModels()).thenReturn(Map.of("skeleton", activeModel1, "dragon", activeModel2));

            modelEngineApi.when(() -> ModelEngineAPI.getModeledEntity(uuid)).thenReturn(modeledEntity);

            assertTrue(hook.isCustomEntityOfType(uuid, "dragon"));
        }

        @Test
        @DisplayName("Given a modeled entity with non-matching blueprint, when isCustomEntityOfType is called, then returns false")
        void returnsFalse_whenEntityHasNonMatchingModel() {
            UUID uuid = UUID.randomUUID();
            modelEngineApi.when(() -> ModelEngineAPI.isModeledEntity(uuid)).thenReturn(true);

            ModelBlueprint blueprint = mock(ModelBlueprint.class);
            when(blueprint.getName()).thenReturn("skeleton");

            ActiveModel activeModel = mock(ActiveModel.class);
            when(activeModel.getBlueprint()).thenReturn(blueprint);

            ModeledEntity modeledEntity = mock(ModeledEntity.class);
            when(modeledEntity.getModels()).thenReturn(Map.of("skeleton", activeModel));

            modelEngineApi.when(() -> ModelEngineAPI.getModeledEntity(uuid)).thenReturn(modeledEntity);

            assertFalse(hook.isCustomEntityOfType(uuid, "dragon"));
        }

        @Test
        @DisplayName("Given a non-modeled entity, when isCustomEntityOfType is called, then returns false")
        void returnsFalse_whenEntityIsNotModeled() {
            UUID uuid = UUID.randomUUID();
            modelEngineApi.when(() -> ModelEngineAPI.isModeledEntity(uuid)).thenReturn(false);

            assertFalse(hook.isCustomEntityOfType(uuid, "dragon"));
        }
    }

    @Nested
    @DisplayName("entityModels(Entity)")
    class EntityModels {

        @Test
        @DisplayName("Given a modeled entity, when entityModels is called, then returns model key set")
        void returnsModels_whenEntityIsModeled() {
            Entity entity = mock(Entity.class);
            UUID uuid = UUID.randomUUID();
            when(entity.getUniqueId()).thenReturn(uuid);

            ModeledEntity modeledEntity = mock(ModeledEntity.class);
            when(modeledEntity.getModels()).thenReturn(Map.of("dragon", mock(ActiveModel.class), "wings", mock(ActiveModel.class)));

            modelEngineApi.when(() -> ModelEngineAPI.getModeledEntity(uuid)).thenReturn(modeledEntity);

            Optional<Set<String>> result = hook.entityModels(entity);
            assertTrue(result.isPresent());
            assertEquals(Set.of("dragon", "wings"), result.get());
        }

        @Test
        @DisplayName("Given a non-modeled entity, when entityModels is called, then returns empty optional")
        void returnsEmpty_whenEntityIsNotModeled() {
            Entity entity = mock(Entity.class);
            UUID uuid = UUID.randomUUID();
            when(entity.getUniqueId()).thenReturn(uuid);

            modelEngineApi.when(() -> ModelEngineAPI.getModeledEntity(uuid)).thenReturn(null);

            Optional<Set<String>> result = hook.entityModels(entity);
            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("entityName(CustomEntityWrapper)")
    class EntityName {

        @Test
        @DisplayName("Given a wrapper with custom entity, when entityName is called, then returns the custom entity id")
        void returnsCustomEntityId_whenPresent() {
            CustomEntityWrapper wrapper = mock(CustomEntityWrapper.class);
            when(wrapper.customEntity()).thenReturn(Optional.of("my_dragon"));

            assertEquals("my_dragon", hook.entityName(wrapper));
        }

        @Test
        @DisplayName("Given a wrapper without custom entity, when entityName is called, then returns Unknown")
        void returnsUnknown_whenCustomEntityAbsent() {
            CustomEntityWrapper wrapper = mock(CustomEntityWrapper.class);
            when(wrapper.customEntity()).thenReturn(Optional.empty());

            assertEquals("Unknown", hook.entityName(wrapper));
        }
    }
}

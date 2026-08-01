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
    private MockedStatic<ModelEngineAPI> modelEngineStatic;

    @BeforeEach
    void setUp() {
        CorePlugin mockPlugin = mock(CorePlugin.class);
        hook = new CoreModelEngineHook(mockPlugin);
        modelEngineStatic = mockStatic(ModelEngineAPI.class);
    }

    @AfterEach
    void tearDown() {
        modelEngineStatic.close();
    }

    @Nested
    @DisplayName("isCustomEntity(UUID)")
    class IsCustomEntityByUUIDTests {

        @Test
        @DisplayName("Given a modeled entity UUID, when isCustomEntity is called, then returns true")
        void isCustomEntity_returnsTrue_whenModeled() {
            UUID uuid = UUID.randomUUID();
            modelEngineStatic.when(() -> ModelEngineAPI.isModeledEntity(uuid)).thenReturn(true);

            assertTrue(hook.isCustomEntity(uuid));
        }

        @Test
        @DisplayName("Given a non-modeled entity UUID, when isCustomEntity is called, then returns false")
        void isCustomEntity_returnsFalse_whenNotModeled() {
            UUID uuid = UUID.randomUUID();
            modelEngineStatic.when(() -> ModelEngineAPI.isModeledEntity(uuid)).thenReturn(false);

            assertFalse(hook.isCustomEntity(uuid));
        }
    }

    @Nested
    @DisplayName("isCustomEntity(String)")
    class IsCustomEntityByStringTests {

        @Test
        @DisplayName("Given a valid blueprint name, when isCustomEntity is called, then returns true")
        void isCustomEntity_returnsTrue_whenBlueprintExists() {
            modelEngineStatic.when(() -> ModelEngineAPI.getBlueprint("dragon")).thenReturn(mock(ModelBlueprint.class));

            assertTrue(hook.isCustomEntity("dragon"));
        }

        @Test
        @DisplayName("Given an invalid blueprint name, when isCustomEntity is called, then returns false")
        void isCustomEntity_returnsFalse_whenBlueprintDoesNotExist() {
            modelEngineStatic.when(() -> ModelEngineAPI.getBlueprint("nonexistent")).thenReturn(null);

            assertFalse(hook.isCustomEntity("nonexistent"));
        }
    }

    @Nested
    @DisplayName("isCustomEntityOfType")
    class IsCustomEntityOfTypeTests {

        @Test
        @DisplayName("Given a modeled entity with matching type, when isCustomEntityOfType is called, then returns true")
        void isCustomEntityOfType_returnsTrue_whenMatches() {
            UUID uuid = UUID.randomUUID();
            ModeledEntity mockModeled = mock(ModeledEntity.class);
            ActiveModel mockActiveModel = mock(ActiveModel.class);
            ModelBlueprint mockBlueprint = mock(ModelBlueprint.class);

            when(mockBlueprint.getName()).thenReturn("dragon");
            when(mockActiveModel.getBlueprint()).thenReturn(mockBlueprint);
            when(mockModeled.getModels()).thenReturn(Map.of("dragon", mockActiveModel));

            modelEngineStatic.when(() -> ModelEngineAPI.isModeledEntity(uuid)).thenReturn(true);
            modelEngineStatic.when(() -> ModelEngineAPI.getModeledEntity(uuid)).thenReturn(mockModeled);

            assertTrue(hook.isCustomEntityOfType(uuid, "dragon"));
        }

        @Test
        @DisplayName("Given a modeled entity with matching type (case insensitive), when isCustomEntityOfType is called, then returns true")
        void isCustomEntityOfType_returnsTrue_caseInsensitive() {
            UUID uuid = UUID.randomUUID();
            ModeledEntity mockModeled = mock(ModeledEntity.class);
            ActiveModel mockActiveModel = mock(ActiveModel.class);
            ModelBlueprint mockBlueprint = mock(ModelBlueprint.class);

            when(mockBlueprint.getName()).thenReturn("Dragon");
            when(mockActiveModel.getBlueprint()).thenReturn(mockBlueprint);
            when(mockModeled.getModels()).thenReturn(Map.of("dragon", mockActiveModel));

            modelEngineStatic.when(() -> ModelEngineAPI.isModeledEntity(uuid)).thenReturn(true);
            modelEngineStatic.when(() -> ModelEngineAPI.getModeledEntity(uuid)).thenReturn(mockModeled);

            assertTrue(hook.isCustomEntityOfType(uuid, "dragon"));
        }

        @Test
        @DisplayName("Given a modeled entity with non-matching type, when isCustomEntityOfType is called, then returns false")
        void isCustomEntityOfType_returnsFalse_whenDifferentType() {
            UUID uuid = UUID.randomUUID();
            ModeledEntity mockModeled = mock(ModeledEntity.class);
            ActiveModel mockActiveModel = mock(ActiveModel.class);
            ModelBlueprint mockBlueprint = mock(ModelBlueprint.class);

            when(mockBlueprint.getName()).thenReturn("skeleton");
            when(mockActiveModel.getBlueprint()).thenReturn(mockBlueprint);
            when(mockModeled.getModels()).thenReturn(Map.of("skeleton", mockActiveModel));

            modelEngineStatic.when(() -> ModelEngineAPI.isModeledEntity(uuid)).thenReturn(true);
            modelEngineStatic.when(() -> ModelEngineAPI.getModeledEntity(uuid)).thenReturn(mockModeled);

            assertFalse(hook.isCustomEntityOfType(uuid, "dragon"));
        }

        @Test
        @DisplayName("Given a non-modeled entity UUID, when isCustomEntityOfType is called, then returns false")
        void isCustomEntityOfType_returnsFalse_whenNotModeled() {
            UUID uuid = UUID.randomUUID();
            modelEngineStatic.when(() -> ModelEngineAPI.isModeledEntity(uuid)).thenReturn(false);

            assertFalse(hook.isCustomEntityOfType(uuid, "dragon"));
        }
    }

    @Nested
    @DisplayName("entityModels")
    class EntityModelsTests {

        @Test
        @DisplayName("Given a modeled entity, when entityModels is called, then returns model keys")
        void entityModels_returnsKeys_whenModeled() {
            Entity mockEntity = mock(Entity.class);
            UUID uuid = UUID.randomUUID();
            when(mockEntity.getUniqueId()).thenReturn(uuid);

            ModeledEntity mockModeled = mock(ModeledEntity.class);
            ActiveModel mockModel1 = mock(ActiveModel.class);
            ActiveModel mockModel2 = mock(ActiveModel.class);
            when(mockModeled.getModels()).thenReturn(Map.of("dragon", mockModel1, "wings", mockModel2));

            modelEngineStatic.when(() -> ModelEngineAPI.getModeledEntity(uuid)).thenReturn(mockModeled);

            Optional<Set<String>> result = hook.entityModels(mockEntity);

            assertTrue(result.isPresent());
            assertEquals(Set.of("dragon", "wings"), result.get());
        }

        @Test
        @DisplayName("Given a non-modeled entity, when entityModels is called, then returns empty")
        void entityModels_returnsEmpty_whenNotModeled() {
            Entity mockEntity = mock(Entity.class);
            UUID uuid = UUID.randomUUID();
            when(mockEntity.getUniqueId()).thenReturn(uuid);

            modelEngineStatic.when(() -> ModelEngineAPI.getModeledEntity(uuid)).thenReturn(null);

            Optional<Set<String>> result = hook.entityModels(mockEntity);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("entityName")
    class EntityNameTests {

        @Test
        @DisplayName("Given a custom entity wrapper, when entityName is called, then returns the blueprint name")
        void entityName_returnsBlueprintName_whenCustomEntity() {
            CustomEntityWrapper wrapper = mock(CustomEntityWrapper.class);
            when(wrapper.customEntity()).thenReturn(Optional.of("dragon"));

            assertEquals("dragon", hook.entityName(wrapper));
        }

        @Test
        @DisplayName("Given a vanilla entity wrapper, when entityName is called, then returns Unknown")
        void entityName_returnsUnknown_whenNoCustomEntity() {
            CustomEntityWrapper wrapper = mock(CustomEntityWrapper.class);
            when(wrapper.customEntity()).thenReturn(Optional.empty());

            assertEquals("Unknown", hook.entityName(wrapper));
        }
    }
}

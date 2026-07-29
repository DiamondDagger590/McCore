package com.diamonddagger590.mccore.external.modelengine;

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
import sun.misc.Unsafe;

import java.lang.reflect.Field;
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
    private MockedStatic<ModelEngineAPI> modelEngineApiMock;

    private static final UUID KNOWN_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID UNKNOWN_UUID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @BeforeEach
    void setUp() throws Exception {
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);
        hook = (CoreModelEngineHook) unsafe.allocateInstance(CoreModelEngineHook.class);
        modelEngineApiMock = mockStatic(ModelEngineAPI.class);
    }

    @AfterEach
    void tearDown() {
        modelEngineApiMock.close();
    }

    @Nested
    @DisplayName("isCustomEntity(UUID)")
    class IsCustomEntityByUuid {

        @Test
        @DisplayName("Given a modeled entity UUID, when checking isCustomEntity, then returns true")
        void returnsTrue_whenEntityIsModeled() {
            modelEngineApiMock.when(() -> ModelEngineAPI.isModeledEntity(KNOWN_UUID)).thenReturn(true);

            assertTrue(hook.isCustomEntity(KNOWN_UUID));
        }

        @Test
        @DisplayName("Given a non-modeled entity UUID, when checking isCustomEntity, then returns false")
        void returnsFalse_whenEntityIsNotModeled() {
            modelEngineApiMock.when(() -> ModelEngineAPI.isModeledEntity(UNKNOWN_UUID)).thenReturn(false);

            assertFalse(hook.isCustomEntity(UNKNOWN_UUID));
        }
    }

    @Nested
    @DisplayName("isCustomEntity(String)")
    class IsCustomEntityByName {

        @Test
        @DisplayName("Given a valid blueprint name, when checking isCustomEntity, then returns true")
        void returnsTrue_whenBlueprintExists() {
            ModelBlueprint blueprint = mock(ModelBlueprint.class);
            modelEngineApiMock.when(() -> ModelEngineAPI.getBlueprint("dragon")).thenReturn(blueprint);

            assertTrue(hook.isCustomEntity("dragon"));
        }

        @Test
        @DisplayName("Given an invalid blueprint name, when checking isCustomEntity, then returns false")
        void returnsFalse_whenBlueprintDoesNotExist() {
            modelEngineApiMock.when(() -> ModelEngineAPI.getBlueprint("unknown")).thenReturn(null);

            assertFalse(hook.isCustomEntity("unknown"));
        }
    }

    @Nested
    @DisplayName("isCustomEntityOfType")
    class IsCustomEntityOfType {

        @Test
        @DisplayName("Given a modeled entity with matching type, when checking isCustomEntityOfType, then returns true")
        void returnsTrue_whenEntityHasMatchingModel() {
            modelEngineApiMock.when(() -> ModelEngineAPI.isModeledEntity(KNOWN_UUID)).thenReturn(true);

            ModelBlueprint blueprint = mock(ModelBlueprint.class);
            when(blueprint.getName()).thenReturn("dragon");

            ActiveModel activeModel = mock(ActiveModel.class);
            when(activeModel.getBlueprint()).thenReturn(blueprint);

            ModeledEntity modeledEntity = mock(ModeledEntity.class);
            when(modeledEntity.getModels()).thenReturn(Map.of("dragon", activeModel));

            modelEngineApiMock.when(() -> ModelEngineAPI.getModeledEntity(KNOWN_UUID)).thenReturn(modeledEntity);

            assertTrue(hook.isCustomEntityOfType(KNOWN_UUID, "dragon"));
        }

        @Test
        @DisplayName("Given a modeled entity with non-matching type, when checking isCustomEntityOfType, then returns false")
        void returnsFalse_whenEntityHasNonMatchingModel() {
            modelEngineApiMock.when(() -> ModelEngineAPI.isModeledEntity(KNOWN_UUID)).thenReturn(true);

            ModelBlueprint blueprint = mock(ModelBlueprint.class);
            when(blueprint.getName()).thenReturn("golem");

            ActiveModel activeModel = mock(ActiveModel.class);
            when(activeModel.getBlueprint()).thenReturn(blueprint);

            ModeledEntity modeledEntity = mock(ModeledEntity.class);
            when(modeledEntity.getModels()).thenReturn(Map.of("golem", activeModel));

            modelEngineApiMock.when(() -> ModelEngineAPI.getModeledEntity(KNOWN_UUID)).thenReturn(modeledEntity);

            assertFalse(hook.isCustomEntityOfType(KNOWN_UUID, "dragon"));
        }

        @Test
        @DisplayName("Given a non-modeled entity, when checking isCustomEntityOfType, then returns false")
        void returnsFalse_whenEntityIsNotModeled() {
            modelEngineApiMock.when(() -> ModelEngineAPI.isModeledEntity(UNKNOWN_UUID)).thenReturn(false);

            assertFalse(hook.isCustomEntityOfType(UNKNOWN_UUID, "dragon"));
        }
    }

    @Nested
    @DisplayName("entityModels")
    class EntityModels {

        @Test
        @DisplayName("Given a modeled entity, when getting entityModels, then returns the model keys")
        void returnsModelKeys_whenEntityIsModeled() {
            Entity entity = mock(Entity.class);
            when(entity.getUniqueId()).thenReturn(KNOWN_UUID);

            ActiveModel activeModel = mock(ActiveModel.class);
            ModeledEntity modeledEntity = mock(ModeledEntity.class);
            when(modeledEntity.getModels()).thenReturn(Map.of("dragon", activeModel, "wings", mock(ActiveModel.class)));

            modelEngineApiMock.when(() -> ModelEngineAPI.getModeledEntity(KNOWN_UUID)).thenReturn(modeledEntity);

            Optional<Set<String>> result = hook.entityModels(entity);

            assertTrue(result.isPresent());
            assertEquals(Set.of("dragon", "wings"), result.get());
        }

        @Test
        @DisplayName("Given a non-modeled entity, when getting entityModels, then returns empty")
        void returnsEmpty_whenEntityIsNotModeled() {
            Entity entity = mock(Entity.class);
            when(entity.getUniqueId()).thenReturn(UNKNOWN_UUID);

            modelEngineApiMock.when(() -> ModelEngineAPI.getModeledEntity(UNKNOWN_UUID)).thenReturn(null);

            Optional<Set<String>> result = hook.entityModels(entity);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("entityName")
    class EntityName {

        @Test
        @DisplayName("Given a custom entity wrapper, when getting entityName, then returns the custom entity id")
        void returnsCustomEntityId_whenPresent() {
            CustomEntityWrapper wrapper = new CustomEntityWrapper("dragon");

            assertEquals("dragon", hook.entityName(wrapper));
        }

        @Test
        @DisplayName("Given a vanilla entity wrapper, when getting entityName, then returns Unknown")
        void returnsUnknown_whenNoCustomEntity() {
            CustomEntityWrapper wrapper = new CustomEntityWrapper(EntityType.ZOMBIE);

            assertEquals("Unknown", hook.entityName(wrapper));
        }
    }
}

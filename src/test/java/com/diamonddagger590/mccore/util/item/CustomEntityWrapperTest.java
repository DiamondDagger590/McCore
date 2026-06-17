package com.diamonddagger590.mccore.util.item;

import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomEntityWrapperTest {

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
    }

    private static CustomEntityWrapper entityTypeWrapper(EntityType entityType) {
        return new CustomEntityWrapper(entityType);
    }

    private static CustomEntityWrapper customEntityWrapper(String customEntity) throws Exception {
        CustomEntityWrapper wrapper = new CustomEntityWrapper(EntityType.ZOMBIE);
        Field entityTypeField = CustomEntityWrapper.class.getDeclaredField("entityType");
        entityTypeField.setAccessible(true);
        entityTypeField.set(wrapper, null);
        Field customEntityField = CustomEntityWrapper.class.getDeclaredField("customEntity");
        customEntityField.setAccessible(true);
        customEntityField.set(wrapper, customEntity);
        return wrapper;
    }

    // ── EntityType constructor ──────────────────────────────────────────────

    @Nested
    @DisplayName("EntityType constructor")
    class EntityTypeConstructor {

        @Test
        @DisplayName("Given an entity type, when constructing, then entityType() returns that type")
        void entityType_returnsProvidedType() {
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.ZOMBIE);
            assertTrue(wrapper.entityType().isPresent());
            assertEquals(EntityType.ZOMBIE, wrapper.entityType().get());
        }

        @Test
        @DisplayName("Given an entity type, when constructing, then customEntity() returns empty")
        void customEntity_returnsEmpty_whenConstructedWithEntityType() {
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.CREEPER);
            assertFalse(wrapper.customEntity().isPresent());
        }
    }

    // ── Custom entity accessors ─────────────────────────────────────────────

    @Nested
    @DisplayName("Custom entity accessors")
    class CustomEntityAccessors {

        @Test
        @DisplayName("Given a custom entity wrapper, then customEntity() returns the custom entity id")
        void customEntity_returnsId() throws Exception {
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:fire_dragon");
            assertTrue(wrapper.customEntity().isPresent());
            assertEquals("mythicmobs:fire_dragon", wrapper.customEntity().get());
        }

        @Test
        @DisplayName("Given a custom entity wrapper, then entityType() returns empty")
        void entityType_returnsEmpty_whenCustomEntity() throws Exception {
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:fire_dragon");
            assertFalse(wrapper.entityType().isPresent());
        }
    }

    // ── equals(EntityType) ──────────────────────────────────────────────────

    @Nested
    @DisplayName("equals(EntityType)")
    class EqualsEntityType {

        @Test
        @DisplayName("Given matching entity type, when comparing, then returns true")
        void returnsTrue_whenEntityTypeMatches() {
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.SKELETON);
            assertTrue(wrapper.equals(EntityType.SKELETON));
        }

        @Test
        @DisplayName("Given different entity type, when comparing, then returns false")
        void returnsFalse_whenEntityTypeDiffers() {
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.SKELETON);
            assertFalse(wrapper.equals(EntityType.ZOMBIE));
        }

        @Test
        @DisplayName("Given custom entity wrapper, when comparing with any entity type, then returns false")
        void returnsFalse_whenWrapperIsCustomEntity() throws Exception {
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:fire_dragon");
            assertFalse(wrapper.equals(EntityType.ENDER_DRAGON));
        }
    }

    // ── equals(String) ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("equals(String)")
    class EqualsString {

        @Test
        @DisplayName("Given matching custom entity id, when comparing, then returns true")
        void returnsTrue_whenCustomEntityMatches() throws Exception {
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:fire_dragon");
            assertTrue(wrapper.equals("mythicmobs:fire_dragon"));
        }

        @Test
        @DisplayName("Given different custom entity id, when comparing, then returns false")
        void returnsFalse_whenCustomEntityDiffers() throws Exception {
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:fire_dragon");
            assertFalse(wrapper.equals("mythicmobs:ice_dragon"));
        }

        @Test
        @DisplayName("Given entity type wrapper, when comparing with any string, then returns false")
        void returnsFalse_whenWrapperIsEntityType() {
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.ZOMBIE);
            assertFalse(wrapper.equals("zombie"));
        }
    }

    // ── equals(CustomEntityWrapper) ─────────────────────────────────────────

    @Nested
    @DisplayName("equals(CustomEntityWrapper)")
    class EqualsWrapper {

        @Test
        @DisplayName("Given two entity type wrappers with same type, when comparing, then returns true")
        void returnsTrue_whenBothEntityTypesMatch() {
            CustomEntityWrapper a = entityTypeWrapper(EntityType.ZOMBIE);
            CustomEntityWrapper b = entityTypeWrapper(EntityType.ZOMBIE);
            assertTrue(a.equals(b));
        }

        @Test
        @DisplayName("Given two entity type wrappers with different types, when comparing, then returns false")
        void returnsFalse_whenEntityTypesDiffer() {
            CustomEntityWrapper a = entityTypeWrapper(EntityType.ZOMBIE);
            CustomEntityWrapper b = entityTypeWrapper(EntityType.SKELETON);
            assertFalse(a.equals(b));
        }

        @Test
        @DisplayName("Given two custom entity wrappers with same id, when comparing, then returns true")
        void returnsTrue_whenBothCustomEntitiesMatch() throws Exception {
            CustomEntityWrapper a = customEntityWrapper("mythicmobs:fire_dragon");
            CustomEntityWrapper b = customEntityWrapper("mythicmobs:fire_dragon");
            assertTrue(a.equals(b));
        }

        @Test
        @DisplayName("Given two custom entity wrappers with different ids, when comparing, then returns false")
        void returnsFalse_whenCustomEntitiesDiffer() throws Exception {
            CustomEntityWrapper a = customEntityWrapper("mythicmobs:fire_dragon");
            CustomEntityWrapper b = customEntityWrapper("mythicmobs:ice_dragon");
            assertFalse(a.equals(b));
        }

        @Test
        @DisplayName("Given entity type wrapper and custom entity wrapper, when comparing, then returns false")
        void returnsFalse_whenTypesAreMixed() throws Exception {
            CustomEntityWrapper entityBased = entityTypeWrapper(EntityType.ZOMBIE);
            CustomEntityWrapper customBased = customEntityWrapper("mythicmobs:zombie");
            assertFalse(entityBased.equals(customBased));
        }
    }

    // ── equals(Object) ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("equals(Object)")
    class EqualsObject {

        @Test
        @DisplayName("Given same instance, when comparing, then returns true")
        void returnsTrue_forSameInstance() {
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.CREEPER);
            assertEquals(wrapper, wrapper);
        }

        @Test
        @DisplayName("Given two entity type wrappers with same type, when comparing as Object, then returns true")
        void returnsTrue_whenEntityTypeWrappersMatch() {
            CustomEntityWrapper a = entityTypeWrapper(EntityType.CREEPER);
            CustomEntityWrapper b = entityTypeWrapper(EntityType.CREEPER);
            assertEquals(a, b);
        }

        @Test
        @DisplayName("Given two custom entity wrappers with same id, when comparing as Object, then returns true")
        void returnsTrue_whenCustomEntityWrappersMatch() throws Exception {
            CustomEntityWrapper a = customEntityWrapper("mythicmobs:fire_dragon");
            CustomEntityWrapper b = customEntityWrapper("mythicmobs:fire_dragon");
            assertEquals(a, b);
        }

        @Test
        @DisplayName("Given entity wrapper and different type, when comparing, then returns false")
        void returnsFalse_whenComparedWithDifferentType() {
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.ZOMBIE);
            assertNotEquals(wrapper, "not a wrapper");
        }

        @Test
        @DisplayName("Given one entity type and one custom wrapper, when comparing as Object, then returns false")
        void returnsFalse_whenOneIsEntityTypeAndOtherIsCustom() throws Exception {
            CustomEntityWrapper a = entityTypeWrapper(EntityType.ZOMBIE);
            CustomEntityWrapper b = customEntityWrapper("mythicmobs:zombie");
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("Given two different entity type wrappers, when comparing as Object, then returns false")
        void returnsFalse_whenEntityTypesDiffer() {
            CustomEntityWrapper a = entityTypeWrapper(EntityType.ZOMBIE);
            CustomEntityWrapper b = entityTypeWrapper(EntityType.SKELETON);
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("Given two different custom entity wrappers, when comparing as Object, then returns false")
        void returnsFalse_whenCustomEntitiesDiffer() throws Exception {
            CustomEntityWrapper a = customEntityWrapper("mythicmobs:fire_dragon");
            CustomEntityWrapper b = customEntityWrapper("mythicmobs:ice_dragon");
            assertNotEquals(a, b);
        }
    }

    // ── hashCode ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("hashCode")
    class HashCode {

        @Test
        @DisplayName("Given two equal entity type wrappers, then hashCodes are equal")
        void hashCodesEqual_forEqualEntityTypeWrappers() {
            CustomEntityWrapper a = entityTypeWrapper(EntityType.ZOMBIE);
            CustomEntityWrapper b = entityTypeWrapper(EntityType.ZOMBIE);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Given two equal custom entity wrappers, then hashCodes are equal")
        void hashCodesEqual_forEqualCustomEntityWrappers() throws Exception {
            CustomEntityWrapper a = customEntityWrapper("mythicmobs:fire_dragon");
            CustomEntityWrapper b = customEntityWrapper("mythicmobs:fire_dragon");
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Given entity type wrapper, then hashCode is consistent across calls")
        void hashCode_isConsistent() {
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.CREEPER);
            int first = wrapper.hashCode();
            int second = wrapper.hashCode();
            assertEquals(first, second);
        }
    }

    // ── entityName ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("entityName")
    class EntityName {

        @Test
        @DisplayName("Given custom entity wrapper with no hooks registered, when getting entityName, then returns Unknown")
        void returnsUnknown_whenNoHooksRegistered() throws Exception {
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:fire_dragon");
            assertEquals("Unknown", wrapper.entityName());
        }
    }
}

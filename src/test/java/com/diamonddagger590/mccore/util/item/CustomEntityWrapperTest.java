package com.diamonddagger590.mccore.util.item;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.external.common.CustomEntityHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    static class TestCustomEntityPluginHook extends PluginHook<CorePlugin> implements CustomEntityHook {

        TestCustomEntityPluginHook() {
            super(null);
        }

        @Override
        public boolean isCustomEntity(@NotNull UUID uuid) {
            return false;
        }

        @Override
        public boolean isCustomEntity(@NotNull String customEntity) {
            return "mythicmobs:fire_dragon".equals(customEntity);
        }

        @Override
        public boolean isCustomEntityOfType(@NotNull UUID uuid, @NotNull String customEntityType) {
            return false;
        }

        @NotNull
        @Override
        public Optional<Set<String>> entityModels(@NotNull Entity entity) {
            return Optional.empty();
        }

        @NotNull
        @Override
        public String entityName(@NotNull CustomEntityWrapper customEntityWrapper) {
            return "Fire Dragon";
        }
    }

    @Nested
    @DisplayName("EntityType constructor")
    class EntityTypeConstructor {

        @Test
        @DisplayName("Given an entity type, when getting entityType, then returns that type")
        void entityType_returnsProvidedType_whenConstructedWithEntityType() {
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.ZOMBIE);
            assertTrue(wrapper.entityType().isPresent());
            assertEquals(EntityType.ZOMBIE, wrapper.entityType().get());
        }

        @Test
        @DisplayName("Given an entity type, when getting customEntity, then returns empty")
        void customEntity_returnsEmpty_whenConstructedWithEntityType() {
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.CREEPER);
            assertFalse(wrapper.customEntity().isPresent());
        }
    }

    @Nested
    @DisplayName("Custom entity accessors")
    class CustomEntityAccessors {

        @Test
        @DisplayName("Given a custom entity wrapper, when getting customEntity, then returns the custom entity id")
        void customEntity_returnsId_whenConstructedWithCustomEntity() throws Exception {
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:fire_dragon");
            assertTrue(wrapper.customEntity().isPresent());
            assertEquals("mythicmobs:fire_dragon", wrapper.customEntity().get());
        }

        @Test
        @DisplayName("Given a custom entity wrapper, when getting entityType, then returns empty")
        void entityType_returnsEmpty_whenConstructedWithCustomEntity() throws Exception {
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:fire_dragon");
            assertFalse(wrapper.entityType().isPresent());
        }
    }

    @Nested
    @DisplayName("isVanilla")
    class IsVanilla {

        @Test
        @DisplayName("Entity type wrapper returns true")
        void isVanilla_returnsTrue_whenEntityTypeWrapper() {
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.ZOMBIE);
            assertTrue(wrapper.isVanilla());
        }

        @Test
        @DisplayName("Custom entity wrapper returns false")
        void isVanilla_returnsFalse_whenCustomEntityWrapper() throws Exception {
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:fire_dragon");
            assertFalse(wrapper.isVanilla());
        }
    }

    @Nested
    @DisplayName("isCustom")
    class IsCustom {

        @Test
        @DisplayName("Custom entity wrapper returns true")
        void isCustom_returnsTrue_whenCustomEntityWrapper() throws Exception {
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:fire_dragon");
            assertTrue(wrapper.isCustom());
        }

        @Test
        @DisplayName("Entity type wrapper returns false")
        void isCustom_returnsFalse_whenEntityTypeWrapper() {
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.ZOMBIE);
            assertFalse(wrapper.isCustom());
        }
    }

    @Nested
    @DisplayName("equals(EntityType)")
    class EqualsEntityType {

        @Test
        @DisplayName("Given matching entity type, when comparing, then returns true")
        void equals_returnsTrue_whenEntityTypeMatches() {
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.SKELETON);
            assertTrue(wrapper.equals(EntityType.SKELETON));
        }

        @Test
        @DisplayName("Given different entity type, when comparing, then returns false")
        void equals_returnsFalse_whenEntityTypeDiffers() {
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.SKELETON);
            assertFalse(wrapper.equals(EntityType.ZOMBIE));
        }

        @Test
        @DisplayName("Given custom entity wrapper, when comparing with any entity type, then returns false")
        void equals_returnsFalse_whenWrapperIsCustomEntity() throws Exception {
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:fire_dragon");
            assertFalse(wrapper.equals(EntityType.ENDER_DRAGON));
        }
    }

    @Nested
    @DisplayName("equals(String)")
    class EqualsString {

        @Test
        @DisplayName("Given matching custom entity id, when comparing, then returns true")
        void equals_returnsTrue_whenCustomEntityMatches() throws Exception {
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:fire_dragon");
            assertTrue(wrapper.equals("mythicmobs:fire_dragon"));
        }

        @Test
        @DisplayName("Given different custom entity id, when comparing, then returns false")
        void equals_returnsFalse_whenCustomEntityDiffers() throws Exception {
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:fire_dragon");
            assertFalse(wrapper.equals("mythicmobs:ice_dragon"));
        }

        @Test
        @DisplayName("Given entity type wrapper, when comparing with any string, then returns false")
        void equals_returnsFalse_whenWrapperIsEntityType() {
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.ZOMBIE);
            assertFalse(wrapper.equals("zombie"));
        }
    }

    @Nested
    @DisplayName("equals(CustomEntityWrapper)")
    class EqualsWrapper {

        @Test
        @DisplayName("Given two entity type wrappers with same type, when comparing, then returns true")
        void equals_returnsTrue_whenBothEntityTypesMatch() {
            CustomEntityWrapper a = entityTypeWrapper(EntityType.ZOMBIE);
            CustomEntityWrapper b = entityTypeWrapper(EntityType.ZOMBIE);
            assertTrue(a.equals(b));
        }

        @Test
        @DisplayName("Given two entity type wrappers with different types, when comparing, then returns false")
        void equals_returnsFalse_whenEntityTypesDiffer() {
            CustomEntityWrapper a = entityTypeWrapper(EntityType.ZOMBIE);
            CustomEntityWrapper b = entityTypeWrapper(EntityType.SKELETON);
            assertFalse(a.equals(b));
        }

        @Test
        @DisplayName("Given two custom entity wrappers with same id, when comparing, then returns true")
        void equals_returnsTrue_whenBothCustomEntitiesMatch() throws Exception {
            CustomEntityWrapper a = customEntityWrapper("mythicmobs:fire_dragon");
            CustomEntityWrapper b = customEntityWrapper("mythicmobs:fire_dragon");
            assertTrue(a.equals(b));
        }

        @Test
        @DisplayName("Given two custom entity wrappers with different ids, when comparing, then returns false")
        void equals_returnsFalse_whenCustomEntitiesDiffer() throws Exception {
            CustomEntityWrapper a = customEntityWrapper("mythicmobs:fire_dragon");
            CustomEntityWrapper b = customEntityWrapper("mythicmobs:ice_dragon");
            assertFalse(a.equals(b));
        }

        @Test
        @DisplayName("Given entity type wrapper and custom entity wrapper, when comparing, then returns false")
        void equals_returnsFalse_whenTypesAreMixed() throws Exception {
            CustomEntityWrapper entityBased = entityTypeWrapper(EntityType.ZOMBIE);
            CustomEntityWrapper customBased = customEntityWrapper("mythicmobs:zombie");
            assertFalse(entityBased.equals(customBased));
        }
    }

    @Nested
    @DisplayName("equals(Object)")
    class EqualsObject {

        @Test
        @DisplayName("Given same instance, when comparing, then returns true")
        void equals_returnsTrue_whenSameInstance() {
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.CREEPER);
            assertEquals(wrapper, wrapper);
        }

        @Test
        @DisplayName("Given null, when comparing, then returns false")
        void equals_returnsFalse_whenComparedWithNull() {
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.CREEPER);
            assertNotEquals(null, wrapper);
        }

        @Test
        @DisplayName("Given two entity type wrappers with same type, when comparing as Object, then returns true")
        void equals_returnsTrue_whenEntityTypeWrappersMatch() {
            CustomEntityWrapper a = entityTypeWrapper(EntityType.CREEPER);
            CustomEntityWrapper b = entityTypeWrapper(EntityType.CREEPER);
            assertEquals(a, b);
        }

        @Test
        @DisplayName("Given two custom entity wrappers with same id, when comparing as Object, then returns true")
        void equals_returnsTrue_whenCustomEntityWrappersMatch() throws Exception {
            CustomEntityWrapper a = customEntityWrapper("mythicmobs:fire_dragon");
            CustomEntityWrapper b = customEntityWrapper("mythicmobs:fire_dragon");
            assertEquals(a, b);
        }

        @Test
        @DisplayName("Given entity wrapper and different type, when comparing, then returns false")
        void equals_returnsFalse_whenComparedWithDifferentType() {
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.ZOMBIE);
            assertNotEquals(wrapper, "not a wrapper");
        }

        @Test
        @DisplayName("Given one entity type and one custom wrapper, when comparing as Object, then returns false")
        void equals_returnsFalse_whenOneIsEntityTypeAndOtherIsCustom() throws Exception {
            CustomEntityWrapper a = entityTypeWrapper(EntityType.ZOMBIE);
            CustomEntityWrapper b = customEntityWrapper("mythicmobs:zombie");
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("Given two different entity type wrappers, when comparing as Object, then returns false")
        void equals_returnsFalse_whenEntityTypesDiffer() {
            CustomEntityWrapper a = entityTypeWrapper(EntityType.ZOMBIE);
            CustomEntityWrapper b = entityTypeWrapper(EntityType.SKELETON);
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("Given two different custom entity wrappers, when comparing as Object, then returns false")
        void equals_returnsFalse_whenCustomEntitiesDiffer() throws Exception {
            CustomEntityWrapper a = customEntityWrapper("mythicmobs:fire_dragon");
            CustomEntityWrapper b = customEntityWrapper("mythicmobs:ice_dragon");
            assertNotEquals(a, b);
        }
    }

    @Nested
    @DisplayName("hashCode")
    class HashCode {

        @Test
        @DisplayName("Given two equal entity type wrappers, when getting hashCode, then values are equal")
        void hashCode_returnsEqualValues_whenEntityTypeWrappersAreEqual() {
            CustomEntityWrapper a = entityTypeWrapper(EntityType.ZOMBIE);
            CustomEntityWrapper b = entityTypeWrapper(EntityType.ZOMBIE);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Given two equal custom entity wrappers, when getting hashCode, then values are equal")
        void hashCode_returnsEqualValues_whenCustomEntityWrappersAreEqual() throws Exception {
            CustomEntityWrapper a = customEntityWrapper("mythicmobs:fire_dragon");
            CustomEntityWrapper b = customEntityWrapper("mythicmobs:fire_dragon");
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Given entity type wrapper, when getting hashCode multiple times, then value is consistent")
        void hashCode_returnsSameValue_whenCalledMultipleTimes() {
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.CREEPER);
            int first = wrapper.hashCode();
            int second = wrapper.hashCode();
            assertEquals(first, second);
        }

        @Test
        @DisplayName("Given two unequal entity type wrappers, when getting hashCode, then values differ")
        void hashCode_returnsDifferentValues_whenEntityTypeWrappersDiffer() {
            CustomEntityWrapper a = entityTypeWrapper(EntityType.ZOMBIE);
            CustomEntityWrapper b = entityTypeWrapper(EntityType.SKELETON);
            assertNotEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Given two unequal custom entity wrappers, when getting hashCode, then values differ")
        void hashCode_returnsDifferentValues_whenCustomEntityWrappersDiffer() throws Exception {
            CustomEntityWrapper a = customEntityWrapper("mythicmobs:fire_dragon");
            CustomEntityWrapper b = customEntityWrapper("mythicmobs:ice_dragon");
            assertNotEquals(a.hashCode(), b.hashCode());
        }
    }

    @Nested
    @DisplayName("entityName")
    class EntityName {

        @Test
        @DisplayName("Given custom entity wrapper with no hooks registered, when getting entityName, then returns Unknown")
        void entityName_returnsUnknown_whenNoHooksRegistered() throws Exception {
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:fire_dragon");
            assertEquals("Unknown", wrapper.entityName());
        }

        @Test
        @DisplayName("Given custom entity wrapper with a hook registered, when getting entityName, then returns hook-provided name")
        void entityName_returnsHookProvidedName_whenHookIsRegistered() throws Exception {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(new TestCustomEntityPluginHook());
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:fire_dragon");
            assertEquals("Fire Dragon", wrapper.entityName());
        }

        @Test
        @DisplayName("Given vanilla entity type wrapper, when getting entityName, then returns lang tag")
        void entityName_returnsLangTag_whenVanillaEntityType() {
            MockBukkit.mock();
            try {
                CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.ZOMBIE);
                String name = wrapper.entityName();
                assertTrue(name.startsWith("<lang:"), "Expected lang tag but got: " + name);
                assertTrue(name.endsWith(">"), "Expected lang tag to end with > but got: " + name);
            } finally {
                MockBukkit.unmock();
            }
        }

        @Test
        @DisplayName("Given custom entity with hook that doesn't recognize it, when getting entityName, then returns Unknown")
        void entityName_returnsUnknown_whenHookDoesNotRecognize() throws Exception {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(new TestCustomEntityPluginHook());
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:unknown_entity");
            assertEquals("Unknown", wrapper.entityName());
        }
    }

    @Nested
    @DisplayName("Constructor(Entity)")
    class EntityConstructor {

        @Test
        @DisplayName("Given an entity with no hooks registered, when constructing, then uses entity type")
        void constructor_usesEntityType_whenNoHooksRegistered() {
            Entity mockEntity = mock(Entity.class);
            when(mockEntity.getType()).thenReturn(EntityType.ZOMBIE);

            CustomEntityWrapper wrapper = new CustomEntityWrapper(mockEntity);

            assertTrue(wrapper.isVanilla());
            assertFalse(wrapper.isCustom());
            assertTrue(wrapper.entityType().isPresent());
            assertEquals(EntityType.ZOMBIE, wrapper.entityType().get());
        }

        @Test
        @DisplayName("Given an entity with hook that recognizes it and returns models, when constructing, then uses custom entity")
        void constructor_usesCustomEntity_whenHookReturnsModels() {
            Entity mockEntity = mock(Entity.class);
            UUID uuid = UUID.randomUUID();
            when(mockEntity.getUniqueId()).thenReturn(uuid);
            ConfigurableEntityHook hook = new ConfigurableEntityHook(true, Optional.of(Set.of("mythicmobs:fire_dragon")));
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            CustomEntityWrapper wrapper = new CustomEntityWrapper(mockEntity);

            assertTrue(wrapper.isCustom());
            assertFalse(wrapper.isVanilla());
            assertTrue(wrapper.customEntity().isPresent());
            assertEquals("mythicmobs:fire_dragon", wrapper.customEntity().get());
        }

        @Test
        @DisplayName("Given an entity with hook that recognizes it but returns empty models, when constructing, then uses entity type")
        void constructor_usesEntityType_whenHookReturnsEmptyModels() {
            Entity mockEntity = mock(Entity.class);
            UUID uuid = UUID.randomUUID();
            when(mockEntity.getUniqueId()).thenReturn(uuid);
            when(mockEntity.getType()).thenReturn(EntityType.CREEPER);
            ConfigurableEntityHook hook = new ConfigurableEntityHook(true, Optional.of(Set.of()));
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            CustomEntityWrapper wrapper = new CustomEntityWrapper(mockEntity);

            assertTrue(wrapper.isVanilla());
            assertEquals(EntityType.CREEPER, wrapper.entityType().get());
        }

        @Test
        @DisplayName("Given an entity with hook that does not recognize it, when constructing, then uses entity type")
        void constructor_usesEntityType_whenHookDoesNotRecognize() {
            Entity mockEntity = mock(Entity.class);
            UUID uuid = UUID.randomUUID();
            when(mockEntity.getUniqueId()).thenReturn(uuid);
            when(mockEntity.getType()).thenReturn(EntityType.SKELETON);
            ConfigurableEntityHook hook = new ConfigurableEntityHook(false, Optional.empty());
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            CustomEntityWrapper wrapper = new CustomEntityWrapper(mockEntity);

            assertTrue(wrapper.isVanilla());
            assertEquals(EntityType.SKELETON, wrapper.entityType().get());
        }

        @Test
        @DisplayName("Given an entity with hook that returns empty optional for models, when constructing, then uses entity type")
        void constructor_usesEntityType_whenHookReturnsEmptyOptional() {
            Entity mockEntity = mock(Entity.class);
            UUID uuid = UUID.randomUUID();
            when(mockEntity.getUniqueId()).thenReturn(uuid);
            when(mockEntity.getType()).thenReturn(EntityType.SPIDER);
            ConfigurableEntityHook hook = new ConfigurableEntityHook(true, Optional.empty());
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            CustomEntityWrapper wrapper = new CustomEntityWrapper(mockEntity);

            assertTrue(wrapper.isVanilla());
            assertEquals(EntityType.SPIDER, wrapper.entityType().get());
        }
    }

    @Nested
    @DisplayName("equals(Entity)")
    class EqualsEntity {

        @Test
        @DisplayName("Given vanilla wrapper and entity with matching type, when comparing, then returns true")
        void equalsEntity_returnsTrue_whenEntityTypeMatches() {
            Entity mockEntity = mock(Entity.class);
            when(mockEntity.getType()).thenReturn(EntityType.ZOMBIE);
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.ZOMBIE);

            assertTrue(wrapper.equals(mockEntity));
        }

        @Test
        @DisplayName("Given vanilla wrapper and entity with different type, when comparing, then returns false")
        void equalsEntity_returnsFalse_whenEntityTypeDiffers() {
            Entity mockEntity = mock(Entity.class);
            when(mockEntity.getType()).thenReturn(EntityType.SKELETON);
            CustomEntityWrapper wrapper = entityTypeWrapper(EntityType.ZOMBIE);

            assertFalse(wrapper.equals(mockEntity));
        }

        @Test
        @DisplayName("Given custom wrapper with hook that confirms type match, when comparing, then returns true")
        void equalsEntity_returnsTrue_whenHookConfirmsMatch() throws Exception {
            Entity mockEntity = mock(Entity.class);
            UUID uuid = UUID.randomUUID();
            when(mockEntity.getUniqueId()).thenReturn(uuid);
            ConfigurableEntityHook hook = new ConfigurableEntityHook(uuid, "mythicmobs:fire_dragon");
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:fire_dragon");

            assertTrue(wrapper.equals(mockEntity));
        }

        @Test
        @DisplayName("Given custom wrapper with no hooks, when comparing with entity, then returns false")
        void equalsEntity_returnsFalse_whenNoHooksRegistered() throws Exception {
            Entity mockEntity = mock(Entity.class);
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:fire_dragon");

            assertFalse(wrapper.equals(mockEntity));
        }

        @Test
        @DisplayName("Given custom wrapper with hook that denies match, when comparing, then returns false")
        void equalsEntity_returnsFalse_whenHookDeniesMatch() throws Exception {
            Entity mockEntity = mock(Entity.class);
            UUID uuid = UUID.randomUUID();
            when(mockEntity.getUniqueId()).thenReturn(uuid);
            ConfigurableEntityHook hook = new ConfigurableEntityHook(uuid, "mythicmobs:ice_dragon");
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:fire_dragon");

            assertFalse(wrapper.equals(mockEntity));
        }
    }

    @Nested
    @DisplayName("customModels (static)")
    class CustomModelsTests {

        @Test
        @DisplayName("Given no hooks registered, when getting customModels, then returns empty set in Optional")
        void customModels_returnsEmptySet_whenNoHooksRegistered() {
            Entity mockEntity = mock(Entity.class);
            Optional<Set<String>> result = CustomEntityWrapper.customModels(mockEntity);

            assertTrue(result.isPresent());
            assertTrue(result.get().isEmpty());
        }

        @Test
        @DisplayName("Given hook that returns models, when getting customModels, then returns those models")
        void customModels_returnsModels_whenHookProvidesModels() {
            Entity mockEntity = mock(Entity.class);
            ConfigurableEntityHook hook = new ConfigurableEntityHook(false, Optional.of(Set.of("model_a", "model_b")));
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            Optional<Set<String>> result = CustomEntityWrapper.customModels(mockEntity);

            assertTrue(result.isPresent());
            assertEquals(Set.of("model_a", "model_b"), result.get());
        }

        @Test
        @DisplayName("Given hook that returns empty optional, when getting customModels, then returns empty set")
        void customModels_returnsEmptySet_whenHookReturnsEmptyOptional() {
            Entity mockEntity = mock(Entity.class);
            ConfigurableEntityHook hook = new ConfigurableEntityHook(false, Optional.empty());
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            Optional<Set<String>> result = CustomEntityWrapper.customModels(mockEntity);

            assertTrue(result.isPresent());
            assertTrue(result.get().isEmpty());
        }
    }

    static class ConfigurableEntityHook extends PluginHook<CorePlugin> implements CustomEntityHook {

        private final boolean isCustomEntity;
        private final Optional<Set<String>> entityModels;
        private final UUID matchingUuid;
        private final String matchingType;

        ConfigurableEntityHook(boolean isCustomEntity, Optional<Set<String>> entityModels) {
            super(null);
            this.isCustomEntity = isCustomEntity;
            this.entityModels = entityModels;
            this.matchingUuid = null;
            this.matchingType = null;
        }

        ConfigurableEntityHook(UUID matchingUuid, String matchingType) {
            super(null);
            this.isCustomEntity = false;
            this.entityModels = Optional.empty();
            this.matchingUuid = matchingUuid;
            this.matchingType = matchingType;
        }

        @Override
        public boolean isCustomEntity(@NotNull Entity entity) {
            return isCustomEntity;
        }

        @Override
        public boolean isCustomEntity(@NotNull UUID uuid) {
            return isCustomEntity || (matchingUuid != null && matchingUuid.equals(uuid));
        }

        @Override
        public boolean isCustomEntity(@NotNull String customEntity) {
            return false;
        }

        @Override
        public boolean isCustomEntityOfType(@NotNull Entity entity, @NotNull String customEntityType) {
            return matchingType != null && matchingType.equals(customEntityType)
                    && matchingUuid != null && matchingUuid.equals(entity.getUniqueId());
        }

        @Override
        public boolean isCustomEntityOfType(@NotNull UUID uuid, @NotNull String customEntityType) {
            return matchingType != null && matchingType.equals(customEntityType)
                    && matchingUuid != null && matchingUuid.equals(uuid);
        }

        @NotNull
        @Override
        public Optional<Set<String>> entityModels(@NotNull Entity entity) {
            return entityModels;
        }

        @NotNull
        @Override
        public String entityName(@NotNull CustomEntityWrapper customEntityWrapper) {
            return "";
        }
    }
}

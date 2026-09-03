package com.diamonddagger590.mccore.util.item;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.external.common.CustomEntityHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomEntityWrapperMockBukkitTest {

    private ServerMock server;
    private TestCorePlugin plugin;
    private World world;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestCorePlugin.class);
        RegistryResetExtension.setupRegistry();
        world = server.addSimpleWorld("test_world");
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
        MockBukkit.unmock();
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

        private final String recognizedEntity;
        private final boolean recognizeByUuid;

        TestCustomEntityPluginHook(String recognizedEntity) {
            this(recognizedEntity, false);
        }

        TestCustomEntityPluginHook(String recognizedEntity, boolean recognizeByUuid) {
            super(null);
            this.recognizedEntity = recognizedEntity;
            this.recognizeByUuid = recognizeByUuid;
        }

        @Override
        public boolean isCustomEntity(@NotNull UUID uuid) {
            return recognizeByUuid;
        }

        @Override
        public boolean isCustomEntity(@NotNull String customEntity) {
            return recognizedEntity.equals(customEntity);
        }

        @Override
        public boolean isCustomEntityOfType(@NotNull UUID uuid, @NotNull String customEntityType) {
            return recognizeByUuid && recognizedEntity.equals(customEntityType);
        }

        @NotNull
        @Override
        public Optional<Set<String>> entityModels(@NotNull Entity entity) {
            if (recognizeByUuid) {
                return Optional.of(Set.of(recognizedEntity));
            }
            return Optional.empty();
        }

        @NotNull
        @Override
        public String entityName(@NotNull CustomEntityWrapper customEntityWrapper) {
            return "Fire Dragon";
        }
    }

    @Nested
    @DisplayName("String constructor")
    class StringConstructor {

        @Test
        @DisplayName("Given a vanilla entity name, when constructing, then sets entityType and null customEntity")
        void constructor_setsEntityType_whenVanillaEntityName() {
            CustomEntityWrapper wrapper = new CustomEntityWrapper("zombie");
            assertTrue(wrapper.isVanilla());
            assertFalse(wrapper.isCustom());
            assertTrue(wrapper.entityType().isPresent());
            assertEquals(EntityType.ZOMBIE, wrapper.entityType().get());
            assertFalse(wrapper.customEntity().isPresent());
        }

        @Test
        @DisplayName("Given a non-vanilla entity name, when constructing, then sets customEntity and null entityType")
        void constructor_setsCustomEntity_whenNonVanillaEntityName() {
            CustomEntityWrapper wrapper = new CustomEntityWrapper("totally_fake_entity");
            assertFalse(wrapper.isVanilla());
            assertTrue(wrapper.isCustom());
            assertFalse(wrapper.entityType().isPresent());
            assertTrue(wrapper.customEntity().isPresent());
            assertEquals("totally_fake_entity", wrapper.customEntity().get());
        }

        @Test
        @DisplayName("Given another vanilla entity name, when constructing, then correctly identifies it as vanilla")
        void constructor_setsEntityType_whenAnotherVanillaEntityName() {
            CustomEntityWrapper wrapper = new CustomEntityWrapper("creeper");
            assertTrue(wrapper.isVanilla());
            assertTrue(wrapper.entityType().isPresent());
            assertEquals(EntityType.CREEPER, wrapper.entityType().get());
        }
    }

    @Nested
    @DisplayName("isVanillaEntity (static)")
    class IsVanillaEntity {

        @Test
        @DisplayName("Given a vanilla entity id, when checking isVanillaEntity, then returns true")
        void isVanillaEntity_returnsTrue_whenVanillaEntityId() {
            assertTrue(CustomEntityWrapper.isVanillaEntity("zombie"));
        }

        @Test
        @DisplayName("Given a custom entity id, when checking isVanillaEntity, then returns false")
        void isVanillaEntity_returnsFalse_whenCustomEntityId() {
            assertFalse(CustomEntityWrapper.isVanillaEntity("totally_fake_entity"));
        }

        @Test
        @DisplayName("Given another vanilla entity id, when checking isVanillaEntity, then returns true")
        void isVanillaEntity_returnsTrue_whenAnotherVanillaEntityId() {
            assertTrue(CustomEntityWrapper.isVanillaEntity("skeleton"));
        }

        @Test
        @DisplayName("Given an invalid entity id, when checking isVanillaEntity, then returns false")
        void isVanillaEntity_returnsFalse_whenInvalidEntityId() {
            assertFalse(CustomEntityWrapper.isVanillaEntity("nonexistent_mob_type"));
        }
    }

    @Nested
    @DisplayName("entityName for vanilla entity type")
    class EntityNameVanilla {

        @Test
        @DisplayName("Given a vanilla entity type wrapper, when getting entityName, then returns lang tag with translation key")
        void entityName_returnsLangTag_whenVanillaEntityType() {
            CustomEntityWrapper wrapper = new CustomEntityWrapper(EntityType.ZOMBIE);
            String name = wrapper.entityName();
            assertEquals("<lang:" + EntityType.ZOMBIE.translationKey() + ">", name);
        }

        @Test
        @DisplayName("Given a different vanilla entity type, when getting entityName, then returns correct lang tag")
        void entityName_returnsCorrectLangTag_whenDifferentVanillaEntityType() {
            CustomEntityWrapper wrapper = new CustomEntityWrapper(EntityType.CREEPER);
            String name = wrapper.entityName();
            assertEquals("<lang:" + EntityType.CREEPER.translationKey() + ">", name);
        }
    }

    @Nested
    @DisplayName("Entity constructor")
    class EntityConstructor {

        @Test
        @DisplayName("Given a vanilla entity with no hooks, when constructing, then sets entityType")
        void constructor_setsEntityType_whenVanillaEntityNoHooks() {
            Entity entity = world.spawnEntity(new Location(world, 0, 64, 0), EntityType.ZOMBIE);
            CustomEntityWrapper wrapper = new CustomEntityWrapper(entity);
            assertTrue(wrapper.isVanilla());
            assertFalse(wrapper.isCustom());
            assertTrue(wrapper.entityType().isPresent());
            assertEquals(EntityType.ZOMBIE, wrapper.entityType().get());
        }

        @Test
        @DisplayName("Given an entity with a hook recognizing it as custom, when constructing, then sets customEntity")
        void constructor_setsCustomEntity_whenHookRecognizesEntity() {
            TestCustomEntityPluginHook hook = new TestCustomEntityPluginHook("mythicmobs:fire_dragon", true);
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);
            Entity entity = world.spawnEntity(new Location(world, 0, 64, 0), EntityType.ZOMBIE);
            CustomEntityWrapper wrapper = new CustomEntityWrapper(entity);
            assertTrue(wrapper.isCustom());
            assertFalse(wrapper.isVanilla());
            assertTrue(wrapper.customEntity().isPresent());
            assertEquals("mythicmobs:fire_dragon", wrapper.customEntity().get());
        }

        @Test
        @DisplayName("Given an entity with a hook that does not recognize it, when constructing, then sets entityType")
        void constructor_setsEntityType_whenHookDoesNotRecognize() {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomEntityPluginHook("mythicmobs:fire_dragon", false));
            Entity entity = world.spawnEntity(new Location(world, 0, 64, 0), EntityType.ZOMBIE);
            CustomEntityWrapper wrapper = new CustomEntityWrapper(entity);
            assertTrue(wrapper.isVanilla());
            assertTrue(wrapper.entityType().isPresent());
            assertEquals(EntityType.ZOMBIE, wrapper.entityType().get());
        }
    }

    @Nested
    @DisplayName("equals(Entity)")
    class EqualsEntity {

        @Test
        @DisplayName("Given a vanilla entity type wrapper matching entity type, when comparing, then returns true")
        void equals_returnsTrue_whenEntityTypeMatches() {
            Entity entity = world.spawnEntity(new Location(world, 0, 64, 0), EntityType.ZOMBIE);
            CustomEntityWrapper wrapper = new CustomEntityWrapper(EntityType.ZOMBIE);
            assertTrue(wrapper.equals(entity));
        }

        @Test
        @DisplayName("Given a vanilla entity type wrapper not matching entity type, when comparing, then returns false")
        void equals_returnsFalse_whenEntityTypeDoesNotMatch() {
            Entity entity = world.spawnEntity(new Location(world, 0, 64, 0), EntityType.ZOMBIE);
            CustomEntityWrapper wrapper = new CustomEntityWrapper(EntityType.SKELETON);
            assertFalse(wrapper.equals(entity));
        }

        @Test
        @DisplayName("Given a custom entity wrapper with no hooks, when comparing, then returns false")
        void equals_returnsFalse_whenCustomEntityNoHooks() throws Exception {
            Entity entity = world.spawnEntity(new Location(world, 0, 64, 0), EntityType.ZOMBIE);
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:fire_dragon");
            assertFalse(wrapper.equals(entity));
        }

        @Test
        @DisplayName("Given a custom entity wrapper with a matching hook, when comparing, then returns true")
        void equals_returnsTrue_whenCustomEntityHookMatches() throws Exception {
            TestCustomEntityPluginHook hook = new TestCustomEntityPluginHook("mythicmobs:fire_dragon", true);
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);
            Entity entity = world.spawnEntity(new Location(world, 0, 64, 0), EntityType.ZOMBIE);
            CustomEntityWrapper wrapper = customEntityWrapper("mythicmobs:fire_dragon");
            assertTrue(wrapper.equals(entity));
        }
    }

    @Nested
    @DisplayName("customModels (static)")
    class CustomModels {

        @Test
        @DisplayName("Given an entity with no hooks, when getting customModels, then returns empty set")
        void customModels_returnsEmptySet_whenNoHooksRegistered() {
            Entity entity = world.spawnEntity(new Location(world, 0, 64, 0), EntityType.ZOMBIE);
            Optional<Set<String>> models = CustomEntityWrapper.customModels(entity);
            assertTrue(models.isPresent());
            assertTrue(models.get().isEmpty());
        }

        @Test
        @DisplayName("Given an entity with a hook, when getting customModels, then returns models from hook")
        void customModels_returnsModelsFromHook_whenHookRegistered() {
            TestCustomEntityPluginHook hook = new TestCustomEntityPluginHook("mythicmobs:fire_dragon", true);
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);
            Entity entity = world.spawnEntity(new Location(world, 0, 64, 0), EntityType.ZOMBIE);
            Optional<Set<String>> models = CustomEntityWrapper.customModels(entity);
            assertTrue(models.isPresent());
            assertTrue(models.get().contains("mythicmobs:fire_dragon"));
        }

        @Test
        @DisplayName("Given an entity with a hook that does not recognize it, when getting customModels, then returns empty set")
        void customModels_returnsEmptySet_whenHookDoesNotRecognize() {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomEntityPluginHook("mythicmobs:fire_dragon", false));
            Entity entity = world.spawnEntity(new Location(world, 0, 64, 0), EntityType.ZOMBIE);
            Optional<Set<String>> models = CustomEntityWrapper.customModels(entity);
            assertTrue(models.isPresent());
            assertTrue(models.get().isEmpty());
        }
    }
}

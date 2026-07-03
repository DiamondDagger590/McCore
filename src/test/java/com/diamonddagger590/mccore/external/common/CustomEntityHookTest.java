package com.diamonddagger590.mccore.external.common;

import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomEntityHookTest {

    private static final UUID KNOWN_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID UNKNOWN_UUID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private CustomEntityHook createHook(String recognizedEntity, UUID recognizedUuid) {
        return new CustomEntityHook() {
            @Override
            public boolean isCustomEntity(@NotNull UUID uuid) {
                return recognizedUuid.equals(uuid);
            }

            @Override
            public boolean isCustomEntity(@NotNull String customEntity) {
                return recognizedEntity.equals(customEntity);
            }

            @Override
            public boolean isCustomEntityOfType(@NotNull UUID uuid, @NotNull String customEntityType) {
                return recognizedUuid.equals(uuid) && recognizedEntity.equals(customEntityType);
            }

            @NotNull
            @Override
            public Optional<Set<String>> entityModels(@NotNull Entity entity) {
                return Optional.empty();
            }

            @NotNull
            @Override
            public String entityName(@NotNull CustomEntityWrapper customEntityWrapper) {
                return "";
            }
        };
    }

    private static Entity proxyEntity(UUID uuid) {
        return (Entity) Proxy.newProxyInstance(
                Entity.class.getClassLoader(),
                new Class[]{Entity.class},
                (proxy, method, args) -> {
                    if ("getUniqueId".equals(method.getName())) {
                        return uuid;
                    }
                    return null;
                }
        );
    }

    // ── isCustomEntity(Entity) ─────────────────────────────────────────────

    @Test
    @DisplayName("Given an entity with a recognized UUID, when checking isCustomEntity via Entity default method, then returns true")
    void isCustomEntity_returnsTrue_whenEntityUuidIsRecognized() {
        CustomEntityHook hook = createHook("custom_dragon", KNOWN_UUID);
        Entity entity = proxyEntity(KNOWN_UUID);

        assertTrue(hook.isCustomEntity(entity));
    }

    @Test
    @DisplayName("Given an entity with an unrecognized UUID, when checking isCustomEntity via Entity default method, then returns false")
    void isCustomEntity_returnsFalse_whenEntityUuidIsUnrecognized() {
        CustomEntityHook hook = createHook("custom_dragon", KNOWN_UUID);
        Entity entity = proxyEntity(UNKNOWN_UUID);

        assertFalse(hook.isCustomEntity(entity));
    }

    // ── isCustomEntityOfType(Entity, String) ───────────────────────────────

    @Test
    @DisplayName("Given a matching entity and type, when checking isCustomEntityOfType via default method, then returns true")
    void isCustomEntityOfType_returnsTrue_whenEntityAndTypeMatch() {
        CustomEntityHook hook = createHook("custom_dragon", KNOWN_UUID);
        Entity entity = proxyEntity(KNOWN_UUID);

        assertTrue(hook.isCustomEntityOfType(entity, "custom_dragon"));
    }

    @Test
    @DisplayName("Given a matching entity but wrong type, when checking isCustomEntityOfType via default method, then returns false")
    void isCustomEntityOfType_returnsFalse_whenTypeDoesNotMatch() {
        CustomEntityHook hook = createHook("custom_dragon", KNOWN_UUID);
        Entity entity = proxyEntity(KNOWN_UUID);

        assertFalse(hook.isCustomEntityOfType(entity, "wrong_type"));
    }

    @Test
    @DisplayName("Given a non-matching entity, when checking isCustomEntityOfType via default method, then returns false")
    void isCustomEntityOfType_returnsFalse_whenEntityDoesNotMatch() {
        CustomEntityHook hook = createHook("custom_dragon", KNOWN_UUID);
        Entity entity = proxyEntity(UNKNOWN_UUID);

        assertFalse(hook.isCustomEntityOfType(entity, "custom_dragon"));
    }

    // ── isCustomEntity(CustomEntityWrapper) ────────────────────────────────

    @Test
    @DisplayName("Given a vanilla entity wrapper, when checking isCustomEntity via wrapper default method, then returns false")
    void isCustomEntity_returnsFalse_whenWrapperHasNoCustomEntity() {
        CustomEntityHook hook = createHook("custom_dragon", KNOWN_UUID);
        CustomEntityWrapper vanillaWrapper = new CustomEntityWrapper(EntityType.ZOMBIE);

        assertFalse(hook.isCustomEntity(vanillaWrapper));
    }

    @Test
    @DisplayName("Given a custom entity wrapper matching the hook, when checking isCustomEntity via wrapper default method, then returns true")
    void isCustomEntity_returnsTrue_whenWrapperHasMatchingCustomEntity() {
        CustomEntityHook hook = createHook("custom_dragon", KNOWN_UUID);
        CustomEntityWrapper customWrapper = new TestCustomEntityWrapper("custom_dragon");

        assertTrue(hook.isCustomEntity(customWrapper));
    }

    @Test
    @DisplayName("Given a custom entity wrapper not matching the hook, when checking isCustomEntity via wrapper default method, then returns false")
    void isCustomEntity_returnsFalse_whenWrapperHasNonMatchingCustomEntity() {
        CustomEntityHook hook = createHook("custom_dragon", KNOWN_UUID);
        CustomEntityWrapper customWrapper = new TestCustomEntityWrapper("other_mob");

        assertFalse(hook.isCustomEntity(customWrapper));
    }

    private static class TestCustomEntityWrapper extends CustomEntityWrapper {

        private final String testCustomEntity;

        TestCustomEntityWrapper(@NotNull String customEntity) {
            super(EntityType.ZOMBIE);
            this.testCustomEntity = customEntity;
        }

        @NotNull
        @Override
        public Optional<String> customEntity() {
            return Optional.ofNullable(testCustomEntity);
        }
    }
}

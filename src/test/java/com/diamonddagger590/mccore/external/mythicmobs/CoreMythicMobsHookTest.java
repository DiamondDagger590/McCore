package com.diamonddagger590.mccore.external.mythicmobs;

import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.core.mobs.MobExecutor;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class CoreMythicMobsHookTest {

    private CoreMythicMobsHook hook;
    private MockedStatic<MythicBukkit> mythicBukkitMock;
    private MythicBukkit mythicBukkitInstance;
    private MobExecutor mobManager;

    private static final UUID KNOWN_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID UNKNOWN_UUID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @BeforeEach
    void setUp() throws Exception {
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);
        hook = (CoreMythicMobsHook) unsafe.allocateInstance(CoreMythicMobsHook.class);

        mythicBukkitInstance = mock(MythicBukkit.class);
        mobManager = mock(MobExecutor.class);
        when(mythicBukkitInstance.getMobManager()).thenReturn(mobManager);

        mythicBukkitMock = mockStatic(MythicBukkit.class);
        mythicBukkitMock.when(MythicBukkit::inst).thenReturn(mythicBukkitInstance);
    }

    @AfterEach
    void tearDown() {
        mythicBukkitMock.close();
    }

    @Nested
    @DisplayName("isCustomEntity(UUID)")
    class IsCustomEntityByUuid {

        @Test
        @DisplayName("Given an active mob UUID, when checking isCustomEntity, then returns true")
        void returnsTrue_whenMobIsActive() {
            when(mobManager.isActiveMob(KNOWN_UUID)).thenReturn(true);

            assertTrue(hook.isCustomEntity(KNOWN_UUID));
        }

        @Test
        @DisplayName("Given a non-active mob UUID, when checking isCustomEntity, then returns false")
        void returnsFalse_whenMobIsNotActive() {
            when(mobManager.isActiveMob(UNKNOWN_UUID)).thenReturn(false);

            assertFalse(hook.isCustomEntity(UNKNOWN_UUID));
        }
    }

    @Nested
    @DisplayName("isCustomEntity(String)")
    class IsCustomEntityByName {

        @Test
        @DisplayName("Given a registered MythicMob type, when checking isCustomEntity, then returns true")
        void returnsTrue_whenMobTypeExists() {
            MythicMob mythicMob = mock(MythicMob.class);
            when(mobManager.getMythicMob("skeleton_king")).thenReturn(Optional.of(mythicMob));

            assertTrue(hook.isCustomEntity("skeleton_king"));
        }

        @Test
        @DisplayName("Given an unregistered MythicMob type, when checking isCustomEntity, then returns false")
        void returnsFalse_whenMobTypeDoesNotExist() {
            when(mobManager.getMythicMob("unknown_mob")).thenReturn(Optional.empty());

            assertFalse(hook.isCustomEntity("unknown_mob"));
        }
    }

    @Nested
    @DisplayName("isCustomEntityOfType")
    class IsCustomEntityOfType {

        @Test
        @DisplayName("Given an active mob with matching type, when checking isCustomEntityOfType, then returns true")
        void returnsTrue_whenMobTypeMatches() {
            ActiveMob activeMob = mock(ActiveMob.class);
            when(activeMob.getMobType()).thenReturn("skeleton_king");
            when(mobManager.getActiveMob(KNOWN_UUID)).thenReturn(Optional.of(activeMob));

            assertTrue(hook.isCustomEntityOfType(KNOWN_UUID, "skeleton_king"));
        }

        @Test
        @DisplayName("Given an active mob with non-matching type, when checking isCustomEntityOfType, then returns false")
        void returnsFalse_whenMobTypeDoesNotMatch() {
            ActiveMob activeMob = mock(ActiveMob.class);
            when(activeMob.getMobType()).thenReturn("skeleton_king");
            when(mobManager.getActiveMob(KNOWN_UUID)).thenReturn(Optional.of(activeMob));

            assertFalse(hook.isCustomEntityOfType(KNOWN_UUID, "dragon_boss"));
        }

        @Test
        @DisplayName("Given a non-active mob UUID, when checking isCustomEntityOfType, then returns false")
        void returnsFalse_whenMobIsNotActive() {
            when(mobManager.getActiveMob(UNKNOWN_UUID)).thenReturn(Optional.empty());

            assertFalse(hook.isCustomEntityOfType(UNKNOWN_UUID, "skeleton_king"));
        }
    }

    @Nested
    @DisplayName("entityModels")
    class EntityModels {

        @Test
        @DisplayName("Given an active mob entity, when getting entityModels, then returns the mob type")
        void returnsMobType_whenEntityIsActive() {
            Entity entity = mock(Entity.class);
            when(entity.getUniqueId()).thenReturn(KNOWN_UUID);

            ActiveMob activeMob = mock(ActiveMob.class);
            when(activeMob.getMobType()).thenReturn("skeleton_king");
            when(mobManager.getActiveMob(KNOWN_UUID)).thenReturn(Optional.of(activeMob));

            Optional<Set<String>> result = hook.entityModels(entity);

            assertTrue(result.isPresent());
            assertEquals(Set.of("skeleton_king"), result.get());
        }

        @Test
        @DisplayName("Given a non-active entity, when getting entityModels, then returns empty")
        void returnsEmpty_whenEntityIsNotActive() {
            Entity entity = mock(Entity.class);
            when(entity.getUniqueId()).thenReturn(UNKNOWN_UUID);

            when(mobManager.getActiveMob(UNKNOWN_UUID)).thenReturn(Optional.empty());

            Optional<Set<String>> result = hook.entityModels(entity);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("entityName")
    class EntityName {

        @Test
        @DisplayName("Given a custom entity with a registered display name, when getting entityName, then returns the display name")
        void returnsDisplayName_whenMobTypeHasDisplayName() {
            PlaceholderString displayName = mock(PlaceholderString.class);
            when(displayName.get()).thenReturn("Skeleton King");

            MythicMob mythicMob = mock(MythicMob.class);
            when(mythicMob.getDisplayName()).thenReturn(displayName);

            when(mobManager.getMythicMob("skeleton_king")).thenReturn(Optional.of(mythicMob));

            CustomEntityWrapper wrapper = new CustomEntityWrapper("skeleton_king");

            assertEquals("Skeleton King", hook.entityName(wrapper));
        }

        @Test
        @DisplayName("Given a custom entity with no registered mob type, when getting entityName, then returns the raw id")
        void returnsRawId_whenMobTypeNotRegistered() {
            when(mobManager.getMythicMob("unknown_mob")).thenReturn(Optional.empty());

            CustomEntityWrapper wrapper = new CustomEntityWrapper("unknown_mob");

            assertEquals("unknown_mob", hook.entityName(wrapper));
        }

        @Test
        @DisplayName("Given a vanilla entity wrapper, when getting entityName, then returns Unknown")
        void returnsUnknown_whenNoCustomEntity() {
            CustomEntityWrapper wrapper = new CustomEntityWrapper(EntityType.ZOMBIE);

            assertEquals("Unknown", hook.entityName(wrapper));
        }
    }
}

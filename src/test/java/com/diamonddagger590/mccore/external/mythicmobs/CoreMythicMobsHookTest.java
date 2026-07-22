package com.diamonddagger590.mccore.external.mythicmobs;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import io.lumine.mythic.core.mobs.MobExecutor;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.Entity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

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
    private MockedStatic<MythicBukkit> mythicBukkitStatic;
    private MythicBukkit mockMythicBukkit;
    private MobExecutor mockMobManager;

    @BeforeEach
    void setUp() {
        CorePlugin mockPlugin = mock(CorePlugin.class);
        hook = new CoreMythicMobsHook(mockPlugin);

        mockMythicBukkit = mock(MythicBukkit.class);
        mockMobManager = mock(MobExecutor.class);
        when(mockMythicBukkit.getMobManager()).thenReturn(mockMobManager);

        mythicBukkitStatic = mockStatic(MythicBukkit.class);
        mythicBukkitStatic.when(MythicBukkit::inst).thenReturn(mockMythicBukkit);
    }

    @AfterEach
    void tearDown() {
        mythicBukkitStatic.close();
    }

    @Nested
    @DisplayName("isCustomEntity(UUID)")
    class IsCustomEntityByUUIDTests {

        @Test
        @DisplayName("Given an active MythicMobs mob UUID, when isCustomEntity is called, then returns true")
        void isCustomEntity_returnsTrue_whenActiveMob() {
            UUID uuid = UUID.randomUUID();
            when(mockMobManager.isActiveMob(uuid)).thenReturn(true);

            assertTrue(hook.isCustomEntity(uuid));
        }

        @Test
        @DisplayName("Given a non-MythicMobs UUID, when isCustomEntity is called, then returns false")
        void isCustomEntity_returnsFalse_whenNotActiveMob() {
            UUID uuid = UUID.randomUUID();
            when(mockMobManager.isActiveMob(uuid)).thenReturn(false);

            assertFalse(hook.isCustomEntity(uuid));
        }
    }

    @Nested
    @DisplayName("isCustomEntity(String)")
    class IsCustomEntityByStringTests {

        @Test
        @DisplayName("Given a valid MythicMobs mob type, when isCustomEntity is called, then returns true")
        void isCustomEntity_returnsTrue_whenMobTypeExists() {
            when(mockMobManager.getMythicMob("SkeletonKing")).thenReturn(Optional.of(mock(MythicMob.class)));

            assertTrue(hook.isCustomEntity("SkeletonKing"));
        }

        @Test
        @DisplayName("Given an invalid mob type, when isCustomEntity is called, then returns false")
        void isCustomEntity_returnsFalse_whenMobTypeDoesNotExist() {
            when(mockMobManager.getMythicMob("nonexistent")).thenReturn(Optional.empty());

            assertFalse(hook.isCustomEntity("nonexistent"));
        }
    }

    @Nested
    @DisplayName("isCustomEntityOfType")
    class IsCustomEntityOfTypeTests {

        @Test
        @DisplayName("Given an active mob of matching type, when isCustomEntityOfType is called, then returns true")
        void isCustomEntityOfType_returnsTrue_whenMatches() {
            UUID uuid = UUID.randomUUID();
            ActiveMob mockActiveMob = mock(ActiveMob.class);
            when(mockActiveMob.getMobType()).thenReturn("SkeletonKing");
            when(mockMobManager.getActiveMob(uuid)).thenReturn(Optional.of(mockActiveMob));

            assertTrue(hook.isCustomEntityOfType(uuid, "SkeletonKing"));
        }

        @Test
        @DisplayName("Given an active mob of matching type (case insensitive), when isCustomEntityOfType is called, then returns true")
        void isCustomEntityOfType_returnsTrue_caseInsensitive() {
            UUID uuid = UUID.randomUUID();
            ActiveMob mockActiveMob = mock(ActiveMob.class);
            when(mockActiveMob.getMobType()).thenReturn("SkeletonKing");
            when(mockMobManager.getActiveMob(uuid)).thenReturn(Optional.of(mockActiveMob));

            assertTrue(hook.isCustomEntityOfType(uuid, "skeletonking"));
        }

        @Test
        @DisplayName("Given an active mob of different type, when isCustomEntityOfType is called, then returns false")
        void isCustomEntityOfType_returnsFalse_whenDifferentType() {
            UUID uuid = UUID.randomUUID();
            ActiveMob mockActiveMob = mock(ActiveMob.class);
            when(mockActiveMob.getMobType()).thenReturn("ZombieGuard");
            when(mockMobManager.getActiveMob(uuid)).thenReturn(Optional.of(mockActiveMob));

            assertFalse(hook.isCustomEntityOfType(uuid, "SkeletonKing"));
        }

        @Test
        @DisplayName("Given a non-MythicMobs UUID, when isCustomEntityOfType is called, then returns false")
        void isCustomEntityOfType_returnsFalse_whenNotActiveMob() {
            UUID uuid = UUID.randomUUID();
            when(mockMobManager.getActiveMob(uuid)).thenReturn(Optional.empty());

            assertFalse(hook.isCustomEntityOfType(uuid, "SkeletonKing"));
        }
    }

    @Nested
    @DisplayName("entityModels")
    class EntityModelsTests {

        @Test
        @DisplayName("Given an active MythicMobs entity, when entityModels is called, then returns mob type")
        void entityModels_returnsMobType_whenActiveMob() {
            Entity mockEntity = mock(Entity.class);
            UUID uuid = UUID.randomUUID();
            when(mockEntity.getUniqueId()).thenReturn(uuid);

            ActiveMob mockActiveMob = mock(ActiveMob.class);
            when(mockActiveMob.getMobType()).thenReturn("SkeletonKing");
            when(mockMobManager.getActiveMob(uuid)).thenReturn(Optional.of(mockActiveMob));

            Optional<Set<String>> result = hook.entityModels(mockEntity);

            assertTrue(result.isPresent());
            assertEquals(Set.of("SkeletonKing"), result.get());
        }

        @Test
        @DisplayName("Given a non-MythicMobs entity, when entityModels is called, then returns empty")
        void entityModels_returnsEmpty_whenNotActiveMob() {
            Entity mockEntity = mock(Entity.class);
            UUID uuid = UUID.randomUUID();
            when(mockEntity.getUniqueId()).thenReturn(uuid);
            when(mockMobManager.getActiveMob(uuid)).thenReturn(Optional.empty());

            Optional<Set<String>> result = hook.entityModels(mockEntity);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("entityName")
    class EntityNameTests {

        @Test
        @DisplayName("Given a custom entity with a display name, when entityName is called, then returns the display name")
        void entityName_returnsDisplayName_whenMobTypeExists() {
            CustomEntityWrapper wrapper = mock(CustomEntityWrapper.class);
            when(wrapper.customEntity()).thenReturn(Optional.of("SkeletonKing"));

            MythicMob mockMob = mock(MythicMob.class);
            PlaceholderString mockDisplayName = mock(PlaceholderString.class);
            when(mockDisplayName.get()).thenReturn("Skeleton King");
            when(mockMob.getDisplayName()).thenReturn(mockDisplayName);
            when(mockMobManager.getMythicMob("SkeletonKing")).thenReturn(Optional.of(mockMob));

            assertEquals("Skeleton King", hook.entityName(wrapper));
        }

        @Test
        @DisplayName("Given a custom entity not found in MythicMobs, when entityName is called, then returns the raw ID")
        void entityName_returnsRawId_whenMobTypeNotFound() {
            CustomEntityWrapper wrapper = mock(CustomEntityWrapper.class);
            when(wrapper.customEntity()).thenReturn(Optional.of("UnknownMob"));

            when(mockMobManager.getMythicMob("UnknownMob")).thenReturn(Optional.empty());

            assertEquals("UnknownMob", hook.entityName(wrapper));
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

package com.diamonddagger590.mccore.external.mythicmobs;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.mobs.MobExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
    private CorePlugin mockPlugin;
    private MockedStatic<MythicBukkit> mythicBukkitMock;
    private MythicBukkit mythicBukkitInstance;
    private MobExecutor mobManager;

    @BeforeEach
    void setUp() {
        mockPlugin = mock(CorePlugin.class);
        hook = new CoreMythicMobsHook(mockPlugin);

        mythicBukkitInstance = mock(MythicBukkit.class);
        mobManager = mock(MobExecutor.class);

        mythicBukkitMock = mockStatic(MythicBukkit.class);
        mythicBukkitMock.when(MythicBukkit::inst).thenReturn(mythicBukkitInstance);
        when(mythicBukkitInstance.getMobManager()).thenReturn(mobManager);
    }

    @AfterEach
    void tearDown() {
        mythicBukkitMock.close();
    }

    @Nested
    @DisplayName("isCustomEntity(UUID)")
    class IsCustomEntityByUuid {

        @Test
        @DisplayName("Given an active mob UUID, when checked, then returns true")
        void returnsTrue_whenUuidIsActiveMob() {
            UUID uuid = UUID.randomUUID();
            when(mobManager.isActiveMob(uuid)).thenReturn(true);

            assertTrue(hook.isCustomEntity(uuid));
        }

        @Test
        @DisplayName("Given a non-active mob UUID, when checked, then returns false")
        void returnsFalse_whenUuidIsNotActiveMob() {
            UUID uuid = UUID.randomUUID();
            when(mobManager.isActiveMob(uuid)).thenReturn(false);

            assertFalse(hook.isCustomEntity(uuid));
        }
    }

    @Nested
    @DisplayName("isCustomEntity(String)")
    class IsCustomEntityByName {

        @Test
        @DisplayName("Given a known mythic mob name, when checked, then returns true")
        void returnsTrue_whenMythicMobExists() {
            MythicMob mythicMob = mock(MythicMob.class);
            when(mobManager.getMythicMob("SkeletonKing")).thenReturn(Optional.of(mythicMob));

            assertTrue(hook.isCustomEntity("SkeletonKing"));
        }

        @Test
        @DisplayName("Given an unknown mythic mob name, when checked, then returns false")
        void returnsFalse_whenMythicMobDoesNotExist() {
            when(mobManager.getMythicMob("nonexistent")).thenReturn(Optional.empty());

            assertFalse(hook.isCustomEntity("nonexistent"));
        }
    }

    @Nested
    @DisplayName("isCustomEntityOfType(UUID, String)")
    class IsCustomEntityOfType {

        @Test
        @DisplayName("Given an active mob with matching type, when checked, then returns true")
        void returnsTrue_whenActiveMobMatchesType() {
            UUID uuid = UUID.randomUUID();
            ActiveMob activeMob = mock(ActiveMob.class);
            when(mobManager.getActiveMob(uuid)).thenReturn(Optional.of(activeMob));
            when(activeMob.getMobType()).thenReturn("SkeletonKing");

            assertTrue(hook.isCustomEntityOfType(uuid, "SkeletonKing"));
        }

        @Test
        @DisplayName("Given an active mob with matching type (case-insensitive), when checked, then returns true")
        void returnsTrue_whenTypeMatchesCaseInsensitively() {
            UUID uuid = UUID.randomUUID();
            ActiveMob activeMob = mock(ActiveMob.class);
            when(mobManager.getActiveMob(uuid)).thenReturn(Optional.of(activeMob));
            when(activeMob.getMobType()).thenReturn("skeletonking");

            assertTrue(hook.isCustomEntityOfType(uuid, "SkeletonKing"));
        }

        @Test
        @DisplayName("Given an active mob with non-matching type, when checked, then returns false")
        void returnsFalse_whenActiveMobDoesNotMatchType() {
            UUID uuid = UUID.randomUUID();
            ActiveMob activeMob = mock(ActiveMob.class);
            when(mobManager.getActiveMob(uuid)).thenReturn(Optional.of(activeMob));
            when(activeMob.getMobType()).thenReturn("ZombieKing");

            assertFalse(hook.isCustomEntityOfType(uuid, "SkeletonKing"));
        }

        @Test
        @DisplayName("Given a non-active mob UUID, when checked, then returns false")
        void returnsFalse_whenNoActiveMob() {
            UUID uuid = UUID.randomUUID();
            when(mobManager.getActiveMob(uuid)).thenReturn(Optional.empty());

            assertFalse(hook.isCustomEntityOfType(uuid, "SkeletonKing"));
        }
    }

    @Nested
    @DisplayName("entityModels(Entity)")
    class EntityModels {

        @Test
        @DisplayName("Given an entity that is an active mob, when getting models, then returns the mob type in a set")
        void returnsMobType_whenEntityIsActiveMob() {
            Entity entity = mock(Entity.class);
            UUID uuid = UUID.randomUUID();
            when(entity.getUniqueId()).thenReturn(uuid);

            ActiveMob activeMob = mock(ActiveMob.class);
            when(mobManager.getActiveMob(uuid)).thenReturn(Optional.of(activeMob));
            when(activeMob.getMobType()).thenReturn("SkeletonKing");

            Optional<Set<String>> result = hook.entityModels(entity);

            assertTrue(result.isPresent());
            assertEquals(Set.of("SkeletonKing"), result.get());
        }

        @Test
        @DisplayName("Given an entity that is not an active mob, when getting models, then returns empty")
        void returnsEmpty_whenEntityIsNotActiveMob() {
            Entity entity = mock(Entity.class);
            UUID uuid = UUID.randomUUID();
            when(entity.getUniqueId()).thenReturn(uuid);

            when(mobManager.getActiveMob(uuid)).thenReturn(Optional.empty());

            Optional<Set<String>> result = hook.entityModels(entity);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("entityName(CustomEntityWrapper)")
    class EntityName {

        @Test
        @DisplayName("Given a wrapper with a known mythic mob, when getting name, then returns the display name")
        void returnsDisplayName_whenMythicMobExists() {
            CustomEntityWrapper wrapper = mock(CustomEntityWrapper.class);
            when(wrapper.customEntity()).thenReturn(Optional.of("SkeletonKing"));

            MythicMob mythicMob = mock(MythicMob.class);
            PlaceholderString displayName = mock(PlaceholderString.class);
            when(mobManager.getMythicMob("SkeletonKing")).thenReturn(Optional.of(mythicMob));
            when(mythicMob.getDisplayName()).thenReturn(displayName);
            when(displayName.get()).thenReturn("Skeleton King");

            assertEquals("Skeleton King", hook.entityName(wrapper));
        }

        @Test
        @DisplayName("Given a wrapper with an unknown mythic mob, when getting name, then returns the custom entity id")
        void returnsCustomEntityId_whenMythicMobNotFound() {
            CustomEntityWrapper wrapper = mock(CustomEntityWrapper.class);
            when(wrapper.customEntity()).thenReturn(Optional.of("UnknownMob"));

            when(mobManager.getMythicMob("UnknownMob")).thenReturn(Optional.empty());

            assertEquals("UnknownMob", hook.entityName(wrapper));
        }

        @Test
        @DisplayName("Given a wrapper with no custom entity, when getting name, then returns Unknown")
        void returnsUnknown_whenNoCustomEntity() {
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

package com.diamonddagger590.mccore.external.mythicmobs;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.core.mobs.MobExecutor;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreMythicMobsHookTest {

    @Mock
    private CorePlugin plugin;

    @Mock
    private MythicBukkit mythicBukkit;

    @Mock
    private MobExecutor mobManager;

    private MockedStatic<MythicBukkit> mythicBukkitStatic;
    private CoreMythicMobsHook hook;

    @BeforeEach
    void setUp() {
        mythicBukkitStatic = mockStatic(MythicBukkit.class);
        mythicBukkitStatic.when(MythicBukkit::inst).thenReturn(mythicBukkit);
        org.mockito.Mockito.lenient().when(mythicBukkit.getMobManager()).thenReturn(mobManager);
        hook = new CoreMythicMobsHook(plugin);
    }

    @AfterEach
    void tearDown() {
        mythicBukkitStatic.close();
    }

    @Test
    @DisplayName("Given a CoreMythicMobsHook, when constructed, then plugin is accessible")
    void constructor_storesPlugin() {
        assertNotNull(hook.plugin());
    }

    @Nested
    @DisplayName("isCustomEntity(UUID)")
    class IsCustomEntityByUUID {

        @Test
        @DisplayName("Given an active MythicMob UUID, when isCustomEntity is called, then returns true")
        void returnsTrue_whenMobIsActive() {
            UUID uuid = UUID.randomUUID();
            when(mobManager.isActiveMob(uuid)).thenReturn(true);

            assertTrue(hook.isCustomEntity(uuid));
        }

        @Test
        @DisplayName("Given a non-active UUID, when isCustomEntity is called, then returns false")
        void returnsFalse_whenMobIsNotActive() {
            UUID uuid = UUID.randomUUID();
            when(mobManager.isActiveMob(uuid)).thenReturn(false);

            assertFalse(hook.isCustomEntity(uuid));
        }
    }

    @Nested
    @DisplayName("isCustomEntity(String)")
    class IsCustomEntityByName {

        @Test
        @DisplayName("Given a valid MythicMob name, when isCustomEntity is called, then returns true")
        void returnsTrue_whenMobTypeExists() {
            MythicMob mythicMob = mock(MythicMob.class);
            when(mobManager.getMythicMob("SkeletonKing")).thenReturn(Optional.of(mythicMob));

            assertTrue(hook.isCustomEntity("SkeletonKing"));
        }

        @Test
        @DisplayName("Given an invalid MythicMob name, when isCustomEntity is called, then returns false")
        void returnsFalse_whenMobTypeDoesNotExist() {
            when(mobManager.getMythicMob("nonexistent")).thenReturn(Optional.empty());

            assertFalse(hook.isCustomEntity("nonexistent"));
        }
    }

    @Nested
    @DisplayName("isCustomEntityOfType(UUID, String)")
    class IsCustomEntityOfType {

        @Test
        @DisplayName("Given an active mob with matching type, when isCustomEntityOfType is called, then returns true")
        void returnsTrue_whenMobTypeMatches() {
            UUID uuid = UUID.randomUUID();
            ActiveMob activeMob = mock(ActiveMob.class);
            when(activeMob.getMobType()).thenReturn("SkeletonKing");
            when(mobManager.getActiveMob(uuid)).thenReturn(Optional.of(activeMob));

            assertTrue(hook.isCustomEntityOfType(uuid, "skeletonking"));
        }

        @Test
        @DisplayName("Given an active mob with non-matching type, when isCustomEntityOfType is called, then returns false")
        void returnsFalse_whenMobTypeDoesNotMatch() {
            UUID uuid = UUID.randomUUID();
            ActiveMob activeMob = mock(ActiveMob.class);
            when(activeMob.getMobType()).thenReturn("SkeletonKing");
            when(mobManager.getActiveMob(uuid)).thenReturn(Optional.of(activeMob));

            assertFalse(hook.isCustomEntityOfType(uuid, "ZombieLord"));
        }

        @Test
        @DisplayName("Given no active mob for UUID, when isCustomEntityOfType is called, then returns false")
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
        @DisplayName("Given an active mob entity, when entityModels is called, then returns mob type set")
        void returnsModels_whenEntityIsActiveMob() {
            Entity entity = mock(Entity.class);
            UUID uuid = UUID.randomUUID();
            when(entity.getUniqueId()).thenReturn(uuid);

            ActiveMob activeMob = mock(ActiveMob.class);
            when(activeMob.getMobType()).thenReturn("SkeletonKing");
            when(mobManager.getActiveMob(uuid)).thenReturn(Optional.of(activeMob));

            Optional<Set<String>> result = hook.entityModels(entity);
            assertTrue(result.isPresent());
            assertEquals(Set.of("SkeletonKing"), result.get());
        }

        @Test
        @DisplayName("Given a non-active entity, when entityModels is called, then returns empty optional")
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
        @DisplayName("Given a wrapper with custom entity that has a display name, when entityName is called, then returns display name")
        void returnsDisplayName_whenMobTypeFound() {
            CustomEntityWrapper wrapper = mock(CustomEntityWrapper.class);
            when(wrapper.customEntity()).thenReturn(Optional.of("SkeletonKing"));

            MythicMob mythicMob = mock(MythicMob.class);
            PlaceholderString displayName = mock(PlaceholderString.class);
            when(displayName.get()).thenReturn("Skeleton King");
            when(mythicMob.getDisplayName()).thenReturn(displayName);
            when(mobManager.getMythicMob("SkeletonKing")).thenReturn(Optional.of(mythicMob));

            assertEquals("Skeleton King", hook.entityName(wrapper));
        }

        @Test
        @DisplayName("Given a wrapper with custom entity that has no mob type, when entityName is called, then returns raw id")
        void returnsRawId_whenMobTypeNotFound() {
            CustomEntityWrapper wrapper = mock(CustomEntityWrapper.class);
            when(wrapper.customEntity()).thenReturn(Optional.of("UnknownMob"));

            when(mobManager.getMythicMob("UnknownMob")).thenReturn(Optional.empty());

            assertEquals("UnknownMob", hook.entityName(wrapper));
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

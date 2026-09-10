package com.diamonddagger590.mccore.external.citizens;

import com.diamonddagger590.mccore.CorePlugin;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.entity.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreCitizensHookTest {

    @Mock
    private CorePlugin plugin;

    @Test
    @DisplayName("Given a CoreCitizensHook, when constructed, then plugin is accessible")
    void constructor_storesPlugin() {
        CoreCitizensHook hook = new CoreCitizensHook(plugin);
        assertNotNull(hook.plugin());
    }

    @Test
    @DisplayName("Given an NPC entity, when isEntityNpc is called, then returns true")
    void isEntityNpc_returnsTrue_whenEntityIsNpc() {
        CoreCitizensHook hook = new CoreCitizensHook(plugin);
        Entity entity = mock(Entity.class);
        NPCRegistry registry = mock(NPCRegistry.class);
        when(registry.isNPC(entity)).thenReturn(true);

        try (MockedStatic<CitizensAPI> citizensApi = mockStatic(CitizensAPI.class)) {
            citizensApi.when(CitizensAPI::getNPCRegistry).thenReturn(registry);
            assertTrue(hook.isEntityNpc(entity));
        }
    }

    @Test
    @DisplayName("Given a non-NPC entity, when isEntityNpc is called, then returns false")
    void isEntityNpc_returnsFalse_whenEntityIsNotNpc() {
        CoreCitizensHook hook = new CoreCitizensHook(plugin);
        Entity entity = mock(Entity.class);
        NPCRegistry registry = mock(NPCRegistry.class);
        when(registry.isNPC(entity)).thenReturn(false);

        try (MockedStatic<CitizensAPI> citizensApi = mockStatic(CitizensAPI.class)) {
            citizensApi.when(CitizensAPI::getNPCRegistry).thenReturn(registry);
            assertFalse(hook.isEntityNpc(entity));
        }
    }
}

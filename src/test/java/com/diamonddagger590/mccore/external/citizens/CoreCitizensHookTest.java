package com.diamonddagger590.mccore.external.citizens;

import com.diamonddagger590.mccore.CorePlugin;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreCitizensHookTest {

    @Mock
    private CorePlugin plugin;

    private CoreCitizensHook hook;

    @BeforeEach
    void setUp() {
        hook = new CoreCitizensHook(plugin);
    }

    @Test
    @DisplayName("Returns true when entity is an NPC according to Citizens")
    void isEntityNpc_returnsTrue_whenCitizensReportsNpc() {
        Entity entity = mock(Entity.class);

        try (MockedStatic<CitizensAPI> mocked = mockStatic(CitizensAPI.class)) {
            NPCRegistry npcRegistry = mock(NPCRegistry.class);
            mocked.when(CitizensAPI::getNPCRegistry).thenReturn(npcRegistry);
            when(npcRegistry.isNPC(entity)).thenReturn(true);

            assertTrue(hook.isEntityNpc(entity));
        }
    }

    @Test
    @DisplayName("Returns false when entity is not an NPC according to Citizens")
    void isEntityNpc_returnsFalse_whenCitizensReportsNotNpc() {
        Entity entity = mock(Entity.class);

        try (MockedStatic<CitizensAPI> mocked = mockStatic(CitizensAPI.class)) {
            NPCRegistry npcRegistry = mock(NPCRegistry.class);
            mocked.when(CitizensAPI::getNPCRegistry).thenReturn(npcRegistry);
            when(npcRegistry.isNPC(entity)).thenReturn(false);

            assertFalse(hook.isEntityNpc(entity));
        }
    }

    @Test
    @DisplayName("Returns true when entity is a Player and Citizens reports NPC")
    void isEntityNpc_returnsTrue_whenPlayerIsNpc() {
        Player player = mock(Player.class);

        try (MockedStatic<CitizensAPI> mocked = mockStatic(CitizensAPI.class)) {
            NPCRegistry npcRegistry = mock(NPCRegistry.class);
            mocked.when(CitizensAPI::getNPCRegistry).thenReturn(npcRegistry);
            when(npcRegistry.isNPC(player)).thenReturn(true);

            assertTrue(hook.isEntityNpc(player));
        }
    }
}

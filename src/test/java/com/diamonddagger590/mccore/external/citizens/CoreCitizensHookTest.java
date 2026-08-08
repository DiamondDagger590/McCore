package com.diamonddagger590.mccore.external.citizens;

import com.diamonddagger590.mccore.CorePlugin;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPCRegistry;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreCitizensHookTest {

    @Mock
    private CorePlugin mockPlugin;

    private CoreCitizensHook hook;

    @BeforeEach
    void setUp() {
        hook = new CoreCitizensHook(mockPlugin);
    }

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        @DisplayName("Given a valid plugin, when constructed, then plugin is accessible")
        void constructor_setsPlugin_whenCreated() {
            assertNotNull(hook.plugin());
        }
    }

    @Nested
    @DisplayName("isEntityNpc")
    class IsEntityNpc {

        private MockedStatic<CitizensAPI> citizensApiStatic;

        @BeforeEach
        void setUp() {
            citizensApiStatic = mockStatic(CitizensAPI.class);
        }

        @AfterEach
        void tearDown() {
            citizensApiStatic.close();
        }

        @Test
        @DisplayName("Given an entity that is an NPC, when isEntityNpc is called, then returns true")
        void isEntityNpc_returnsTrue_whenEntityIsNpc() {
            Entity entity = mock(Entity.class);
            NPCRegistry npcRegistry = mock(NPCRegistry.class);
            citizensApiStatic.when(CitizensAPI::getNPCRegistry).thenReturn(npcRegistry);
            when(npcRegistry.isNPC(entity)).thenReturn(true);

            assertTrue(hook.isEntityNpc(entity));
            verify(npcRegistry).isNPC(entity);
        }

        @Test
        @DisplayName("Given an entity that is not an NPC, when isEntityNpc is called, then returns false")
        void isEntityNpc_returnsFalse_whenEntityIsNotNpc() {
            Entity entity = mock(Entity.class);
            NPCRegistry npcRegistry = mock(NPCRegistry.class);
            citizensApiStatic.when(CitizensAPI::getNPCRegistry).thenReturn(npcRegistry);
            when(npcRegistry.isNPC(entity)).thenReturn(false);

            assertFalse(hook.isEntityNpc(entity));
            verify(npcRegistry).isNPC(entity);
        }
    }
}

package com.diamonddagger590.mccore.listener;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.chat.ChatResponse;
import com.diamonddagger590.mccore.chat.ChatResponseManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKeyImpl;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatResponseListenerTest {

    private ChatResponseListener listener;

    @Mock
    private CorePlugin mockPlugin;

    @Mock
    private ChatResponseManager mockChatResponseManager;

    @Mock
    private Player mockPlayer;

    @Mock
    private PlayerChatEvent mockEvent;

    private MockedStatic<CorePlugin> corePluginStatic;
    private UUID playerUUID;

    @BeforeEach
    void setUp() {
        listener = new ChatResponseListener();
        playerUUID = UUID.randomUUID();

        RegistryResetExtension.setupRegistry();
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockChatResponseManager);

        corePluginStatic = mockStatic(CorePlugin.class);
        corePluginStatic.when(CorePlugin::getInstance).thenReturn(mockPlugin);
        when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());

        when(mockEvent.getPlayer()).thenReturn(mockPlayer);
        when(mockPlayer.getUniqueId()).thenReturn(playerUUID);
    }

    @AfterEach
    void tearDown() {
        corePluginStatic.close();
        RegistryResetExtension.resetRegistry();
    }

    @Test
    @DisplayName("Given no pending response for player, when chat event fires, then event is not cancelled")
    void onChat_doesNotCancel_whenNoPendingResponse() {
        when(mockChatResponseManager.getPendingResponse(playerUUID)).thenReturn(Optional.empty());

        listener.onChat(mockEvent);

        verify(mockEvent, never()).setCancelled(true);
    }

    @Test
    @DisplayName("Given a pending response for player, when chat event fires, then response is consumed and event cancelled")
    void onChat_consumesResponseAndCancels_whenPendingResponseExists() {
        ChatResponse mockResponse = mock(ChatResponse.class);
        when(mockChatResponseManager.getPendingResponse(playerUUID)).thenReturn(Optional.of(mockResponse));

        listener.onChat(mockEvent);

        verify(mockResponse).onResponse(mockEvent);
        verify(mockChatResponseManager).removePendingResponse(mockResponse);
        verify(mockEvent).setCancelled(true);
    }

    @Test
    @DisplayName("Given a pending response, when chat event fires, then onResponse is called before removePendingResponse")
    void onChat_callsOnResponseBeforeRemove_whenPendingResponseExists() {
        ChatResponse mockResponse = mock(ChatResponse.class);
        when(mockChatResponseManager.getPendingResponse(playerUUID)).thenReturn(Optional.of(mockResponse));

        listener.onChat(mockEvent);

        var inOrder = org.mockito.Mockito.inOrder(mockResponse, mockChatResponseManager, mockEvent);
        inOrder.verify(mockResponse).onResponse(mockEvent);
        inOrder.verify(mockChatResponseManager).removePendingResponse(mockResponse);
        inOrder.verify(mockEvent).setCancelled(true);
    }

    @Test
    @DisplayName("Given no pending response, when chat event fires, then removePendingResponse is never called")
    void onChat_doesNotRemoveResponse_whenNoPendingResponse() {
        when(mockChatResponseManager.getPendingResponse(playerUUID)).thenReturn(Optional.empty());

        listener.onChat(mockEvent);

        verify(mockChatResponseManager, never()).removePendingResponse(org.mockito.ArgumentMatchers.any(ChatResponse.class));
    }
}

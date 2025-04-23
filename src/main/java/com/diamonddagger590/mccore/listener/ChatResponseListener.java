package com.diamonddagger590.mccore.listener;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.chat.ChatResponseManager;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

/**
 * This listener handles responding to {@link com.diamonddagger590.mccore.chat.ChatResponse}s using the
 * {@link PlayerChatEvent}.
 */
public class ChatResponseListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(PlayerChatEvent event) {
        ChatResponseManager chatResponseManager = CorePlugin.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.CHAT_RESPONSE);
        chatResponseManager.getPendingResponse(event.getPlayer().getUniqueId()).ifPresent(chatResponse -> {
            chatResponse.onResponse(event);
            chatResponseManager.removePendingResponse(chatResponse);
            event.setCancelled(true);
        });
    }
}

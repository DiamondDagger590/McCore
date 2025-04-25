package com.diamonddagger590.mccore.registry.manager;

import com.diamonddagger590.mccore.chat.ChatResponseManager;
import com.diamonddagger590.mccore.command.CoreCommandManager;
import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.database.driver.DriverManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.jetbrains.annotations.NotNull;

import static com.diamonddagger590.mccore.registry.manager.ManagerKeyImpl.create;

/**
 * A key that allows access to a {@link Manager} through the {@link ManagerRegistry}.
 * <p>
 * To access the hook, users will need to call {@link RegistryAccess#registryAccess()} and provide
 * {@link com.diamonddagger590.mccore.registry.RegistryKey#MANAGER} to get back the {@link ManagerRegistry}.
 * <p>
 * From there, users can call {@link ManagerRegistry#manager(ManagerKey)} to get the manager belonging to the
 * provided key.
 *
 * @param <M> The {@link Manager} being represented by this key.
 */
public interface ManagerKey<M> {

    ManagerKey<CoreCommandManager> COMMAND = create(CoreCommandManager.class);
    ManagerKey<DriverManager> DRIVER = create(DriverManager.class);
    ManagerKey<ReloadableContentManager> RELOADABLE_CONTENT = create(ReloadableContentManager.class);
    ManagerKey<ChatResponseManager> CHAT_RESPONSE = create(ChatResponseManager.class);

    /**
     * Gets the {@link Class} of the {@link Manager} represented by this key.
     *
     * @return The {@link Class} of the {@link Manager} represented by this key.
     */
    @NotNull
    Class<M> managerClass();
}

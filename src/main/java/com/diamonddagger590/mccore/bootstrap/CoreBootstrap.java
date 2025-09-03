package com.diamonddagger590.mccore.bootstrap;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.bootstrap.registrar.CommandRegistrar;
import com.diamonddagger590.mccore.bootstrap.registrar.HooksRegistrar;
import com.diamonddagger590.mccore.bootstrap.registrar.ListenerRegistrar;
import com.diamonddagger590.mccore.chat.ChatResponseManager;
import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.database.driver.DriverRegistry;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.CoreManagerKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry;
import com.diamonddagger590.mccore.setting.PlayerSettingRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * A bootstrap is used to load features and functionality of a given plugin on plugin enablement
 * depending on the {@link StartupProfile} being used. This allows plugins to only load common/core functionality
 * that is likely to be unit test agnostic as MockBukkit loads an actual plugin instance PER unit test,
 * and there are some things that can't be initialized in that environment.
 * <p>
 * One possible implementation while testing is to have a test-specific bootstrap and use static mocking
 * along with a static bootstrap factory to override what bootstrap is being provided at runtime.
 * It's likely, however, that most plugins will instead adopt a simpler approach and just use one
 * bootstrap that loads a minimal set of features based on the profile.
 *
 * @param <P>
 */
public abstract class CoreBootstrap<P extends CorePlugin> {

    private final P plugin;

    public CoreBootstrap(@NotNull P plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public P getPlugin() {
        return plugin;
    }

    /**
     * Starts this bootstrap's features based off the provided {@link StartupProfile}.
     *
     * @param startupProfile The profile containing the type of runtime this bootstrap is loading in.
     */
    public void start(@NotNull StartupProfile startupProfile) {
        BootstrapContext<P> bootstrapContext = new BootstrapContext<>(plugin, startupProfile);
        RegistryAccess registryAccess = plugin.registryAccess();
        registryAccess.register(new ManagerRegistry());
        registryAccess.register(new PluginHookRegistry());
        registryAccess.register(new PlayerSettingRegistry());
        registryAccess.registry(RegistryKey.MANAGER).register(new ReloadableContentManager(plugin));
        registryAccess.registry(RegistryKey.MANAGER).register(new ChatResponseManager(plugin));

        new HooksRegistrar<P>().register(bootstrapContext);
        new ListenerRegistrar<P>().register(bootstrapContext);
        if (startupProfile == StartupProfile.PROD) {
            registryAccess.register(new DriverRegistry());
            new CommandRegistrar<P>().register(bootstrapContext);
        }
    }

    /**
     * Stops this bootstrap's features based off the provided {@link StartupProfile}.
     *
     * @param startupProfile The profile containing the type of runtime this bootstrap is unloading in.
     */
    public void stop(@NotNull StartupProfile startupProfile) {
        ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
        if (managerRegistry.registered(CoreManagerKey.CORE_DATABASE_MANAGER)) {
            managerRegistry.manager(CoreManagerKey.CORE_DATABASE_MANAGER).getDatabase().shutdown();
        }
    }
}

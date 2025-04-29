package com.diamonddagger590.mccore;

import com.diamonddagger590.mccore.builder.item.ItemPluginType;
import com.diamonddagger590.mccore.chat.ChatResponseManager;
import com.diamonddagger590.mccore.command.CoreCommandManager;
import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.driver.DriverManager;
import com.diamonddagger590.mccore.external.headdatabase.CoreHeadDatabaseHook;
import com.diamonddagger590.mccore.external.itemsadder.CoreItemsAdderHook;
import com.diamonddagger590.mccore.external.modelengine.CoreModelEngineHook;
import com.diamonddagger590.mccore.external.mythicmobs.CoreMythicMobsHook;
import com.diamonddagger590.mccore.external.nexo.CoreNexoHook;
import com.diamonddagger590.mccore.external.papi.CorePapiHook;
import com.diamonddagger590.mccore.listener.ChatResponseListener;
import com.diamonddagger590.mccore.listener.GuiCloseListener;
import com.diamonddagger590.mccore.listener.GuiRefreshListener;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry;
import com.diamonddagger590.mccore.setting.PlayerSettingRegistry;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.jetbrains.annotations.NotNull;

/**
 * The abstract version of a plugin that provides some common logic for plugins
 * to use.
 */
public abstract class CorePlugin extends JavaPlugin {

    private static CorePlugin instance;

    private RegistryAccess registryAccess;

    private AnnotationParser<CommandSender> annotationParser;
    private BukkitAudiences adventure;
    private MiniMessage miniMessage;

    protected PlayerSettingRegistry playerSettingRegistry;

    @Override
    public void onEnable() {
        instance = this;
        registryAccess = RegistryAccess.registryAccess();
        registryAccess.register(new ManagerRegistry());
        registryAccess.register(new PluginHookRegistry());

        adventure = BukkitAudiences.create(this);
        miniMessage = MiniMessage.miniMessage();
        registryAccess.registry(RegistryKey.MANAGER).register(new DriverManager(this));
        registerDrivers();
        registryAccess.register(new PlayerSettingRegistry());
        registryAccess.registry(RegistryKey.MANAGER).register(new ReloadableContentManager(this));
        registryAccess.registry(RegistryKey.MANAGER).register(new ChatResponseManager(this));

        // We can't setup cloud when mocking so ignore if we are in unit test mode
        if (!isUnitTest()) {
            registryAccess.registry(RegistryKey.MANAGER).register(new CoreCommandManager(this));
        }

        setupHooks();
    }

    @Override
    public void onDisable() {
        adventure.close();
        if (!isUnitTest()) {
            getDatabase().shutdown();
        }
    }

    /**
     * Initializes the databases for the plugin.
     * <p>
     * It is up to the plugin implementing this on the {@link #onEnable()} method
     */
    public void initializeDatabase() {
    }

    /**
     * Constructs commands for plugins
     */
    protected void constructCommands() {
    }

    /**
     * Registers listeners for plugins
     */
    protected void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new GuiCloseListener(), this);
        Bukkit.getPluginManager().registerEvents(new GuiRefreshListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChatResponseListener(), this);
    }

    /**
     * Registers the database drivers for plugins.
     */
    protected void registerDrivers() {
    }

    /**
     * Sets up external plugin hooks for plugins.
     */
    protected void setupHooks() {
        if (Bukkit.getPluginManager().isPluginEnabled("Nexo")) {
            getLogger().info("Nexo found... registering hooks for core");
            registryAccess.registry(RegistryKey.PLUGIN_HOOK).register(new CoreNexoHook(this));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
            getLogger().info("ItemsAdder found... registering hook for core");
            registryAccess.registry(RegistryKey.PLUGIN_HOOK).register(new CoreItemsAdderHook(this));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("HeadDatabase")) {
            getLogger().info("HeadDatabase found... registering hooks for core");
            registryAccess.registry(RegistryKey.PLUGIN_HOOK).register(new CoreHeadDatabaseHook(this));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().info("PlaceholderAPI found... registering placeholders translation support for core");
            registryAccess.registry(RegistryKey.PLUGIN_HOOK).register(new CorePapiHook(this));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("ModelEngine")) {
            getLogger().info("ModelEngine found... registering placeholders translation support for core");
            registryAccess.registry(RegistryKey.PLUGIN_HOOK).register(new CoreModelEngineHook(this));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            getLogger().info("MythicMobs found... registering placeholders translation support for core");
            registryAccess.registry(RegistryKey.PLUGIN_HOOK).register(new CoreMythicMobsHook(this));
        }
    }

    /**
     * Gets the {@link ItemPluginType} currently being supported.
     *
     * @return The {@link ItemPluginType} currently being supported.
     */
    @NotNull
    public ItemPluginType getItemPlugin() {
        return ItemPluginType.NONE;
    }

    /**
     * Gets the {@link Database} instance for this plugin.
     *
     * @return The {@link Database} instance for this plugin.
     */
    @NotNull
    public abstract Database getDatabase();

    /**
     * Gets the {@link AnnotationParser} used by this plugin.
     *
     * @return The {@link AnnotationParser} used by this plugin.
     */
    @NotNull
    public final AnnotationParser<CommandSender> getAnnotationParser() {
        return annotationParser;
    }

    /**
     * Gets the {@link BukkitAudiences} used by {@link net.kyori.adventure.Adventure}
     *
     * @return The {@link BukkitAudiences} used by {@link net.kyori.adventure.Adventure}
     */
    @NotNull
    public final BukkitAudiences getAdventure() {
        return adventure;
    }

    /**
     * Gets the centralized {@link MiniMessage} for deserializing chat messages
     *
     * @return The centralized {@link MiniMessage} for deserializing chat messages
     */
    @NotNull
    public final MiniMessage getMiniMessage() {
        return miniMessage;
    }

    /**
     * Gets the {@link RegistryAccess} instance used by this plugin.
     *
     * @return The {@link RegistryAccess} instance used by this plugin.
     */
    @NotNull
    public final RegistryAccess registryAccess() {
        return registryAccess;
    }

    /**
     * Checks to see if we are running in unit test mode
     *
     * @return {@code true} if we are running in unit test mode
     */
    @Deprecated(forRemoval = true, since = "1.0.0.13-SNAPSHOT")
    public boolean isUnitTest() {
        return (getClassLoader().getClass().getPackageName().startsWith("be.seeseemelk.mockbukkit"));
    }

    /**
     * Gets the instance of this plugin or throws a {@link NullPointerException} if not initialized.
     *
     * @return The instance of this plugin.
     */
    @NotNull
    public static CorePlugin getInstance() {
        if (instance == null) {
            throw new NullPointerException("Plugin was not initialized.");
        }
        return instance;
    }
}

package com.diamonddagger590.mccore;

import com.diamonddagger590.mccore.builder.item.ItemPluginType;
import com.diamonddagger590.mccore.chat.ChatResponseManager;
import com.diamonddagger590.mccore.command.CoreCommandManager;
import com.diamonddagger590.mccore.configuration.ReloadableContentRegistry;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.driver.DriverManager;
import com.diamonddagger590.mccore.external.headdatabase.HeadDatabaseHook;
import com.diamonddagger590.mccore.external.itemsadder.ItemsAdderHook;
import com.diamonddagger590.mccore.external.nexo.NexoHook;
import com.diamonddagger590.mccore.external.papi.PapiHook;
import com.diamonddagger590.mccore.gui.GuiTracker;
import com.diamonddagger590.mccore.listener.ChatResponseListener;
import com.diamonddagger590.mccore.listener.GuiCloseListener;
import com.diamonddagger590.mccore.listener.GuiRefreshListener;
import com.diamonddagger590.mccore.player.PlayerManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * The abstract version of a plugin that provides some common logic for plugins
 * to use.
 */
public abstract class CorePlugin extends JavaPlugin {

    private static CorePlugin instance;

    private CoreCommandManager commandManager;
    private AnnotationParser<CommandSender> annotationParser;
    private BukkitAudiences adventure;
    private MiniMessage miniMessage;

    protected DriverManager driverManager;
    protected PlayerManager playerManager;
    protected GuiTracker guiTracker;
    protected ReloadableContentRegistry reloadableContentRegistry;
    protected ChatResponseManager chatResponseManager;

    @Nullable
    private PapiHook papiHook;
    @Nullable
    private ItemsAdderHook itemsAdderHook;
    @Nullable
    private NexoHook nexoHook;
    @Nullable
    private HeadDatabaseHook headDatabaseHook;

    @Override
    public void onEnable() {
        instance = this;
        adventure = BukkitAudiences.create(this);
        miniMessage = MiniMessage.miniMessage();
        driverManager = new DriverManager(this);
        registerDrivers();
        guiTracker = new GuiTracker(this);
        reloadableContentRegistry = new ReloadableContentRegistry();
        chatResponseManager = new ChatResponseManager(this);

        // We can't setup cloud when mocking so ignore if we are in unit test mode
        if (!isUnitTest()) {
            commandManager = new CoreCommandManager(this);
        }
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
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().info("Papi PlaceholderAPI found... registering hooks");
            papiHook = new PapiHook(this);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Nexo")) {
            getLogger().info("Nexo found... registering hooks");
            nexoHook = new NexoHook(this);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
            getLogger().info("ItemsAdder found... registering hooks");
            itemsAdderHook = new ItemsAdderHook(this);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("HeadDatabase")) {
            getLogger().info("HeadDatabase found... registering hooks");
            headDatabaseHook = new HeadDatabaseHook(this);
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

    @NotNull
    public abstract Database getDatabase();

    /**
     * Gets the {@link DriverManager} used by the plugin.
     *
     * @return The {@link DriverManager} used by the plugin.
     */
    @NotNull
    public final DriverManager getDriverManager() {
        return driverManager;
    }

    /**
     * Gets the {@link PlayerManager} that stores all the plugin's {@link com.diamonddagger590.mccore.player.CorePlayer}
     * objects.
     *
     * @return The {@link PlayerManager} that stores all the plugin's {@link com.diamonddagger590.mccore.player.CorePlayer}
     * objects.
     */
    @NotNull
    public final PlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * Gets the {@link GuiTracker} that tracks all {@link com.diamonddagger590.mccore.gui.Gui}s
     *
     * @return The {@link GuiTracker} that tracks all {@link com.diamonddagger590.mccore.gui.Gui}s
     */
    @NotNull
    public final GuiTracker getGuiTracker() {
        return guiTracker;
    }

    /**
     * Gets the {@link ChatResponseManager} that manages any responses needed for chat messages.
     *
     * @return The {@link ChatResponseManager} that manages any responses needed for chat messages.
     */
    @NotNull
    public final ChatResponseManager getChatResponseManager() {
        return chatResponseManager;
    }

    /**
     * Gets the {@link ReloadableContentRegistry} used to manage all {@link com.diamonddagger590.mccore.configuration.ReloadableContent}.
     *
     * @return The {@link ReloadableContentRegistry} used to manage all {@link com.diamonddagger590.mccore.configuration.ReloadableContent}.¬
     */
    @NotNull
    public final ReloadableContentRegistry getReloadableContentRegistry() {
        return reloadableContentRegistry;
    }

    /**
     * Gets the {@link CommandManager} used by this plugin.
     *
     * @return The {@link CommandManager} used by this plugin.
     */
    @NotNull
    public final CoreCommandManager getCommandManager() {
        return commandManager;
    }

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
     * Gets the {@link PapiHook} this plugin uses to support PlaceholderAPI.
     *
     * @return An {@link Optional} containing the {@link PapiHook} this plugin uses to support
     * <a href="https://www.spigotmc.org/resources/placeholderapi.6245/">PlaceholderAPI</a> if the plugin is running.
     */
    @NotNull
    public Optional<PapiHook> getPapiHook() {
        return Optional.ofNullable(papiHook);
    }

    /**
     * Gets the {@link NexoHook} this plugin uses to support Nexo.
     *
     * @return An {@link Optional} containing the {@link NexoHook} this plugin uses to support
     * <a href="https://polymart.org/resource/nexo.6901">Nexo</a> if the plugin is running.
     */
    @NotNull
    public Optional<NexoHook> getNexoHook() {
        return Optional.ofNullable(nexoHook);
    }

    /**
     * Gets the {@link ItemsAdderHook} this plugin uses to support ItemsAdder.
     *
     * @return An {@link Optional} containing the {@link ItemsAdderHook} this plugin uses to support
     * <a href="https://www.spigotmc.org/resources/%E2%9C%A8itemsadder%E2%AD%90emotes-mobs-items-armors-hud-gui-emojis-blocks-wings-hats-liquids.73355/">ItemsAdder</a>
     * if the plugin is running.
     */
    @NotNull
    public Optional<ItemsAdderHook> getItemsAdderHook() {
        return Optional.ofNullable(itemsAdderHook);
    }

    /**
     * Gets the {@link HeadDatabaseHook} this plugin uses to support HeadDatabase.
     *
     * @return An {@link Optional} containing the {@link HeadDatabaseHook} this plugin uses to support
     * <a href="https://www.spigotmc.org/resources/head-database.14280/">HeadDatabase</a> if the plugin is running.
     */
    @NotNull
    public Optional<HeadDatabaseHook> getHeadDatabaseHook() {
        return Optional.ofNullable(headDatabaseHook);
    }

    /**
     * Checks to see if we are running in unit test mode
     *
     * @return {@code true} if we are running in unit test mode
     */
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

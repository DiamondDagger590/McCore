package com.diamonddagger590.mccore;

import com.diamonddagger590.mccore.command.CoreCommandManager;
import com.diamonddagger590.mccore.configuration.ReloadableContentRegistry;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.driver.DriverManager;
import com.diamonddagger590.mccore.gui.GuiTracker;
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

    @Override
    public void onEnable() {
        instance = this;
        adventure = BukkitAudiences.create(this);
        miniMessage = MiniMessage.miniMessage();
        driverManager = new DriverManager(this);
        registerDrivers();
        guiTracker = new GuiTracker(this);
        reloadableContentRegistry = new ReloadableContentRegistry();

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
    }

    protected void registerDrivers() {
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

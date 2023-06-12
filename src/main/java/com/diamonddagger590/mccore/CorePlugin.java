package com.diamonddagger590.mccore;

import cloud.commandframework.CommandTree;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import com.diamonddagger590.mccore.database.DatabaseManager;
import com.diamonddagger590.mccore.player.PlayerManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.logging.Level;

/**
 * The abstract version of a plugin that provides some common logic for plugins
 * to use.
 */
public abstract class CorePlugin extends JavaPlugin {

    private static CorePlugin instance;

    private BukkitCommandManager<CommandSender> bukkitCommandManager;
    private AnnotationParser<CommandSender> annotationParser;
    private BukkitAudiences adventure;
    private MiniMessage miniMessage;

    protected DatabaseManager databaseManager;
    protected PlayerManager playerManager;

    @Override
    public void onEnable() {
        instance = this;
        adventure = BukkitAudiences.create(this);
        miniMessage = MiniMessage.miniMessage();
        setupCloud();
    }

    @Override
    public void onDisable() {
        databaseManager.getDatabaseExecutorService().shutdown();
        adventure.close();
    }

    private void setupCloud() {
        Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction = CommandExecutionCoordinator.simpleCoordinator();
        Function<CommandSender, CommandSender> mapperFunction = Function.identity();

        try {
            bukkitCommandManager = new BukkitCommandManager<>(this, executionCoordinatorFunction, mapperFunction, mapperFunction);
            bukkitCommandManager.createCommandHelpHandler();
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Failed to initialize command manager");
        }

        final Function<ParserParameters, CommandMeta> commandMetaFunction =
                p -> CommandMeta.simple()
                        .with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description")).build();

        annotationParser = new AnnotationParser<CommandSender>(bukkitCommandManager, CommandSender.class, commandMetaFunction);
    }

    /**
     * Initializes the databases for the plugin.
     * <p>
     * It is up to the plugin implementing this on the {@link #onEnable()} method
     */
    public abstract void initializeDatabase();

    /**
     * Constructs commands for plugins
     */
    protected void constructCommands() {
    }

    /**
     * Get the {@link DatabaseManager} used by the plugin
     *
     * @return The {@link DatabaseManager} used by the plugin
     */
    @NotNull
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Gets the {@link PlayerManager} that stores all the plugin's {@link com.diamonddagger590.mccore.player.CorePlayer}
     * objects.
     *
     * @return The {@link PlayerManager} that stores all the plugin's {@link com.diamonddagger590.mccore.player.CorePlayer}
     * objects.
     */
    @NotNull
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * Gets
     * the {@link BukkitCommandManager} used by this plugin.
     *
     * @return The {@link BukkitCommandManager} used by this plugin.
     */
    @NotNull
    public BukkitCommandManager<CommandSender> getBukkitCommandManager() {
        return bukkitCommandManager;
    }

    /**
     * Gets the {@link AnnotationParser} used by this plugin.
     *
     * @return The {@link AnnotationParser} used by this plugin.
     */
    @NotNull
    public AnnotationParser<CommandSender> getAnnotationParser() {
        return annotationParser;
    }

    /**
     * Gets the {@link BukkitAudiences} used by {@link net.kyori.adventure.Adventure}
     *
     * @return The {@link BukkitAudiences} used by {@link net.kyori.adventure.Adventure}
     */
    @NotNull
    public BukkitAudiences getAdventure() {
        return adventure;
    }

    /**
     * Gets the centralized {@link MiniMessage} for deserializing chat messages
     *
     * @return The centralized {@link MiniMessage} for deserializing chat messages
     */
    @NotNull
    public MiniMessage getMiniMessage() {
        return miniMessage;
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

package com.diamonddagger590.mccore.command;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.manager.Manager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.processors.confirmation.ConfirmationManager;
import org.jetbrains.annotations.NotNull;

/**
 * The core implementation of a command manager which wraps
 * <a href=https://cloud.incendo.org/>Cloud</a>'s API functionality.
 */
public final class CoreCommandManager extends Manager<CorePlugin> {

    private final PaperCommandManager<CommandSourceStack> commandManager;
    private ConfirmationManager<CommandSourceStack> confirmationManager;
    private final AnnotationParser<CommandSender> annotationParser;

    public CoreCommandManager(@NotNull CorePlugin corePlugin) {
        super(corePlugin);
        commandManager = PaperCommandManager.builder()
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(corePlugin);
        annotationParser = new AnnotationParser(commandManager, CommandSender.class);
    }

    /**
     * Gets the {@link PaperCommandManager} to be used for command construction.
     *
     * @return The {@link PaperCommandManager} to be used for command construction.
     */
    @NotNull
    public PaperCommandManager<CommandSourceStack> getCommandManager() {
        return commandManager;
    }

    /**
     * Gets the {@link AnnotationParser} to be used in commands.
     *
     * @return The {@link AnnotationParser} to be used in commands.
     */
    @NotNull
    public AnnotationParser<CommandSender> getAnnotationParser() {
        return annotationParser;
    }

    /**
     * Registers the provided {@link ConfirmationManager} to this manager. Without calling this method,
     * confirmation commands will not be supported.
     *
     * @param confirmationManager The manager to register.
     * @throws IllegalStateException If a confirmation manager has already been registered.
     */
    public void registerConfirmationCommand(@NotNull ConfirmationManager<CommandSourceStack> confirmationManager) {
        if (this.confirmationManager != null) {
            throw new IllegalStateException("ConfirmationManager already registered.");
        }
        this.confirmationManager = confirmationManager;
    }

    /**
     * Gets the {@link ConfirmationManager} instance from this manager.
     *
     * @return The {@link ConfirmationManager} instance from this manager.
     * @throws IllegalStateException If there is no confirmation manager registered.
     *                               See {@link #registerConfirmationCommand(ConfirmationManager)}.
     */
    @NotNull
    public ConfirmationManager<CommandSourceStack> getConfirmationManager() {
        if (confirmationManager == null) {
            throw new IllegalStateException("ConfirmationManager was not registered, so confirmation commands are unsupported");
        }
        return confirmationManager;
    }
}

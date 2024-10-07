package com.diamonddagger590.mccore.command;

import com.diamonddagger590.mccore.CorePlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.jetbrains.annotations.NotNull;

public final class CoreCommandManager {

    private final PaperCommandManager<CommandSourceStack> commandManager;
    private final AnnotationParser<CommandSender> annotationParser;

    public CoreCommandManager(@NotNull CorePlugin corePlugin) {
        commandManager = PaperCommandManager.builder()
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(corePlugin);

        annotationParser = new AnnotationParser(commandManager, CommandSender.class);
    }

    @NotNull
    public PaperCommandManager<CommandSourceStack> getCommandManager() {
        return commandManager;
    }

    @NotNull
    public AnnotationParser<CommandSender> getAnnotationParser() {
        return annotationParser;
    }
}

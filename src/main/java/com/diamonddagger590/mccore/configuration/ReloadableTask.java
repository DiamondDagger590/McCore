package com.diamonddagger590.mccore.configuration;

import com.diamonddagger590.mccore.task.core.CancellableCoreTask;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/**
 * A Reloadable Task represents an intersection of {@link CancellableCoreTask}s and {@link ReloadableContent}.
 * <p>
 * This object wraps a task inside reloadable content which allows for tasks to be automatically canceled and started
 * again whenever {@link ReloadableContent#reloadContent()} is called.
 * <p>
 * The task inside of this object may not be at all equal to one that was retrieved earlier so this object should
 * be read from each time the desired task is needed to be used.
 *
 * @param <T> The {@link CancellableCoreTask} being stored inside this {@link ReloadableContent}.
 */
public class ReloadableTask<T extends CancellableCoreTask> extends ReloadableContent<T> {

    private final boolean async;

    public ReloadableTask(@NotNull YamlDocument yamlDocument, @NotNull Route route, @NotNull BiFunction<YamlDocument, Route, T> reloadCallback, boolean async) {
        super(yamlDocument, route, reloadCallback);
        this.async = async;
    }

    @Override
    public void reloadContent() {
        // Cancel the task, reload and create a new one and then run the new task
        content.cancelTask();
        super.reloadContent();
        content.runTask(async);
    }
}

package com.diamonddagger590.mccore.configuration.task;

import com.diamonddagger590.mccore.task.core.CancelableCoreTask;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ReloadableTaskTest {

    private YamlDocument yamlDocument;
    private Route route;

    @BeforeEach
    void setUp() {
        yamlDocument = mock(YamlDocument.class);
        route = Route.from("test", "route");
    }

    @Test
    @DisplayName("Given a ReloadableTask, when first constructed, then content is loaded via callback")
    void constructor_loadsContentViaCallback() {
        CancelableCoreTask task = mock(CancelableCoreTask.class);
        BiFunction<YamlDocument, Route, CancelableCoreTask> callback = (doc, r) -> task;

        ReloadableTask<CancelableCoreTask> reloadableTask = new ReloadableTask<>(yamlDocument, route, callback, false);

        assertNotNull(reloadableTask.getContent());
        assertSame(task, reloadableTask.getContent());
    }

    @Test
    @DisplayName("Given a ReloadableTask with async=true, when reloadContent is called, then new task runs async")
    void reloadContent_runsTaskAsync_whenAsyncIsTrue() {
        CancelableCoreTask initialTask = mock(CancelableCoreTask.class);
        CancelableCoreTask reloadedTask = mock(CancelableCoreTask.class);
        AtomicBoolean firstCall = new AtomicBoolean(true);
        BiFunction<YamlDocument, Route, CancelableCoreTask> callback = (doc, r) -> {
            if (firstCall.getAndSet(false)) {
                return initialTask;
            }
            return reloadedTask;
        };

        ReloadableTask<CancelableCoreTask> reloadableTask = new ReloadableTask<>(yamlDocument, route, callback, true);
        reloadableTask.reloadContent();

        verify(initialTask).cancelTask();
        verify(reloadedTask).runTask(true);
        assertSame(reloadedTask, reloadableTask.getContent());
    }

    @Test
    @DisplayName("Given a ReloadableTask with async=false, when reloadContent is called, then new task runs sync")
    void reloadContent_runsTaskSync_whenAsyncIsFalse() {
        CancelableCoreTask initialTask = mock(CancelableCoreTask.class);
        CancelableCoreTask reloadedTask = mock(CancelableCoreTask.class);
        AtomicBoolean firstCall = new AtomicBoolean(true);
        BiFunction<YamlDocument, Route, CancelableCoreTask> callback = (doc, r) -> {
            if (firstCall.getAndSet(false)) {
                return initialTask;
            }
            return reloadedTask;
        };

        ReloadableTask<CancelableCoreTask> reloadableTask = new ReloadableTask<>(yamlDocument, route, callback, false);
        reloadableTask.reloadContent();

        verify(initialTask).cancelTask();
        verify(reloadedTask).runTask(false);
    }

    @Test
    @DisplayName("Given a ReloadableTask, when reloadContent called multiple times, then each previous task is cancelled")
    void reloadContent_cancelsEachPreviousTask() {
        CancelableCoreTask task1 = mock(CancelableCoreTask.class);
        CancelableCoreTask task2 = mock(CancelableCoreTask.class);
        CancelableCoreTask task3 = mock(CancelableCoreTask.class);
        AtomicInteger callCount = new AtomicInteger(0);
        BiFunction<YamlDocument, Route, CancelableCoreTask> callback = (doc, r) -> {
            int count = callCount.getAndIncrement();
            return switch (count) {
                case 0 -> task1;
                case 1 -> task2;
                default -> task3;
            };
        };

        ReloadableTask<CancelableCoreTask> reloadableTask = new ReloadableTask<>(yamlDocument, route, callback, false);

        reloadableTask.reloadContent();
        verify(task1).cancelTask();
        verify(task2).runTask(false);

        reloadableTask.reloadContent();
        verify(task2).cancelTask();
        verify(task3).runTask(false);

        assertSame(task3, reloadableTask.getContent());
    }

    @Test
    @DisplayName("Given a ReloadableTask on first construction, when callback returns initial task, then cancelTask is not called on it")
    void constructor_doesNotCancelInitialTask() {
        CancelableCoreTask initialTask = mock(CancelableCoreTask.class);
        BiFunction<YamlDocument, Route, CancelableCoreTask> callback = (doc, r) -> initialTask;

        new ReloadableTask<>(yamlDocument, route, callback, false);

        verify(initialTask, never()).cancelTask();
    }
}

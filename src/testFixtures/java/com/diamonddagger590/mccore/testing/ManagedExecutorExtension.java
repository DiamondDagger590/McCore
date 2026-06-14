package com.diamonddagger590.mccore.testing;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A JUnit 5 extension that provides a managed single-thread {@link ExecutorService}
 * for cross-thread tests. The executor is created before each test and shut down
 * with {@link ExecutorService#awaitTermination(long, TimeUnit)} after each test.
 *
 * <p>Usage with {@code @RegisterExtension}:</p>
 * <pre>{@code
 * @RegisterExtension
 * ManagedExecutorExtension executorExtension = new ManagedExecutorExtension();
 *
 * @Test
 * void myTest() {
 *     ExecutorService executor = executorExtension.getExecutor();
 *     executor.submit(() -> { ... });
 * }
 * }</pre>
 */
public class ManagedExecutorExtension implements BeforeEachCallback, AfterEachCallback {

    private static final long TERMINATION_TIMEOUT_SECONDS = 5;

    private ExecutorService executor;

    @Override
    public void beforeEach(@NotNull ExtensionContext context) {
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void afterEach(@NotNull ExtensionContext context) {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Gets the managed {@link ExecutorService} for the current test.
     *
     * @return The managed {@link ExecutorService}.
     */
    @NotNull
    public ExecutorService getExecutor() {
        if (executor == null) {
            throw new IllegalStateException("Executor not initialized — is the extension registered?");
        }
        return executor;
    }
}

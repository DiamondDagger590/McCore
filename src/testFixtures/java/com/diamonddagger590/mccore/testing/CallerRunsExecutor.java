package com.diamonddagger590.mccore.testing;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A {@link ThreadPoolExecutor} that executes tasks on the calling thread,
 * ensuring {@code mockStatic} scopes remain valid during task execution.
 *
 * <p>Mockito's {@code mockStatic} is thread-local, so production code that
 * submits work to an executor will not see static mocks when running on
 * a separate thread. This executor avoids that by running submitted tasks
 * synchronously on the caller's thread.</p>
 */
public class CallerRunsExecutor extends ThreadPoolExecutor {

    public CallerRunsExecutor() {
        super(0, 1, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<>(),
                new CallerRunsPolicy());
    }

    @Override
    public void execute(Runnable command) {
        command.run();
    }
}

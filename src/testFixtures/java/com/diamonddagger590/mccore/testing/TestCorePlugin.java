package com.diamonddagger590.mccore.testing;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.util.TimeProvider;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * A minimal concrete {@link CorePlugin} for use with MockBukkit in unit tests.
 * <p>
 * Defaults to a fixed clock at {@link Instant#EPOCH} for deterministic time assertions.
 * Use {@link #setClock(Clock)} to inject a different clock before running a test.
 */
public class TestCorePlugin extends CorePlugin {

    private TimeProvider timeProvider = new TimeProvider(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

    /**
     * Replaces the clock used by this plugin's {@link TimeProvider}.
     *
     * @param clock The clock to use.
     */
    public void setClock(@NotNull Clock clock) {
        this.timeProvider = new TimeProvider(clock);
    }

    @Override
    public @NotNull TimeProvider getTimeProvider() {
        return timeProvider;
    }
}

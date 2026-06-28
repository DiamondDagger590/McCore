package com.diamonddagger590.mccore.testing;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.util.TimeProvider;
import org.jetbrains.annotations.NotNull;

/**
 * A minimal concrete {@link CorePlugin} for use with MockBukkit in unit tests.
 */
public class TestCorePlugin extends CorePlugin {

    private final TimeProvider timeProvider = new TimeProvider(java.time.Clock.systemUTC());

    @Override
    public @NotNull TimeProvider getTimeProvider() {
        return timeProvider;
    }
}

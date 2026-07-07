package com.diamonddagger590.mccore.testing;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.util.TimeProvider;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * A non-abstract subclass of {@link CorePlugin} used exclusively by {@link CorePluginTestHelper}
 * to satisfy {@code Unsafe.allocateInstance}. This class is never constructed normally.
 */
final class StubCorePlugin extends CorePlugin {

    @Override
    @NotNull
    public TimeProvider getTimeProvider() {
        return new TimeProvider(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
    }
}

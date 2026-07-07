package com.diamonddagger590.mccore.testing;

import com.diamonddagger590.mccore.CorePlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Provides helpers to inject and clear a mock {@link CorePlugin} singleton for tests
 * that need {@link CorePlugin#getInstance()} without a running Bukkit server.
 */
public class CorePluginMockExtension {

    private static final String INSTANCE_FIELD_NAME = "instance";

    /**
     * Injects a mock {@link CorePlugin} whose {@link CorePlugin#getLogger()} returns
     * a {@link Logger} with the given name.
     *
     * @param loggerName The name for the test logger.
     */
    public static void injectMockPlugin(@NotNull String loggerName) {
        try {
            CorePlugin mockPlugin = mock(CorePlugin.class);
            when(mockPlugin.getLogger()).thenReturn(Logger.getLogger(loggerName));
            Field instanceField = CorePlugin.class.getDeclaredField(INSTANCE_FIELD_NAME);
            instanceField.setAccessible(true);
            instanceField.set(null, mockPlugin);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock CorePlugin", e);
        }
    }

    /**
     * Clears the {@link CorePlugin} singleton, resetting it to {@code null}.
     */
    public static void clearMockPlugin() {
        try {
            Field instanceField = CorePlugin.class.getDeclaredField(INSTANCE_FIELD_NAME);
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear mock CorePlugin", e);
        }
    }
}

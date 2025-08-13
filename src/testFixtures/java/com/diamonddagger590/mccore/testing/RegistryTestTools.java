package com.diamonddagger590.mccore.testing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Provides a way to reset the internals of {@link com.diamonddagger590.mccore.registry.RegistryAccess}
 * in between tests to insure implementation doesn't bleed over while not exposing this functionality
 * outside of testing.
 */
public class RegistryTestTools {

    public static void resetRegistryAccess() {
        try {
            Class<?> raClass = Class.forName("com.diamonddagger590.mccore.registry.RegistryAccess");
            Method resetMethod = findRestMethod(raClass);
            assert resetMethod != null;
            resetMethod.setAccessible(true);
            resetMethod.invoke(null);
        } catch (Throwable ignored) {
            throw new RuntimeException(ignored);
        }
    }

    @Nullable
    private static Method findRestMethod(@NotNull Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals("reset") && method.getParameterCount() == 0) return method;
        }
        return null;
    }
}

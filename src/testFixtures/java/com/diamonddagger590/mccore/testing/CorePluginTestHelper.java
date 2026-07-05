package com.diamonddagger590.mccore.testing;

import com.diamonddagger590.mccore.CorePlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Test utility that initializes a minimal {@link CorePlugin} singleton via reflection,
 * bypassing the {@link org.bukkit.plugin.java.JavaPlugin} constructor. Only the fields
 * needed by pure-logic helpers (e.g. {@code getMiniMessage()}) are populated.
 *
 * <p>Call {@link #installMinimalInstance()} before tests that depend on
 * {@code CorePlugin.getInstance()} and {@link #uninstallInstance()} afterward.</p>
 */
public final class CorePluginTestHelper {

    private CorePluginTestHelper() {
    }

    /**
     * Creates a bare {@link CorePlugin} instance (without running the JavaPlugin constructor),
     * populates its {@code miniMessage} field, and installs it as the static singleton.
     */
    public static void installMinimalInstance() {
        try {
            Unsafe unsafe = getUnsafe();
            CorePlugin stub = (CorePlugin) unsafe.allocateInstance(StubCorePlugin.class);

            Field miniMessageField = CorePlugin.class.getDeclaredField("miniMessage");
            miniMessageField.setAccessible(true);
            miniMessageField.set(stub, MiniMessage.miniMessage());

            Field instanceField = CorePlugin.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, stub);
        } catch (Exception e) {
            throw new RuntimeException("Failed to install minimal CorePlugin instance", e);
        }
    }

    /**
     * Clears the static {@link CorePlugin} singleton.
     */
    public static void uninstallInstance() {
        try {
            Field instanceField = CorePlugin.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to uninstall CorePlugin instance", e);
        }
    }

    @NotNull
    private static Unsafe getUnsafe() throws NoSuchFieldException, IllegalAccessException {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }
}

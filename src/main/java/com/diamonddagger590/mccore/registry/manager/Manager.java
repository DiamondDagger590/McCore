package com.diamonddagger590.mccore.registry.manager;

import com.diamonddagger590.mccore.CorePlugin;
import org.jetbrains.annotations.NotNull;

/**
 * A Manager is a type of class that exhibits some or all of the following behaviors:
 * <ul>
 *     <li>Dynamic addition/removal of stored data</li>
 *     <li>Contains a variety of utility methods</li>
 *     <li>Stored data is operated on in some sort of way</li>
 * </ul>
 * <p>
 * A similar but different concept is a {@link com.diamonddagger590.mccore.registry.Registry} which has its
 * own documentation outlining the use cases it applies to.
 *
 * @param <CP> The {@link CorePlugin} creating this manager.
 */
public abstract class Manager<CP extends CorePlugin> {

    private final CP plugin;

    public Manager(@NotNull CP plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets the {@link CP} that created this manager.
     *
     * @return The {@link CP} that created this manager.
     */
    @NotNull
    public CP plugin() {
        return plugin;
    }
}

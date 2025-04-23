package com.diamonddagger590.mccore.configuration;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.manager.Manager;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This registry is used to register any {@link ReloadableContent}. After the reloadable
 * content has been registered, any calls to {@link #reloadAllContent()} will automatically call
 * {@link ReloadableContent#reloadContent()} for all content that is registered.
 * <p>
 * This is useful for having things like {@link ReloadableSet}s that contain some sort of type such as a
 * set of {@link org.bukkit.Material}s.
 * <p>
 * Additionally, any plugin using McCore should call {@link #reloadAllContent()} for their plugin's
 * specific reload command.
 */
public class ReloadableContentManager extends Manager<CorePlugin> {

    private final Set<ReloadableContent<?>> reloadableContent;

    public ReloadableContentManager(@NotNull CorePlugin plugin) {
        super(plugin);
        this.reloadableContent = new HashSet<>();
    }

    /**
     * Tracks the provided {@link ReloadableContent} for reloading.
     *
     * @param reloadableContent The {@link ReloadableContent} to track.
     */
    public void trackReloadableContent(@NotNull ReloadableContent<?> reloadableContent) {
        this.reloadableContent.add(reloadableContent);
    }

    /**
     * Tracks all of the {@link ReloadableContent} provided in the {@link Collection}.
     *
     * @param reloadableContent A {@link Collection} of all the {@link ReloadableContent} to track.
     */
    public void trackReloadableContent(@NotNull Collection<ReloadableContent<?>> reloadableContent) {
        this.reloadableContent.addAll(reloadableContent);
    }

    /**
     * Reloads all content tracked by this registry.
     */
    public void reloadAllContent() {
        reloadableContent.forEach(ReloadableContent::reloadContent);
    }
}

package com.diamonddagger590.mccore;

import com.diamonddagger590.mccore.bootstrap.StartupProfile;
import com.diamonddagger590.mccore.builder.item.ItemPluginType;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.setting.PlayerSettingRegistry;
import com.diamonddagger590.mccore.util.TimeProvider;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * The abstract version of a plugin that provides some common logic for plugins
 * to use.
 */
public abstract class CorePlugin extends JavaPlugin {

    private static CorePlugin instance;

    private MiniMessage miniMessage;

    protected PlayerSettingRegistry playerSettingRegistry;
    protected TimeProvider timeProvider;

    @Override
    public void onEnable() {
        instance = this;
        miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public void onDisable() {
    }

    @NotNull
    protected StartupProfile resolveProfile() {
        String testMode = System.getProperty("mccore.testMode");
        boolean prod = Boolean.parseBoolean(testMode == null ? "true" : testMode);
        return prod ? StartupProfile.PROD : StartupProfile.TEST;
    }

    /**
     * Gets the {@link ItemPluginType} currently being supported.
     *
     * @return The {@link ItemPluginType} currently being supported.
     */
    @NotNull
    public ItemPluginType getItemPlugin() {
        return ItemPluginType.NONE;
    }

    /**
     * Gets the centralized {@link MiniMessage} for deserializing chat messages
     *
     * @return The centralized {@link MiniMessage} for deserializing chat messages
     */
    @NotNull
    public final MiniMessage getMiniMessage() {
        return miniMessage;
    }

    /**
     * Gets the {@link RegistryAccess} instance used by this plugin.
     *
     * @return The {@link RegistryAccess} instance used by this plugin.
     */
    @NotNull
    public final RegistryAccess registryAccess() {
        return RegistryAccess.registryAccess();
    }

    /**
     * Gets the {@link TimeProvider} used by this plugin instance.
     *
     * @return The {@link TimeProvider} used by this plugin instance.
     */
    @NotNull
    public abstract TimeProvider getTimeProvider();

    /**
     * Gets the instance of this plugin or throws a {@link NullPointerException} if not initialized.
     *
     * @return The instance of this plugin.
     */
    @NotNull
    public static CorePlugin getInstance() {
        if (instance == null) {
            throw new NullPointerException("Plugin was not initialized.");
        }
        return instance;
    }
}

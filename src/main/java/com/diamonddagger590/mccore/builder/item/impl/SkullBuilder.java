package com.diamonddagger590.mccore.builder.item.impl;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.diamonddagger590.mccore.builder.item.BaseItemBuilder;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.UUID;

/**
 * An item builder that builds player heads.
 */
public class SkullBuilder extends BaseItemBuilder<SkullBuilder> {

    private final ResolvableProfile.Builder builder;

    public SkullBuilder(@NotNull final ItemStack itemStack) {
        super(itemStack);

        this.builder = ResolvableProfile.resolvableProfile();
    }

    /**
     * Uses the {@link UUID} of the provided {@link Audience} (if there is one) to
     * set the texture uuid of the player head.
     *
     * @param audience The {@link Audience} to get the UUID of.
     * @return This builder.
     */
    @NotNull
    public SkullBuilder withAudience(@NotNull final Audience audience) {
        final UUID uuid = audience.getOrDefault(Identity.UUID, null);
        if (uuid == null) return this;
        this.builder.uuid(uuid);
        return this;
    }

    /**
     * Uses the provided {@link UUID} to set the texture uuid of the player head.
     *
     * @param uuid The {@link UUID} to use.
     * @return This builder.
     */
    @NotNull
    public SkullBuilder withAudience(@NotNull final UUID uuid) {
        this.builder.uuid(uuid);
        return this;
    }

    /**
     * Uses the provided url to get a skin to use for the player head.
     *
     * @param url The url to get a skin from.
     * @return This builder.
     */
    @NotNull
    public SkullBuilder withUrl(@NotNull final String url) {
        if (url.isEmpty()) return this;
        final String newUrl = "https://textures.minecraft.net/texture/" + url;
        final PlayerProfile profile = corePlugin.getServer().createProfile(UUID.randomUUID(), null);
        profile.setProperty(new ProfileProperty("", ""));
        final PlayerTextures textures = profile.getTextures();
        try {
            textures.setSkin(URI.create(newUrl).toURL(), PlayerTextures.SkinModel.CLASSIC);
        } catch (MalformedURLException exception) {
            exception.printStackTrace();
        }
        profile.setTextures(textures);
        this.builder.addProperties(profile.getProperties());
        return this;
    }

    /**
     * Sets the texture of the player head using the provided base64 representation.
     *
     * @param base64 The base64 representation of the texture.
     * @return This builder.
     */
    @NotNull
    @Override
    public SkullBuilder withBase64(@NotNull final String base64) {
        if (base64.isEmpty()) return this;
        this.builder.addProperty(new ProfileProperty("textures", base64));
        return this;
    }

    /**
     * Uses the provided player name to set the texture of the player head.
     *
     * @param playerName The name of the player to use.
     * @return This builder.
     */
    @NotNull
    public SkullBuilder withName(@NotNull final String playerName) {
        if (playerName.isEmpty()) return this;
        if (playerName.length() > 16) {
            return withUrl(playerName);
        }
        this.builder.name(playerName);
        return this;
    }

    @NotNull
    @Override
    public SkullBuilder build() {
        getItemStack().setData(DataComponentTypes.PROFILE, this.builder.build());
        return this;
    }
}

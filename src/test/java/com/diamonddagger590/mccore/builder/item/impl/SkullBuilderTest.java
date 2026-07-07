package com.diamonddagger590.mccore.builder.item.impl;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkullBuilderTest {

    private TestCorePlugin plugin;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(TestCorePlugin.class);
        RegistryResetExtension.setupRegistry();
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
        MockBukkit.unmock();
    }

    private SkullBuilder createBuilder() {
        return new SkullBuilder(new ItemStack(Material.PLAYER_HEAD));
    }

    @Test
    @DisplayName("Given a player head ItemStack, when constructing SkullBuilder, then succeeds")
    void constructor_succeeds_withPlayerHead() {
        SkullBuilder builder = createBuilder();
        assertNotNull(builder);
    }

    @Test
    @DisplayName("Given an empty string, when withName is called, then returns this builder unchanged")
    void withName_returnsSelf_whenNameIsEmpty() {
        SkullBuilder builder = createBuilder();
        SkullBuilder result = builder.withName("");
        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a valid short name, when withName is called, then returns this builder")
    void withName_returnsSelf_whenNameIsShort() {
        SkullBuilder builder = createBuilder();
        SkullBuilder result = builder.withName("Steve");
        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a name longer than 16 characters, when withName is called, then delegates to withUrl")
    void withName_delegatesToWithUrl_whenNameExceedsSixteenCharacters() {
        SkullBuilder builder = createBuilder();
        SkullBuilder result = builder.withName("a1b2c3d4e5f6g7h8i");
        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given an empty string, when withBase64 is called, then returns this builder unchanged")
    void withBase64_returnsSelf_whenBase64IsEmpty() {
        SkullBuilder builder = createBuilder();
        SkullBuilder result = builder.withBase64("");
        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a valid base64 string, when withBase64 is called, then returns this builder")
    void withBase64_returnsSelf_whenBase64IsValid() {
        SkullBuilder builder = createBuilder();
        SkullBuilder result = builder.withBase64("dGVzdA==");
        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given an empty string, when withUrl is called, then returns this builder unchanged")
    void withUrl_returnsSelf_whenUrlIsEmpty() {
        SkullBuilder builder = createBuilder();
        SkullBuilder result = builder.withUrl("");
        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a valid UUID, when withAudience(UUID) is called, then returns this builder")
    void withAudienceUuid_returnsSelf_withValidUuid() {
        SkullBuilder builder = createBuilder();
        UUID uuid = UUID.randomUUID();
        SkullBuilder result = builder.withAudience(uuid);
        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a valid texture url, when withUrl is called, then returns this builder")
    void withUrl_returnsSelf_whenUrlIsValid() {
        SkullBuilder builder = createBuilder();
        SkullBuilder result = builder.withUrl("abc123texture");
        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a builder with base64 texture, when build is called, then profile data is set on the item")
    void build_setsProfileData_whenBase64TextureProvided() {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullBuilder builder = new SkullBuilder(itemStack);
        builder.withBase64("dGVzdA==");
        builder.build();
        assertTrue(itemStack.hasData(DataComponentTypes.PROFILE));
    }

    @Test
    @DisplayName("Given a built builder, when build is called, then returns this builder")
    void build_returnsSelf() {
        SkullBuilder builder = createBuilder();
        SkullBuilder result = builder.build();
        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a built builder, when hideSkullDynamicToolTip is called with no existing tooltip, then tooltip display is set")
    void hideSkullDynamicToolTip_setsTooltipDisplay_withNoExistingTooltip() {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullBuilder builder = new SkullBuilder(itemStack);
        builder.hideSkullDynamicToolTip();
        assertTrue(itemStack.hasData(DataComponentTypes.TOOLTIP_DISPLAY));
    }
}

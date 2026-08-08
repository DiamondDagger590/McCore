package com.diamonddagger590.mccore.builder.item.impl;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    @DisplayName("Given an item with existing tooltip data, when hideSkullDynamicToolTip is called, then PROFILE is merged into existing hidden components")
    void hideSkullDynamicToolTip_mergesProfileIntoExistingHiddenComponents_whenTooltipAlreadyExists() {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        itemStack.setData(DataComponentTypes.TOOLTIP_DISPLAY,
                TooltipDisplay.tooltipDisplay()
                        .addHiddenComponents(DataComponentTypes.ENCHANTMENTS)
                        .build());

        SkullBuilder builder = new SkullBuilder(itemStack);
        builder.hideSkullDynamicToolTip();

        assertTrue(itemStack.hasData(DataComponentTypes.TOOLTIP_DISPLAY));
        TooltipDisplay result = itemStack.getData(DataComponentTypes.TOOLTIP_DISPLAY);
        assertNotNull(result);
        assertTrue(result.hiddenComponents().contains(DataComponentTypes.PROFILE));
        assertTrue(result.hiddenComponents().contains(DataComponentTypes.ENCHANTMENTS));
    }

    @Test
    @DisplayName("Given an Audience with a UUID, when withAudience(Audience) is called, then sets the UUID on the builder")
    void withAudience_setsUuid_whenAudienceHasUuid() {
        SkullBuilder builder = createBuilder();
        UUID testUuid = UUID.randomUUID();
        Audience audience = mock(Audience.class);
        when(audience.getOrDefault(Identity.UUID, null)).thenReturn(testUuid);

        SkullBuilder result = builder.withAudience(audience);

        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given an Audience without a UUID, when withAudience(Audience) is called, then returns builder unchanged")
    void withAudience_returnsSelf_whenAudienceHasNoUuid() {
        SkullBuilder builder = createBuilder();
        Audience audience = mock(Audience.class);
        when(audience.getOrDefault(Identity.UUID, null)).thenReturn(null);

        SkullBuilder result = builder.withAudience(audience);

        assertSame(builder, result);
    }

    @Test
    @DisplayName("Given a builder with UUID audience, when build is called, then profile includes the UUID")
    void build_setsProfile_whenAudienceUuidProvided() {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullBuilder builder = new SkullBuilder(itemStack);
        UUID testUuid = UUID.randomUUID();
        builder.withAudience(testUuid);
        builder.build();

        assertTrue(itemStack.hasData(DataComponentTypes.PROFILE));
    }

    @Test
    @DisplayName("Given a builder with name and url texture, when build is called, then profile data is set")
    void build_setsProfile_whenNameAndUrlProvided() {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullBuilder builder = new SkullBuilder(itemStack);
        builder.withName("Steve");
        builder.withUrl("abc123textureid");
        builder.build();

        assertTrue(itemStack.hasData(DataComponentTypes.PROFILE));
    }
}

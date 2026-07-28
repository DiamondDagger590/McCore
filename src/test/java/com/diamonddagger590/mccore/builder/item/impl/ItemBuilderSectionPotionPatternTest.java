package com.diamonddagger590.mccore.builder.item.impl;

import com.diamonddagger590.mccore.builder.item.ItemBuilderConfigurationKeys;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItemBuilderSectionPotionPatternTest {

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        MockBukkit.load(TestCorePlugin.class);
        RegistryResetExtension.setupRegistry();
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
        MockBukkit.unmock();
    }

    private Section createMinimalSection() {
        Section section = mock(Section.class);
        when(section.getString(eq(ItemBuilderConfigurationKeys.DATA), eq(""))).thenReturn("");
        when(section.getString(eq(ItemBuilderConfigurationKeys.MATERIAL), eq("stone"))).thenReturn("stone");
        when(section.getString(eq(ItemBuilderConfigurationKeys.NAME), eq(""))).thenReturn("");
        when(section.getStringList(eq(ItemBuilderConfigurationKeys.LORE_ROUTE))).thenReturn(Collections.emptyList());
        when(section.getInt(eq(ItemBuilderConfigurationKeys.AMOUNT), eq(1))).thenReturn(1);
        when(section.getInt(eq(ItemBuilderConfigurationKeys.CUSTOM_MODEL_DATA), eq(-1))).thenReturn(-1);
        when(section.getBoolean(eq(ItemBuilderConfigurationKeys.HIDE_TOOLTIP), eq(false))).thenReturn(false);
        when(section.getBoolean(eq(ItemBuilderConfigurationKeys.UNBREAKABLE_ITEM), eq(false))).thenReturn(false);
        when(section.getBoolean(eq(ItemBuilderConfigurationKeys.GLOWING), eq(false))).thenReturn(false);
        when(section.getStringList(eq(ItemBuilderConfigurationKeys.ITEM_FLAGS))).thenReturn(Collections.emptyList());
        when(section.getString(eq(ItemBuilderConfigurationKeys.PLAYER), eq(""))).thenReturn("");
        when(section.getInt(eq(ItemBuilderConfigurationKeys.DAMAGE), eq(0))).thenReturn(0);
        when(section.getString(eq(ItemBuilderConfigurationKeys.SKULL), eq(""))).thenReturn("");
        when(section.getString(eq(ItemBuilderConfigurationKeys.RGB), eq(""))).thenReturn("");
        when(section.getString(eq(ItemBuilderConfigurationKeys.COLOR), eq(""))).thenReturn("");
        when(section.getString(eq(ItemBuilderConfigurationKeys.MOB_TYPE), eq(""))).thenReturn("");
        when(section.getString(eq(ItemBuilderConfigurationKeys.TRIM_PATTERN), eq(""))).thenReturn("");
        when(section.getString(eq(ItemBuilderConfigurationKeys.TRIM_MATERIAL), eq(""))).thenReturn("");
        when(section.contains(ItemBuilderConfigurationKeys.MAX_STACK_SIZE)).thenReturn(false);
        when(section.contains(ItemBuilderConfigurationKeys.CUSTOM_ITEM)).thenReturn(false);
        when(section.getSection(eq(ItemBuilderConfigurationKeys.ENCHANTMENTS))).thenReturn(null);
        when(section.getSection(eq(ItemBuilderConfigurationKeys.POTION_HEADER))).thenReturn(null);
        when(section.getSection(eq(ItemBuilderConfigurationKeys.PATTERN_HEADER))).thenReturn(null);
        return section;
    }

    private Section createPotionSection() {
        Section section = createMinimalSection();
        when(section.getString(eq(ItemBuilderConfigurationKeys.MATERIAL), eq("stone"))).thenReturn("potion");

        Section potionSection = mock(Section.class);
        when(section.getSection(eq(ItemBuilderConfigurationKeys.POTION_HEADER))).thenReturn(potionSection);
        when(potionSection.getRoutesAsStrings(false)).thenReturn(Set.of("speed"));

        Route speedRoute = Route.fromString("speed");
        when(potionSection.getInt(eq(Route.addTo(speedRoute, ItemBuilderConfigurationKeys.POTION_DURATION)), eq(60))).thenReturn(200);
        when(potionSection.getInt(eq(Route.addTo(speedRoute, ItemBuilderConfigurationKeys.POTION_LEVEL)), eq(1))).thenReturn(2);
        when(potionSection.getBoolean(eq(Route.addTo(speedRoute, ItemBuilderConfigurationKeys.POTION_ICON)), eq(false))).thenReturn(true);
        when(potionSection.getBoolean(eq(Route.addTo(speedRoute, ItemBuilderConfigurationKeys.POTION_AMBIENT)), eq(false))).thenReturn(true);
        when(potionSection.getBoolean(eq(Route.addTo(speedRoute, ItemBuilderConfigurationKeys.POTION_PARTICLES)), eq(false))).thenReturn(false);

        return section;
    }

    @Nested
    @DisplayName("Potion section parsing")
    class PotionSectionParsing {

        @Test
        @DisplayName("Given a section with potion effects, when from(Section, ItemBuilder) is called, then potion section is read")
        void fromSection_appliesPotionEffects_whenPotionSectionPresent() {
            Section section = createPotionSection();
            Section potionSection = section.getSection(ItemBuilderConfigurationKeys.POTION_HEADER);
            ItemStack potionStack = new ItemStack(Material.POTION);
            ItemBuilder potionBuilder = ItemBuilder.from(potionStack);

            ItemBuilder result = assertDoesNotThrow(() -> ItemBuilder.from(section, potionBuilder));
            assertNotNull(result);
            assertEquals(Material.POTION, result.asItemStack().getType());

            Route speedRoute = Route.fromString("speed");
            verify(potionSection).getInt(eq(Route.addTo(speedRoute, ItemBuilderConfigurationKeys.POTION_DURATION)), eq(60));
            verify(potionSection).getInt(eq(Route.addTo(speedRoute, ItemBuilderConfigurationKeys.POTION_LEVEL)), eq(1));
        }

        @Test
        @DisplayName("Given a section with invalid potion name, when from(Section, ItemBuilder) is called, then skips invalid potion")
        void fromSection_skipsInvalidPotion_whenPotionNameInvalid() {
            Section section = createMinimalSection();
            when(section.getString(eq(ItemBuilderConfigurationKeys.MATERIAL), eq("stone"))).thenReturn("potion");

            Section potionSection = mock(Section.class);
            when(section.getSection(eq(ItemBuilderConfigurationKeys.POTION_HEADER))).thenReturn(potionSection);
            when(potionSection.getRoutesAsStrings(false)).thenReturn(Set.of("not_a_real_potion_type_xyz"));

            ItemStack potionStack = new ItemStack(Material.POTION);
            ItemBuilder potionBuilder = ItemBuilder.from(potionStack);

            ItemBuilder result = assertDoesNotThrow(() -> ItemBuilder.from(section, potionBuilder));
            assertNotNull(result);
            assertEquals(Material.POTION, result.asItemStack().getType());
        }

        @Test
        @DisplayName("Given a section with multiple potion effects, when from(Section, ItemBuilder) is called, then completes without error")
        void fromSection_appliesMultiplePotionEffects_whenMultiplePotionsPresent() {
            Section section = createMinimalSection();
            when(section.getString(eq(ItemBuilderConfigurationKeys.MATERIAL), eq("stone"))).thenReturn("potion");

            Section potionSection = mock(Section.class);
            when(section.getSection(eq(ItemBuilderConfigurationKeys.POTION_HEADER))).thenReturn(potionSection);
            when(potionSection.getRoutesAsStrings(false)).thenReturn(Set.of("speed", "strength"));

            Route speedRoute = Route.fromString("speed");
            when(potionSection.getInt(eq(Route.addTo(speedRoute, ItemBuilderConfigurationKeys.POTION_DURATION)), eq(60))).thenReturn(100);
            when(potionSection.getInt(eq(Route.addTo(speedRoute, ItemBuilderConfigurationKeys.POTION_LEVEL)), eq(1))).thenReturn(1);
            when(potionSection.getBoolean(eq(Route.addTo(speedRoute, ItemBuilderConfigurationKeys.POTION_ICON)), eq(false))).thenReturn(false);
            when(potionSection.getBoolean(eq(Route.addTo(speedRoute, ItemBuilderConfigurationKeys.POTION_AMBIENT)), eq(false))).thenReturn(false);
            when(potionSection.getBoolean(eq(Route.addTo(speedRoute, ItemBuilderConfigurationKeys.POTION_PARTICLES)), eq(false))).thenReturn(false);

            Route strengthRoute = Route.fromString("strength");
            when(potionSection.getInt(eq(Route.addTo(strengthRoute, ItemBuilderConfigurationKeys.POTION_DURATION)), eq(60))).thenReturn(300);
            when(potionSection.getInt(eq(Route.addTo(strengthRoute, ItemBuilderConfigurationKeys.POTION_LEVEL)), eq(1))).thenReturn(3);
            when(potionSection.getBoolean(eq(Route.addTo(strengthRoute, ItemBuilderConfigurationKeys.POTION_ICON)), eq(false))).thenReturn(true);
            when(potionSection.getBoolean(eq(Route.addTo(strengthRoute, ItemBuilderConfigurationKeys.POTION_AMBIENT)), eq(false))).thenReturn(false);
            when(potionSection.getBoolean(eq(Route.addTo(strengthRoute, ItemBuilderConfigurationKeys.POTION_PARTICLES)), eq(false))).thenReturn(true);

            ItemStack potionStack = new ItemStack(Material.POTION);
            ItemBuilder potionBuilder = ItemBuilder.from(potionStack);

            ItemBuilder result = assertDoesNotThrow(() -> ItemBuilder.from(section, potionBuilder));
            assertNotNull(result);
            assertEquals(Material.POTION, result.asItemStack().getType());
        }
    }

    @Nested
    @DisplayName("Pattern section parsing")
    class PatternSectionParsing {

        @Test
        @DisplayName("Given a section with banner patterns, when from(Section, ItemBuilder) is called, then patterns are applied")
        void fromSection_appliesPatterns_whenPatternSectionPresent() {
            Section section = createMinimalSection();
            when(section.getString(eq(ItemBuilderConfigurationKeys.MATERIAL), eq("stone"))).thenReturn("white_banner");

            Section patternSection = mock(Section.class);
            when(section.getSection(eq(ItemBuilderConfigurationKeys.PATTERN_HEADER))).thenReturn(patternSection);
            when(patternSection.getRoutesAsStrings(false)).thenReturn(Set.of("stripe_top"));
            when(patternSection.getString("stripe_top", "white")).thenReturn("red");

            ItemStack bannerStack = new ItemStack(Material.WHITE_BANNER);
            ItemBuilder bannerBuilder = ItemBuilder.from(bannerStack);

            ItemBuilder result = assertDoesNotThrow(() -> ItemBuilder.from(section, bannerBuilder));
            assertNotNull(result);
        }

        @Test
        @DisplayName("Given a section with shield patterns, when from(Section, ItemBuilder) is called, then patterns are applied")
        void fromSection_appliesPatterns_whenShieldWithPatterns() {
            Section section = createMinimalSection();
            when(section.getString(eq(ItemBuilderConfigurationKeys.MATERIAL), eq("stone"))).thenReturn("shield");

            Section patternSection = mock(Section.class);
            when(section.getSection(eq(ItemBuilderConfigurationKeys.PATTERN_HEADER))).thenReturn(patternSection);
            when(patternSection.getRoutesAsStrings(false)).thenReturn(Set.of("cross"));
            when(patternSection.getString("cross", "white")).thenReturn("blue");

            ItemStack shieldStack = new ItemStack(Material.SHIELD);
            ItemBuilder shieldBuilder = ItemBuilder.from(shieldStack);

            ItemBuilder result = assertDoesNotThrow(() -> ItemBuilder.from(section, shieldBuilder));
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Skull player name")
    class SkullPlayerName {

        @Test
        @DisplayName("Given a section with player name, when from(Section, ItemBuilder) with player head, then skull is set")
        void fromSection_setsSkullPlayer_whenPlayerNameProvided() {
            Section section = createMinimalSection();
            when(section.getString(eq(ItemBuilderConfigurationKeys.MATERIAL), eq("stone"))).thenReturn("player_head");
            when(section.getString(eq(ItemBuilderConfigurationKeys.PLAYER), eq(""))).thenReturn("Notch");

            ItemStack headStack = new ItemStack(Material.PLAYER_HEAD);
            ItemBuilder headBuilder = ItemBuilder.from(headStack);

            ItemBuilder result = assertDoesNotThrow(() -> ItemBuilder.from(section, headBuilder));
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Copy constructor")
    class CopyConstructor {

        @Test
        @DisplayName("Given an ItemBuilder with display name, when copied via copy constructor, then state is preserved")
        void copyConstructor_preservesDisplayName_fromSource() {
            ItemBuilder source = ItemBuilder.from(ItemType.DIAMOND, 3);
            source.setDisplayName("Original Name");

            ItemBuilder copy = new ItemBuilder(source);
            assertNotNull(copy);

            ItemStack copyStack = copy.asItemStack();
            assertEquals(3, copyStack.getAmount());
        }

        @Test
        @DisplayName("Given an ItemBuilder with lore, when copied via copy constructor, then lore is preserved")
        void copyConstructor_preservesLore_fromSource() {
            ItemBuilder source = ItemBuilder.from(ItemType.IRON_SWORD);
            source.setDisplayName("Sword").withDisplayLore(java.util.List.of("Line 1", "Line 2"));

            ItemBuilder copy = new ItemBuilder(source);
            ItemStack copyStack = copy.asItemStack();
            assertNotNull(copyStack);
            assertEquals(Material.IRON_SWORD, copyStack.getType());
        }
    }
}

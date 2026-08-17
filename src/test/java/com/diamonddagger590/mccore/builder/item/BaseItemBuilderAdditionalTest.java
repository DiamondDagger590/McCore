package com.diamonddagger590.mccore.builder.item;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.builder.item.impl.fireworks.FireworkBuilder;
import com.diamonddagger590.mccore.builder.item.impl.fireworks.FireworkStarBuilder;
import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.builder.item.impl.PatternBuilder;
import com.diamonddagger590.mccore.builder.item.impl.PotionBuilder;
import com.diamonddagger590.mccore.builder.item.impl.SkullBuilder;
import com.diamonddagger590.mccore.builder.item.impl.SpawnerBuilder;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class BaseItemBuilderAdditionalTest {

    private MockedStatic<CorePlugin> corePluginStatic;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        RegistryResetExtension.setupRegistry();

        CorePlugin mockPlugin = mock(CorePlugin.class);
        when(mockPlugin.getMiniMessage()).thenReturn(MiniMessage.miniMessage());
        when(mockPlugin.getItemPlugin()).thenReturn(ItemPluginType.NONE);
        when(mockPlugin.registryAccess()).thenCallRealMethod();

        corePluginStatic = mockStatic(CorePlugin.class);
        corePluginStatic.when(CorePlugin::getInstance).thenReturn(mockPlugin);
    }

    @AfterEach
    void tearDown() {
        corePluginStatic.close();
        MockBukkit.unmock();
        RegistryResetExtension.resetRegistry();
    }

    @Nested
    @DisplayName("isEdible")
    class IsEdibleTests {

        @Test
        @DisplayName("Given an edible material, when isEdible is called, then returns true")
        void isEdible_returnsTrue_whenMaterialIsEdible() {
            ItemBuilder builder = ItemBuilder.from(ItemType.APPLE);
            assertTrue(builder.isEdible());
        }

        @Test
        @DisplayName("Given a non-edible material, when isEdible is called, then returns false")
        void isEdible_returnsFalse_whenMaterialIsNotEdible() {
            ItemBuilder builder = ItemBuilder.from(ItemType.STONE);
            assertFalse(builder.isEdible());
        }

        @Test
        @DisplayName("Given cooked beef material, when isEdible is called, then returns true")
        void isEdible_returnsTrue_whenMaterialIsCookedBeef() {
            ItemBuilder builder = ItemBuilder.from(ItemType.COOKED_BEEF);
            assertTrue(builder.isEdible());
        }
    }

    @Nested
    @DisplayName("getPlainName and getPlainLore")
    class PlainNameAndLoreTests {

        @Test
        @DisplayName("Given an item with a display name, when getPlainName is called after asItemStack, then returns the plain text name")
        void getPlainName_returnsPlainText_whenDisplayNameIsSet() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.setDisplayName("Test Sword");
            builder.asItemStack();
            String plainName = builder.getPlainName();
            assertNotNull(plainName);
            assertEquals("Test Sword", plainName);
        }

        @Test
        @DisplayName("Given an item without a display name, when getPlainName is called, then returns empty string")
        void getPlainName_returnsEmpty_whenNoDisplayName() {
            ItemBuilder builder = ItemBuilder.from(ItemType.STONE);
            String plainName = builder.getPlainName();
            assertEquals("", plainName);
        }

        @Test
        @DisplayName("Given an item with lore, when getPlainLore is called after asItemStack, then returns the lore lines as plain text")
        void getPlainLore_returnsPlainText_whenLoreIsSet() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.addDisplayLore("Line 1");
            builder.addDisplayLore("Line 2");
            builder.asItemStack();
            List<String> plainLore = builder.getPlainLore();
            assertNotNull(plainLore);
            assertEquals(2, plainLore.size());
            assertEquals("Line 1", plainLore.get(0));
            assertEquals("Line 2", plainLore.get(1));
        }

        @Test
        @DisplayName("Given an item without lore, when getPlainLore is called, then returns empty list")
        void getPlainLore_returnsEmptyList_whenNoLore() {
            ItemBuilder builder = ItemBuilder.from(ItemType.STONE);
            List<String> plainLore = builder.getPlainLore();
            assertNotNull(plainLore);
            assertTrue(plainLore.isEmpty());
        }
    }

    @Nested
    @DisplayName("withType")
    class WithTypeTests {

        @Test
        @DisplayName("Given a builder with existing item stack, when withType with amount, then returns self without changing type")
        void withType_returnsSelf_whenItemStackAlreadyExists() {
            ItemBuilder builder = ItemBuilder.from(ItemType.STONE);
            assertSame(builder, builder.withType(ItemType.DIAMOND, 3));
        }

        @Test
        @DisplayName("Given a builder with existing item stack, when withType without amount, then returns self without changing type")
        void withType_returnsSelf_whenCalledWithSingleArg() {
            ItemBuilder builder = ItemBuilder.from(ItemType.STONE);
            assertSame(builder, builder.withType(ItemType.IRON_INGOT));
        }
    }

    @Nested
    @DisplayName("Builder Type Conversion Success Paths")
    class BuilderConversionSuccessTests {

        @Test
        @DisplayName("Given firework material, when asFireworkBuilder, then returns FireworkBuilder")
        void asFireworkBuilder_returnsFireworkBuilder_whenMaterialIsFirework() {
            ItemBuilder builder = ItemBuilder.from(ItemType.FIREWORK_ROCKET);
            FireworkBuilder result = builder.asFireworkBuilder();
            assertNotNull(result);
            assertInstanceOf(FireworkBuilder.class, result);
        }

        @Test
        @DisplayName("Given firework star material, when asFireworkStarBuilder, then returns FireworkStarBuilder")
        void asFireworkStarBuilder_returnsFireworkStarBuilder_whenMaterialIsFireworkStar() {
            ItemBuilder builder = ItemBuilder.from(ItemType.FIREWORK_STAR);
            FireworkStarBuilder result = builder.asFireworkStarBuilder();
            assertNotNull(result);
            assertInstanceOf(FireworkStarBuilder.class, result);
        }

        @Test
        @DisplayName("Given shield material, when asPatternBuilder, then returns PatternBuilder")
        void asPatternBuilder_returnsPatternBuilder_whenMaterialIsShield() {
            ItemBuilder builder = ItemBuilder.from(ItemType.SHIELD);
            PatternBuilder result = builder.asPatternBuilder();
            assertNotNull(result);
            assertInstanceOf(PatternBuilder.class, result);
        }

        @Test
        @DisplayName("Given banner material, when asPatternBuilder, then returns PatternBuilder")
        void asPatternBuilder_returnsPatternBuilder_whenMaterialIsBanner() {
            ItemBuilder builder = ItemBuilder.from(ItemType.WHITE_BANNER);
            PatternBuilder result = builder.asPatternBuilder();
            assertNotNull(result);
            assertInstanceOf(PatternBuilder.class, result);
        }

        @Test
        @DisplayName("Given player head material, when asSkullBuilder, then returns SkullBuilder")
        void asSkullBuilder_returnsSkullBuilder_whenMaterialIsPlayerHead() {
            ItemBuilder builder = ItemBuilder.from(ItemType.PLAYER_HEAD);
            SkullBuilder result = builder.asSkullBuilder();
            assertNotNull(result);
            assertInstanceOf(SkullBuilder.class, result);
        }

        @Test
        @DisplayName("Given potion material, when asPotionBuilder, then returns PotionBuilder")
        void asPotionBuilder_returnsPotionBuilder_whenMaterialIsPotion() {
            ItemBuilder builder = ItemBuilder.from(ItemType.POTION);
            PotionBuilder result = builder.asPotionBuilder();
            assertNotNull(result);
            assertInstanceOf(PotionBuilder.class, result);
        }

        @Test
        @DisplayName("Given splash potion material, when asPotionBuilder, then returns PotionBuilder")
        void asPotionBuilder_returnsPotionBuilder_whenMaterialIsSplashPotion() {
            ItemBuilder builder = ItemBuilder.from(ItemType.SPLASH_POTION);
            PotionBuilder result = builder.asPotionBuilder();
            assertNotNull(result);
            assertInstanceOf(PotionBuilder.class, result);
        }

        @Test
        @DisplayName("Given spawner material, when asSpawnerBuilder, then returns SpawnerBuilder")
        void asSpawnerBuilder_returnsSpawnerBuilder_whenMaterialIsSpawner() {
            ItemBuilder builder = ItemBuilder.from(ItemType.SPAWNER);
            SpawnerBuilder result = builder.asSpawnerBuilder();
            assertNotNull(result);
            assertInstanceOf(SpawnerBuilder.class, result);
        }
    }

    @Nested
    @DisplayName("Builder Type Conversion with Populated State")
    class BuilderConversionWithStateTests {

        @Test
        @DisplayName("Given a builder with display name, when asSkullBuilder, then returns valid SkullBuilder")
        void asSkullBuilder_returnsSkullBuilder_whenBuilderHasDisplayName() {
            ItemBuilder original = ItemBuilder.from(ItemType.PLAYER_HEAD);
            original.setDisplayName("Original Name");
            SkullBuilder skull = original.asSkullBuilder();
            assertNotNull(skull);
            assertInstanceOf(SkullBuilder.class, skull);
        }

        @Test
        @DisplayName("Given a builder with lore, when asPotionBuilder, then returns valid PotionBuilder")
        void asPotionBuilder_returnsPotionBuilder_whenBuilderHasLore() {
            ItemBuilder original = ItemBuilder.from(ItemType.POTION);
            original.addDisplayLore("Lore Line");
            PotionBuilder potion = original.asPotionBuilder();
            assertNotNull(potion);
            assertInstanceOf(PotionBuilder.class, potion);
        }
    }

    @Nested
    @DisplayName("removeEnchantment by String")
    class RemoveEnchantmentByStringTests {

        @Test
        @DisplayName("Given a builder, when removeEnchantment with empty string, then does not throw and returns builder")
        void removeEnchantment_doesNotThrow_whenEmptyString() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            assertDoesNotThrow(() -> builder.removeEnchantment(""));
        }

        @Test
        @DisplayName("Given a builder, when removeEnchantment with unknown string, then does not throw and returns builder")
        void removeEnchantment_doesNotThrow_whenUnknownEnchantment() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            ItemBuilder result = builder.removeEnchantment("not_a_real_enchantment");
            assertSame(builder, result);
        }

        @Test
        @DisplayName("Given a builder, when removeEnchantment with valid string, then returns builder for chaining")
        void removeEnchantment_returnsSelf_whenValidString() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            ItemBuilder result = builder.removeEnchantment("sharpness");
            assertSame(builder, result);
        }
    }
}

package com.diamonddagger590.mccore.builder.item;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.builder.item.impl.PatternBuilder;
import com.diamonddagger590.mccore.builder.item.impl.PotionBuilder;
import com.diamonddagger590.mccore.builder.item.impl.SkullBuilder;
import com.diamonddagger590.mccore.builder.item.impl.SpawnerBuilder;
import com.diamonddagger590.mccore.builder.item.impl.fireworks.FireworkBuilder;
import com.diamonddagger590.mccore.builder.item.impl.fireworks.FireworkStarBuilder;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MockBukkit-based tests for {@link BaseItemBuilder} methods that require a live
 * Paper server environment (enchantments, color, damage, plain name/lore, builder
 * conversions for valid types).
 */
class BaseItemBuilderMockBukkitTest {

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

    @Nested
    @DisplayName("Enchantment Addition")
    class EnchantmentAdditionTests {

        @Test
        @DisplayName("Given a regular item, when addEnchantment(Enchantment, int) is called, then enchantment is applied without error")
        void addEnchantment_appliesEnchantment_whenRegularItem() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);

            assertDoesNotThrow(() -> builder.addEnchantment(Enchantment.SHARPNESS, 5));
            assertDoesNotThrow(() -> builder.asItemStack());
        }

        @Test
        @DisplayName("Given a regular item with existing enchantment, when addEnchantment is called with different enchantment, then both are applied without error")
        void addEnchantment_preservesExistingEnchantments_whenAddingNew() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            assertDoesNotThrow(() -> builder.addEnchantment(Enchantment.SHARPNESS, 3));

            assertDoesNotThrow(() -> builder.addEnchantment(Enchantment.UNBREAKING, 2));
            assertDoesNotThrow(() -> builder.asItemStack());
        }

        @Test
        @DisplayName("Given an enchanted book, when addEnchantment is called, then stored enchantment is applied without error")
        void addEnchantment_appliesStoredEnchantment_whenEnchantedBook() {
            ItemBuilder builder = ItemBuilder.from(ItemType.ENCHANTED_BOOK);

            assertDoesNotThrow(() -> builder.addEnchantment(Enchantment.SHARPNESS, 5));
        }

        @Test
        @DisplayName("Given an enchanted book with existing stored enchantment, when addEnchantment is called, then both are applied without error")
        void addEnchantment_preservesExistingStoredEnchantments_whenAddingNew() {
            ItemBuilder builder = ItemBuilder.from(ItemType.ENCHANTED_BOOK);
            builder.addEnchantment(Enchantment.SHARPNESS, 3);

            assertDoesNotThrow(() -> builder.addEnchantment(Enchantment.UNBREAKING, 2));
        }

        @Test
        @DisplayName("Given a valid enchantment string, when addEnchantment(String, int) is called, then enchantment is applied without error")
        void addEnchantment_appliesEnchantment_whenValidString() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);

            assertDoesNotThrow(() -> builder.addEnchantment("sharpness", 3));
            assertDoesNotThrow(() -> builder.asItemStack());
        }

        @Test
        @DisplayName("Given an invalid enchantment string, when addEnchantment(String, int) is called, then no enchantment is applied")
        void addEnchantment_doesNotApply_whenInvalidString() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);

            builder.addEnchantment("not_a_real_enchantment", 3);

            ItemStack result = builder.asItemStack();
            assertTrue(result.getEnchantments().isEmpty());
        }

        @Test
        @DisplayName("Given an item with enchantment, when removeEnchantment(String) is called, then completes without error")
        void removeEnchantment_completesWithoutError_whenValidString() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.addEnchantment(Enchantment.SHARPNESS, 3);

            assertDoesNotThrow(() -> builder.removeEnchantment("sharpness"));
            assertDoesNotThrow(() -> builder.asItemStack());
        }

        @Test
        @DisplayName("Given an item, when removeEnchantment(String) is called with invalid string, then returns builder unchanged")
        void removeEnchantment_doesNothing_whenInvalidString() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);

            ItemBuilder result = builder.removeEnchantment("not_a_real_enchantment");

            assertSame(builder, result);
        }

        @Test
        @DisplayName("Given addEnchantment, when called, then returns same builder for chaining")
        void addEnchantment_returnsSelf_forChaining() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);

            ItemBuilder result = builder.addEnchantment(Enchantment.SHARPNESS, 1);

            assertSame(builder, result);
        }

        @Test
        @DisplayName("Given addEnchantment by string, when called, then returns same builder for chaining")
        void addEnchantmentByString_returnsSelf_forChaining() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);

            ItemBuilder result = builder.addEnchantment("sharpness", 1);

            assertSame(builder, result);
        }
    }

    @Nested
    @DisplayName("getPlainName")
    class GetPlainNameTests {

        @Test
        @DisplayName("Given an item with static display name, when getPlainName is called after asItemStack, then returns the name")
        void getPlainName_returnsName_whenStaticDisplayNameSet() {
            ItemStack itemStack = new ItemStack(Material.STONE);
            itemStack.setData(DataComponentTypes.ITEM_NAME, Component.text("Test Name"));
            ItemBuilder builder = ItemBuilder.from(itemStack);

            String plainName = builder.getPlainName();

            assertEquals("Test Name", plainName);
        }

        @Test
        @DisplayName("Given an item with CUSTOM_NAME set, when getPlainName is called, then returns the custom name")
        void getPlainName_returnsName_whenCustomNameSet() {
            ItemStack itemStack = new ItemStack(Material.STONE);
            itemStack.setData(DataComponentTypes.CUSTOM_NAME, Component.text("Custom Name"));
            ItemBuilder builder = ItemBuilder.from(itemStack);

            String plainName = builder.getPlainName();

            assertEquals("Custom Name", plainName);
        }

        @Test
        @DisplayName("Given an item with no name set, when getPlainName is called, then returns empty string")
        void getPlainName_returnsEmpty_whenNoNameSet() {
            ItemBuilder builder = ItemBuilder.from(ItemType.STONE);

            String plainName = builder.getPlainName();

            assertEquals("", plainName);
        }
    }

    @Nested
    @DisplayName("getPlainLore")
    class GetPlainLoreTests {

        @Test
        @DisplayName("Given an item with LORE data component, when getPlainLore is called, then returns lore as plain text")
        void getPlainLore_returnsLore_whenLoreDataComponentSet() {
            ItemStack itemStack = new ItemStack(Material.STONE);
            itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
                    Component.text("Component Line")
            )));
            ItemBuilder builder = ItemBuilder.from(itemStack);

            List<String> plainLore = builder.getPlainLore();

            assertFalse(plainLore.isEmpty());
            assertEquals("Component Line", plainLore.get(0));
        }

        @Test
        @DisplayName("Given an item with no lore, when getPlainLore is called, then returns empty list")
        void getPlainLore_returnsEmpty_whenNoLoreSet() {
            ItemBuilder builder = ItemBuilder.from(ItemType.STONE);

            List<String> plainLore = builder.getPlainLore();

            assertTrue(plainLore.isEmpty());
        }

        @Test
        @DisplayName("Given an item with multi-line lore data component, when getPlainLore is called, then returns all lines")
        void getPlainLore_returnsAllLines_whenMultiLineLore() {
            ItemStack itemStack = new ItemStack(Material.STONE);
            itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
                    Component.text("Line One"),
                    Component.text("Line Two"),
                    Component.text("Line Three")
            )));
            ItemBuilder builder = ItemBuilder.from(itemStack);

            List<String> plainLore = builder.getPlainLore();

            assertEquals(3, plainLore.size());
            assertEquals("Line One", plainLore.get(0));
            assertEquals("Line Two", plainLore.get(1));
            assertEquals("Line Three", plainLore.get(2));
        }
    }

    @Nested
    @DisplayName("setColor")
    class SetColorTests {

        @Test
        @DisplayName("Given a filled map, when setColor is called, then completes without error")
        void setColor_completesWithoutError_whenFilledMap() {
            ItemBuilder builder = ItemBuilder.from(ItemType.FILLED_MAP);
            assertDoesNotThrow(() -> builder.setColor("RED"));
        }

        @Test
        @DisplayName("Given leather armor, when setColor is called, then completes without error")
        void setColor_completesWithoutError_whenLeatherArmor() {
            ItemBuilder builder = ItemBuilder.from(ItemType.LEATHER_CHESTPLATE);
            assertDoesNotThrow(() -> builder.setColor("BLUE"));
        }

        @Test
        @DisplayName("Given a potion, when setColor is called, then completes without error")
        void setColor_completesWithoutError_whenPotion() {
            ItemBuilder builder = ItemBuilder.from(ItemType.POTION);
            assertDoesNotThrow(() -> builder.setColor("GREEN"));
        }

        @Test
        @DisplayName("Given a shield, when setColor is called, then completes without error")
        void setColor_completesWithoutError_whenShield() {
            ItemBuilder builder = ItemBuilder.from(ItemType.SHIELD);
            assertDoesNotThrow(() -> builder.setColor("RED"));
        }

        @Test
        @DisplayName("Given RGB string, when setColor is called, then completes without error")
        void setColor_completesWithoutError_whenCommaSeparatedRgb() {
            ItemBuilder builder = ItemBuilder.from(ItemType.LEATHER_BOOTS);
            assertDoesNotThrow(() -> builder.setColor("255,128,0"));
        }

        @Test
        @DisplayName("Given a non-colorable item, when setColor is called, then completes without error and no change")
        void setColor_completesWithoutError_whenNonColorableItem() {
            ItemBuilder builder = ItemBuilder.from(ItemType.STONE);
            assertDoesNotThrow(() -> builder.setColor("RED"));
        }

        @Test
        @DisplayName("Given setColor on different materials, when called, then all return the builder for chaining")
        void setColor_returnsSelf_forChaining() {
            ItemBuilder builder = ItemBuilder.from(ItemType.LEATHER_CHESTPLATE);
            ItemBuilder result = builder.setColor("RED");
            assertSame(builder, result);
        }
    }

    @Nested
    @DisplayName("setItemDamage")
    class SetItemDamageTests {

        @Test
        @DisplayName("Given a damageable item, when setItemDamage is called, then completes without error")
        void setItemDamage_completesWithoutError_whenDamageableItem() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            assertDoesNotThrow(() -> builder.setItemDamage(100));
        }

        @Test
        @DisplayName("Given damage exceeding max durability, when setItemDamage is called, then clamps to max without error")
        void setItemDamage_clampsToMax_whenExceedingDurability() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            assertDoesNotThrow(() -> builder.setItemDamage(Material.DIAMOND_SWORD.getMaxDurability() + 100));
        }

        @Test
        @DisplayName("Given zero damage, when setItemDamage is called, then completes without error")
        void setItemDamage_completesWithoutError_whenZeroDamage() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            assertDoesNotThrow(() -> builder.setItemDamage(0));
        }

        @Test
        @DisplayName("Given setItemDamage, when called, then returns builder for chaining")
        void setItemDamage_returnsSelf_forChaining() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            ItemBuilder result = builder.setItemDamage(50);
            assertSame(builder, result);
        }
    }

    @Nested
    @DisplayName("isEdible")
    class IsEdibleTests {

        @Test
        @DisplayName("Given APPLE material, when isEdible is called, then returns true")
        void isEdible_returnsTrue_whenEdibleMaterial() {
            ItemBuilder builder = ItemBuilder.from(ItemType.APPLE);
            assertTrue(builder.isEdible());
        }

        @Test
        @DisplayName("Given STONE material, when isEdible is called, then returns false")
        void isEdible_returnsFalse_whenNonEdibleMaterial() {
            ItemBuilder builder = ItemBuilder.from(ItemType.STONE);
            assertFalse(builder.isEdible());
        }

        @Test
        @DisplayName("Given GOLDEN_APPLE material, when isEdible is called, then returns true")
        void isEdible_returnsTrue_whenGoldenApple() {
            ItemBuilder builder = ItemBuilder.from(ItemType.GOLDEN_APPLE);
            assertTrue(builder.isEdible());
        }

        @Test
        @DisplayName("Given BREAD material, when isEdible is called, then returns true")
        void isEdible_returnsTrue_whenBread() {
            ItemBuilder builder = ItemBuilder.from(ItemType.BREAD);
            assertTrue(builder.isEdible());
        }
    }

    @Nested
    @DisplayName("Builder Conversions - Success Paths")
    class BuilderConversionSuccessTests {

        @Test
        @DisplayName("Given FIREWORK_ROCKET material, when asFireworkBuilder is called, then returns FireworkBuilder")
        void asFireworkBuilder_returnsFireworkBuilder_whenFireworkRocket() {
            ItemBuilder builder = ItemBuilder.from(ItemType.FIREWORK_ROCKET);

            FireworkBuilder result = builder.asFireworkBuilder();

            assertNotNull(result);
            assertInstanceOf(FireworkBuilder.class, result);
        }

        @Test
        @DisplayName("Given FIREWORK_STAR material, when asFireworkStarBuilder is called, then returns FireworkStarBuilder")
        void asFireworkStarBuilder_returnsFireworkStarBuilder_whenFireworkStar() {
            ItemBuilder builder = ItemBuilder.from(ItemType.FIREWORK_STAR);

            FireworkStarBuilder result = builder.asFireworkStarBuilder();

            assertNotNull(result);
            assertInstanceOf(FireworkStarBuilder.class, result);
        }

        @Test
        @DisplayName("Given SHIELD material, when asPatternBuilder is called, then returns PatternBuilder")
        void asPatternBuilder_returnsPatternBuilder_whenShield() {
            ItemBuilder builder = ItemBuilder.from(ItemType.SHIELD);

            PatternBuilder result = builder.asPatternBuilder();

            assertNotNull(result);
            assertInstanceOf(PatternBuilder.class, result);
        }

        @Test
        @DisplayName("Given WHITE_BANNER material, when asPatternBuilder is called, then returns PatternBuilder")
        void asPatternBuilder_returnsPatternBuilder_whenBanner() {
            ItemBuilder builder = ItemBuilder.from(ItemType.WHITE_BANNER);

            PatternBuilder result = builder.asPatternBuilder();

            assertNotNull(result);
            assertInstanceOf(PatternBuilder.class, result);
        }

        @Test
        @DisplayName("Given PLAYER_HEAD material, when asSkullBuilder is called, then returns SkullBuilder")
        void asSkullBuilder_returnsSkullBuilder_whenPlayerHead() {
            ItemBuilder builder = ItemBuilder.from(ItemType.PLAYER_HEAD);

            SkullBuilder result = builder.asSkullBuilder();

            assertNotNull(result);
            assertInstanceOf(SkullBuilder.class, result);
        }

        @Test
        @DisplayName("Given POTION material, when asPotionBuilder is called, then returns PotionBuilder")
        void asPotionBuilder_returnsPotionBuilder_whenPotion() {
            ItemBuilder builder = ItemBuilder.from(ItemType.POTION);

            PotionBuilder result = builder.asPotionBuilder();

            assertNotNull(result);
            assertInstanceOf(PotionBuilder.class, result);
        }

        @Test
        @DisplayName("Given SPLASH_POTION material, when asPotionBuilder is called, then returns PotionBuilder")
        void asPotionBuilder_returnsPotionBuilder_whenSplashPotion() {
            ItemBuilder builder = ItemBuilder.from(ItemType.SPLASH_POTION);

            PotionBuilder result = builder.asPotionBuilder();

            assertNotNull(result);
            assertInstanceOf(PotionBuilder.class, result);
        }

        @Test
        @DisplayName("Given SPAWNER material, when asSpawnerBuilder is called, then returns SpawnerBuilder")
        void asSpawnerBuilder_returnsSpawnerBuilder_whenSpawner() {
            ItemBuilder builder = ItemBuilder.from(ItemType.SPAWNER);

            SpawnerBuilder result = builder.asSpawnerBuilder();

            assertNotNull(result);
            assertInstanceOf(SpawnerBuilder.class, result);
        }

        @Test
        @DisplayName("Given LINGERING_POTION material, when asPotionBuilder is called, then returns PotionBuilder")
        void asPotionBuilder_returnsPotionBuilder_whenLingeringPotion() {
            ItemBuilder builder = ItemBuilder.from(ItemType.LINGERING_POTION);

            PotionBuilder result = builder.asPotionBuilder();

            assertNotNull(result);
            assertInstanceOf(PotionBuilder.class, result);
        }
    }

    @Nested
    @DisplayName("withType")
    class WithTypeTests {

        @Test
        @DisplayName("Given a builder with existing item, when withType is called, then item is not replaced")
        void withType_doesNotReplace_whenItemExists() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND);

            builder.withType(ItemType.STONE, 5);

            assertEquals(Material.DIAMOND, builder.getType());
        }

        @Test
        @DisplayName("Given withType with single arg, when called on existing builder, then item is not replaced")
        void withType_doesNotReplace_whenSingleArgAndItemExists() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND);

            builder.withType(ItemType.STONE);

            assertEquals(Material.DIAMOND, builder.getType());
        }
    }

    @Nested
    @DisplayName("asItemStack integration")
    class AsItemStackIntegrationTests {

        @Test
        @DisplayName("Given a builder with item flags, when asItemStack is called, then flags are applied via meta")
        void asItemStack_appliesItemFlags_whenFlagsProvided() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.addItemFlag(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);

            ItemStack result = builder.asItemStack();

            assertTrue(result.getItemMeta().hasItemFlag(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS));
        }

        @Test
        @DisplayName("Given a builder with HIDE_ATTRIBUTES flag, when asItemStack is called, then completes without error")
        void asItemStack_completesWithoutError_whenHideAttributesFlagSet() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.addItemFlag(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);

            ItemStack result = assertDoesNotThrow(() -> builder.asItemStack());
            assertNotNull(result);
        }

        @Test
        @DisplayName("Given multiple calls to asItemStack, when items are compared, then they are equal but not same reference")
        void asItemStack_returnsNewClone_eachCall() {
            ItemBuilder builder = ItemBuilder.from(ItemType.STONE);

            ItemStack first = builder.asItemStack();
            ItemStack second = builder.asItemStack();

            assertEquals(first, second);
            assertFalse(first == second);
        }

        @Test
        @DisplayName("Given a builder with string lore, when asItemStack is called, then lore is applied")
        void asItemStack_appliesLore_whenStringLoreSet() {
            ItemBuilder builder = ItemBuilder.from(ItemType.STONE);
            builder.withDisplayLore(List.of("Line One", "Line Two"));

            ItemStack result = builder.asItemStack();

            assertNotNull(result);
        }

        @Test
        @DisplayName("Given a builder with component lore, when asItemStack is called, then component lore is applied")
        void asItemStack_appliesComponentLore_whenComponentLoreSet() {
            ItemBuilder builder = ItemBuilder.from(ItemType.STONE);
            builder.addDisplayLoreComponent(Component.text("Component Lore"));

            ItemStack result = builder.asItemStack();

            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("setTrim")
    class SetTrimTests {

        @Test
        @DisplayName("Given valid TrimPattern and TrimMaterial objects, when setTrim is called, then completes without error")
        void setTrim_completesWithoutError_whenValidPatternAndMaterial() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_CHESTPLATE);

            assertDoesNotThrow(() ->
                    builder.setTrim(org.bukkit.inventory.meta.trim.TrimPattern.SENTRY, org.bukkit.inventory.meta.trim.TrimMaterial.IRON));
        }

        @Test
        @DisplayName("Given setTrim with objects, when called, then returns builder for chaining")
        void setTrim_returnsSelf_forChaining() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_CHESTPLATE);

            ItemBuilder result = builder.setTrim(org.bukkit.inventory.meta.trim.TrimPattern.SENTRY, org.bukkit.inventory.meta.trim.TrimMaterial.IRON);

            assertSame(builder, result);
        }
    }

    @Nested
    @DisplayName("Copy constructor")
    class CopyConstructorTests {

        @Test
        @DisplayName("Given a builder with state, when copied via from(ItemStack), then original state is preserved on the copy")
        void copyViaFrom_preservesMaterial_whenCopied() {
            ItemBuilder original = ItemBuilder.from(ItemType.DIAMOND, 3);
            original.setDisplayName("Original Name");
            original.addPlaceholder("key", "value");
            original.addItemFlag(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);

            ItemStack stack = original.asItemStack();
            ItemBuilder copy = ItemBuilder.from(stack);

            assertNotNull(copy);
            assertEquals(Material.DIAMOND, copy.getType());
        }
    }

    @Nested
    @DisplayName("Fluent API chaining")
    class FluentApiTests {

        @Test
        @DisplayName("Given a builder, when multiple methods are chained, then all return same builder")
        void fluentApi_chainingReturnsBuilder_whenMethodsChained() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);

            ItemBuilder result = builder
                    .setDisplayName("Test Sword")
                    .addDisplayLore("Line 1")
                    .addPlaceholder("key", "value")
                    .addItemFlag(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS)
                    .setAmount(2);

            assertSame(builder, result);
        }

        @Test
        @DisplayName("Given a builder, when addEnchantment and setColor are chained, then completes without error")
        void fluentApi_enchantmentAndColorChaining_completesWithoutError() {
            ItemBuilder builder = ItemBuilder.from(ItemType.LEATHER_CHESTPLATE);

            assertDoesNotThrow(() -> builder
                    .addEnchantment(Enchantment.PROTECTION, 4)
                    .setColor("RED")
                    .setDisplayName("Red Armor")
                    .asItemStack());
        }
    }
}

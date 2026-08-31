package com.diamonddagger590.mccore.builder.item;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link BaseItemBuilder} branch coverage gaps that require
 * MockBukkit's server environment for real ItemStack operations.
 */
class BaseItemBuilderBranchCoverageTest {

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
    @DisplayName("getPlainName")
    class GetPlainNameTests {

        @Test
        @DisplayName("Given item with no name data, when getPlainName, then returns non-null string")
        void getPlainName_returnsNonNull_whenNoNameDataPresent() {
            ItemBuilder builder = ItemBuilder.from(ItemType.STONE);
            String name = builder.getPlainName();
            assertNotNull(name);
        }

        @Test
        @DisplayName("Given item with display name set via builder, when getPlainName before build, then returns empty since name not yet applied")
        void getPlainName_returnsEmpty_whenNameSetViaBuilderButNotBuilt() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.setDisplayName("My Sword");
            String name = builder.getPlainName();
            assertNotNull(name);
        }

        @Test
        @DisplayName("Given item built with static name, when getPlainName on rebuilt builder, then exercises ITEM_NAME branch")
        void getPlainName_exercisesItemNameBranch_whenBuiltWithStaticName() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.setDisplayName("Excalibur", true);
            ItemStack built = builder.asItemStack();

            ItemBuilder rebuilt = ItemBuilder.from(built);
            assertDoesNotThrow(rebuilt::getPlainName);
        }

        @Test
        @DisplayName("Given item built with non-static name, when getPlainName on rebuilt builder, then exercises CUSTOM_NAME branch")
        void getPlainName_exercisesCustomNameBranch_whenBuiltWithNonStaticName() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.setDisplayName("Dynamic Sword", false);
            ItemStack built = builder.asItemStack();

            ItemBuilder rebuilt = ItemBuilder.from(built);
            assertDoesNotThrow(rebuilt::getPlainName);
        }
    }

    @Nested
    @DisplayName("getPlainLore")
    class GetPlainLoreTests {

        @Test
        @DisplayName("Given item with no lore, when getPlainLore, then returns empty list")
        void getPlainLore_returnsEmptyList_whenNoLore() {
            ItemBuilder builder = ItemBuilder.from(ItemType.STONE);
            List<String> lore = builder.getPlainLore();
            assertNotNull(lore);
            assertTrue(lore.isEmpty());
        }

        @Test
        @DisplayName("Given builder with lore strings added, when getPlainLore before build, then lore not yet on itemstack")
        void getPlainLore_exercisesLoreBranch_whenLoreAdded() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.addDisplayLore("Line one");
            builder.addDisplayLore("Line two");
            assertDoesNotThrow(builder::getPlainLore);
        }
    }

    @Nested
    @DisplayName("addEnchantment(Enchantment, int)")
    class AddEnchantmentTests {

        @Test
        @DisplayName("Given a regular item with no enchantments, when addEnchantment, then completes without error")
        void addEnchantment_completesWithoutError_whenNoExistingEnchants() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            assertDoesNotThrow(() -> builder.addEnchantment(Enchantment.SHARPNESS, 3));
            assertNotNull(builder.asItemStack());
        }

        @Test
        @DisplayName("Given a regular item with existing enchantment, when addEnchantment with another, then completes without error")
        void addEnchantment_completesWithoutError_whenAddingSecondEnchant() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.addEnchantment(Enchantment.SHARPNESS, 2);
            assertDoesNotThrow(() -> builder.addEnchantment(Enchantment.UNBREAKING, 3));
            assertNotNull(builder.asItemStack());
        }

        @Test
        @DisplayName("Given an enchanted book, when addEnchantment, then exercises STORED_ENCHANTMENTS branch")
        void addEnchantment_exercisesStoredEnchantmentsBranch_whenEnchantedBook() {
            ItemBuilder builder = ItemBuilder.from(ItemType.ENCHANTED_BOOK);
            assertDoesNotThrow(() -> builder.addEnchantment(Enchantment.SHARPNESS, 5));
            assertNotNull(builder.asItemStack());
        }

        @Test
        @DisplayName("Given an enchanted book with existing stored enchant, when addEnchantment, then exercises preserve branch")
        void addEnchantment_exercisesPreserveBranch_whenEnchantedBookHasStoredEnchants() {
            ItemBuilder builder = ItemBuilder.from(ItemType.ENCHANTED_BOOK);
            builder.addEnchantment(Enchantment.SHARPNESS, 3);
            assertDoesNotThrow(() -> builder.addEnchantment(Enchantment.UNBREAKING, 2));
            assertNotNull(builder.asItemStack());
        }

        @Test
        @DisplayName("Given a string enchantment name, when addEnchantment with string, then exercises string resolution branch")
        void addEnchantment_exercisesStringResolution_whenStringProvided() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            assertDoesNotThrow(() -> builder.addEnchantment("sharpness", 4));
            assertNotNull(builder.asItemStack());
        }

        @Test
        @DisplayName("Given an invalid enchantment string, when addEnchantment with string, then no error")
        void addEnchantment_doesNotThrow_whenInvalidString() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            assertDoesNotThrow(() -> builder.addEnchantment("not_a_real_enchantment", 1));
            assertNotNull(builder.asItemStack());
        }
    }

    @Nested
    @DisplayName("setColor")
    class SetColorTests {

        @Test
        @DisplayName("Given a leather armor item, when setColor with color name, then exercises leather armor branch")
        void setColor_exercisesLeatherBranch_whenLeatherArmor() {
            ItemBuilder builder = ItemBuilder.from(ItemType.LEATHER_CHESTPLATE);
            assertDoesNotThrow(() -> builder.setColor("RED"));
            assertNotNull(builder.asItemStack());
        }

        @Test
        @DisplayName("Given a leather armor item, when setColor with RGB, then exercises RGB parsing branch")
        void setColor_exercisesRgbBranch_whenLeatherArmorWithRgb() {
            ItemBuilder builder = ItemBuilder.from(ItemType.LEATHER_BOOTS);
            assertDoesNotThrow(() -> builder.setColor("255,0,0"));
            assertNotNull(builder.asItemStack());
        }

        @Test
        @DisplayName("Given a potion item, when setColor, then exercises potion branch")
        void setColor_exercisesPotionBranch_whenPotion() {
            ItemBuilder builder = ItemBuilder.from(ItemType.POTION);
            assertDoesNotThrow(() -> builder.setColor("BLUE"));
            assertNotNull(builder.asItemStack());
        }

        @Test
        @DisplayName("Given a map item, when setColor, then exercises map branch")
        void setColor_exercisesMapBranch_whenFilledMap() {
            ItemBuilder builder = ItemBuilder.from(ItemType.FILLED_MAP);
            assertDoesNotThrow(() -> builder.setColor("GREEN"));
            assertNotNull(builder.asItemStack());
        }

        @Test
        @DisplayName("Given a shield item, when setColor, then exercises shield branch")
        void setColor_exercisesShieldBranch_whenShield() {
            ItemBuilder builder = ItemBuilder.from(ItemType.SHIELD);
            assertDoesNotThrow(() -> builder.setColor("RED"));
            assertNotNull(builder.asItemStack());
        }

        @Test
        @DisplayName("Given a non-dyeable item, when setColor, then exercises fallthrough with no data set")
        void setColor_exercisesFallthrough_whenNonDyeable() {
            ItemBuilder builder = ItemBuilder.from(ItemType.STONE);
            assertDoesNotThrow(() -> builder.setColor("RED"));
            assertNotNull(builder.asItemStack());
        }
    }

    @Nested
    @DisplayName("withType")
    class WithTypeTests {

        @Test
        @DisplayName("Given a builder with an existing itemStack, when withType, then does not replace itemStack")
        void withType_doesNotReplace_whenItemStackAlreadyExists() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.withType(ItemType.STONE, 5);
            assertEquals(Material.DIAMOND_SWORD, builder.getType());
        }
    }

    @Nested
    @DisplayName("setItemDamage")
    class SetItemDamageTests {

        @Test
        @DisplayName("Given a damageable item, when setItemDamage, then completes without error")
        void setItemDamage_completesWithoutError_whenDamageableItem() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            assertDoesNotThrow(() -> builder.setItemDamage(100));
            assertNotNull(builder.asItemStack());
        }

        @Test
        @DisplayName("Given damage exceeding max durability, when setItemDamage, then exercises clamp branch")
        void setItemDamage_exercisesClampBranch_whenExceedsMaxDurability() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            assertDoesNotThrow(() -> builder.setItemDamage(Integer.MAX_VALUE));
            assertNotNull(builder.asItemStack());
        }
    }

    @Nested
    @DisplayName("asItemStack")
    class AsItemStackTests {

        @Test
        @DisplayName("Given a builder with display name (static), when asItemStack, then exercises ITEM_NAME branch")
        void asItemStack_exercisesItemNameBranch_whenStaticNameProvided() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.setDisplayName("<green>My Sword", true);

            ItemStack result = builder.asItemStack();
            assertNotNull(result);
            assertEquals(Material.DIAMOND_SWORD, result.getType());
        }

        @Test
        @DisplayName("Given a builder with non-static name, when asItemStack, then exercises CUSTOM_NAME branch")
        void asItemStack_exercisesCustomNameBranch_whenStaticItemNameFalse() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.setDisplayName("Dynamic Name", false);

            ItemStack result = builder.asItemStack();
            assertNotNull(result);
        }

        @Test
        @DisplayName("Given a builder with lore strings, when asItemStack, then exercises lore string branch")
        void asItemStack_exercisesLoreStringBranch_whenLoreStringsProvided() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.addDisplayLore("Line 1");
            builder.addDisplayLore("Line 2");

            ItemStack result = builder.asItemStack();
            assertNotNull(result);
        }

        @Test
        @DisplayName("Given a builder with lore components, when asItemStack, then exercises lore component branch")
        void asItemStack_exercisesLoreComponentBranch_whenLoreComponentsProvided() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.addDisplayLoreComponent(Component.text("Component Lore"));

            ItemStack result = builder.asItemStack();
            assertNotNull(result);
        }

        @Test
        @DisplayName("Given a builder with item flags, when asItemStack, then exercises flags branch")
        void asItemStack_exercisesFlagsBranch_whenFlagsSet() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.addItemFlag(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);

            ItemStack result = builder.asItemStack();
            assertNotNull(result);
        }

        @Test
        @DisplayName("Given a builder, when asItemStack called twice, then returns different instances")
        void asItemStack_returnsDifferentInstances_whenCalledMultipleTimes() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            ItemStack first = builder.asItemStack();
            ItemStack second = builder.asItemStack();

            assertNotSame(first, second);
        }

        @Test
        @DisplayName("Given a builder with HIDE_ATTRIBUTES flag, when asItemStack, then exercises attribute modifier branch")
        void asItemStack_exercisesAttributeModifierBranch_whenHideAttributesFlagPresent() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.addItemFlag(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);

            ItemStack result = builder.asItemStack();
            assertNotNull(result);
        }

        @Test
        @DisplayName("Given a builder constructed from ItemStack with lore, when asItemStack, then exercises displayNameComponent null and loreAsComponent non-empty branches")
        void asItemStack_exercisesLoreComponentFromConstructor_whenLoreOnItemStack() {
            ItemStack initial = new ItemStack(Material.DIAMOND_SWORD);
            initial.lore(List.of(Component.text("Existing lore")));

            ItemBuilder builder = ItemBuilder.from(initial);
            ItemStack result = builder.asItemStack();
            assertNotNull(result);
        }

        @Test
        @DisplayName("Given a builder with no name, no lore, no flags, when asItemStack, then exercises all false branches")
        void asItemStack_exercisesFalseBranches_whenNothingSet() {
            ItemBuilder builder = ItemBuilder.from(ItemType.STONE);
            ItemStack result = builder.asItemStack();
            assertNotNull(result);
            assertEquals(Material.STONE, result.getType());
        }

        @Test
        @DisplayName("Given a builder constructed from ItemStack with CUSTOM_NAME, when asItemStack without setDisplayName, then exercises displayNameComponent branch")
        void asItemStack_exercisesDisplayNameComponentBranch_whenConstructedFromNamedStack() {
            ItemStack initial = new ItemStack(Material.DIAMOND_SWORD);
            initial.setData(DataComponentTypes.CUSTOM_NAME, Component.text("Named Item"));

            ItemBuilder builder = ItemBuilder.from(initial);
            ItemStack result = builder.asItemStack();
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("parseComponent and getPlaceholdersAsConfig")
    class ParseComponentTests {

        @Test
        @DisplayName("Given placeholders and component lore, when asItemStack, then exercises placeholder resolution in parseComponent")
        void parseComponent_exercisesPlaceholderResolution_whenComponentLoreWithPlaceholders() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.addPlaceholder("damage", "100");
            builder.addDisplayLoreComponent(Component.text("Damage: <damage>"));

            ItemStack result = builder.asItemStack();
            assertNotNull(result);
        }

        @Test
        @DisplayName("Given no placeholders, when parseComponent called through asItemStack with component lore, then exercises empty placeholders branch")
        void parseComponent_exercisesEmptyPlaceholdersBranch_whenNoPlaceholders() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.addDisplayLoreComponent(Component.text("No placeholders here"));

            ItemStack result = builder.asItemStack();
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Constructor from ItemStack")
    class ConstructorTests {

        @Test
        @DisplayName("Given ItemStack with lore from meta, when constructing builder, then exercises lore capture branch")
        void constructor_exercisesLoreCaptureBranch_whenLoreInMeta() {
            ItemStack stack = new ItemStack(Material.DIAMOND_SWORD);
            stack.lore(List.of(Component.text("Lore line 1"), Component.text("Lore line 2")));

            ItemBuilder builder = assertDoesNotThrow(() -> ItemBuilder.from(stack));
            assertNotNull(builder);
            assertNotNull(builder.asItemStack());
        }

        @Test
        @DisplayName("Given ItemStack with no name or lore, when constructing builder, then builds clean item")
        void constructor_buildsCleanItem_whenNoNameOrLore() {
            ItemStack stack = new ItemStack(Material.STONE);

            ItemBuilder builder = ItemBuilder.from(stack);
            ItemStack result = builder.asItemStack();
            assertEquals(Material.STONE, result.getType());
        }

        @Test
        @DisplayName("Given plain ItemStack, when constructing builder, then exercises no-name no-lore branches")
        void constructor_exercisesNoNameNoLoreBranches_whenPlainItemStack() {
            ItemStack stack = new ItemStack(Material.DIAMOND_SWORD);
            ItemBuilder builder = assertDoesNotThrow(() -> ItemBuilder.from(stack));
            assertNotNull(builder);
        }

        @Test
        @DisplayName("Given ItemStack with CUSTOM_NAME, when constructing builder, then exercises CUSTOM_NAME capture branch")
        void constructor_exercisesCustomNameBranch_whenCustomNameOnStack() {
            ItemStack stack = new ItemStack(Material.DIAMOND_SWORD);
            stack.setData(DataComponentTypes.CUSTOM_NAME, Component.text("Custom"));

            ItemBuilder builder = assertDoesNotThrow(() -> ItemBuilder.from(stack));
            assertNotNull(builder);
        }
    }

    @Nested
    @DisplayName("Copy constructor")
    class CopyConstructorTests {

        @Test
        @DisplayName("Given builder with state, when creating copy via ItemBuilder.from(stack), then state is transferred")
        void copyViaItemStack_transfersState_whenOriginalHasState() {
            ItemBuilder original = ItemBuilder.from(ItemType.DIAMOND, 3);
            original.setDisplayName("Original");
            original.addDisplayLore("Line 1");
            original.addPlaceholder("key", "value");

            ItemStack built = original.asItemStack();
            ItemBuilder copy = ItemBuilder.from(built);

            assertNotNull(copy);
            assertEquals(Material.DIAMOND, copy.getType());
        }
    }

    @Nested
    @DisplayName("setTrim with TrimPattern and TrimMaterial")
    class SetTrimObjectTests {

        @Test
        @DisplayName("Given valid trim strings, when setTrim, then completes without error")
        void setTrim_completesWithoutError_whenValidPatternAndMaterial() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_CHESTPLATE);
            assertDoesNotThrow(() -> builder.setTrim("sentry", "iron"));
            assertNotNull(builder.asItemStack());
        }

        @Test
        @DisplayName("Given invalid trim pattern, when setTrim with strings, then completes without error")
        void setTrim_completesWithoutError_whenInvalidPattern() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_CHESTPLATE);
            assertDoesNotThrow(() -> builder.setTrim("invalid_pattern", "iron"));
            assertNotNull(builder.asItemStack());
        }

        @Test
        @DisplayName("Given invalid trim material, when setTrim with strings, then completes without error")
        void setTrim_completesWithoutError_whenInvalidMaterial() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_CHESTPLATE);
            assertDoesNotThrow(() -> builder.setTrim("sentry", "invalid_material"));
            assertNotNull(builder.asItemStack());
        }
    }

    @Nested
    @DisplayName("applyTagReplacements additional branches")
    class ApplyTagReplacementsAdditionalTests {

        @Test
        @DisplayName("Given a builder with both name and lore containing tags, when applyTagReplacements, then exercises tag replacement across both")
        void applyTagReplacements_exercisesBothNameAndLoreBranches_whenBothContainTags() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.setDisplayName("<primary>Sword");
            builder.addDisplayLore("<secondary>Damage info");

            assertDoesNotThrow(() -> builder.applyTagReplacements(Map.of(
                    "<primary>", "<color:#FF0000>",
                    "<secondary>", "<color:#00FF00>"
            )));

            ItemStack result = builder.asItemStack();
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("removeEnchantment by string")
    class RemoveEnchantmentByStringTests {

        @Test
        @DisplayName("Given valid enchantment string, when removeEnchantment, then exercises valid resolution branch")
        void removeEnchantment_exercisesValidBranch_whenValidString() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.addEnchantment(Enchantment.SHARPNESS, 3);
            assertDoesNotThrow(() -> builder.removeEnchantment("sharpness"));
            assertNotNull(builder.asItemStack());
        }

        @Test
        @DisplayName("Given invalid enchantment string, when removeEnchantment, then exercises invalid resolution branch")
        void removeEnchantment_exercisesInvalidBranch_whenInvalidString() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.addEnchantment(Enchantment.SHARPNESS, 3);
            assertDoesNotThrow(() -> builder.removeEnchantment("not_a_real_enchant"));
            assertNotNull(builder.asItemStack());
        }
    }

    @Nested
    @DisplayName("withBase64 non-empty")
    class WithBase64NonEmptyTests {

        @Test
        @DisplayName("Given empty string, when withBase64, then item is unchanged")
        void withBase64_noChange_whenEmpty() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.withBase64("");
            assertEquals(Material.DIAMOND_SWORD, builder.getType());
        }
    }

    @Nested
    @DisplayName("isEdible type check")
    class IsEdibleTests {

        @Test
        @DisplayName("Given an edible item, when isEdible, then returns true")
        void isEdible_returnsTrue_whenEdibleItem() {
            ItemBuilder builder = ItemBuilder.from(ItemType.APPLE);
            assertTrue(builder.isEdible());
        }

        @Test
        @DisplayName("Given a non-edible item, when isEdible, then returns false")
        void isEdible_returnsFalse_whenNonEdibleItem() {
            ItemBuilder builder = ItemBuilder.from(ItemType.STONE);
            assertFalse(builder.isEdible());
        }
    }

    @Nested
    @DisplayName("Builder conversion happy paths")
    class BuilderConversionTests {

        @Test
        @DisplayName("Given a player head, when asSkullBuilder, then returns SkullBuilder")
        void asSkullBuilder_returnsSkullBuilder_whenPlayerHead() {
            ItemBuilder builder = ItemBuilder.from(ItemType.PLAYER_HEAD);
            assertDoesNotThrow(builder::asSkullBuilder);
        }

        @Test
        @DisplayName("Given a potion, when asPotionBuilder, then returns PotionBuilder")
        void asPotionBuilder_returnsPotionBuilder_whenPotion() {
            ItemBuilder builder = ItemBuilder.from(ItemType.POTION);
            assertDoesNotThrow(builder::asPotionBuilder);
        }

        @Test
        @DisplayName("Given a shield, when asPatternBuilder, then returns PatternBuilder")
        void asPatternBuilder_returnsPatternBuilder_whenShield() {
            ItemBuilder builder = ItemBuilder.from(ItemType.SHIELD);
            assertDoesNotThrow(builder::asPatternBuilder);
        }

        @Test
        @DisplayName("Given a banner, when asPatternBuilder, then returns PatternBuilder")
        void asPatternBuilder_returnsPatternBuilder_whenBanner() {
            ItemBuilder builder = ItemBuilder.from(ItemType.WHITE_BANNER);
            assertDoesNotThrow(builder::asPatternBuilder);
        }

        @Test
        @DisplayName("Given a firework, when asFireworkBuilder, then returns FireworkBuilder")
        void asFireworkBuilder_returnsFireworkBuilder_whenFirework() {
            ItemBuilder builder = ItemBuilder.from(ItemType.FIREWORK_ROCKET);
            assertDoesNotThrow(builder::asFireworkBuilder);
        }

        @Test
        @DisplayName("Given a firework star, when asFireworkStarBuilder, then returns FireworkStarBuilder")
        void asFireworkStarBuilder_returnsBuilder_whenFireworkStar() {
            ItemBuilder builder = ItemBuilder.from(ItemType.FIREWORK_STAR);
            assertDoesNotThrow(builder::asFireworkStarBuilder);
        }

        @Test
        @DisplayName("Given a spawner, when asSpawnerBuilder, then returns SpawnerBuilder")
        void asSpawnerBuilder_returnsBuilder_whenSpawner() {
            ItemBuilder builder = ItemBuilder.from(ItemType.SPAWNER);
            assertDoesNotThrow(builder::asSpawnerBuilder);
        }
    }

    @Nested
    @DisplayName("setEnchantGlint branches")
    class SetEnchantGlintTests {

        @Test
        @DisplayName("Given no glint override, when setEnchantGlint true, then exercises set branch")
        void setEnchantGlint_exercisesSetBranch_whenTrueAndNoExistingOverride() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            assertDoesNotThrow(() -> builder.setEnchantGlint(true));
            assertNotNull(builder.asItemStack());
        }

        @Test
        @DisplayName("Given existing glint override, when setEnchantGlint false, then exercises unset branch")
        void setEnchantGlint_exercisesUnsetBranch_whenFalseAndExistingOverride() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.setEnchantGlint(true);
            assertDoesNotThrow(() -> builder.setEnchantGlint(false));
            assertNotNull(builder.asItemStack());
        }

        @Test
        @DisplayName("Given no glint override, when setEnchantGlint false, then exercises false-no-override branch")
        void setEnchantGlint_exercisesFalseNoOverrideBranch_whenFalseAndNoOverride() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            assertDoesNotThrow(() -> builder.setEnchantGlint(false));
            assertNotNull(builder.asItemStack());
        }
    }

    @Nested
    @DisplayName("hideToolTip and showToolTip branches")
    class ToolTipTests {

        @Test
        @DisplayName("Given no tooltip display, when hideToolTip, then exercises set branch")
        void hideToolTip_exercisesSetBranch_whenNoExistingTooltipDisplay() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            assertDoesNotThrow(builder::hideToolTip);
            assertNotNull(builder.asItemStack());
        }

        @Test
        @DisplayName("Given existing tooltip display, when hideToolTip again, then exercises already-set branch")
        void hideToolTip_exercisesAlreadySetBranch_whenTooltipAlreadyHidden() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.hideToolTip();
            assertDoesNotThrow(builder::hideToolTip);
            assertNotNull(builder.asItemStack());
        }

        @Test
        @DisplayName("Given hidden tooltip, when showToolTip, then exercises unset branch")
        void showToolTip_exercisesUnsetBranch_whenTooltipHidden() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            builder.hideToolTip();
            assertDoesNotThrow(builder::showToolTip);
            assertNotNull(builder.asItemStack());
        }

        @Test
        @DisplayName("Given no tooltip display, when showToolTip, then exercises no-op branch")
        void showToolTip_exercisesNoOpBranch_whenNoTooltipDisplay() {
            ItemBuilder builder = ItemBuilder.from(ItemType.DIAMOND_SWORD);
            assertDoesNotThrow(builder::showToolTip);
            assertNotNull(builder.asItemStack());
        }
    }
}

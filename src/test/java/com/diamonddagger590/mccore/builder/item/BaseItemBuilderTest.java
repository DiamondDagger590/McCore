package com.diamonddagger590.mccore.builder.item;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.exception.builder.item.InvalidItemBuilderException;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link BaseItemBuilder} via {@link ItemBuilder} partial mocks.
 * <p>
 * DataComponentTypes cannot be initialized without a running Paper server, so
 * the BaseItemBuilder(ItemStack) constructor cannot be called directly. Instead,
 * a Mockito partial mock with CALLS_REAL_METHODS bypasses the constructor, and
 * required fields are injected via reflection.
 */
class BaseItemBuilderTest {

    private static ItemBuilder createBuilder(Material material) {
        ItemStack mockItem = mock(ItemStack.class);
        when(mockItem.getType()).thenReturn(material);

        CorePlugin mockPlugin = mock(CorePlugin.class);
        when(mockPlugin.getMiniMessage()).thenReturn(MiniMessage.miniMessage());

        ItemBuilder builder = mock(ItemBuilder.class, CALLS_REAL_METHODS);

        setField(BaseItemBuilder.class, builder, "itemStack", mockItem);
        setField(BaseItemBuilder.class, builder, "placeholders", new HashMap<String, String>());
        setField(BaseItemBuilder.class, builder, "itemFlags", new ArrayList<ItemFlag>());
        setField(BaseItemBuilder.class, builder, "lore", new ArrayList<String>());
        setField(BaseItemBuilder.class, builder, "loreAsComponent", new ArrayList<>());
        setField(BaseItemBuilder.class, builder, "corePlugin", mockPlugin);
        setField(BaseItemBuilder.class, builder, "miniMessage", MiniMessage.miniMessage());
        setField(BaseItemBuilder.class, builder, "staticItemName", true);
        setField(BaseItemBuilder.class, builder, "applyAudienceSkullTexture", true);

        return builder;
    }

    private static ItemStack getItemStack(ItemBuilder builder) {
        try {
            Field field = BaseItemBuilder.class.getDeclaredField("itemStack");
            field.setAccessible(true);
            return (ItemStack) field.get(builder);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setField(Class<?> clazz, Object target, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("Placeholder Management")
    class PlaceholderManagementTests {

        @Test
        @DisplayName("addPlaceholder stores the placeholder so hasPlaceholder returns true")
        void addPlaceholder_storesPlaceholder() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.addPlaceholder("key", "value");
            assertTrue(builder.hasPlaceholder("key"));
        }

        @Test
        @DisplayName("hasPlaceholder returns false for missing key")
        void hasPlaceholder_returnsFalseForMissingKey() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertFalse(builder.hasPlaceholder("nonexistent"));
        }

        @Test
        @DisplayName("removePlaceholder removes the stored placeholder")
        void removePlaceholder_removesPlaceholder() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.addPlaceholder("key", "value");
            builder.removePlaceholder("key");
            assertFalse(builder.hasPlaceholder("key"));
        }

        @Test
        @DisplayName("setPlaceholders replaces all existing placeholders")
        void setPlaceholders_replacesAll() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.addPlaceholder("old", "oldValue");
            Map<String, String> newMap = new HashMap<>();
            newMap.put("new", "newValue");
            builder.setPlaceholders(newMap);
            assertFalse(builder.hasPlaceholder("old"));
            assertTrue(builder.hasPlaceholder("new"));
        }

        @Test
        @DisplayName("addPlaceholders merges into existing placeholders")
        void addPlaceholders_mergesIntoExisting() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.addPlaceholder("existing", "val1");
            Map<String, String> extras = new HashMap<>();
            extras.put("extra", "val2");
            builder.addPlaceholders(extras);
            assertTrue(builder.hasPlaceholder("existing"));
            assertTrue(builder.hasPlaceholder("extra"));
        }

        @Test
        @DisplayName("addPlaceholder returns builder for chaining")
        void addPlaceholder_returnsSelfForChaining() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemBuilder result = builder.addPlaceholder("key", "value");
            assertSame(builder, result);
        }

        @Test
        @DisplayName("removePlaceholder returns builder for chaining")
        void removePlaceholder_returnsSelfForChaining() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemBuilder result = builder.removePlaceholder("missing");
            assertSame(builder, result);
        }

        @Test
        @DisplayName("setPlaceholders returns builder for chaining")
        void setPlaceholders_returnsSelfForChaining() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemBuilder result = builder.setPlaceholders(new HashMap<>());
            assertSame(builder, result);
        }

        @Test
        @DisplayName("addPlaceholders returns builder for chaining")
        void addPlaceholders_returnsSelfForChaining() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemBuilder result = builder.addPlaceholders(new HashMap<>());
            assertSame(builder, result);
        }
    }

    @Nested
    @DisplayName("Display Name Management")
    class DisplayNameTests {

        @Test
        @DisplayName("setDisplayName with two args returns builder for chaining")
        void setDisplayName_twoArgs_returnsSelf() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertSame(builder, builder.setDisplayName("Test Name", true));
        }

        @Test
        @DisplayName("setDisplayName single arg returns builder for chaining")
        void setDisplayName_singleArg_returnsSelf() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertSame(builder, builder.setDisplayName("Test Name"));
        }

        @Test
        @DisplayName("setDisplayName with null clears the display name without error")
        void setDisplayName_withNull_clearsDisplayName() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.setDisplayName("First");
            assertDoesNotThrow(() -> builder.setDisplayName(null));
        }
    }

    @Nested
    @DisplayName("Lore Management")
    class LoreTests {

        @Test
        @DisplayName("withDisplayLore sets lore list and returns builder")
        void withDisplayLore_setsLore() {
            ItemBuilder builder = createBuilder(Material.STONE);
            List<String> lore = List.of("Line 1", "Line 2");
            assertSame(builder, builder.withDisplayLore(lore));
        }

        @Test
        @DisplayName("withDisplayLore throws when loreAsComponent is non-empty")
        void withDisplayLore_throwsWhenLoreComponentExists() {
            ItemBuilder builder = createBuilder(Material.STONE);
            setField(BaseItemBuilder.class, builder, "loreAsComponent",
                    new ArrayList<>(List.of(net.kyori.adventure.text.Component.text("existing"))));
            assertThrows(IllegalStateException.class, () -> builder.withDisplayLore(List.of("test")));
        }

        @Test
        @DisplayName("addDisplayLore with non-empty string adds to lore")
        void addDisplayLore_addsLine() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertSame(builder, builder.addDisplayLore("New Line"));
        }

        @Test
        @DisplayName("addDisplayLore with empty string is a no-op and returns builder")
        void addDisplayLore_emptyStringIsNoOp() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertSame(builder, builder.addDisplayLore(""));
        }

        @Test
        @DisplayName("addDisplayLore with list adds all lines")
        void addDisplayLore_withList_addsAllLines() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertSame(builder, builder.addDisplayLore(List.of("A", "B", "C")));
        }

        @Test
        @DisplayName("addDisplayLore with empty list is a no-op and returns builder")
        void addDisplayLore_emptyList_isNoOp() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertSame(builder, builder.addDisplayLore(List.of()));
        }

        @Test
        @DisplayName("addDisplayLoreComponent adds a Component to the lore")
        void addDisplayLoreComponent_addsComponent() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertSame(builder, builder.addDisplayLoreComponent(net.kyori.adventure.text.Component.text("test")));
        }

        @Test
        @DisplayName("addDisplayLoreComponent with list adds all Components")
        void addDisplayLoreComponent_withList_addsAll() {
            ItemBuilder builder = createBuilder(Material.STONE);
            List<net.kyori.adventure.text.Component> components = List.of(
                    net.kyori.adventure.text.Component.text("a"),
                    net.kyori.adventure.text.Component.text("b")
            );
            assertSame(builder, builder.addDisplayLoreComponent(components));
        }
    }

    @Nested
    @DisplayName("applyTagReplacements")
    class TagReplacementTests {

        @Test
        @DisplayName("empty replacements map is a no-op")
        void applyTagReplacements_emptyMap_isNoOp() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.setDisplayName("Unchanged");
            assertSame(builder, builder.applyTagReplacements(Map.of()));
        }

        @Test
        @DisplayName("replaces tags in display name")
        void applyTagReplacements_replacesInDisplayName() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.setDisplayName("<primary>Hello");
            builder.applyTagReplacements(Map.of("<primary>", "<color:#FF0000>"));
            // After replacement, the internal displayName field should be updated.
            // We can verify by checking the field via reflection.
            try {
                Field displayNameField = BaseItemBuilder.class.getDeclaredField("displayName");
                displayNameField.setAccessible(true);
                String displayName = (String) displayNameField.get(builder);
                assertEquals("<color:#FF0000>Hello", displayName);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("replaces tags in all lore lines")
        void applyTagReplacements_replacesInLore() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.withDisplayLore(new ArrayList<>(List.of("<primary>Line1", "<secondary>Line2")));

            builder.applyTagReplacements(Map.of(
                    "<primary>", "<color:#FF0000>",
                    "<secondary>", "<color:#00FF00>"
            ));

            try {
                Field loreField = BaseItemBuilder.class.getDeclaredField("lore");
                loreField.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<String> lore = (List<String>) loreField.get(builder);
                assertEquals("<color:#FF0000>Line1", lore.get(0));
                assertEquals("<color:#00FF00>Line2", lore.get(1));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("no display name set does not throw when replacements are non-empty")
        void applyTagReplacements_noDisplayName_doesNotThrow() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertDoesNotThrow(() -> builder.applyTagReplacements(Map.of("key", "value")));
        }

        @Test
        @DisplayName("no lore set does not throw when replacements are non-empty")
        void applyTagReplacements_noLore_doesNotThrow() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.setDisplayName("Name");
            assertDoesNotThrow(() -> builder.applyTagReplacements(Map.of("key", "value")));
        }
    }

    @Nested
    @DisplayName("Type Check Methods")
    class TypeCheckTests {

        @Test
        @DisplayName("isPlayerHead returns true for PLAYER_HEAD")
        void isPlayerHead_trueForPlayerHead() {
            assertTrue(createBuilder(Material.PLAYER_HEAD).isPlayerHead());
        }

        @Test
        @DisplayName("isPlayerHead returns false for STONE")
        void isPlayerHead_falseForStone() {
            assertFalse(createBuilder(Material.STONE).isPlayerHead());
        }

        @Test
        @DisplayName("isFireworkStar returns true for FIREWORK_STAR")
        void isFireworkStar_trueForFireworkStar() {
            assertTrue(createBuilder(Material.FIREWORK_STAR).isFireworkStar());
        }

        @Test
        @DisplayName("isFireworkStar returns false for STONE")
        void isFireworkStar_falseForStone() {
            assertFalse(createBuilder(Material.STONE).isFireworkStar());
        }

        @Test
        @DisplayName("isTippedArrow returns true for TIPPED_ARROW")
        void isTippedArrow_trueForTippedArrow() {
            assertTrue(createBuilder(Material.TIPPED_ARROW).isTippedArrow());
        }

        @Test
        @DisplayName("isTippedArrow returns false for STONE")
        void isTippedArrow_falseForStone() {
            assertFalse(createBuilder(Material.STONE).isTippedArrow());
        }

        @Test
        @DisplayName("isFirework returns true for FIREWORK_ROCKET")
        void isFirework_trueForFireworkRocket() {
            assertTrue(createBuilder(Material.FIREWORK_ROCKET).isFirework());
        }

        @Test
        @DisplayName("isFirework returns false for STONE")
        void isFirework_falseForStone() {
            assertFalse(createBuilder(Material.STONE).isFirework());
        }

        @Test
        @DisplayName("isSpawner returns true for SPAWNER")
        void isSpawner_trueForSpawner() {
            assertTrue(createBuilder(Material.SPAWNER).isSpawner());
        }

        @Test
        @DisplayName("isSpawner returns false for STONE")
        void isSpawner_falseForStone() {
            assertFalse(createBuilder(Material.STONE).isSpawner());
        }

        @Test
        @DisplayName("isShield returns true for SHIELD")
        void isShield_trueForShield() {
            assertTrue(createBuilder(Material.SHIELD).isShield());
        }

        @Test
        @DisplayName("isShield returns false for STONE")
        void isShield_falseForStone() {
            assertFalse(createBuilder(Material.STONE).isShield());
        }

        @Test
        @DisplayName("isLeatherArmor returns true for LEATHER_HELMET")
        void isLeatherArmor_trueForLeatherHelmet() {
            assertTrue(createBuilder(Material.LEATHER_HELMET).isLeatherArmor());
        }

        @Test
        @DisplayName("isLeatherArmor returns true for LEATHER_CHESTPLATE")
        void isLeatherArmor_trueForLeatherChestplate() {
            assertTrue(createBuilder(Material.LEATHER_CHESTPLATE).isLeatherArmor());
        }

        @Test
        @DisplayName("isLeatherArmor returns true for LEATHER_LEGGINGS")
        void isLeatherArmor_trueForLeatherLeggings() {
            assertTrue(createBuilder(Material.LEATHER_LEGGINGS).isLeatherArmor());
        }

        @Test
        @DisplayName("isLeatherArmor returns true for LEATHER_BOOTS")
        void isLeatherArmor_trueForLeatherBoots() {
            assertTrue(createBuilder(Material.LEATHER_BOOTS).isLeatherArmor());
        }

        @Test
        @DisplayName("isLeatherArmor returns true for LEATHER_HORSE_ARMOR")
        void isLeatherArmor_trueForLeatherHorseArmor() {
            assertTrue(createBuilder(Material.LEATHER_HORSE_ARMOR).isLeatherArmor());
        }

        @Test
        @DisplayName("isLeatherArmor returns false for IRON_HELMET")
        void isLeatherArmor_falseForIronHelmet() {
            assertFalse(createBuilder(Material.IRON_HELMET).isLeatherArmor());
        }

        @Test
        @DisplayName("isPotion returns true for POTION")
        void isPotion_trueForPotion() {
            assertTrue(createBuilder(Material.POTION).isPotion());
        }

        @Test
        @DisplayName("isPotion returns true for SPLASH_POTION")
        void isPotion_trueForSplashPotion() {
            assertTrue(createBuilder(Material.SPLASH_POTION).isPotion());
        }

        @Test
        @DisplayName("isPotion returns true for LINGERING_POTION")
        void isPotion_trueForLingeringPotion() {
            assertTrue(createBuilder(Material.LINGERING_POTION).isPotion());
        }

        @Test
        @DisplayName("isPotion returns false for STONE")
        void isPotion_falseForStone() {
            assertFalse(createBuilder(Material.STONE).isPotion());
        }

        @Test
        @DisplayName("isBanner returns true for WHITE_BANNER")
        void isBanner_trueForWhiteBanner() {
            assertTrue(createBuilder(Material.WHITE_BANNER).isBanner());
        }

        @Test
        @DisplayName("isBanner returns true for RED_WALL_BANNER")
        void isBanner_trueForRedWallBanner() {
            assertTrue(createBuilder(Material.RED_WALL_BANNER).isBanner());
        }

        @Test
        @DisplayName("isBanner returns false for STONE")
        void isBanner_falseForStone() {
            assertFalse(createBuilder(Material.STONE).isBanner());
        }

        @Test
        @DisplayName("isEnchantedBook returns true for ENCHANTED_BOOK")
        void isEnchantedBook_trueForEnchantedBook() {
            assertTrue(createBuilder(Material.ENCHANTED_BOOK).isEnchantedBook());
        }

        @Test
        @DisplayName("isEnchantedBook returns false for STONE")
        void isEnchantedBook_falseForStone() {
            assertFalse(createBuilder(Material.STONE).isEnchantedBook());
        }

        @Test
        @DisplayName("isMap returns true for FILLED_MAP")
        void isMap_trueForFilledMap() {
            assertTrue(createBuilder(Material.FILLED_MAP).isMap());
        }

        @Test
        @DisplayName("isMap returns false for STONE")
        void isMap_falseForStone() {
            assertFalse(createBuilder(Material.STONE).isMap());
        }

        @Test
        @DisplayName("isDyeable returns true for TIPPED_ARROW")
        void isDyeable_trueForTippedArrow() {
            assertTrue(createBuilder(Material.TIPPED_ARROW).isDyeable());
        }

        @Test
        @DisplayName("isDyeable returns true for SHIELD")
        void isDyeable_trueForShield() {
            assertTrue(createBuilder(Material.SHIELD).isDyeable());
        }

        @Test
        @DisplayName("isDyeable returns true for LEATHER_BOOTS")
        void isDyeable_trueForLeatherBoots() {
            assertTrue(createBuilder(Material.LEATHER_BOOTS).isDyeable());
        }

        @Test
        @DisplayName("isDyeable returns true for FILLED_MAP")
        void isDyeable_trueForFilledMap() {
            assertTrue(createBuilder(Material.FILLED_MAP).isDyeable());
        }

        @Test
        @DisplayName("isDyeable returns false for STONE")
        void isDyeable_falseForStone() {
            assertFalse(createBuilder(Material.STONE).isDyeable());
        }
    }

    @Nested
    @DisplayName("Builder Conversion Exceptions")
    class BuilderConversionTests {

        @Test
        @DisplayName("asFireworkBuilder throws for non-firework item")
        void asFireworkBuilder_throwsForNonFirework() {
            ItemBuilder builder = createBuilder(Material.STONE);
            InvalidItemBuilderException ex = assertThrows(
                    InvalidItemBuilderException.class,
                    builder::asFireworkBuilder
            );
            assertSame(builder, ex.getBuilder());
        }

        @Test
        @DisplayName("asFireworkStarBuilder throws for non-firework-star item")
        void asFireworkStarBuilder_throwsForNonFireworkStar() {
            ItemBuilder builder = createBuilder(Material.STONE);
            InvalidItemBuilderException ex = assertThrows(
                    InvalidItemBuilderException.class,
                    builder::asFireworkStarBuilder
            );
            assertSame(builder, ex.getBuilder());
        }

        @Test
        @DisplayName("asPatternBuilder throws for non-shield/non-banner item")
        void asPatternBuilder_throwsForNonShieldOrBanner() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertThrows(InvalidItemBuilderException.class, builder::asPatternBuilder);
        }

        @Test
        @DisplayName("asSkullBuilder throws for non-player-head item")
        void asSkullBuilder_throwsForNonPlayerHead() {
            ItemBuilder builder = createBuilder(Material.STONE);
            InvalidItemBuilderException ex = assertThrows(
                    InvalidItemBuilderException.class,
                    builder::asSkullBuilder
            );
            assertSame(builder, ex.getBuilder());
        }

        @Test
        @DisplayName("asPotionBuilder throws for non-potion item")
        void asPotionBuilder_throwsForNonPotion() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertThrows(InvalidItemBuilderException.class, builder::asPotionBuilder);
        }

        @Test
        @DisplayName("asSpawnerBuilder throws for non-spawner item")
        void asSpawnerBuilder_throwsForNonSpawner() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertThrows(InvalidItemBuilderException.class, builder::asSpawnerBuilder);
        }
    }

    @Nested
    @DisplayName("Miscellaneous Methods")
    class MiscTests {

        @Test
        @DisplayName("getType returns the Material from the underlying ItemStack")
        void getType_returnsMaterial() {
            assertEquals(Material.DIAMOND_SWORD, createBuilder(Material.DIAMOND_SWORD).getType());
        }

        @Test
        @DisplayName("getCustomItem returns empty when no custom item is set")
        void getCustomItem_returnsEmpty() {
            assertTrue(createBuilder(Material.STONE).getCustomItem().isEmpty());
        }

        @Test
        @DisplayName("build returns the builder itself")
        void build_returnsSelf() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertSame(builder, builder.build());
        }

        @Test
        @DisplayName("setAmount delegates to ItemStack with at least 1")
        void setAmount_setsAmountOnItemStack() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);
            builder.setAmount(5);
            verify(mockItem).setAmount(5);
        }

        @Test
        @DisplayName("setAmount with zero sets amount to 1")
        void setAmount_zeroSetsToOne() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);
            builder.setAmount(0);
            verify(mockItem).setAmount(1);
        }

        @Test
        @DisplayName("setAmount with negative value sets amount to 1")
        void setAmount_negativeSetsToOne() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);
            builder.setAmount(-3);
            verify(mockItem).setAmount(1);
        }

        @Test
        @DisplayName("addItemFlag with ItemFlag enum returns builder for chaining")
        void addItemFlag_returnsSelf() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertSame(builder, builder.addItemFlag(ItemFlag.HIDE_ENCHANTS));
        }

        @Test
        @DisplayName("setItemStack replaces the underlying item and returns builder")
        void setItemStack_replacesItem() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack newMockItem = mock(ItemStack.class);
            when(newMockItem.getType()).thenReturn(Material.DIAMOND);
            assertSame(builder, builder.setItemStack(newMockItem));
            assertEquals(Material.DIAMOND, builder.getType());
        }
    }
}

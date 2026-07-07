package com.diamonddagger590.mccore.builder.item;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.exception.builder.item.InvalidItemBuilderException;
import net.kyori.adventure.text.Component;
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
import static org.junit.jupiter.api.Assertions.assertNull;
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
        return getField(BaseItemBuilder.class, builder, "itemStack");
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Class<?> clazz, Object target, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(target);
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
        @DisplayName("Given a key-value pair, when addPlaceholder is called, then hasPlaceholder returns true")
        void addPlaceholder_storesPlaceholder_whenKeyAdded() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.addPlaceholder("key", "value");
            assertTrue(builder.hasPlaceholder("key"));
        }

        @Test
        @DisplayName("Given no placeholders registered, when hasPlaceholder is called, then returns false")
        void hasPlaceholder_returnsFalse_whenKeyNotRegistered() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertFalse(builder.hasPlaceholder("nonexistent"));
        }

        @Test
        @DisplayName("Given a registered placeholder, when removePlaceholder is called, then placeholder is removed")
        void removePlaceholder_removesPlaceholder_whenKeyExists() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.addPlaceholder("key", "value");
            builder.removePlaceholder("key");
            assertFalse(builder.hasPlaceholder("key"));
        }

        @Test
        @DisplayName("Given existing placeholders, when setPlaceholders is called with new map, then old placeholders are replaced")
        void setPlaceholders_replacesAllPlaceholders_whenNewMapProvided() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.addPlaceholder("old", "oldValue");
            Map<String, String> newMap = new HashMap<>();
            newMap.put("new", "newValue");
            builder.setPlaceholders(newMap);
            assertFalse(builder.hasPlaceholder("old"));
            assertTrue(builder.hasPlaceholder("new"));
        }

        @Test
        @DisplayName("Given existing placeholders, when addPlaceholders is called, then new placeholders are merged")
        void addPlaceholders_mergesIntoExisting_whenExistingPlaceholdersPresent() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.addPlaceholder("existing", "val1");
            Map<String, String> extras = new HashMap<>();
            extras.put("extra", "val2");
            builder.addPlaceholders(extras);
            assertTrue(builder.hasPlaceholder("existing"));
            assertTrue(builder.hasPlaceholder("extra"));
        }

        @Test
        @DisplayName("Given a builder, when addPlaceholder is called, then returns builder for chaining")
        void addPlaceholder_returnsSelf_whenCalled() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemBuilder result = builder.addPlaceholder("key", "value");
            assertSame(builder, result);
        }

        @Test
        @DisplayName("Given a builder, when removePlaceholder is called, then returns builder for chaining")
        void removePlaceholder_returnsSelf_whenCalled() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemBuilder result = builder.removePlaceholder("missing");
            assertSame(builder, result);
        }

        @Test
        @DisplayName("Given a builder, when setPlaceholders is called, then returns builder for chaining")
        void setPlaceholders_returnsSelf_whenCalled() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemBuilder result = builder.setPlaceholders(new HashMap<>());
            assertSame(builder, result);
        }

        @Test
        @DisplayName("Given a builder, when addPlaceholders is called, then returns builder for chaining")
        void addPlaceholders_returnsSelf_whenCalled() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemBuilder result = builder.addPlaceholders(new HashMap<>());
            assertSame(builder, result);
        }
    }

    @Nested
    @DisplayName("Display Name Management")
    class DisplayNameTests {

        @Test
        @DisplayName("Given a builder, when setDisplayName is called with two args, then returns builder and stores name")
        void setDisplayName_returnsSelfAndStoresName_whenCalledWithTwoArgs() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertSame(builder, builder.setDisplayName("Test Name", true));
            String storedName = getField(BaseItemBuilder.class, builder, "displayName");
            assertEquals("Test Name", storedName);
        }

        @Test
        @DisplayName("Given a builder, when setDisplayName is called with single arg, then returns builder and stores name")
        void setDisplayName_returnsSelfAndStoresName_whenCalledWithSingleArg() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertSame(builder, builder.setDisplayName("Test Name"));
            String storedName = getField(BaseItemBuilder.class, builder, "displayName");
            assertEquals("Test Name", storedName);
        }

        @Test
        @DisplayName("Given a builder with a display name, when setDisplayName is called with null, then clears display name")
        void setDisplayName_clearsDisplayName_whenNullProvided() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.setDisplayName("First");
            builder.setDisplayName(null);
            String storedName = getField(BaseItemBuilder.class, builder, "displayName");
            assertNull(storedName);
        }

        @Test
        @DisplayName("Given a builder, when setDisplayName is called with staticItemName false, then sets staticItemName to false")
        void setDisplayName_setsStaticItemNameToFalse_whenFalseProvided() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.setDisplayName("Dynamic Name", false);
            boolean staticItemName = getField(BaseItemBuilder.class, builder, "staticItemName");
            assertFalse(staticItemName);
            String storedName = getField(BaseItemBuilder.class, builder, "displayName");
            assertEquals("Dynamic Name", storedName);
        }
    }

    @Nested
    @DisplayName("Lore Management")
    class LoreTests {

        @Test
        @DisplayName("Given a builder, when withDisplayLore is called, then sets lore list and returns builder")
        void withDisplayLore_setsLoreList_whenCalled() {
            ItemBuilder builder = createBuilder(Material.STONE);
            List<String> lore = List.of("Line 1", "Line 2");
            assertSame(builder, builder.withDisplayLore(lore));
            List<String> storedLore = getField(BaseItemBuilder.class, builder, "lore");
            assertEquals(2, storedLore.size());
            assertEquals("Line 1", storedLore.get(0));
            assertEquals("Line 2", storedLore.get(1));
        }

        @Test
        @DisplayName("Given loreAsComponent is non-empty, when withDisplayLore is called, then throws IllegalStateException")
        void withDisplayLore_throwsIllegalStateException_whenLoreComponentExists() {
            ItemBuilder builder = createBuilder(Material.STONE);
            setField(BaseItemBuilder.class, builder, "loreAsComponent",
                    new ArrayList<>(List.of(Component.text("existing"))));
            assertThrows(IllegalStateException.class, () -> builder.withDisplayLore(List.of("test")));
        }

        @Test
        @DisplayName("Given a builder, when addDisplayLore is called with non-empty string, then adds line to lore")
        void addDisplayLore_addsLine_whenNonEmptyStringProvided() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertSame(builder, builder.addDisplayLore("New Line"));
            List<String> storedLore = getField(BaseItemBuilder.class, builder, "lore");
            assertEquals(1, storedLore.size());
            assertEquals("New Line", storedLore.get(0));
        }

        @Test
        @DisplayName("Given a builder, when addDisplayLore is called with empty string, then lore remains empty")
        void addDisplayLore_doesNotAddToLore_whenEmptyStringProvided() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertSame(builder, builder.addDisplayLore(""));
            List<String> storedLore = getField(BaseItemBuilder.class, builder, "lore");
            assertTrue(storedLore.isEmpty());
        }

        @Test
        @DisplayName("Given a builder, when addDisplayLore is called with a list, then adds all lines to lore")
        void addDisplayLore_addsAllLines_whenListProvided() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertSame(builder, builder.addDisplayLore(List.of("A", "B", "C")));
            List<String> storedLore = getField(BaseItemBuilder.class, builder, "lore");
            assertEquals(3, storedLore.size());
            assertEquals("A", storedLore.get(0));
            assertEquals("B", storedLore.get(1));
            assertEquals("C", storedLore.get(2));
        }

        @Test
        @DisplayName("Given a builder, when addDisplayLore is called with empty list, then lore remains empty")
        void addDisplayLore_doesNotAddToLore_whenEmptyListProvided() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertSame(builder, builder.addDisplayLore(List.of()));
            List<String> storedLore = getField(BaseItemBuilder.class, builder, "lore");
            assertTrue(storedLore.isEmpty());
        }

        @Test
        @DisplayName("Given a builder, when addDisplayLoreComponent is called, then adds component to loreAsComponent")
        void addDisplayLoreComponent_addsComponent_whenComponentProvided() {
            ItemBuilder builder = createBuilder(Material.STONE);
            Component component = Component.text("test");
            assertSame(builder, builder.addDisplayLoreComponent(component));
            List<Component> storedComponents = getField(BaseItemBuilder.class, builder, "loreAsComponent");
            assertEquals(1, storedComponents.size());
            assertEquals(component, storedComponents.get(0));
        }

        @Test
        @DisplayName("Given a builder, when addDisplayLoreComponent is called with list, then adds all components")
        void addDisplayLoreComponent_addsAllComponents_whenListProvided() {
            ItemBuilder builder = createBuilder(Material.STONE);
            List<Component> components = List.of(
                    Component.text("a"),
                    Component.text("b")
            );
            assertSame(builder, builder.addDisplayLoreComponent(components));
            List<Component> storedComponents = getField(BaseItemBuilder.class, builder, "loreAsComponent");
            assertEquals(2, storedComponents.size());
        }
    }

    @Nested
    @DisplayName("applyTagReplacements")
    class TagReplacementTests {

        @Test
        @DisplayName("Given an empty replacements map, when applyTagReplacements is called, then no changes occur")
        void applyTagReplacements_isNoOp_whenEmptyMapProvided() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.setDisplayName("Unchanged");
            assertSame(builder, builder.applyTagReplacements(Map.of()));
            String storedName = getField(BaseItemBuilder.class, builder, "displayName");
            assertEquals("Unchanged", storedName);
        }

        @Test
        @DisplayName("Given a display name with a tag, when applyTagReplacements is called, then tag is replaced")
        void applyTagReplacements_replacesTagInDisplayName_whenMatchingTagExists() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.setDisplayName("<primary>Hello");
            builder.applyTagReplacements(Map.of("<primary>", "<color:#FF0000>"));
            String storedName = getField(BaseItemBuilder.class, builder, "displayName");
            assertEquals("<color:#FF0000>Hello", storedName);
        }

        @Test
        @DisplayName("Given lore lines with tags, when applyTagReplacements is called, then tags are replaced in all lines")
        void applyTagReplacements_replacesTagsInAllLoreLines_whenMatchingTagsExist() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.withDisplayLore(new ArrayList<>(List.of("<primary>Line1", "<secondary>Line2")));

            builder.applyTagReplacements(Map.of(
                    "<primary>", "<color:#FF0000>",
                    "<secondary>", "<color:#00FF00>"
            ));

            List<String> storedLore = getField(BaseItemBuilder.class, builder, "lore");
            assertEquals("<color:#FF0000>Line1", storedLore.get(0));
            assertEquals("<color:#00FF00>Line2", storedLore.get(1));
        }

        @Test
        @DisplayName("Given no display name set, when applyTagReplacements is called with non-empty map, then does not throw")
        void applyTagReplacements_doesNotThrow_whenNoDisplayNameSet() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertDoesNotThrow(() -> builder.applyTagReplacements(Map.of("key", "value")));
        }

        @Test
        @DisplayName("Given no lore set, when applyTagReplacements is called with non-empty map, then does not throw")
        void applyTagReplacements_doesNotThrow_whenNoLoreSet() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.setDisplayName("Name");
            assertDoesNotThrow(() -> builder.applyTagReplacements(Map.of("key", "value")));
        }
    }

    @Nested
    @DisplayName("Type Check Methods")
    class TypeCheckTests {

        @Test
        @DisplayName("Given PLAYER_HEAD material, when isPlayerHead is called, then returns true")
        void isPlayerHead_returnsTrue_whenMaterialIsPlayerHead() {
            assertTrue(createBuilder(Material.PLAYER_HEAD).isPlayerHead());
        }

        @Test
        @DisplayName("Given STONE material, when isPlayerHead is called, then returns false")
        void isPlayerHead_returnsFalse_whenMaterialIsStone() {
            assertFalse(createBuilder(Material.STONE).isPlayerHead());
        }

        @Test
        @DisplayName("Given FIREWORK_STAR material, when isFireworkStar is called, then returns true")
        void isFireworkStar_returnsTrue_whenMaterialIsFireworkStar() {
            assertTrue(createBuilder(Material.FIREWORK_STAR).isFireworkStar());
        }

        @Test
        @DisplayName("Given STONE material, when isFireworkStar is called, then returns false")
        void isFireworkStar_returnsFalse_whenMaterialIsStone() {
            assertFalse(createBuilder(Material.STONE).isFireworkStar());
        }

        @Test
        @DisplayName("Given TIPPED_ARROW material, when isTippedArrow is called, then returns true")
        void isTippedArrow_returnsTrue_whenMaterialIsTippedArrow() {
            assertTrue(createBuilder(Material.TIPPED_ARROW).isTippedArrow());
        }

        @Test
        @DisplayName("Given STONE material, when isTippedArrow is called, then returns false")
        void isTippedArrow_returnsFalse_whenMaterialIsStone() {
            assertFalse(createBuilder(Material.STONE).isTippedArrow());
        }

        @Test
        @DisplayName("Given FIREWORK_ROCKET material, when isFirework is called, then returns true")
        void isFirework_returnsTrue_whenMaterialIsFireworkRocket() {
            assertTrue(createBuilder(Material.FIREWORK_ROCKET).isFirework());
        }

        @Test
        @DisplayName("Given STONE material, when isFirework is called, then returns false")
        void isFirework_returnsFalse_whenMaterialIsStone() {
            assertFalse(createBuilder(Material.STONE).isFirework());
        }

        @Test
        @DisplayName("Given SPAWNER material, when isSpawner is called, then returns true")
        void isSpawner_returnsTrue_whenMaterialIsSpawner() {
            assertTrue(createBuilder(Material.SPAWNER).isSpawner());
        }

        @Test
        @DisplayName("Given STONE material, when isSpawner is called, then returns false")
        void isSpawner_returnsFalse_whenMaterialIsStone() {
            assertFalse(createBuilder(Material.STONE).isSpawner());
        }

        @Test
        @DisplayName("Given SHIELD material, when isShield is called, then returns true")
        void isShield_returnsTrue_whenMaterialIsShield() {
            assertTrue(createBuilder(Material.SHIELD).isShield());
        }

        @Test
        @DisplayName("Given STONE material, when isShield is called, then returns false")
        void isShield_returnsFalse_whenMaterialIsStone() {
            assertFalse(createBuilder(Material.STONE).isShield());
        }

        @Test
        @DisplayName("Given LEATHER_HELMET material, when isLeatherArmor is called, then returns true")
        void isLeatherArmor_returnsTrue_whenMaterialIsLeatherHelmet() {
            assertTrue(createBuilder(Material.LEATHER_HELMET).isLeatherArmor());
        }

        @Test
        @DisplayName("Given LEATHER_CHESTPLATE material, when isLeatherArmor is called, then returns true")
        void isLeatherArmor_returnsTrue_whenMaterialIsLeatherChestplate() {
            assertTrue(createBuilder(Material.LEATHER_CHESTPLATE).isLeatherArmor());
        }

        @Test
        @DisplayName("Given LEATHER_LEGGINGS material, when isLeatherArmor is called, then returns true")
        void isLeatherArmor_returnsTrue_whenMaterialIsLeatherLeggings() {
            assertTrue(createBuilder(Material.LEATHER_LEGGINGS).isLeatherArmor());
        }

        @Test
        @DisplayName("Given LEATHER_BOOTS material, when isLeatherArmor is called, then returns true")
        void isLeatherArmor_returnsTrue_whenMaterialIsLeatherBoots() {
            assertTrue(createBuilder(Material.LEATHER_BOOTS).isLeatherArmor());
        }

        @Test
        @DisplayName("Given LEATHER_HORSE_ARMOR material, when isLeatherArmor is called, then returns true")
        void isLeatherArmor_returnsTrue_whenMaterialIsLeatherHorseArmor() {
            assertTrue(createBuilder(Material.LEATHER_HORSE_ARMOR).isLeatherArmor());
        }

        @Test
        @DisplayName("Given IRON_HELMET material, when isLeatherArmor is called, then returns false")
        void isLeatherArmor_returnsFalse_whenMaterialIsIronHelmet() {
            assertFalse(createBuilder(Material.IRON_HELMET).isLeatherArmor());
        }

        @Test
        @DisplayName("Given POTION material, when isPotion is called, then returns true")
        void isPotion_returnsTrue_whenMaterialIsPotion() {
            assertTrue(createBuilder(Material.POTION).isPotion());
        }

        @Test
        @DisplayName("Given SPLASH_POTION material, when isPotion is called, then returns true")
        void isPotion_returnsTrue_whenMaterialIsSplashPotion() {
            assertTrue(createBuilder(Material.SPLASH_POTION).isPotion());
        }

        @Test
        @DisplayName("Given LINGERING_POTION material, when isPotion is called, then returns true")
        void isPotion_returnsTrue_whenMaterialIsLingeringPotion() {
            assertTrue(createBuilder(Material.LINGERING_POTION).isPotion());
        }

        @Test
        @DisplayName("Given STONE material, when isPotion is called, then returns false")
        void isPotion_returnsFalse_whenMaterialIsStone() {
            assertFalse(createBuilder(Material.STONE).isPotion());
        }

        @Test
        @DisplayName("Given WHITE_BANNER material, when isBanner is called, then returns true")
        void isBanner_returnsTrue_whenMaterialIsWhiteBanner() {
            assertTrue(createBuilder(Material.WHITE_BANNER).isBanner());
        }

        @Test
        @DisplayName("Given RED_WALL_BANNER material, when isBanner is called, then returns true")
        void isBanner_returnsTrue_whenMaterialIsRedWallBanner() {
            assertTrue(createBuilder(Material.RED_WALL_BANNER).isBanner());
        }

        @Test
        @DisplayName("Given STONE material, when isBanner is called, then returns false")
        void isBanner_returnsFalse_whenMaterialIsStone() {
            assertFalse(createBuilder(Material.STONE).isBanner());
        }

        @Test
        @DisplayName("Given ENCHANTED_BOOK material, when isEnchantedBook is called, then returns true")
        void isEnchantedBook_returnsTrue_whenMaterialIsEnchantedBook() {
            assertTrue(createBuilder(Material.ENCHANTED_BOOK).isEnchantedBook());
        }

        @Test
        @DisplayName("Given STONE material, when isEnchantedBook is called, then returns false")
        void isEnchantedBook_returnsFalse_whenMaterialIsStone() {
            assertFalse(createBuilder(Material.STONE).isEnchantedBook());
        }

        @Test
        @DisplayName("Given FILLED_MAP material, when isMap is called, then returns true")
        void isMap_returnsTrue_whenMaterialIsFilledMap() {
            assertTrue(createBuilder(Material.FILLED_MAP).isMap());
        }

        @Test
        @DisplayName("Given STONE material, when isMap is called, then returns false")
        void isMap_returnsFalse_whenMaterialIsStone() {
            assertFalse(createBuilder(Material.STONE).isMap());
        }

        @Test
        @DisplayName("Given TIPPED_ARROW material, when isDyeable is called, then returns true")
        void isDyeable_returnsTrue_whenMaterialIsTippedArrow() {
            assertTrue(createBuilder(Material.TIPPED_ARROW).isDyeable());
        }

        @Test
        @DisplayName("Given SHIELD material, when isDyeable is called, then returns true")
        void isDyeable_returnsTrue_whenMaterialIsShield() {
            assertTrue(createBuilder(Material.SHIELD).isDyeable());
        }

        @Test
        @DisplayName("Given LEATHER_BOOTS material, when isDyeable is called, then returns true")
        void isDyeable_returnsTrue_whenMaterialIsLeatherBoots() {
            assertTrue(createBuilder(Material.LEATHER_BOOTS).isDyeable());
        }

        @Test
        @DisplayName("Given FILLED_MAP material, when isDyeable is called, then returns true")
        void isDyeable_returnsTrue_whenMaterialIsFilledMap() {
            assertTrue(createBuilder(Material.FILLED_MAP).isDyeable());
        }

        @Test
        @DisplayName("Given STONE material, when isDyeable is called, then returns false")
        void isDyeable_returnsFalse_whenMaterialIsStone() {
            assertFalse(createBuilder(Material.STONE).isDyeable());
        }
    }

    @Nested
    @DisplayName("Builder Conversion Exceptions")
    class BuilderConversionTests {

        @Test
        @DisplayName("Given non-firework material, when asFireworkBuilder is called, then throws InvalidItemBuilderException")
        void asFireworkBuilder_throwsInvalidItemBuilderException_whenNotFirework() {
            ItemBuilder builder = createBuilder(Material.STONE);
            InvalidItemBuilderException ex = assertThrows(
                    InvalidItemBuilderException.class,
                    builder::asFireworkBuilder
            );
            assertSame(builder, ex.getBuilder());
        }

        @Test
        @DisplayName("Given non-firework-star material, when asFireworkStarBuilder is called, then throws InvalidItemBuilderException")
        void asFireworkStarBuilder_throwsInvalidItemBuilderException_whenNotFireworkStar() {
            ItemBuilder builder = createBuilder(Material.STONE);
            InvalidItemBuilderException ex = assertThrows(
                    InvalidItemBuilderException.class,
                    builder::asFireworkStarBuilder
            );
            assertSame(builder, ex.getBuilder());
        }

        @Test
        @DisplayName("Given non-shield/non-banner material, when asPatternBuilder is called, then throws InvalidItemBuilderException")
        void asPatternBuilder_throwsInvalidItemBuilderException_whenNotShieldOrBanner() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertThrows(InvalidItemBuilderException.class, builder::asPatternBuilder);
        }

        @Test
        @DisplayName("Given non-player-head material, when asSkullBuilder is called, then throws InvalidItemBuilderException")
        void asSkullBuilder_throwsInvalidItemBuilderException_whenNotPlayerHead() {
            ItemBuilder builder = createBuilder(Material.STONE);
            InvalidItemBuilderException ex = assertThrows(
                    InvalidItemBuilderException.class,
                    builder::asSkullBuilder
            );
            assertSame(builder, ex.getBuilder());
        }

        @Test
        @DisplayName("Given non-potion material, when asPotionBuilder is called, then throws InvalidItemBuilderException")
        void asPotionBuilder_throwsInvalidItemBuilderException_whenNotPotion() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertThrows(InvalidItemBuilderException.class, builder::asPotionBuilder);
        }

        @Test
        @DisplayName("Given non-spawner material, when asSpawnerBuilder is called, then throws InvalidItemBuilderException")
        void asSpawnerBuilder_throwsInvalidItemBuilderException_whenNotSpawner() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertThrows(InvalidItemBuilderException.class, builder::asSpawnerBuilder);
        }
    }

    @Nested
    @DisplayName("Miscellaneous Methods")
    class MiscTests {

        @Test
        @DisplayName("Given a builder with DIAMOND_SWORD, when getType is called, then returns DIAMOND_SWORD")
        void getType_returnsMaterial_whenItemStackHasMaterial() {
            assertEquals(Material.DIAMOND_SWORD, createBuilder(Material.DIAMOND_SWORD).getType());
        }

        @Test
        @DisplayName("Given no custom item set, when getCustomItem is called, then returns empty Optional")
        void getCustomItem_returnsEmpty_whenNoCustomItemSet() {
            assertTrue(createBuilder(Material.STONE).getCustomItem().isEmpty());
        }

        @Test
        @DisplayName("Given a builder, when build is called, then returns the builder itself")
        void build_returnsSelf_whenCalled() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertSame(builder, builder.build());
        }

        @Test
        @DisplayName("Given a positive amount, when setAmount is called, then delegates to ItemStack with that amount")
        void setAmount_setsAmount_whenPositiveValueProvided() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);
            builder.setAmount(5);
            verify(mockItem).setAmount(5);
        }

        @Test
        @DisplayName("Given zero amount, when setAmount is called, then sets amount to 1")
        void setAmount_setsAmountToOne_whenZeroProvided() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);
            builder.setAmount(0);
            verify(mockItem).setAmount(1);
        }

        @Test
        @DisplayName("Given negative amount, when setAmount is called, then sets amount to 1")
        void setAmount_setsAmountToOne_whenNegativeValueProvided() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);
            builder.setAmount(-3);
            verify(mockItem).setAmount(1);
        }

        @Test
        @DisplayName("Given Integer.MAX_VALUE, when setAmount is called, then delegates to ItemStack with that value")
        void setAmount_setsAmount_whenMaxIntProvided() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);
            builder.setAmount(Integer.MAX_VALUE);
            verify(mockItem).setAmount(Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("Given an ItemFlag, when addItemFlag is called, then flag is added and builder is returned")
        void addItemFlag_addsFlagAndReturnsSelf_whenFlagProvided() {
            ItemBuilder builder = createBuilder(Material.STONE);
            assertSame(builder, builder.addItemFlag(ItemFlag.HIDE_ENCHANTS));
            List<ItemFlag> storedFlags = getField(BaseItemBuilder.class, builder, "itemFlags");
            assertEquals(1, storedFlags.size());
            assertEquals(ItemFlag.HIDE_ENCHANTS, storedFlags.get(0));
        }

        @Test
        @DisplayName("Given a new ItemStack, when setItemStack is called, then replaces the underlying item")
        void setItemStack_replacesUnderlyingItem_whenNewItemProvided() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack newMockItem = mock(ItemStack.class);
            when(newMockItem.getType()).thenReturn(Material.DIAMOND);
            assertSame(builder, builder.setItemStack(newMockItem));
            assertEquals(Material.DIAMOND, builder.getType());
        }
    }
}

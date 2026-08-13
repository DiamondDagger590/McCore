package com.diamonddagger590.mccore.builder.item;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.exception.builder.item.InvalidItemBuilderException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.PatternReplacementResult;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

    @Nested
    @DisplayName("Enchantment Glint")
    class EnchantGlintTests {

        @Test
        @DisplayName("Given glint enabled without existing data, when setEnchantGlint true, then sets data on item")
        void setEnchantGlint_setsData_whenEnablingWithoutExistingData() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);
            when(mockItem.hasData(io.papermc.paper.datacomponent.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE)).thenReturn(false);

            assertSame(builder, builder.setEnchantGlint(true));

            verify(mockItem).setData(io.papermc.paper.datacomponent.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        @Test
        @DisplayName("Given glint already enabled, when setEnchantGlint true, then does not set data again")
        void setEnchantGlint_doesNotSetData_whenAlreadyEnabled() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);
            when(mockItem.hasData(io.papermc.paper.datacomponent.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE)).thenReturn(true);

            builder.setEnchantGlint(true);

            verify(mockItem, never()).setData(io.papermc.paper.datacomponent.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        @Test
        @DisplayName("Given glint enabled, when setEnchantGlint false, then removes data")
        void setEnchantGlint_removesData_whenDisablingWithExistingData() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);
            when(mockItem.hasData(io.papermc.paper.datacomponent.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE)).thenReturn(true);

            builder.setEnchantGlint(false);

            verify(mockItem).unsetData(io.papermc.paper.datacomponent.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        }

        @Test
        @DisplayName("Given glint not set, when setEnchantGlint false, then does nothing")
        void setEnchantGlint_doesNothing_whenDisablingWithNoExistingData() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);
            when(mockItem.hasData(io.papermc.paper.datacomponent.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE)).thenReturn(false);

            builder.setEnchantGlint(false);

            verify(mockItem, never()).unsetData(io.papermc.paper.datacomponent.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        }

        @Test
        @DisplayName("Given glint set, when removeEnchantGlint, then removes data")
        void removeEnchantGlint_removesData_whenGlintExists() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);
            when(mockItem.hasData(io.papermc.paper.datacomponent.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE)).thenReturn(true);

            assertSame(builder, builder.removeEnchantGlint());

            verify(mockItem).unsetData(io.papermc.paper.datacomponent.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        }

        @Test
        @DisplayName("Given glint not set, when removeEnchantGlint, then does nothing")
        void removeEnchantGlint_doesNothing_whenGlintNotSet() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);
            when(mockItem.hasData(io.papermc.paper.datacomponent.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE)).thenReturn(false);

            builder.removeEnchantGlint();

            verify(mockItem, never()).unsetData(io.papermc.paper.datacomponent.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        }
    }

    @Nested
    @DisplayName("Unbreakable")
    class UnbreakableTests {

        @Test
        @DisplayName("Given item is not unbreakable, when setUnbreakable true, then sets data")
        void setUnbreakable_setsData_whenEnablingOnBreakableItem() {
            ItemBuilder builder = createBuilder(Material.DIAMOND_SWORD);
            ItemStack mockItem = getItemStack(builder);
            when(mockItem.hasData(io.papermc.paper.datacomponent.DataComponentTypes.UNBREAKABLE)).thenReturn(false);

            assertSame(builder, builder.setUnbreakable(true));

            verify(mockItem).setData(io.papermc.paper.datacomponent.DataComponentTypes.UNBREAKABLE);
        }

        @Test
        @DisplayName("Given item is already unbreakable, when setUnbreakable true, then does not set again")
        void setUnbreakable_doesNotSetAgain_whenAlreadyUnbreakable() {
            ItemBuilder builder = createBuilder(Material.DIAMOND_SWORD);
            ItemStack mockItem = getItemStack(builder);
            when(mockItem.hasData(io.papermc.paper.datacomponent.DataComponentTypes.UNBREAKABLE)).thenReturn(true);

            builder.setUnbreakable(true);

            verify(mockItem, never()).setData(io.papermc.paper.datacomponent.DataComponentTypes.UNBREAKABLE);
        }

        @Test
        @DisplayName("Given item is unbreakable, when setUnbreakable false, then removes data")
        void setUnbreakable_removesData_whenDisablingUnbreakableItem() {
            ItemBuilder builder = createBuilder(Material.DIAMOND_SWORD);
            ItemStack mockItem = getItemStack(builder);
            when(mockItem.hasData(io.papermc.paper.datacomponent.DataComponentTypes.UNBREAKABLE)).thenReturn(true);

            builder.setUnbreakable(false);

            verify(mockItem).unsetData(io.papermc.paper.datacomponent.DataComponentTypes.UNBREAKABLE);
        }

        @Test
        @DisplayName("Given item is breakable, when setUnbreakable false, then does nothing")
        void setUnbreakable_doesNothing_whenAlreadyBreakable() {
            ItemBuilder builder = createBuilder(Material.DIAMOND_SWORD);
            ItemStack mockItem = getItemStack(builder);
            when(mockItem.hasData(io.papermc.paper.datacomponent.DataComponentTypes.UNBREAKABLE)).thenReturn(false);

            builder.setUnbreakable(false);

            verify(mockItem, never()).unsetData(io.papermc.paper.datacomponent.DataComponentTypes.UNBREAKABLE);
        }
    }

    @Nested
    @DisplayName("Max Stack Size")
    class MaxStackSizeTests {

        @Test
        @DisplayName("Given a positive value, when setMaxStackSize, then sets data on item")
        void setMaxStackSize_setsData_whenPositiveValue() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);

            assertSame(builder, builder.setMaxStackSize(16));

            verify(mockItem).setData(io.papermc.paper.datacomponent.DataComponentTypes.MAX_STACK_SIZE, 16);
        }

        @Test
        @DisplayName("Given zero value, when setMaxStackSize, then clamps to 1")
        void setMaxStackSize_clampsToOne_whenZero() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);

            builder.setMaxStackSize(0);

            verify(mockItem).setData(io.papermc.paper.datacomponent.DataComponentTypes.MAX_STACK_SIZE, 1);
        }

        @Test
        @DisplayName("Given negative value, when setMaxStackSize, then clamps to 1")
        void setMaxStackSize_clampsToOne_whenNegative() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);

            builder.setMaxStackSize(-5);

            verify(mockItem).setData(io.papermc.paper.datacomponent.DataComponentTypes.MAX_STACK_SIZE, 1);
        }
    }

    @Nested
    @DisplayName("withBase64")
    class WithBase64Tests {

        @Test
        @DisplayName("Given empty string, when withBase64, then does not change item and returns builder")
        void withBase64_doesNothing_whenEmptyString() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack originalItem = getItemStack(builder);

            assertSame(builder, builder.withBase64(""));

            assertSame(originalItem, getItemStack(builder));
        }
    }

    @Nested
    @DisplayName("Tooltip Management")
    class TooltipTests {

        @Test
        @DisplayName("Given no tooltip data, when hideToolTip, then sets tooltip data and returns builder")
        void hideToolTip_setsData_whenNoExistingTooltip() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);
            when(mockItem.hasData(io.papermc.paper.datacomponent.DataComponentTypes.TOOLTIP_DISPLAY)).thenReturn(false);

            assertSame(builder, builder.hideToolTip());

            verify(mockItem).setData(
                    org.mockito.ArgumentMatchers.eq(io.papermc.paper.datacomponent.DataComponentTypes.TOOLTIP_DISPLAY),
                    org.mockito.ArgumentMatchers.any(io.papermc.paper.datacomponent.item.TooltipDisplay.class)
            );
        }

        @Test
        @DisplayName("Given tooltip already hidden, when hideToolTip, then does not set data again")
        void hideToolTip_doesNothing_whenTooltipAlreadyHidden() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);
            when(mockItem.hasData(io.papermc.paper.datacomponent.DataComponentTypes.TOOLTIP_DISPLAY)).thenReturn(true);

            builder.hideToolTip();

            verify(mockItem, never()).setData(
                    org.mockito.ArgumentMatchers.eq(io.papermc.paper.datacomponent.DataComponentTypes.TOOLTIP_DISPLAY),
                    org.mockito.ArgumentMatchers.any(io.papermc.paper.datacomponent.item.TooltipDisplay.class)
            );
        }

        @Test
        @DisplayName("Given tooltip hidden, when showToolTip, then removes tooltip data and returns builder")
        void showToolTip_removesData_whenTooltipIsHidden() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);
            when(mockItem.hasData(io.papermc.paper.datacomponent.DataComponentTypes.TOOLTIP_DISPLAY)).thenReturn(true);

            assertSame(builder, builder.showToolTip());

            verify(mockItem).unsetData(io.papermc.paper.datacomponent.DataComponentTypes.TOOLTIP_DISPLAY);
        }

        @Test
        @DisplayName("Given tooltip not hidden, when showToolTip, then does nothing")
        void showToolTip_doesNothing_whenTooltipNotHidden() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);
            when(mockItem.hasData(io.papermc.paper.datacomponent.DataComponentTypes.TOOLTIP_DISPLAY)).thenReturn(false);

            builder.showToolTip();

            verify(mockItem, never()).unsetData(io.papermc.paper.datacomponent.DataComponentTypes.TOOLTIP_DISPLAY);
        }
    }

    @Nested
    @DisplayName("Item Flags by String")
    class ItemFlagsByStringTests {

        @Test
        @DisplayName("Given a valid flag string, when addItemFlag with string, then adds the flag")
        void addItemFlag_addsFlag_whenValidString() {
            ItemBuilder builder = createBuilder(Material.STONE);

            assertSame(builder, builder.addItemFlag("HIDE_ENCHANTS"));

            List<ItemFlag> storedFlags = getField(BaseItemBuilder.class, builder, "itemFlags");
            assertEquals(1, storedFlags.size());
            assertEquals(ItemFlag.HIDE_ENCHANTS, storedFlags.get(0));
        }

        @Test
        @DisplayName("Given an invalid flag string, when addItemFlag with string, then no flag is added")
        void addItemFlag_doesNotAddFlag_whenInvalidString() {
            ItemBuilder builder = createBuilder(Material.STONE);

            builder.addItemFlag("NOT_A_REAL_FLAG");

            List<ItemFlag> storedFlags = getField(BaseItemBuilder.class, builder, "itemFlags");
            assertTrue(storedFlags.isEmpty());
        }

        @Test
        @DisplayName("Given a list of valid flag strings, when addItemFlags, then adds all flags")
        void addItemFlags_addsAllFlags_whenValidStrings() {
            ItemBuilder builder = createBuilder(Material.STONE);

            assertSame(builder, builder.addItemFlags(List.of("HIDE_ENCHANTS", "HIDE_ATTRIBUTES")));

            List<ItemFlag> storedFlags = getField(BaseItemBuilder.class, builder, "itemFlags");
            assertEquals(2, storedFlags.size());
            assertTrue(storedFlags.contains(ItemFlag.HIDE_ENCHANTS));
            assertTrue(storedFlags.contains(ItemFlag.HIDE_ATTRIBUTES));
        }

        @Test
        @DisplayName("Given an empty list, when addItemFlags, then no flags are added")
        void addItemFlags_addsNothing_whenEmptyList() {
            ItemBuilder builder = createBuilder(Material.STONE);

            builder.addItemFlags(List.of());

            List<ItemFlag> storedFlags = getField(BaseItemBuilder.class, builder, "itemFlags");
            assertTrue(storedFlags.isEmpty());
        }

        @Test
        @DisplayName("Given a list of valid flag strings, when removeItemFlags, then removes matching flags")
        void removeItemFlags_removesFlags_whenValid() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.addItemFlag(ItemFlag.HIDE_ENCHANTS);
            builder.addItemFlag(ItemFlag.HIDE_ATTRIBUTES);

            assertSame(builder, builder.removeItemFlags(List.of("HIDE_ENCHANTS")));

            List<ItemFlag> storedFlags = getField(BaseItemBuilder.class, builder, "itemFlags");
            assertEquals(1, storedFlags.size());
            assertEquals(ItemFlag.HIDE_ATTRIBUTES, storedFlags.get(0));
        }

        @Test
        @DisplayName("Given a valid flag string, when removeItemFlag by string, then removes the flag")
        void removeItemFlag_removesFlag_whenValidString() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.addItemFlag(ItemFlag.HIDE_ENCHANTS);

            assertSame(builder, builder.removeItemFlag("HIDE_ENCHANTS"));

            List<ItemFlag> storedFlags = getField(BaseItemBuilder.class, builder, "itemFlags");
            assertTrue(storedFlags.isEmpty());
        }

        @Test
        @DisplayName("Given an invalid flag string, when removeItemFlag by string, then no flag is removed")
        void removeItemFlag_doesNothing_whenInvalidString() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.addItemFlag(ItemFlag.HIDE_ENCHANTS);

            builder.removeItemFlag("NOT_A_REAL_FLAG");

            List<ItemFlag> storedFlags = getField(BaseItemBuilder.class, builder, "itemFlags");
            assertEquals(1, storedFlags.size());
        }
    }

    @Nested
    @DisplayName("Custom Model Data")
    class CustomModelDataTests {

        @Test
        @DisplayName("Given -1, when setCustomModelData, then does not set data and returns builder")
        void setCustomModelData_doesNothing_whenNegativeOne() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);

            assertSame(builder, builder.setCustomModelData(-1));

            verify(mockItem, never()).setData(
                    org.mockito.ArgumentMatchers.eq(io.papermc.paper.datacomponent.DataComponentTypes.CUSTOM_MODEL_DATA),
                    org.mockito.ArgumentMatchers.any(io.papermc.paper.datacomponent.item.CustomModelData.class)
            );
        }

        @Test
        @DisplayName("Given a positive value, when setCustomModelData, then sets data on item")
        void setCustomModelData_setsData_whenPositiveValue() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);

            builder.setCustomModelData(42);

            verify(mockItem).setData(
                    org.mockito.ArgumentMatchers.eq(io.papermc.paper.datacomponent.DataComponentTypes.CUSTOM_MODEL_DATA),
                    org.mockito.ArgumentMatchers.any(io.papermc.paper.datacomponent.item.CustomModelData.class)
            );
        }

        @Test
        @DisplayName("Given zero, when setCustomModelData, then sets data on item")
        void setCustomModelData_setsData_whenZero() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);

            builder.setCustomModelData(0);

            verify(mockItem).setData(
                    org.mockito.ArgumentMatchers.eq(io.papermc.paper.datacomponent.DataComponentTypes.CUSTOM_MODEL_DATA),
                    org.mockito.ArgumentMatchers.any(io.papermc.paper.datacomponent.item.CustomModelData.class)
            );
        }
    }

    @Nested
    @DisplayName("Item Model")
    class ItemModelTests {

        @Test
        @DisplayName("Given empty string, when setItemModel, then does not set data and returns builder")
        void setItemModel_doesNothing_whenEmptyString() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);

            assertSame(builder, builder.setItemModel(""));

            verify(mockItem, never()).setData(
                    org.mockito.ArgumentMatchers.eq(io.papermc.paper.datacomponent.DataComponentTypes.ITEM_MODEL),
                    org.mockito.ArgumentMatchers.any(org.bukkit.NamespacedKey.class)
            );
        }

        @Test
        @DisplayName("Given a valid model name, when setItemModel with single arg, then sets data")
        void setItemModel_setsData_whenValidName() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);

            builder.setItemModel("custom_sword");

            verify(mockItem).setData(
                    org.mockito.ArgumentMatchers.eq(io.papermc.paper.datacomponent.DataComponentTypes.ITEM_MODEL),
                    org.mockito.ArgumentMatchers.eq(org.bukkit.NamespacedKey.minecraft("custom_sword"))
            );
        }

        @Test
        @DisplayName("Given empty item model, when setItemModel with namespace, then does not set data")
        void setItemModel_doesNothing_whenEmptyModelWithNamespace() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);

            assertSame(builder, builder.setItemModel("myplugin", ""));

            verify(mockItem, never()).setData(
                    org.mockito.ArgumentMatchers.eq(io.papermc.paper.datacomponent.DataComponentTypes.ITEM_MODEL),
                    org.mockito.ArgumentMatchers.any(org.bukkit.NamespacedKey.class)
            );
        }

        @Test
        @DisplayName("Given valid namespace and model, when setItemModel with two args, then sets data")
        void setItemModel_setsData_whenValidNamespaceAndModel() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);

            builder.setItemModel("myplugin", "custom_axe");

            verify(mockItem).setData(
                    org.mockito.ArgumentMatchers.eq(io.papermc.paper.datacomponent.DataComponentTypes.ITEM_MODEL),
                    org.mockito.ArgumentMatchers.eq(new org.bukkit.NamespacedKey("myplugin", "custom_axe"))
            );
        }
    }

    @Nested
    @DisplayName("Inventory Methods")
    class InventoryMethodTests {

        @Test
        @DisplayName("Given an inventory and slot, when setItemToInventory with null audience, then sets item at slot")
        void setItemToInventory_setsItem_whenNullAudience() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);
            ItemStack clonedItem = mock(ItemStack.class);
            when(mockItem.clone()).thenReturn(clonedItem);
            org.bukkit.inventory.Inventory mockInventory = mock(org.bukkit.inventory.Inventory.class);

            builder.setItemToInventory(mockInventory, 5);

            verify(mockInventory).setItem(org.mockito.ArgumentMatchers.eq(5), org.mockito.ArgumentMatchers.any(ItemStack.class));
        }

        @Test
        @DisplayName("Given an inventory, when addItemToInventory with null audience, then adds item to inventory")
        void addItemToInventory_addsItem_whenNullAudience() {
            ItemBuilder builder = createBuilder(Material.STONE);
            ItemStack mockItem = getItemStack(builder);
            ItemStack clonedItem = mock(ItemStack.class);
            when(mockItem.clone()).thenReturn(clonedItem);
            org.bukkit.inventory.Inventory mockInventory = mock(org.bukkit.inventory.Inventory.class);

            builder.addItemToInventory(mockInventory);

            verify(mockInventory).addItem(org.mockito.ArgumentMatchers.any(ItemStack.class));
        }
    }

    @Nested
    @DisplayName("Trim")
    class TrimTests {

        @Test
        @DisplayName("Given empty pattern string, when setTrim with strings, then does not set data")
        void setTrim_doesNothing_whenPatternIsEmpty() {
            ItemBuilder builder = createBuilder(Material.DIAMOND_CHESTPLATE);
            ItemStack mockItem = getItemStack(builder);

            assertSame(builder, builder.setTrim("", "iron"));

            verify(mockItem, never()).setData(
                    org.mockito.ArgumentMatchers.eq(io.papermc.paper.datacomponent.DataComponentTypes.TRIM),
                    org.mockito.ArgumentMatchers.any(io.papermc.paper.datacomponent.item.ItemArmorTrim.class)
            );
        }

        @Test
        @DisplayName("Given empty material string, when setTrim with strings, then does not set data")
        void setTrim_doesNothing_whenMaterialIsEmpty() {
            ItemBuilder builder = createBuilder(Material.DIAMOND_CHESTPLATE);
            ItemStack mockItem = getItemStack(builder);

            assertSame(builder, builder.setTrim("sentry", ""));

            verify(mockItem, never()).setData(
                    org.mockito.ArgumentMatchers.eq(io.papermc.paper.datacomponent.DataComponentTypes.TRIM),
                    org.mockito.ArgumentMatchers.any(io.papermc.paper.datacomponent.item.ItemArmorTrim.class)
            );
        }
    }

    @Nested
    @DisplayName("Display Name with Component Clearing")
    class DisplayNameComponentTests {

        @Test
        @DisplayName("Given a builder with displayNameComponent, when setDisplayName is called, then displayNameComponent is cleared")
        void setDisplayName_clearsComponent_whenComponentWasSet() {
            ItemBuilder builder = createBuilder(Material.STONE);
            setField(BaseItemBuilder.class, builder, "displayNameComponent", Component.text("Old Component"));

            builder.setDisplayName("New Name", true);

            Component storedComponent = getField(BaseItemBuilder.class, builder, "displayNameComponent");
            assertNull(storedComponent);
            String storedName = getField(BaseItemBuilder.class, builder, "displayName");
            assertEquals("New Name", storedName);
        }

        @Test
        @DisplayName("Given a builder, when setDisplayName with staticItemName false, then stores correctly")
        void setDisplayName_storesStaticFlag_whenExplicitlySet() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.setDisplayName("Dynamic", false);

            boolean staticFlag = getField(BaseItemBuilder.class, builder, "staticItemName");
            assertFalse(staticFlag);
        }
    }

    @Nested
    @DisplayName("Enchantment Removal")
    class EnchantmentRemovalTests {

        @Test
        @DisplayName("Given a builder, when removeEnchantment with Enchantment, then delegates to item stack")
        void removeEnchantment_delegatesToItemStack_whenEnchantmentProvided() {
            ItemBuilder builder = createBuilder(Material.DIAMOND_SWORD);
            ItemStack mockItem = getItemStack(builder);
            org.bukkit.enchantments.Enchantment mockEnchant = mock(org.bukkit.enchantments.Enchantment.class);

            assertSame(builder, builder.removeEnchantment(mockEnchant));

            verify(mockItem).removeEnchantment(mockEnchant);
        }
    }

    @Nested
    @DisplayName("Placeholder Config Generation")
    class PlaceholderConfigTests {

        @SuppressWarnings("unchecked")
        private List<TextReplacementConfig> invokeGetPlaceholdersAsConfig(ItemBuilder builder) {
            try {
                Method method = BaseItemBuilder.class.getDeclaredMethod("getPlaceholdersAsConfig");
                method.setAccessible(true);
                return (List<TextReplacementConfig>) method.invoke(builder);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("Given no placeholders, when getPlaceholdersAsConfig, then returns empty list")
        void getPlaceholdersAsConfig_returnsEmptyList_whenNoPlaceholders() {
            ItemBuilder builder = createBuilder(Material.STONE);
            List<TextReplacementConfig> configs = invokeGetPlaceholdersAsConfig(builder);
            assertNotNull(configs);
            assertTrue(configs.isEmpty());
        }

        @Test
        @DisplayName("Given one placeholder, when getPlaceholdersAsConfig, then returns one config")
        void getPlaceholdersAsConfig_returnsOneConfig_whenOnePlaceholder() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.addPlaceholder("level", "5");
            List<TextReplacementConfig> configs = invokeGetPlaceholdersAsConfig(builder);
            assertEquals(1, configs.size());
        }

        @Test
        @DisplayName("Given multiple placeholders, when getPlaceholdersAsConfig, then returns matching number of configs")
        void getPlaceholdersAsConfig_returnsMatchingCount_whenMultiplePlaceholders() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.addPlaceholder("level", "5");
            builder.addPlaceholder("name", "Sword");
            builder.addPlaceholder("rarity", "Rare");
            List<TextReplacementConfig> configs = invokeGetPlaceholdersAsConfig(builder);
            assertEquals(3, configs.size());
        }

        @Test
        @DisplayName("Given a placeholder, when applying config to component, then placeholder is replaced")
        void getPlaceholdersAsConfig_replacesPlaceholderInComponent_whenApplied() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.addPlaceholder("level", "10");
            List<TextReplacementConfig> configs = invokeGetPlaceholdersAsConfig(builder);

            Component original = Component.text("Level: <level>");
            Component result = original;
            for (TextReplacementConfig config : configs) {
                result = result.replaceText(config);
            }
            String plain = PlainTextComponentSerializer.plainText().serialize(result);
            assertEquals("Level: 10", plain);
        }
    }

    @Nested
    @DisplayName("Component Parsing")
    class ParseComponentTests {

        private Component invokeParseComponent(ItemBuilder builder, Component message) {
            try {
                Method method = BaseItemBuilder.class.getDeclaredMethod("parseComponent", Component.class);
                method.setAccessible(true);
                return (Component) method.invoke(builder, message);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("Given no placeholders, when parseComponent, then returns component unchanged")
        void parseComponent_returnsUnchanged_whenNoPlaceholders() {
            ItemBuilder builder = createBuilder(Material.STONE);
            Component input = Component.text("Hello World");
            Component result = invokeParseComponent(builder, input);
            String plain = PlainTextComponentSerializer.plainText().serialize(result);
            assertEquals("Hello World", plain);
        }

        @Test
        @DisplayName("Given a placeholder, when parseComponent with matching tag, then replaces the tag")
        void parseComponent_replacesTag_whenPlaceholderMatches() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.addPlaceholder("damage", "50");
            Component input = Component.text("Damage: <damage>");
            Component result = invokeParseComponent(builder, input);
            String plain = PlainTextComponentSerializer.plainText().serialize(result);
            assertEquals("Damage: 50", plain);
        }

        @Test
        @DisplayName("Given multiple placeholders, when parseComponent, then replaces all matching tags")
        void parseComponent_replacesAllTags_whenMultiplePlaceholders() {
            ItemBuilder builder = createBuilder(Material.STONE);
            builder.addPlaceholder("min", "1");
            builder.addPlaceholder("max", "10");
            Component input = Component.text("<min> to <max>");
            Component result = invokeParseComponent(builder, input);
            String plain = PlainTextComponentSerializer.plainText().serialize(result);
            assertEquals("1 to 10", plain);
        }
    }

    @Nested
    @DisplayName("Replacement Condition")
    class ReplacementConditionTests {

        private TextReplacementConfig.Condition invokeGetReplacementCondition(ItemBuilder builder) {
            try {
                Method method = BaseItemBuilder.class.getDeclaredMethod("getReplacementCondition");
                method.setAccessible(true);
                return (TextReplacementConfig.Condition) method.invoke(builder);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("Given a builder, when getReplacementCondition, then returns non-null condition")
        void getReplacementCondition_returnsNonNull() {
            ItemBuilder builder = createBuilder(Material.STONE);
            TextReplacementConfig.Condition condition = invokeGetReplacementCondition(builder);
            assertNotNull(condition);
        }

        @Test
        @DisplayName("Given the replacement condition, when applying, then always returns REPLACE")
        void getReplacementCondition_alwaysReturnsReplace() {
            ItemBuilder builder = createBuilder(Material.STONE);
            TextReplacementConfig.Condition condition = invokeGetReplacementCondition(builder);
            PatternReplacementResult result = condition.shouldReplace(null, 0, 0);
            assertEquals(PatternReplacementResult.REPLACE, result);
        }
    }
}

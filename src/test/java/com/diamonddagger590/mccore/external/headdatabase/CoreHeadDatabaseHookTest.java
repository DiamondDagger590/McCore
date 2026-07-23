package com.diamonddagger590.mccore.external.headdatabase;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreHeadDatabaseHookTest {

    @Mock
    private HeadDatabaseAPI headDatabaseAPI;

    private CoreHeadDatabaseHook hook;

    @BeforeEach
    void setUp() throws Exception {
        Unsafe unsafe = getUnsafe();
        hook = (CoreHeadDatabaseHook) unsafe.allocateInstance(CoreHeadDatabaseHook.class);
        Field apiField = CoreHeadDatabaseHook.class.getDeclaredField("headDatabaseAPI");
        apiField.setAccessible(true);
        apiField.set(hook, headDatabaseAPI);
    }

    private static Unsafe getUnsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    @Nested
    @DisplayName("isItem(String)")
    class IsItemString {

        @Test
        @DisplayName("Given a recognized head ID, when checking isItem, then returns true")
        void isItem_returnsTrue_whenApiRecognizesHead() {
            when(headDatabaseAPI.isHead("12345")).thenReturn(true);

            assertTrue(hook.isItem("12345"));
        }

        @Test
        @DisplayName("Given an unrecognized head ID, when checking isItem, then returns false")
        void isItem_returnsFalse_whenApiDoesNotRecognizeHead() {
            when(headDatabaseAPI.isHead("unknown")).thenReturn(false);

            assertFalse(hook.isItem("unknown"));
        }
    }

    @Nested
    @DisplayName("isItem(ItemStack)")
    class IsItemItemStack {

        @Test
        @DisplayName("Given an ItemStack with a head ID, when checking isItem, then returns true")
        void isItem_returnsTrue_whenItemStackHasHeadId() {
            ItemStack itemStack = mock(ItemStack.class);
            when(headDatabaseAPI.getItemID(itemStack)).thenReturn("12345");

            assertTrue(hook.isItem(itemStack));
        }

        @Test
        @DisplayName("Given an ItemStack without a head ID, when checking isItem, then returns false")
        void isItem_returnsFalse_whenItemStackHasNoHeadId() {
            ItemStack itemStack = mock(ItemStack.class);
            when(headDatabaseAPI.getItemID(itemStack)).thenReturn(null);

            assertFalse(hook.isItem(itemStack));
        }
    }

    @Nested
    @DisplayName("isItemOfType(ItemStack, String)")
    class IsItemOfType {

        @Test
        @DisplayName("Given an ItemStack matching the specified type, when checking isItemOfType, then returns true")
        void isItemOfType_returnsTrue_whenItemIdMatchesIgnoreCase() {
            ItemStack itemStack = mock(ItemStack.class);
            when(headDatabaseAPI.getItemID(itemStack)).thenReturn("MyHead");

            assertTrue(hook.isItemOfType(itemStack, "myhead"));
        }

        @Test
        @DisplayName("Given an ItemStack not matching the specified type, when checking isItemOfType, then returns false")
        void isItemOfType_returnsFalse_whenItemIdDoesNotMatch() {
            ItemStack itemStack = mock(ItemStack.class);
            when(headDatabaseAPI.getItemID(itemStack)).thenReturn("OtherHead");

            assertFalse(hook.isItemOfType(itemStack, "myhead"));
        }

        @Test
        @DisplayName("Given an ItemStack with null head ID, when checking isItemOfType, then throws NullPointerException")
        void isItemOfType_throwsNpe_whenItemIdIsNull() {
            ItemStack itemStack = mock(ItemStack.class);
            when(headDatabaseAPI.getItemID(itemStack)).thenReturn(null);

            assertThrows(NullPointerException.class, () -> hook.isItemOfType(itemStack, "myhead"));
        }
    }

    @Nested
    @DisplayName("item(String)")
    class Item {

        @Test
        @DisplayName("Given a valid head ID, when getting item, then returns present Optional")
        void item_returnsPresent_whenApiReturnsItemStack() {
            ItemStack head = mock(ItemStack.class);
            when(headDatabaseAPI.getItemHead("12345")).thenReturn(head);

            Optional<ItemStack> result = hook.item("12345");

            assertTrue(result.isPresent());
            assertEquals(head, result.get());
        }

        @Test
        @DisplayName("Given an unknown head ID, when getting item, then returns empty Optional")
        void item_returnsEmpty_whenApiReturnsNull() {
            when(headDatabaseAPI.getItemHead("unknown")).thenReturn(null);

            Optional<ItemStack> result = hook.item("unknown");

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("itemModels(ItemStack)")
    class ItemModels {

        @Test
        @DisplayName("Given an ItemStack with a head ID, when getting itemModels, then returns singleton set")
        void itemModels_returnsSingletonSet_whenItemHasHeadId() {
            ItemStack itemStack = mock(ItemStack.class);
            when(headDatabaseAPI.getItemID(itemStack)).thenReturn("12345");

            Optional<Set<String>> result = hook.itemModels(itemStack);

            assertTrue(result.isPresent());
            assertEquals(Set.of("12345"), result.get());
        }

        @Test
        @DisplayName("Given an ItemStack without a head ID, when getting itemModels, then returns empty Optional")
        void itemModels_returnsEmpty_whenItemHasNoHeadId() {
            ItemStack itemStack = mock(ItemStack.class);
            when(headDatabaseAPI.getItemID(itemStack)).thenReturn(null);

            Optional<Set<String>> result = hook.itemModels(itemStack);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("itemName(CustomItemWrapper)")
    class ItemName {

        @Test
        @DisplayName("Given a custom item with a display name, when getting itemName, then returns the display name text")
        void itemName_returnsDisplayName_whenHeadHasDisplayName() {
            CustomItemWrapper wrapper = new TestCustomItemWrapper("hdb:fancy_head", null);
            ItemStack head = mock(ItemStack.class);
            ItemMeta meta = mock(ItemMeta.class);
            when(headDatabaseAPI.getItemHead("hdb:fancy_head")).thenReturn(head);
            when(head.hasItemMeta()).thenReturn(true);
            when(head.getItemMeta()).thenReturn(meta);
            when(meta.hasDisplayName()).thenReturn(true);
            when(meta.displayName()).thenReturn(Component.text("Fancy Head"));

            String result = hook.itemName(wrapper);

            assertEquals("Fancy Head", result);
        }

        @Test
        @DisplayName("Given a custom item whose head has no display name, when getting itemName, then returns formatted head ID")
        void itemName_returnsFormattedId_whenHeadHasNoDisplayName() {
            CustomItemWrapper wrapper = new TestCustomItemWrapper("hdb:golden_crown", null);
            ItemStack head = mock(ItemStack.class);
            ItemMeta meta = mock(ItemMeta.class);
            when(headDatabaseAPI.getItemHead("hdb:golden_crown")).thenReturn(head);
            when(head.hasItemMeta()).thenReturn(true);
            when(head.getItemMeta()).thenReturn(meta);
            when(meta.hasDisplayName()).thenReturn(false);

            String result = hook.itemName(wrapper);

            assertEquals("Golden Crown", result);
        }

        @Test
        @DisplayName("Given a custom item whose head is null, when getting itemName, then returns formatted head ID")
        void itemName_returnsFormattedId_whenHeadIsNull() {
            CustomItemWrapper wrapper = new TestCustomItemWrapper("missing_head", null);
            when(headDatabaseAPI.getItemHead("missing_head")).thenReturn(null);

            String result = hook.itemName(wrapper);

            assertEquals("Missing Head", result);
        }

        @Test
        @DisplayName("Given a custom item whose display name is empty, when getting itemName, then returns formatted head ID")
        void itemName_returnsFormattedId_whenDisplayNameIsEmpty() {
            CustomItemWrapper wrapper = new TestCustomItemWrapper("ns:empty_name", null);
            ItemStack head = mock(ItemStack.class);
            ItemMeta meta = mock(ItemMeta.class);
            when(headDatabaseAPI.getItemHead("ns:empty_name")).thenReturn(head);
            when(head.hasItemMeta()).thenReturn(true);
            when(head.getItemMeta()).thenReturn(meta);
            when(meta.hasDisplayName()).thenReturn(true);
            when(meta.displayName()).thenReturn(Component.empty());

            String result = hook.itemName(wrapper);

            assertEquals("Empty Name", result);
        }

        @Test
        @DisplayName("Given a custom item whose head has no item meta, when getting itemName, then returns formatted head ID")
        void itemName_returnsFormattedId_whenHeadHasNoMeta() {
            CustomItemWrapper wrapper = new TestCustomItemWrapper("some_item", null);
            ItemStack head = mock(ItemStack.class);
            when(headDatabaseAPI.getItemHead("some_item")).thenReturn(head);
            when(head.hasItemMeta()).thenReturn(false);

            String result = hook.itemName(wrapper);

            assertEquals("Some Item", result);
        }

        @Test
        @DisplayName("Given a vanilla material wrapper, when getting itemName, then returns formatted material name")
        void itemName_returnsFormattedMaterial_whenNoCustomItem() {
            CustomItemWrapper wrapper = new TestCustomItemWrapper(null, Material.DIAMOND_SWORD);

            String result = hook.itemName(wrapper);

            assertEquals("Diamond Sword", result);
        }
    }

    @Nested
    @DisplayName("formatItemId (via itemName)")
    class FormatItemId {

        @Test
        @DisplayName("Given a namespaced ID with underscores, when formatting, then strips namespace and title-cases")
        void formatItemId_stripsNamespaceAndTitleCases() {
            CustomItemWrapper wrapper = new TestCustomItemWrapper("hdb:iron_golem_head", null);
            when(headDatabaseAPI.getItemHead("hdb:iron_golem_head")).thenReturn(null);

            assertEquals("Iron Golem Head", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given an ID without a namespace, when formatting, then title-cases directly")
        void formatItemId_titleCasesWithoutNamespace() {
            CustomItemWrapper wrapper = new TestCustomItemWrapper("simple_item", null);
            when(headDatabaseAPI.getItemHead("simple_item")).thenReturn(null);

            assertEquals("Simple Item", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a single-word ID, when formatting, then capitalizes the word")
        void formatItemId_capitalizeSingleWord() {
            CustomItemWrapper wrapper = new TestCustomItemWrapper("diamond", null);
            when(headDatabaseAPI.getItemHead("diamond")).thenReturn(null);

            assertEquals("Diamond", hook.itemName(wrapper));
        }
    }

    @Nested
    @DisplayName("formatMaterial (via itemName)")
    class FormatMaterial {

        @Test
        @DisplayName("Given a multi-word material, when formatting, then returns title-cased string")
        void formatMaterial_titleCasesMultiWordMaterial() {
            CustomItemWrapper wrapper = new TestCustomItemWrapper(null, Material.GOLDEN_APPLE);

            assertEquals("Golden Apple", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a single-word material, when formatting, then capitalizes the word")
        void formatMaterial_capitalizesSingleWord() {
            CustomItemWrapper wrapper = new TestCustomItemWrapper(null, Material.STONE);

            assertEquals("Stone", hook.itemName(wrapper));
        }
    }

    private static class TestCustomItemWrapper extends CustomItemWrapper {

        private final String testCustomItem;

        TestCustomItemWrapper(String customItem, Material material) {
            super(material != null ? material : Material.STONE);
            this.testCustomItem = customItem;
        }

        @Override
        public Optional<String> customItem() {
            return Optional.ofNullable(testCustomItem);
        }

        @Override
        public Optional<Material> material() {
            if (testCustomItem != null) {
                return Optional.empty();
            }
            return super.material();
        }
    }
}

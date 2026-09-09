package com.diamonddagger590.mccore.external.headdatabase;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreHeadDatabaseHookTest {

    @Mock
    private CorePlugin mockPlugin;

    @Nested
    @DisplayName("isItem(String)")
    class IsItemByString {

        @Test
        @DisplayName("Given head ID recognized by HeadDatabaseAPI, when checking isItem, then returns true")
        void isItem_returnsTrue_whenHeadIdRecognized() {
            try (MockedConstruction<HeadDatabaseAPI> construction = mockConstruction(HeadDatabaseAPI.class,
                (mock, context) -> when(mock.isHead("12345")).thenReturn(true))) {

                CoreHeadDatabaseHook hook = new CoreHeadDatabaseHook(mockPlugin);
                assertTrue(hook.isItem("12345"));
            }
        }

        @Test
        @DisplayName("Given head ID not recognized by HeadDatabaseAPI, when checking isItem, then returns false")
        void isItem_returnsFalse_whenHeadIdNotRecognized() {
            try (MockedConstruction<HeadDatabaseAPI> construction = mockConstruction(HeadDatabaseAPI.class,
                (mock, context) -> when(mock.isHead(anyString())).thenReturn(false))) {

                CoreHeadDatabaseHook hook = new CoreHeadDatabaseHook(mockPlugin);
                assertFalse(hook.isItem("unknown"));
            }
        }
    }

    @Nested
    @DisplayName("isItem(ItemStack)")
    class IsItemByItemStack {

        @Test
        @DisplayName("Given ItemStack recognized by HeadDatabaseAPI, when checking isItem, then returns true")
        void isItem_returnsTrue_whenItemStackRecognized() {
            ItemStack mockItemStack = mock(ItemStack.class);
            try (MockedConstruction<HeadDatabaseAPI> construction = mockConstruction(HeadDatabaseAPI.class,
                (mock, context) -> when(mock.getItemID(mockItemStack)).thenReturn("12345"))) {

                CoreHeadDatabaseHook hook = new CoreHeadDatabaseHook(mockPlugin);
                assertTrue(hook.isItem(mockItemStack));
            }
        }

        @Test
        @DisplayName("Given ItemStack not recognized by HeadDatabaseAPI, when checking isItem, then returns false")
        void isItem_returnsFalse_whenItemStackNotRecognized() {
            ItemStack mockItemStack = mock(ItemStack.class);
            try (MockedConstruction<HeadDatabaseAPI> construction = mockConstruction(HeadDatabaseAPI.class,
                (mock, context) -> when(mock.getItemID(mockItemStack)).thenReturn(null))) {

                CoreHeadDatabaseHook hook = new CoreHeadDatabaseHook(mockPlugin);
                assertFalse(hook.isItem(mockItemStack));
            }
        }
    }

    @Nested
    @DisplayName("isItemOfType")
    class IsItemOfType {

        @Test
        @DisplayName("Given ItemStack matching item name, when checking isItemOfType, then returns true")
        void isItemOfType_returnsTrue_whenItemIdMatchesIgnoreCase() {
            ItemStack mockItemStack = mock(ItemStack.class);
            try (MockedConstruction<HeadDatabaseAPI> construction = mockConstruction(HeadDatabaseAPI.class,
                (mock, context) -> when(mock.getItemID(mockItemStack)).thenReturn("MyHead"))) {

                CoreHeadDatabaseHook hook = new CoreHeadDatabaseHook(mockPlugin);
                assertTrue(hook.isItemOfType(mockItemStack, "myhead"));
            }
        }

        @Test
        @DisplayName("Given ItemStack not matching item name, when checking isItemOfType, then returns false")
        void isItemOfType_returnsFalse_whenItemIdDoesNotMatch() {
            ItemStack mockItemStack = mock(ItemStack.class);
            try (MockedConstruction<HeadDatabaseAPI> construction = mockConstruction(HeadDatabaseAPI.class,
                (mock, context) -> when(mock.getItemID(mockItemStack)).thenReturn("OtherHead"))) {

                CoreHeadDatabaseHook hook = new CoreHeadDatabaseHook(mockPlugin);
                assertFalse(hook.isItemOfType(mockItemStack, "myhead"));
            }
        }

        @Test
        @DisplayName("Given ItemStack not recognized by HeadDatabaseAPI, when checking isItemOfType, then throws NullPointerException")
        void isItemOfType_throwsNpe_whenItemIdIsNull() {
            ItemStack mockItemStack = mock(ItemStack.class);
            try (MockedConstruction<HeadDatabaseAPI> construction = mockConstruction(HeadDatabaseAPI.class,
                (mock, context) -> when(mock.getItemID(mockItemStack)).thenReturn(null))) {

                CoreHeadDatabaseHook hook = new CoreHeadDatabaseHook(mockPlugin);
                assertThrows(NullPointerException.class, () -> hook.isItemOfType(mockItemStack, "anything"));
            }
        }
    }

    @Nested
    @DisplayName("item")
    class Item {

        @Test
        @DisplayName("Given valid head ID, when getting item, then returns present Optional with ItemStack")
        void item_returnsPresent_whenHeadIdValid() {
            ItemStack headItem = mock(ItemStack.class);
            try (MockedConstruction<HeadDatabaseAPI> construction = mockConstruction(HeadDatabaseAPI.class,
                (mock, context) -> when(mock.getItemHead("12345")).thenReturn(headItem))) {

                CoreHeadDatabaseHook hook = new CoreHeadDatabaseHook(mockPlugin);
                Optional<ItemStack> result = hook.item("12345");
                assertTrue(result.isPresent());
                assertEquals(headItem, result.get());
            }
        }

        @Test
        @DisplayName("Given invalid head ID, when getting item, then returns empty Optional")
        void item_returnsEmpty_whenHeadIdInvalid() {
            try (MockedConstruction<HeadDatabaseAPI> construction = mockConstruction(HeadDatabaseAPI.class,
                (mock, context) -> when(mock.getItemHead("invalid")).thenReturn(null))) {

                CoreHeadDatabaseHook hook = new CoreHeadDatabaseHook(mockPlugin);
                Optional<ItemStack> result = hook.item("invalid");
                assertFalse(result.isPresent());
            }
        }
    }

    @Nested
    @DisplayName("itemModels")
    class ItemModels {

        @Test
        @DisplayName("Given ItemStack with head ID, when getting itemModels, then returns set containing the ID")
        void itemModels_returnsSetWithId_whenItemHasHeadId() {
            ItemStack mockItemStack = mock(ItemStack.class);
            try (MockedConstruction<HeadDatabaseAPI> construction = mockConstruction(HeadDatabaseAPI.class,
                (mock, context) -> when(mock.getItemID(mockItemStack)).thenReturn("head_42"))) {

                CoreHeadDatabaseHook hook = new CoreHeadDatabaseHook(mockPlugin);
                Optional<Set<String>> result = hook.itemModels(mockItemStack);
                assertTrue(result.isPresent());
                assertEquals(Set.of("head_42"), result.get());
            }
        }

        @Test
        @DisplayName("Given ItemStack without head ID, when getting itemModels, then returns empty Optional")
        void itemModels_returnsEmpty_whenItemHasNoHeadId() {
            ItemStack mockItemStack = mock(ItemStack.class);
            try (MockedConstruction<HeadDatabaseAPI> construction = mockConstruction(HeadDatabaseAPI.class,
                (mock, context) -> when(mock.getItemID(mockItemStack)).thenReturn(null))) {

                CoreHeadDatabaseHook hook = new CoreHeadDatabaseHook(mockPlugin);
                Optional<Set<String>> result = hook.itemModels(mockItemStack);
                assertFalse(result.isPresent());
            }
        }
    }

    @Nested
    @DisplayName("itemName")
    class ItemName {

        @Test
        @DisplayName("Given custom item with head that has display name, when getting itemName, then returns display name")
        void itemName_returnsDisplayName_whenHeadHasDisplayName() {
            try (MockedConstruction<HeadDatabaseAPI> construction = mockConstruction(HeadDatabaseAPI.class,
                (mock, context) -> {
                    ItemStack headItem = mock(ItemStack.class);
                    ItemMeta headMeta = mock(ItemMeta.class);
                    when(mock.getItemHead("head_1")).thenReturn(headItem);
                    when(headItem.hasItemMeta()).thenReturn(true);
                    when(headItem.getItemMeta()).thenReturn(headMeta);
                    when(headMeta.hasDisplayName()).thenReturn(true);
                    when(headMeta.displayName()).thenReturn(Component.text("Cool Head"));
                })) {

                CoreHeadDatabaseHook hook = new CoreHeadDatabaseHook(mockPlugin);
                CustomItemWrapper wrapper = createCustomItemWrapper("head_1");
                assertEquals("Cool Head", hook.itemName(wrapper));
            }
        }

        @Test
        @DisplayName("Given custom item with head that has empty display name, when getting itemName, then returns formatted ID")
        void itemName_returnsFormattedId_whenDisplayNameIsEmpty() {
            try (MockedConstruction<HeadDatabaseAPI> construction = mockConstruction(HeadDatabaseAPI.class,
                (mock, context) -> {
                    ItemStack headItem = mock(ItemStack.class);
                    ItemMeta headMeta = mock(ItemMeta.class);
                    when(mock.getItemHead("cool_head")).thenReturn(headItem);
                    when(headItem.hasItemMeta()).thenReturn(true);
                    when(headItem.getItemMeta()).thenReturn(headMeta);
                    when(headMeta.hasDisplayName()).thenReturn(true);
                    when(headMeta.displayName()).thenReturn(Component.empty());
                })) {

                CoreHeadDatabaseHook hook = new CoreHeadDatabaseHook(mockPlugin);
                CustomItemWrapper wrapper = createCustomItemWrapper("cool_head");
                assertEquals("Cool Head", hook.itemName(wrapper));
            }
        }

        @Test
        @DisplayName("Given custom item with head that has no display name, when getting itemName, then returns formatted ID")
        void itemName_returnsFormattedId_whenHeadHasNoDisplayName() {
            try (MockedConstruction<HeadDatabaseAPI> construction = mockConstruction(HeadDatabaseAPI.class,
                (mock, context) -> {
                    ItemStack headItem = mock(ItemStack.class);
                    ItemMeta headMeta = mock(ItemMeta.class);
                    when(mock.getItemHead("dragon_skull")).thenReturn(headItem);
                    when(headItem.hasItemMeta()).thenReturn(true);
                    when(headItem.getItemMeta()).thenReturn(headMeta);
                    when(headMeta.hasDisplayName()).thenReturn(false);
                })) {

                CoreHeadDatabaseHook hook = new CoreHeadDatabaseHook(mockPlugin);
                CustomItemWrapper wrapper = createCustomItemWrapper("dragon_skull");
                assertEquals("Dragon Skull", hook.itemName(wrapper));
            }
        }

        @Test
        @DisplayName("Given custom item with head that has no item meta, when getting itemName, then returns formatted ID")
        void itemName_returnsFormattedId_whenHeadHasNoItemMeta() {
            try (MockedConstruction<HeadDatabaseAPI> construction = mockConstruction(HeadDatabaseAPI.class,
                (mock, context) -> {
                    ItemStack headItem = mock(ItemStack.class);
                    when(mock.getItemHead("fire_head")).thenReturn(headItem);
                    when(headItem.hasItemMeta()).thenReturn(false);
                })) {

                CoreHeadDatabaseHook hook = new CoreHeadDatabaseHook(mockPlugin);
                CustomItemWrapper wrapper = createCustomItemWrapper("fire_head");
                assertEquals("Fire Head", hook.itemName(wrapper));
            }
        }

        @Test
        @DisplayName("Given custom item with null head from API, when getting itemName, then returns formatted ID")
        void itemName_returnsFormattedId_whenHeadIsNull() {
            try (MockedConstruction<HeadDatabaseAPI> construction = mockConstruction(HeadDatabaseAPI.class,
                (mock, context) -> when(mock.getItemHead("ice_crown")).thenReturn(null))) {

                CoreHeadDatabaseHook hook = new CoreHeadDatabaseHook(mockPlugin);
                CustomItemWrapper wrapper = createCustomItemWrapper("ice_crown");
                assertEquals("Ice Crown", hook.itemName(wrapper));
            }
        }

        @Test
        @DisplayName("Given custom item with namespaced ID, when getting itemName, then strips namespace and formats")
        void itemName_stripsNamespace_whenIdContainsColon() {
            try (MockedConstruction<HeadDatabaseAPI> construction = mockConstruction(HeadDatabaseAPI.class,
                (mock, context) -> when(mock.getItemHead("hdb:golden_apple")).thenReturn(null))) {

                CoreHeadDatabaseHook hook = new CoreHeadDatabaseHook(mockPlugin);
                CustomItemWrapper wrapper = createCustomItemWrapper("hdb:golden_apple");
                assertEquals("Golden Apple", hook.itemName(wrapper));
            }
        }

        @Test
        @DisplayName("Given vanilla material wrapper, when getting itemName, then returns formatted material name")
        void itemName_returnsFormattedMaterial_whenWrapperIsVanilla() {
            try (MockedConstruction<HeadDatabaseAPI> ignored = mockConstruction(HeadDatabaseAPI.class)) {

                CoreHeadDatabaseHook hook = new CoreHeadDatabaseHook(mockPlugin);
                CustomItemWrapper wrapper = new CustomItemWrapper(Material.DIAMOND_SWORD);
                assertEquals("Diamond Sword", hook.itemName(wrapper));
            }
        }

        @Test
        @DisplayName("Given single-word material, when getting itemName, then returns title-cased word")
        void itemName_returnsTitleCased_whenMaterialIsSingleWord() {
            try (MockedConstruction<HeadDatabaseAPI> ignored = mockConstruction(HeadDatabaseAPI.class)) {

                CoreHeadDatabaseHook hook = new CoreHeadDatabaseHook(mockPlugin);
                CustomItemWrapper wrapper = new CustomItemWrapper(Material.STONE);
                assertEquals("Stone", hook.itemName(wrapper));
            }
        }
    }

    private static CustomItemWrapper createCustomItemWrapper(String customItem) {
        return new CustomItemWrapper(Material.PLAYER_HEAD) {
            @Override
            public @org.jetbrains.annotations.NotNull Optional<String> customItem() {
                return Optional.of(customItem);
            }
        };
    }
}

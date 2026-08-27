package com.diamonddagger590.mccore.builder.item;

import com.diamonddagger590.mccore.external.itemsadder.CoreItemsAdderHook;
import com.diamonddagger590.mccore.external.nexo.CoreNexoHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.plugin.CorePluginHookKey;
import com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ItemPluginTypeGetCustomItemTest {

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

    private PluginHookRegistry getPluginHookRegistry() {
        return plugin.registryAccess().registry(RegistryKey.PLUGIN_HOOK);
    }

    @Nested
    @DisplayName("NEXO getCustomItem")
    class NexoTests {

        @Test
        @DisplayName("Given Nexo hook with matching item, when getCustomItem called, then returns hook's item")
        void getCustomItem_returnsHookItem_whenNexoHookReturnsItem() {
            PluginHookRegistry hookRegistry = getPluginHookRegistry();
            CoreNexoHook mockNexoHook = mock(CoreNexoHook.class);
            ItemStack expectedItem = ItemType.DIAMOND_SWORD.createItemStack(1);
            when(mockNexoHook.item("custom_sword")).thenReturn(Optional.of(expectedItem));
            hookRegistry.register(mockNexoHook);

            ItemStack result = ItemPluginType.NEXO.getCustomItem("custom_sword");

            assertEquals(expectedItem, result);
        }

        @Test
        @DisplayName("Given Nexo hook with no matching item and valid ItemType, when getCustomItem called, then returns ItemType item")
        void getCustomItem_returnsItemTypeItem_whenNexoHookReturnsEmptyAndItemTypeValid() {
            PluginHookRegistry hookRegistry = getPluginHookRegistry();
            CoreNexoHook mockNexoHook = mock(CoreNexoHook.class);
            when(mockNexoHook.item("stone")).thenReturn(Optional.empty());
            hookRegistry.register(mockNexoHook);

            ItemStack result = ItemPluginType.NEXO.getCustomItem("stone");

            assertNotNull(result);
            assertEquals(ItemType.STONE, result.getType().asItemType());
        }

        @Test
        @DisplayName("Given no Nexo hook and valid ItemType, when getCustomItem called, then returns ItemType item")
        void getCustomItem_returnsItemTypeItem_whenNoNexoHookAndItemTypeValid() {
            ItemStack result = ItemPluginType.NEXO.getCustomItem("diamond");

            assertNotNull(result);
            assertEquals(ItemType.DIAMOND, result.getType().asItemType());
        }

        @Test
        @DisplayName("Given no Nexo hook and invalid item name, when getCustomItem called, then falls back to STONE")
        void getCustomItem_fallsBackToStone_whenNoHookAndInvalidItemName() {
            ItemStack result = ItemPluginType.NEXO.getCustomItem("not_a_real_item_xyz_123");

            assertNotNull(result);
            assertEquals(ItemType.STONE, result.getType().asItemType());
        }

        @Test
        @DisplayName("Given Nexo hook returning empty and invalid item name, when getCustomItem called, then falls back to STONE")
        void getCustomItem_fallsBackToStone_whenHookEmptyAndInvalidItemName() {
            PluginHookRegistry hookRegistry = getPluginHookRegistry();
            CoreNexoHook mockNexoHook = mock(CoreNexoHook.class);
            when(mockNexoHook.item(anyString())).thenReturn(Optional.empty());
            hookRegistry.register(mockNexoHook);

            ItemStack result = ItemPluginType.NEXO.getCustomItem("nonexistent_garbage_xyz");

            assertNotNull(result);
            assertEquals(ItemType.STONE, result.getType().asItemType());
        }
    }

    @Nested
    @DisplayName("ITEMS_ADDER getCustomItem")
    class ItemsAdderTests {

        @Test
        @DisplayName("Given ItemsAdder hook with matching item, when getCustomItem called, then returns hook's item")
        void getCustomItem_returnsHookItem_whenItemsAdderHookReturnsItem() {
            PluginHookRegistry hookRegistry = getPluginHookRegistry();
            CoreItemsAdderHook mockHook = mock(CoreItemsAdderHook.class);
            ItemStack expectedItem = ItemType.IRON_INGOT.createItemStack(1);
            when(mockHook.item("custom_ingot")).thenReturn(Optional.of(expectedItem));
            hookRegistry.register(mockHook);

            ItemStack result = ItemPluginType.ITEMS_ADDER.getCustomItem("custom_ingot");

            assertEquals(expectedItem, result);
        }

        @Test
        @DisplayName("Given ItemsAdder hook with no matching item and valid ItemType, when getCustomItem called, then returns ItemType item")
        void getCustomItem_returnsItemTypeItem_whenItemsAdderHookReturnsEmptyAndItemTypeValid() {
            PluginHookRegistry hookRegistry = getPluginHookRegistry();
            CoreItemsAdderHook mockHook = mock(CoreItemsAdderHook.class);
            when(mockHook.item("oak_log")).thenReturn(Optional.empty());
            hookRegistry.register(mockHook);

            ItemStack result = ItemPluginType.ITEMS_ADDER.getCustomItem("oak_log");

            assertNotNull(result);
            assertEquals(ItemType.OAK_LOG, result.getType().asItemType());
        }

        @Test
        @DisplayName("Given no ItemsAdder hook and valid ItemType, when getCustomItem called, then returns ItemType item")
        void getCustomItem_returnsItemTypeItem_whenNoItemsAdderHookAndItemTypeValid() {
            ItemStack result = ItemPluginType.ITEMS_ADDER.getCustomItem("iron_sword");

            assertNotNull(result);
            assertEquals(ItemType.IRON_SWORD, result.getType().asItemType());
        }

        @Test
        @DisplayName("Given no ItemsAdder hook and invalid item name, when getCustomItem called, then falls back to STONE")
        void getCustomItem_fallsBackToStone_whenNoHookAndInvalidItemName() {
            ItemStack result = ItemPluginType.ITEMS_ADDER.getCustomItem("fake_item_xyz_999");

            assertNotNull(result);
            assertEquals(ItemType.STONE, result.getType().asItemType());
        }

        @Test
        @DisplayName("Given ItemsAdder hook returning empty and invalid item name, when getCustomItem called, then falls back to STONE")
        void getCustomItem_fallsBackToStone_whenHookEmptyAndInvalidItemName() {
            PluginHookRegistry hookRegistry = getPluginHookRegistry();
            CoreItemsAdderHook mockHook = mock(CoreItemsAdderHook.class);
            when(mockHook.item(anyString())).thenReturn(Optional.empty());
            hookRegistry.register(mockHook);

            ItemStack result = ItemPluginType.ITEMS_ADDER.getCustomItem("nonexistent_garbage_abc");

            assertNotNull(result);
            assertEquals(ItemType.STONE, result.getType().asItemType());
        }
    }

    @Nested
    @DisplayName("NONE getCustomItem")
    class NoneTests {

        @Test
        @DisplayName("Given Nexo hook recognizes item and returns it, when getCustomItem called, then returns Nexo item")
        void getCustomItem_returnsNexoItem_whenNexoHookRecognizesItem() {
            PluginHookRegistry hookRegistry = getPluginHookRegistry();
            CoreNexoHook mockNexoHook = mock(CoreNexoHook.class);
            ItemStack expectedItem = ItemType.GOLDEN_APPLE.createItemStack(1);
            when(mockNexoHook.isItem("nexo_apple")).thenReturn(true);
            when(mockNexoHook.item("nexo_apple")).thenReturn(Optional.of(expectedItem));
            hookRegistry.register(mockNexoHook);

            ItemStack result = ItemPluginType.NONE.getCustomItem("nexo_apple");

            assertEquals(expectedItem, result);
        }

        @Test
        @DisplayName("Given Nexo hook recognizes item but returns empty optional, when getCustomItem called, then falls through to ItemType")
        void getCustomItem_fallsThroughToItemType_whenNexoHookReturnsEmptyItem() {
            PluginHookRegistry hookRegistry = getPluginHookRegistry();
            CoreNexoHook mockNexoHook = mock(CoreNexoHook.class);
            when(mockNexoHook.isItem("diamond")).thenReturn(true);
            when(mockNexoHook.item("diamond")).thenReturn(Optional.empty());
            hookRegistry.register(mockNexoHook);

            ItemStack result = ItemPluginType.NONE.getCustomItem("diamond");

            assertNotNull(result);
            assertEquals(ItemType.DIAMOND, result.getType().asItemType());
        }

        @Test
        @DisplayName("Given Nexo hook does not recognize item but ItemsAdder does, when getCustomItem called, then returns ItemsAdder item")
        void getCustomItem_returnsItemsAdderItem_whenNexoDoesNotRecognizeButItemsAdderDoes() {
            PluginHookRegistry hookRegistry = getPluginHookRegistry();
            CoreNexoHook mockNexoHook = mock(CoreNexoHook.class);
            when(mockNexoHook.isItem("ia_item")).thenReturn(false);
            hookRegistry.register(mockNexoHook);

            CoreItemsAdderHook mockIaHook = mock(CoreItemsAdderHook.class);
            ItemStack expectedItem = ItemType.EMERALD.createItemStack(1);
            when(mockIaHook.isItem("ia_item")).thenReturn(true);
            when(mockIaHook.item("ia_item")).thenReturn(Optional.of(expectedItem));
            hookRegistry.register(mockIaHook);

            ItemStack result = ItemPluginType.NONE.getCustomItem("ia_item");

            assertEquals(expectedItem, result);
        }

        @Test
        @DisplayName("Given ItemsAdder recognizes item but returns empty optional, when getCustomItem called, then falls through to ItemType")
        void getCustomItem_fallsThroughToItemType_whenItemsAdderReturnsEmptyItem() {
            PluginHookRegistry hookRegistry = getPluginHookRegistry();
            CoreItemsAdderHook mockIaHook = mock(CoreItemsAdderHook.class);
            when(mockIaHook.isItem("gold_ingot")).thenReturn(true);
            when(mockIaHook.item("gold_ingot")).thenReturn(Optional.empty());
            hookRegistry.register(mockIaHook);

            ItemStack result = ItemPluginType.NONE.getCustomItem("gold_ingot");

            assertNotNull(result);
            assertEquals(ItemType.GOLD_INGOT, result.getType().asItemType());
        }

        @Test
        @DisplayName("Given no hooks registered and valid ItemType, when getCustomItem called, then returns ItemType item")
        void getCustomItem_returnsItemTypeItem_whenNoHooksAndItemTypeValid() {
            ItemStack result = ItemPluginType.NONE.getCustomItem("stick");

            assertNotNull(result);
            assertEquals(ItemType.STICK, result.getType().asItemType());
        }

        @Test
        @DisplayName("Given no hooks registered and invalid item name, when getCustomItem called, then falls back to STONE")
        void getCustomItem_fallsBackToStone_whenNoHooksAndInvalidItemName() {
            ItemStack result = ItemPluginType.NONE.getCustomItem("completely_invalid_item_xyz");

            assertNotNull(result);
            assertEquals(ItemType.STONE, result.getType().asItemType());
        }

        @Test
        @DisplayName("Given both hooks present but neither recognizes item and valid ItemType, when getCustomItem called, then returns ItemType item")
        void getCustomItem_returnsItemTypeItem_whenBothHooksPresentButNeitherRecognizesAndItemTypeValid() {
            PluginHookRegistry hookRegistry = getPluginHookRegistry();
            CoreNexoHook mockNexoHook = mock(CoreNexoHook.class);
            when(mockNexoHook.isItem("coal")).thenReturn(false);
            hookRegistry.register(mockNexoHook);

            CoreItemsAdderHook mockIaHook = mock(CoreItemsAdderHook.class);
            when(mockIaHook.isItem("coal")).thenReturn(false);
            hookRegistry.register(mockIaHook);

            ItemStack result = ItemPluginType.NONE.getCustomItem("coal");

            assertNotNull(result);
            assertEquals(ItemType.COAL, result.getType().asItemType());
        }

        @Test
        @DisplayName("Given both hooks present but neither recognizes item and invalid name, when getCustomItem called, then falls back to STONE")
        void getCustomItem_fallsBackToStone_whenBothHooksPresentNeitherRecognizesAndInvalidName() {
            PluginHookRegistry hookRegistry = getPluginHookRegistry();
            CoreNexoHook mockNexoHook = mock(CoreNexoHook.class);
            when(mockNexoHook.isItem("gibberish_abc")).thenReturn(false);
            hookRegistry.register(mockNexoHook);

            CoreItemsAdderHook mockIaHook = mock(CoreItemsAdderHook.class);
            when(mockIaHook.isItem("gibberish_abc")).thenReturn(false);
            hookRegistry.register(mockIaHook);

            ItemStack result = ItemPluginType.NONE.getCustomItem("gibberish_abc");

            assertNotNull(result);
            assertEquals(ItemType.STONE, result.getType().asItemType());
        }

        @Test
        @DisplayName("Given only ItemsAdder hook registered and it recognizes item, when getCustomItem called, then returns ItemsAdder item")
        void getCustomItem_returnsItemsAdderItem_whenOnlyItemsAdderRegisteredAndRecognizes() {
            PluginHookRegistry hookRegistry = getPluginHookRegistry();
            CoreItemsAdderHook mockIaHook = mock(CoreItemsAdderHook.class);
            ItemStack expectedItem = ItemType.REDSTONE.createItemStack(1);
            when(mockIaHook.isItem("custom_redstone")).thenReturn(true);
            when(mockIaHook.item("custom_redstone")).thenReturn(Optional.of(expectedItem));
            hookRegistry.register(mockIaHook);

            ItemStack result = ItemPluginType.NONE.getCustomItem("custom_redstone");

            assertEquals(expectedItem, result);
        }
    }
}

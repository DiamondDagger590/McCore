package com.diamonddagger590.mccore.builder.item;

import com.diamonddagger590.mccore.external.itemsadder.CoreItemsAdderHook;
import com.diamonddagger590.mccore.external.nexo.CoreNexoHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry;
import com.diamonddagger590.mccore.testing.CorePluginTestHelper;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link ItemPluginType#getCustomItem(String)} lambdas across all enum constants.
 */
class ItemPluginTypeGetCustomItemTest {

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        RegistryResetExtension.setupRegistry();
        CorePluginTestHelper.installMinimalInstance();
    }

    @AfterEach
    void tearDown() {
        CorePluginTestHelper.uninstallInstance();
        RegistryResetExtension.resetRegistry();
        MockBukkit.unmock();
    }

    @SuppressWarnings("unchecked")
    private void registerHookWithKey(PluginHook<?> hook, Class<?> keyClass) throws Exception {
        PluginHookRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK);
        Field hooksField = PluginHookRegistry.class.getDeclaredField("hooks");
        hooksField.setAccessible(true);
        Map<Class<?>, PluginHook<?>> hooks = (Map<Class<?>, PluginHook<?>>) hooksField.get(registry);
        hooks.put(keyClass, hook);
    }

    private void registerNexoHook(StubNexoHook hook) throws Exception {
        registerHookWithKey(hook, CoreNexoHook.class);
    }

    private void registerItemsAdderHook(StubItemsAdderHook hook) throws Exception {
        registerHookWithKey(hook, CoreItemsAdderHook.class);
    }

    static class StubNexoHook extends CoreNexoHook {

        private final String recognizedItem;
        private final ItemStack returnedItem;

        StubNexoHook(String recognizedItem, ItemStack returnedItem) {
            super(null);
            this.recognizedItem = recognizedItem;
            this.returnedItem = returnedItem;
        }

        @Override
        public boolean isItem(@NotNull String itemName) {
            return recognizedItem != null && recognizedItem.equals(itemName);
        }

        @Override
        public boolean isItem(@NotNull ItemStack itemStack) {
            return false;
        }

        @NotNull
        @Override
        public Optional<ItemStack> item(@NotNull String itemName) {
            if (recognizedItem != null && recognizedItem.equals(itemName) && returnedItem != null) {
                return Optional.of(returnedItem);
            }
            return Optional.empty();
        }

        @NotNull
        @Override
        public Optional<Set<String>> itemModels(@NotNull ItemStack itemStack) {
            return Optional.empty();
        }

        @NotNull
        @Override
        public String itemName(@NotNull CustomItemWrapper customItemWrapper) {
            return "Test Item";
        }
    }

    static class StubItemsAdderHook extends CoreItemsAdderHook {

        private final String recognizedItem;
        private final ItemStack returnedItem;

        StubItemsAdderHook(String recognizedItem, ItemStack returnedItem) {
            super(null);
            this.recognizedItem = recognizedItem;
            this.returnedItem = returnedItem;
        }

        @Override
        public boolean isItem(@NotNull String itemName) {
            return recognizedItem != null && recognizedItem.equals(itemName);
        }

        @Override
        public boolean isItem(@NotNull ItemStack itemStack) {
            return false;
        }

        @NotNull
        @Override
        public Optional<ItemStack> item(@NotNull String itemName) {
            if (recognizedItem != null && recognizedItem.equals(itemName) && returnedItem != null) {
                return Optional.of(returnedItem);
            }
            return Optional.empty();
        }

        @NotNull
        @Override
        public Optional<Set<String>> itemModels(@NotNull ItemStack itemStack) {
            return Optional.empty();
        }

        @NotNull
        @Override
        public String itemName(@NotNull CustomItemWrapper customItemWrapper) {
            return "Test Item";
        }
    }

    @Nested
    @DisplayName("NEXO getCustomItem")
    class NexoGetCustomItem {

        @Test
        @DisplayName("Given Nexo hook present and item found, when getting custom item, then returns hook item")
        void getCustomItem_returnsHookItem_whenNexoHookPresentAndItemFound() throws Exception {
            ItemStack expected = new ItemStack(Material.DIAMOND_SWORD);
            registerNexoHook(new StubNexoHook("nexo:custom_sword", expected));

            ItemStack result = ItemPluginType.NEXO.getCustomItem("nexo:custom_sword");
            assertNotNull(result);
            assertEquals(Material.DIAMOND_SWORD, result.getType());
        }

        @Test
        @DisplayName("Given Nexo hook present but item not found, when getting custom item with vanilla name, then falls back to vanilla ItemType")
        void getCustomItem_fallsBackToVanilla_whenNexoHookPresentButItemNotFound() throws Exception {
            registerNexoHook(new StubNexoHook("nexo:other", null));

            ItemStack result = ItemPluginType.NEXO.getCustomItem("stone");
            assertNotNull(result);
            assertEquals(Material.STONE, result.getType());
        }

        @Test
        @DisplayName("Given no Nexo hook and vanilla item name, when getting custom item, then returns vanilla item")
        void getCustomItem_returnsVanillaItem_whenNoHookAndVanillaName() {
            ItemStack result = ItemPluginType.NEXO.getCustomItem("diamond");
            assertNotNull(result);
            assertEquals(Material.DIAMOND, result.getType());
        }

        @Test
        @DisplayName("Given no Nexo hook and invalid item name, when getting custom item, then returns stone fallback")
        void getCustomItem_returnsStoneFallback_whenNoHookAndInvalidName() {
            ItemStack result = ItemPluginType.NEXO.getCustomItem("completely_invalid_item_xyz_12345");
            assertNotNull(result);
            assertEquals(Material.STONE, result.getType());
        }
    }

    @Nested
    @DisplayName("ITEMS_ADDER getCustomItem")
    class ItemsAdderGetCustomItem {

        @Test
        @DisplayName("Given ItemsAdder hook present and item found, when getting custom item, then returns hook item")
        void getCustomItem_returnsHookItem_whenItemsAdderHookPresentAndItemFound() throws Exception {
            ItemStack expected = new ItemStack(Material.IRON_SWORD);
            registerItemsAdderHook(new StubItemsAdderHook("ia:custom_sword", expected));

            ItemStack result = ItemPluginType.ITEMS_ADDER.getCustomItem("ia:custom_sword");
            assertNotNull(result);
            assertEquals(Material.IRON_SWORD, result.getType());
        }

        @Test
        @DisplayName("Given ItemsAdder hook present but item not found, when getting custom item with vanilla name, then falls back to vanilla ItemType")
        void getCustomItem_fallsBackToVanilla_whenItemsAdderHookPresentButItemNotFound() throws Exception {
            registerItemsAdderHook(new StubItemsAdderHook("ia:other", null));

            ItemStack result = ItemPluginType.ITEMS_ADDER.getCustomItem("iron_ingot");
            assertNotNull(result);
            assertEquals(Material.IRON_INGOT, result.getType());
        }

        @Test
        @DisplayName("Given no ItemsAdder hook and vanilla item name, when getting custom item, then returns vanilla item")
        void getCustomItem_returnsVanillaItem_whenNoHookAndVanillaName() {
            ItemStack result = ItemPluginType.ITEMS_ADDER.getCustomItem("gold_ingot");
            assertNotNull(result);
            assertEquals(Material.GOLD_INGOT, result.getType());
        }

        @Test
        @DisplayName("Given no ItemsAdder hook and invalid item name, when getting custom item, then returns stone fallback")
        void getCustomItem_returnsStoneFallback_whenNoHookAndInvalidName() {
            ItemStack result = ItemPluginType.ITEMS_ADDER.getCustomItem("completely_invalid_item_xyz_12345");
            assertNotNull(result);
            assertEquals(Material.STONE, result.getType());
        }
    }

    @Nested
    @DisplayName("NONE getCustomItem")
    class NoneGetCustomItem {

        @Test
        @DisplayName("Given Nexo hook present and isItem true and item found, when getting custom item, then returns Nexo hook item")
        void getCustomItem_returnsNexoItem_whenNexoRecognizesItem() throws Exception {
            ItemStack expected = new ItemStack(Material.EMERALD);
            registerNexoHook(new StubNexoHook("nexo:emerald_gem", expected));

            ItemStack result = ItemPluginType.NONE.getCustomItem("nexo:emerald_gem");
            assertNotNull(result);
            assertEquals(Material.EMERALD, result.getType());
        }

        @Test
        @DisplayName("Given ItemsAdder hook present and isItem true and item found, when getting custom item, then returns ItemsAdder hook item")
        void getCustomItem_returnsItemsAdderItem_whenItemsAdderRecognizesItem() throws Exception {
            ItemStack expected = new ItemStack(Material.GOLD_INGOT);
            registerItemsAdderHook(new StubItemsAdderHook("ia:gold_bar", expected));

            ItemStack result = ItemPluginType.NONE.getCustomItem("ia:gold_bar");
            assertNotNull(result);
            assertEquals(Material.GOLD_INGOT, result.getType());
        }

        @Test
        @DisplayName("Given both hooks present but neither recognizes item, when getting custom item with vanilla name, then returns vanilla item")
        void getCustomItem_returnsVanillaItem_whenNoHookRecognizesItem() throws Exception {
            registerNexoHook(new StubNexoHook("nexo:other", null));
            registerItemsAdderHook(new StubItemsAdderHook("ia:other", null));

            ItemStack result = ItemPluginType.NONE.getCustomItem("oak_planks");
            assertNotNull(result);
            assertEquals(Material.OAK_PLANKS, result.getType());
        }

        @Test
        @DisplayName("Given no hooks and vanilla item name, when getting custom item, then returns vanilla item")
        void getCustomItem_returnsVanillaItem_whenNoHooksRegistered() {
            ItemStack result = ItemPluginType.NONE.getCustomItem("cobblestone");
            assertNotNull(result);
            assertEquals(Material.COBBLESTONE, result.getType());
        }

        @Test
        @DisplayName("Given no hooks and invalid item name, when getting custom item, then returns stone fallback")
        void getCustomItem_returnsStoneFallback_whenNoHooksAndInvalidName() {
            ItemStack result = ItemPluginType.NONE.getCustomItem("completely_invalid_item_xyz_12345");
            assertNotNull(result);
            assertEquals(Material.STONE, result.getType());
        }

        @Test
        @DisplayName("Given Nexo hook recognizes item but item() returns empty, when getting custom item with vanilla name, then falls through to vanilla")
        void getCustomItem_fallsThrough_whenNexoRecognizesButItemIsEmpty() throws Exception {
            registerNexoHook(new StubNexoHook("stone", null) {
                @Override
                public boolean isItem(@NotNull String itemName) {
                    return "stone".equals(itemName);
                }
            });

            ItemStack result = ItemPluginType.NONE.getCustomItem("stone");
            assertNotNull(result);
            assertEquals(Material.STONE, result.getType());
        }

        @Test
        @DisplayName("Given ItemsAdder hook recognizes item but item() returns empty, when getting custom item with vanilla name, then falls through to vanilla")
        void getCustomItem_fallsThrough_whenItemsAdderRecognizesButItemIsEmpty() throws Exception {
            registerItemsAdderHook(new StubItemsAdderHook("iron_ingot", null) {
                @Override
                public boolean isItem(@NotNull String itemName) {
                    return "iron_ingot".equals(itemName);
                }
            });

            ItemStack result = ItemPluginType.NONE.getCustomItem("iron_ingot");
            assertNotNull(result);
            assertEquals(Material.IRON_INGOT, result.getType());
        }
    }
}

package com.diamonddagger590.mccore.util.item;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.external.common.CustomItemHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomItemWrapperMockBukkitTest {

    private ServerMock server;
    private TestCorePlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestCorePlugin.class);
        RegistryResetExtension.setupRegistry();
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
        MockBukkit.unmock();
    }

    private static CustomItemWrapper customItemWrapper(String customItem) throws Exception {
        CustomItemWrapper wrapper = new CustomItemWrapper(Material.AIR);
        Field materialField = CustomItemWrapper.class.getDeclaredField("material");
        materialField.setAccessible(true);
        materialField.set(wrapper, null);
        Field customItemField = CustomItemWrapper.class.getDeclaredField("customItem");
        customItemField.setAccessible(true);
        customItemField.set(wrapper, customItem);
        return wrapper;
    }

    static class TestCustomItemPluginHook extends PluginHook<CorePlugin> implements CustomItemHook {

        private final String recognizedItem;
        private final ItemStack providedItem;

        TestCustomItemPluginHook(String recognizedItem, ItemStack providedItem) {
            super(null);
            this.recognizedItem = recognizedItem;
            this.providedItem = providedItem;
        }

        @Override
        public boolean isItem(@NotNull String itemName) {
            return recognizedItem.equals(itemName);
        }

        @Override
        public boolean isItem(@NotNull ItemStack itemStack) {
            return providedItem != null && providedItem.isSimilar(itemStack);
        }

        @Override
        public boolean isItemOfType(@NotNull ItemStack itemStack, @NotNull String itemName) {
            return recognizedItem.equals(itemName) && providedItem != null && providedItem.isSimilar(itemStack);
        }

        @NotNull
        @Override
        public Optional<ItemStack> item(@NotNull String itemName) {
            if (recognizedItem.equals(itemName) && providedItem != null) {
                return Optional.of(providedItem);
            }
            return Optional.empty();
        }

        @NotNull
        @Override
        public Optional<Set<String>> itemModels(@NotNull ItemStack itemStack) {
            if (providedItem != null && providedItem.isSimilar(itemStack)) {
                return Optional.of(Set.of(recognizedItem));
            }
            return Optional.empty();
        }

        @NotNull
        @Override
        public String itemName(@NotNull CustomItemWrapper customItemWrapper) {
            return "Ruby Sword";
        }
    }

    @Nested
    @DisplayName("String constructor")
    class StringConstructor {

        @Test
        @DisplayName("Given a vanilla item name, when constructing, then sets material and null customItem")
        void constructor_setsMaterial_whenVanillaItemName() {
            CustomItemWrapper wrapper = new CustomItemWrapper("stone");
            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.STONE, wrapper.material().get());
            assertFalse(wrapper.customItem().isPresent());
        }

        @Test
        @DisplayName("Given a non-vanilla item name, when constructing, then sets customItem and null material")
        void constructor_setsCustomItem_whenNonVanillaItemName() {
            CustomItemWrapper wrapper = new CustomItemWrapper("totally_fake_item");
            assertFalse(wrapper.material().isPresent());
            assertTrue(wrapper.customItem().isPresent());
            assertEquals("totally_fake_item", wrapper.customItem().get());
        }

        @Test
        @DisplayName("Given another vanilla item name, when constructing, then correctly identifies it as vanilla")
        void constructor_setsMaterial_whenAnotherVanillaItemName() {
            CustomItemWrapper wrapper = new CustomItemWrapper("diamond");
            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.DIAMOND, wrapper.material().get());
        }
    }

    @Nested
    @DisplayName("itemName for vanilla material")
    class ItemNameVanilla {

        @Test
        @DisplayName("Given a vanilla material wrapper, when getting itemName, then returns lang tag with translation key")
        void itemName_returnsLangTag_whenVanillaMaterial() {
            CustomItemWrapper wrapper = new CustomItemWrapper(Material.STONE);
            String name = wrapper.itemName();
            assertEquals("<lang:" + Material.STONE.translationKey() + ">", name);
        }

        @Test
        @DisplayName("Given a different vanilla material, when getting itemName, then returns correct lang tag")
        void itemName_returnsCorrectLangTag_whenDifferentVanillaMaterial() {
            CustomItemWrapper wrapper = new CustomItemWrapper(Material.IRON_INGOT);
            String name = wrapper.itemName();
            assertEquals("<lang:" + Material.IRON_INGOT.translationKey() + ">", name);
        }
    }

    @Nested
    @DisplayName("itemBuilder")
    class ItemBuilderTests {

        @Test
        @DisplayName("Given a material wrapper, when getting itemBuilder, then returns builder with material ItemStack")
        void itemBuilder_returnsBuilderWithMaterial_whenMaterialWrapper() {
            CustomItemWrapper wrapper = new CustomItemWrapper(Material.DIAMOND);
            ItemBuilder builder = wrapper.itemBuilder();
            assertNotNull(builder);
            ItemStack item = builder.asItemStack();
            assertEquals(Material.DIAMOND, item.getType());
        }

        @Test
        @DisplayName("Given a custom item with a registered hook returning an item, when getting itemBuilder, then returns builder from hook")
        void itemBuilder_returnsBuilderFromHook_whenCustomItemHookRegistered() throws Exception {
            ItemStack hookItem = new ItemStack(Material.DIAMOND_SWORD);
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomItemPluginHook("nexo:ruby_sword", hookItem));
            CustomItemWrapper wrapper = customItemWrapper("nexo:ruby_sword");
            ItemBuilder builder = wrapper.itemBuilder();
            assertNotNull(builder);
            ItemStack item = builder.asItemStack();
            assertEquals(Material.DIAMOND_SWORD, item.getType());
        }

        @Test
        @DisplayName("Given a custom item with no hooks, when getting itemBuilder, then returns builder with AIR")
        void itemBuilder_returnsBuilderWithAir_whenNoHooksRegistered() throws Exception {
            CustomItemWrapper wrapper = customItemWrapper("nexo:ruby_sword");
            ItemBuilder builder = wrapper.itemBuilder();
            assertNotNull(builder);
            ItemStack item = builder.asItemStack();
            assertEquals(Material.AIR, item.getType());
        }

        @Test
        @DisplayName("Given a custom item with a hook that does not match, when getting itemBuilder, then returns builder with AIR")
        void itemBuilder_returnsBuilderWithAir_whenHookDoesNotMatch() throws Exception {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomItemPluginHook("nexo:other_item", new ItemStack(Material.GOLD_INGOT)));
            CustomItemWrapper wrapper = customItemWrapper("nexo:ruby_sword");
            ItemBuilder builder = wrapper.itemBuilder();
            assertNotNull(builder);
            ItemStack item = builder.asItemStack();
            assertEquals(Material.AIR, item.getType());
        }
    }

    @Nested
    @DisplayName("equals(ItemStack)")
    class EqualsItemStack {

        @Test
        @DisplayName("Given a material wrapper matching an ItemStack type, when comparing, then returns true")
        void equals_returnsTrue_whenMaterialMatchesItemStack() {
            CustomItemWrapper wrapper = new CustomItemWrapper(Material.STONE);
            ItemStack itemStack = new ItemStack(Material.STONE);
            assertTrue(wrapper.equals(itemStack));
        }

        @Test
        @DisplayName("Given a material wrapper not matching an ItemStack type, when comparing, then returns false")
        void equals_returnsFalse_whenMaterialDoesNotMatchItemStack() {
            CustomItemWrapper wrapper = new CustomItemWrapper(Material.STONE);
            ItemStack itemStack = new ItemStack(Material.DIRT);
            assertFalse(wrapper.equals(itemStack));
        }

        @Test
        @DisplayName("Given a custom item wrapper, when comparing with ItemStack via plugin, then delegates to item plugin")
        void equals_delegatesToPlugin_whenCustomItem() throws Exception {
            CustomItemWrapper wrapper = customItemWrapper("totally_fake_item");
            ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD);
            assertFalse(wrapper.equals(itemStack));
        }
    }

    @Nested
    @DisplayName("ItemStack constructor")
    class ItemStackConstructor {

        @Test
        @DisplayName("Given a vanilla ItemStack with no hooks, when constructing, then sets material")
        void constructor_setsMaterial_whenVanillaItemStackNoHooks() {
            ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD);
            CustomItemWrapper wrapper = new CustomItemWrapper(itemStack);
            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.DIAMOND_SWORD, wrapper.material().get());
            assertFalse(wrapper.customItem().isPresent());
        }

        @Test
        @DisplayName("Given an ItemStack with a hook recognizing it as custom, when constructing, then sets customItem")
        void constructor_setsCustomItem_whenHookRecognizesItem() {
            ItemStack hookItem = new ItemStack(Material.DIAMOND_SWORD);
            TestCustomItemPluginHook hook = new TestCustomItemPluginHook("nexo:ruby_sword", hookItem);
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);
            CustomItemWrapper wrapper = new CustomItemWrapper(new ItemStack(Material.DIAMOND_SWORD));
            assertFalse(wrapper.material().isPresent());
            assertTrue(wrapper.customItem().isPresent());
            assertEquals("nexo:ruby_sword", wrapper.customItem().get());
        }

        @Test
        @DisplayName("Given an ItemStack with a hook that does not recognize it, when constructing, then sets material")
        void constructor_setsMaterial_whenHookDoesNotRecognize() {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomItemPluginHook("nexo:ruby_sword", new ItemStack(Material.DIAMOND_SWORD)));
            CustomItemWrapper wrapper = new CustomItemWrapper(new ItemStack(Material.STONE));
            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.STONE, wrapper.material().get());
            assertFalse(wrapper.customItem().isPresent());
        }
    }

    @Nested
    @DisplayName("customModels (static)")
    class CustomModels {

        @Test
        @DisplayName("Given an ItemStack with no hooks, when getting customModels, then returns empty set")
        void customModels_returnsEmptySet_whenNoHooksRegistered() {
            ItemStack itemStack = new ItemStack(Material.STONE);
            Optional<Set<String>> models = CustomItemWrapper.customModels(itemStack);
            assertTrue(models.isPresent());
            assertTrue(models.get().isEmpty());
        }

        @Test
        @DisplayName("Given an ItemStack with a hook, when getting customModels, then returns models from hook")
        void customModels_returnsModelsFromHook_whenHookRegistered() {
            ItemStack hookItem = new ItemStack(Material.DIAMOND_SWORD);
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomItemPluginHook("nexo:ruby_sword", hookItem));
            Optional<Set<String>> models = CustomItemWrapper.customModels(new ItemStack(Material.DIAMOND_SWORD));
            assertTrue(models.isPresent());
            assertTrue(models.get().contains("nexo:ruby_sword"));
        }

        @Test
        @DisplayName("Given an ItemStack with a hook that does not match, when getting customModels, then returns empty set")
        void customModels_returnsEmptySet_whenHookDoesNotMatch() {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomItemPluginHook("nexo:ruby_sword", new ItemStack(Material.DIAMOND_SWORD)));
            Optional<Set<String>> models = CustomItemWrapper.customModels(new ItemStack(Material.STONE));
            assertTrue(models.isPresent());
            assertTrue(models.get().isEmpty());
        }
    }
}

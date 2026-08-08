package com.diamonddagger590.mccore.util.item;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.external.common.CustomItemHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import com.diamonddagger590.mccore.testing.CorePluginTestHelper;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomItemWrapperTest {

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

    private static CustomItemWrapper materialWrapper(Material material) {
        return new CustomItemWrapper(material);
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
        private final ItemStack returnedItem;
        private final boolean recognizesItemStack;
        private final Set<String> models;

        TestCustomItemPluginHook() {
            this("nexo:ruby_sword", null, false, Set.of());
        }

        TestCustomItemPluginHook(String recognizedItem, ItemStack returnedItem, boolean recognizesItemStack, Set<String> models) {
            super(null);
            this.recognizedItem = recognizedItem;
            this.returnedItem = returnedItem;
            this.recognizesItemStack = recognizesItemStack;
            this.models = models;
        }

        @Override
        public boolean isItem(@NotNull String itemName) {
            return recognizedItem != null && recognizedItem.equals(itemName);
        }

        @Override
        public boolean isItem(@NotNull ItemStack itemStack) {
            return recognizesItemStack;
        }

        @Override
        public boolean isItemOfType(@NotNull ItemStack itemStack, @NotNull String itemName) {
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
            return models.isEmpty() ? Optional.empty() : Optional.of(models);
        }

        @NotNull
        @Override
        public String itemName(@NotNull CustomItemWrapper customItemWrapper) {
            return "Ruby Sword";
        }
    }

    @Nested
    @DisplayName("Material constructor")
    class MaterialConstructor {

        @Test
        @DisplayName("Given a material, when getting material, then returns that material")
        void material_returnsProvidedMaterial_whenConstructedWithMaterial() {
            CustomItemWrapper wrapper = materialWrapper(Material.DIAMOND);
            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.DIAMOND, wrapper.material().get());
        }

        @Test
        @DisplayName("Given a material, when getting customItem, then returns empty")
        void customItem_returnsEmpty_whenConstructedWithMaterial() {
            CustomItemWrapper wrapper = materialWrapper(Material.STONE);
            assertFalse(wrapper.customItem().isPresent());
        }
    }

    @Nested
    @DisplayName("Custom item accessors")
    class CustomItemAccessors {

        @Test
        @DisplayName("Given a custom item wrapper, when getting customItem, then returns the custom item id")
        void customItem_returnsId_whenConstructedWithCustomItem() throws Exception {
            CustomItemWrapper wrapper = customItemWrapper("nexo:ruby_sword");
            assertTrue(wrapper.customItem().isPresent());
            assertEquals("nexo:ruby_sword", wrapper.customItem().get());
        }

        @Test
        @DisplayName("Given a custom item wrapper, when getting material, then returns empty")
        void material_returnsEmpty_whenConstructedWithCustomItem() throws Exception {
            CustomItemWrapper wrapper = customItemWrapper("nexo:ruby_sword");
            assertFalse(wrapper.material().isPresent());
        }
    }

    @Nested
    @DisplayName("equals(Material)")
    class EqualsMaterial {

        @Test
        @DisplayName("Given matching material, when comparing, then returns true")
        void equals_returnsTrue_whenMaterialMatches() {
            CustomItemWrapper wrapper = materialWrapper(Material.IRON_INGOT);
            assertTrue(wrapper.equals(Material.IRON_INGOT));
        }

        @Test
        @DisplayName("Given different material, when comparing, then returns false")
        void equals_returnsFalse_whenMaterialDiffers() {
            CustomItemWrapper wrapper = materialWrapper(Material.IRON_INGOT);
            assertFalse(wrapper.equals(Material.GOLD_INGOT));
        }

        @Test
        @DisplayName("Given custom item wrapper, when comparing with any material, then returns false")
        void equals_returnsFalse_whenWrapperIsCustomItem() throws Exception {
            CustomItemWrapper wrapper = customItemWrapper("nexo:ruby");
            assertFalse(wrapper.equals(Material.DIAMOND));
        }
    }

    @Nested
    @DisplayName("equals(String)")
    class EqualsString {

        @Test
        @DisplayName("Given matching custom item id, when comparing, then returns true")
        void equals_returnsTrue_whenCustomItemMatches() throws Exception {
            CustomItemWrapper wrapper = customItemWrapper("nexo:ruby_sword");
            assertTrue(wrapper.equals("nexo:ruby_sword"));
        }

        @Test
        @DisplayName("Given different custom item id, when comparing, then returns false")
        void equals_returnsFalse_whenCustomItemDiffers() throws Exception {
            CustomItemWrapper wrapper = customItemWrapper("nexo:ruby_sword");
            assertFalse(wrapper.equals("nexo:diamond_sword"));
        }

        @Test
        @DisplayName("Given material wrapper, when comparing with any string, then returns false")
        void equals_returnsFalse_whenWrapperIsMaterial() {
            CustomItemWrapper wrapper = materialWrapper(Material.STONE);
            assertFalse(wrapper.equals("stone"));
        }
    }

    @Nested
    @DisplayName("equals(CustomItemWrapper)")
    class EqualsWrapper {

        @Test
        @DisplayName("Given two material wrappers with same material, when comparing, then returns true")
        void equals_returnsTrue_whenBothMaterialsMatch() {
            CustomItemWrapper a = materialWrapper(Material.DIAMOND);
            CustomItemWrapper b = materialWrapper(Material.DIAMOND);
            assertTrue(a.equals(b));
        }

        @Test
        @DisplayName("Given two material wrappers with different materials, when comparing, then returns false")
        void equals_returnsFalse_whenMaterialsDiffer() {
            CustomItemWrapper a = materialWrapper(Material.DIAMOND);
            CustomItemWrapper b = materialWrapper(Material.EMERALD);
            assertFalse(a.equals(b));
        }

        @Test
        @DisplayName("Given two custom item wrappers with same id, when comparing, then returns true")
        void equals_returnsTrue_whenBothCustomItemsMatch() throws Exception {
            CustomItemWrapper a = customItemWrapper("nexo:ruby");
            CustomItemWrapper b = customItemWrapper("nexo:ruby");
            assertTrue(a.equals(b));
        }

        @Test
        @DisplayName("Given two custom item wrappers with different ids, when comparing, then returns false")
        void equals_returnsFalse_whenCustomItemsDiffer() throws Exception {
            CustomItemWrapper a = customItemWrapper("nexo:ruby");
            CustomItemWrapper b = customItemWrapper("nexo:sapphire");
            assertFalse(a.equals(b));
        }

        @Test
        @DisplayName("Given material wrapper and custom item wrapper, when comparing, then returns false")
        void equals_returnsFalse_whenTypesAreMixed() throws Exception {
            CustomItemWrapper materialBased = materialWrapper(Material.STONE);
            CustomItemWrapper customBased = customItemWrapper("nexo:stone");
            assertFalse(materialBased.equals(customBased));
        }
    }

    @Nested
    @DisplayName("equals(Object)")
    class EqualsObject {

        @Test
        @DisplayName("Given same instance, when comparing, then returns true")
        void equals_returnsTrue_whenSameInstance() {
            CustomItemWrapper wrapper = materialWrapper(Material.STONE);
            assertEquals(wrapper, wrapper);
        }

        @Test
        @DisplayName("Given null, when comparing, then returns false")
        void equals_returnsFalse_whenComparedWithNull() {
            CustomItemWrapper wrapper = materialWrapper(Material.STONE);
            assertNotEquals(null, wrapper);
        }

        @Test
        @DisplayName("Given two material wrappers with same material, when comparing as Object, then returns true")
        void equals_returnsTrue_whenMaterialWrappersMatch() {
            CustomItemWrapper a = materialWrapper(Material.STONE);
            CustomItemWrapper b = materialWrapper(Material.STONE);
            assertEquals(a, b);
        }

        @Test
        @DisplayName("Given two custom item wrappers with same id, when comparing as Object, then returns true")
        void equals_returnsTrue_whenCustomItemWrappersMatch() throws Exception {
            CustomItemWrapper a = customItemWrapper("nexo:ruby");
            CustomItemWrapper b = customItemWrapper("nexo:ruby");
            assertEquals(a, b);
        }

        @Test
        @DisplayName("Given material wrapper and different type, when comparing, then returns false")
        void equals_returnsFalse_whenComparedWithDifferentType() {
            CustomItemWrapper wrapper = materialWrapper(Material.STONE);
            assertNotEquals(wrapper, "not a wrapper");
        }

        @Test
        @DisplayName("Given one material and one custom wrapper, when comparing as Object, then returns false")
        void equals_returnsFalse_whenOneIsMaterialAndOtherIsCustom() throws Exception {
            CustomItemWrapper a = materialWrapper(Material.STONE);
            CustomItemWrapper b = customItemWrapper("nexo:stone");
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("Given two different material wrappers, when comparing as Object, then returns false")
        void equals_returnsFalse_whenMaterialsDiffer() {
            CustomItemWrapper a = materialWrapper(Material.STONE);
            CustomItemWrapper b = materialWrapper(Material.DIRT);
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("Given two different custom item wrappers, when comparing as Object, then returns false")
        void equals_returnsFalse_whenCustomItemsDiffer() throws Exception {
            CustomItemWrapper a = customItemWrapper("nexo:ruby");
            CustomItemWrapper b = customItemWrapper("nexo:sapphire");
            assertNotEquals(a, b);
        }
    }

    @Nested
    @DisplayName("hashCode")
    class HashCode {

        @Test
        @DisplayName("Given two equal material wrappers, when getting hashCode, then values are equal")
        void hashCode_returnsEqualValues_whenMaterialWrappersAreEqual() {
            CustomItemWrapper a = materialWrapper(Material.STONE);
            CustomItemWrapper b = materialWrapper(Material.STONE);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Given two equal custom item wrappers, when getting hashCode, then values are equal")
        void hashCode_returnsEqualValues_whenCustomItemWrappersAreEqual() throws Exception {
            CustomItemWrapper a = customItemWrapper("nexo:ruby");
            CustomItemWrapper b = customItemWrapper("nexo:ruby");
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Given material wrapper, when getting hashCode multiple times, then value is consistent")
        void hashCode_returnsSameValue_whenCalledMultipleTimes() {
            CustomItemWrapper wrapper = materialWrapper(Material.DIAMOND);
            int first = wrapper.hashCode();
            int second = wrapper.hashCode();
            assertEquals(first, second);
        }

        @Test
        @DisplayName("Given two unequal material wrappers, when getting hashCode, then values differ")
        void hashCode_returnsDifferentValues_whenMaterialWrappersDiffer() {
            CustomItemWrapper a = materialWrapper(Material.STONE);
            CustomItemWrapper b = materialWrapper(Material.DIAMOND);
            assertNotEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Given two unequal custom item wrappers, when getting hashCode, then values differ")
        void hashCode_returnsDifferentValues_whenCustomItemWrappersDiffer() throws Exception {
            CustomItemWrapper a = customItemWrapper("nexo:ruby");
            CustomItemWrapper b = customItemWrapper("nexo:sapphire");
            assertNotEquals(a.hashCode(), b.hashCode());
        }
    }

    @Nested
    @DisplayName("itemName")
    class ItemName {

        @Test
        @DisplayName("Given custom item wrapper with no hooks registered, when getting itemName, then returns Missing Item")
        void itemName_returnsMissingItem_whenNoHooksRegistered() throws Exception {
            CustomItemWrapper wrapper = customItemWrapper("nexo:ruby_sword");
            assertEquals("Missing Item", wrapper.itemName());
        }

        @Test
        @DisplayName("Given custom item wrapper with a hook registered, when getting itemName, then returns hook-provided name")
        void itemName_returnsHookProvidedName_whenHookIsRegistered() throws Exception {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(new TestCustomItemPluginHook());
            CustomItemWrapper wrapper = customItemWrapper("nexo:ruby_sword");
            assertEquals("Ruby Sword", wrapper.itemName());
        }
    }

    @Nested
    @DisplayName("itemName - vanilla material path")
    class ItemNameVanilla {

        @Test
        @DisplayName("Given material wrapper, when getting itemName, then returns lang tag with translation key")
        void itemName_returnsLangTag_whenMaterialWrapper() {
            CustomItemWrapper wrapper = materialWrapper(Material.IRON_INGOT);
            String result = wrapper.itemName();
            assertTrue(result.startsWith("<lang:"), "Expected lang tag, got: " + result);
            assertTrue(result.endsWith(">"), "Expected lang tag to end with >, got: " + result);
            assertTrue(result.contains(Material.IRON_INGOT.translationKey()), "Expected translation key, got: " + result);
        }
    }

    @Nested
    @DisplayName("itemBuilder")
    class ItemBuilderTests {

        @Test
        @DisplayName("Given material wrapper, when getting itemBuilder, then returns builder with that material")
        void itemBuilder_returnsMaterialBuilder_whenMaterialWrapper() {
            CustomItemWrapper wrapper = materialWrapper(Material.DIAMOND);
            var builder = wrapper.itemBuilder();
            assertNotNull(builder);
        }

        @Test
        @DisplayName("Given custom item wrapper with no hooks, when getting itemBuilder, then returns AIR builder")
        void itemBuilder_returnsAirBuilder_whenNoHooksRegistered() throws Exception {
            CustomItemWrapper wrapper = customItemWrapper("nexo:unknown_item");
            var builder = wrapper.itemBuilder();
            assertNotNull(builder);
        }

        @Test
        @DisplayName("Given custom item wrapper with hook providing item, when getting itemBuilder, then returns hook item builder")
        void itemBuilder_returnsHookItemBuilder_whenHookProvidesItem() throws Exception {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomItemPluginHook("nexo:ruby_sword", new ItemStack(Material.DIAMOND_SWORD), false, Set.of()));
            CustomItemWrapper wrapper = customItemWrapper("nexo:ruby_sword");
            var builder = wrapper.itemBuilder();
            assertNotNull(builder);
        }

        @Test
        @DisplayName("Given custom item wrapper with hook not providing item, when getting itemBuilder, then returns AIR builder")
        void itemBuilder_returnsAirBuilder_whenHookDoesNotProvideItem() throws Exception {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomItemPluginHook("nexo:other_item", new ItemStack(Material.EMERALD), false, Set.of()));
            CustomItemWrapper wrapper = customItemWrapper("nexo:ruby_sword");
            var builder = wrapper.itemBuilder();
            assertNotNull(builder);
        }
    }

    @Nested
    @DisplayName("ItemStack constructor")
    class ItemStackConstructor {

        @Test
        @DisplayName("Given item stack with hook recognizing it, when constructing wrapper, then sets custom item")
        void constructor_setsCustomItem_whenHookRecognizesItemStack() {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomItemPluginHook("nexo:ruby_sword", null, true, Set.of("nexo:ruby_sword")));
            ItemStack itemStack = Mockito.mock(ItemStack.class);
            Mockito.when(itemStack.getType()).thenReturn(Material.DIAMOND_SWORD);

            CustomItemWrapper wrapper = new CustomItemWrapper(itemStack);
            assertTrue(wrapper.customItem().isPresent());
            assertEquals("nexo:ruby_sword", wrapper.customItem().get());
            assertFalse(wrapper.material().isPresent());
        }

        @Test
        @DisplayName("Given item stack with no hooks, when constructing wrapper, then sets material from item type")
        void constructor_setsMaterial_whenNoHooksRecognizeItemStack() {
            ItemStack itemStack = Mockito.mock(ItemStack.class);
            Mockito.when(itemStack.getType()).thenReturn(Material.IRON_INGOT);

            CustomItemWrapper wrapper = new CustomItemWrapper(itemStack);
            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.IRON_INGOT, wrapper.material().get());
            assertFalse(wrapper.customItem().isPresent());
        }

        @Test
        @DisplayName("Given item stack with hook that recognizes but returns empty models, when constructing wrapper, then sets material")
        void constructor_setsMaterial_whenHookReturnsEmptyModels() {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomItemPluginHook("nexo:ruby_sword", null, true, Set.of()));
            ItemStack itemStack = Mockito.mock(ItemStack.class);
            Mockito.when(itemStack.getType()).thenReturn(Material.DIAMOND_SWORD);

            CustomItemWrapper wrapper = new CustomItemWrapper(itemStack);
            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.DIAMOND_SWORD, wrapper.material().get());
        }
    }

    @Nested
    @DisplayName("static customModels")
    class CustomModelsTests {

        @Test
        @DisplayName("Given item stack with no hooks registered, when getting customModels, then returns empty set")
        void customModels_returnsEmptySet_whenNoHooksRegistered() {
            ItemStack itemStack = Mockito.mock(ItemStack.class);
            Optional<Set<String>> result = CustomItemWrapper.customModels(itemStack);
            assertTrue(result.isPresent());
            assertTrue(result.get().isEmpty());
        }

        @Test
        @DisplayName("Given item stack with hook providing models, when getting customModels, then returns those models")
        void customModels_returnsModels_whenHookProvidesModels() {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomItemPluginHook("nexo:ruby_sword", null, true, Set.of("nexo:ruby_sword", "nexo:ruby_shield")));
            ItemStack itemStack = Mockito.mock(ItemStack.class);
            Optional<Set<String>> result = CustomItemWrapper.customModels(itemStack);
            assertTrue(result.isPresent());
            assertEquals(2, result.get().size());
            assertTrue(result.get().contains("nexo:ruby_sword"));
            assertTrue(result.get().contains("nexo:ruby_shield"));
        }
    }
}

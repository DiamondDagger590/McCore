package com.diamonddagger590.mccore.util.item;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.builder.item.ItemPluginType;
import com.diamonddagger590.mccore.external.common.CustomItemHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class CustomItemWrapperTest {

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
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

        TestCustomItemPluginHook() {
            super(null);
        }

        @Override
        public boolean isItem(@NotNull String itemName) {
            return "nexo:ruby_sword".equals(itemName);
        }

        @Override
        public boolean isItem(@NotNull ItemStack itemStack) {
            return false;
        }

        @Override
        public boolean isItemOfType(@NotNull ItemStack itemStack, @NotNull String itemName) {
            return false;
        }

        @NotNull
        @Override
        public Optional<ItemStack> item(@NotNull String itemName) {
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

        @Test
        @DisplayName("Given vanilla material wrapper, when getting itemName, then returns lang tag")
        void itemName_returnsLangTag_whenVanillaMaterial() {
            MockBukkit.mock();
            try {
                CustomItemWrapper wrapper = materialWrapper(Material.DIAMOND);
                String name = wrapper.itemName();
                assertTrue(name.startsWith("<lang:"), "Expected lang tag but got: " + name);
                assertTrue(name.endsWith(">"), "Expected lang tag to end with > but got: " + name);
            } finally {
                MockBukkit.unmock();
            }
        }

        @Test
        @DisplayName("Given custom item with hook that doesn't recognize it, when getting itemName, then returns Missing Item")
        void itemName_returnsMissingItem_whenHookDoesNotRecognize() throws Exception {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(new TestCustomItemPluginHook());
            CustomItemWrapper wrapper = customItemWrapper("nexo:unknown_item");
            assertEquals("Missing Item", wrapper.itemName());
        }
    }

    @Nested
    @DisplayName("Constructor(ItemStack)")
    class ItemStackConstructor {

        @BeforeEach
        void setUpMockBukkit() {
            MockBukkit.mock();
        }

        @AfterEach
        void tearDownMockBukkit() {
            MockBukkit.unmock();
        }

        @Test
        @DisplayName("Given an ItemStack with no hooks registered, when constructing, then uses item material")
        void constructor_usesMaterial_whenNoHooksRegistered() {
            ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD);

            CustomItemWrapper wrapper = new CustomItemWrapper(itemStack);

            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.DIAMOND_SWORD, wrapper.material().get());
            assertFalse(wrapper.customItem().isPresent());
        }

        @Test
        @DisplayName("Given an ItemStack with hook that recognizes it and returns models, when constructing, then uses custom item")
        void constructor_usesCustomItem_whenHookReturnsModels() {
            ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD);
            ConfigurableItemHook hook = new ConfigurableItemHook(true, Optional.of(Set.of("nexo:ruby_sword")));
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            CustomItemWrapper wrapper = new CustomItemWrapper(itemStack);

            assertTrue(wrapper.customItem().isPresent());
            assertEquals("nexo:ruby_sword", wrapper.customItem().get());
            assertFalse(wrapper.material().isPresent());
        }

        @Test
        @DisplayName("Given an ItemStack with hook that recognizes it but returns empty models, when constructing, then uses material")
        void constructor_usesMaterial_whenHookReturnsEmptyModels() {
            ItemStack itemStack = new ItemStack(Material.IRON_INGOT);
            ConfigurableItemHook hook = new ConfigurableItemHook(true, Optional.of(Set.of()));
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            CustomItemWrapper wrapper = new CustomItemWrapper(itemStack);

            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.IRON_INGOT, wrapper.material().get());
        }

        @Test
        @DisplayName("Given an ItemStack with hook that does not recognize it, when constructing, then uses material")
        void constructor_usesMaterial_whenHookDoesNotRecognize() {
            ItemStack itemStack = new ItemStack(Material.STONE);
            ConfigurableItemHook hook = new ConfigurableItemHook(false, Optional.empty());
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            CustomItemWrapper wrapper = new CustomItemWrapper(itemStack);

            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.STONE, wrapper.material().get());
        }

        @Test
        @DisplayName("Given an ItemStack with hook that returns empty optional for models, when constructing, then uses material")
        void constructor_usesMaterial_whenHookReturnsEmptyOptional() {
            ItemStack itemStack = new ItemStack(Material.GOLD_INGOT);
            ConfigurableItemHook hook = new ConfigurableItemHook(true, Optional.empty());
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            CustomItemWrapper wrapper = new CustomItemWrapper(itemStack);

            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.GOLD_INGOT, wrapper.material().get());
        }
    }

    @Nested
    @DisplayName("itemBuilder")
    class ItemBuilderTests {

        private MockedStatic<CorePlugin> corePluginStatic;

        @BeforeEach
        void setUpMockBukkit() {
            MockBukkit.mock();
            CorePlugin mockPlugin = mock(CorePlugin.class);
            when(mockPlugin.getMiniMessage()).thenReturn(MiniMessage.miniMessage());
            when(mockPlugin.getItemPlugin()).thenReturn(ItemPluginType.NONE);
            when(mockPlugin.registryAccess()).thenCallRealMethod();
            corePluginStatic = mockStatic(CorePlugin.class);
            corePluginStatic.when(CorePlugin::getInstance).thenReturn(mockPlugin);
        }

        @AfterEach
        void tearDownMockBukkit() {
            corePluginStatic.close();
            MockBukkit.unmock();
        }

        @Test
        @DisplayName("Given vanilla material wrapper, when getting itemBuilder, then returns non-null builder")
        void itemBuilder_returnsBuilder_whenVanillaMaterial() {
            CustomItemWrapper wrapper = materialWrapper(Material.STONE);
            assertNotNull(wrapper.itemBuilder());
        }

        @Test
        @DisplayName("Given custom item wrapper with no hooks, when getting itemBuilder, then returns AIR-based builder")
        void itemBuilder_returnsAirBuilder_whenNoHooksRegistered() throws Exception {
            CustomItemWrapper wrapper = customItemWrapper("nexo:ruby_sword");
            assertNotNull(wrapper.itemBuilder());
        }

        @Test
        @DisplayName("Given custom item wrapper with hook providing item, when getting itemBuilder, then returns hook-provided builder")
        void itemBuilder_returnsHookBuilder_whenHookProvidesItem() throws Exception {
            ItemStack customItem = new ItemStack(Material.DIAMOND);
            ConfigurableItemHook hook = new ConfigurableItemHook("nexo:ruby_sword", customItem);
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);
            CustomItemWrapper wrapper = customItemWrapper("nexo:ruby_sword");

            assertNotNull(wrapper.itemBuilder());
        }
    }

    @Nested
    @DisplayName("customModels (static)")
    class CustomModelsTests {

        @BeforeEach
        void setUpMockBukkit() {
            MockBukkit.mock();
        }

        @AfterEach
        void tearDownMockBukkit() {
            MockBukkit.unmock();
        }

        @Test
        @DisplayName("Given no hooks registered, when getting customModels, then returns empty set in Optional")
        void customModels_returnsEmptySet_whenNoHooksRegistered() {
            ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD);
            Optional<Set<String>> result = CustomItemWrapper.customModels(itemStack);

            assertTrue(result.isPresent());
            assertTrue(result.get().isEmpty());
        }

        @Test
        @DisplayName("Given hook that returns models, when getting customModels, then returns those models")
        void customModels_returnsModels_whenHookProvidesModels() {
            ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD);
            ConfigurableItemHook hook = new ConfigurableItemHook(false, Optional.of(Set.of("model_a", "model_b")));
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            Optional<Set<String>> result = CustomItemWrapper.customModels(itemStack);

            assertTrue(result.isPresent());
            assertEquals(Set.of("model_a", "model_b"), result.get());
        }

        @Test
        @DisplayName("Given hook that returns empty optional, when getting customModels, then returns empty set")
        void customModels_returnsEmptySet_whenHookReturnsEmptyOptional() {
            ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD);
            ConfigurableItemHook hook = new ConfigurableItemHook(false, Optional.empty());
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            Optional<Set<String>> result = CustomItemWrapper.customModels(itemStack);

            assertTrue(result.isPresent());
            assertTrue(result.get().isEmpty());
        }
    }

    static class ConfigurableItemHook extends PluginHook<CorePlugin> implements CustomItemHook {

        private final boolean isItem;
        private final Optional<Set<String>> models;
        private final String itemForName;
        private final ItemStack providedItem;

        ConfigurableItemHook(boolean isItem, Optional<Set<String>> models) {
            super(null);
            this.isItem = isItem;
            this.models = models;
            this.itemForName = null;
            this.providedItem = null;
        }

        ConfigurableItemHook(String itemForName, ItemStack providedItem) {
            super(null);
            this.isItem = false;
            this.models = Optional.empty();
            this.itemForName = itemForName;
            this.providedItem = providedItem;
        }

        @Override
        public boolean isItem(@NotNull String itemName) {
            return false;
        }

        @Override
        public boolean isItem(@NotNull ItemStack itemStack) {
            return isItem;
        }

        @Override
        public boolean isItemOfType(@NotNull ItemStack itemStack, @NotNull String itemName) {
            return false;
        }

        @NotNull
        @Override
        public Optional<ItemStack> item(@NotNull String itemName) {
            return itemForName != null && itemForName.equals(itemName) ? Optional.ofNullable(providedItem) : Optional.empty();
        }

        @NotNull
        @Override
        public Optional<Set<String>> itemModels(@NotNull ItemStack itemStack) {
            return models;
        }

        @NotNull
        @Override
        public String itemName(@NotNull CustomItemWrapper customItemWrapper) {
            return "";
        }
    }
}

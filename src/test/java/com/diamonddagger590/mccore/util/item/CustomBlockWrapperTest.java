package com.diamonddagger590.mccore.util.item;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.external.common.CustomBlockHook;
import com.diamonddagger590.mccore.external.common.CustomItemHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import com.diamonddagger590.mccore.testing.CorePluginTestHelper;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomBlockWrapperTest {

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

    private static CustomBlockWrapper materialWrapper(Material material) {
        return new CustomBlockWrapper(material);
    }

    private static CustomBlockWrapper customBlockWrapper(String customBlock) throws Exception {
        CustomBlockWrapper wrapper = new CustomBlockWrapper(Material.STONE);
        Field materialField = CustomBlockWrapper.class.getDeclaredField("material");
        materialField.setAccessible(true);
        materialField.set(wrapper, null);
        Field customBlockField = CustomBlockWrapper.class.getDeclaredField("customBlock");
        customBlockField.setAccessible(true);
        customBlockField.set(wrapper, customBlock);
        return wrapper;
    }

    static class TestCustomBlockPluginHook extends PluginHook<CorePlugin> implements CustomBlockHook {

        private final boolean recognizesBlock;
        private final Set<String> models;
        private final List<ItemStack> drops;

        TestCustomBlockPluginHook() {
            this(false, Set.of(), List.of());
        }

        TestCustomBlockPluginHook(boolean recognizesBlock, Set<String> models, List<ItemStack> drops) {
            super(null);
            this.recognizesBlock = recognizesBlock;
            this.models = models;
            this.drops = drops;
        }

        @Override
        public boolean isCustomBlock(@NotNull Block block) {
            return recognizesBlock;
        }

        @Override
        public boolean isCustomBlock(@NotNull String customBlock) {
            return "nexo:ruby_ore".equals(customBlock);
        }

        @Override
        public boolean isCustomBlockOfType(@NotNull Block block, @NotNull String customBlockType) {
            return recognizesBlock && "nexo:ruby_ore".equals(customBlockType);
        }

        @Override
        public void placeCustomBlock(@NotNull Location location, @NotNull String blockId) {
        }

        @NotNull
        @Override
        public List<ItemStack> drops(@NotNull Block block, @NotNull ItemStack itemToBreakWith, @Nullable Entity entityBreaking) {
            return drops;
        }

        @Override
        public void playBlockDropEffects(@NotNull Block block) {
        }

        @Override
        public void removeBlock(@NotNull Block block) {
        }

        @NotNull
        @Override
        public Optional<Set<String>> blockModels(@NotNull Block block) {
            return models.isEmpty() ? Optional.empty() : Optional.of(models);
        }

        @NotNull
        @Override
        public String blockName(@NotNull CustomBlockWrapper customBlockWrapper) {
            return "Ruby Ore";
        }
    }

    static class TestCustomItemPluginHook extends PluginHook<CorePlugin> implements CustomItemHook {

        private final String recognizedItem;
        private final ItemStack returnedItem;

        TestCustomItemPluginHook(String recognizedItem, ItemStack returnedItem) {
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
            return Optional.empty();
        }

        @NotNull
        @Override
        public String itemName(@NotNull CustomItemWrapper customItemWrapper) {
            return "Test Item";
        }
    }

    @Nested
    @DisplayName("Material constructor")
    class MaterialConstructor {

        @Test
        @DisplayName("Given a material, when getting material, then returns that material")
        void material_returnsProvidedMaterial_whenConstructedWithMaterial() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.STONE, wrapper.material().get());
        }

        @Test
        @DisplayName("Given a material, when getting customBlock, then returns empty")
        void customBlock_returnsEmpty_whenConstructedWithMaterial() {
            CustomBlockWrapper wrapper = materialWrapper(Material.DIAMOND_ORE);
            assertFalse(wrapper.customBlock().isPresent());
        }
    }

    @Nested
    @DisplayName("Custom block accessors")
    class CustomBlockAccessors {

        @Test
        @DisplayName("Given a custom block wrapper, when getting customBlock, then returns the custom block id")
        void customBlock_returnsId_whenConstructedWithCustomBlock() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertTrue(wrapper.customBlock().isPresent());
            assertEquals("nexo:ruby_ore", wrapper.customBlock().get());
        }

        @Test
        @DisplayName("Given a custom block wrapper, when getting material, then returns empty")
        void material_returnsEmpty_whenConstructedWithCustomBlock() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertFalse(wrapper.material().isPresent());
        }
    }

    @Nested
    @DisplayName("equals(Material)")
    class EqualsMaterial {

        @Test
        @DisplayName("Given matching material, when comparing, then returns true")
        void equals_returnsTrue_whenMaterialMatches() {
            CustomBlockWrapper wrapper = materialWrapper(Material.IRON_ORE);
            assertTrue(wrapper.equals(Material.IRON_ORE));
        }

        @Test
        @DisplayName("Given different material, when comparing, then returns false")
        void equals_returnsFalse_whenMaterialDiffers() {
            CustomBlockWrapper wrapper = materialWrapper(Material.IRON_ORE);
            assertFalse(wrapper.equals(Material.GOLD_ORE));
        }

        @Test
        @DisplayName("Given custom block wrapper, when comparing with any material, then returns false")
        void equals_returnsFalse_whenWrapperIsCustomBlock() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertFalse(wrapper.equals(Material.DIAMOND_ORE));
        }
    }

    @Nested
    @DisplayName("equals(String)")
    class EqualsString {

        @Test
        @DisplayName("Given matching custom block id, when comparing, then returns true")
        void equals_returnsTrue_whenCustomBlockMatches() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertTrue(wrapper.equals("nexo:ruby_ore"));
        }

        @Test
        @DisplayName("Given different custom block id, when comparing, then returns false")
        void equals_returnsFalse_whenCustomBlockDiffers() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertFalse(wrapper.equals("nexo:sapphire_ore"));
        }

        @Test
        @DisplayName("Given material wrapper, when comparing with any string, then returns false")
        void equals_returnsFalse_whenWrapperIsMaterial() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            assertFalse(wrapper.equals("stone"));
        }
    }

    @Nested
    @DisplayName("equals(CustomItemWrapper)")
    class EqualsCustomItemWrapper {

        @Test
        @DisplayName("Given block and item wrappers with same material, when comparing, then returns true")
        void equals_returnsTrue_whenBothMaterialsMatch() {
            CustomBlockWrapper blockWrapper = materialWrapper(Material.STONE);
            CustomItemWrapper itemWrapper = new CustomItemWrapper(Material.STONE);
            assertTrue(blockWrapper.equals(itemWrapper));
        }

        @Test
        @DisplayName("Given block and item wrappers with different materials, when comparing, then returns false")
        void equals_returnsFalse_whenMaterialsDiffer() {
            CustomBlockWrapper blockWrapper = materialWrapper(Material.STONE);
            CustomItemWrapper itemWrapper = new CustomItemWrapper(Material.DIRT);
            assertFalse(blockWrapper.equals(itemWrapper));
        }

        @Test
        @DisplayName("Given custom block and custom item wrappers with same id, when comparing, then returns true")
        void equals_returnsTrue_whenBothCustomIdsMatch() throws Exception {
            CustomBlockWrapper blockWrapper = customBlockWrapper("nexo:ruby_ore");
            Field materialField = CustomItemWrapper.class.getDeclaredField("material");
            materialField.setAccessible(true);
            Field customItemField = CustomItemWrapper.class.getDeclaredField("customItem");
            customItemField.setAccessible(true);
            CustomItemWrapper itemWrapper = new CustomItemWrapper(Material.AIR);
            materialField.set(itemWrapper, null);
            customItemField.set(itemWrapper, "nexo:ruby_ore");
            assertTrue(blockWrapper.equals(itemWrapper));
        }

        @Test
        @DisplayName("Given custom block and material item wrapper, when comparing, then returns false")
        void equals_returnsFalse_whenTypesAreMixed() throws Exception {
            CustomBlockWrapper blockWrapper = customBlockWrapper("nexo:ruby_ore");
            CustomItemWrapper itemWrapper = new CustomItemWrapper(Material.STONE);
            assertFalse(blockWrapper.equals(itemWrapper));
        }
    }

    @Nested
    @DisplayName("equals(Object)")
    class EqualsObject {

        @Test
        @DisplayName("Given same instance, when comparing, then returns true")
        void equals_returnsTrue_whenSameInstance() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            assertEquals(wrapper, wrapper);
        }

        @Test
        @DisplayName("Given null, when comparing, then returns false")
        void equals_returnsFalse_whenComparedWithNull() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            assertNotEquals(null, wrapper);
        }

        @Test
        @DisplayName("Given two material wrappers with same material, when comparing as Object, then returns true")
        void equals_returnsTrue_whenMaterialWrappersMatch() {
            CustomBlockWrapper a = materialWrapper(Material.STONE);
            CustomBlockWrapper b = materialWrapper(Material.STONE);
            assertEquals(a, b);
        }

        @Test
        @DisplayName("Given two custom block wrappers with same id, when comparing as Object, then returns true")
        void equals_returnsTrue_whenCustomBlockWrappersMatch() throws Exception {
            CustomBlockWrapper a = customBlockWrapper("nexo:ruby_ore");
            CustomBlockWrapper b = customBlockWrapper("nexo:ruby_ore");
            assertEquals(a, b);
        }

        @Test
        @DisplayName("Given block wrapper and different type, when comparing, then returns false")
        void equals_returnsFalse_whenComparedWithDifferentType() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            assertNotEquals(wrapper, "not a wrapper");
        }

        @Test
        @DisplayName("Given one material and one custom wrapper, when comparing as Object, then returns false")
        void equals_returnsFalse_whenOneIsMaterialAndOtherIsCustom() throws Exception {
            CustomBlockWrapper a = materialWrapper(Material.STONE);
            CustomBlockWrapper b = customBlockWrapper("nexo:stone");
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("Given two different material wrappers, when comparing as Object, then returns false")
        void equals_returnsFalse_whenMaterialsDiffer() {
            CustomBlockWrapper a = materialWrapper(Material.STONE);
            CustomBlockWrapper b = materialWrapper(Material.DIRT);
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("Given two different custom block wrappers, when comparing as Object, then returns false")
        void equals_returnsFalse_whenCustomBlocksDiffer() throws Exception {
            CustomBlockWrapper a = customBlockWrapper("nexo:ruby_ore");
            CustomBlockWrapper b = customBlockWrapper("nexo:sapphire_ore");
            assertNotEquals(a, b);
        }
    }

    @Nested
    @DisplayName("hashCode")
    class HashCode {

        @Test
        @DisplayName("Given two equal material wrappers, when getting hashCode, then values are equal")
        void hashCode_returnsEqualValues_whenMaterialWrappersAreEqual() {
            CustomBlockWrapper a = materialWrapper(Material.STONE);
            CustomBlockWrapper b = materialWrapper(Material.STONE);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Given two equal custom block wrappers, when getting hashCode, then values are equal")
        void hashCode_returnsEqualValues_whenCustomBlockWrappersAreEqual() throws Exception {
            CustomBlockWrapper a = customBlockWrapper("nexo:ruby_ore");
            CustomBlockWrapper b = customBlockWrapper("nexo:ruby_ore");
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Given material wrapper, when getting hashCode multiple times, then value is consistent")
        void hashCode_returnsSameValue_whenCalledMultipleTimes() {
            CustomBlockWrapper wrapper = materialWrapper(Material.DIAMOND_ORE);
            int first = wrapper.hashCode();
            int second = wrapper.hashCode();
            assertEquals(first, second);
        }

        @Test
        @DisplayName("Given two unequal material wrappers, when getting hashCode, then values differ")
        void hashCode_returnsDifferentValues_whenMaterialWrappersDiffer() {
            CustomBlockWrapper a = materialWrapper(Material.STONE);
            CustomBlockWrapper b = materialWrapper(Material.DIAMOND_ORE);
            assertNotEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Given two unequal custom block wrappers, when getting hashCode, then values differ")
        void hashCode_returnsDifferentValues_whenCustomBlockWrappersDiffer() throws Exception {
            CustomBlockWrapper a = customBlockWrapper("nexo:ruby_ore");
            CustomBlockWrapper b = customBlockWrapper("nexo:sapphire_ore");
            assertNotEquals(a.hashCode(), b.hashCode());
        }
    }

    @Nested
    @DisplayName("blockName")
    class BlockName {

        @Test
        @DisplayName("Given custom block wrapper with no hooks registered, when getting blockName, then returns Missing Block")
        void blockName_returnsMissingBlock_whenNoHooksRegistered() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertEquals("Missing Block", wrapper.blockName());
        }

        @Test
        @DisplayName("Given custom block wrapper with a hook registered, when getting blockName, then returns hook-provided name")
        void blockName_returnsHookProvidedName_whenHookIsRegistered() throws Exception {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(new TestCustomBlockPluginHook());
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertEquals("Ruby Ore", wrapper.blockName());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("Given material wrapper, when calling toString, then contains material name")
        void toString_containsMaterialName_whenMaterialWrapper() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            String result = wrapper.toString();
            assertTrue(result.contains("STONE"), "Expected toString to contain STONE, got: " + result);
            assertTrue(result.contains("customBlock=null"), "Expected customBlock=null, got: " + result);
        }

        @Test
        @DisplayName("Given custom block wrapper, when calling toString, then contains custom block id")
        void toString_containsCustomBlockId_whenCustomBlockWrapper() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            String result = wrapper.toString();
            assertTrue(result.contains("nexo:ruby_ore"), "Expected toString to contain nexo:ruby_ore, got: " + result);
            assertTrue(result.contains("material=null"), "Expected material=null, got: " + result);
        }
    }

    @Nested
    @DisplayName("blockName - vanilla material path")
    class BlockNameVanilla {

        @Test
        @DisplayName("Given material wrapper, when getting blockName, then returns lang tag with translation key")
        void blockName_returnsLangTag_whenMaterialWrapper() {
            CustomBlockWrapper wrapper = materialWrapper(Material.IRON_ORE);
            String result = wrapper.blockName();
            assertTrue(result.startsWith("<lang:"), "Expected lang tag, got: " + result);
            assertTrue(result.endsWith(">"), "Expected lang tag to end with >, got: " + result);
            assertTrue(result.contains(Material.IRON_ORE.translationKey()), "Expected translation key, got: " + result);
        }
    }

    @Nested
    @DisplayName("itemBuilder")
    class ItemBuilderTests {

        @Test
        @DisplayName("Given material wrapper, when getting itemBuilder, then returns builder with that material")
        void itemBuilder_returnsMaterialBuilder_whenMaterialWrapper() {
            CustomBlockWrapper wrapper = materialWrapper(Material.DIAMOND_ORE);
            var builder = wrapper.itemBuilder();
            assertNotNull(builder);
        }

        @Test
        @DisplayName("Given custom block wrapper with no hooks, when getting itemBuilder, then returns AIR builder")
        void itemBuilder_returnsAirBuilder_whenNoHooksRegistered() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:unknown_block");
            var builder = wrapper.itemBuilder();
            assertNotNull(builder);
        }

        @Test
        @DisplayName("Given custom block wrapper with hook providing item, when getting itemBuilder, then returns hook item builder")
        void itemBuilder_returnsHookItemBuilder_whenHookProvidesItem() throws Exception {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomItemPluginHook("nexo:ruby_ore", new ItemStack(Material.EMERALD)));
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            var builder = wrapper.itemBuilder();
            assertNotNull(builder);
        }

        @Test
        @DisplayName("Given custom block wrapper with hook not providing item, when getting itemBuilder, then returns AIR builder")
        void itemBuilder_returnsAirBuilder_whenHookDoesNotProvideItem() throws Exception {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomItemPluginHook("nexo:other_item", new ItemStack(Material.EMERALD)));
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            var builder = wrapper.itemBuilder();
            assertNotNull(builder);
        }
    }

    @Nested
    @DisplayName("equals(Block)")
    class EqualsBlock {

        @Test
        @DisplayName("Given material wrapper matching block type, when comparing with block, then returns true")
        void equals_returnsTrue_whenMaterialMatchesBlockType() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            Block block = Mockito.mock(Block.class);
            Mockito.when(block.getType()).thenReturn(Material.STONE);
            assertTrue(wrapper.equals(block));
        }

        @Test
        @DisplayName("Given material wrapper not matching block type, when comparing with block, then returns false")
        void equals_returnsFalse_whenMaterialDoesNotMatchBlockType() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            Block block = Mockito.mock(Block.class);
            Mockito.when(block.getType()).thenReturn(Material.DIRT);
            assertFalse(wrapper.equals(block));
        }

        @Test
        @DisplayName("Given custom block wrapper with hook confirming type, when comparing with block, then returns true")
        void equals_returnsTrue_whenHookConfirmsCustomBlockType() throws Exception {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomBlockPluginHook(true, Set.of("nexo:ruby_ore"), List.of()));
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            Block block = Mockito.mock(Block.class);
            assertTrue(wrapper.equals(block));
        }

        @Test
        @DisplayName("Given custom block wrapper with no hooks, when comparing with block, then returns false")
        void equals_returnsFalse_whenNoHooksForCustomBlock() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            Block block = Mockito.mock(Block.class);
            assertFalse(wrapper.equals(block));
        }
    }

    @Nested
    @DisplayName("Block constructor")
    class BlockConstructor {

        @Test
        @DisplayName("Given block with custom hook recognizing it, when constructing wrapper, then sets custom block")
        void constructor_setsCustomBlock_whenHookRecognizesBlock() {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomBlockPluginHook(true, Set.of("nexo:ruby_ore"), List.of()));
            Block block = Mockito.mock(Block.class);
            Mockito.when(block.getType()).thenReturn(Material.STONE);

            CustomBlockWrapper wrapper = new CustomBlockWrapper(block);
            assertTrue(wrapper.customBlock().isPresent());
            assertEquals("nexo:ruby_ore", wrapper.customBlock().get());
            assertFalse(wrapper.material().isPresent());
        }

        @Test
        @DisplayName("Given block with no hooks, when constructing wrapper, then sets material from block type")
        void constructor_setsMaterial_whenNoHooksRecognizeBlock() {
            Block block = Mockito.mock(Block.class);
            Mockito.when(block.getType()).thenReturn(Material.DIAMOND_ORE);

            CustomBlockWrapper wrapper = new CustomBlockWrapper(block);
            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.DIAMOND_ORE, wrapper.material().get());
            assertFalse(wrapper.customBlock().isPresent());
        }

        @Test
        @DisplayName("Given block with hook that recognizes but returns empty models, when constructing wrapper, then sets material")
        void constructor_setsMaterial_whenHookReturnsEmptyModels() {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomBlockPluginHook(true, Set.of(), List.of()));
            Block block = Mockito.mock(Block.class);
            Mockito.when(block.getType()).thenReturn(Material.STONE);

            CustomBlockWrapper wrapper = new CustomBlockWrapper(block);
            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.STONE, wrapper.material().get());
        }
    }

    @Nested
    @DisplayName("static customModels")
    class CustomModels {

        @Test
        @DisplayName("Given block with no hooks registered, when getting customModels, then returns empty set")
        void customModels_returnsEmptySet_whenNoHooksRegistered() {
            Block block = Mockito.mock(Block.class);
            Optional<Set<String>> result = CustomBlockWrapper.customModels(block);
            assertTrue(result.isPresent());
            assertTrue(result.get().isEmpty());
        }

        @Test
        @DisplayName("Given block with hook providing models, when getting customModels, then returns those models")
        void customModels_returnsModels_whenHookProvidesModels() {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomBlockPluginHook(true, Set.of("nexo:ruby_ore", "nexo:sapphire_ore"), List.of()));
            Block block = Mockito.mock(Block.class);
            Optional<Set<String>> result = CustomBlockWrapper.customModels(block);
            assertTrue(result.isPresent());
            assertEquals(2, result.get().size());
            assertTrue(result.get().contains("nexo:ruby_ore"));
            assertTrue(result.get().contains("nexo:sapphire_ore"));
        }
    }

    @Nested
    @DisplayName("static drops")
    class Drops {

        @Test
        @DisplayName("Given block with hook registered, when getting drops, then returns hook drops")
        void drops_returnsHookDrops_whenHookRegistered() {
            ItemStack drop = new ItemStack(Material.DIAMOND);
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomBlockPluginHook(true, Set.of("nexo:ruby_ore"), List.of(drop)));
            Block block = Mockito.mock(Block.class);
            ItemStack tool = new ItemStack(Material.DIAMOND_PICKAXE);

            List<ItemStack> result = CustomBlockWrapper.drops(block, tool, null);
            assertEquals(1, result.size());
            assertEquals(Material.DIAMOND, result.get(0).getType());
        }
    }

    @Nested
    @DisplayName("static removeBlock")
    class RemoveBlock {

        @Test
        @DisplayName("Given block with no hooks, when removing block, then sets type to AIR")
        void removeBlock_setsAir_whenNoHooksRegistered() {
            Block block = Mockito.mock(Block.class);
            CustomBlockWrapper.removeBlock(block);
            Mockito.verify(block).setType(Material.AIR);
        }

        @Test
        @DisplayName("Given block with hook registered, when removing block, then delegates to hook")
        void removeBlock_delegatesToHook_whenHookRegistered() {
            TestCustomBlockPluginHook hook = Mockito.spy(new TestCustomBlockPluginHook(true, Set.of("nexo:ruby_ore"), List.of()));
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);
            Block block = Mockito.mock(Block.class);
            CustomBlockWrapper.removeBlock(block);
            Mockito.verify(hook).removeBlock(block);
        }
    }
}

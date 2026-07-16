package com.diamonddagger590.mccore.util.item;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.external.common.CustomBlockHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomBlockWrapperTest {

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
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

        TestCustomBlockPluginHook() {
            super(null);
        }

        @Override
        public boolean isCustomBlock(@NotNull Block block) {
            return false;
        }

        @Override
        public boolean isCustomBlock(@NotNull String customBlock) {
            return "nexo:ruby_ore".equals(customBlock);
        }

        @Override
        public boolean isCustomBlockOfType(@NotNull Block block, @NotNull String customBlockType) {
            return false;
        }

        @Override
        public void placeCustomBlock(@NotNull Location location, @NotNull String blockId) {
        }

        @NotNull
        @Override
        public List<ItemStack> drops(@NotNull Block block, @NotNull ItemStack itemToBreakWith, @Nullable Entity entityBreaking) {
            return List.of();
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
            return Optional.empty();
        }

        @NotNull
        @Override
        public String blockName(@NotNull CustomBlockWrapper customBlockWrapper) {
            return "Ruby Ore";
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
    @DisplayName("isVanilla")
    class IsVanilla {

        @Test
        @DisplayName("Material wrapper returns true")
        void isVanilla_returnsTrue_whenMaterialWrapper() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            assertTrue(wrapper.isVanilla());
        }

        @Test
        @DisplayName("Custom block wrapper returns false")
        void isVanilla_returnsFalse_whenCustomBlockWrapper() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertFalse(wrapper.isVanilla());
        }
    }

    @Nested
    @DisplayName("isCustom")
    class IsCustom {

        @Test
        @DisplayName("Custom block wrapper returns true")
        void isCustom_returnsTrue_whenCustomBlockWrapper() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertTrue(wrapper.isCustom());
        }

        @Test
        @DisplayName("Material wrapper returns false")
        void isCustom_returnsFalse_whenMaterialWrapper() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            assertFalse(wrapper.isCustom());
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
}

package com.diamonddagger590.mccore.util.item;

import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

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

    // ── Material constructor ────────────────────────────────────────────────

    @Nested
    @DisplayName("Material constructor")
    class MaterialConstructor {

        @Test
        @DisplayName("Given a material, when constructing, then material() returns that material")
        void material_returnsProvidedMaterial() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.STONE, wrapper.material().get());
        }

        @Test
        @DisplayName("Given a material, when constructing, then customBlock() returns empty")
        void customBlock_returnsEmpty_whenConstructedWithMaterial() {
            CustomBlockWrapper wrapper = materialWrapper(Material.DIAMOND_ORE);
            assertFalse(wrapper.customBlock().isPresent());
        }
    }

    // ── Custom block accessors ──────────────────────────────────────────────

    @Nested
    @DisplayName("Custom block accessors")
    class CustomBlockAccessors {

        @Test
        @DisplayName("Given a custom block wrapper, then customBlock() returns the custom block id")
        void customBlock_returnsId() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertTrue(wrapper.customBlock().isPresent());
            assertEquals("nexo:ruby_ore", wrapper.customBlock().get());
        }

        @Test
        @DisplayName("Given a custom block wrapper, then material() returns empty")
        void material_returnsEmpty_whenCustomBlock() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertFalse(wrapper.material().isPresent());
        }
    }

    // ── equals(Material) ────────────────────────────────────────────────────

    @Nested
    @DisplayName("equals(Material)")
    class EqualsMaterial {

        @Test
        @DisplayName("Given matching material, when comparing, then returns true")
        void returnsTrue_whenMaterialMatches() {
            CustomBlockWrapper wrapper = materialWrapper(Material.IRON_ORE);
            assertTrue(wrapper.equals(Material.IRON_ORE));
        }

        @Test
        @DisplayName("Given different material, when comparing, then returns false")
        void returnsFalse_whenMaterialDiffers() {
            CustomBlockWrapper wrapper = materialWrapper(Material.IRON_ORE);
            assertFalse(wrapper.equals(Material.GOLD_ORE));
        }

        @Test
        @DisplayName("Given custom block wrapper, when comparing with any material, then returns false")
        void returnsFalse_whenWrapperIsCustomBlock() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertFalse(wrapper.equals(Material.DIAMOND_ORE));
        }
    }

    // ── equals(String) ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("equals(String)")
    class EqualsString {

        @Test
        @DisplayName("Given matching custom block id, when comparing, then returns true")
        void returnsTrue_whenCustomBlockMatches() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertTrue(wrapper.equals("nexo:ruby_ore"));
        }

        @Test
        @DisplayName("Given different custom block id, when comparing, then returns false")
        void returnsFalse_whenCustomBlockDiffers() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertFalse(wrapper.equals("nexo:sapphire_ore"));
        }

        @Test
        @DisplayName("Given material wrapper, when comparing with any string, then returns false")
        void returnsFalse_whenWrapperIsMaterial() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            assertFalse(wrapper.equals("stone"));
        }
    }

    // ── equals(CustomItemWrapper) ───────────────────────────────────────────

    @Nested
    @DisplayName("equals(CustomItemWrapper)")
    class EqualsCustomItemWrapper {

        @Test
        @DisplayName("Given block and item wrappers with same material, when comparing, then returns true")
        void returnsTrue_whenBothMaterialsMatch() {
            CustomBlockWrapper blockWrapper = materialWrapper(Material.STONE);
            CustomItemWrapper itemWrapper = new CustomItemWrapper(Material.STONE);
            assertTrue(blockWrapper.equals(itemWrapper));
        }

        @Test
        @DisplayName("Given block and item wrappers with different materials, when comparing, then returns false")
        void returnsFalse_whenMaterialsDiffer() {
            CustomBlockWrapper blockWrapper = materialWrapper(Material.STONE);
            CustomItemWrapper itemWrapper = new CustomItemWrapper(Material.DIRT);
            assertFalse(blockWrapper.equals(itemWrapper));
        }

        @Test
        @DisplayName("Given custom block and custom item wrappers with same id, when comparing, then returns true")
        void returnsTrue_whenBothCustomIdsMatch() throws Exception {
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
        void returnsFalse_whenTypesAreMixed() throws Exception {
            CustomBlockWrapper blockWrapper = customBlockWrapper("nexo:ruby_ore");
            CustomItemWrapper itemWrapper = new CustomItemWrapper(Material.STONE);
            assertFalse(blockWrapper.equals(itemWrapper));
        }
    }

    // ── equals(Object) ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("equals(Object)")
    class EqualsObject {

        @Test
        @DisplayName("Given same instance, when comparing, then returns true")
        void returnsTrue_forSameInstance() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            assertEquals(wrapper, wrapper);
        }

        @Test
        @DisplayName("Given two material wrappers with same material, when comparing as Object, then returns true")
        void returnsTrue_whenMaterialWrappersMatch() {
            CustomBlockWrapper a = materialWrapper(Material.STONE);
            CustomBlockWrapper b = materialWrapper(Material.STONE);
            assertEquals(a, b);
        }

        @Test
        @DisplayName("Given two custom block wrappers with same id, when comparing as Object, then returns true")
        void returnsTrue_whenCustomBlockWrappersMatch() throws Exception {
            CustomBlockWrapper a = customBlockWrapper("nexo:ruby_ore");
            CustomBlockWrapper b = customBlockWrapper("nexo:ruby_ore");
            assertEquals(a, b);
        }

        @Test
        @DisplayName("Given block wrapper and different type, when comparing, then returns false")
        void returnsFalse_whenComparedWithDifferentType() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            assertNotEquals(wrapper, "not a wrapper");
        }

        @Test
        @DisplayName("Given one material and one custom wrapper, when comparing as Object, then returns false")
        void returnsFalse_whenOneIsMaterialAndOtherIsCustom() throws Exception {
            CustomBlockWrapper a = materialWrapper(Material.STONE);
            CustomBlockWrapper b = customBlockWrapper("nexo:stone");
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("Given two different material wrappers, when comparing as Object, then returns false")
        void returnsFalse_whenMaterialsDiffer() {
            CustomBlockWrapper a = materialWrapper(Material.STONE);
            CustomBlockWrapper b = materialWrapper(Material.DIRT);
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("Given two different custom block wrappers, when comparing as Object, then returns false")
        void returnsFalse_whenCustomBlocksDiffer() throws Exception {
            CustomBlockWrapper a = customBlockWrapper("nexo:ruby_ore");
            CustomBlockWrapper b = customBlockWrapper("nexo:sapphire_ore");
            assertNotEquals(a, b);
        }
    }

    // ── hashCode ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("hashCode")
    class HashCode {

        @Test
        @DisplayName("Given two equal material wrappers, then hashCodes are equal")
        void hashCodesEqual_forEqualMaterialWrappers() {
            CustomBlockWrapper a = materialWrapper(Material.STONE);
            CustomBlockWrapper b = materialWrapper(Material.STONE);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Given two equal custom block wrappers, then hashCodes are equal")
        void hashCodesEqual_forEqualCustomBlockWrappers() throws Exception {
            CustomBlockWrapper a = customBlockWrapper("nexo:ruby_ore");
            CustomBlockWrapper b = customBlockWrapper("nexo:ruby_ore");
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Given material wrapper, then hashCode is consistent across calls")
        void hashCode_isConsistent() {
            CustomBlockWrapper wrapper = materialWrapper(Material.DIAMOND_ORE);
            int first = wrapper.hashCode();
            int second = wrapper.hashCode();
            assertEquals(first, second);
        }
    }

    // ── blockName ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("blockName")
    class BlockName {

        @Test
        @DisplayName("Given custom block wrapper with no hooks registered, when getting blockName, then returns Missing Block")
        void returnsMissingBlock_whenNoHooksRegistered() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertEquals("Missing Block", wrapper.blockName());
        }
    }

    // ── toString ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("Given material wrapper, when calling toString, then contains material name")
        void containsMaterialName_forMaterialWrapper() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            String result = wrapper.toString();
            assertTrue(result.contains("STONE"), "Expected toString to contain STONE, got: " + result);
            assertTrue(result.contains("customBlock=null"), "Expected customBlock=null, got: " + result);
        }

        @Test
        @DisplayName("Given custom block wrapper, when calling toString, then contains custom block id")
        void containsCustomBlockId_forCustomBlockWrapper() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            String result = wrapper.toString();
            assertTrue(result.contains("nexo:ruby_ore"), "Expected toString to contain nexo:ruby_ore, got: " + result);
            assertTrue(result.contains("material=null"), "Expected material=null, got: " + result);
        }
    }
}

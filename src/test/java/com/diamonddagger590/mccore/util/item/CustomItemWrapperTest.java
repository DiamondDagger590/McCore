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

    // ── Material constructor ────────────────────────────────────────────────

    @Nested
    @DisplayName("Material constructor")
    class MaterialConstructor {

        @Test
        @DisplayName("Given a material, when constructing, then material() returns that material")
        void material_returnsProvidedMaterial() {
            CustomItemWrapper wrapper = materialWrapper(Material.DIAMOND);
            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.DIAMOND, wrapper.material().get());
        }

        @Test
        @DisplayName("Given a material, when constructing, then customItem() returns empty")
        void customItem_returnsEmpty_whenConstructedWithMaterial() {
            CustomItemWrapper wrapper = materialWrapper(Material.STONE);
            assertFalse(wrapper.customItem().isPresent());
        }
    }

    // ── Custom item wrapper (reflective construction) ───────────────────────

    @Nested
    @DisplayName("Custom item accessors")
    class CustomItemAccessors {

        @Test
        @DisplayName("Given a custom item wrapper, then customItem() returns the custom item id")
        void customItem_returnsId() throws Exception {
            CustomItemWrapper wrapper = customItemWrapper("nexo:ruby_sword");
            assertTrue(wrapper.customItem().isPresent());
            assertEquals("nexo:ruby_sword", wrapper.customItem().get());
        }

        @Test
        @DisplayName("Given a custom item wrapper, then material() returns empty")
        void material_returnsEmpty_whenCustomItem() throws Exception {
            CustomItemWrapper wrapper = customItemWrapper("nexo:ruby_sword");
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
            CustomItemWrapper wrapper = materialWrapper(Material.IRON_INGOT);
            assertTrue(wrapper.equals(Material.IRON_INGOT));
        }

        @Test
        @DisplayName("Given different material, when comparing, then returns false")
        void returnsFalse_whenMaterialDiffers() {
            CustomItemWrapper wrapper = materialWrapper(Material.IRON_INGOT);
            assertFalse(wrapper.equals(Material.GOLD_INGOT));
        }

        @Test
        @DisplayName("Given custom item wrapper, when comparing with any material, then returns false")
        void returnsFalse_whenWrapperIsCustomItem() throws Exception {
            CustomItemWrapper wrapper = customItemWrapper("nexo:ruby");
            assertFalse(wrapper.equals(Material.DIAMOND));
        }
    }

    // ── equals(String) ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("equals(String)")
    class EqualsString {

        @Test
        @DisplayName("Given matching custom item id, when comparing, then returns true")
        void returnsTrue_whenCustomItemMatches() throws Exception {
            CustomItemWrapper wrapper = customItemWrapper("nexo:ruby_sword");
            assertTrue(wrapper.equals("nexo:ruby_sword"));
        }

        @Test
        @DisplayName("Given different custom item id, when comparing, then returns false")
        void returnsFalse_whenCustomItemDiffers() throws Exception {
            CustomItemWrapper wrapper = customItemWrapper("nexo:ruby_sword");
            assertFalse(wrapper.equals("nexo:diamond_sword"));
        }

        @Test
        @DisplayName("Given material wrapper, when comparing with any string, then returns false")
        void returnsFalse_whenWrapperIsMaterial() {
            CustomItemWrapper wrapper = materialWrapper(Material.STONE);
            assertFalse(wrapper.equals("stone"));
        }
    }

    // ── equals(CustomItemWrapper) ───────────────────────────────────────────

    @Nested
    @DisplayName("equals(CustomItemWrapper)")
    class EqualsWrapper {

        @Test
        @DisplayName("Given two material wrappers with same material, when comparing, then returns true")
        void returnsTrue_whenBothMaterialsMatch() {
            CustomItemWrapper a = materialWrapper(Material.DIAMOND);
            CustomItemWrapper b = materialWrapper(Material.DIAMOND);
            assertTrue(a.equals(b));
        }

        @Test
        @DisplayName("Given two material wrappers with different materials, when comparing, then returns false")
        void returnsFalse_whenMaterialsDiffer() {
            CustomItemWrapper a = materialWrapper(Material.DIAMOND);
            CustomItemWrapper b = materialWrapper(Material.EMERALD);
            assertFalse(a.equals(b));
        }

        @Test
        @DisplayName("Given two custom item wrappers with same id, when comparing, then returns true")
        void returnsTrue_whenBothCustomItemsMatch() throws Exception {
            CustomItemWrapper a = customItemWrapper("nexo:ruby");
            CustomItemWrapper b = customItemWrapper("nexo:ruby");
            assertTrue(a.equals(b));
        }

        @Test
        @DisplayName("Given two custom item wrappers with different ids, when comparing, then returns false")
        void returnsFalse_whenCustomItemsDiffer() throws Exception {
            CustomItemWrapper a = customItemWrapper("nexo:ruby");
            CustomItemWrapper b = customItemWrapper("nexo:sapphire");
            assertFalse(a.equals(b));
        }

        @Test
        @DisplayName("Given material wrapper and custom item wrapper, when comparing, then returns false")
        void returnsFalse_whenTypesAreMixed() throws Exception {
            CustomItemWrapper materialBased = materialWrapper(Material.STONE);
            CustomItemWrapper customBased = customItemWrapper("nexo:stone");
            assertFalse(materialBased.equals(customBased));
        }
    }

    // ── equals(Object) ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("equals(Object)")
    class EqualsObject {

        @Test
        @DisplayName("Given same instance, when comparing, then returns true")
        void returnsTrue_forSameInstance() {
            CustomItemWrapper wrapper = materialWrapper(Material.STONE);
            assertEquals(wrapper, wrapper);
        }

        @Test
        @DisplayName("Given two material wrappers with same material, when comparing as Object, then returns true")
        void returnsTrue_whenMaterialWrappersMatch() {
            CustomItemWrapper a = materialWrapper(Material.STONE);
            CustomItemWrapper b = materialWrapper(Material.STONE);
            assertEquals(a, b);
        }

        @Test
        @DisplayName("Given two custom item wrappers with same id, when comparing as Object, then returns true")
        void returnsTrue_whenCustomItemWrappersMatch() throws Exception {
            CustomItemWrapper a = customItemWrapper("nexo:ruby");
            CustomItemWrapper b = customItemWrapper("nexo:ruby");
            assertEquals(a, b);
        }

        @Test
        @DisplayName("Given material wrapper and different type, when comparing, then returns false")
        void returnsFalse_whenComparedWithDifferentType() {
            CustomItemWrapper wrapper = materialWrapper(Material.STONE);
            assertNotEquals(wrapper, "not a wrapper");
        }

        @Test
        @DisplayName("Given one material and one custom wrapper, when comparing as Object, then returns false")
        void returnsFalse_whenOneIsMaterialAndOtherIsCustom() throws Exception {
            CustomItemWrapper a = materialWrapper(Material.STONE);
            CustomItemWrapper b = customItemWrapper("nexo:stone");
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("Given two different material wrappers, when comparing as Object, then returns false")
        void returnsFalse_whenMaterialsDiffer() {
            CustomItemWrapper a = materialWrapper(Material.STONE);
            CustomItemWrapper b = materialWrapper(Material.DIRT);
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("Given two different custom item wrappers, when comparing as Object, then returns false")
        void returnsFalse_whenCustomItemsDiffer() throws Exception {
            CustomItemWrapper a = customItemWrapper("nexo:ruby");
            CustomItemWrapper b = customItemWrapper("nexo:sapphire");
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
            CustomItemWrapper a = materialWrapper(Material.STONE);
            CustomItemWrapper b = materialWrapper(Material.STONE);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Given two equal custom item wrappers, then hashCodes are equal")
        void hashCodesEqual_forEqualCustomItemWrappers() throws Exception {
            CustomItemWrapper a = customItemWrapper("nexo:ruby");
            CustomItemWrapper b = customItemWrapper("nexo:ruby");
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Given material wrapper, then hashCode is consistent across calls")
        void hashCode_isConsistent() {
            CustomItemWrapper wrapper = materialWrapper(Material.DIAMOND);
            int first = wrapper.hashCode();
            int second = wrapper.hashCode();
            assertEquals(first, second);
        }
    }

    // ── itemName ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("itemName")
    class ItemName {

        @Test
        @DisplayName("Given custom item wrapper with no hooks registered, when getting itemName, then returns Missing Item")
        void returnsMissingItem_whenNoHooksRegistered() throws Exception {
            CustomItemWrapper wrapper = customItemWrapper("nexo:ruby_sword");
            assertEquals("Missing Item", wrapper.itemName());
        }
    }
}

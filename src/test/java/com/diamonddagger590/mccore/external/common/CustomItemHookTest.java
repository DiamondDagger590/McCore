package com.diamonddagger590.mccore.external.common;

import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomItemHookTest {

    private CustomItemHook createHook(String recognizedItem) {
        return new CustomItemHook() {
            @Override
            public boolean isItem(@NotNull String itemName) {
                return recognizedItem.equals(itemName);
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
                return "";
            }
        };
    }

    @Test
    @DisplayName("Given a vanilla material wrapper, when checking isItem via default method, then returns false")
    void isItem_returnsFalse_whenWrapperHasNoCustomItem() {
        CustomItemHook hook = createHook("magic_sword");
        CustomItemWrapper vanillaWrapper = new CustomItemWrapper(Material.STONE);

        assertFalse(hook.isItem(vanillaWrapper));
    }

    @Test
    @DisplayName("Given a custom item wrapper matching the hook, when checking isItem via default method, then returns true")
    void isItem_returnsTrue_whenWrapperHasMatchingCustomItem() {
        CustomItemHook hook = createHook("magic_sword");
        CustomItemWrapper customWrapper = new TestCustomItemWrapper("magic_sword");

        assertTrue(hook.isItem(customWrapper));
    }

    @Test
    @DisplayName("Given a custom item wrapper not matching the hook, when checking isItem via default method, then returns false")
    void isItem_returnsFalse_whenWrapperHasNonMatchingCustomItem() {
        CustomItemHook hook = createHook("magic_sword");
        CustomItemWrapper customWrapper = new TestCustomItemWrapper("other_item");

        assertFalse(hook.isItem(customWrapper));
    }

    private static class TestCustomItemWrapper extends CustomItemWrapper {

        private final String testCustomItem;

        TestCustomItemWrapper(@NotNull String customItem) {
            super(Material.STONE);
            this.testCustomItem = customItem;
        }

        @NotNull
        @Override
        public Optional<String> customItem() {
            return Optional.ofNullable(testCustomItem);
        }
    }
}

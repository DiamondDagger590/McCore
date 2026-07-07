package com.diamonddagger590.mccore.external.common;

import com.diamonddagger590.mccore.util.item.CustomBlockWrapper;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomBlockHookTest {

    private CustomBlockHook createHook(String recognizedBlock) {
        return new CustomBlockHook() {
            @Override
            public boolean isCustomBlock(@NotNull org.bukkit.block.Block block) {
                return false;
            }

            @Override
            public boolean isCustomBlock(@NotNull String customBlock) {
                return recognizedBlock.equals(customBlock);
            }

            @Override
            public boolean isCustomBlockOfType(@NotNull org.bukkit.block.Block block, @NotNull String customBlockType) {
                return false;
            }

            @Override
            public void placeCustomBlock(@NotNull org.bukkit.Location location, @NotNull String blockId) {
            }

            @NotNull
            @Override
            public java.util.List<org.bukkit.inventory.ItemStack> drops(@NotNull org.bukkit.block.Block block,
                                                                        @NotNull org.bukkit.inventory.ItemStack itemToBreakWith,
                                                                        @org.jetbrains.annotations.Nullable org.bukkit.entity.Entity entityBreaking) {
                return java.util.List.of();
            }

            @Override
            public void playBlockDropEffects(@NotNull org.bukkit.block.Block block) {
            }

            @Override
            public void removeBlock(@NotNull org.bukkit.block.Block block) {
            }

            @NotNull
            @Override
            public Optional<java.util.Set<String>> blockModels(@NotNull org.bukkit.block.Block block) {
                return Optional.empty();
            }

            @NotNull
            @Override
            public String blockName(@NotNull CustomBlockWrapper customBlockWrapper) {
                return "";
            }
        };
    }

    @Test
    @DisplayName("Given a vanilla material wrapper, when checking isCustomBlock via default method, then returns false")
    void isCustomBlock_returnsFalse_whenWrapperHasNoCustomBlock() {
        CustomBlockHook hook = createHook("custom_ore");
        CustomBlockWrapper vanillaWrapper = new CustomBlockWrapper(Material.STONE);

        assertFalse(hook.isCustomBlock(vanillaWrapper));
    }

    @Test
    @DisplayName("Given a custom block wrapper matching the hook, when checking isCustomBlock via default method, then returns true")
    void isCustomBlock_returnsTrue_whenWrapperHasMatchingCustomBlock() {
        CustomBlockHook hook = createHook("custom_ore");
        CustomBlockWrapper customWrapper = new TestCustomBlockWrapper("custom_ore");

        assertTrue(hook.isCustomBlock(customWrapper));
    }

    @Test
    @DisplayName("Given a custom block wrapper not matching the hook, when checking isCustomBlock via default method, then returns false")
    void isCustomBlock_returnsFalse_whenWrapperHasNonMatchingCustomBlock() {
        CustomBlockHook hook = createHook("custom_ore");
        CustomBlockWrapper customWrapper = new TestCustomBlockWrapper("other_block");

        assertFalse(hook.isCustomBlock(customWrapper));
    }

    private static class TestCustomBlockWrapper extends CustomBlockWrapper {

        private final String testCustomBlock;

        TestCustomBlockWrapper(@NotNull String customBlock) {
            super(Material.STONE);
            this.testCustomBlock = customBlock;
        }

        @NotNull
        @Override
        public Optional<String> customBlock() {
            return Optional.ofNullable(testCustomBlock);
        }
    }
}

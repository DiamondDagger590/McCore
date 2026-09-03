package com.diamonddagger590.mccore.util.item;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.external.common.CustomBlockHook;
import com.diamonddagger590.mccore.external.common.CustomItemHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import com.diamonddagger590.mccore.testing.TestCorePlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
import org.mockbukkit.mockbukkit.ServerMock;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomBlockWrapperMockBukkitTest {

    private ServerMock server;
    private TestCorePlugin plugin;
    private World world;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(TestCorePlugin.class);
        RegistryResetExtension.setupRegistry();
        world = server.addSimpleWorld("test_world");
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
        MockBukkit.unmock();
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

        private final String recognizedBlock;
        private boolean removeBlockCalled = false;
        private boolean playEffectsCalled = false;

        TestCustomBlockPluginHook() {
            this("nexo:ruby_ore");
        }

        TestCustomBlockPluginHook(String recognizedBlock) {
            super(null);
            this.recognizedBlock = recognizedBlock;
        }

        @Override
        public boolean isCustomBlock(@NotNull Block block) {
            return false;
        }

        @Override
        public boolean isCustomBlock(@NotNull String customBlock) {
            return recognizedBlock.equals(customBlock);
        }

        @Override
        public boolean isCustomBlockOfType(@NotNull Block block, @NotNull String customBlockType) {
            return recognizedBlock.equals(customBlockType);
        }

        @Override
        public void placeCustomBlock(@NotNull Location location, @NotNull String blockId) {
        }

        @NotNull
        @Override
        public List<ItemStack> drops(@NotNull Block block, @NotNull ItemStack itemToBreakWith, @Nullable Entity entityBreaking) {
            return List.of(new ItemStack(Material.DIAMOND));
        }

        @Override
        public void playBlockDropEffects(@NotNull Block block) {
            playEffectsCalled = true;
        }

        @Override
        public void removeBlock(@NotNull Block block) {
            removeBlockCalled = true;
        }

        @NotNull
        @Override
        public Optional<Set<String>> blockModels(@NotNull Block block) {
            return Optional.of(Set.of(recognizedBlock));
        }

        @NotNull
        @Override
        public String blockName(@NotNull CustomBlockWrapper customBlockWrapper) {
            return "Ruby Ore";
        }

        boolean wasRemoveBlockCalled() {
            return removeBlockCalled;
        }

        boolean wasPlayEffectsCalled() {
            return playEffectsCalled;
        }
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
            return false;
        }

        @Override
        public boolean isItemOfType(@NotNull ItemStack itemStack, @NotNull String itemName) {
            return false;
        }

        @NotNull
        @Override
        public Optional<ItemStack> item(@NotNull String itemName) {
            if (recognizedItem.equals(itemName)) {
                return Optional.of(providedItem);
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
            return "Ruby Ore Item";
        }
    }

    @Nested
    @DisplayName("String constructor")
    class StringConstructor {

        @Test
        @DisplayName("Given a vanilla block name, when constructing, then sets material and null customBlock")
        void constructor_setsMaterial_whenVanillaBlockName() {
            CustomBlockWrapper wrapper = new CustomBlockWrapper("stone");
            assertTrue(wrapper.isVanilla());
            assertFalse(wrapper.isCustom());
            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.STONE, wrapper.material().get());
            assertFalse(wrapper.customBlock().isPresent());
        }

        @Test
        @DisplayName("Given a non-vanilla block name, when constructing, then sets customBlock and null material")
        void constructor_setsCustomBlock_whenNonVanillaBlockName() {
            CustomBlockWrapper wrapper = new CustomBlockWrapper("totally_fake_block");
            assertFalse(wrapper.isVanilla());
            assertTrue(wrapper.isCustom());
            assertFalse(wrapper.material().isPresent());
            assertTrue(wrapper.customBlock().isPresent());
            assertEquals("totally_fake_block", wrapper.customBlock().get());
        }

        @Test
        @DisplayName("Given another vanilla block name, when constructing, then correctly identifies it as vanilla")
        void constructor_setsMaterial_whenAnotherVanillaBlockName() {
            CustomBlockWrapper wrapper = new CustomBlockWrapper("oak_log");
            assertTrue(wrapper.isVanilla());
            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.OAK_LOG, wrapper.material().get());
        }
    }

    @Nested
    @DisplayName("isVanillaBlock (static)")
    class IsVanillaBlock {

        @Test
        @DisplayName("Given a vanilla block id, when checking isVanillaBlock, then returns true")
        void isVanillaBlock_returnsTrue_whenVanillaBlockId() {
            assertTrue(CustomBlockWrapper.isVanillaBlock("stone"));
        }

        @Test
        @DisplayName("Given a custom block id, when checking isVanillaBlock, then returns false")
        void isVanillaBlock_returnsFalse_whenCustomBlockId() {
            assertFalse(CustomBlockWrapper.isVanillaBlock("totally_fake_block"));
        }

        @Test
        @DisplayName("Given another vanilla block id, when checking isVanillaBlock, then returns true")
        void isVanillaBlock_returnsTrue_whenAnotherVanillaBlockId() {
            assertTrue(CustomBlockWrapper.isVanillaBlock("dirt"));
        }

        @Test
        @DisplayName("Given an invalid block id, when checking isVanillaBlock, then returns false")
        void isVanillaBlock_returnsFalse_whenInvalidBlockId() {
            assertFalse(CustomBlockWrapper.isVanillaBlock("totally_not_a_block"));
        }
    }

    @Nested
    @DisplayName("blockName for vanilla material")
    class BlockNameVanilla {

        @Test
        @DisplayName("Given a vanilla material wrapper, when getting blockName, then returns lang tag with translation key")
        void blockName_returnsLangTag_whenVanillaMaterial() {
            CustomBlockWrapper wrapper = new CustomBlockWrapper(Material.STONE);
            String name = wrapper.blockName();
            assertEquals("<lang:" + Material.STONE.translationKey() + ">", name);
        }

        @Test
        @DisplayName("Given a different vanilla material, when getting blockName, then returns correct lang tag")
        void blockName_returnsCorrectLangTag_whenDifferentVanillaMaterial() {
            CustomBlockWrapper wrapper = new CustomBlockWrapper(Material.IRON_ORE);
            String name = wrapper.blockName();
            assertEquals("<lang:" + Material.IRON_ORE.translationKey() + ">", name);
        }
    }

    @Nested
    @DisplayName("itemBuilder")
    class ItemBuilderTests {

        @Test
        @DisplayName("Given a material wrapper, when getting itemBuilder, then returns builder with material ItemStack")
        void itemBuilder_returnsBuilderWithMaterial_whenMaterialWrapper() {
            CustomBlockWrapper wrapper = new CustomBlockWrapper(Material.STONE);
            ItemBuilder builder = wrapper.itemBuilder();
            assertNotNull(builder);
            ItemStack item = builder.asItemStack();
            assertEquals(Material.STONE, item.getType());
        }

        @Test
        @DisplayName("Given a custom block with a registered CustomItemHook, when getting itemBuilder, then returns builder from hook")
        void itemBuilder_returnsBuilderFromHook_whenCustomItemHookRegistered() throws Exception {
            ItemStack hookItem = new ItemStack(Material.DIAMOND_BLOCK);
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomItemPluginHook("nexo:ruby_ore", hookItem));
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            ItemBuilder builder = wrapper.itemBuilder();
            assertNotNull(builder);
            ItemStack item = builder.asItemStack();
            assertEquals(Material.DIAMOND_BLOCK, item.getType());
        }

        @Test
        @DisplayName("Given a custom block with no hooks, when getting itemBuilder, then returns builder with AIR")
        void itemBuilder_returnsBuilderWithAir_whenNoHooksRegistered() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            ItemBuilder builder = wrapper.itemBuilder();
            assertNotNull(builder);
            ItemStack item = builder.asItemStack();
            assertEquals(Material.AIR, item.getType());
        }
    }

    @Nested
    @DisplayName("equals(Block)")
    class EqualsBlock {

        @Test
        @DisplayName("Given a material wrapper matching a block type, when comparing, then returns true")
        void equals_returnsTrue_whenMaterialMatchesBlockType() {
            Block block = world.getBlockAt(0, 64, 0);
            block.setType(Material.STONE);
            CustomBlockWrapper wrapper = new CustomBlockWrapper(Material.STONE);
            assertTrue(wrapper.equals(block));
        }

        @Test
        @DisplayName("Given a material wrapper not matching a block type, when comparing, then returns false")
        void equals_returnsFalse_whenMaterialDoesNotMatchBlockType() {
            Block block = world.getBlockAt(0, 64, 0);
            block.setType(Material.DIRT);
            CustomBlockWrapper wrapper = new CustomBlockWrapper(Material.STONE);
            assertFalse(wrapper.equals(block));
        }

        @Test
        @DisplayName("Given a custom block wrapper with a hook that matches, when comparing, then returns true")
        void equals_returnsTrue_whenCustomBlockHookMatches() throws Exception {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomBlockPluginHook("nexo:ruby_ore"));
            Block block = world.getBlockAt(0, 64, 0);
            block.setType(Material.STONE);
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertTrue(wrapper.equals(block));
        }

        @Test
        @DisplayName("Given a custom block wrapper with no hooks, when comparing, then returns false")
        void equals_returnsFalse_whenCustomBlockNoHooks() throws Exception {
            Block block = world.getBlockAt(0, 64, 0);
            block.setType(Material.STONE);
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertFalse(wrapper.equals(block));
        }
    }

    @Nested
    @DisplayName("Block constructor")
    class BlockConstructor {

        @Test
        @DisplayName("Given a vanilla block with no hooks, when constructing, then sets material to block type")
        void constructor_setsMaterial_whenVanillaBlockNoHooks() {
            Block block = world.getBlockAt(0, 64, 0);
            block.setType(Material.STONE);
            CustomBlockWrapper wrapper = new CustomBlockWrapper(block);
            assertTrue(wrapper.isVanilla());
            assertFalse(wrapper.isCustom());
            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.STONE, wrapper.material().get());
        }

        @Test
        @DisplayName("Given a block with a hook recognizing it as custom, when constructing, then sets customBlock")
        void constructor_setsCustomBlock_whenHookRecognizesBlock() {
            TestCustomBlockPluginHook hook = new TestCustomBlockPluginHook("nexo:ruby_ore") {
                @Override
                public boolean isCustomBlock(@NotNull Block block) {
                    return true;
                }
            };
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);
            Block block = world.getBlockAt(0, 64, 0);
            block.setType(Material.STONE);
            CustomBlockWrapper wrapper = new CustomBlockWrapper(block);
            assertTrue(wrapper.isCustom());
            assertFalse(wrapper.isVanilla());
            assertTrue(wrapper.customBlock().isPresent());
            assertEquals("nexo:ruby_ore", wrapper.customBlock().get());
        }

        @Test
        @DisplayName("Given a block with a hook that does not recognize it, when constructing, then sets material")
        void constructor_setsMaterial_whenHookDoesNotRecognize() {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomBlockPluginHook("nexo:ruby_ore"));
            Block block = world.getBlockAt(0, 64, 0);
            block.setType(Material.IRON_ORE);
            CustomBlockWrapper wrapper = new CustomBlockWrapper(block);
            assertTrue(wrapper.isVanilla());
            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.IRON_ORE, wrapper.material().get());
        }
    }

    @Nested
    @DisplayName("customModels (static)")
    class CustomModels {

        @Test
        @DisplayName("Given a block with no hooks, when getting customModels, then returns empty set")
        void customModels_returnsEmptySet_whenNoHooksRegistered() {
            Block block = world.getBlockAt(0, 64, 0);
            block.setType(Material.STONE);
            Optional<Set<String>> models = CustomBlockWrapper.customModels(block);
            assertTrue(models.isPresent());
            assertTrue(models.get().isEmpty());
        }

        @Test
        @DisplayName("Given a block with a hook, when getting customModels, then returns models from hook")
        void customModels_returnsModelsFromHook_whenHookRegistered() {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomBlockPluginHook("nexo:ruby_ore"));
            Block block = world.getBlockAt(0, 64, 0);
            block.setType(Material.STONE);
            Optional<Set<String>> models = CustomBlockWrapper.customModels(block);
            assertTrue(models.isPresent());
            assertTrue(models.get().contains("nexo:ruby_ore"));
        }
    }

    @Nested
    @DisplayName("drops (static)")
    class Drops {

        @Test
        @DisplayName("Given a block with no hooks, when getting drops, then returns block getDrops")
        void drops_returnsBlockDrops_whenNoHooksRegistered() {
            Block block = world.getBlockAt(0, 64, 0);
            block.setType(Material.STONE);
            ItemStack tool = new ItemStack(Material.DIAMOND_PICKAXE);
            List<ItemStack> drops = CustomBlockWrapper.drops(block, tool, null);
            assertNotNull(drops);
        }

        @Test
        @DisplayName("Given a block with a hook, when getting drops, then returns hook drops")
        void drops_returnsHookDrops_whenHookRegistered() {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                    .register(new TestCustomBlockPluginHook("nexo:ruby_ore"));
            Block block = world.getBlockAt(0, 64, 0);
            block.setType(Material.STONE);
            ItemStack tool = new ItemStack(Material.DIAMOND_PICKAXE);
            List<ItemStack> drops = CustomBlockWrapper.drops(block, tool, null);
            assertNotNull(drops);
            assertEquals(1, drops.size());
            assertEquals(Material.DIAMOND, drops.get(0).getType());
        }
    }

    @Nested
    @DisplayName("removeBlock (static)")
    class RemoveBlock {

        @Test
        @DisplayName("Given a block with no hooks, when removing, then sets block type to AIR")
        void removeBlock_setsToAir_whenNoHooksRegistered() {
            Block block = world.getBlockAt(0, 64, 0);
            block.setType(Material.STONE);
            CustomBlockWrapper.removeBlock(block);
            assertEquals(Material.AIR, block.getType());
        }

        @Test
        @DisplayName("Given a block with a hook, when removing, then delegates to hook")
        void removeBlock_delegatesToHook_whenHookRegistered() {
            TestCustomBlockPluginHook hook = new TestCustomBlockPluginHook("nexo:ruby_ore");
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);
            Block block = world.getBlockAt(0, 64, 0);
            block.setType(Material.STONE);
            CustomBlockWrapper.removeBlock(block);
            assertTrue(hook.wasRemoveBlockCalled());
        }
    }

    @Nested
    @DisplayName("playBlockDropEffects (static)")
    class PlayBlockDropEffects {

        @Test
        @DisplayName("Given a block with no hooks, when playing effects, then does not throw")
        void playBlockDropEffects_doesNotThrow_whenNoHooksRegistered() {
            Block block = world.getBlockAt(0, 64, 0);
            block.setType(Material.STONE);
            CustomBlockWrapper.playBlockDropEffects(block);
        }

        @Test
        @DisplayName("Given a block with a hook, when playing effects, then delegates to hook")
        void playBlockDropEffects_delegatesToHook_whenHookRegistered() {
            TestCustomBlockPluginHook hook = new TestCustomBlockPluginHook("nexo:ruby_ore");
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);
            Block block = world.getBlockAt(0, 64, 0);
            block.setType(Material.STONE);
            CustomBlockWrapper.playBlockDropEffects(block);
            assertTrue(hook.wasPlayEffectsCalled());
        }
    }
}

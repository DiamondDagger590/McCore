package com.diamonddagger590.mccore.external.itemsadder;

import com.diamonddagger590.mccore.util.item.CustomBlockWrapper;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.SoundGroup;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CoreItemsAdderHookTest {

    private CoreItemsAdderHook hook;
    private MockedStatic<CustomStack> customStackMock;
    private MockedStatic<CustomBlock> customBlockMock;

    @BeforeEach
    void setUp() throws Exception {
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);
        hook = (CoreItemsAdderHook) unsafe.allocateInstance(CoreItemsAdderHook.class);

        customStackMock = mockStatic(CustomStack.class);
        customBlockMock = mockStatic(CustomBlock.class);
    }

    @AfterEach
    void tearDown() {
        customStackMock.close();
        customBlockMock.close();
    }

    @Nested
    @DisplayName("item")
    class Item {

        @Test
        @DisplayName("Given a valid item name, when getting item, then returns the ItemStack")
        void returnsItemStack_whenItemExists() {
            ItemStack expected = mock(ItemStack.class);
            CustomStack stack = mock(CustomStack.class);
            when(stack.getItemStack()).thenReturn(expected);
            customStackMock.when(() -> CustomStack.getInstance("ia:sword")).thenReturn(stack);

            Optional<ItemStack> result = hook.item("ia:sword");

            assertTrue(result.isPresent());
            assertEquals(expected, result.get());
        }

        @Test
        @DisplayName("Given an invalid item name, when getting item, then returns empty")
        void returnsEmpty_whenItemDoesNotExist() {
            customStackMock.when(() -> CustomStack.getInstance("unknown")).thenReturn(null);

            Optional<ItemStack> result = hook.item("unknown");

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("itemModels")
    class ItemModels {

        @Test
        @DisplayName("Given a custom ItemStack, when getting itemModels, then returns model path set")
        void returnsModelPath_whenItemIsCustom() {
            ItemStack itemStack = mock(ItemStack.class);
            CustomStack stack = mock(CustomStack.class);
            when(stack.getModelPath()).thenReturn("ia:sword");
            customStackMock.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(stack);

            Optional<Set<String>> result = hook.itemModels(itemStack);

            assertTrue(result.isPresent());
            assertEquals(Set.of("ia:sword"), result.get());
        }

        @Test
        @DisplayName("Given a vanilla ItemStack, when getting itemModels, then returns empty")
        void returnsEmpty_whenItemIsVanilla() {
            ItemStack itemStack = mock(ItemStack.class);
            customStackMock.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(null);

            Optional<Set<String>> result = hook.itemModels(itemStack);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("isItem(String)")
    class IsItemByName {

        @Test
        @DisplayName("Given a registered item name, when checking isItem, then returns true")
        void returnsTrue_whenItemInRegistry() {
            customStackMock.when(() -> CustomStack.isInRegistry("ia:sword")).thenReturn(true);

            assertTrue(hook.isItem("ia:sword"));
        }

        @Test
        @DisplayName("Given an unregistered item name, when checking isItem, then returns false")
        void returnsFalse_whenItemNotInRegistry() {
            customStackMock.when(() -> CustomStack.isInRegistry("unknown")).thenReturn(false);

            assertFalse(hook.isItem("unknown"));
        }
    }

    @Nested
    @DisplayName("isItem(ItemStack)")
    class IsItemByItemStack {

        @Test
        @DisplayName("Given a custom ItemStack, when checking isItem, then returns true")
        void returnsTrue_whenItemIsCustom() {
            ItemStack itemStack = mock(ItemStack.class);
            customStackMock.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(mock(CustomStack.class));

            assertTrue(hook.isItem(itemStack));
        }

        @Test
        @DisplayName("Given a vanilla ItemStack, when checking isItem, then returns false")
        void returnsFalse_whenItemIsVanilla() {
            ItemStack itemStack = mock(ItemStack.class);
            customStackMock.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(null);

            assertFalse(hook.isItem(itemStack));
        }
    }

    @Nested
    @DisplayName("isItemOfType")
    class IsItemOfType {

        @Test
        @DisplayName("Given a matching custom ItemStack, when checking isItemOfType, then returns true")
        void returnsTrue_whenItemMatchesType() {
            ItemStack itemStack = mock(ItemStack.class);
            CustomStack stack = mock(CustomStack.class);
            when(stack.getModelPath()).thenReturn("ia:sword");
            customStackMock.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(stack);

            assertTrue(hook.isItemOfType(itemStack, "ia:sword"));
        }

        @Test
        @DisplayName("Given a non-matching custom ItemStack, when checking isItemOfType, then returns false")
        void returnsFalse_whenItemDoesNotMatchType() {
            ItemStack itemStack = mock(ItemStack.class);
            CustomStack stack = mock(CustomStack.class);
            when(stack.getModelPath()).thenReturn("ia:axe");
            customStackMock.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(stack);

            assertFalse(hook.isItemOfType(itemStack, "ia:sword"));
        }

        @Test
        @DisplayName("Given a vanilla ItemStack, when checking isItemOfType, then returns false")
        void returnsFalse_whenItemIsVanilla() {
            ItemStack itemStack = mock(ItemStack.class);
            customStackMock.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(null);

            assertFalse(hook.isItemOfType(itemStack, "ia:sword"));
        }
    }

    @Nested
    @DisplayName("isCustomBlock(Block)")
    class IsCustomBlockByBlock {

        @Test
        @DisplayName("Given a custom block, when checking isCustomBlock, then returns true")
        void returnsTrue_whenBlockIsCustom() {
            Block block = mock(Block.class);
            customBlockMock.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(mock(CustomBlock.class));

            assertTrue(hook.isCustomBlock(block));
        }

        @Test
        @DisplayName("Given a vanilla block, when checking isCustomBlock, then returns false")
        void returnsFalse_whenBlockIsVanilla() {
            Block block = mock(Block.class);
            customBlockMock.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(null);

            assertFalse(hook.isCustomBlock(block));
        }
    }

    @Nested
    @DisplayName("isCustomBlock(String)")
    class IsCustomBlockByName {

        @Test
        @DisplayName("ItemsAdder does not support isCustomBlock by name, always returns false")
        void alwaysReturnsFalse() {
            assertFalse(hook.isCustomBlock("ia:ore"));
        }
    }

    @Nested
    @DisplayName("isCustomBlockOfType")
    class IsCustomBlockOfType {

        @Test
        @DisplayName("Given a matching custom block, when checking isCustomBlockOfType, then returns true")
        void returnsTrue_whenBlockMatchesType() {
            Block block = mock(Block.class);
            CustomBlock customBlock = mock(CustomBlock.class);
            when(customBlock.getModelPath()).thenReturn("ia:ore");
            customBlockMock.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(customBlock);

            assertTrue(hook.isCustomBlockOfType(block, "ia:ore"));
        }

        @Test
        @DisplayName("Given a custom block with non-matching type, when checking isCustomBlockOfType, then returns false")
        void returnsFalse_whenBlockDoesNotMatchType() {
            Block block = mock(Block.class);
            CustomBlock customBlock = mock(CustomBlock.class);
            when(customBlock.getModelPath()).thenReturn("ia:log");
            customBlockMock.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(customBlock);

            assertFalse(hook.isCustomBlockOfType(block, "ia:ore"));
        }

        @Test
        @DisplayName("Given a vanilla block, when checking isCustomBlockOfType, then returns false")
        void returnsFalse_whenBlockIsVanilla() {
            Block block = mock(Block.class);
            customBlockMock.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(null);

            assertFalse(hook.isCustomBlockOfType(block, "ia:ore"));
        }
    }

    @Nested
    @DisplayName("blockModels")
    class BlockModels {

        @Test
        @DisplayName("Given a custom block, when getting blockModels, then returns the model path")
        void returnsModelPath_whenBlockIsCustom() {
            Block block = mock(Block.class);
            CustomBlock customBlock = mock(CustomBlock.class);
            when(customBlock.getModelPath()).thenReturn("ia:ore");
            customBlockMock.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(customBlock);

            Optional<Set<String>> result = hook.blockModels(block);

            assertTrue(result.isPresent());
            assertEquals(Set.of("ia:ore"), result.get());
        }

        @Test
        @DisplayName("Given a vanilla block, when getting blockModels, then returns empty")
        void returnsEmpty_whenBlockIsVanilla() {
            Block block = mock(Block.class);
            customBlockMock.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(null);

            Optional<Set<String>> result = hook.blockModels(block);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("placeCustomBlock")
    class PlaceCustomBlock {

        @Test
        @DisplayName("Given isCustomBlock(String) always returns false for ItemsAdder, when placing, then throws")
        void throwsIllegalArgument_becauseIsCustomBlockByStringAlwaysReturnsFalse() {
            Location location = mock(Location.class);

            assertThrows(IllegalArgumentException.class, () -> hook.placeCustomBlock(location, "ia:ore"));
        }
    }

    @Nested
    @DisplayName("drops")
    class Drops {

        @Test
        @DisplayName("Given a custom block, when getting drops, then delegates to CustomBlock.getLoot")
        void delegatesToCustomBlockGetLoot_whenBlockIsCustom() {
            Block block = mock(Block.class);
            ItemStack tool = mock(ItemStack.class);
            Entity entity = mock(Entity.class);
            List<ItemStack> expectedDrops = List.of(mock(ItemStack.class));

            customBlockMock.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(mock(CustomBlock.class));
            customBlockMock.when(() -> CustomBlock.getLoot(block, tool, true)).thenReturn(expectedDrops);

            List<ItemStack> result = hook.drops(block, tool, entity);

            assertEquals(expectedDrops, result);
        }

        @Test
        @DisplayName("Given a vanilla block, when getting drops, then delegates to block.getDrops")
        void delegatesToBlockGetDrops_whenBlockIsVanilla() {
            Block block = mock(Block.class);
            ItemStack tool = mock(ItemStack.class);
            Entity entity = mock(Entity.class);
            Collection<ItemStack> blockDrops = List.of(mock(ItemStack.class));

            customBlockMock.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(null);
            when(block.getDrops(tool, entity)).thenReturn(blockDrops);

            List<ItemStack> result = hook.drops(block, tool, entity);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("removeBlock")
    class RemoveBlock {

        @Test
        @DisplayName("Given a custom block that removes successfully, when removing, then completes without error")
        void removesSuccessfully_whenCustomBlockRemoves() {
            Block block = mock(Block.class);
            CustomBlock customBlock = mock(CustomBlock.class);
            when(customBlock.remove()).thenReturn(true);
            customBlockMock.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(customBlock);

            hook.removeBlock(block);

            verify(customBlock).remove();
        }

        @Test
        @DisplayName("Given a custom block that fails to remove, when removing, then throws IllegalStateException")
        void throwsIllegalState_whenCustomBlockFailsToRemove() {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);

            CustomBlock customBlock = mock(CustomBlock.class);
            when(customBlock.remove()).thenReturn(false);
            when(customBlock.getModelPath()).thenReturn("ia:ore");
            customBlockMock.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(customBlock);

            assertThrows(IllegalStateException.class, () -> hook.removeBlock(block));
        }

        @Test
        @DisplayName("Given a vanilla block, when removing, then sets type to AIR")
        void setsTypeToAir_whenBlockIsVanilla() {
            Block block = mock(Block.class);
            customBlockMock.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(null);

            hook.removeBlock(block);

            verify(block).setType(Material.AIR);
        }
    }

    @Nested
    @DisplayName("playBlockDropEffects")
    class PlayBlockDropEffects {

        @Test
        @DisplayName("Given a custom block, when playing effects, then delegates to CustomBlock break methods")
        void delegatesToCustomBlockBreakMethods_whenBlockIsCustom() {
            Block block = mock(Block.class);
            CustomBlock customBlock = mock(CustomBlock.class);
            customBlockMock.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(customBlock);

            hook.playBlockDropEffects(block);

            verify(customBlock).playBreakEffect();
            verify(customBlock).playBreakSound();
            verify(customBlock).playBreakParticles();
        }

        @Test
        @DisplayName("Given a vanilla block, when playing effects, then plays sound and spawns particles")
        void playsVanillaEffects_whenBlockIsVanilla() {
            Block block = mock(Block.class);
            World world = mock(World.class);
            Location location = mock(Location.class);
            Location clonedLocation = mock(Location.class);
            BlockData blockData = mock(BlockData.class);
            SoundGroup soundGroup = mock(SoundGroup.class);

            customBlockMock.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(null);
            when(block.getWorld()).thenReturn(world);
            when(block.getLocation()).thenReturn(location);
            when(location.clone()).thenReturn(clonedLocation);
            when(clonedLocation.add(0.5, 0.5, 0.5)).thenReturn(clonedLocation);
            when(block.getBlockData()).thenReturn(blockData);
            when(block.getBlockSoundGroup()).thenReturn(soundGroup);
            when(soundGroup.getBreakSound()).thenReturn(org.bukkit.Sound.BLOCK_STONE_BREAK);
            when(soundGroup.getVolume()).thenReturn(1.0f);
            when(soundGroup.getPitch()).thenReturn(1.0f);

            hook.playBlockDropEffects(block);

            verify(world).playSound(location, org.bukkit.Sound.BLOCK_STONE_BREAK, 1.0f, 1.0f);
            verify(world).spawnParticle(
                eq(Particle.BLOCK),
                eq(clonedLocation),
                eq(20),
                eq(0.25),
                eq(0.25),
                eq(0.25),
                eq(0.05),
                eq(blockData));
        }
    }

    @Nested
    @DisplayName("itemName")
    class ItemName {

        @Test
        @DisplayName("Given a custom item with Adventure Component name, when getting itemName, then returns serialized name")
        void returnsComponentName_whenItemHasComponentName() {
            CustomStack stack = mock(CustomStack.class);
            when(stack.itemName()).thenReturn(Component.text("Magic Sword"));
            customStackMock.when(() -> CustomStack.getInstance("ia:magic_sword")).thenReturn(stack);

            CustomItemWrapper wrapper = new TestCustomItemWrapper("ia:magic_sword");

            assertEquals("Magic Sword", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a custom item with empty Component but legacy name, when getting itemName, then returns stripped legacy name")
        void returnsLegacyName_whenComponentNameIsEmpty() {
            CustomStack stack = mock(CustomStack.class);
            when(stack.itemName()).thenReturn(Component.empty());
            when(stack.getDisplayName()).thenReturn("§6Magic §cSword");
            customStackMock.when(() -> CustomStack.getInstance("ia:magic_sword")).thenReturn(stack);

            CustomItemWrapper wrapper = new TestCustomItemWrapper("ia:magic_sword");

            assertEquals("Magic Sword", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a custom item with null Component and null legacy name, when getting itemName, then returns formatted id")
        void returnsFormattedId_whenNoNameAvailable() {
            CustomStack stack = mock(CustomStack.class);
            when(stack.itemName()).thenReturn(null);
            when(stack.getDisplayName()).thenReturn(null);
            customStackMock.when(() -> CustomStack.getInstance("ia:cool_sword")).thenReturn(stack);

            CustomItemWrapper wrapper = new TestCustomItemWrapper("ia:cool_sword");

            assertEquals("Cool Sword", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a custom item not found in registry, when getting itemName, then returns formatted id")
        void returnsFormattedId_whenItemNotInRegistry() {
            customStackMock.when(() -> CustomStack.getInstance("ia:missing_item")).thenReturn(null);

            CustomItemWrapper wrapper = new TestCustomItemWrapper("ia:missing_item");

            assertEquals("Missing Item", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a custom item id without namespace, when getting itemName, then formats the full id")
        void returnsFormattedId_whenNoNamespace() {
            customStackMock.when(() -> CustomStack.getInstance("cool_sword")).thenReturn(null);

            CustomItemWrapper wrapper = new TestCustomItemWrapper("cool_sword");

            assertEquals("Cool Sword", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a vanilla item wrapper, when getting itemName, then returns formatted material name")
        void returnsFormattedMaterialName_whenVanilla() {
            CustomItemWrapper wrapper = new CustomItemWrapper(Material.OAK_LOG);

            assertEquals("Oak Log", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a custom item with empty legacy name, when getting itemName, then returns formatted id")
        void returnsFormattedId_whenLegacyNameIsEmpty() {
            CustomStack stack = mock(CustomStack.class);
            when(stack.itemName()).thenReturn(null);
            when(stack.getDisplayName()).thenReturn("");
            customStackMock.when(() -> CustomStack.getInstance("ia:cool_sword")).thenReturn(stack);

            CustomItemWrapper wrapper = new TestCustomItemWrapper("ia:cool_sword");

            assertEquals("Cool Sword", hook.itemName(wrapper));
        }
    }

    @Nested
    @DisplayName("blockName")
    class BlockName {

        @Test
        @DisplayName("Given a custom block with Adventure Component name, when getting blockName, then returns serialized name")
        void returnsComponentName_whenBlockHasComponentName() {
            CustomStack stack = mock(CustomStack.class);
            when(stack.itemName()).thenReturn(Component.text("Custom Ore"));
            customStackMock.when(() -> CustomStack.getInstance("ia:custom_ore")).thenReturn(stack);

            CustomBlockWrapper wrapper = new TestCustomBlockWrapper("ia:custom_ore");

            assertEquals("Custom Ore", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a custom block with legacy name only, when getting blockName, then returns stripped legacy name")
        void returnsLegacyName_whenComponentNameIsEmpty() {
            CustomStack stack = mock(CustomStack.class);
            when(stack.itemName()).thenReturn(Component.empty());
            when(stack.getDisplayName()).thenReturn("§aCustom §bOre");
            customStackMock.when(() -> CustomStack.getInstance("ia:custom_ore")).thenReturn(stack);

            CustomBlockWrapper wrapper = new TestCustomBlockWrapper("ia:custom_ore");

            assertEquals("Custom Ore", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a custom block not in registry, when getting blockName, then returns formatted id")
        void returnsFormattedId_whenBlockNotInRegistry() {
            customStackMock.when(() -> CustomStack.getInstance("ia:missing_block")).thenReturn(null);

            CustomBlockWrapper wrapper = new TestCustomBlockWrapper("ia:missing_block");

            assertEquals("Missing Block", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a vanilla block wrapper, when getting blockName, then returns formatted material name")
        void returnsFormattedMaterialName_whenVanilla() {
            CustomBlockWrapper wrapper = new CustomBlockWrapper(Material.STONE);

            assertEquals("Stone", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a custom block with null names, when getting blockName, then returns formatted id")
        void returnsFormattedId_whenNoNamesAvailable() {
            CustomStack stack = mock(CustomStack.class);
            when(stack.itemName()).thenReturn(null);
            when(stack.getDisplayName()).thenReturn(null);
            customStackMock.when(() -> CustomStack.getInstance("ia:fancy_block")).thenReturn(stack);

            CustomBlockWrapper wrapper = new TestCustomBlockWrapper("ia:fancy_block");

            assertEquals("Fancy Block", hook.blockName(wrapper));
        }
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
            return Optional.of(testCustomItem);
        }
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
            return Optional.of(testCustomBlock);
        }
    }
}

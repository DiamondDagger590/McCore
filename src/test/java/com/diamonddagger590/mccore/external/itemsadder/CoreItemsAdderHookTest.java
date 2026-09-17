package com.diamonddagger590.mccore.external.itemsadder;

import com.diamonddagger590.mccore.CorePlugin;
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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreItemsAdderHookTest {

    @Mock
    private CorePlugin plugin;

    private CoreItemsAdderHook hook;

    @BeforeEach
    void setUp() {
        hook = new CoreItemsAdderHook(plugin);
    }

    @Nested
    @DisplayName("item(String)")
    class ItemTests {

        @Test
        @DisplayName("Returns the ItemStack when CustomStack exists for the given item ID")
        void item_returnsItemStack_whenCustomStackExists() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                CustomStack customStack = mock(CustomStack.class);
                ItemStack expectedItem = mock(ItemStack.class);
                mocked.when(() -> CustomStack.getInstance("my_item")).thenReturn(customStack);
                when(customStack.getItemStack()).thenReturn(expectedItem);

                Optional<ItemStack> result = hook.item("my_item");

                assertTrue(result.isPresent());
                assertEquals(expectedItem, result.get());
            }
        }

        @Test
        @DisplayName("Returns empty when CustomStack does not exist for the given item ID")
        void item_returnsEmpty_whenCustomStackDoesNotExist() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                mocked.when(() -> CustomStack.getInstance("nonexistent")).thenReturn(null);

                Optional<ItemStack> result = hook.item("nonexistent");

                assertFalse(result.isPresent());
            }
        }
    }

    @Nested
    @DisplayName("itemModels(ItemStack)")
    class ItemModelsTests {

        @Test
        @DisplayName("Returns model path set when CustomStack exists for the item")
        void itemModels_returnsModelSet_whenCustomStackExists() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                CustomStack customStack = mock(CustomStack.class);
                ItemStack itemStack = mock(ItemStack.class);
                mocked.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(customStack);
                when(customStack.getModelPath()).thenReturn("my_namespace:my_model");

                Optional<Set<String>> result = hook.itemModels(itemStack);

                assertTrue(result.isPresent());
                assertEquals(Set.of("my_namespace:my_model"), result.get());
            }
        }

        @Test
        @DisplayName("Returns empty when CustomStack does not exist for the item")
        void itemModels_returnsEmpty_whenNoCustomStack() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                ItemStack itemStack = mock(ItemStack.class);
                mocked.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(null);

                Optional<Set<String>> result = hook.itemModels(itemStack);

                assertFalse(result.isPresent());
            }
        }
    }

    @Nested
    @DisplayName("isItem")
    class IsItemTests {

        @Test
        @DisplayName("isItem(String) returns true when item is in registry")
        void isItem_string_returnsTrue_whenInRegistry() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                mocked.when(() -> CustomStack.isInRegistry("valid_item")).thenReturn(true);

                assertTrue(hook.isItem("valid_item"));
            }
        }

        @Test
        @DisplayName("isItem(String) returns false when item is not in registry")
        void isItem_string_returnsFalse_whenNotInRegistry() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                mocked.when(() -> CustomStack.isInRegistry("invalid_item")).thenReturn(false);

                assertFalse(hook.isItem("invalid_item"));
            }
        }

        @Test
        @DisplayName("isItem(ItemStack) returns true when CustomStack exists")
        void isItem_itemStack_returnsTrue_whenCustomStackExists() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                ItemStack itemStack = mock(ItemStack.class);
                mocked.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(mock(CustomStack.class));

                assertTrue(hook.isItem(itemStack));
            }
        }

        @Test
        @DisplayName("isItem(ItemStack) returns false when CustomStack does not exist")
        void isItem_itemStack_returnsFalse_whenNoCustomStack() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                ItemStack itemStack = mock(ItemStack.class);
                mocked.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(null);

                assertFalse(hook.isItem(itemStack));
            }
        }
    }

    @Nested
    @DisplayName("isItemOfType")
    class IsItemOfTypeTests {

        @Test
        @DisplayName("Returns true when item matches the given type name")
        void isItemOfType_returnsTrue_whenMatches() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                ItemStack itemStack = mock(ItemStack.class);
                CustomStack customStack = mock(CustomStack.class);
                mocked.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(customStack);
                when(customStack.getModelPath()).thenReturn("my_sword");

                assertTrue(hook.isItemOfType(itemStack, "my_sword"));
            }
        }

        @Test
        @DisplayName("Returns true when item matches type name case-insensitively")
        void isItemOfType_returnsTrue_caseInsensitive() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                ItemStack itemStack = mock(ItemStack.class);
                CustomStack customStack = mock(CustomStack.class);
                mocked.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(customStack);
                when(customStack.getModelPath()).thenReturn("My_Sword");

                assertTrue(hook.isItemOfType(itemStack, "my_sword"));
            }
        }

        @Test
        @DisplayName("Returns false when no CustomStack exists for the item")
        void isItemOfType_returnsFalse_whenNoCustomStack() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                ItemStack itemStack = mock(ItemStack.class);
                mocked.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(null);

                assertFalse(hook.isItemOfType(itemStack, "my_sword"));
            }
        }

        @Test
        @DisplayName("Returns false when item type does not match")
        void isItemOfType_returnsFalse_whenTypeMismatch() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                ItemStack itemStack = mock(ItemStack.class);
                CustomStack customStack = mock(CustomStack.class);
                mocked.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(customStack);
                when(customStack.getModelPath()).thenReturn("other_item");

                assertFalse(hook.isItemOfType(itemStack, "my_sword"));
            }
        }
    }

    @Nested
    @DisplayName("Block operations")
    class BlockTests {

        @Mock
        private Block block;

        @Test
        @DisplayName("isCustomBlock(Block) returns true when block is a custom block")
        void isCustomBlock_returnsTrue_whenCustomBlockExists() {
            try (MockedStatic<CustomBlock> mocked = mockStatic(CustomBlock.class)) {
                mocked.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(mock(CustomBlock.class));

                assertTrue(hook.isCustomBlock(block));
            }
        }

        @Test
        @DisplayName("isCustomBlock(Block) returns false when block is not a custom block")
        void isCustomBlock_returnsFalse_whenNotCustomBlock() {
            try (MockedStatic<CustomBlock> mocked = mockStatic(CustomBlock.class)) {
                mocked.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(null);

                assertFalse(hook.isCustomBlock(block));
            }
        }

        @Test
        @DisplayName("isCustomBlock(String) always returns false for ItemsAdder")
        void isCustomBlock_string_alwaysReturnsFalse() {
            assertFalse(hook.isCustomBlock("any_block"));
        }

        @Test
        @DisplayName("isCustomBlockOfType returns true when block matches the type")
        void isCustomBlockOfType_returnsTrue_whenMatches() {
            try (MockedStatic<CustomBlock> mocked = mockStatic(CustomBlock.class)) {
                CustomBlock customBlock = mock(CustomBlock.class);
                mocked.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(customBlock);
                when(customBlock.getModelPath()).thenReturn("my_block");

                assertTrue(hook.isCustomBlockOfType(block, "my_block"));
            }
        }

        @Test
        @DisplayName("isCustomBlockOfType returns true case-insensitively")
        void isCustomBlockOfType_returnsTrue_caseInsensitive() {
            try (MockedStatic<CustomBlock> mocked = mockStatic(CustomBlock.class)) {
                CustomBlock customBlock = mock(CustomBlock.class);
                mocked.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(customBlock);
                when(customBlock.getModelPath()).thenReturn("My_Block");

                assertTrue(hook.isCustomBlockOfType(block, "my_block"));
            }
        }

        @Test
        @DisplayName("isCustomBlockOfType returns false when block is not custom")
        void isCustomBlockOfType_returnsFalse_whenNotCustom() {
            try (MockedStatic<CustomBlock> mocked = mockStatic(CustomBlock.class)) {
                mocked.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(null);

                assertFalse(hook.isCustomBlockOfType(block, "my_block"));
            }
        }

        @Test
        @DisplayName("isCustomBlockOfType returns false when type does not match")
        void isCustomBlockOfType_returnsFalse_whenTypeMismatch() {
            try (MockedStatic<CustomBlock> mocked = mockStatic(CustomBlock.class)) {
                CustomBlock customBlock = mock(CustomBlock.class);
                mocked.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(customBlock);
                when(customBlock.getModelPath()).thenReturn("other_block");

                assertFalse(hook.isCustomBlockOfType(block, "my_block"));
            }
        }

        @Test
        @DisplayName("blockModels returns model set when custom block exists")
        void blockModels_returnsModelSet_whenCustomBlockExists() {
            try (MockedStatic<CustomBlock> mocked = mockStatic(CustomBlock.class)) {
                CustomBlock customBlock = mock(CustomBlock.class);
                mocked.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(customBlock);
                when(customBlock.getModelPath()).thenReturn("ns:my_model");

                Optional<Set<String>> result = hook.blockModels(block);

                assertTrue(result.isPresent());
                assertEquals(Set.of("ns:my_model"), result.get());
            }
        }

        @Test
        @DisplayName("blockModels returns empty when block is not custom")
        void blockModels_returnsEmpty_whenNotCustom() {
            try (MockedStatic<CustomBlock> mocked = mockStatic(CustomBlock.class)) {
                mocked.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(null);

                assertFalse(hook.blockModels(block).isPresent());
            }
        }
    }

    @Nested
    @DisplayName("placeCustomBlock")
    class PlaceCustomBlockTests {

        @Test
        @DisplayName("Throws when blockId is not a valid custom block")
        void placeCustomBlock_throwsIllegalArgument_whenNotValidBlock() {
            Location location = mock(Location.class);

            assertThrows(IllegalArgumentException.class,
                () -> hook.placeCustomBlock(location, "invalid_block"));
        }
    }

    @Nested
    @DisplayName("drops")
    class DropsTests {

        @Mock
        private Block block;

        @Test
        @DisplayName("Returns custom block drops when block is a custom block")
        void drops_returnsCustomDrops_whenCustomBlock() {
            try (MockedStatic<CustomBlock> mocked = mockStatic(CustomBlock.class)) {
                ItemStack tool = mock(ItemStack.class);
                Entity entity = mock(Entity.class);
                List<ItemStack> expectedDrops = List.of(mock(ItemStack.class));

                mocked.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(mock(CustomBlock.class));
                mocked.when(() -> CustomBlock.getLoot(block, tool, true)).thenReturn(expectedDrops);

                List<ItemStack> result = hook.drops(block, tool, entity);

                assertEquals(expectedDrops, result);
            }
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Returns vanilla block drops when block is not custom")
        void drops_returnsVanillaDrops_whenNotCustomBlock() {
            try (MockedStatic<CustomBlock> mocked = mockStatic(CustomBlock.class)) {
                ItemStack tool = mock(ItemStack.class);
                Entity entity = mock(Entity.class);
                ItemStack dropItem = mock(ItemStack.class);
                Collection<ItemStack> drops = List.of(dropItem);

                mocked.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(null);
                when(block.getDrops(tool, entity)).thenReturn(drops);

                List<ItemStack> result = hook.drops(block, tool, entity);

                assertEquals(1, result.size());
                assertTrue(result.contains(dropItem));
            }
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Returns vanilla block drops when entity is null")
        void drops_returnsVanillaDrops_whenEntityIsNull() {
            try (MockedStatic<CustomBlock> mocked = mockStatic(CustomBlock.class)) {
                ItemStack tool = mock(ItemStack.class);
                ItemStack dropItem = mock(ItemStack.class);
                Collection<ItemStack> drops = List.of(dropItem);

                mocked.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(null);
                when(block.getDrops(tool, null)).thenReturn(drops);

                List<ItemStack> result = hook.drops(block, tool, null);

                assertEquals(1, result.size());
                assertTrue(result.contains(dropItem));
            }
        }
    }

    @Nested
    @DisplayName("playBlockDropEffects")
    class PlayBlockDropEffectsTests {

        @Mock
        private Block block;

        @Test
        @DisplayName("Plays custom block effects when block is custom")
        void playBlockDropEffects_playsCustomEffects_whenCustomBlock() {
            try (MockedStatic<CustomBlock> mocked = mockStatic(CustomBlock.class)) {
                CustomBlock customBlock = mock(CustomBlock.class);
                mocked.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(customBlock);

                hook.playBlockDropEffects(block);

                verify(customBlock).playBreakEffect();
                verify(customBlock).playBreakSound();
                verify(customBlock).playBreakParticles();
            }
        }

        @Test
        @DisplayName("Plays vanilla block effects when block is not custom")
        void playBlockDropEffects_playsVanillaEffects_whenNotCustomBlock() {
            try (MockedStatic<CustomBlock> mocked = mockStatic(CustomBlock.class)) {
                mocked.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(null);

                World world = mock(World.class);
                BlockData blockData = mock(BlockData.class);
                SoundGroup soundGroup = mock(SoundGroup.class);
                Location location = mock(Location.class);

                when(block.getWorld()).thenReturn(world);
                when(block.getBlockData()).thenReturn(blockData);
                when(block.getBlockSoundGroup()).thenReturn(soundGroup);
                when(block.getLocation()).thenReturn(location);
                when(location.clone()).thenReturn(location);
                when(location.add(0.5, 0.5, 0.5)).thenReturn(location);
                when(soundGroup.getBreakSound()).thenReturn(org.bukkit.Sound.BLOCK_STONE_BREAK);
                when(soundGroup.getVolume()).thenReturn(1.0f);
                when(soundGroup.getPitch()).thenReturn(1.0f);

                hook.playBlockDropEffects(block);

                verify(world).playSound(eq(location), eq(org.bukkit.Sound.BLOCK_STONE_BREAK), eq(1.0f), eq(1.0f));
                verify(world).spawnParticle(eq(Particle.BLOCK), eq(location), eq(20),
                    eq(0.25), eq(0.25), eq(0.25), eq(0.05), eq(blockData));
            }
        }
    }

    @Nested
    @DisplayName("removeBlock")
    class RemoveBlockTests {

        @Mock
        private Block block;

        @Test
        @DisplayName("Removes custom block when it is a custom block and removal succeeds")
        void removeBlock_removesCustomBlock_whenSuccessful() {
            try (MockedStatic<CustomBlock> mocked = mockStatic(CustomBlock.class)) {
                CustomBlock customBlock = mock(CustomBlock.class);
                mocked.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(customBlock);
                when(customBlock.remove()).thenReturn(true);

                hook.removeBlock(block);

                verify(customBlock).remove();
            }
        }

        @Test
        @DisplayName("Throws when custom block removal fails")
        void removeBlock_throwsIllegalState_whenRemovalFails() {
            try (MockedStatic<CustomBlock> mocked = mockStatic(CustomBlock.class)) {
                CustomBlock customBlock = mock(CustomBlock.class);
                mocked.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(customBlock);
                when(customBlock.remove()).thenReturn(false);
                when(customBlock.getModelPath()).thenReturn("ns:failing_block");
                Location loc = mock(Location.class);
                when(block.getLocation()).thenReturn(loc);

                assertThrows(IllegalStateException.class, () -> hook.removeBlock(block));
            }
        }

        @Test
        @DisplayName("Sets block to AIR when it is not a custom block")
        void removeBlock_setsAir_whenNotCustomBlock() {
            try (MockedStatic<CustomBlock> mocked = mockStatic(CustomBlock.class)) {
                mocked.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(null);

                hook.removeBlock(block);

                verify(block).setType(Material.AIR);
            }
        }
    }

    @Nested
    @DisplayName("itemName")
    class ItemNameTests {

        @Test
        @DisplayName("Returns Adventure component name when available")
        void itemName_returnsComponentName_whenAvailable() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                CustomStack customStack = mock(CustomStack.class);
                mocked.when(() -> CustomStack.getInstance("ns:my_item")).thenReturn(customStack);
                when(customStack.itemName()).thenReturn(Component.text("Fancy Sword"));

                CustomItemWrapper wrapper = new TestCustomItemWrapper("ns:my_item");
                String result = hook.itemName(wrapper);

                assertEquals("Fancy Sword", result);
            }
        }

        @Test
        @DisplayName("Falls back to legacy display name when component name is null")
        void itemName_returnsLegacyName_whenComponentNull() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                CustomStack customStack = mock(CustomStack.class);
                mocked.when(() -> CustomStack.getInstance("ns:my_item")).thenReturn(customStack);
                when(customStack.itemName()).thenReturn(null);
                when(customStack.getDisplayName()).thenReturn("§6Golden Blade");

                CustomItemWrapper wrapper = new TestCustomItemWrapper("ns:my_item");
                String result = hook.itemName(wrapper);

                assertEquals("Golden Blade", result);
            }
        }

        @Test
        @DisplayName("Falls back to legacy display name when component name is empty")
        void itemName_returnsLegacyName_whenComponentEmpty() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                CustomStack customStack = mock(CustomStack.class);
                mocked.when(() -> CustomStack.getInstance("ns:my_item")).thenReturn(customStack);
                when(customStack.itemName()).thenReturn(Component.empty());
                when(customStack.getDisplayName()).thenReturn("Legacy Name");

                CustomItemWrapper wrapper = new TestCustomItemWrapper("ns:my_item");
                String result = hook.itemName(wrapper);

                assertEquals("Legacy Name", result);
            }
        }

        @Test
        @DisplayName("Falls back to formatted block ID when both names are unavailable")
        void itemName_returnsFormattedId_whenBothNamesUnavailable() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                CustomStack customStack = mock(CustomStack.class);
                mocked.when(() -> CustomStack.getInstance("ns:cool_item")).thenReturn(customStack);
                when(customStack.itemName()).thenReturn(null);
                when(customStack.getDisplayName()).thenReturn(null);

                CustomItemWrapper wrapper = new TestCustomItemWrapper("ns:cool_item");
                String result = hook.itemName(wrapper);

                assertEquals("Cool Item", result);
            }
        }

        @Test
        @DisplayName("Falls back to formatted block ID when legacy name is empty")
        void itemName_returnsFormattedId_whenLegacyNameEmpty() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                CustomStack customStack = mock(CustomStack.class);
                mocked.when(() -> CustomStack.getInstance("ns:cool_item")).thenReturn(customStack);
                when(customStack.itemName()).thenReturn(null);
                when(customStack.getDisplayName()).thenReturn("");

                CustomItemWrapper wrapper = new TestCustomItemWrapper("ns:cool_item");
                String result = hook.itemName(wrapper);

                assertEquals("Cool Item", result);
            }
        }

        @Test
        @DisplayName("Falls back to formatted block ID when CustomStack is null")
        void itemName_returnsFormattedId_whenCustomStackNull() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                mocked.when(() -> CustomStack.getInstance("ns:cool_item")).thenReturn(null);

                CustomItemWrapper wrapper = new TestCustomItemWrapper("ns:cool_item");
                String result = hook.itemName(wrapper);

                assertEquals("Cool Item", result);
            }
        }

        @Test
        @DisplayName("Returns formatted material name when wrapper has no custom item")
        void itemName_returnsFormattedMaterial_whenVanilla() {
            CustomItemWrapper wrapper = new TestVanillaItemWrapper(Material.OAK_LOG);
            String result = hook.itemName(wrapper);

            assertEquals("Oak Log", result);
        }
    }

    @Nested
    @DisplayName("blockName")
    class BlockNameTests {

        @Test
        @DisplayName("Returns Adventure component name when available")
        void blockName_returnsComponentName_whenAvailable() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                CustomStack customStack = mock(CustomStack.class);
                mocked.when(() -> CustomStack.getInstance("ns:my_block")).thenReturn(customStack);
                when(customStack.itemName()).thenReturn(Component.text("Fancy Block"));

                CustomBlockWrapper wrapper = new TestCustomBlockWrapper("ns:my_block");
                String result = hook.blockName(wrapper);

                assertEquals("Fancy Block", result);
            }
        }

        @Test
        @DisplayName("Falls back to legacy display name when component name is null")
        void blockName_returnsLegacyName_whenComponentNull() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                CustomStack customStack = mock(CustomStack.class);
                mocked.when(() -> CustomStack.getInstance("ns:my_block")).thenReturn(customStack);
                when(customStack.itemName()).thenReturn(null);
                when(customStack.getDisplayName()).thenReturn("§aGreen Block");

                CustomBlockWrapper wrapper = new TestCustomBlockWrapper("ns:my_block");
                String result = hook.blockName(wrapper);

                assertEquals("Green Block", result);
            }
        }

        @Test
        @DisplayName("Falls back to legacy display name when component name is empty")
        void blockName_returnsLegacyName_whenComponentEmpty() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                CustomStack customStack = mock(CustomStack.class);
                mocked.when(() -> CustomStack.getInstance("ns:my_block")).thenReturn(customStack);
                when(customStack.itemName()).thenReturn(Component.empty());
                when(customStack.getDisplayName()).thenReturn("Legacy Block");

                CustomBlockWrapper wrapper = new TestCustomBlockWrapper("ns:my_block");
                String result = hook.blockName(wrapper);

                assertEquals("Legacy Block", result);
            }
        }

        @Test
        @DisplayName("Falls back to formatted block ID when legacy name is empty")
        void blockName_returnsFormattedId_whenLegacyNameEmpty() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                CustomStack customStack = mock(CustomStack.class);
                mocked.when(() -> CustomStack.getInstance("ns:cool_block")).thenReturn(customStack);
                when(customStack.itemName()).thenReturn(null);
                when(customStack.getDisplayName()).thenReturn("");

                CustomBlockWrapper wrapper = new TestCustomBlockWrapper("ns:cool_block");
                String result = hook.blockName(wrapper);

                assertEquals("Cool Block", result);
            }
        }

        @Test
        @DisplayName("Falls back to formatted block ID when both names are unavailable")
        void blockName_returnsFormattedId_whenBothNamesUnavailable() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                CustomStack customStack = mock(CustomStack.class);
                mocked.when(() -> CustomStack.getInstance("ns:cool_block")).thenReturn(customStack);
                when(customStack.itemName()).thenReturn(null);
                when(customStack.getDisplayName()).thenReturn(null);

                CustomBlockWrapper wrapper = new TestCustomBlockWrapper("ns:cool_block");
                String result = hook.blockName(wrapper);

                assertEquals("Cool Block", result);
            }
        }

        @Test
        @DisplayName("Falls back to formatted block ID when CustomStack is null")
        void blockName_returnsFormattedId_whenCustomStackNull() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                mocked.when(() -> CustomStack.getInstance("ns:cool_block")).thenReturn(null);

                CustomBlockWrapper wrapper = new TestCustomBlockWrapper("ns:cool_block");
                String result = hook.blockName(wrapper);

                assertEquals("Cool Block", result);
            }
        }

        @Test
        @DisplayName("Returns formatted material name when wrapper has no custom block")
        void blockName_returnsFormattedMaterial_whenVanilla() {
            CustomBlockWrapper wrapper = new TestVanillaBlockWrapper(Material.STONE_BRICKS);
            String result = hook.blockName(wrapper);

            assertEquals("Stone Bricks", result);
        }

        @Test
        @DisplayName("Formats block ID without namespace correctly")
        void blockName_formatsIdWithoutNamespace() {
            try (MockedStatic<CustomStack> mocked = mockStatic(CustomStack.class)) {
                mocked.when(() -> CustomStack.getInstance("simple_block")).thenReturn(null);

                CustomBlockWrapper wrapper = new TestCustomBlockWrapper("simple_block");
                String result = hook.blockName(wrapper);

                assertEquals("Simple Block", result);
            }
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

        @NotNull
        @Override
        public Optional<Material> material() {
            return Optional.empty();
        }
    }

    private static class TestVanillaItemWrapper extends CustomItemWrapper {

        private final Material testMaterial;

        TestVanillaItemWrapper(@NotNull Material material) {
            super(material);
            this.testMaterial = material;
        }

        @NotNull
        @Override
        public Optional<String> customItem() {
            return Optional.empty();
        }

        @NotNull
        @Override
        public Optional<Material> material() {
            return Optional.of(testMaterial);
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

        @NotNull
        @Override
        public Optional<Material> material() {
            return Optional.empty();
        }
    }

    private static class TestVanillaBlockWrapper extends CustomBlockWrapper {

        private final Material testMaterial;

        TestVanillaBlockWrapper(@NotNull Material material) {
            super(material);
            this.testMaterial = material;
        }

        @NotNull
        @Override
        public Optional<String> customBlock() {
            return Optional.empty();
        }

        @NotNull
        @Override
        public Optional<Material> material() {
            return Optional.of(testMaterial);
        }
    }
}

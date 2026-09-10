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
import org.junit.jupiter.api.AfterEach;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyFloat;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoreItemsAdderHookTest {

    @Mock
    private CorePlugin plugin;

    private MockedStatic<CustomStack> customStackStatic;
    private MockedStatic<CustomBlock> customBlockStatic;
    private CoreItemsAdderHook hook;

    @BeforeEach
    void setUp() {
        customStackStatic = mockStatic(CustomStack.class);
        customBlockStatic = mockStatic(CustomBlock.class);
        hook = new CoreItemsAdderHook(plugin);
    }

    @AfterEach
    void tearDown() {
        customBlockStatic.close();
        customStackStatic.close();
    }

    @Test
    @DisplayName("Given a CoreItemsAdderHook, when constructed, then plugin is accessible")
    void constructor_storesPlugin() {
        assertNotNull(hook.plugin());
    }

    @Nested
    @DisplayName("item(String)")
    class ItemByName {

        @Test
        @DisplayName("Given a valid ItemsAdder item, when item is called, then returns the ItemStack")
        void returnsItemStack_whenCustomStackExists() {
            ItemStack itemStack = mock(ItemStack.class);
            CustomStack customStack = mock(CustomStack.class);
            when(customStack.getItemStack()).thenReturn(itemStack);
            customStackStatic.when(() -> CustomStack.getInstance("my_item")).thenReturn(customStack);

            Optional<ItemStack> result = hook.item("my_item");
            assertTrue(result.isPresent());
            assertEquals(itemStack, result.get());
        }

        @Test
        @DisplayName("Given an invalid item name, when item is called, then returns empty")
        void returnsEmpty_whenCustomStackDoesNotExist() {
            customStackStatic.when(() -> CustomStack.getInstance("nonexistent")).thenReturn(null);

            Optional<ItemStack> result = hook.item("nonexistent");
            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("itemModels(ItemStack)")
    class ItemModels {

        @Test
        @DisplayName("Given a custom ItemStack, when itemModels is called, then returns model path set")
        void returnsModelPath_whenItemIsCustom() {
            ItemStack itemStack = mock(ItemStack.class);
            CustomStack customStack = mock(CustomStack.class);
            when(customStack.getModelPath()).thenReturn("my_namespace:cool_sword");
            customStackStatic.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(customStack);

            Optional<Set<String>> result = hook.itemModels(itemStack);
            assertTrue(result.isPresent());
            assertEquals(Set.of("my_namespace:cool_sword"), result.get());
        }

        @Test
        @DisplayName("Given a vanilla ItemStack, when itemModels is called, then returns empty")
        void returnsEmpty_whenItemIsNotCustom() {
            ItemStack itemStack = mock(ItemStack.class);
            customStackStatic.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(null);

            Optional<Set<String>> result = hook.itemModels(itemStack);
            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("isItem(String)")
    class IsItemByName {

        @Test
        @DisplayName("Given a registered item, when isItem is called, then returns true")
        void returnsTrue_whenItemIsInRegistry() {
            customStackStatic.when(() -> CustomStack.isInRegistry("my_item")).thenReturn(true);
            assertTrue(hook.isItem("my_item"));
        }

        @Test
        @DisplayName("Given an unregistered item, when isItem is called, then returns false")
        void returnsFalse_whenItemIsNotInRegistry() {
            customStackStatic.when(() -> CustomStack.isInRegistry("nonexistent")).thenReturn(false);
            assertFalse(hook.isItem("nonexistent"));
        }
    }

    @Nested
    @DisplayName("isItem(ItemStack)")
    class IsItemByStack {

        @Test
        @DisplayName("Given a custom ItemStack, when isItem is called, then returns true")
        void returnsTrue_whenItemStackIsCustom() {
            ItemStack itemStack = mock(ItemStack.class);
            customStackStatic.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(mock(CustomStack.class));
            assertTrue(hook.isItem(itemStack));
        }

        @Test
        @DisplayName("Given a vanilla ItemStack, when isItem is called, then returns false")
        void returnsFalse_whenItemStackIsNotCustom() {
            ItemStack itemStack = mock(ItemStack.class);
            customStackStatic.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(null);
            assertFalse(hook.isItem(itemStack));
        }
    }

    @Nested
    @DisplayName("isItemOfType(ItemStack, String)")
    class IsItemOfType {

        @Test
        @DisplayName("Given a custom ItemStack matching the type, when isItemOfType is called, then returns true")
        void returnsTrue_whenItemStackMatchesType() {
            ItemStack itemStack = mock(ItemStack.class);
            CustomStack customStack = mock(CustomStack.class);
            when(customStack.getModelPath()).thenReturn("my_sword");
            customStackStatic.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(customStack);

            assertTrue(hook.isItemOfType(itemStack, "MY_SWORD"));
        }

        @Test
        @DisplayName("Given a custom ItemStack not matching the type, when isItemOfType is called, then returns false")
        void returnsFalse_whenItemStackDoesNotMatchType() {
            ItemStack itemStack = mock(ItemStack.class);
            CustomStack customStack = mock(CustomStack.class);
            when(customStack.getModelPath()).thenReturn("my_sword");
            customStackStatic.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(customStack);

            assertFalse(hook.isItemOfType(itemStack, "my_axe"));
        }

        @Test
        @DisplayName("Given a vanilla ItemStack, when isItemOfType is called, then returns false")
        void returnsFalse_whenItemStackIsNotCustom() {
            ItemStack itemStack = mock(ItemStack.class);
            customStackStatic.when(() -> CustomStack.byItemStack(itemStack)).thenReturn(null);

            assertFalse(hook.isItemOfType(itemStack, "my_sword"));
        }
    }

    @Nested
    @DisplayName("isCustomBlock(Block)")
    class IsCustomBlockByBlock {

        @Test
        @DisplayName("Given a custom block, when isCustomBlock is called, then returns true")
        void returnsTrue_whenBlockIsCustom() {
            Block block = mock(Block.class);
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(mock(CustomBlock.class));
            assertTrue(hook.isCustomBlock(block));
        }

        @Test
        @DisplayName("Given a vanilla block, when isCustomBlock is called, then returns false")
        void returnsFalse_whenBlockIsNotCustom() {
            Block block = mock(Block.class);
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(null);
            assertFalse(hook.isCustomBlock(block));
        }
    }

    @Nested
    @DisplayName("isCustomBlock(String)")
    class IsCustomBlockByName {

        @Test
        @DisplayName("Given any string, when isCustomBlock is called, then always returns false")
        void alwaysReturnsFalse() {
            assertFalse(hook.isCustomBlock("any_block"));
        }
    }

    @Nested
    @DisplayName("isCustomBlockOfType(Block, String)")
    class IsCustomBlockOfType {

        @Test
        @DisplayName("Given a custom block matching the type, when isCustomBlockOfType is called, then returns true")
        void returnsTrue_whenBlockMatchesType() {
            Block block = mock(Block.class);
            CustomBlock customBlock = mock(CustomBlock.class);
            when(customBlock.getModelPath()).thenReturn("custom_ore");
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(customBlock);

            assertTrue(hook.isCustomBlockOfType(block, "CUSTOM_ORE"));
        }

        @Test
        @DisplayName("Given a custom block not matching the type, when isCustomBlockOfType is called, then returns false")
        void returnsFalse_whenBlockDoesNotMatchType() {
            Block block = mock(Block.class);
            CustomBlock customBlock = mock(CustomBlock.class);
            when(customBlock.getModelPath()).thenReturn("custom_ore");
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(customBlock);

            assertFalse(hook.isCustomBlockOfType(block, "other_block"));
        }

        @Test
        @DisplayName("Given a vanilla block, when isCustomBlockOfType is called, then returns false")
        void returnsFalse_whenBlockIsNotCustom() {
            Block block = mock(Block.class);
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(null);

            assertFalse(hook.isCustomBlockOfType(block, "custom_ore"));
        }
    }

    @Nested
    @DisplayName("blockModels(Block)")
    class BlockModels {

        @Test
        @DisplayName("Given a custom block, when blockModels is called, then returns model path set")
        void returnsModelPath_whenBlockIsCustom() {
            Block block = mock(Block.class);
            CustomBlock customBlock = mock(CustomBlock.class);
            when(customBlock.getModelPath()).thenReturn("my_ore");
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(customBlock);

            Optional<Set<String>> result = hook.blockModels(block);
            assertTrue(result.isPresent());
            assertEquals(Set.of("my_ore"), result.get());
        }

        @Test
        @DisplayName("Given a vanilla block, when blockModels is called, then returns empty")
        void returnsEmpty_whenBlockIsNotCustom() {
            Block block = mock(Block.class);
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(null);

            Optional<Set<String>> result = hook.blockModels(block);
            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("placeCustomBlock(Location, String)")
    class PlaceCustomBlock {

        @Test
        @DisplayName("Given an invalid block id, when placeCustomBlock is called, then throws IllegalArgumentException")
        void throwsException_whenBlockIdInvalid() {
            Location location = mock(Location.class);
            assertThrows(IllegalArgumentException.class, () -> hook.placeCustomBlock(location, "invalid_block"));
        }
    }

    @Nested
    @DisplayName("drops(Block, ItemStack, Entity)")
    class Drops {

        @Test
        @DisplayName("Given a custom block, when drops is called, then returns custom loot")
        void returnsCustomLoot_whenBlockIsCustom() {
            Block block = mock(Block.class);
            ItemStack tool = mock(ItemStack.class);
            Entity entity = mock(Entity.class);
            List<ItemStack> expectedDrops = List.of(mock(ItemStack.class));

            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(mock(CustomBlock.class));
            customBlockStatic.when(() -> CustomBlock.getLoot(block, tool, true)).thenReturn(expectedDrops);

            List<ItemStack> result = hook.drops(block, tool, entity);
            assertEquals(expectedDrops, result);
        }

        @Test
        @DisplayName("Given a vanilla block, when drops is called, then returns vanilla drops")
        void returnsVanillaDrops_whenBlockIsNotCustom() {
            Block block = mock(Block.class);
            ItemStack tool = mock(ItemStack.class);
            Entity entity = mock(Entity.class);
            Collection<ItemStack> vanillaDrops = List.of(mock(ItemStack.class));

            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(null);
            when(block.getDrops(tool, entity)).thenReturn(vanillaDrops);

            List<ItemStack> result = hook.drops(block, tool, entity);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Given a vanilla block and null entity, when drops is called, then returns vanilla drops")
        void returnsVanillaDrops_whenEntityIsNull() {
            Block block = mock(Block.class);
            ItemStack tool = mock(ItemStack.class);
            Collection<ItemStack> vanillaDrops = List.of(mock(ItemStack.class));

            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(null);
            when(block.getDrops(tool, null)).thenReturn(vanillaDrops);

            List<ItemStack> result = hook.drops(block, tool, null);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("playBlockDropEffects(Block)")
    class PlayBlockDropEffects {

        @Test
        @DisplayName("Given a custom block, when playBlockDropEffects is called, then plays custom effects")
        void playsCustomEffects_whenBlockIsCustom() {
            Block block = mock(Block.class);
            CustomBlock customBlock = mock(CustomBlock.class);
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(customBlock);

            hook.playBlockDropEffects(block);

            verify(customBlock).playBreakEffect();
            verify(customBlock).playBreakSound();
            verify(customBlock).playBreakParticles();
        }

        @Test
        @DisplayName("Given a vanilla block, when playBlockDropEffects is called, then plays vanilla sound and particle effects")
        void playsVanillaEffects_whenBlockIsNotCustom() {
            Block block = mock(Block.class);
            World world = mock(World.class);
            Location location = mock(Location.class);
            BlockData blockData = mock(BlockData.class);
            SoundGroup soundGroup = mock(SoundGroup.class);
            org.bukkit.Sound breakSound = org.bukkit.Sound.BLOCK_STONE_BREAK;

            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(null);
            when(block.getWorld()).thenReturn(world);
            when(block.getLocation()).thenReturn(location);
            when(location.clone()).thenReturn(location);
            when(location.add(0.5, 0.5, 0.5)).thenReturn(location);
            when(block.getBlockData()).thenReturn(blockData);
            when(block.getBlockSoundGroup()).thenReturn(soundGroup);
            when(soundGroup.getBreakSound()).thenReturn(breakSound);
            when(soundGroup.getVolume()).thenReturn(1.0f);
            when(soundGroup.getPitch()).thenReturn(1.0f);

            hook.playBlockDropEffects(block);

            verify(world).playSound(location, breakSound, 1.0f, 1.0f);
            verify(world).spawnParticle(eq(Particle.BLOCK), eq(location), eq(20),
                    eq(0.25), eq(0.25), eq(0.25), eq(0.05), eq(blockData));
        }
    }

    @Nested
    @DisplayName("removeBlock(Block)")
    class RemoveBlock {

        @Test
        @DisplayName("Given a custom block that removes successfully, when removeBlock is called, then succeeds")
        void removesCustomBlock_whenRemoveSucceeds() {
            Block block = mock(Block.class);
            CustomBlock customBlock = mock(CustomBlock.class);
            when(customBlock.remove()).thenReturn(true);
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(customBlock);

            hook.removeBlock(block);
            verify(customBlock).remove();
        }

        @Test
        @DisplayName("Given a custom block that fails to remove, when removeBlock is called, then throws IllegalStateException")
        void throwsException_whenCustomBlockRemoveFails() {
            Block block = mock(Block.class);
            CustomBlock customBlock = mock(CustomBlock.class);
            when(customBlock.remove()).thenReturn(false);
            when(customBlock.getModelPath()).thenReturn("custom_ore");
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(customBlock);

            assertThrows(IllegalStateException.class, () -> hook.removeBlock(block));
        }

        @Test
        @DisplayName("Given a vanilla block, when removeBlock is called, then sets type to AIR")
        void setsToAir_whenBlockIsVanilla() {
            Block block = mock(Block.class);
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(block)).thenReturn(null);

            hook.removeBlock(block);
            verify(block).setType(Material.AIR);
        }
    }

    @Nested
    @DisplayName("itemName(CustomItemWrapper)")
    class ItemName {

        @Test
        @DisplayName("Given a custom item with Adventure Component name, when itemName is called, then returns serialized name")
        void returnsComponentName_whenItemNameComponentPresent() {
            CustomItemWrapper wrapper = mock(CustomItemWrapper.class);
            when(wrapper.customItem()).thenReturn(Optional.of("my_namespace:cool_sword"));

            CustomStack customStack = mock(CustomStack.class);
            when(customStack.itemName()).thenReturn(Component.text("Cool Sword"));
            customStackStatic.when(() -> CustomStack.getInstance("my_namespace:cool_sword")).thenReturn(customStack);

            assertEquals("Cool Sword", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a custom item with empty Component but valid display name, when itemName is called, then returns stripped display name")
        void returnsStrippedDisplayName_whenComponentEmptyButDisplayNamePresent() {
            CustomItemWrapper wrapper = mock(CustomItemWrapper.class);
            when(wrapper.customItem()).thenReturn(Optional.of("my_item"));

            CustomStack customStack = mock(CustomStack.class);
            when(customStack.itemName()).thenReturn(Component.empty());
            when(customStack.getDisplayName()).thenReturn("§6Golden Sword");
            customStackStatic.when(() -> CustomStack.getInstance("my_item")).thenReturn(customStack);

            assertEquals("Golden Sword", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a custom item with null Component and null display name, when itemName is called, then returns formatted block id")
        void returnsFormattedId_whenNoNameAvailable() {
            CustomItemWrapper wrapper = mock(CustomItemWrapper.class);
            when(wrapper.customItem()).thenReturn(Optional.of("my_namespace:cool_sword"));

            CustomStack customStack = mock(CustomStack.class);
            when(customStack.itemName()).thenReturn(null);
            when(customStack.getDisplayName()).thenReturn(null);
            customStackStatic.when(() -> CustomStack.getInstance("my_namespace:cool_sword")).thenReturn(customStack);

            assertEquals("Cool Sword", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a custom item with empty display name, when itemName is called, then returns formatted block id")
        void returnsFormattedId_whenDisplayNameEmpty() {
            CustomItemWrapper wrapper = mock(CustomItemWrapper.class);
            when(wrapper.customItem()).thenReturn(Optional.of("cool_item"));

            CustomStack customStack = mock(CustomStack.class);
            when(customStack.itemName()).thenReturn(null);
            when(customStack.getDisplayName()).thenReturn("");
            customStackStatic.when(() -> CustomStack.getInstance("cool_item")).thenReturn(customStack);

            assertEquals("Cool Item", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a custom item ID not in registry, when itemName is called, then returns formatted id")
        void returnsFormattedId_whenItemNotInRegistry() {
            CustomItemWrapper wrapper = mock(CustomItemWrapper.class);
            when(wrapper.customItem()).thenReturn(Optional.of("my_namespace:missing_item"));

            customStackStatic.when(() -> CustomStack.getInstance("my_namespace:missing_item")).thenReturn(null);

            assertEquals("Missing Item", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a vanilla material wrapper, when itemName is called, then returns formatted material name")
        void returnsFormattedMaterial_whenNoCustomItem() {
            CustomItemWrapper wrapper = mock(CustomItemWrapper.class);
            when(wrapper.customItem()).thenReturn(Optional.empty());
            when(wrapper.material()).thenReturn(Optional.of(Material.OAK_LOG));

            assertEquals("Oak Log", hook.itemName(wrapper));
        }
    }

    @Nested
    @DisplayName("blockName(CustomBlockWrapper)")
    class BlockName {

        @Test
        @DisplayName("Given a custom block with Adventure Component name, when blockName is called, then returns serialized name")
        void returnsComponentName_whenBlockNameComponentPresent() {
            CustomBlockWrapper wrapper = mock(CustomBlockWrapper.class);
            when(wrapper.customBlock()).thenReturn(Optional.of("my_namespace:custom_ore"));

            CustomStack customStack = mock(CustomStack.class);
            when(customStack.itemName()).thenReturn(Component.text("Custom Ore"));
            customStackStatic.when(() -> CustomStack.getInstance("my_namespace:custom_ore")).thenReturn(customStack);

            assertEquals("Custom Ore", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a custom block with legacy display name, when blockName is called, then returns stripped name")
        void returnsStrippedDisplayName_whenFallingBackToLegacy() {
            CustomBlockWrapper wrapper = mock(CustomBlockWrapper.class);
            when(wrapper.customBlock()).thenReturn(Optional.of("my_ore"));

            CustomStack customStack = mock(CustomStack.class);
            when(customStack.itemName()).thenReturn(null);
            when(customStack.getDisplayName()).thenReturn("§aMagic Ore");
            customStackStatic.when(() -> CustomStack.getInstance("my_ore")).thenReturn(customStack);

            assertEquals("Magic Ore", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a custom block not in registry, when blockName is called, then returns formatted block id")
        void returnsFormattedId_whenBlockNotInRegistry() {
            CustomBlockWrapper wrapper = mock(CustomBlockWrapper.class);
            when(wrapper.customBlock()).thenReturn(Optional.of("my_namespace:unknown_block"));

            customStackStatic.when(() -> CustomStack.getInstance("my_namespace:unknown_block")).thenReturn(null);

            assertEquals("Unknown Block", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a custom block with empty component name and empty display name, when blockName is called, then returns formatted id")
        void returnsFormattedId_whenBothNamesEmpty() {
            CustomBlockWrapper wrapper = mock(CustomBlockWrapper.class);
            when(wrapper.customBlock()).thenReturn(Optional.of("fancy_block"));

            CustomStack customStack = mock(CustomStack.class);
            when(customStack.itemName()).thenReturn(Component.empty());
            when(customStack.getDisplayName()).thenReturn("");
            customStackStatic.when(() -> CustomStack.getInstance("fancy_block")).thenReturn(customStack);

            assertEquals("Fancy Block", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a custom block with null itemName and null displayName, when blockName is called, then returns formatted id")
        void returnsFormattedId_whenBothNamesNull() {
            CustomBlockWrapper wrapper = mock(CustomBlockWrapper.class);
            when(wrapper.customBlock()).thenReturn(Optional.of("my_namespace:rare_ore"));

            CustomStack customStack = mock(CustomStack.class);
            when(customStack.itemName()).thenReturn(null);
            when(customStack.getDisplayName()).thenReturn(null);
            customStackStatic.when(() -> CustomStack.getInstance("my_namespace:rare_ore")).thenReturn(customStack);

            assertEquals("Rare Ore", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a block id without namespace, when blockName is called, then formats correctly")
        void returnsFormattedName_whenIdHasNoNamespace() {
            CustomBlockWrapper wrapper = mock(CustomBlockWrapper.class);
            when(wrapper.customBlock()).thenReturn(Optional.of("simple_block"));

            customStackStatic.when(() -> CustomStack.getInstance("simple_block")).thenReturn(null);

            assertEquals("Simple Block", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a vanilla material wrapper, when blockName is called, then returns formatted material name")
        void returnsFormattedMaterial_whenNoCustomBlock() {
            CustomBlockWrapper wrapper = mock(CustomBlockWrapper.class);
            when(wrapper.customBlock()).thenReturn(Optional.empty());
            when(wrapper.material()).thenReturn(Optional.of(Material.DIAMOND_ORE));

            assertEquals("Diamond Ore", hook.blockName(wrapper));
        }
    }
}

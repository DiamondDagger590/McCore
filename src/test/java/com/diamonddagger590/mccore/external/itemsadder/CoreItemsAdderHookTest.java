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
import org.mockito.MockedStatic;

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

class CoreItemsAdderHookTest {

    private CoreItemsAdderHook hook;
    private MockedStatic<CustomStack> customStackStatic;
    private MockedStatic<CustomBlock> customBlockStatic;

    @BeforeEach
    void setUp() {
        CorePlugin mockPlugin = mock(CorePlugin.class);
        hook = new CoreItemsAdderHook(mockPlugin);
        customStackStatic = mockStatic(CustomStack.class);
        customBlockStatic = mockStatic(CustomBlock.class);
    }

    @AfterEach
    void tearDown() {
        customBlockStatic.close();
        customStackStatic.close();
    }

    @Nested
    @DisplayName("item")
    class ItemTests {

        @Test
        @DisplayName("Given a valid ItemsAdder item ID, when item is called, then returns the ItemStack")
        void item_returnsItemStack_whenCustomStackExists() {
            CustomStack mockStack = mock(CustomStack.class);
            ItemStack expectedStack = mock(ItemStack.class);
            when(mockStack.getItemStack()).thenReturn(expectedStack);
            customStackStatic.when(() -> CustomStack.getInstance("ia_sword")).thenReturn(mockStack);

            Optional<ItemStack> result = hook.item("ia_sword");

            assertTrue(result.isPresent());
            assertEquals(expectedStack, result.get());
        }

        @Test
        @DisplayName("Given an invalid ItemsAdder item ID, when item is called, then returns empty")
        void item_returnsEmpty_whenCustomStackDoesNotExist() {
            customStackStatic.when(() -> CustomStack.getInstance("nonexistent")).thenReturn(null);

            Optional<ItemStack> result = hook.item("nonexistent");

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("itemModels")
    class ItemModelsTests {

        @Test
        @DisplayName("Given an ItemsAdder ItemStack, when itemModels is called, then returns the model path")
        void itemModels_returnsModelPath_whenCustomStackExists() {
            ItemStack mockItemStack = mock(ItemStack.class);
            CustomStack mockStack = mock(CustomStack.class);
            when(mockStack.getModelPath()).thenReturn("ia:sword_model");
            customStackStatic.when(() -> CustomStack.byItemStack(mockItemStack)).thenReturn(mockStack);

            Optional<Set<String>> result = hook.itemModels(mockItemStack);

            assertTrue(result.isPresent());
            assertEquals(Set.of("ia:sword_model"), result.get());
        }

        @Test
        @DisplayName("Given a non-ItemsAdder ItemStack, when itemModels is called, then returns empty")
        void itemModels_returnsEmpty_whenNotCustomStack() {
            ItemStack mockItemStack = mock(ItemStack.class);
            customStackStatic.when(() -> CustomStack.byItemStack(mockItemStack)).thenReturn(null);

            Optional<Set<String>> result = hook.itemModels(mockItemStack);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("isItem")
    class IsItemTests {

        @Test
        @DisplayName("Given a valid ItemsAdder item name, when isItem(String) is called, then returns true")
        void isItem_string_returnsTrue_whenInRegistry() {
            customStackStatic.when(() -> CustomStack.isInRegistry("ia_sword")).thenReturn(true);

            assertTrue(hook.isItem("ia_sword"));
        }

        @Test
        @DisplayName("Given an invalid item name, when isItem(String) is called, then returns false")
        void isItem_string_returnsFalse_whenNotInRegistry() {
            customStackStatic.when(() -> CustomStack.isInRegistry("nonexistent")).thenReturn(false);

            assertFalse(hook.isItem("nonexistent"));
        }

        @Test
        @DisplayName("Given a valid ItemsAdder ItemStack, when isItem(ItemStack) is called, then returns true")
        void isItem_itemStack_returnsTrue_whenCustomStackExists() {
            ItemStack mockItemStack = mock(ItemStack.class);
            customStackStatic.when(() -> CustomStack.byItemStack(mockItemStack)).thenReturn(mock(CustomStack.class));

            assertTrue(hook.isItem(mockItemStack));
        }

        @Test
        @DisplayName("Given a non-ItemsAdder ItemStack, when isItem(ItemStack) is called, then returns false")
        void isItem_itemStack_returnsFalse_whenNotCustomStack() {
            ItemStack mockItemStack = mock(ItemStack.class);
            customStackStatic.when(() -> CustomStack.byItemStack(mockItemStack)).thenReturn(null);

            assertFalse(hook.isItem(mockItemStack));
        }
    }

    @Nested
    @DisplayName("isItemOfType")
    class IsItemOfTypeTests {

        @Test
        @DisplayName("Given matching ItemsAdder item, when isItemOfType is called, then returns true")
        void isItemOfType_returnsTrue_whenMatches() {
            ItemStack mockItemStack = mock(ItemStack.class);
            CustomStack mockStack = mock(CustomStack.class);
            when(mockStack.getModelPath()).thenReturn("ia:sword_model");
            customStackStatic.when(() -> CustomStack.byItemStack(mockItemStack)).thenReturn(mockStack);

            assertTrue(hook.isItemOfType(mockItemStack, "ia:sword_model"));
        }

        @Test
        @DisplayName("Given non-matching ItemsAdder item, when isItemOfType is called, then returns false")
        void isItemOfType_returnsFalse_whenDifferentType() {
            ItemStack mockItemStack = mock(ItemStack.class);
            CustomStack mockStack = mock(CustomStack.class);
            when(mockStack.getModelPath()).thenReturn("ia:bow_model");
            customStackStatic.when(() -> CustomStack.byItemStack(mockItemStack)).thenReturn(mockStack);

            assertFalse(hook.isItemOfType(mockItemStack, "ia:sword_model"));
        }

        @Test
        @DisplayName("Given matching ItemsAdder item with different case, when isItemOfType is called, then returns true")
        void isItemOfType_returnsTrue_caseInsensitive() {
            ItemStack mockItemStack = mock(ItemStack.class);
            CustomStack mockStack = mock(CustomStack.class);
            when(mockStack.getModelPath()).thenReturn("IA:Sword_Model");
            customStackStatic.when(() -> CustomStack.byItemStack(mockItemStack)).thenReturn(mockStack);

            assertTrue(hook.isItemOfType(mockItemStack, "ia:sword_model"));
        }

        @Test
        @DisplayName("Given a non-ItemsAdder item, when isItemOfType is called, then returns false")
        void isItemOfType_returnsFalse_whenNotCustomStack() {
            ItemStack mockItemStack = mock(ItemStack.class);
            customStackStatic.when(() -> CustomStack.byItemStack(mockItemStack)).thenReturn(null);

            assertFalse(hook.isItemOfType(mockItemStack, "ia:sword_model"));
        }
    }

    @Nested
    @DisplayName("isCustomBlock")
    class IsCustomBlockTests {

        @Test
        @DisplayName("Given an ItemsAdder block, when isCustomBlock(Block) is called, then returns true")
        void isCustomBlock_block_returnsTrue() {
            Block mockBlock = mock(Block.class);
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(mockBlock)).thenReturn(mock(CustomBlock.class));

            assertTrue(hook.isCustomBlock(mockBlock));
        }

        @Test
        @DisplayName("Given a non-ItemsAdder block, when isCustomBlock(Block) is called, then returns false")
        void isCustomBlock_block_returnsFalse() {
            Block mockBlock = mock(Block.class);
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(mockBlock)).thenReturn(null);

            assertFalse(hook.isCustomBlock(mockBlock));
        }

        @Test
        @DisplayName("Given any string, when isCustomBlock(String) is called, then always returns false")
        void isCustomBlock_string_alwaysReturnsFalse() {
            assertFalse(hook.isCustomBlock("any_block"));
        }
    }

    @Nested
    @DisplayName("isCustomBlockOfType")
    class IsCustomBlockOfTypeTests {

        @Test
        @DisplayName("Given matching ItemsAdder block, when isCustomBlockOfType is called, then returns true")
        void isCustomBlockOfType_returnsTrue_whenMatches() {
            Block mockBlock = mock(Block.class);
            CustomBlock mockCustomBlock = mock(CustomBlock.class);
            when(mockCustomBlock.getModelPath()).thenReturn("ia:custom_ore");
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(mockBlock)).thenReturn(mockCustomBlock);

            assertTrue(hook.isCustomBlockOfType(mockBlock, "ia:custom_ore"));
        }

        @Test
        @DisplayName("Given non-matching ItemsAdder block, when isCustomBlockOfType is called, then returns false")
        void isCustomBlockOfType_returnsFalse_whenDifferentType() {
            Block mockBlock = mock(Block.class);
            CustomBlock mockCustomBlock = mock(CustomBlock.class);
            when(mockCustomBlock.getModelPath()).thenReturn("ia:other_ore");
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(mockBlock)).thenReturn(mockCustomBlock);

            assertFalse(hook.isCustomBlockOfType(mockBlock, "ia:custom_ore"));
        }

        @Test
        @DisplayName("Given matching ItemsAdder block with different case, when isCustomBlockOfType is called, then returns true")
        void isCustomBlockOfType_returnsTrue_caseInsensitive() {
            Block mockBlock = mock(Block.class);
            CustomBlock mockCustomBlock = mock(CustomBlock.class);
            when(mockCustomBlock.getModelPath()).thenReturn("IA:Custom_Ore");
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(mockBlock)).thenReturn(mockCustomBlock);

            assertTrue(hook.isCustomBlockOfType(mockBlock, "ia:custom_ore"));
        }

        @Test
        @DisplayName("Given a non-ItemsAdder block, when isCustomBlockOfType is called, then returns false")
        void isCustomBlockOfType_returnsFalse_whenNotCustomBlock() {
            Block mockBlock = mock(Block.class);
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(mockBlock)).thenReturn(null);

            assertFalse(hook.isCustomBlockOfType(mockBlock, "ia:custom_ore"));
        }
    }

    @Nested
    @DisplayName("blockModels")
    class BlockModelsTests {

        @Test
        @DisplayName("Given an ItemsAdder block, when blockModels is called, then returns model path")
        void blockModels_returnsModelPath_whenCustomBlock() {
            Block mockBlock = mock(Block.class);
            CustomBlock mockCustomBlock = mock(CustomBlock.class);
            when(mockCustomBlock.getModelPath()).thenReturn("ia:custom_ore");
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(mockBlock)).thenReturn(mockCustomBlock);

            Optional<Set<String>> result = hook.blockModels(mockBlock);

            assertTrue(result.isPresent());
            assertEquals(Set.of("ia:custom_ore"), result.get());
        }

        @Test
        @DisplayName("Given a non-ItemsAdder block, when blockModels is called, then returns empty")
        void blockModels_returnsEmpty_whenNotCustomBlock() {
            Block mockBlock = mock(Block.class);
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(mockBlock)).thenReturn(null);

            Optional<Set<String>> result = hook.blockModels(mockBlock);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("placeCustomBlock")
    class PlaceCustomBlockTests {

        @Test
        @DisplayName("Given any block ID, when placeCustomBlock is called, then throws because isCustomBlock(String) always returns false")
        void placeCustomBlock_alwaysThrows_becauseStringCheckAlwaysFalse() {
            Location mockLocation = mock(Location.class);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> hook.placeCustomBlock(mockLocation, "ia_ore"));
            assertTrue(ex.getMessage().contains("ia_ore"));
        }
    }

    @Nested
    @DisplayName("drops")
    class DropsTests {

        @Test
        @DisplayName("Given an ItemsAdder block, when drops is called, then returns CustomBlock loot")
        void drops_returnsLoot_whenCustomBlock() {
            Block mockBlock = mock(Block.class);
            ItemStack mockTool = mock(ItemStack.class);
            Entity mockEntity = mock(Entity.class);
            ItemStack lootDrop = mock(ItemStack.class);

            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(mockBlock)).thenReturn(mock(CustomBlock.class));
            customBlockStatic.when(() -> CustomBlock.getLoot(mockBlock, mockTool, true)).thenReturn(List.of(lootDrop));

            List<ItemStack> result = hook.drops(mockBlock, mockTool, mockEntity);

            assertEquals(1, result.size());
            assertEquals(lootDrop, result.get(0));
        }

        @Test
        @DisplayName("Given a non-ItemsAdder block, when drops is called, then returns vanilla drops")
        @SuppressWarnings("unchecked")
        void drops_returnsVanillaDrops_whenNotCustomBlock() {
            Block mockBlock = mock(Block.class);
            ItemStack mockTool = mock(ItemStack.class);
            Entity mockEntity = mock(Entity.class);
            ItemStack vanillaDrop = mock(ItemStack.class);

            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(mockBlock)).thenReturn(null);
            when(mockBlock.getDrops(mockTool, mockEntity)).thenReturn((Collection) List.of(vanillaDrop));

            List<ItemStack> result = hook.drops(mockBlock, mockTool, mockEntity);

            assertEquals(1, result.size());
            assertEquals(vanillaDrop, result.get(0));
        }
    }

    @Nested
    @DisplayName("removeBlock")
    class RemoveBlockTests {

        @Test
        @DisplayName("Given an ItemsAdder block, when removeBlock succeeds, then no exception")
        void removeBlock_succeeds_whenCustomBlockRemoved() {
            Block mockBlock = mock(Block.class);
            CustomBlock mockCustomBlock = mock(CustomBlock.class);
            when(mockCustomBlock.remove()).thenReturn(true);
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(mockBlock)).thenReturn(mockCustomBlock);

            hook.removeBlock(mockBlock);

            verify(mockCustomBlock).remove();
        }

        @Test
        @DisplayName("Given an ItemsAdder block, when removeBlock fails, then throws IllegalStateException")
        void removeBlock_throws_whenRemovalFails() {
            Block mockBlock = mock(Block.class);
            Location mockLocation = mock(Location.class);
            when(mockBlock.getLocation()).thenReturn(mockLocation);

            CustomBlock mockCustomBlock = mock(CustomBlock.class);
            when(mockCustomBlock.remove()).thenReturn(false);
            when(mockCustomBlock.getModelPath()).thenReturn("ia:custom_ore");
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(mockBlock)).thenReturn(mockCustomBlock);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> hook.removeBlock(mockBlock));
            assertTrue(ex.getMessage().contains("ia:custom_ore"));
        }

        @Test
        @DisplayName("Given a vanilla block, when removeBlock is called, then sets type to AIR")
        void removeBlock_setsAir_whenVanillaBlock() {
            Block mockBlock = mock(Block.class);
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(mockBlock)).thenReturn(null);

            hook.removeBlock(mockBlock);

            verify(mockBlock).setType(Material.AIR);
        }
    }

    @Nested
    @DisplayName("playBlockDropEffects")
    class PlayBlockDropEffectsTests {

        @Test
        @DisplayName("Given an ItemsAdder block, when playBlockDropEffects is called, then delegates to CustomBlock effects")
        void playBlockDropEffects_delegatesToCustomBlock_whenCustomBlock() {
            Block mockBlock = mock(Block.class);
            CustomBlock mockCustomBlock = mock(CustomBlock.class);
            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(mockBlock)).thenReturn(mockCustomBlock);

            hook.playBlockDropEffects(mockBlock);

            verify(mockCustomBlock).playBreakEffect();
            verify(mockCustomBlock).playBreakSound();
            verify(mockCustomBlock).playBreakParticles();
        }

        @Test
        @DisplayName("Given a vanilla block, when playBlockDropEffects is called, then plays vanilla sound and particle")
        void playBlockDropEffects_playsVanillaEffects_whenNotCustomBlock() {
            Block mockBlock = mock(Block.class);
            Location mockLocation = mock(Location.class);
            Location clonedLocation = mock(Location.class);
            World mockWorld = mock(World.class);
            BlockData mockData = mock(BlockData.class);
            SoundGroup mockSoundGroup = mock(SoundGroup.class);

            when(mockBlock.getLocation()).thenReturn(mockLocation);
            when(mockBlock.getWorld()).thenReturn(mockWorld);
            when(mockBlock.getBlockData()).thenReturn(mockData);
            when(mockBlock.getBlockSoundGroup()).thenReturn(mockSoundGroup);
            when(mockLocation.clone()).thenReturn(clonedLocation);
            when(clonedLocation.add(0.5, 0.5, 0.5)).thenReturn(clonedLocation);
            when(mockSoundGroup.getBreakSound()).thenReturn(org.bukkit.Sound.BLOCK_STONE_BREAK);
            when(mockSoundGroup.getVolume()).thenReturn(1.0f);
            when(mockSoundGroup.getPitch()).thenReturn(1.0f);

            customBlockStatic.when(() -> CustomBlock.byAlreadyPlaced(mockBlock)).thenReturn(null);

            hook.playBlockDropEffects(mockBlock);

            verify(mockWorld).playSound(eq(mockLocation), eq(org.bukkit.Sound.BLOCK_STONE_BREAK), eq(1.0f), eq(1.0f));
            verify(mockWorld).spawnParticle(eq(Particle.BLOCK), eq(clonedLocation), eq(20),
                eq(0.25), eq(0.25), eq(0.25), eq(0.05), eq(mockData));
        }
    }

    @Nested
    @DisplayName("itemName")
    class ItemNameTests {

        @Test
        @DisplayName("Given a custom item with itemName component, when itemName is called, then returns plain name")
        void itemName_returnsPlainName_whenItemNamePresent() {
            CustomItemWrapper wrapper = mock(CustomItemWrapper.class);
            when(wrapper.customItem()).thenReturn(Optional.of("ia_sword"));

            CustomStack mockStack = mock(CustomStack.class);
            when(mockStack.itemName()).thenReturn(Component.text("ItemsAdder Sword"));
            customStackStatic.when(() -> CustomStack.getInstance("ia_sword")).thenReturn(mockStack);

            assertEquals("ItemsAdder Sword", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a custom item with null itemName but valid displayName, when itemName is called, then returns stripped displayName")
        void itemName_returnsStrippedDisplayName_whenItemNameNull() {
            CustomItemWrapper wrapper = mock(CustomItemWrapper.class);
            when(wrapper.customItem()).thenReturn(Optional.of("ia_sword"));

            CustomStack mockStack = mock(CustomStack.class);
            when(mockStack.itemName()).thenReturn(null);
            when(mockStack.getDisplayName()).thenReturn("§6Golden Sword");
            customStackStatic.when(() -> CustomStack.getInstance("ia_sword")).thenReturn(mockStack);

            assertEquals("Golden Sword", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a custom item with empty itemName component and valid displayName, when itemName is called, then returns stripped displayName")
        void itemName_returnsStrippedDisplayName_whenItemNameEmpty() {
            CustomItemWrapper wrapper = mock(CustomItemWrapper.class);
            when(wrapper.customItem()).thenReturn(Optional.of("ia_sword"));

            CustomStack mockStack = mock(CustomStack.class);
            when(mockStack.itemName()).thenReturn(Component.empty());
            when(mockStack.getDisplayName()).thenReturn("§cFire Sword");
            customStackStatic.when(() -> CustomStack.getInstance("ia_sword")).thenReturn(mockStack);

            assertEquals("Fire Sword", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a custom item with no name at all, when itemName is called, then returns formatted ID")
        void itemName_returnsFormattedId_whenNoNameAvailable() {
            CustomItemWrapper wrapper = mock(CustomItemWrapper.class);
            when(wrapper.customItem()).thenReturn(Optional.of("my_namespace:cool_sword"));

            CustomStack mockStack = mock(CustomStack.class);
            when(mockStack.itemName()).thenReturn(null);
            when(mockStack.getDisplayName()).thenReturn(null);
            customStackStatic.when(() -> CustomStack.getInstance("my_namespace:cool_sword")).thenReturn(mockStack);

            assertEquals("Cool Sword", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a custom item with empty displayName, when itemName is called, then returns formatted ID")
        void itemName_returnsFormattedId_whenDisplayNameEmpty() {
            CustomItemWrapper wrapper = mock(CustomItemWrapper.class);
            when(wrapper.customItem()).thenReturn(Optional.of("my_namespace:cool_sword"));

            CustomStack mockStack = mock(CustomStack.class);
            when(mockStack.itemName()).thenReturn(null);
            when(mockStack.getDisplayName()).thenReturn("");
            customStackStatic.when(() -> CustomStack.getInstance("my_namespace:cool_sword")).thenReturn(mockStack);

            assertEquals("Cool Sword", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a custom item not in ItemsAdder, when itemName is called, then returns formatted ID")
        void itemName_returnsFormattedId_whenCustomStackNull() {
            CustomItemWrapper wrapper = mock(CustomItemWrapper.class);
            when(wrapper.customItem()).thenReturn(Optional.of("my_namespace:missing_item"));

            customStackStatic.when(() -> CustomStack.getInstance("my_namespace:missing_item")).thenReturn(null);

            assertEquals("Missing Item", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a vanilla material wrapper, when itemName is called, then returns formatted material name")
        void itemName_returnsFormattedMaterial_whenVanillaItem() {
            CustomItemWrapper wrapper = mock(CustomItemWrapper.class);
            when(wrapper.customItem()).thenReturn(Optional.empty());
            when(wrapper.material()).thenReturn(Optional.of(Material.DIAMOND_SWORD));

            assertEquals("Diamond Sword", hook.itemName(wrapper));
        }
    }

    @Nested
    @DisplayName("blockName")
    class BlockNameTests {

        @Test
        @DisplayName("Given a custom block with itemName component, when blockName is called, then returns plain name")
        void blockName_returnsPlainName_whenItemNamePresent() {
            CustomBlockWrapper wrapper = mock(CustomBlockWrapper.class);
            when(wrapper.customBlock()).thenReturn(Optional.of("ia_ore"));

            CustomStack mockStack = mock(CustomStack.class);
            when(mockStack.itemName()).thenReturn(Component.text("ItemsAdder Ore"));
            customStackStatic.when(() -> CustomStack.getInstance("ia_ore")).thenReturn(mockStack);

            assertEquals("ItemsAdder Ore", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a custom block with null itemName but valid displayName, when blockName is called, then returns stripped displayName")
        void blockName_returnsStrippedDisplayName_whenItemNameNull() {
            CustomBlockWrapper wrapper = mock(CustomBlockWrapper.class);
            when(wrapper.customBlock()).thenReturn(Optional.of("ia_ore"));

            CustomStack mockStack = mock(CustomStack.class);
            when(mockStack.itemName()).thenReturn(null);
            when(mockStack.getDisplayName()).thenReturn("§bMagic Ore");
            customStackStatic.when(() -> CustomStack.getInstance("ia_ore")).thenReturn(mockStack);

            assertEquals("Magic Ore", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a custom block with no name, when blockName is called, then returns formatted ID")
        void blockName_returnsFormattedId_whenNoNameAvailable() {
            CustomBlockWrapper wrapper = mock(CustomBlockWrapper.class);
            when(wrapper.customBlock()).thenReturn(Optional.of("my_namespace:custom_ore"));

            CustomStack mockStack = mock(CustomStack.class);
            when(mockStack.itemName()).thenReturn(null);
            when(mockStack.getDisplayName()).thenReturn(null);
            customStackStatic.when(() -> CustomStack.getInstance("my_namespace:custom_ore")).thenReturn(mockStack);

            assertEquals("Custom Ore", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a custom block not in ItemsAdder, when blockName is called, then returns formatted ID")
        void blockName_returnsFormattedId_whenCustomStackNull() {
            CustomBlockWrapper wrapper = mock(CustomBlockWrapper.class);
            when(wrapper.customBlock()).thenReturn(Optional.of("my_namespace:unknown_ore"));

            customStackStatic.when(() -> CustomStack.getInstance("my_namespace:unknown_ore")).thenReturn(null);

            assertEquals("Unknown Ore", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a vanilla material wrapper, when blockName is called, then returns formatted material name")
        void blockName_returnsFormattedMaterial_whenVanillaBlock() {
            CustomBlockWrapper wrapper = mock(CustomBlockWrapper.class);
            when(wrapper.customBlock()).thenReturn(Optional.empty());
            when(wrapper.material()).thenReturn(Optional.of(Material.IRON_ORE));

            assertEquals("Iron Ore", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a custom block with empty itemName and empty displayName, when blockName is called, then returns formatted ID")
        void blockName_returnsFormattedId_whenBothNamesEmpty() {
            CustomBlockWrapper wrapper = mock(CustomBlockWrapper.class);
            when(wrapper.customBlock()).thenReturn(Optional.of("my_namespace:rare_ore"));

            CustomStack mockStack = mock(CustomStack.class);
            when(mockStack.itemName()).thenReturn(Component.empty());
            when(mockStack.getDisplayName()).thenReturn("");
            customStackStatic.when(() -> CustomStack.getInstance("my_namespace:rare_ore")).thenReturn(mockStack);

            assertEquals("Rare Ore", hook.blockName(wrapper));
        }
    }
}

package com.diamonddagger590.mccore.external.nexo;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.util.item.CustomBlockWrapper;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import com.nexomc.nexo.api.NexoBlocks;
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

/**
 * Tests for {@link CoreNexoHook}.
 * <p>
 * Nexo is written in Kotlin. Its {@code NexoItems} class and several value types
 * ({@code ItemBuilder}, {@code CustomBlockMechanic}, {@code BlockSounds}) have static
 * initialisers that depend on {@code NexoPlugin} being loaded, so they cannot be
 * mocked or even class-loaded in a unit-test environment.  Only {@code NexoBlocks}
 * can be mocked statically.  Tests are therefore limited to methods and code paths
 * that go through the {@code NexoBlocks} API plus vanilla-fallback branches.
 */
class CoreNexoHookTest {

    private CoreNexoHook hook;
    private MockedStatic<NexoBlocks> nexoBlocksStatic;

    @BeforeEach
    void setUp() {
        CorePlugin mockPlugin = mock(CorePlugin.class);
        hook = new CoreNexoHook(mockPlugin);
        nexoBlocksStatic = mockStatic(NexoBlocks.class);
    }

    @AfterEach
    void tearDown() {
        nexoBlocksStatic.close();
    }

    @Nested
    @DisplayName("isCustomBlock")
    class IsCustomBlockTests {

        @Test
        @DisplayName("Given a custom Nexo block, when isCustomBlock(Block) is called, then returns true")
        void isCustomBlock_block_returnsTrue() {
            Block mockBlock = mock(Block.class);
            nexoBlocksStatic.when(() -> NexoBlocks.isCustomBlock(mockBlock)).thenReturn(true);

            assertTrue(hook.isCustomBlock(mockBlock));
        }

        @Test
        @DisplayName("Given a non-Nexo block, when isCustomBlock(Block) is called, then returns false")
        void isCustomBlock_block_returnsFalse() {
            Block mockBlock = mock(Block.class);
            nexoBlocksStatic.when(() -> NexoBlocks.isCustomBlock(mockBlock)).thenReturn(false);

            assertFalse(hook.isCustomBlock(mockBlock));
        }

        @Test
        @DisplayName("Given a valid Nexo block ID, when isCustomBlock(String) is called, then returns true")
        void isCustomBlock_string_returnsTrue() {
            nexoBlocksStatic.when(() -> NexoBlocks.isCustomBlock("nexo_ore")).thenReturn(true);

            assertTrue(hook.isCustomBlock("nexo_ore"));
        }

        @Test
        @DisplayName("Given an invalid Nexo block ID, when isCustomBlock(String) is called, then returns false")
        void isCustomBlock_string_returnsFalse() {
            nexoBlocksStatic.when(() -> NexoBlocks.isCustomBlock("nonexistent")).thenReturn(false);

            assertFalse(hook.isCustomBlock("nonexistent"));
        }
    }

    @Nested
    @DisplayName("isCustomBlockOfType")
    class IsCustomBlockOfTypeTests {

        @Test
        @DisplayName("Given a non-Nexo block, when isCustomBlockOfType is called, then returns false")
        void isCustomBlockOfType_returnsFalse_whenNotCustomBlock() {
            Block mockBlock = mock(Block.class);
            nexoBlocksStatic.when(() -> NexoBlocks.isCustomBlock(mockBlock)).thenReturn(false);

            assertFalse(hook.isCustomBlockOfType(mockBlock, "nexo_ore"));
        }

        @Test
        @DisplayName("Given a custom block with null mechanic, when isCustomBlockOfType is called, then returns false")
        void isCustomBlockOfType_returnsFalse_whenMechanicIsNull() {
            Block mockBlock = mock(Block.class);
            Location mockLocation = mock(Location.class);
            when(mockBlock.getLocation()).thenReturn(mockLocation);

            nexoBlocksStatic.when(() -> NexoBlocks.isCustomBlock(mockBlock)).thenReturn(true);
            nexoBlocksStatic.when(() -> NexoBlocks.customBlockMechanic(mockLocation)).thenReturn(null);

            assertFalse(hook.isCustomBlockOfType(mockBlock, "nexo_ore"));
        }
    }

    @Nested
    @DisplayName("blockModels")
    class BlockModelsTests {

        @Test
        @DisplayName("Given a non-Nexo block, when blockModels is called, then returns empty")
        void blockModels_returnsEmpty_whenNotCustomBlock() {
            Block mockBlock = mock(Block.class);
            Location mockLocation = mock(Location.class);
            when(mockBlock.getLocation()).thenReturn(mockLocation);
            nexoBlocksStatic.when(() -> NexoBlocks.customBlockMechanic(mockLocation)).thenReturn(null);

            Optional<Set<String>> result = hook.blockModels(mockBlock);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("placeCustomBlock")
    class PlaceCustomBlockTests {

        @Test
        @DisplayName("Given a valid Nexo block ID, when placeCustomBlock is called, then delegates to NexoBlocks")
        void placeCustomBlock_delegates_whenValidBlock() {
            Location mockLocation = mock(Location.class);
            nexoBlocksStatic.when(() -> NexoBlocks.isCustomBlock("nexo_ore")).thenReturn(true);

            hook.placeCustomBlock(mockLocation, "nexo_ore");

            nexoBlocksStatic.verify(() -> NexoBlocks.place("nexo_ore", mockLocation));
        }

        @Test
        @DisplayName("Given an invalid block ID, when placeCustomBlock is called, then throws IllegalArgumentException")
        void placeCustomBlock_throws_whenInvalidBlock() {
            Location mockLocation = mock(Location.class);
            nexoBlocksStatic.when(() -> NexoBlocks.isCustomBlock("nonexistent")).thenReturn(false);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> hook.placeCustomBlock(mockLocation, "nonexistent"));
            assertTrue(ex.getMessage().contains("nonexistent"));
        }
    }

    @Nested
    @DisplayName("drops")
    class DropsTests {

        @Test
        @DisplayName("Given a custom block with null mechanic, when drops is called, then returns empty list")
        void drops_returnsEmpty_whenCustomBlockMechanicIsNull() {
            Block mockBlock = mock(Block.class);
            Location mockLocation = mock(Location.class);
            when(mockBlock.getLocation()).thenReturn(mockLocation);

            nexoBlocksStatic.when(() -> NexoBlocks.isCustomBlock(mockBlock)).thenReturn(true);
            nexoBlocksStatic.when(() -> NexoBlocks.customBlockMechanic(mockLocation)).thenReturn(null);

            List<ItemStack> result = hook.drops(mockBlock, mock(ItemStack.class), mock(Entity.class));

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given a vanilla block, when drops is called, then returns vanilla drops")
        @SuppressWarnings("unchecked")
        void drops_returnsVanillaDrops_whenNotCustomBlock() {
            Block mockBlock = mock(Block.class);
            ItemStack mockTool = mock(ItemStack.class);
            Entity mockEntity = mock(Entity.class);
            ItemStack vanillaDrop = mock(ItemStack.class);

            nexoBlocksStatic.when(() -> NexoBlocks.isCustomBlock(mockBlock)).thenReturn(false);
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
        @DisplayName("Given a custom block, when removeBlock is called and removal succeeds, then no exception")
        void removeBlock_succeeds_whenCustomBlockRemoved() {
            Block mockBlock = mock(Block.class);
            Location mockLocation = mock(Location.class);
            when(mockBlock.getLocation()).thenReturn(mockLocation);

            nexoBlocksStatic.when(() -> NexoBlocks.isCustomBlock(mockBlock)).thenReturn(true);
            nexoBlocksStatic.when(() -> NexoBlocks.remove(mockLocation)).thenReturn(true);

            hook.removeBlock(mockBlock);

            nexoBlocksStatic.verify(() -> NexoBlocks.remove(mockLocation));
        }

        @Test
        @DisplayName("Given a vanilla block, when removeBlock is called, then sets type to AIR")
        void removeBlock_setsAir_whenVanillaBlock() {
            Block mockBlock = mock(Block.class);
            nexoBlocksStatic.when(() -> NexoBlocks.isCustomBlock(mockBlock)).thenReturn(false);

            hook.removeBlock(mockBlock);

            verify(mockBlock).setType(Material.AIR);
        }
    }

    @Nested
    @DisplayName("playBlockDropEffects")
    class PlayBlockDropEffectsTests {

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

            nexoBlocksStatic.when(() -> NexoBlocks.isCustomBlock(mockBlock)).thenReturn(false);

            hook.playBlockDropEffects(mockBlock);

            verify(mockWorld).playSound(eq(mockLocation), eq(org.bukkit.Sound.BLOCK_STONE_BREAK), eq(1.0f), eq(1.0f));
            verify(mockWorld).spawnParticle(eq(Particle.BLOCK), eq(clonedLocation), eq(20),
                eq(0.25), eq(0.25), eq(0.25), eq(0.05), eq(mockData));
        }

        @Test
        @DisplayName("Given a custom block with null mechanic, when playBlockDropEffects is called, then plays vanilla effects")
        void playBlockDropEffects_playsVanilla_whenMechanicIsNull() {
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

            nexoBlocksStatic.when(() -> NexoBlocks.isCustomBlock(mockBlock)).thenReturn(true);
            nexoBlocksStatic.when(() -> NexoBlocks.customBlockMechanic(mockLocation)).thenReturn(null);

            hook.playBlockDropEffects(mockBlock);

            verify(mockWorld).playSound(eq(mockLocation), eq(org.bukkit.Sound.BLOCK_STONE_BREAK), eq(1.0f), eq(1.0f));
        }
    }

    @Nested
    @DisplayName("blockName")
    class BlockNameTests {

        @Test
        @DisplayName("Given a custom block not in Nexo, when blockName is called, then returns formatted block ID")
        void blockName_returnsFormattedId_whenNotValidNexoBlock() {
            CustomBlockWrapper wrapper = mock(CustomBlockWrapper.class);
            when(wrapper.customBlock()).thenReturn(Optional.of("my_namespace:unknown_block"));
            nexoBlocksStatic.when(() -> NexoBlocks.isCustomBlock("my_namespace:unknown_block")).thenReturn(false);

            assertEquals("Unknown Block", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a vanilla material wrapper, when blockName is called, then returns formatted material name")
        void blockName_returnsFormattedMaterial_whenVanillaBlock() {
            CustomBlockWrapper wrapper = mock(CustomBlockWrapper.class);
            when(wrapper.customBlock()).thenReturn(Optional.empty());
            when(wrapper.material()).thenReturn(Optional.of(Material.STONE));

            assertEquals("Stone", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a simple custom block ID without namespace, when blockName is called, then returns formatted ID")
        void blockName_returnsFormattedId_forSimpleIdWithoutNamespace() {
            CustomBlockWrapper wrapper = mock(CustomBlockWrapper.class);
            when(wrapper.customBlock()).thenReturn(Optional.of("fancy_ore"));
            nexoBlocksStatic.when(() -> NexoBlocks.isCustomBlock("fancy_ore")).thenReturn(false);

            assertEquals("Fancy Ore", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a material with multiple underscores, when blockName is called, then formats each word")
        void blockName_formatsMultiWordMaterial() {
            CustomBlockWrapper wrapper = mock(CustomBlockWrapper.class);
            when(wrapper.customBlock()).thenReturn(Optional.empty());
            when(wrapper.material()).thenReturn(Optional.of(Material.DARK_OAK_PLANKS));

            assertEquals("Dark Oak Planks", hook.blockName(wrapper));
        }
    }

    @Nested
    @DisplayName("itemName")
    class ItemNameTests {

        @Test
        @DisplayName("Given a vanilla material wrapper, when itemName is called, then returns formatted material name")
        void itemName_returnsFormattedMaterial_whenVanillaItem() {
            CustomItemWrapper wrapper = mock(CustomItemWrapper.class);
            when(wrapper.customItem()).thenReturn(Optional.empty());
            when(wrapper.material()).thenReturn(Optional.of(Material.OAK_LOG));

            assertEquals("Oak Log", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a vanilla material with single word, when itemName is called, then formats correctly")
        void itemName_formatsSingleWordMaterial() {
            CustomItemWrapper wrapper = mock(CustomItemWrapper.class);
            when(wrapper.customItem()).thenReturn(Optional.empty());
            when(wrapper.material()).thenReturn(Optional.of(Material.DIAMOND));

            assertEquals("Diamond", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a vanilla material with multiple underscores, when itemName is called, then formats each word")
        void itemName_formatsMultiWordMaterial() {
            CustomItemWrapper wrapper = mock(CustomItemWrapper.class);
            when(wrapper.customItem()).thenReturn(Optional.empty());
            when(wrapper.material()).thenReturn(Optional.of(Material.DARK_OAK_PLANKS));

            assertEquals("Dark Oak Planks", hook.itemName(wrapper));
        }
    }
}

package com.diamonddagger590.mccore.external.nexo;

import com.diamonddagger590.mccore.util.item.CustomBlockWrapper;
import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link CoreNexoHook} — block-related methods and formatting fallbacks.
 * <p>
 * Nexo's {@code NexoItems} class cannot be statically mocked because its method signatures
 * reference {@code ItemBuilder}, whose static initializer requires a running {@code NexoPlugin}.
 * Similarly, {@code CustomBlockMechanic} cannot be mocked by Mockito because its parent class
 * {@code Mechanic} references {@code ItemBuilder}; instances are created via {@code Unsafe} instead.
 */
class CoreNexoHookTest {

    private CoreNexoHook hook;
    private MockedStatic<NexoBlocks> nexoBlocksMock;
    private Unsafe unsafe;

    @BeforeEach
    void setUp() throws Exception {
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        unsafe = (Unsafe) unsafeField.get(null);
        hook = (CoreNexoHook) unsafe.allocateInstance(CoreNexoHook.class);

        nexoBlocksMock = mockStatic(NexoBlocks.class);
    }

    @AfterEach
    void tearDown() {
        nexoBlocksMock.close();
    }

    private CustomBlockMechanic createMechanicWithItemId(@NotNull String itemId) throws Exception {
        Class<?> concreteClass = Class.forName(
            "com.nexomc.nexo.mechanics.custom_block.noteblock.NoteBlockMechanic",
            false,
            getClass().getClassLoader()
        );
        CustomBlockMechanic mechanic = (CustomBlockMechanic) unsafe.allocateInstance(concreteClass);
        Field itemIdField = mechanic.getClass().getSuperclass().getSuperclass().getDeclaredField("itemID");
        itemIdField.setAccessible(true);
        itemIdField.set(mechanic, itemId);
        return mechanic;
    }

    @Nested
    @DisplayName("isCustomBlock(Block)")
    class IsCustomBlockByBlock {

        @Test
        @DisplayName("Given a Nexo block, when checking isCustomBlock, then returns true")
        void returnsTrue_whenBlockIsNexo() {
            Block block = mock(Block.class);
            nexoBlocksMock.when(() -> NexoBlocks.isCustomBlock(block)).thenReturn(true);

            assertTrue(hook.isCustomBlock(block));
        }

        @Test
        @DisplayName("Given a vanilla block, when checking isCustomBlock, then returns false")
        void returnsFalse_whenBlockIsVanilla() {
            Block block = mock(Block.class);
            nexoBlocksMock.when(() -> NexoBlocks.isCustomBlock(block)).thenReturn(false);

            assertFalse(hook.isCustomBlock(block));
        }
    }

    @Nested
    @DisplayName("isCustomBlock(String)")
    class IsCustomBlockByName {

        @Test
        @DisplayName("Given a valid Nexo block id, when checking isCustomBlock, then returns true")
        void returnsTrue_whenBlockExists() {
            nexoBlocksMock.when(() -> NexoBlocks.isCustomBlock("nexo:ore")).thenReturn(true);

            assertTrue(hook.isCustomBlock("nexo:ore"));
        }

        @Test
        @DisplayName("Given an invalid block id, when checking isCustomBlock, then returns false")
        void returnsFalse_whenBlockDoesNotExist() {
            nexoBlocksMock.when(() -> NexoBlocks.isCustomBlock("unknown")).thenReturn(false);

            assertFalse(hook.isCustomBlock("unknown"));
        }
    }

    @Nested
    @DisplayName("isCustomBlockOfType")
    class IsCustomBlockOfType {

        @Test
        @DisplayName("Given a matching Nexo block, when checking isCustomBlockOfType, then returns true")
        void returnsTrue_whenBlockMatchesType() throws Exception {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);

            nexoBlocksMock.when(() -> NexoBlocks.isCustomBlock(block)).thenReturn(true);
            CustomBlockMechanic mechanic = createMechanicWithItemId("nexo:ore");
            nexoBlocksMock.when(() -> NexoBlocks.customBlockMechanic(location)).thenReturn(mechanic);

            assertTrue(hook.isCustomBlockOfType(block, "nexo:ore"));
        }

        @Test
        @DisplayName("Given a non-matching Nexo block, when checking isCustomBlockOfType, then returns false")
        void returnsFalse_whenBlockTypeDoesNotMatch() throws Exception {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);

            nexoBlocksMock.when(() -> NexoBlocks.isCustomBlock(block)).thenReturn(true);
            CustomBlockMechanic mechanic = createMechanicWithItemId("nexo:stone");
            nexoBlocksMock.when(() -> NexoBlocks.customBlockMechanic(location)).thenReturn(mechanic);

            assertFalse(hook.isCustomBlockOfType(block, "nexo:ore"));
        }

        @Test
        @DisplayName("Given a vanilla block, when checking isCustomBlockOfType, then returns false")
        void returnsFalse_whenBlockIsVanilla() {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);

            nexoBlocksMock.when(() -> NexoBlocks.isCustomBlock(block)).thenReturn(false);
            nexoBlocksMock.when(() -> NexoBlocks.customBlockMechanic(location)).thenReturn(null);

            assertFalse(hook.isCustomBlockOfType(block, "nexo:ore"));
        }
    }

    @Nested
    @DisplayName("blockModels")
    class BlockModelsTest {

        @Test
        @DisplayName("Given a Nexo block, when getting blockModels, then returns the item id")
        void returnsItemId_whenBlockIsNexo() throws Exception {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);

            CustomBlockMechanic mechanic = createMechanicWithItemId("nexo:ore");
            nexoBlocksMock.when(() -> NexoBlocks.customBlockMechanic(location)).thenReturn(mechanic);

            Optional<Set<String>> result = hook.blockModels(block);

            assertTrue(result.isPresent());
            assertEquals(Set.of("nexo:ore"), result.get());
        }

        @Test
        @DisplayName("Given a vanilla block, when getting blockModels, then returns empty")
        void returnsEmpty_whenBlockIsVanilla() {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);

            nexoBlocksMock.when(() -> NexoBlocks.customBlockMechanic(location)).thenReturn(null);

            Optional<Set<String>> result = hook.blockModels(block);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("placeCustomBlock")
    class PlaceCustomBlock {

        @Test
        @DisplayName("Given a valid Nexo block id, when placing, then delegates to NexoBlocks.place")
        void delegatesToNexoBlocksPlace_whenBlockIsValid() {
            Location location = mock(Location.class);
            nexoBlocksMock.when(() -> NexoBlocks.isCustomBlock("nexo:ore")).thenReturn(true);

            hook.placeCustomBlock(location, "nexo:ore");

            nexoBlocksMock.verify(() -> NexoBlocks.place("nexo:ore", location));
        }

        @Test
        @DisplayName("Given an invalid block id, when placing, then throws IllegalArgumentException")
        void throwsIllegalArgument_whenBlockIsInvalid() {
            Location location = mock(Location.class);
            nexoBlocksMock.when(() -> NexoBlocks.isCustomBlock("unknown")).thenReturn(false);

            assertThrows(IllegalArgumentException.class, () -> hook.placeCustomBlock(location, "unknown"));
        }
    }

    @Nested
    @DisplayName("drops")
    class DropsTest {

        @Test
        @DisplayName("Given a vanilla block, when getting drops, then delegates to block.getDrops")
        void delegatesToBlockGetDrops_whenBlockIsVanilla() {
            Block block = mock(Block.class);
            ItemStack tool = mock(ItemStack.class);
            Entity entity = mock(Entity.class);
            Collection<ItemStack> blockDrops = List.of(mock(ItemStack.class));

            nexoBlocksMock.when(() -> NexoBlocks.isCustomBlock(block)).thenReturn(false);
            when(block.getDrops(tool, entity)).thenReturn(blockDrops);

            List<ItemStack> result = hook.drops(block, tool, entity);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Given a custom block with no mechanic, when getting drops, then returns empty list")
        void returnsEmptyList_whenCustomBlockHasNoMechanic() {
            Block block = mock(Block.class);
            ItemStack tool = mock(ItemStack.class);
            Entity entity = mock(Entity.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);

            nexoBlocksMock.when(() -> NexoBlocks.isCustomBlock(block)).thenReturn(true);
            nexoBlocksMock.when(() -> NexoBlocks.customBlockMechanic(location)).thenReturn(null);

            List<ItemStack> result = hook.drops(block, tool, entity);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given a custom block with non-player entity, when getting drops, then returns empty list")
        void returnsEmptyList_whenEntityIsNotPlayer() throws Exception {
            Block block = mock(Block.class);
            ItemStack tool = mock(ItemStack.class);
            Entity entity = mock(Entity.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);

            nexoBlocksMock.when(() -> NexoBlocks.isCustomBlock(block)).thenReturn(true);
            CustomBlockMechanic mechanic = createMechanicWithItemId("nexo:ore");
            nexoBlocksMock.when(() -> NexoBlocks.customBlockMechanic(location)).thenReturn(mechanic);

            List<ItemStack> result = hook.drops(block, tool, entity);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("removeBlock")
    class RemoveBlock {

        @Test
        @DisplayName("Given a custom block that removes successfully, when removing, then completes without error")
        void removesSuccessfully_whenNexoBlockRemoves() {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);

            nexoBlocksMock.when(() -> NexoBlocks.isCustomBlock(block)).thenReturn(true);
            nexoBlocksMock.when(() -> NexoBlocks.remove(location)).thenReturn(true);

            hook.removeBlock(block);

            nexoBlocksMock.verify(() -> NexoBlocks.remove(location));
        }

        @Test
        @DisplayName("Given a custom block that fails to remove, when removing, then throws IllegalStateException")
        void throwsIllegalState_whenNexoBlockFailsToRemove() throws Exception {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);

            nexoBlocksMock.when(() -> NexoBlocks.isCustomBlock(block)).thenReturn(true);
            nexoBlocksMock.when(() -> NexoBlocks.remove(location)).thenReturn(false);

            CustomBlockMechanic mechanic = createMechanicWithItemId("nexo:ore");
            nexoBlocksMock.when(() -> NexoBlocks.customBlockMechanic(location)).thenReturn(mechanic);

            assertThrows(IllegalStateException.class, () -> hook.removeBlock(block));
        }

        @Test
        @DisplayName("Given a vanilla block, when removing, then sets type to AIR")
        void setsTypeToAir_whenBlockIsVanilla() {
            Block block = mock(Block.class);
            nexoBlocksMock.when(() -> NexoBlocks.isCustomBlock(block)).thenReturn(false);

            hook.removeBlock(block);

            verify(block).setType(Material.AIR);
        }
    }

    @Nested
    @DisplayName("playBlockDropEffects")
    class PlayBlockDropEffects {

        @Test
        @DisplayName("Given a vanilla block, when playing effects, then plays vanilla sound and spawns particles")
        void playsVanillaEffects_whenBlockIsVanilla() {
            Block block = mock(Block.class);
            World world = mock(World.class);
            Location location = mock(Location.class);
            Location clonedLocation = mock(Location.class);
            BlockData blockData = mock(BlockData.class);
            SoundGroup soundGroup = mock(SoundGroup.class);

            nexoBlocksMock.when(() -> NexoBlocks.isCustomBlock(block)).thenReturn(false);
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

        @Test
        @DisplayName("Given a custom block without sounds, when playing effects, then falls back to vanilla sounds")
        void playsVanillaEffects_whenCustomBlockHasNoSounds() throws Exception {
            Block block = mock(Block.class);
            World world = mock(World.class);
            Location location = mock(Location.class);
            Location clonedLocation = mock(Location.class);
            BlockData blockData = mock(BlockData.class);
            SoundGroup soundGroup = mock(SoundGroup.class);

            nexoBlocksMock.when(() -> NexoBlocks.isCustomBlock(block)).thenReturn(true);
            CustomBlockMechanic mechanic = createMechanicWithItemId("nexo:ore");
            nexoBlocksMock.when(() -> NexoBlocks.customBlockMechanic(location)).thenReturn(mechanic);

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
        }

        @Test
        @DisplayName("Given a custom block with null mechanic, when playing effects, then falls back to vanilla sounds")
        void playsVanillaEffects_whenMechanicIsNull() {
            Block block = mock(Block.class);
            World world = mock(World.class);
            Location location = mock(Location.class);
            Location clonedLocation = mock(Location.class);
            BlockData blockData = mock(BlockData.class);
            SoundGroup soundGroup = mock(SoundGroup.class);

            nexoBlocksMock.when(() -> NexoBlocks.isCustomBlock(block)).thenReturn(true);
            nexoBlocksMock.when(() -> NexoBlocks.customBlockMechanic(location)).thenReturn(null);

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
        }

        @Test
        @DisplayName("Given a custom block with sounds, when playing effects, then plays custom break sound")
        void playsCustomSound_whenCustomBlockHasSounds() throws Exception {
            Block block = mock(Block.class);
            World world = mock(World.class);
            Location location = mock(Location.class);

            nexoBlocksMock.when(() -> NexoBlocks.isCustomBlock(block)).thenReturn(true);

            CustomBlockMechanic mechanic = createMechanicWithItemId("nexo:ore");
            com.nexomc.nexo.utils.blocksounds.BlockSounds blockSounds =
                (com.nexomc.nexo.utils.blocksounds.BlockSounds) unsafe.allocateInstance(
                    com.nexomc.nexo.utils.blocksounds.BlockSounds.class);
            Field breakSoundField = com.nexomc.nexo.utils.blocksounds.BlockSounds.class.getDeclaredField("breakSound");
            breakSoundField.setAccessible(true);
            breakSoundField.set(blockSounds, "custom.break.sound");
            Field breakVolumeField = com.nexomc.nexo.utils.blocksounds.BlockSounds.class.getDeclaredField("breakVolume");
            breakVolumeField.setAccessible(true);
            breakVolumeField.setFloat(blockSounds, 0.8f);
            Field breakPitchField = com.nexomc.nexo.utils.blocksounds.BlockSounds.class.getDeclaredField("breakPitch");
            breakPitchField.setAccessible(true);
            breakPitchField.setFloat(blockSounds, 1.2f);

            Field blockSoundsField = CustomBlockMechanic.class.getDeclaredField("blockSounds");
            blockSoundsField.setAccessible(true);
            blockSoundsField.set(mechanic, blockSounds);

            nexoBlocksMock.when(() -> NexoBlocks.customBlockMechanic(location)).thenReturn(mechanic);

            when(block.getWorld()).thenReturn(world);
            when(block.getLocation()).thenReturn(location);

            hook.playBlockDropEffects(block);

            verify(world).playSound(location, "custom.break.sound", 0.8f, 1.2f);
        }
    }

    @Nested
    @DisplayName("blockName")
    class BlockNameTest {

        @Test
        @DisplayName("Given an unregistered Nexo block id, when getting blockName, then returns formatted id")
        void returnsFormattedId_whenBlockNotRegistered() {
            nexoBlocksMock.when(() -> NexoBlocks.isCustomBlock("nexo:missing_block")).thenReturn(false);

            CustomBlockWrapper wrapper = new TestCustomBlockWrapper("nexo:missing_block");

            assertEquals("Missing Block", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a vanilla block wrapper, when getting blockName, then returns formatted material name")
        void returnsFormattedMaterialName_whenVanilla() {
            CustomBlockWrapper wrapper = new CustomBlockWrapper(Material.STONE);

            assertEquals("Stone", hook.blockName(wrapper));
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

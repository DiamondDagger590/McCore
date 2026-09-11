package com.diamonddagger590.mccore.external.nexo;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.util.item.CustomBlockWrapper;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import com.nexomc.nexo.utils.drops.Loot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundGroup;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link CoreNexoHook}.
 * <p>
 * The Nexo API classes ({@code NexoItems}, {@code NexoBlocks}, {@code ItemBuilder},
 * {@code BlockSounds}) cannot be loaded in a unit test environment because their static
 * initializers require a running server. This test class uses a spy on {@link TestableNexoHook}
 * which overrides only the thin API-delegating methods and the protected Nexo API extraction
 * methods. All branching logic in the complex methods runs as real production code from
 * {@link CoreNexoHook}.
 */
@ExtendWith(MockitoExtension.class)
class CoreNexoHookTest {

    @Mock
    private CorePlugin mockPlugin;

    private CoreNexoHook hook;

    @BeforeEach
    void setUp() {
        hook = spy(new TestableNexoHook(mockPlugin));
    }

    @Nested
    @DisplayName("isCustomBlockOfType")
    class IsCustomBlockOfType {

        @Test
        @DisplayName("Given a Nexo custom block matching the expected type, when isCustomBlockOfType is called, then returns true")
        void isCustomBlockOfType_returnsTrue_whenBlockMatchesType() {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);
            when(hook.isCustomBlock(block)).thenReturn(true);

            CustomBlockMechanic mockMechanic = mock(CustomBlockMechanic.class);
            when(mockMechanic.getItemID()).thenReturn("custom_ore");
            ((TestableNexoHook) hook).setMockMechanic(mockMechanic);

            assertTrue(hook.isCustomBlockOfType(block, "custom_ore"));
        }

        @Test
        @DisplayName("Given a Nexo custom block matching with different case, when isCustomBlockOfType is called, then returns true")
        void isCustomBlockOfType_returnsTrue_caseInsensitive() {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);
            when(hook.isCustomBlock(block)).thenReturn(true);

            CustomBlockMechanic mockMechanic = mock(CustomBlockMechanic.class);
            when(mockMechanic.getItemID()).thenReturn("Custom_Ore");
            ((TestableNexoHook) hook).setMockMechanic(mockMechanic);

            assertTrue(hook.isCustomBlockOfType(block, "custom_ore"));
        }

        @Test
        @DisplayName("Given a vanilla block, when isCustomBlockOfType is called, then returns false")
        void isCustomBlockOfType_returnsFalse_whenBlockIsVanilla() {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);
            when(hook.isCustomBlock(block)).thenReturn(false);

            assertFalse(hook.isCustomBlockOfType(block, "custom_ore"));
        }

        @Test
        @DisplayName("Given a Nexo block of a different type, when isCustomBlockOfType is called, then returns false")
        void isCustomBlockOfType_returnsFalse_whenBlockTypeDoesNotMatch() {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);
            when(hook.isCustomBlock(block)).thenReturn(true);

            CustomBlockMechanic mockMechanic = mock(CustomBlockMechanic.class);
            when(mockMechanic.getItemID()).thenReturn("other_block");
            ((TestableNexoHook) hook).setMockMechanic(mockMechanic);

            assertFalse(hook.isCustomBlockOfType(block, "custom_ore"));
        }

        @Test
        @DisplayName("Given a Nexo block with null mechanic, when isCustomBlockOfType is called, then returns false")
        void isCustomBlockOfType_returnsFalse_whenMechanicIsNull() {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);
            when(hook.isCustomBlock(block)).thenReturn(true);
            ((TestableNexoHook) hook).setMockMechanic(null);

            assertFalse(hook.isCustomBlockOfType(block, "custom_ore"));
        }
    }

    @Nested
    @DisplayName("blockModels")
    class BlockModels {

        @Test
        @DisplayName("Given a block with a custom mechanic, when blockModels is called, then returns set with the item ID")
        void blockModels_returnsSetWithId_whenMechanicExists() {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);

            CustomBlockMechanic mockMechanic = mock(CustomBlockMechanic.class);
            when(mockMechanic.getItemID()).thenReturn("custom_ore");
            ((TestableNexoHook) hook).setMockMechanic(mockMechanic);

            Optional<Set<String>> result = hook.blockModels(block);

            assertTrue(result.isPresent());
            assertEquals(Set.of("custom_ore"), result.get());
        }

        @Test
        @DisplayName("Given a block without a custom mechanic, when blockModels is called, then returns empty Optional")
        void blockModels_returnsEmpty_whenNoMechanic() {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);
            ((TestableNexoHook) hook).setMockMechanic(null);

            Optional<Set<String>> result = hook.blockModels(block);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("placeCustomBlock")
    class PlaceCustomBlock {

        @Test
        @DisplayName("Given a valid block ID, when placeCustomBlock is called, then places the block")
        void placeCustomBlock_placesBlock_whenBlockIdIsValid() {
            Location location = mock(Location.class);
            when(hook.isCustomBlock("custom_ore")).thenReturn(true);

            hook.placeCustomBlock(location, "custom_ore");

            assertTrue(((TestableNexoHook) hook).wasPlaceCalled());
        }

        @Test
        @DisplayName("Given an invalid block ID, when placeCustomBlock is called, then throws IllegalArgumentException")
        void placeCustomBlock_throwsException_whenBlockIdIsInvalid() {
            Location location = mock(Location.class);
            when(hook.isCustomBlock("nonexistent")).thenReturn(false);

            assertThrows(IllegalArgumentException.class, () -> hook.placeCustomBlock(location, "nonexistent"));
        }
    }

    @Nested
    @DisplayName("drops")
    class Drops {

        @Test
        @DisplayName("Given a Nexo custom block with a player breaking it, when drops is called, then returns loot drops")
        void drops_returnsLootDrops_whenCustomBlockWithPlayer() {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);
            ItemStack breakItem = mock(ItemStack.class);
            Player player = mock(Player.class);
            ItemStack lootItem = mock(ItemStack.class);

            when(hook.isCustomBlock(block)).thenReturn(true);

            CustomBlockMechanic mockMechanic = mock(CustomBlockMechanic.class, Mockito.RETURNS_DEEP_STUBS);
            Loot mockLoot = mock(Loot.class);
            when(mockLoot.getItemStack()).thenReturn(lootItem);
            when(mockMechanic.getBreakable().getDrop().lootToDrop(any(Player.class))).thenReturn(List.of(mockLoot));
            ((TestableNexoHook) hook).setMockMechanic(mockMechanic);

            List<ItemStack> result = hook.drops(block, breakItem, player);

            assertEquals(1, result.size());
            assertEquals(lootItem, result.get(0));
        }

        @Test
        @DisplayName("Given a Nexo custom block with a non-player entity, when drops is called, then returns empty list")
        void drops_returnsEmptyList_whenCustomBlockWithNonPlayer() {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);
            ItemStack breakItem = mock(ItemStack.class);
            Entity entity = mock(Entity.class);

            when(hook.isCustomBlock(block)).thenReturn(true);

            CustomBlockMechanic mockMechanic = mock(CustomBlockMechanic.class);
            ((TestableNexoHook) hook).setMockMechanic(mockMechanic);

            List<ItemStack> result = hook.drops(block, breakItem, entity);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given a Nexo custom block with null mechanic, when drops is called, then returns empty list")
        void drops_returnsEmptyList_whenCustomBlockWithNullMechanic() {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);
            ItemStack breakItem = mock(ItemStack.class);
            Player player = mock(Player.class);

            when(hook.isCustomBlock(block)).thenReturn(true);
            ((TestableNexoHook) hook).setMockMechanic(null);

            List<ItemStack> result = hook.drops(block, breakItem, player);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given a Nexo custom block with null entity, when drops is called, then returns empty list")
        void drops_returnsEmptyList_whenCustomBlockWithNullEntity() {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);
            ItemStack breakItem = mock(ItemStack.class);

            when(hook.isCustomBlock(block)).thenReturn(true);

            CustomBlockMechanic mockMechanic = mock(CustomBlockMechanic.class);
            ((TestableNexoHook) hook).setMockMechanic(mockMechanic);

            List<ItemStack> result = hook.drops(block, breakItem, null);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given a vanilla block, when drops is called, then returns vanilla block drops")
        @SuppressWarnings("unchecked")
        void drops_returnsVanillaDrops_whenBlockIsVanilla() {
            Block block = mock(Block.class);
            ItemStack breakItem = mock(ItemStack.class);
            Entity entity = mock(Entity.class);
            ItemStack vanillaDrop = mock(ItemStack.class);

            when(hook.isCustomBlock(block)).thenReturn(false);
            when(block.getDrops(breakItem, entity)).thenReturn((Collection) List.of(vanillaDrop));

            List<ItemStack> result = hook.drops(block, breakItem, entity);

            assertEquals(1, result.size());
            assertEquals(vanillaDrop, result.get(0));
        }
    }

    @Nested
    @DisplayName("removeBlock")
    class RemoveBlock {

        @Test
        @DisplayName("Given a Nexo custom block that removes successfully, when removeBlock is called, then completes without exception")
        void removeBlock_removesViaNexo_whenCustomBlock() {
            Block block = mock(Block.class);
            when(hook.isCustomBlock(block)).thenReturn(true);
            ((TestableNexoHook) hook).setRemoveResult(true);

            hook.removeBlock(block);
        }

        @Test
        @DisplayName("Given a Nexo custom block that fails to remove, when removeBlock is called, then throws IllegalStateException")
        void removeBlock_throwsException_whenRemoveFails() {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);
            when(hook.isCustomBlock(block)).thenReturn(true);
            ((TestableNexoHook) hook).setRemoveResult(false);

            CustomBlockMechanic mockMechanic = mock(CustomBlockMechanic.class);
            when(mockMechanic.getItemID()).thenReturn("custom_ore");
            ((TestableNexoHook) hook).setMockMechanic(mockMechanic);

            assertThrows(IllegalStateException.class, () -> hook.removeBlock(block));
        }

        @Test
        @DisplayName("Given a vanilla block, when removeBlock is called, then sets block type to AIR")
        void removeBlock_setsToAir_whenVanillaBlock() {
            Block block = mock(Block.class);
            when(hook.isCustomBlock(block)).thenReturn(false);

            hook.removeBlock(block);

            verify(block).setType(Material.AIR);
        }
    }

    @Nested
    @DisplayName("playBlockDropEffects")
    class PlayBlockDropEffects {

        @Test
        @DisplayName("Given a Nexo custom block with block sounds, when playBlockDropEffects is called, then plays custom break sound")
        void playBlockDropEffects_playsCustomSound_whenCustomBlockWithSounds() {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            when(block.getLocation()).thenReturn(location);

            when(hook.isCustomBlock(block)).thenReturn(true);

            CustomBlockMechanic mockMechanic = mock(CustomBlockMechanic.class);
            ((TestableNexoHook) hook).setMockMechanic(mockMechanic);
            ((TestableNexoHook) hook).setCustomSoundResult(true);

            hook.playBlockDropEffects(block);

            assertTrue(((TestableNexoHook) hook).wasCustomSoundPlayed());
        }

        @Test
        @DisplayName("Given a Nexo custom block without block sounds, when playBlockDropEffects is called, then plays vanilla effects")
        void playBlockDropEffects_playsVanillaEffects_whenCustomBlockWithoutSounds() {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            Location clonedLocation = mock(Location.class);
            when(block.getLocation()).thenReturn(location);
            when(location.clone()).thenReturn(clonedLocation);
            when(clonedLocation.add(0.5, 0.5, 0.5)).thenReturn(clonedLocation);
            World world = mock(World.class);
            when(block.getWorld()).thenReturn(world);
            BlockData blockData = mock(BlockData.class);
            when(block.getBlockData()).thenReturn(blockData);
            SoundGroup soundGroup = mock(SoundGroup.class);
            when(block.getBlockSoundGroup()).thenReturn(soundGroup);
            Sound breakSound = Sound.BLOCK_STONE_BREAK;
            when(soundGroup.getBreakSound()).thenReturn(breakSound);
            when(soundGroup.getVolume()).thenReturn(1.0f);
            when(soundGroup.getPitch()).thenReturn(1.0f);

            when(hook.isCustomBlock(block)).thenReturn(true);

            CustomBlockMechanic mockMechanic = mock(CustomBlockMechanic.class);
            ((TestableNexoHook) hook).setMockMechanic(mockMechanic);
            ((TestableNexoHook) hook).setCustomSoundResult(false);

            hook.playBlockDropEffects(block);

            verify(world).playSound(location, breakSound, 1.0f, 1.0f);
            verify(world).spawnParticle(eq(Particle.BLOCK), eq(clonedLocation), eq(20), eq(0.25), eq(0.25), eq(0.25), eq(0.05), eq(blockData));
        }

        @Test
        @DisplayName("Given a Nexo custom block with null mechanic, when playBlockDropEffects is called, then plays vanilla effects")
        void playBlockDropEffects_playsVanillaEffects_whenNullMechanic() {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            Location clonedLocation = mock(Location.class);
            when(block.getLocation()).thenReturn(location);
            when(location.clone()).thenReturn(clonedLocation);
            when(clonedLocation.add(0.5, 0.5, 0.5)).thenReturn(clonedLocation);
            World world = mock(World.class);
            when(block.getWorld()).thenReturn(world);
            BlockData blockData = mock(BlockData.class);
            when(block.getBlockData()).thenReturn(blockData);
            SoundGroup soundGroup = mock(SoundGroup.class);
            when(block.getBlockSoundGroup()).thenReturn(soundGroup);
            Sound breakSound = Sound.BLOCK_STONE_BREAK;
            when(soundGroup.getBreakSound()).thenReturn(breakSound);
            when(soundGroup.getVolume()).thenReturn(1.0f);
            when(soundGroup.getPitch()).thenReturn(1.0f);

            when(hook.isCustomBlock(block)).thenReturn(true);
            ((TestableNexoHook) hook).setMockMechanic(null);

            hook.playBlockDropEffects(block);

            verify(world).playSound(location, breakSound, 1.0f, 1.0f);
            verify(world).spawnParticle(eq(Particle.BLOCK), eq(clonedLocation), eq(20), eq(0.25), eq(0.25), eq(0.25), eq(0.05), eq(blockData));
        }

        @Test
        @DisplayName("Given a vanilla block, when playBlockDropEffects is called, then plays vanilla sound and particle effects")
        void playBlockDropEffects_playsVanillaEffects_whenVanillaBlock() {
            Block block = mock(Block.class);
            Location location = mock(Location.class);
            Location clonedLocation = mock(Location.class);
            when(block.getLocation()).thenReturn(location);
            when(location.clone()).thenReturn(clonedLocation);
            when(clonedLocation.add(0.5, 0.5, 0.5)).thenReturn(clonedLocation);
            World world = mock(World.class);
            when(block.getWorld()).thenReturn(world);
            BlockData blockData = mock(BlockData.class);
            when(block.getBlockData()).thenReturn(blockData);
            SoundGroup soundGroup = mock(SoundGroup.class);
            when(block.getBlockSoundGroup()).thenReturn(soundGroup);
            Sound breakSound = Sound.BLOCK_STONE_BREAK;
            when(soundGroup.getBreakSound()).thenReturn(breakSound);
            when(soundGroup.getVolume()).thenReturn(1.0f);
            when(soundGroup.getPitch()).thenReturn(1.0f);

            when(hook.isCustomBlock(block)).thenReturn(false);

            hook.playBlockDropEffects(block);

            verify(world).playSound(location, breakSound, 1.0f, 1.0f);
            verify(world).spawnParticle(eq(Particle.BLOCK), eq(clonedLocation), eq(20), eq(0.25), eq(0.25), eq(0.25), eq(0.05), eq(blockData));
        }
    }

    @Nested
    @DisplayName("itemName")
    class ItemName {

        @Test
        @DisplayName("Given a custom item with a resolved Nexo name, when itemName is called, then returns the resolved name")
        void itemName_returnsResolvedName_whenNexoNameExists() {
            when(hook.isItem("magic_sword")).thenReturn(true);
            ((TestableNexoHook) hook).setResolvedItemName("Magic Sword");

            CustomItemWrapper wrapper = new TestCustomItemWrapper("magic_sword");

            assertEquals("Magic Sword", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a custom item where resolveNexoItemName returns null, when itemName is called, then returns formatted ID")
        void itemName_returnsFormattedId_whenNexoNameIsNull() {
            when(hook.isItem("my_namespace:cool_item")).thenReturn(true);
            ((TestableNexoHook) hook).setResolvedItemName(null);

            CustomItemWrapper wrapper = new TestCustomItemWrapper("my_namespace:cool_item");

            assertEquals("Cool Item", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a custom item that is not a valid Nexo item, when itemName is called, then returns formatted block ID")
        void itemName_returnsFormattedId_whenNotAValidNexoItem() {
            when(hook.isItem("unknown_item")).thenReturn(false);

            CustomItemWrapper wrapper = new TestCustomItemWrapper("unknown_item");

            assertEquals("Unknown Item", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a vanilla material wrapper, when itemName is called, then returns formatted material name")
        void itemName_returnsFormattedMaterial_whenVanillaMaterial() {
            CustomItemWrapper wrapper = new CustomItemWrapper(Material.OAK_LOG);

            assertEquals("Oak Log", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a single-word custom item ID with no Nexo name, when itemName is called, then returns title-cased word")
        void itemName_returnsTitleCase_whenSingleWordId() {
            when(hook.isItem("sword")).thenReturn(true);
            ((TestableNexoHook) hook).setResolvedItemName(null);

            CustomItemWrapper wrapper = new TestCustomItemWrapper("sword");

            assertEquals("Sword", hook.itemName(wrapper));
        }

        @Test
        @DisplayName("Given a custom item with a namespaced ID and resolved name, when itemName is called, then returns the name")
        void itemName_returnsResolvedName_whenNamespacedIdHasName() {
            when(hook.isItem("nexo:enchanted_blade")).thenReturn(true);
            ((TestableNexoHook) hook).setResolvedItemName("Enchanted Blade");

            CustomItemWrapper wrapper = new TestCustomItemWrapper("nexo:enchanted_blade");

            assertEquals("Enchanted Blade", hook.itemName(wrapper));
        }
    }

    @Nested
    @DisplayName("blockName")
    class BlockName {

        @Test
        @DisplayName("Given a custom block with a resolved Nexo name, when blockName is called, then returns the resolved name")
        void blockName_returnsResolvedName_whenNexoNameExists() {
            when(hook.isCustomBlock("custom_ore")).thenReturn(true);
            ((TestableNexoHook) hook).setResolvedItemName("Custom Ore");

            CustomBlockWrapper wrapper = new TestCustomBlockWrapper("custom_ore");

            assertEquals("Custom Ore", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a custom block where resolveNexoItemName returns null, when blockName is called, then returns formatted ID")
        void blockName_returnsFormattedId_whenNexoNameIsNull() {
            when(hook.isCustomBlock("my_namespace:cool_block")).thenReturn(true);
            ((TestableNexoHook) hook).setResolvedItemName(null);

            CustomBlockWrapper wrapper = new TestCustomBlockWrapper("my_namespace:cool_block");

            assertEquals("Cool Block", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a custom block that is not a valid Nexo block, when blockName is called, then returns formatted block ID")
        void blockName_returnsFormattedId_whenNotAValidNexoBlock() {
            when(hook.isCustomBlock("unknown_block")).thenReturn(false);

            CustomBlockWrapper wrapper = new TestCustomBlockWrapper("unknown_block");

            assertEquals("Unknown Block", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a vanilla material block wrapper, when blockName is called, then returns formatted material name")
        void blockName_returnsFormattedMaterial_whenVanillaMaterial() {
            CustomBlockWrapper wrapper = new TestVanillaBlockWrapper(Material.OAK_LOG);

            assertEquals("Oak Log", hook.blockName(wrapper));
        }

        @Test
        @DisplayName("Given a custom block with a resolved name and namespaced ID, when blockName is called, then returns the name")
        void blockName_returnsResolvedName_whenNamespacedIdHasName() {
            when(hook.isCustomBlock("nexo:glowing_ore")).thenReturn(true);
            ((TestableNexoHook) hook).setResolvedItemName("Glowing Ore");

            CustomBlockWrapper wrapper = new TestCustomBlockWrapper("nexo:glowing_ore");

            assertEquals("Glowing Ore", hook.blockName(wrapper));
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
            return Optional.ofNullable(testCustomItem);
        }

        @NotNull
        @Override
        public Optional<Material> material() {
            return Optional.empty();
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
            return Optional.ofNullable(testCustomBlock);
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

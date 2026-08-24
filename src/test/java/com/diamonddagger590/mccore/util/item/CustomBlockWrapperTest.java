package com.diamonddagger590.mccore.util.item;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.builder.item.ItemPluginType;
import com.diamonddagger590.mccore.external.common.CustomBlockHook;
import com.diamonddagger590.mccore.external.common.CustomItemHook;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundGroup;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
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
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomBlockWrapperTest {

    @BeforeEach
    void setUp() {
        RegistryResetExtension.setupRegistry();
    }

    @AfterEach
    void tearDown() {
        RegistryResetExtension.resetRegistry();
    }

    private static CustomBlockWrapper materialWrapper(Material material) {
        return new CustomBlockWrapper(material);
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

        TestCustomBlockPluginHook() {
            super(null);
        }

        @Override
        public boolean isCustomBlock(@NotNull Block block) {
            return false;
        }

        @Override
        public boolean isCustomBlock(@NotNull String customBlock) {
            return "nexo:ruby_ore".equals(customBlock);
        }

        @Override
        public boolean isCustomBlockOfType(@NotNull Block block, @NotNull String customBlockType) {
            return false;
        }

        @Override
        public void placeCustomBlock(@NotNull Location location, @NotNull String blockId) {
        }

        @NotNull
        @Override
        public List<ItemStack> drops(@NotNull Block block, @NotNull ItemStack itemToBreakWith, @Nullable Entity entityBreaking) {
            return List.of();
        }

        @Override
        public void playBlockDropEffects(@NotNull Block block) {
        }

        @Override
        public void removeBlock(@NotNull Block block) {
        }

        @NotNull
        @Override
        public Optional<Set<String>> blockModels(@NotNull Block block) {
            return Optional.empty();
        }

        @NotNull
        @Override
        public String blockName(@NotNull CustomBlockWrapper customBlockWrapper) {
            return "Ruby Ore";
        }
    }

    @Nested
    @DisplayName("Material constructor")
    class MaterialConstructor {

        @Test
        @DisplayName("Given a material, when getting material, then returns that material")
        void material_returnsProvidedMaterial_whenConstructedWithMaterial() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.STONE, wrapper.material().get());
        }

        @Test
        @DisplayName("Given a material, when getting customBlock, then returns empty")
        void customBlock_returnsEmpty_whenConstructedWithMaterial() {
            CustomBlockWrapper wrapper = materialWrapper(Material.DIAMOND_ORE);
            assertFalse(wrapper.customBlock().isPresent());
        }
    }

    @Nested
    @DisplayName("Custom block accessors")
    class CustomBlockAccessors {

        @Test
        @DisplayName("Given a custom block wrapper, when getting customBlock, then returns the custom block id")
        void customBlock_returnsId_whenConstructedWithCustomBlock() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertTrue(wrapper.customBlock().isPresent());
            assertEquals("nexo:ruby_ore", wrapper.customBlock().get());
        }

        @Test
        @DisplayName("Given a custom block wrapper, when getting material, then returns empty")
        void material_returnsEmpty_whenConstructedWithCustomBlock() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertFalse(wrapper.material().isPresent());
        }
    }

    @Nested
    @DisplayName("isVanilla")
    class IsVanilla {

        @Test
        @DisplayName("Material wrapper returns true")
        void isVanilla_returnsTrue_whenMaterialWrapper() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            assertTrue(wrapper.isVanilla());
        }

        @Test
        @DisplayName("Custom block wrapper returns false")
        void isVanilla_returnsFalse_whenCustomBlockWrapper() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertFalse(wrapper.isVanilla());
        }
    }

    @Nested
    @DisplayName("isCustom")
    class IsCustom {

        @Test
        @DisplayName("Custom block wrapper returns true")
        void isCustom_returnsTrue_whenCustomBlockWrapper() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertTrue(wrapper.isCustom());
        }

        @Test
        @DisplayName("Material wrapper returns false")
        void isCustom_returnsFalse_whenMaterialWrapper() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            assertFalse(wrapper.isCustom());
        }
    }

    @Nested
    @DisplayName("equals(Material)")
    class EqualsMaterial {

        @Test
        @DisplayName("Given matching material, when comparing, then returns true")
        void equals_returnsTrue_whenMaterialMatches() {
            CustomBlockWrapper wrapper = materialWrapper(Material.IRON_ORE);
            assertTrue(wrapper.equals(Material.IRON_ORE));
        }

        @Test
        @DisplayName("Given different material, when comparing, then returns false")
        void equals_returnsFalse_whenMaterialDiffers() {
            CustomBlockWrapper wrapper = materialWrapper(Material.IRON_ORE);
            assertFalse(wrapper.equals(Material.GOLD_ORE));
        }

        @Test
        @DisplayName("Given custom block wrapper, when comparing with any material, then returns false")
        void equals_returnsFalse_whenWrapperIsCustomBlock() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertFalse(wrapper.equals(Material.DIAMOND_ORE));
        }
    }

    @Nested
    @DisplayName("equals(String)")
    class EqualsString {

        @Test
        @DisplayName("Given matching custom block id, when comparing, then returns true")
        void equals_returnsTrue_whenCustomBlockMatches() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertTrue(wrapper.equals("nexo:ruby_ore"));
        }

        @Test
        @DisplayName("Given different custom block id, when comparing, then returns false")
        void equals_returnsFalse_whenCustomBlockDiffers() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertFalse(wrapper.equals("nexo:sapphire_ore"));
        }

        @Test
        @DisplayName("Given material wrapper, when comparing with any string, then returns false")
        void equals_returnsFalse_whenWrapperIsMaterial() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            assertFalse(wrapper.equals("stone"));
        }
    }

    @Nested
    @DisplayName("equals(CustomItemWrapper)")
    class EqualsCustomItemWrapper {

        @Test
        @DisplayName("Given block and item wrappers with same material, when comparing, then returns true")
        void equals_returnsTrue_whenBothMaterialsMatch() {
            CustomBlockWrapper blockWrapper = materialWrapper(Material.STONE);
            CustomItemWrapper itemWrapper = new CustomItemWrapper(Material.STONE);
            assertTrue(blockWrapper.equals(itemWrapper));
        }

        @Test
        @DisplayName("Given block and item wrappers with different materials, when comparing, then returns false")
        void equals_returnsFalse_whenMaterialsDiffer() {
            CustomBlockWrapper blockWrapper = materialWrapper(Material.STONE);
            CustomItemWrapper itemWrapper = new CustomItemWrapper(Material.DIRT);
            assertFalse(blockWrapper.equals(itemWrapper));
        }

        @Test
        @DisplayName("Given custom block and custom item wrappers with same id, when comparing, then returns true")
        void equals_returnsTrue_whenBothCustomIdsMatch() throws Exception {
            CustomBlockWrapper blockWrapper = customBlockWrapper("nexo:ruby_ore");
            Field materialField = CustomItemWrapper.class.getDeclaredField("material");
            materialField.setAccessible(true);
            Field customItemField = CustomItemWrapper.class.getDeclaredField("customItem");
            customItemField.setAccessible(true);
            CustomItemWrapper itemWrapper = new CustomItemWrapper(Material.AIR);
            materialField.set(itemWrapper, null);
            customItemField.set(itemWrapper, "nexo:ruby_ore");
            assertTrue(blockWrapper.equals(itemWrapper));
        }

        @Test
        @DisplayName("Given custom block and material item wrapper, when comparing, then returns false")
        void equals_returnsFalse_whenTypesAreMixed() throws Exception {
            CustomBlockWrapper blockWrapper = customBlockWrapper("nexo:ruby_ore");
            CustomItemWrapper itemWrapper = new CustomItemWrapper(Material.STONE);
            assertFalse(blockWrapper.equals(itemWrapper));
        }
    }

    @Nested
    @DisplayName("equals(Object)")
    class EqualsObject {

        @Test
        @DisplayName("Given same instance, when comparing, then returns true")
        void equals_returnsTrue_whenSameInstance() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            assertEquals(wrapper, wrapper);
        }

        @Test
        @DisplayName("Given null, when comparing, then returns false")
        void equals_returnsFalse_whenComparedWithNull() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            assertNotEquals(null, wrapper);
        }

        @Test
        @DisplayName("Given two material wrappers with same material, when comparing as Object, then returns true")
        void equals_returnsTrue_whenMaterialWrappersMatch() {
            CustomBlockWrapper a = materialWrapper(Material.STONE);
            CustomBlockWrapper b = materialWrapper(Material.STONE);
            assertEquals(a, b);
        }

        @Test
        @DisplayName("Given two custom block wrappers with same id, when comparing as Object, then returns true")
        void equals_returnsTrue_whenCustomBlockWrappersMatch() throws Exception {
            CustomBlockWrapper a = customBlockWrapper("nexo:ruby_ore");
            CustomBlockWrapper b = customBlockWrapper("nexo:ruby_ore");
            assertEquals(a, b);
        }

        @Test
        @DisplayName("Given block wrapper and different type, when comparing, then returns false")
        void equals_returnsFalse_whenComparedWithDifferentType() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            assertNotEquals(wrapper, "not a wrapper");
        }

        @Test
        @DisplayName("Given one material and one custom wrapper, when comparing as Object, then returns false")
        void equals_returnsFalse_whenOneIsMaterialAndOtherIsCustom() throws Exception {
            CustomBlockWrapper a = materialWrapper(Material.STONE);
            CustomBlockWrapper b = customBlockWrapper("nexo:stone");
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("Given two different material wrappers, when comparing as Object, then returns false")
        void equals_returnsFalse_whenMaterialsDiffer() {
            CustomBlockWrapper a = materialWrapper(Material.STONE);
            CustomBlockWrapper b = materialWrapper(Material.DIRT);
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("Given two different custom block wrappers, when comparing as Object, then returns false")
        void equals_returnsFalse_whenCustomBlocksDiffer() throws Exception {
            CustomBlockWrapper a = customBlockWrapper("nexo:ruby_ore");
            CustomBlockWrapper b = customBlockWrapper("nexo:sapphire_ore");
            assertNotEquals(a, b);
        }
    }

    @Nested
    @DisplayName("hashCode")
    class HashCode {

        @Test
        @DisplayName("Given two equal material wrappers, when getting hashCode, then values are equal")
        void hashCode_returnsEqualValues_whenMaterialWrappersAreEqual() {
            CustomBlockWrapper a = materialWrapper(Material.STONE);
            CustomBlockWrapper b = materialWrapper(Material.STONE);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Given two equal custom block wrappers, when getting hashCode, then values are equal")
        void hashCode_returnsEqualValues_whenCustomBlockWrappersAreEqual() throws Exception {
            CustomBlockWrapper a = customBlockWrapper("nexo:ruby_ore");
            CustomBlockWrapper b = customBlockWrapper("nexo:ruby_ore");
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Given material wrapper, when getting hashCode multiple times, then value is consistent")
        void hashCode_returnsSameValue_whenCalledMultipleTimes() {
            CustomBlockWrapper wrapper = materialWrapper(Material.DIAMOND_ORE);
            int first = wrapper.hashCode();
            int second = wrapper.hashCode();
            assertEquals(first, second);
        }

        @Test
        @DisplayName("Given two unequal material wrappers, when getting hashCode, then values differ")
        void hashCode_returnsDifferentValues_whenMaterialWrappersDiffer() {
            CustomBlockWrapper a = materialWrapper(Material.STONE);
            CustomBlockWrapper b = materialWrapper(Material.DIAMOND_ORE);
            assertNotEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Given two unequal custom block wrappers, when getting hashCode, then values differ")
        void hashCode_returnsDifferentValues_whenCustomBlockWrappersDiffer() throws Exception {
            CustomBlockWrapper a = customBlockWrapper("nexo:ruby_ore");
            CustomBlockWrapper b = customBlockWrapper("nexo:sapphire_ore");
            assertNotEquals(a.hashCode(), b.hashCode());
        }
    }

    @Nested
    @DisplayName("blockName")
    class BlockName {

        @Test
        @DisplayName("Given custom block wrapper with no hooks registered, when getting blockName, then returns Missing Block")
        void blockName_returnsMissingBlock_whenNoHooksRegistered() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertEquals("Missing Block", wrapper.blockName());
        }

        @Test
        @DisplayName("Given custom block wrapper with a hook registered, when getting blockName, then returns hook-provided name")
        void blockName_returnsHookProvidedName_whenHookIsRegistered() throws Exception {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(new TestCustomBlockPluginHook());
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertEquals("Ruby Ore", wrapper.blockName());
        }

        @Test
        @DisplayName("Given vanilla material wrapper, when getting blockName, then returns lang tag")
        void blockName_returnsLangTag_whenVanillaMaterial() {
            MockBukkit.mock();
            try {
                CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
                String name = wrapper.blockName();
                assertTrue(name.startsWith("<lang:"), "Expected lang tag but got: " + name);
                assertTrue(name.endsWith(">"), "Expected lang tag to end with > but got: " + name);
            } finally {
                MockBukkit.unmock();
            }
        }

        @Test
        @DisplayName("Given custom block with hook that doesn't recognize it, when getting blockName, then returns Missing Block")
        void blockName_returnsMissingBlock_whenHookDoesNotRecognize() throws Exception {
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(new TestCustomBlockPluginHook());
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:unknown_block");
            assertEquals("Missing Block", wrapper.blockName());
        }
    }

    @Nested
    @DisplayName("Constructor(Block)")
    class BlockConstructor {

        @Test
        @DisplayName("Given a block with no hooks registered, when constructing, then uses block material")
        void constructor_usesMaterial_whenNoHooksRegistered() {
            Block mockBlock = mock(Block.class);
            when(mockBlock.getType()).thenReturn(Material.IRON_ORE);

            CustomBlockWrapper wrapper = new CustomBlockWrapper(mockBlock);

            assertTrue(wrapper.isVanilla());
            assertFalse(wrapper.isCustom());
            assertTrue(wrapper.material().isPresent());
            assertEquals(Material.IRON_ORE, wrapper.material().get());
        }

        @Test
        @DisplayName("Given a block with hook that recognizes it and returns models, when constructing, then uses custom block")
        void constructor_usesCustomBlock_whenHookReturnsModels() {
            Block mockBlock = mock(Block.class);
            ConfigurableBlockHook hook = new ConfigurableBlockHook(true, Optional.of(Set.of("nexo:ruby_ore")));
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            CustomBlockWrapper wrapper = new CustomBlockWrapper(mockBlock);

            assertTrue(wrapper.isCustom());
            assertFalse(wrapper.isVanilla());
            assertTrue(wrapper.customBlock().isPresent());
            assertEquals("nexo:ruby_ore", wrapper.customBlock().get());
        }

        @Test
        @DisplayName("Given a block with hook that recognizes it but returns empty models, when constructing, then uses block material")
        void constructor_usesMaterial_whenHookReturnsEmptyModels() {
            Block mockBlock = mock(Block.class);
            when(mockBlock.getType()).thenReturn(Material.STONE);
            ConfigurableBlockHook hook = new ConfigurableBlockHook(true, Optional.of(Set.of()));
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            CustomBlockWrapper wrapper = new CustomBlockWrapper(mockBlock);

            assertTrue(wrapper.isVanilla());
            assertEquals(Material.STONE, wrapper.material().get());
        }

        @Test
        @DisplayName("Given a block with hook that does not recognize it, when constructing, then uses block material")
        void constructor_usesMaterial_whenHookDoesNotRecognize() {
            Block mockBlock = mock(Block.class);
            when(mockBlock.getType()).thenReturn(Material.DIRT);
            ConfigurableBlockHook hook = new ConfigurableBlockHook(false, Optional.empty());
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            CustomBlockWrapper wrapper = new CustomBlockWrapper(mockBlock);

            assertTrue(wrapper.isVanilla());
            assertEquals(Material.DIRT, wrapper.material().get());
        }

        @Test
        @DisplayName("Given a block with hook that returns models with no results optional, when constructing, then uses block material")
        void constructor_usesMaterial_whenHookReturnsEmptyOptional() {
            Block mockBlock = mock(Block.class);
            when(mockBlock.getType()).thenReturn(Material.GOLD_ORE);
            ConfigurableBlockHook hook = new ConfigurableBlockHook(true, Optional.empty());
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            CustomBlockWrapper wrapper = new CustomBlockWrapper(mockBlock);

            assertTrue(wrapper.isVanilla());
            assertEquals(Material.GOLD_ORE, wrapper.material().get());
        }
    }

    @Nested
    @DisplayName("equals(Block)")
    class EqualsBlock {

        @Test
        @DisplayName("Given vanilla wrapper and block with matching material, when comparing, then returns true")
        void equalsBlock_returnsTrue_whenMaterialMatches() {
            Block mockBlock = mock(Block.class);
            when(mockBlock.getType()).thenReturn(Material.STONE);
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);

            assertTrue(wrapper.equals(mockBlock));
        }

        @Test
        @DisplayName("Given vanilla wrapper and block with different material, when comparing, then returns false")
        void equalsBlock_returnsFalse_whenMaterialDiffers() {
            Block mockBlock = mock(Block.class);
            when(mockBlock.getType()).thenReturn(Material.DIRT);
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);

            assertFalse(wrapper.equals(mockBlock));
        }

        @Test
        @DisplayName("Given custom wrapper with hook that confirms type match, when comparing, then returns true")
        void equalsBlock_returnsTrue_whenHookConfirmsMatch() throws Exception {
            Block mockBlock = mock(Block.class);
            ConfigurableBlockHook hook = new ConfigurableBlockHook("nexo:ruby_ore");
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");

            assertTrue(wrapper.equals(mockBlock));
        }

        @Test
        @DisplayName("Given custom wrapper with no hooks, when comparing with block, then returns false")
        void equalsBlock_returnsFalse_whenNoHooksRegistered() throws Exception {
            Block mockBlock = mock(Block.class);
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");

            assertFalse(wrapper.equals(mockBlock));
        }

        @Test
        @DisplayName("Given custom wrapper with hook that denies match, when comparing, then returns false")
        void equalsBlock_returnsFalse_whenHookDeniesMatch() throws Exception {
            Block mockBlock = mock(Block.class);
            ConfigurableBlockHook hook = new ConfigurableBlockHook("nexo:sapphire_ore");
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");

            assertFalse(wrapper.equals(mockBlock));
        }
    }

    @Nested
    @DisplayName("itemBuilder")
    class ItemBuilderTests {

        @BeforeEach
        void setUpMockBukkit() {
            MockBukkit.mock();
            CorePlugin mockPlugin = mock(CorePlugin.class);
            when(mockPlugin.getMiniMessage()).thenReturn(MiniMessage.miniMessage());
            when(mockPlugin.getItemPlugin()).thenReturn(ItemPluginType.NONE);
            corePluginStatic = mockStatic(CorePlugin.class);
            corePluginStatic.when(CorePlugin::getInstance).thenReturn(mockPlugin);
        }

        @AfterEach
        void tearDownMockBukkit() {
            corePluginStatic.close();
            MockBukkit.unmock();
        }

        private MockedStatic<CorePlugin> corePluginStatic;

        @Test
        @DisplayName("Given vanilla material wrapper, when getting itemBuilder, then returns non-null builder")
        void itemBuilder_returnsBuilder_whenVanillaMaterial() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            assertNotNull(wrapper.itemBuilder());
        }

        @Test
        @DisplayName("Given custom block wrapper with no hooks, when getting itemBuilder, then returns AIR-based builder")
        void itemBuilder_returnsAirBuilder_whenNoHooksRegistered() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            assertNotNull(wrapper.itemBuilder());
        }

        @Test
        @DisplayName("Given custom block wrapper with hook providing item, when getting itemBuilder, then returns hook-provided builder")
        void itemBuilder_returnsHookBuilder_whenHookProvidesItem() throws Exception {
            ItemStack customItem = new ItemStack(Material.DIAMOND);
            ConfigurableItemHookForBlock hook = new ConfigurableItemHookForBlock("nexo:ruby_ore", customItem);
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");

            assertNotNull(wrapper.itemBuilder());
        }
    }

    @Nested
    @DisplayName("customModels (static)")
    class CustomModelsTests {

        @Test
        @DisplayName("Given no hooks registered, when getting customModels, then returns empty set in Optional")
        void customModels_returnsEmptySet_whenNoHooksRegistered() {
            Block mockBlock = mock(Block.class);
            Optional<Set<String>> result = CustomBlockWrapper.customModels(mockBlock);

            assertTrue(result.isPresent());
            assertTrue(result.get().isEmpty());
        }

        @Test
        @DisplayName("Given hook that returns models, when getting customModels, then returns those models")
        void customModels_returnsModels_whenHookProvidesModels() {
            Block mockBlock = mock(Block.class);
            ConfigurableBlockHook hook = new ConfigurableBlockHook(false, Optional.of(Set.of("model_a", "model_b")));
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            Optional<Set<String>> result = CustomBlockWrapper.customModels(mockBlock);

            assertTrue(result.isPresent());
            assertEquals(Set.of("model_a", "model_b"), result.get());
        }

        @Test
        @DisplayName("Given hook that returns empty optional, when getting customModels, then returns empty set")
        void customModels_returnsEmptySet_whenHookReturnsEmptyOptional() {
            Block mockBlock = mock(Block.class);
            ConfigurableBlockHook hook = new ConfigurableBlockHook(false, Optional.empty());
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            Optional<Set<String>> result = CustomBlockWrapper.customModels(mockBlock);

            assertTrue(result.isPresent());
            assertTrue(result.get().isEmpty());
        }
    }

    @Nested
    @DisplayName("drops (static)")
    class DropsTests {

        @Test
        @DisplayName("Given hook registered, when getting drops, then returns hook-provided drops")
        void drops_returnsHookDrops_whenHookRegistered() {
            Block mockBlock = mock(Block.class);
            ItemStack tool = mock(ItemStack.class);
            Entity mockEntity = mock(Entity.class);
            ItemStack expectedDrop = mock(ItemStack.class);
            ConfigurableBlockHook hook = new ConfigurableBlockHook(List.of(expectedDrop));
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            List<ItemStack> drops = CustomBlockWrapper.drops(mockBlock, tool, mockEntity);

            assertEquals(1, drops.size());
            assertEquals(expectedDrop, drops.get(0));
        }

        @Test
        @DisplayName("Given no hooks registered, when getting drops, then returns block's vanilla drops")
        void drops_returnsVanillaDrops_whenNoHooksRegistered() {
            Block mockBlock = mock(Block.class);
            ItemStack tool = mock(ItemStack.class);
            Entity mockEntity = mock(Entity.class);
            ItemStack vanillaDrop = mock(ItemStack.class);
            when(mockBlock.getDrops(tool, mockEntity)).thenReturn(List.of(vanillaDrop));

            List<ItemStack> drops = CustomBlockWrapper.drops(mockBlock, tool, mockEntity);

            assertEquals(1, drops.size());
            assertEquals(vanillaDrop, drops.get(0));
        }
    }

    @Nested
    @DisplayName("removeBlock (static)")
    class RemoveBlockTests {

        @Test
        @DisplayName("Given hook registered, when removing block, then hook handles removal")
        void removeBlock_delegatesToHook_whenHookRegistered() {
            Block mockBlock = mock(Block.class);
            TrackingBlockHook hook = new TrackingBlockHook();
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            CustomBlockWrapper.removeBlock(mockBlock);

            assertTrue(hook.removeBlockCalled);
        }

        @Test
        @DisplayName("Given no hooks registered, when removing block, then sets block type to AIR")
        void removeBlock_setsAir_whenNoHooksRegistered() {
            Block mockBlock = mock(Block.class);

            CustomBlockWrapper.removeBlock(mockBlock);

            verify(mockBlock).setType(Material.AIR);
        }
    }

    @Nested
    @DisplayName("playBlockDropEffects (static)")
    class PlayBlockDropEffectsTests {

        @Test
        @DisplayName("Given hook registered, when playing effects, then hook handles effects")
        void playBlockDropEffects_delegatesToHook_whenHookRegistered() {
            Block mockBlock = mock(Block.class);
            TrackingBlockHook hook = new TrackingBlockHook();
            RegistryAccess.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

            CustomBlockWrapper.playBlockDropEffects(mockBlock);

            assertTrue(hook.playEffectsCalled);
        }

        @Test
        @DisplayName("Given no hooks registered, when playing effects, then plays vanilla sound and particle")
        void playBlockDropEffects_playsVanillaEffects_whenNoHooksRegistered() {
            Block mockBlock = mock(Block.class);
            World mockWorld = mock(World.class);
            BlockData mockBlockData = mock(BlockData.class);
            SoundGroup mockSoundGroup = mock(SoundGroup.class);
            Location location = mock(Location.class);
            Location clonedLocation = mock(Location.class);
            Sound breakSound = Sound.BLOCK_STONE_BREAK;

            when(mockBlock.getWorld()).thenReturn(mockWorld);
            when(mockBlock.getBlockData()).thenReturn(mockBlockData);
            when(mockBlock.getBlockSoundGroup()).thenReturn(mockSoundGroup);
            when(mockBlock.getLocation()).thenReturn(location);
            when(location.clone()).thenReturn(clonedLocation);
            when(clonedLocation.add(0.5, 0.5, 0.5)).thenReturn(clonedLocation);
            when(mockSoundGroup.getBreakSound()).thenReturn(breakSound);
            when(mockSoundGroup.getVolume()).thenReturn(1.0f);
            when(mockSoundGroup.getPitch()).thenReturn(1.0f);

            CustomBlockWrapper.playBlockDropEffects(mockBlock);

            verify(mockWorld).playSound(eq(location), eq(breakSound), eq(1.0f), eq(1.0f));
            verify(mockWorld).spawnParticle(
                    eq(Particle.BLOCK),
                    eq(clonedLocation),
                    eq(20),
                    eq(0.25),
                    eq(0.25),
                    eq(0.25),
                    eq(0.05),
                    eq(mockBlockData));
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("Given material wrapper, when calling toString, then contains material name")
        void toString_containsMaterialName_whenMaterialWrapper() {
            CustomBlockWrapper wrapper = materialWrapper(Material.STONE);
            String result = wrapper.toString();
            assertTrue(result.contains("STONE"), "Expected toString to contain STONE, got: " + result);
            assertTrue(result.contains("customBlock=null"), "Expected customBlock=null, got: " + result);
        }

        @Test
        @DisplayName("Given custom block wrapper, when calling toString, then contains custom block id")
        void toString_containsCustomBlockId_whenCustomBlockWrapper() throws Exception {
            CustomBlockWrapper wrapper = customBlockWrapper("nexo:ruby_ore");
            String result = wrapper.toString();
            assertTrue(result.contains("nexo:ruby_ore"), "Expected toString to contain nexo:ruby_ore, got: " + result);
            assertTrue(result.contains("material=null"), "Expected material=null, got: " + result);
        }
    }

    static class ConfigurableBlockHook extends PluginHook<CorePlugin> implements CustomBlockHook {

        private final boolean isCustomBlock;
        private final Optional<Set<String>> blockModels;
        private final String matchingType;
        private final List<ItemStack> drops;

        ConfigurableBlockHook(boolean isCustomBlock, Optional<Set<String>> blockModels) {
            super(null);
            this.isCustomBlock = isCustomBlock;
            this.blockModels = blockModels;
            this.matchingType = null;
            this.drops = List.of();
        }

        ConfigurableBlockHook(String matchingType) {
            super(null);
            this.isCustomBlock = false;
            this.blockModels = Optional.empty();
            this.matchingType = matchingType;
            this.drops = List.of();
        }

        ConfigurableBlockHook(List<ItemStack> drops) {
            super(null);
            this.isCustomBlock = false;
            this.blockModels = Optional.empty();
            this.matchingType = null;
            this.drops = drops;
        }

        @Override
        public boolean isCustomBlock(@NotNull Block block) {
            return isCustomBlock;
        }

        @Override
        public boolean isCustomBlock(@NotNull String customBlock) {
            return false;
        }

        @Override
        public boolean isCustomBlockOfType(@NotNull Block block, @NotNull String customBlockType) {
            return matchingType != null && matchingType.equals(customBlockType);
        }

        @Override
        public void placeCustomBlock(@NotNull Location location, @NotNull String blockId) {
        }

        @NotNull
        @Override
        public List<ItemStack> drops(@NotNull Block block, @NotNull ItemStack itemToBreakWith, @Nullable Entity entityBreaking) {
            return drops;
        }

        @Override
        public void playBlockDropEffects(@NotNull Block block) {
        }

        @Override
        public void removeBlock(@NotNull Block block) {
        }

        @NotNull
        @Override
        public Optional<Set<String>> blockModels(@NotNull Block block) {
            return blockModels;
        }

        @NotNull
        @Override
        public String blockName(@NotNull CustomBlockWrapper customBlockWrapper) {
            return "";
        }
    }

    static class TrackingBlockHook extends PluginHook<CorePlugin> implements CustomBlockHook {

        boolean removeBlockCalled;
        boolean playEffectsCalled;

        TrackingBlockHook() {
            super(null);
        }

        @Override
        public boolean isCustomBlock(@NotNull Block block) {
            return false;
        }

        @Override
        public boolean isCustomBlock(@NotNull String customBlock) {
            return false;
        }

        @Override
        public boolean isCustomBlockOfType(@NotNull Block block, @NotNull String customBlockType) {
            return false;
        }

        @Override
        public void placeCustomBlock(@NotNull Location location, @NotNull String blockId) {
        }

        @NotNull
        @Override
        public List<ItemStack> drops(@NotNull Block block, @NotNull ItemStack itemToBreakWith, @Nullable Entity entityBreaking) {
            return List.of();
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
            return Optional.empty();
        }

        @NotNull
        @Override
        public String blockName(@NotNull CustomBlockWrapper customBlockWrapper) {
            return "";
        }
    }

    static class ConfigurableItemHookForBlock extends PluginHook<CorePlugin> implements CustomItemHook {

        private final String itemName;
        private final ItemStack item;

        ConfigurableItemHookForBlock(String itemName, ItemStack item) {
            super(null);
            this.itemName = itemName;
            this.item = item;
        }

        @Override
        public boolean isItem(@NotNull String name) {
            return itemName.equals(name);
        }

        @Override
        public boolean isItem(@NotNull ItemStack itemStack) {
            return false;
        }

        @Override
        public boolean isItemOfType(@NotNull ItemStack itemStack, @NotNull String name) {
            return false;
        }

        @NotNull
        @Override
        public Optional<ItemStack> item(@NotNull String name) {
            return itemName.equals(name) ? Optional.of(item) : Optional.empty();
        }

        @NotNull
        @Override
        public Optional<Set<String>> itemModels(@NotNull ItemStack itemStack) {
            return Optional.empty();
        }

        @NotNull
        @Override
        public String itemName(@NotNull CustomItemWrapper wrapper) {
            return "";
        }
    }
}

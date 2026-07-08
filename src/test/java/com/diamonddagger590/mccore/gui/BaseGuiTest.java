package com.diamonddagger590.mccore.gui;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.exception.gui.IllegalSlotAssignmentException;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.player.PlayerManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.CoreManagerKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BaseGuiTest {

    private static class TestCorePlayer extends CorePlayer {
        public TestCorePlayer(@NotNull UUID uuid, @NotNull CorePlugin corePlugin) {
            super(uuid, corePlugin);
        }

        @Override
        public boolean useMutex() {
            return false;
        }
    }

    private static class TestGui extends BaseGui<TestCorePlayer> {

        private final Inventory mockInventory;

        public TestGui(@NotNull TestCorePlayer creatingPlayer, @NotNull Inventory mockInventory) {
            super(creatingPlayer);
            this.mockInventory = mockInventory;
        }

        @Override
        protected void buildInventory() {
            this.inventory = mockInventory;
        }

        @Override
        public void paintInventory() {
        }

        @Override
        public void registerListeners() {
        }

        @Override
        public void unregisterListeners() {
        }
    }

    private static class RestrictedSlot implements Slot<TestCorePlayer> {
        private final Set<Class<?>> validTypes;

        RestrictedSlot(Set<Class<?>> validTypes) {
            this.validTypes = validTypes;
        }

        @Override
        public boolean onClick(@NotNull TestCorePlayer corePlayer, @NotNull ClickType clickType) {
            return true;
        }

        @NotNull
        @Override
        public Set<Class<?>> getValidGuiTypes() {
            return validTypes;
        }
    }

    private TestCorePlayer player;
    private Inventory mockInventory;
    private TestGui gui;

    @BeforeEach
    void setUp() {
        CorePlugin plugin = mock(CorePlugin.class);
        UUID uuid = UUID.randomUUID();
        player = new TestCorePlayer(uuid, plugin);
        mockInventory = mock(Inventory.class);
        when(mockInventory.getSize()).thenReturn(54);
        gui = new TestGui(player, mockInventory);
    }

    @Nested
    @DisplayName("Constructor and getters")
    class ConstructorAndGetters {

        @Test
        @DisplayName("Given a CorePlayer, when BaseGui is created, then getUUID returns player UUID")
        void getUUID_returnsPlayerUUID() {
            assertEquals(player.getUUID(), gui.getUUID());
        }

        @Test
        @DisplayName("Given a CorePlayer, when BaseGui is created, then getCreatingPlayer returns the player")
        void getCreatingPlayer_returnsCreatingPlayer() {
            assertSame(player, gui.getCreatingPlayer());
        }
    }

    @Nested
    @DisplayName("getSlot")
    class GetSlot {

        @Test
        @DisplayName("Given no slot registered at index, when getSlot, then returns default slot")
        void returnsDefaultSlot_whenNoSlotRegistered() {
            Slot<TestCorePlayer> slot = gui.getSlot(0);

            assertNotNull(slot);
            assertSame(gui.DEFAULT_SLOT, slot);
        }

        @Test
        @DisplayName("Given default slot, when onClick, then returns false")
        void defaultSlot_onClick_returnsFalse() {
            Slot<TestCorePlayer> defaultSlot = gui.getSlot(0);

            boolean result = defaultSlot.onClick(player, ClickType.LEFT);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("getInventory")
    class GetInventory {

        @Test
        @DisplayName("Given inventory not yet built, when getInventory, then calls buildInventory and returns it")
        void callsBuildInventory_whenInventoryIsNull() {
            Inventory result = gui.getInventory();

            assertNotNull(result);
            assertSame(mockInventory, result);
        }

        @Test
        @DisplayName("Given inventory already built, when getInventory called again, then returns same inventory")
        void returnsSameInventory_onSubsequentCalls() {
            Inventory first = gui.getInventory();
            Inventory second = gui.getInventory();

            assertSame(first, second);
        }
    }

    @Nested
    @DisplayName("canSlotBelongToGui")
    class CanSlotBelongToGui {

        @Test
        @DisplayName("Given slot with empty valid gui types, when canSlotBelongToGui, then returns true")
        void returnsTrue_whenSlotHasNoRestrictions() {
            Slot<TestCorePlayer> unrestricted = new Slot<>() {
                @Override
                public boolean onClick(@NotNull TestCorePlayer corePlayer, @NotNull ClickType clickType) {
                    return false;
                }
            };

            assertTrue(gui.canSlotBelongToGui(unrestricted));
        }

        @Test
        @DisplayName("Given slot restricted to this gui type, when canSlotBelongToGui, then returns true")
        void returnsTrue_whenSlotAllowsThisGuiType() {
            Set<Class<?>> validTypes = new HashSet<>();
            validTypes.add(TestGui.class);
            RestrictedSlot slot = new RestrictedSlot(validTypes);

            assertTrue(gui.canSlotBelongToGui(slot));
        }

        @Test
        @DisplayName("Given slot restricted to parent type, when canSlotBelongToGui, then returns true via instanceof")
        void returnsTrue_whenSlotAllowsParentType() {
            Set<Class<?>> validTypes = new HashSet<>();
            validTypes.add(BaseGui.class);
            RestrictedSlot slot = new RestrictedSlot(validTypes);

            assertTrue(gui.canSlotBelongToGui(slot));
        }

        @Test
        @DisplayName("Given slot restricted to different gui type, when canSlotBelongToGui, then returns false")
        void returnsFalse_whenSlotDoesNotAllowThisGuiType() {
            Set<Class<?>> validTypes = new HashSet<>();
            validTypes.add(PaginatedGui.class);
            RestrictedSlot slot = new RestrictedSlot(validTypes);

            assertFalse(gui.canSlotBelongToGui(slot));
        }
    }

    @Nested
    @DisplayName("allowBottomInventoryClick")
    class AllowBottomInventoryClick {

        @Test
        @DisplayName("Given default BaseGui, when allowBottomInventoryClick, then returns false")
        void returnsFalse_byDefault() {
            assertFalse(gui.allowBottomInventoryClick());
        }
    }

    @Nested
    @DisplayName("removeSlot")
    class RemoveSlot {

        @Test
        @DisplayName("Given inventory is built, when removeSlot, then clears the inventory at that index")
        void clearsInventoryAtIndex() {
            gui.getInventory();

            gui.removeSlot(5);

            org.mockito.Mockito.verify(mockInventory).clear(5);
        }

        @Test
        @DisplayName("Given a slot was registered, when removeSlot, then getSlot returns default")
        void getSlotReturnsDefault_afterRemoval() throws Exception {
            gui.getInventory();
            Slot<TestCorePlayer> customSlot = new RestrictedSlot(new HashSet<>());
            java.lang.reflect.Field slotsField = BaseGui.class.getDeclaredField("slots");
            slotsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<Integer, Slot<TestCorePlayer>> slots =
                    (java.util.Map<Integer, Slot<TestCorePlayer>>) slotsField.get(gui);
            slots.put(0, customSlot);
            assertSame(customSlot, gui.getSlot(0));

            gui.removeSlot(0);

            assertSame(gui.DEFAULT_SLOT, gui.getSlot(0));
        }
    }

    @Nested
    @DisplayName("setSlot with invalid slot type")
    class SetSlotInvalid {

        @Test
        @DisplayName("Given slot restricted to wrong gui type, when setSlot, then throws IllegalSlotAssignmentException")
        void throwsIllegalSlotAssignment_whenSlotTypeIsWrong() {
            gui.getInventory();
            Set<Class<?>> validTypes = new HashSet<>();
            validTypes.add(PaginatedGui.class);
            RestrictedSlot restrictedSlot = new RestrictedSlot(validTypes);

            assertThrows(IllegalSlotAssignmentException.class, () -> gui.setSlot(0, restrictedSlot));
        }
    }

    @Nested
    @DisplayName("setSlot success path")
    class SetSlotSuccess {

        @Test
        @DisplayName("Given valid unrestricted slot with online player, when setSlot at valid index, then stores slot and sets item in inventory")
        void setSlot_storesSlotAndSetsItem_whenValid() {
            gui.getInventory();

            Player mockBukkitPlayer = mock(Player.class);
            when(mockBukkitPlayer.getUniqueId()).thenReturn(player.getUUID());

            ItemBuilder mockItemBuilder = mock(ItemBuilder.class);
            ItemStack mockItemStack = mock(ItemStack.class);
            when(mockItemBuilder.asItemStack(any())).thenReturn(mockItemStack);

            Slot<TestCorePlayer> slot = new Slot<>() {
                @Override
                public boolean onClick(@NotNull TestCorePlayer corePlayer, @NotNull ClickType clickType) {
                    return true;
                }

                @Override
                @NotNull
                public ItemBuilder getItem(@NotNull TestCorePlayer corePlayer) {
                    return mockItemBuilder;
                }
            };

            try (MockedStatic<org.bukkit.Bukkit> bukkitStatic = mockStatic(org.bukkit.Bukkit.class)) {
                bukkitStatic.when(() -> org.bukkit.Bukkit.getPlayer(player.getUUID())).thenReturn(mockBukkitPlayer);

                gui.setSlot(0, slot);

                assertSame(slot, gui.getSlot(0));
                verify(mockInventory).setItem(eq(0), eq(mockItemStack));
            }
        }

        @Test
        @DisplayName("Given null inventory, when setSlot, then throws NullPointerException")
        void setSlot_throwsNPE_whenInventoryIsNull() {
            Slot<TestCorePlayer> slot = new Slot<>() {
                @Override
                public boolean onClick(@NotNull TestCorePlayer corePlayer, @NotNull ClickType clickType) {
                    return false;
                }
            };

            assertThrows(NullPointerException.class, () -> gui.setSlot(0, slot));
        }
    }

    @Nested
    @DisplayName("canProcessEvent")
    class CanProcessEvent {

        private MockedStatic<CorePlugin> corePluginStatic;
        private GuiManager<TestCorePlayer, CorePlugin> mockGuiManager;

        @SuppressWarnings("unchecked")
        @BeforeEach
        void setUpStatic() {
            RegistryResetExtension.setupRegistry();

            mockGuiManager = mock(GuiManager.class);

            ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
            managerRegistry.register(mockGuiManager);

            CorePlugin mockPlugin = mock(CorePlugin.class);
            when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());

            corePluginStatic = mockStatic(CorePlugin.class);
            corePluginStatic.when(CorePlugin::getInstance).thenReturn(mockPlugin);
        }

        @AfterEach
        void tearDownStatic() {
            corePluginStatic.close();
            RegistryResetExtension.resetRegistry();
        }

        @Test
        @DisplayName("Given player has this gui open and inventory matches, when canProcessEvent, then returns true")
        void canProcessEvent_returnsTrue_whenPlayerHasThisGuiOpenAndInventoryMatches() {
            gui.getInventory();
            Player mockBukkitPlayer = mock(Player.class);
            when(mockBukkitPlayer.getUniqueId()).thenReturn(player.getUUID());
            when(mockGuiManager.getOpenedGui(mockBukkitPlayer)).thenReturn(Optional.of(gui));

            boolean result = gui.canProcessEvent(mockBukkitPlayer, mockInventory);

            assertTrue(result);
        }

        @Test
        @DisplayName("Given player has different gui open, when canProcessEvent, then returns false")
        void canProcessEvent_returnsFalse_whenPlayerHasDifferentGuiOpen() {
            gui.getInventory();
            Player mockBukkitPlayer = mock(Player.class);
            when(mockBukkitPlayer.getUniqueId()).thenReturn(player.getUUID());
            TestGui otherGui = new TestGui(player, mock(Inventory.class));
            when(mockGuiManager.getOpenedGui(mockBukkitPlayer)).thenReturn(Optional.of(otherGui));

            boolean result = gui.canProcessEvent(mockBukkitPlayer, mockInventory);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given player has no gui open, when canProcessEvent, then returns false")
        void canProcessEvent_returnsFalse_whenPlayerHasNoGuiOpen() {
            gui.getInventory();
            Player mockBukkitPlayer = mock(Player.class);
            when(mockBukkitPlayer.getUniqueId()).thenReturn(player.getUUID());
            when(mockGuiManager.getOpenedGui(mockBukkitPlayer)).thenReturn(Optional.empty());

            boolean result = gui.canProcessEvent(mockBukkitPlayer, mockInventory);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given wrong inventory, when canProcessEvent, then returns false")
        void canProcessEvent_returnsFalse_whenInventoryDoesNotMatch() {
            gui.getInventory();
            Player mockBukkitPlayer = mock(Player.class);
            when(mockBukkitPlayer.getUniqueId()).thenReturn(player.getUUID());
            Inventory differentInventory = mock(Inventory.class);

            boolean result = gui.canProcessEvent(mockBukkitPlayer, differentInventory);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("handleClickEvent")
    class HandleClickEvent {

        private MockedStatic<CorePlugin> corePluginStatic;
        @SuppressWarnings("rawtypes")
        private GuiManager mockGuiManager;
        @SuppressWarnings("rawtypes")
        private PlayerManager mockPlayerManager;

        @SuppressWarnings("unchecked")
        @BeforeEach
        void setUpStatic() {
            RegistryResetExtension.setupRegistry();

            mockGuiManager = mock(GuiManager.class);
            mockPlayerManager = mock(PlayerManager.class);

            ManagerRegistry managerRegistry = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER);
            managerRegistry.register(mockGuiManager);
            managerRegistry.register(mockPlayerManager);

            CorePlugin mockPlugin = mock(CorePlugin.class);
            when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());

            corePluginStatic = mockStatic(CorePlugin.class);
            corePluginStatic.when(CorePlugin::getInstance).thenReturn(mockPlugin);
        }

        @AfterEach
        void tearDownStatic() {
            corePluginStatic.close();
            RegistryResetExtension.resetRegistry();
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Given bottom inventory clicked and bottom click not allowed, when handleClickEvent, then cancels event")
        void handleClickEvent_cancelsEvent_whenBottomInventoryClickedAndNotAllowed() {
            gui.getInventory();
            Player mockBukkitPlayer = mock(Player.class);
            when(mockBukkitPlayer.getUniqueId()).thenReturn(player.getUUID());
            when(mockGuiManager.getOpenedGui(mockBukkitPlayer)).thenReturn(Optional.of(gui));
            when(mockPlayerManager.getPlayer(player.getUUID())).thenReturn(Optional.of(player));

            InventoryClickEvent event = mock(InventoryClickEvent.class);
            when(event.getSlot()).thenReturn(0);
            when(event.getWhoClicked()).thenReturn(mockBukkitPlayer);

            InventoryView mockView = mock(InventoryView.class);
            when(event.getView()).thenReturn(mockView);
            when(mockView.getTopInventory()).thenReturn(mockInventory);

            Inventory bottomInventory = mock(Inventory.class);
            when(mockView.getBottomInventory()).thenReturn(bottomInventory);
            when(event.getClickedInventory()).thenReturn(bottomInventory);

            gui.handleClickEvent(event);

            verify(event).setCancelled(true);
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Given canProcessEvent returns false, when handleClickEvent, then does not cancel event")
        void handleClickEvent_doesNotCancel_whenCanProcessEventReturnsFalse() {
            gui.getInventory();
            Player mockBukkitPlayer = mock(Player.class);
            when(mockBukkitPlayer.getUniqueId()).thenReturn(player.getUUID());
            when(mockGuiManager.getOpenedGui(mockBukkitPlayer)).thenReturn(Optional.empty());
            when(mockPlayerManager.getPlayer(player.getUUID())).thenReturn(Optional.of(player));

            InventoryClickEvent event = mock(InventoryClickEvent.class);
            when(event.getSlot()).thenReturn(0);
            when(event.getWhoClicked()).thenReturn(mockBukkitPlayer);

            InventoryView mockView = mock(InventoryView.class);
            when(event.getView()).thenReturn(mockView);
            when(mockView.getTopInventory()).thenReturn(mockInventory);

            gui.handleClickEvent(event);

            verify(event, never()).setCancelled(true);
            verify(event, never()).setCancelled(false);
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Given top inventory clicked with valid player, when handleClickEvent, then delegates to slot onClick")
        void handleClickEvent_delegatesToSlot_whenTopInventoryClicked() throws Exception {
            gui.getInventory();
            Player mockBukkitPlayer = mock(Player.class);
            when(mockBukkitPlayer.getUniqueId()).thenReturn(player.getUUID());
            when(mockGuiManager.getOpenedGui(mockBukkitPlayer)).thenReturn(Optional.of(gui));
            when(mockPlayerManager.getPlayer(player.getUUID())).thenReturn(Optional.of(player));

            Slot<TestCorePlayer> customSlot = new Slot<>() {
                @Override
                public boolean onClick(@NotNull TestCorePlayer corePlayer, @NotNull ClickType clickType) {
                    return true;
                }
            };

            java.lang.reflect.Field slotsField = BaseGui.class.getDeclaredField("slots");
            slotsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<Integer, Slot<TestCorePlayer>> slots =
                    (java.util.Map<Integer, Slot<TestCorePlayer>>) slotsField.get(gui);
            slots.put(3, customSlot);

            InventoryClickEvent event = mock(InventoryClickEvent.class);
            when(event.getSlot()).thenReturn(3);
            when(event.getClick()).thenReturn(ClickType.LEFT);
            when(event.getWhoClicked()).thenReturn(mockBukkitPlayer);

            InventoryView mockView = mock(InventoryView.class);
            when(event.getView()).thenReturn(mockView);
            when(mockView.getTopInventory()).thenReturn(mockInventory);
            when(mockView.getBottomInventory()).thenReturn(mock(Inventory.class));
            when(event.getClickedInventory()).thenReturn(mockInventory);

            gui.handleClickEvent(event);

            verify(event).setCancelled(true);
        }
    }
}

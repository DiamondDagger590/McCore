package com.diamonddagger590.mccore.gui;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.exception.gui.IllegalSlotAssignmentException;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
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
}

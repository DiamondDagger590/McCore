package com.diamonddagger590.mccore.gui.slot;

import com.diamonddagger590.mccore.gui.BaseGui;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class SlotTest {

    @Mock
    private CorePlayer mockPlayer;

    @Test
    @DisplayName("Given a Slot with default getValidGuiTypes, when calling getValidGuiTypes, then returns empty set")
    void defaultGetValidGuiTypes_returnsEmptySet() {
        Slot<CorePlayer> slot = new TestSlot();
        Set<Class<?>> validTypes = slot.getValidGuiTypes();
        assertNotNull(validTypes);
        assertTrue(validTypes.isEmpty());
    }

    @Test
    @DisplayName("Given a Slot with custom getValidGuiTypes, when calling getValidGuiTypes, then returns the specified types")
    void customGetValidGuiTypes_returnsSpecifiedTypes() {
        Slot<CorePlayer> slot = new RestrictedSlot();
        Set<Class<?>> validTypes = slot.getValidGuiTypes();
        assertNotNull(validTypes);
        assertEquals(1, validTypes.size());
        assertTrue(validTypes.contains(BaseGui.class));
    }

    @Test
    @DisplayName("Given a Slot implementation, when onClick returns true, then event should be cancelled")
    void onClick_returnsTrue_indicatesCancellation() {
        Slot<CorePlayer> slot = new CancellingSlot();
        assertTrue(slot.onClick(mockPlayer, ClickType.LEFT));
    }

    @Test
    @DisplayName("Given a Slot implementation, when onClick returns false, then event should not be cancelled")
    void onClick_returnsFalse_indicatesNoCancellation() {
        Slot<CorePlayer> slot = new NonCancellingSlot();
        assertFalse(slot.onClick(mockPlayer, ClickType.LEFT));
    }

    @Test
    @DisplayName("Given a Slot implementation, when onClick with different ClickTypes, then returns false for all")
    void onClick_returnsFalse_forAllClickTypes() {
        Slot<CorePlayer> slot = new TestSlot();
        for (ClickType clickType : ClickType.values()) {
            assertFalse(slot.onClick(mockPlayer, clickType));
        }
    }

    @Test
    @DisplayName("Given a Slot with default getValidGuiTypes, when set is mutated, then subsequent calls return fresh empty set")
    void defaultGetValidGuiTypes_returnsNewSetPerCall() {
        Slot<CorePlayer> slot = new TestSlot();
        Set<Class<?>> firstCall = slot.getValidGuiTypes();
        firstCall.add(BaseGui.class);
        assertEquals(1, firstCall.size());
        Set<Class<?>> secondCall = slot.getValidGuiTypes();
        assertTrue(secondCall.isEmpty());
    }

    private static class TestSlot implements Slot<CorePlayer> {

        @Override
        public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
            return false;
        }
    }

    private static class CancellingSlot implements Slot<CorePlayer> {

        @Override
        public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
            return true;
        }
    }

    private static class NonCancellingSlot implements Slot<CorePlayer> {

        @Override
        public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
            return false;
        }
    }

    private static class RestrictedSlot implements Slot<CorePlayer> {

        @Override
        public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
            return true;
        }

        @Override
        public @NotNull Set<Class<?>> getValidGuiTypes() {
            return Set.of(BaseGui.class);
        }
    }
}

package com.diamonddagger590.mccore.gui;

import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class GuiRefreshDefaultMethodTest {

    @Test
    @DisplayName("Given a Gui implementation, when refreshGUI is called, then paintInventory is invoked")
    void refreshGUI_callsPaintInventory_whenInvoked() {
        AtomicInteger paintCount = new AtomicInteger(0);
        Gui<CorePlayer> gui = new TestGui(paintCount);

        gui.refreshGUI();

        assertEquals(1, paintCount.get(),
                "refreshGUI should delegate to paintInventory exactly once");
    }

    @Test
    @DisplayName("Given a Gui implementation, when refreshGUI is called twice, then paintInventory is invoked twice")
    void refreshGUI_callsPaintInventoryTwice_whenInvokedTwice() {
        AtomicInteger paintCount = new AtomicInteger(0);
        Gui<CorePlayer> gui = new TestGui(paintCount);

        gui.refreshGUI();
        gui.refreshGUI();

        assertEquals(2, paintCount.get(),
                "refreshGUI should delegate to paintInventory each time it is called");
    }

    @SuppressWarnings("unchecked")
    private static class TestGui implements Gui<CorePlayer> {

        private final UUID uuid = UUID.randomUUID();
        private final AtomicInteger paintCount;

        TestGui(AtomicInteger paintCount) {
            this.paintCount = paintCount;
        }

        @Override
        @NotNull
        public UUID getUUID() {
            return uuid;
        }

        @Override
        @NotNull
        public Slot<CorePlayer> getSlot(int index) {
            return mock(Slot.class);
        }

        @Override
        @NotNull
        public Inventory getInventory() {
            return mock(Inventory.class);
        }

        @Override
        public void handleClickEvent(@NotNull InventoryClickEvent inventoryClickEvent) {
        }

        @Override
        public void paintInventory() {
            paintCount.incrementAndGet();
        }

        @Override
        public void registerListeners() {
        }

        @Override
        public void unregisterListeners() {
        }
    }
}

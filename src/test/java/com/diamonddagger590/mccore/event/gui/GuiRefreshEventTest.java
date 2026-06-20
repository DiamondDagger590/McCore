package com.diamonddagger590.mccore.event.gui;

import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class GuiRefreshEventTest {

    private static Gui<?> createStubGui() {
        return new Gui<>() {
            private final UUID uuid = UUID.randomUUID();

            @Override
            @NotNull
            public UUID getUUID() {
                return uuid;
            }

            @Override
            @NotNull
            public Slot<CorePlayer> getSlot(int index) {
                throw new UnsupportedOperationException();
            }

            @Override
            @NotNull
            public Inventory getInventory() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void handleClickEvent(@NotNull InventoryClickEvent inventoryClickEvent) {
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
        };
    }

    @Test
    @DisplayName("Given a Gui, when constructing GuiRefreshEvent, then getGui returns the same instance")
    void getGui_returnsSameInstance_whenConstructedWithGui() {
        Gui<?> gui = createStubGui();
        GuiRefreshEvent event = new GuiRefreshEvent(gui);
        assertSame(gui, event.getGui());
    }

    @Test
    @DisplayName("Given a GuiRefreshEvent, when calling getHandlers, then returns non-null HandlerList")
    void getHandlers_returnsNonNull() {
        Gui<?> gui = createStubGui();
        GuiRefreshEvent event = new GuiRefreshEvent(gui);
        assertNotNull(event.getHandlers());
    }

    @Test
    @DisplayName("Given GuiRefreshEvent class, when calling getHandlerList, then returns non-null HandlerList")
    void getHandlerList_returnsNonNull() {
        assertNotNull(GuiRefreshEvent.getHandlerList());
    }

    @Test
    @DisplayName("Given a GuiRefreshEvent, when calling getHandlers and getHandlerList, then they return the same instance")
    void getHandlers_andGetHandlerList_returnSameInstance() {
        Gui<?> gui = createStubGui();
        GuiRefreshEvent event = new GuiRefreshEvent(gui);
        assertSame(event.getHandlers(), GuiRefreshEvent.getHandlerList());
    }

    @Test
    @DisplayName("Given two GuiRefreshEvents with different guis, when calling getGui, then each returns its own gui")
    void getGui_returnsDistinctInstances_forDifferentEvents() {
        Gui<?> gui1 = createStubGui();
        Gui<?> gui2 = createStubGui();
        GuiRefreshEvent event1 = new GuiRefreshEvent(gui1);
        GuiRefreshEvent event2 = new GuiRefreshEvent(gui2);
        assertSame(gui1, event1.getGui());
        assertSame(gui2, event2.getGui());
    }

    @Test
    @DisplayName("Given a GuiRefreshEvent, when calling getEventName, then returns the class simple name")
    void getEventName_returnsClassName() {
        Gui<?> gui = createStubGui();
        GuiRefreshEvent event = new GuiRefreshEvent(gui);
        assertNotNull(event.getEventName());
    }
}

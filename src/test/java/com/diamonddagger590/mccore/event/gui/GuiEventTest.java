package com.diamonddagger590.mccore.event.gui;

import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuiEventTest {

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Gui<?> createStubGui() {
        return new Gui() {
            private final UUID uuid = UUID.randomUUID();

            @Override
            public UUID getUUID() {
                return uuid;
            }

            @Override
            public Slot getSlot(int index) {
                return null;
            }

            @Override
            public Inventory getInventory() {
                return null;
            }

            @Override
            public void handleClickEvent(InventoryClickEvent inventoryClickEvent) {}

            @Override
            public void paintInventory() {}

            @Override
            public void registerListeners() {}

            @Override
            public void unregisterListeners() {}
        };
    }

    // --- GuiRefreshEvent ---

    @Test
    @DisplayName("Given a Gui instance, when GuiRefreshEvent constructed, then getGui returns the same instance")
    void guiRefreshEvent_getGui_returnsSameInstance() {
        Gui<?> gui = createStubGui();
        GuiRefreshEvent event = new GuiRefreshEvent(gui);
        assertSame(gui, event.getGui());
    }

    @Test
    @DisplayName("Given a GuiRefreshEvent, when getHandlers called, then matches static handler list")
    void guiRefreshEvent_getHandlers_matchesStaticHandlerList() {
        GuiRefreshEvent event = new GuiRefreshEvent(createStubGui());
        assertSame(GuiRefreshEvent.getHandlerList(), event.getHandlers());
    }

    @Test
    @DisplayName("Given GuiRefreshEvent, when getHandlerList called multiple times, then returns same instance")
    void guiRefreshEvent_getHandlerList_returnsSameInstance() {
        HandlerList first = GuiRefreshEvent.getHandlerList();
        HandlerList second = GuiRefreshEvent.getHandlerList();
        assertSame(first, second);
    }

    // --- CoreGuiOpenEvent ---

    @Test
    @DisplayName("Given UUID, Gui, and NamespacedKey, when CoreGuiOpenEvent constructed, then getPlayerUUID returns the UUID")
    void coreGuiOpenEvent_getPlayerUUID_returnsProvidedUUID() {
        UUID uuid = UUID.randomUUID();
        Gui<?> gui = createStubGui();
        NamespacedKey key = NamespacedKey.fromString("test:my_gui");
        CoreGuiOpenEvent event = new CoreGuiOpenEvent(uuid, gui, key);
        assertEquals(uuid, event.getPlayerUUID());
    }

    @Test
    @DisplayName("Given UUID and Gui, when CoreGuiOpenEvent constructed, then getGui returns the same Gui instance")
    void coreGuiOpenEvent_getGui_returnsSameInstance() {
        UUID uuid = UUID.randomUUID();
        Gui<?> gui = createStubGui();
        CoreGuiOpenEvent event = new CoreGuiOpenEvent(uuid, gui, null);
        assertSame(gui, event.getGui());
    }

    @Test
    @DisplayName("Given a non-null NamespacedKey, when getGuiKey called, then returns Optional containing the key")
    void coreGuiOpenEvent_getGuiKey_returnsOptionalWithKey_whenKeyProvided() {
        UUID uuid = UUID.randomUUID();
        Gui<?> gui = createStubGui();
        NamespacedKey key = NamespacedKey.fromString("test:settings_gui");
        CoreGuiOpenEvent event = new CoreGuiOpenEvent(uuid, gui, key);

        Optional<NamespacedKey> result = event.getGuiKey();
        assertTrue(result.isPresent());
        assertEquals(key, result.get());
    }

    @Test
    @DisplayName("Given null NamespacedKey, when getGuiKey called, then returns empty Optional")
    void coreGuiOpenEvent_getGuiKey_returnsEmptyOptional_whenKeyIsNull() {
        UUID uuid = UUID.randomUUID();
        Gui<?> gui = createStubGui();
        CoreGuiOpenEvent event = new CoreGuiOpenEvent(uuid, gui, null);

        Optional<NamespacedKey> result = event.getGuiKey();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Given a CoreGuiOpenEvent, when getHandlers called, then matches static handler list")
    void coreGuiOpenEvent_getHandlers_matchesStaticHandlerList() {
        CoreGuiOpenEvent event = new CoreGuiOpenEvent(UUID.randomUUID(), createStubGui(), null);
        assertSame(CoreGuiOpenEvent.getHandlerList(), event.getHandlers());
    }

    @Test
    @DisplayName("Given CoreGuiOpenEvent, when getHandlerList called multiple times, then returns same instance")
    void coreGuiOpenEvent_getHandlerList_returnsSameInstance() {
        HandlerList first = CoreGuiOpenEvent.getHandlerList();
        HandlerList second = CoreGuiOpenEvent.getHandlerList();
        assertSame(first, second);
    }

    // --- Cross-event isolation ---

    @Test
    @DisplayName("Given GuiRefreshEvent and CoreGuiOpenEvent, when comparing handler lists, then they are distinct")
    void guiEvents_haveDistinctHandlerLists() {
        assertNotSame(GuiRefreshEvent.getHandlerList(), CoreGuiOpenEvent.getHandlerList());
    }
}

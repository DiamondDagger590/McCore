package com.diamonddagger590.mccore.event.gui;

import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.KeyedGui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreGuiOpenEventTest {

    @SuppressWarnings("deprecation")
    private static NamespacedKey key(String namespace, String key) {
        return new NamespacedKey(namespace, key);
    }

    private static final UUID PLAYER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final NamespacedKey GUI_KEY = key("test", "home_gui");

    private static class StubGui implements Gui<CorePlayer> {
        private final UUID uuid = UUID.randomUUID();

        @NotNull @Override public UUID getUUID() { return uuid; }
        @NotNull @Override public Slot<CorePlayer> getSlot(int index) { throw new UnsupportedOperationException(); }
        @NotNull @Override public Inventory getInventory() { throw new UnsupportedOperationException(); }
        @Override public void handleClickEvent(@NotNull InventoryClickEvent event) {}
        @Override public void paintInventory() {}
        @Override public void registerListeners() {}
        @Override public void unregisterListeners() {}
    }

    private static class StubKeyedGui extends StubGui implements KeyedGui {
        private final NamespacedKey guiKey;

        StubKeyedGui(NamespacedKey guiKey) {
            this.guiKey = guiKey;
        }

        @NotNull @Override
        public Optional<NamespacedKey> getGuiKey() {
            return Optional.ofNullable(guiKey);
        }
    }

    @Test
    @DisplayName("Given a player UUID, GUI, and key, when creating event, then getPlayerUUID returns the UUID")
    void getPlayerUUID_returnsCorrectUUID_whenEventIsCreated() {
        StubGui gui = new StubGui();
        CoreGuiOpenEvent event = new CoreGuiOpenEvent(PLAYER_UUID, gui, GUI_KEY);
        assertEquals(PLAYER_UUID, event.getPlayerUUID());
    }

    @Test
    @DisplayName("Given a GUI, when creating event, then getGui returns the same GUI instance")
    void getGui_returnsSameInstance_whenEventIsCreated() {
        StubGui gui = new StubGui();
        CoreGuiOpenEvent event = new CoreGuiOpenEvent(PLAYER_UUID, gui, GUI_KEY);
        assertEquals(gui, event.getGui());
    }

    @Test
    @DisplayName("Given a non-null GUI key, when calling getGuiKey, then returns present Optional with correct key")
    void getGuiKey_returnsPresentOptional_whenKeyIsProvided() {
        StubGui gui = new StubGui();
        CoreGuiOpenEvent event = new CoreGuiOpenEvent(PLAYER_UUID, gui, GUI_KEY);
        assertTrue(event.getGuiKey().isPresent());
        assertEquals(GUI_KEY, event.getGuiKey().get());
    }

    @Test
    @DisplayName("Given a null GUI key, when calling getGuiKey, then returns empty Optional")
    void getGuiKey_returnsEmptyOptional_whenKeyIsNull() {
        StubGui gui = new StubGui();
        CoreGuiOpenEvent event = new CoreGuiOpenEvent(PLAYER_UUID, gui, null);
        assertTrue(event.getGuiKey().isEmpty());
    }

    @Test
    @DisplayName("Given a CoreGuiOpenEvent, when calling getHandlers, then returns non-null HandlerList")
    void getHandlers_returnsNonNullHandlerList_whenCalled() {
        StubGui gui = new StubGui();
        CoreGuiOpenEvent event = new CoreGuiOpenEvent(PLAYER_UUID, gui, null);
        assertNotNull(event.getHandlers());
    }

    @Test
    @DisplayName("Given CoreGuiOpenEvent class, when calling getHandlerList, then returns non-null static HandlerList")
    void getHandlerList_returnsNonNullStaticHandlerList_whenCalled() {
        HandlerList handlerList = CoreGuiOpenEvent.getHandlerList();
        assertNotNull(handlerList);
    }

    @Test
    @DisplayName("Given a CoreGuiOpenEvent, when calling getHandlers and getHandlerList, then they return the same instance")
    void getHandlers_returnsSameInstanceAsStaticGetHandlerList() {
        StubGui gui = new StubGui();
        CoreGuiOpenEvent event = new CoreGuiOpenEvent(PLAYER_UUID, gui, null);
        assertEquals(CoreGuiOpenEvent.getHandlerList(), event.getHandlers());
    }

    @Test
    @DisplayName("Given a keyed GUI with a key, when creating event with that key, then event reflects the key")
    void event_reflectsGuiKey_whenKeyedGuiIsUsed() {
        StubKeyedGui keyedGui = new StubKeyedGui(GUI_KEY);
        NamespacedKey extractedKey = keyedGui.getGuiKey().orElse(null);
        CoreGuiOpenEvent event = new CoreGuiOpenEvent(PLAYER_UUID, keyedGui, extractedKey);
        assertTrue(event.getGuiKey().isPresent());
        assertEquals(GUI_KEY, event.getGuiKey().get());
    }

    @Test
    @DisplayName("Given a keyed GUI without a key, when creating event, then event has empty guiKey")
    void event_hasEmptyGuiKey_whenKeyedGuiHasNoKey() {
        StubKeyedGui keyedGui = new StubKeyedGui(null);
        NamespacedKey extractedKey = keyedGui.getGuiKey().orElse(null);
        CoreGuiOpenEvent event = new CoreGuiOpenEvent(PLAYER_UUID, keyedGui, extractedKey);
        assertTrue(event.getGuiKey().isEmpty());
    }

    @Test
    @DisplayName("Given different player UUIDs, when creating events, then each event has its own UUID")
    void events_haveDifferentUUIDs_whenCreatedWithDifferentPlayers() {
        UUID uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID uuid2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
        StubGui gui = new StubGui();

        CoreGuiOpenEvent event1 = new CoreGuiOpenEvent(uuid1, gui, null);
        CoreGuiOpenEvent event2 = new CoreGuiOpenEvent(uuid2, gui, null);

        assertEquals(uuid1, event1.getPlayerUUID());
        assertEquals(uuid2, event2.getPlayerUUID());
    }
}

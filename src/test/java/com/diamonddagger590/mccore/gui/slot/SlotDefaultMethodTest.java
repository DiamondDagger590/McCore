package com.diamonddagger590.mccore.gui.slot;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.builder.item.ItemPluginType;
import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class SlotDefaultMethodTest {

    private MockedStatic<CorePlugin> corePluginStatic;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        RegistryResetExtension.setupRegistry();

        CorePlugin mockPlugin = mock(CorePlugin.class);
        when(mockPlugin.getMiniMessage()).thenReturn(MiniMessage.miniMessage());
        when(mockPlugin.getItemPlugin()).thenReturn(ItemPluginType.NONE);
        when(mockPlugin.registryAccess()).thenCallRealMethod();

        corePluginStatic = mockStatic(CorePlugin.class);
        corePluginStatic.when(CorePlugin::getInstance).thenReturn(mockPlugin);
    }

    @AfterEach
    void tearDown() {
        corePluginStatic.close();
        MockBukkit.unmock();
        RegistryResetExtension.resetRegistry();
    }

    @Test
    @DisplayName("Given a Slot that does not override getItem, when getItem is called, then returns an AIR ItemBuilder")
    void getItem_returnsAirItemBuilder_whenDefaultImplementation() {
        CorePlayer mockPlayer = mock(CorePlayer.class);
        Slot<CorePlayer> slot = new DefaultItemSlot();

        ItemBuilder result = slot.getItem(mockPlayer);

        assertNotNull(result);
        assertEquals(Material.AIR, result.getType());
    }

    @Test
    @DisplayName("Given a Slot that overrides getItem, when getItem is called, then returns the overridden item")
    void getItem_returnsOverriddenItem_whenImplementationOverrides() {
        CorePlayer mockPlayer = mock(CorePlayer.class);
        Slot<CorePlayer> slot = new CustomItemSlot();

        ItemBuilder result = slot.getItem(mockPlayer);

        assertNotNull(result);
        assertEquals(Material.DIAMOND, result.getType());
    }

    private static class DefaultItemSlot implements Slot<CorePlayer> {

        @Override
        public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
            return false;
        }
    }

    private static class CustomItemSlot implements Slot<CorePlayer> {

        @NotNull
        @Override
        public ItemBuilder getItem(@NotNull CorePlayer corePlayer) {
            return ItemBuilder.from(ItemType.DIAMOND);
        }

        @Override
        public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
            return false;
        }
    }
}

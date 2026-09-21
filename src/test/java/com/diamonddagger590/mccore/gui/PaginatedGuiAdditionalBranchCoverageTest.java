package com.diamonddagger590.mccore.gui;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.gui.slot.pagination.NextPageSlot;
import com.diamonddagger590.mccore.gui.slot.pagination.PreviousPageSlot;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class PaginatedGuiAdditionalBranchCoverageTest {

    private static class TestCorePlayer extends CorePlayer {
        public TestCorePlayer(@NotNull UUID uuid, @NotNull CorePlugin corePlugin) {
            super(uuid, corePlugin);
        }

        @Override
        public boolean useMutex() {
            return false;
        }
    }

    private static class ZeroMaxPageGui extends PaginatedGui<TestCorePlayer> {

        private final Inventory mockInventory;

        public ZeroMaxPageGui(@NotNull TestCorePlayer player, @NotNull Inventory mockInventory) {
            super(player);
            this.mockInventory = mockInventory;
        }

        @Override
        public int getMaximumPage() {
            return 0;
        }

        @NotNull
        @Override
        public PreviousPageSlot<TestCorePlayer> getPreviousPageSlot() {
            return mock(PreviousPageSlot.class);
        }

        @NotNull
        @Override
        public NextPageSlot<TestCorePlayer> getNextPageSlot() {
            return mock(NextPageSlot.class);
        }

        @NotNull
        @Override
        protected Inventory getInventoryForPage(int page) {
            return mockInventory;
        }

        @Override
        protected void paintInventoryForPage(@NotNull Inventory inventory, int page) {
        }

        @Override
        public void paintInventory() {
            super.paintInventory();
        }

        @Override
        public void registerListeners() {
        }

        @Override
        public void unregisterListeners() {
        }
    }

    private TestCorePlayer player;
    private Inventory mockInventory;

    @BeforeEach
    void setUp() {
        CorePlugin plugin = mock(CorePlugin.class);
        player = new TestCorePlayer(UUID.randomUUID(), plugin);
        mockInventory = mock(Inventory.class);
    }

    @Test
    @DisplayName("Given maximum page is 0, when setPage called, then throws IllegalArgumentException for max page < 1 precondition")
    void setPage_throwsForMaxPageLessThanOne_whenMaximumPageIsZero() {
        ZeroMaxPageGui gui = new ZeroMaxPageGui(player, mockInventory);
        gui.getInventory();

        assertThrows(IllegalArgumentException.class, () -> gui.setPage(1));
    }
}

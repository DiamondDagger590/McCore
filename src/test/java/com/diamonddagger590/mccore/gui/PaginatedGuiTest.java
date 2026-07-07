package com.diamonddagger590.mccore.gui;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.exception.gui.InventoryAlreadyExistsForGuiException;
import com.diamonddagger590.mccore.gui.slot.pagination.NextPageSlot;
import com.diamonddagger590.mccore.gui.slot.pagination.PreviousPageSlot;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PaginatedGuiTest {

    private static class TestCorePlayer extends CorePlayer {
        public TestCorePlayer(@NotNull UUID uuid, @NotNull CorePlugin corePlugin) {
            super(uuid, corePlugin);
        }

        @Override
        public boolean useMutex() {
            return false;
        }
    }

    private static class TestPaginatedGui extends PaginatedGui<TestCorePlayer> {

        private final Inventory mockInventory;
        private final int maxPage;
        private final AtomicInteger paintCallCount = new AtomicInteger(0);
        private final NextPageSlot<TestCorePlayer> nextPageSlot;
        private final PreviousPageSlot<TestCorePlayer> previousPageSlot;

        public TestPaginatedGui(@NotNull TestCorePlayer player, @NotNull Inventory mockInventory, int maxPage) {
            super(player);
            this.mockInventory = mockInventory;
            this.maxPage = maxPage;
            this.nextPageSlot = mock(NextPageSlot.class);
            this.previousPageSlot = mock(PreviousPageSlot.class);
        }

        public TestPaginatedGui(@NotNull TestCorePlayer player, @NotNull Inventory mockInventory, int maxPage, int startPage) {
            super(player, startPage);
            this.mockInventory = mockInventory;
            this.maxPage = maxPage;
            this.nextPageSlot = mock(NextPageSlot.class);
            this.previousPageSlot = mock(PreviousPageSlot.class);
        }

        @Override
        public int getMaximumPage() {
            return maxPage;
        }

        @NotNull
        @Override
        public PreviousPageSlot<TestCorePlayer> getPreviousPageSlot() {
            return previousPageSlot;
        }

        @NotNull
        @Override
        public NextPageSlot<TestCorePlayer> getNextPageSlot() {
            return nextPageSlot;
        }

        @NotNull
        @Override
        protected Inventory getInventoryForPage(int page) {
            return mockInventory;
        }

        @Override
        protected void paintInventoryForPage(@NotNull Inventory inventory, int page) {
            paintCallCount.incrementAndGet();
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

        public int getPaintCallCount() {
            return paintCallCount.get();
        }

        public void callBuildInventory() {
            buildInventory();
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

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Given a player, when single-arg constructor used, then page defaults to 1")
        void defaultsPageTo1() {
            TestPaginatedGui gui = new TestPaginatedGui(player, mockInventory, 5);

            assertEquals(1, gui.getPage());
        }

        @Test
        @DisplayName("Given a player and start page, when two-arg constructor used, then page is set to that value")
        void setsStartPage() {
            TestPaginatedGui gui = new TestPaginatedGui(player, mockInventory, 5, 3);

            assertEquals(3, gui.getPage());
        }
    }

    @Nested
    @DisplayName("getPage and setPage")
    class GetPageAndSetPage {

        @Test
        @DisplayName("Given page 1, when setPage to 2, then getPage returns 2")
        void setPage_updatesPage() {
            TestPaginatedGui gui = new TestPaginatedGui(player, mockInventory, 5);
            gui.getInventory();

            gui.setPage(2);

            assertEquals(2, gui.getPage());
        }

        @Test
        @DisplayName("Given max page of 5, when setPage to 5, then succeeds")
        void setPage_acceptsMaxPage() {
            TestPaginatedGui gui = new TestPaginatedGui(player, mockInventory, 5);
            gui.getInventory();

            gui.setPage(5);

            assertEquals(5, gui.getPage());
        }

        @Test
        @DisplayName("Given max page of 3, when setPage to 4, then throws IllegalArgumentException")
        void setPage_throwsForPageAboveMax() {
            TestPaginatedGui gui = new TestPaginatedGui(player, mockInventory, 3);
            gui.getInventory();

            assertThrows(IllegalArgumentException.class, () -> gui.setPage(4));
        }

        @Test
        @DisplayName("When setPage to 0, then throws IllegalArgumentException")
        void setPage_throwsForPageZero() {
            TestPaginatedGui gui = new TestPaginatedGui(player, mockInventory, 3);
            gui.getInventory();

            assertThrows(IllegalArgumentException.class, () -> gui.setPage(0));
        }

        @Test
        @DisplayName("When setPage to negative, then throws IllegalArgumentException")
        void setPage_throwsForNegativePage() {
            TestPaginatedGui gui = new TestPaginatedGui(player, mockInventory, 3);
            gui.getInventory();

            assertThrows(IllegalArgumentException.class, () -> gui.setPage(-1));
        }
    }

    @Nested
    @DisplayName("buildInventory")
    class BuildInventory {

        @Test
        @DisplayName("Given inventory not built, when getInventory called, then builds and returns inventory")
        void buildsInventory_onFirstCall() {
            TestPaginatedGui gui = new TestPaginatedGui(player, mockInventory, 3);

            Inventory result = gui.getInventory();

            assertSame(mockInventory, result);
        }

        @Test
        @DisplayName("Given inventory not built, when getInventory called, then paints the inventory")
        void paintsInventory_onFirstBuild() {
            TestPaginatedGui gui = new TestPaginatedGui(player, mockInventory, 3);

            gui.getInventory();

            assertEquals(1, gui.getPaintCallCount());
        }

        @Test
        @DisplayName("Given inventory already built, when buildInventory called again directly, then throws InventoryAlreadyExistsForGuiException")
        void throwsException_whenInventoryAlreadyExists() {
            TestPaginatedGui gui = new TestPaginatedGui(player, mockInventory, 3);
            gui.getInventory();

            assertThrows(InventoryAlreadyExistsForGuiException.class, gui::callBuildInventory);
        }
    }

    @Nested
    @DisplayName("refreshGUI")
    class RefreshGUI {

        @Test
        @DisplayName("Given inventory is built, when refreshGUI, then repaints the current page")
        void repaintsCurrentPage() {
            TestPaginatedGui gui = new TestPaginatedGui(player, mockInventory, 3);
            gui.getInventory();
            int countAfterBuild = gui.getPaintCallCount();

            gui.refreshGUI();

            assertEquals(countAfterBuild + 1, gui.getPaintCallCount());
        }

        @Test
        @DisplayName("Given inventory is not built, when refreshGUI, then builds and paints twice (build + refresh)")
        void buildsAndPaints_whenInventoryIsNull() {
            TestPaginatedGui gui = new TestPaginatedGui(player, mockInventory, 3);

            gui.refreshGUI();

            assertEquals(2, gui.getPaintCallCount());
        }
    }

    @Nested
    @DisplayName("paintInventory")
    class PaintInventory {

        @Test
        @DisplayName("Given inventory is built, when paintInventory called, then delegates to paintInventoryForPage with current page")
        void delegatesToPaintInventoryForPage() {
            TestPaginatedGui gui = new TestPaginatedGui(player, mockInventory, 3, 2);
            gui.getInventory();
            int countAfterBuild = gui.getPaintCallCount();

            gui.paintInventory();

            assertEquals(countAfterBuild + 1, gui.getPaintCallCount());
        }
    }

    @Nested
    @DisplayName("setPage triggers refreshGUI")
    class SetPageRefresh {

        @Test
        @DisplayName("Given inventory is built, when setPage called, then refreshGUI paints the new page")
        void setPage_triggersRefresh() {
            TestPaginatedGui gui = new TestPaginatedGui(player, mockInventory, 5);
            gui.getInventory();
            int countAfterBuild = gui.getPaintCallCount();

            gui.setPage(3);

            assertTrue(gui.getPaintCallCount() > countAfterBuild);
            assertEquals(3, gui.getPage());
        }
    }

    @Nested
    @DisplayName("getPreviousPageSlot and getNextPageSlot")
    class NavigationSlots {

        @Test
        @DisplayName("Given TestPaginatedGui, when getPreviousPageSlot, then returns non-null slot")
        void previousPageSlot_isNotNull() {
            TestPaginatedGui gui = new TestPaginatedGui(player, mockInventory, 3);

            assertNotNull(gui.getPreviousPageSlot());
        }

        @Test
        @DisplayName("Given TestPaginatedGui, when getNextPageSlot, then returns non-null slot")
        void nextPageSlot_isNotNull() {
            TestPaginatedGui gui = new TestPaginatedGui(player, mockInventory, 3);

            assertNotNull(gui.getNextPageSlot());
        }
    }
}

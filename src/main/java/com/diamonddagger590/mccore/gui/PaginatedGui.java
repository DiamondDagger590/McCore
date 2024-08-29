package com.diamonddagger590.mccore.gui;

import com.diamonddagger590.mccore.exception.gui.InventoryAlreadyExistsForGuiException;
import com.diamonddagger590.mccore.gui.slot.NextPageSlot;
import com.diamonddagger590.mccore.gui.slot.PreviousPageSlot;
import com.google.common.base.Preconditions;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * A paginated gui is a gui that can have multiple pages of content,
 * requiring the ability to go back and forth between pages.
 */
public abstract class PaginatedGui extends Guiv2 {

    protected static final PreviousPageSlot PREVIOUS_PAGE_SLOT = new PreviousPageSlot();
    protected static final NextPageSlot NEXT_PAGE_SLOT = new NextPageSlot();

    private int page;

    public PaginatedGui() {
        super();
        this.page = 1;
    }

    public PaginatedGui(int page) {
        this.page = page;
    }

    /**
     * {@inheritDoc}
     *
     * @throws InventoryAlreadyExistsForGuiException If {@link #getInventory()} the inventory in this gui is already
     *                                               built.
     */
    @Override
    protected void buildInventory() {
        if (inventory != null) {
            throw new InventoryAlreadyExistsForGuiException(this);
        }
        this.inventory = getInventoryForPage(page);
        paintInventoryForPage(inventory, page);
    }

    /**
     * Gets a Bukkit {@link Inventory} for the provided page.
     *
     * @param page The page to get an {@link Inventory} for.
     * @return A Bukkit {@link Inventory} for the provided page.
     */
    @NotNull
    protected abstract Inventory getInventoryForPage(int page);

    /**
     * Paints the provided {@link Inventory} for the provided page.
     *
     * @param inventory The {@link Inventory} to paint.
     * @param page      The page to use when painting.
     */
    protected abstract void paintInventoryForPage(@NotNull Inventory inventory, int page);

    /**
     * Paints the current page of the gui.
     */
    @Override
    public void paintInventory() {
        paintInventoryForPage(inventory, page);
    }

    /**
     * Gets the maximum page allowed for this gui.
     * <p>
     * The maximum page should be at least 1.
     *
     * @return The maximum page allowed for this gui.
     */
    public abstract int getMaximumPage();

    /**
     * Gets the current page the gui is on.
     *
     * @return The current page the gui is on.
     */
    public int getPage() {
        return page;
    }

    /**
     * Sets the current page for the gui and repaints the gui as well.
     * <p>
     * This method checks and will error if any of the following criteria aren't met:
     * <ul>
     *     <li>
     *         {@link #getMaximumPage()} < 1
     *     </li>
     *     <li>
     *         The provided page is > {@link #getMaximumPage()}
     *     </li>
     *     <li>
     *         The provided page is < 1
     *     </li>
     * </ul>
     *
     * @param page The new page for this gui.
     */
    public void setPage(int page) {
        Preconditions.checkArgument(getMaximumPage() >= 1, "Maximum page must be at least 1");
        Preconditions.checkArgument(page <= getMaximumPage(), "Page number must be less or equal to than " + getMaximumPage());
        Preconditions.checkArgument(page >= 1, "Page number must be at least 1.");
        this.page = page;
        refreshGUI();
    }

    @Override
    public void refreshGUI() {
        if (this.inventory == null) {
            buildInventory();
        }
        paintInventoryForPage(inventory, page);
    }
}

package com.diamonddagger590.mccore.gui;

import com.diamonddagger590.mccore.gui.function.GuiFillerFunction;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * The base GUI class for any plugin wanting to make a
 * GUI that works with this GUI system.
 * <p>
 * For more specialized behavior, implement one of the specific gui components
 * such as {@link com.diamonddagger590.mccore.gui.component.ClickableGui}
 */
public abstract class CoreGui implements Gui {

    protected Inventory inventory;
    protected GuiFillerFunction guiFillerFunction = (inventory -> {
    });

    /**
     * Executes the {@link GuiFillerFunction} for this GUI
     */
    public void executeFillerFunction() {
        guiFillerFunction.fillGui(getInventory());
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}

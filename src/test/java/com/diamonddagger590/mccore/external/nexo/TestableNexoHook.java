package com.diamonddagger590.mccore.external.nexo;

import com.diamonddagger590.mccore.CorePlugin;
import com.nexomc.nexo.mechanics.custom_block.CustomBlockMechanic;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

/**
 * A testable subclass of {@link CoreNexoHook} that replaces all direct Nexo API calls with
 * configurable test doubles. This avoids loading the Nexo runtime classes ({@code NexoItems},
 * {@code NexoBlocks}, {@code ItemBuilder}, {@code BlockSounds}) which require a running server
 * due to their static initializers.
 * <p>
 * Only the thin API-delegating methods and the protected extraction methods are overridden.
 * All branching logic in the complex methods (e.g. {@link CoreNexoHook#itemName},
 * {@link CoreNexoHook#drops}) runs as real production code.
 */
class TestableNexoHook extends CoreNexoHook {

    @Nullable
    private CustomBlockMechanic mockMechanic;
    private boolean removeResult = true;
    private boolean placeCalled;
    @Nullable
    private String resolvedItemName;
    private boolean customSoundPlayed;
    private boolean customSoundResult;

    TestableNexoHook(@NotNull CorePlugin corePlugin) {
        super(corePlugin);
    }

    void setMockMechanic(@Nullable CustomBlockMechanic mechanic) {
        this.mockMechanic = mechanic;
    }

    void setRemoveResult(boolean removeResult) {
        this.removeResult = removeResult;
    }

    boolean wasPlaceCalled() {
        return placeCalled;
    }

    void setResolvedItemName(@Nullable String resolvedItemName) {
        this.resolvedItemName = resolvedItemName;
    }

    void setCustomSoundResult(boolean result) {
        this.customSoundResult = result;
    }

    boolean wasCustomSoundPlayed() {
        return customSoundPlayed;
    }

    // === Thin wrapper overrides (avoid loading NexoItems/NexoBlocks static initializers) ===

    @NotNull
    @Override
    public Optional<ItemStack> item(@NotNull String item) {
        return Optional.empty();
    }

    @NotNull
    @Override
    public Optional<Set<String>> itemModels(@NotNull ItemStack itemStack) {
        return Optional.empty();
    }

    @Override
    public boolean isItem(@NotNull String item) {
        return false;
    }

    @Override
    public boolean isItem(@NotNull ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean isItemOfType(@NotNull ItemStack itemStack, @NotNull String itemName) {
        return false;
    }

    @Override
    public boolean isCustomBlock(@NotNull Block block) {
        return false;
    }

    @Override
    public boolean isCustomBlock(@NotNull String customBlock) {
        return false;
    }

    // === Protected extraction method overrides ===

    @Nullable
    @Override
    protected CustomBlockMechanic customBlockMechanic(@NotNull Location location) {
        return mockMechanic;
    }

    @Override
    protected boolean removeNexoBlock(@NotNull Location location) {
        return removeResult;
    }

    @Override
    protected void placeNexoBlock(@NotNull String blockId, @NotNull Location location) {
        placeCalled = true;
    }

    @Nullable
    @Override
    protected String resolveNexoItemName(@NotNull String itemId) {
        return resolvedItemName;
    }

    @Override
    protected boolean playCustomBlockSound(@NotNull Block block, @NotNull CustomBlockMechanic mechanic) {
        customSoundPlayed = true;
        return customSoundResult;
    }
}

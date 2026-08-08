package com.diamonddagger590.mccore.command;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.incendo.cloud.Command;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.CommandExecutionHandler;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.parser.ParserDescriptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoreCommandTest {

    @Mock
    private CorePlugin mockPlugin;

    @Mock
    private CoreCommandManager mockCoreCommandManager;

    private MockedStatic<CorePlugin> corePluginStatic;
    private List<CommandExecutionHandler<CommandSourceStack>> capturedHandlers;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        RegistryResetExtension.setupRegistry();
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockCoreCommandManager);

        corePluginStatic = mockStatic(CorePlugin.class);
        corePluginStatic.when(CorePlugin::getInstance).thenReturn(mockPlugin);
        when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());
        when(mockPlugin.getMiniMessage()).thenReturn(MiniMessage.miniMessage());

        @SuppressWarnings("unchecked")
        PaperCommandManager<CommandSourceStack> mockCmdManager = mock(PaperCommandManager.class);
        when(mockCoreCommandManager.getCommandManager()).thenReturn(mockCmdManager);

        @SuppressWarnings("unchecked")
        Command.Builder<CommandSourceStack> mockBuilder = mock(Command.Builder.class);
        when(mockCmdManager.commandBuilder(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.literal(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.required(anyString(), any(ParserDescriptor.class))).thenReturn(mockBuilder);
        when(mockBuilder.optional(anyString(), any(ParserDescriptor.class))).thenReturn(mockBuilder);

        capturedHandlers = new ArrayList<>();
        when(mockBuilder.handler(any())).thenAnswer(invocation -> {
            capturedHandlers.add(invocation.getArgument(0));
            return mockBuilder;
        });

        LoreCommand.registerCommand();
    }

    @AfterEach
    void tearDown() {
        corePluginStatic.close();
        RegistryResetExtension.resetRegistry();
    }

    private CommandContext<CommandSourceStack> createPlayerContext(Player player) {
        @SuppressWarnings("unchecked")
        CommandContext<CommandSourceStack> context = mock(CommandContext.class);
        CommandSourceStack sourceStack = mock(CommandSourceStack.class);
        when(context.sender()).thenReturn(sourceStack);
        when(sourceStack.getSender()).thenReturn(player);
        return context;
    }

    private record PlayerItemMocks(Player player, PlayerInventory inventory, ItemStack itemStack) {}

    private PlayerItemMocks createPlayerWithItem(ItemMeta itemMeta) {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        ItemStack itemStack = mock(ItemStack.class);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getItemInMainHand()).thenReturn(itemStack);
        when(itemStack.getItemMeta()).thenReturn(itemMeta);
        return new PlayerItemMocks(player, inventory, itemStack);
    }

    @Nested
    @DisplayName("registerCommand")
    class RegisterCommand {

        @Test
        @DisplayName("Given valid command manager, when registerCommand is called, then three handlers are registered")
        void registerCommand_registersThreeHandlers_whenCalled() {
            assertEquals(3, capturedHandlers.size());
        }
    }

    @Nested
    @DisplayName("lore clear handler")
    class LoreClearHandler {

        @Test
        @DisplayName("Given player with lore on item, when clear handler executes, then lore is cleared")
        void clearHandler_clearsLore_whenPlayerHasItemWithLore() {
            CommandExecutionHandler<CommandSourceStack> clearHandler = capturedHandlers.get(0);
            ItemMeta itemMeta = mock(ItemMeta.class);
            List<Component> lore = new ArrayList<>(List.of(Component.text("line1"), Component.text("line2")));
            when(itemMeta.lore()).thenReturn(lore);

            PlayerItemMocks mocks = createPlayerWithItem(itemMeta);
            CommandContext<CommandSourceStack> context = createPlayerContext(mocks.player());

            clearHandler.execute(context);

            assertTrue(lore.isEmpty());
            verify(itemMeta).lore(lore);
            verify(mocks.itemStack()).setItemMeta(itemMeta);
            verify(mocks.inventory()).setItemInMainHand(mocks.itemStack());
            verify(mocks.player()).updateInventory();
        }

        @Test
        @DisplayName("Given player with null lore on item, when clear handler executes, then no modification occurs")
        void clearHandler_doesNotModify_whenLoreIsNull() {
            CommandExecutionHandler<CommandSourceStack> clearHandler = capturedHandlers.get(0);
            ItemMeta itemMeta = mock(ItemMeta.class);
            when(itemMeta.lore()).thenReturn(null);

            PlayerItemMocks mocks = createPlayerWithItem(itemMeta);
            CommandContext<CommandSourceStack> context = createPlayerContext(mocks.player());

            clearHandler.execute(context);

            verify(itemMeta, never()).lore(any());
            verify(mocks.player(), never()).updateInventory();
        }

        @Test
        @DisplayName("Given non-player sender, when clear handler executes, then no action is taken")
        void clearHandler_doesNothing_whenSenderIsNotPlayer() {
            CommandExecutionHandler<CommandSourceStack> clearHandler = capturedHandlers.get(0);

            ConsoleCommandSender consoleSender = mock(ConsoleCommandSender.class);
            @SuppressWarnings("unchecked")
            CommandContext<CommandSourceStack> context = mock(CommandContext.class);
            CommandSourceStack sourceStack = mock(CommandSourceStack.class);
            when(context.sender()).thenReturn(sourceStack);
            when(sourceStack.getSender()).thenReturn(consoleSender);

            clearHandler.execute(context);

            verifyNoInteractions(consoleSender);
        }
    }

    @Nested
    @DisplayName("lore add handler")
    class LoreAddHandler {

        @Test
        @DisplayName("Given player with lore and no index specified, when add handler executes, then lore line is appended")
        void addHandler_appendsLore_whenNoIndexSpecified() {
            CommandExecutionHandler<CommandSourceStack> addHandler = capturedHandlers.get(1);
            ItemMeta itemMeta = mock(ItemMeta.class);
            List<Component> lore = new ArrayList<>(List.of(Component.text("existing")));
            when(itemMeta.lore()).thenReturn(lore);

            PlayerItemMocks mocks = createPlayerWithItem(itemMeta);
            CommandContext<CommandSourceStack> context = createPlayerContext(mocks.player());

            CloudKey<String> loreKey = CloudKey.of("lore", String.class);
            CloudKey<Integer> indexKey = CloudKey.of("index", Integer.class);
            when(context.get(loreKey)).thenReturn("<green>new line");
            when(context.getOrDefault(indexKey, 0)).thenReturn(0);
            when(context.contains(indexKey)).thenReturn(false);

            addHandler.execute(context);

            assertEquals(2, lore.size());
            assertEquals(MiniMessage.miniMessage().deserialize("<green>new line"), lore.get(1));
            verify(itemMeta).lore(lore);
            verify(mocks.player()).updateInventory();
        }

        @Test
        @DisplayName("Given player with lore and index specified, when add handler executes, then lore line is inserted at index")
        void addHandler_insertsAtIndex_whenIndexSpecified() {
            CommandExecutionHandler<CommandSourceStack> addHandler = capturedHandlers.get(1);
            ItemMeta itemMeta = mock(ItemMeta.class);
            List<Component> lore = new ArrayList<>(List.of(Component.text("first"), Component.text("third")));
            when(itemMeta.lore()).thenReturn(lore);

            PlayerItemMocks mocks = createPlayerWithItem(itemMeta);
            CommandContext<CommandSourceStack> context = createPlayerContext(mocks.player());

            CloudKey<String> loreKey = CloudKey.of("lore", String.class);
            CloudKey<Integer> indexKey = CloudKey.of("index", Integer.class);
            when(context.get(loreKey)).thenReturn("second");
            when(context.getOrDefault(indexKey, 0)).thenReturn(1);
            when(context.contains(indexKey)).thenReturn(true);

            addHandler.execute(context);

            assertEquals(3, lore.size());
            assertEquals(MiniMessage.miniMessage().deserialize("second"), lore.get(1));
            verify(itemMeta).lore(lore);
            verify(mocks.player()).updateInventory();
        }

        @Test
        @DisplayName("Given player with null lore, when add handler executes, then no modification occurs")
        void addHandler_doesNotModify_whenLoreIsNull() {
            CommandExecutionHandler<CommandSourceStack> addHandler = capturedHandlers.get(1);
            ItemMeta itemMeta = mock(ItemMeta.class);
            when(itemMeta.lore()).thenReturn(null);

            PlayerItemMocks mocks = createPlayerWithItem(itemMeta);
            CommandContext<CommandSourceStack> context = createPlayerContext(mocks.player());

            CloudKey<String> loreKey = CloudKey.of("lore", String.class);
            CloudKey<Integer> indexKey = CloudKey.of("index", Integer.class);
            when(context.get(loreKey)).thenReturn("test");
            when(context.getOrDefault(indexKey, 0)).thenReturn(0);
            when(context.contains(indexKey)).thenReturn(false);

            addHandler.execute(context);

            verify(itemMeta, never()).lore(any());
            verify(mocks.player(), never()).updateInventory();
        }

        @Test
        @DisplayName("Given non-player sender, when add handler executes, then no action is taken")
        void addHandler_doesNothing_whenSenderIsNotPlayer() {
            CommandExecutionHandler<CommandSourceStack> addHandler = capturedHandlers.get(1);

            ConsoleCommandSender consoleSender = mock(ConsoleCommandSender.class);
            @SuppressWarnings("unchecked")
            CommandContext<CommandSourceStack> context = mock(CommandContext.class);
            CommandSourceStack sourceStack = mock(CommandSourceStack.class);
            when(context.sender()).thenReturn(sourceStack);
            when(sourceStack.getSender()).thenReturn(consoleSender);

            addHandler.execute(context);

            verifyNoInteractions(consoleSender);
        }
    }

    @Nested
    @DisplayName("lore remove handler")
    class LoreRemoveHandler {

        @Test
        @DisplayName("Given player with lore and valid index, when remove handler executes, then lore line is removed")
        void removeHandler_removesLoreLine_whenIndexIsValid() {
            CommandExecutionHandler<CommandSourceStack> removeHandler = capturedHandlers.get(2);
            ItemMeta itemMeta = mock(ItemMeta.class);
            Component line0 = Component.text("line0");
            Component line1 = Component.text("line1");
            List<Component> lore = new ArrayList<>(List.of(line0, line1));
            when(itemMeta.lore()).thenReturn(lore);

            PlayerItemMocks mocks = createPlayerWithItem(itemMeta);
            CommandContext<CommandSourceStack> context = createPlayerContext(mocks.player());

            CloudKey<Integer> indexKey = CloudKey.of("index", Integer.class);
            when(context.getOrDefault(indexKey, 0)).thenReturn(0);

            removeHandler.execute(context);

            assertEquals(1, lore.size());
            assertEquals(line1, lore.get(0));
            verify(itemMeta).lore(lore);
            verify(mocks.player()).updateInventory();
        }

        @Test
        @DisplayName("Given player with lore and index at last position, when remove handler executes, then last lore line is removed")
        void removeHandler_removesLastLine_whenIndexIsLastPosition() {
            CommandExecutionHandler<CommandSourceStack> removeHandler = capturedHandlers.get(2);
            ItemMeta itemMeta = mock(ItemMeta.class);
            Component line0 = Component.text("line0");
            Component line1 = Component.text("line1");
            List<Component> lore = new ArrayList<>(List.of(line0, line1));
            when(itemMeta.lore()).thenReturn(lore);

            PlayerItemMocks mocks = createPlayerWithItem(itemMeta);
            CommandContext<CommandSourceStack> context = createPlayerContext(mocks.player());

            CloudKey<Integer> indexKey = CloudKey.of("index", Integer.class);
            when(context.getOrDefault(indexKey, 0)).thenReturn(1);

            removeHandler.execute(context);

            assertEquals(1, lore.size());
            assertEquals(line0, lore.get(0));
            verify(itemMeta).lore(lore);
            verify(mocks.player()).updateInventory();
        }

        @Test
        @DisplayName("Given player with single lore line, when remove handler executes, then lore becomes empty")
        void removeHandler_emptiesLore_whenSingleElement() {
            CommandExecutionHandler<CommandSourceStack> removeHandler = capturedHandlers.get(2);
            ItemMeta itemMeta = mock(ItemMeta.class);
            List<Component> lore = new ArrayList<>(List.of(Component.text("only")));
            when(itemMeta.lore()).thenReturn(lore);

            PlayerItemMocks mocks = createPlayerWithItem(itemMeta);
            CommandContext<CommandSourceStack> context = createPlayerContext(mocks.player());

            CloudKey<Integer> indexKey = CloudKey.of("index", Integer.class);
            when(context.getOrDefault(indexKey, 0)).thenReturn(0);

            removeHandler.execute(context);

            assertTrue(lore.isEmpty());
            verify(itemMeta).lore(lore);
            verify(mocks.player()).updateInventory();
        }

        @Test
        @DisplayName("Given player with lore and negative index, when remove handler executes, then no modification occurs")
        void removeHandler_doesNotModify_whenIndexIsNegative() {
            CommandExecutionHandler<CommandSourceStack> removeHandler = capturedHandlers.get(2);
            ItemMeta itemMeta = mock(ItemMeta.class);
            List<Component> lore = new ArrayList<>(List.of(Component.text("line")));
            when(itemMeta.lore()).thenReturn(lore);

            PlayerItemMocks mocks = createPlayerWithItem(itemMeta);
            CommandContext<CommandSourceStack> context = createPlayerContext(mocks.player());

            CloudKey<Integer> indexKey = CloudKey.of("index", Integer.class);
            when(context.getOrDefault(indexKey, 0)).thenReturn(-1);

            removeHandler.execute(context);

            assertEquals(1, lore.size());
            verify(itemMeta, never()).lore(any());
            verify(mocks.player(), never()).updateInventory();
        }

        @Test
        @DisplayName("Given player with lore and out-of-bounds index, when remove handler executes, then no modification occurs")
        void removeHandler_doesNotModify_whenIndexIsOutOfBounds() {
            CommandExecutionHandler<CommandSourceStack> removeHandler = capturedHandlers.get(2);
            ItemMeta itemMeta = mock(ItemMeta.class);
            List<Component> lore = new ArrayList<>(List.of(Component.text("line")));
            when(itemMeta.lore()).thenReturn(lore);

            PlayerItemMocks mocks = createPlayerWithItem(itemMeta);
            CommandContext<CommandSourceStack> context = createPlayerContext(mocks.player());

            CloudKey<Integer> indexKey = CloudKey.of("index", Integer.class);
            when(context.getOrDefault(indexKey, 0)).thenReturn(5);

            removeHandler.execute(context);

            assertEquals(1, lore.size());
            verify(itemMeta, never()).lore(any());
            verify(mocks.player(), never()).updateInventory();
        }

        @Test
        @DisplayName("Given player with null lore, when remove handler executes, then no modification occurs")
        void removeHandler_doesNotModify_whenLoreIsNull() {
            CommandExecutionHandler<CommandSourceStack> removeHandler = capturedHandlers.get(2);
            ItemMeta itemMeta = mock(ItemMeta.class);
            when(itemMeta.lore()).thenReturn(null);

            PlayerItemMocks mocks = createPlayerWithItem(itemMeta);
            CommandContext<CommandSourceStack> context = createPlayerContext(mocks.player());

            CloudKey<Integer> indexKey = CloudKey.of("index", Integer.class);
            when(context.getOrDefault(indexKey, 0)).thenReturn(0);

            removeHandler.execute(context);

            verify(itemMeta, never()).lore(any());
            verify(mocks.player(), never()).updateInventory();
        }

        @Test
        @DisplayName("Given non-player sender, when remove handler executes, then no action is taken")
        void removeHandler_doesNothing_whenSenderIsNotPlayer() {
            CommandExecutionHandler<CommandSourceStack> removeHandler = capturedHandlers.get(2);

            ConsoleCommandSender consoleSender = mock(ConsoleCommandSender.class);
            @SuppressWarnings("unchecked")
            CommandContext<CommandSourceStack> context = mock(CommandContext.class);
            CommandSourceStack sourceStack = mock(CommandSourceStack.class);
            when(context.sender()).thenReturn(sourceStack);
            when(sourceStack.getSender()).thenReturn(consoleSender);

            removeHandler.execute(context);

            verifyNoInteractions(consoleSender);
        }
    }
}

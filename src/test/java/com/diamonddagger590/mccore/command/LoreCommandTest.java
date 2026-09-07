package com.diamonddagger590.mccore.command;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class LoreCommandTest {

    @Mock
    private CorePlugin mockPlugin;

    @Mock
    private CoreCommandManager mockCoreCommandManager;

    @Mock
    private PaperCommandManager<CommandSourceStack> mockCommandManager;

    @Mock
    private Command.Builder<CommandSourceStack> mockBuilder;

    private MockedStatic<CorePlugin> corePluginStatic;

    private final List<CommandExecutionHandler<CommandSourceStack>> capturedHandlers = new ArrayList<>();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        RegistryResetExtension.setupRegistry();
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockCoreCommandManager);

        corePluginStatic = mockStatic(CorePlugin.class);
        corePluginStatic.when(CorePlugin::getInstance).thenReturn(mockPlugin);
        lenient().when(mockPlugin.registryAccess()).thenReturn(RegistryAccess.registryAccess());
        lenient().when(mockPlugin.getMiniMessage()).thenReturn(MiniMessage.miniMessage());
        lenient().when(mockCoreCommandManager.getCommandManager()).thenReturn(mockCommandManager);

        lenient().when(mockCommandManager.commandBuilder(anyString())).thenReturn(mockBuilder);
        lenient().when(mockBuilder.literal(anyString())).thenReturn(mockBuilder);
        lenient().when(mockBuilder.literal(anyString(), any(String[].class))).thenReturn(mockBuilder);
        lenient().when(mockBuilder.required(anyString(), any(ParserDescriptor.class))).thenReturn(mockBuilder);
        lenient().when(mockBuilder.optional(anyString(), any(ParserDescriptor.class))).thenReturn(mockBuilder);

        capturedHandlers.clear();
        lenient().when(mockBuilder.handler(any(CommandExecutionHandler.class))).thenAnswer(invocation -> {
            capturedHandlers.add(invocation.getArgument(0));
            return mockBuilder;
        });
        lenient().when(mockCommandManager.command(any(Command.Builder.class))).thenReturn(mockCommandManager);
    }

    @AfterEach
    void tearDown() {
        corePluginStatic.close();
        RegistryResetExtension.resetRegistry();
    }

    @Test
    @DisplayName("Given a valid setup, when registerCommand is called, then three subcommands are registered")
    void registerCommand_registersThreeSubcommands() {
        LoreCommand.registerCommand();

        verify(mockCommandManager, times(3)).command(any(Command.Builder.class));
        assertEquals(3, capturedHandlers.size());
    }

    private CommandContext<CommandSourceStack> createMockContext(CommandSender sender) {
        CommandContext<CommandSourceStack> context = mock(CommandContext.class);
        CommandSourceStack sourceStack = mock(CommandSourceStack.class);
        when(context.sender()).thenReturn(sourceStack);
        when(sourceStack.getSender()).thenReturn(sender);
        return context;
    }

    private void registerAndGetHandlers() {
        LoreCommand.registerCommand();
    }

    @Nested
    @DisplayName("Clear subcommand handler")
    class ClearHandler {

        @Test
        @DisplayName("Given a non-Player sender, when clear handler executes, then no inventory interaction occurs")
        void execute_noOp_whenSenderIsNotPlayer() {
            registerAndGetHandlers();
            CommandExecutionHandler<CommandSourceStack> clearHandler = capturedHandlers.get(0);

            CommandSender nonPlayer = mock(CommandSender.class);
            CommandContext<CommandSourceStack> context = createMockContext(nonPlayer);

            assertDoesNotThrow(() -> clearHandler.execute(context));
            verifyNoInteractions(nonPlayer);
        }

        @Test
        @DisplayName("Given a Player holding an item with lore, when clear handler executes, then lore is cleared")
        void execute_clearsLore_whenPlayerHoldsItemWithLore() {
            registerAndGetHandlers();
            CommandExecutionHandler<CommandSourceStack> clearHandler = capturedHandlers.get(0);

            Player player = mock(Player.class);
            PlayerInventory inventory = mock(PlayerInventory.class);
            ItemStack itemStack = mock(ItemStack.class);
            ItemMeta itemMeta = mock(ItemMeta.class);
            List<Component> lore = new ArrayList<>(List.of(Component.text("Line 1"), Component.text("Line 2")));

            when(player.getInventory()).thenReturn(inventory);
            when(inventory.getItemInMainHand()).thenReturn(itemStack);
            when(itemStack.getItemMeta()).thenReturn(itemMeta);
            when(itemMeta.lore()).thenReturn(lore);

            CommandContext<CommandSourceStack> context = createMockContext(player);

            clearHandler.execute(context);

            assertTrue(lore.isEmpty());
            verify(itemMeta).lore(lore);
            verify(itemStack).setItemMeta(itemMeta);
            verify(inventory).setItemInMainHand(itemStack);
            verify(player).updateInventory();
        }

        @Test
        @DisplayName("Given a Player holding an item with null lore, when clear handler executes, then no modification occurs")
        void execute_noOp_whenLoreIsNull() {
            registerAndGetHandlers();
            CommandExecutionHandler<CommandSourceStack> clearHandler = capturedHandlers.get(0);

            Player player = mock(Player.class);
            PlayerInventory inventory = mock(PlayerInventory.class);
            ItemStack itemStack = mock(ItemStack.class);
            ItemMeta itemMeta = mock(ItemMeta.class);

            when(player.getInventory()).thenReturn(inventory);
            when(inventory.getItemInMainHand()).thenReturn(itemStack);
            when(itemStack.getItemMeta()).thenReturn(itemMeta);
            when(itemMeta.lore()).thenReturn(null);

            CommandContext<CommandSourceStack> context = createMockContext(player);

            clearHandler.execute(context);

            verify(itemMeta, never()).lore(any());
            verify(itemStack, never()).setItemMeta(any());
        }
    }

    @Nested
    @DisplayName("Add subcommand handler")
    class AddHandler {

        @Test
        @DisplayName("Given a non-Player sender, when add handler executes, then no inventory interaction occurs")
        void execute_noOp_whenSenderIsNotPlayer() {
            registerAndGetHandlers();
            CommandExecutionHandler<CommandSourceStack> addHandler = capturedHandlers.get(1);

            CommandSender nonPlayer = mock(CommandSender.class);
            CommandContext<CommandSourceStack> context = createMockContext(nonPlayer);

            assertDoesNotThrow(() -> addHandler.execute(context));
            verifyNoInteractions(nonPlayer);
        }

        @Test
        @DisplayName("Given a Player and no index, when add handler executes, then lore line is appended")
        void execute_appendsLoreLine_whenNoIndexProvided() {
            registerAndGetHandlers();
            CommandExecutionHandler<CommandSourceStack> addHandler = capturedHandlers.get(1);

            Player player = mock(Player.class);
            PlayerInventory inventory = mock(PlayerInventory.class);
            ItemStack itemStack = mock(ItemStack.class);
            ItemMeta itemMeta = mock(ItemMeta.class);
            List<Component> lore = new ArrayList<>(List.of(Component.text("Existing")));

            when(player.getInventory()).thenReturn(inventory);
            when(inventory.getItemInMainHand()).thenReturn(itemStack);
            when(itemStack.getItemMeta()).thenReturn(itemMeta);
            when(itemMeta.lore()).thenReturn(lore);

            CloudKey<String> loreKey = CloudKey.of("lore", String.class);
            CloudKey<Integer> indexKey = CloudKey.of("index", Integer.class);

            CommandContext<CommandSourceStack> context = createMockContext(player);
            when(context.get(loreKey)).thenReturn("<green>New Line");
            when(context.getOrDefault(indexKey, 0)).thenReturn(0);
            when(context.contains(indexKey)).thenReturn(false);

            addHandler.execute(context);

            assertEquals(2, lore.size());
            verify(itemMeta).lore(lore);
            verify(itemStack).setItemMeta(itemMeta);
            verify(inventory).setItemInMainHand(itemStack);
            verify(player).updateInventory();
        }

        @Test
        @DisplayName("Given a Player and an index, when add handler executes, then lore line is inserted at index")
        void execute_insertsLoreLineAtIndex_whenIndexProvided() {
            registerAndGetHandlers();
            CommandExecutionHandler<CommandSourceStack> addHandler = capturedHandlers.get(1);

            Player player = mock(Player.class);
            PlayerInventory inventory = mock(PlayerInventory.class);
            ItemStack itemStack = mock(ItemStack.class);
            ItemMeta itemMeta = mock(ItemMeta.class);
            List<Component> lore = new ArrayList<>(List.of(Component.text("First"), Component.text("Third")));

            when(player.getInventory()).thenReturn(inventory);
            when(inventory.getItemInMainHand()).thenReturn(itemStack);
            when(itemStack.getItemMeta()).thenReturn(itemMeta);
            when(itemMeta.lore()).thenReturn(lore);

            CloudKey<String> loreKey = CloudKey.of("lore", String.class);
            CloudKey<Integer> indexKey = CloudKey.of("index", Integer.class);

            CommandContext<CommandSourceStack> context = createMockContext(player);
            when(context.get(loreKey)).thenReturn("Second");
            when(context.getOrDefault(indexKey, 0)).thenReturn(1);
            when(context.contains(indexKey)).thenReturn(true);

            addHandler.execute(context);

            assertEquals(3, lore.size());
            verify(itemMeta).lore(lore);
            verify(itemStack).setItemMeta(itemMeta);
        }

        @Test
        @DisplayName("Given a Player holding an item with null lore, when add handler executes, then no modification occurs")
        void execute_noOp_whenLoreIsNull() {
            registerAndGetHandlers();
            CommandExecutionHandler<CommandSourceStack> addHandler = capturedHandlers.get(1);

            Player player = mock(Player.class);
            PlayerInventory inventory = mock(PlayerInventory.class);
            ItemStack itemStack = mock(ItemStack.class);
            ItemMeta itemMeta = mock(ItemMeta.class);

            when(player.getInventory()).thenReturn(inventory);
            when(inventory.getItemInMainHand()).thenReturn(itemStack);
            when(itemStack.getItemMeta()).thenReturn(itemMeta);
            when(itemMeta.lore()).thenReturn(null);

            CloudKey<String> loreKey = CloudKey.of("lore", String.class);
            CloudKey<Integer> indexKey = CloudKey.of("index", Integer.class);

            CommandContext<CommandSourceStack> context = createMockContext(player);
            when(context.get(loreKey)).thenReturn("Test");
            when(context.getOrDefault(indexKey, 0)).thenReturn(0);
            when(context.contains(indexKey)).thenReturn(false);

            addHandler.execute(context);

            verify(itemMeta, never()).lore(any());
            verify(itemStack, never()).setItemMeta(any());
        }

        @Test
        @DisplayName("Given a Player and an out-of-bounds index, when add handler executes, then IndexOutOfBoundsException is thrown")
        void execute_throws_whenIndexIsOutOfBounds() {
            registerAndGetHandlers();
            CommandExecutionHandler<CommandSourceStack> addHandler = capturedHandlers.get(1);

            Player player = mock(Player.class);
            PlayerInventory inventory = mock(PlayerInventory.class);
            ItemStack itemStack = mock(ItemStack.class);
            ItemMeta itemMeta = mock(ItemMeta.class);
            List<Component> lore = new ArrayList<>(List.of(Component.text("Only line")));

            when(player.getInventory()).thenReturn(inventory);
            when(inventory.getItemInMainHand()).thenReturn(itemStack);
            when(itemStack.getItemMeta()).thenReturn(itemMeta);
            when(itemMeta.lore()).thenReturn(lore);

            CloudKey<String> loreKey = CloudKey.of("lore", String.class);
            CloudKey<Integer> indexKey = CloudKey.of("index", Integer.class);

            CommandContext<CommandSourceStack> context = createMockContext(player);
            when(context.get(loreKey)).thenReturn("New Line");
            when(context.getOrDefault(indexKey, 0)).thenReturn(10);
            when(context.contains(indexKey)).thenReturn(true);

            assertThrows(IndexOutOfBoundsException.class, () -> addHandler.execute(context));
        }
    }

    @Nested
    @DisplayName("Remove subcommand handler")
    class RemoveHandler {

        @Test
        @DisplayName("Given a non-Player sender, when remove handler executes, then no inventory interaction occurs")
        void execute_noOp_whenSenderIsNotPlayer() {
            registerAndGetHandlers();
            CommandExecutionHandler<CommandSourceStack> removeHandler = capturedHandlers.get(2);

            CommandSender nonPlayer = mock(CommandSender.class);
            CommandContext<CommandSourceStack> context = createMockContext(nonPlayer);

            assertDoesNotThrow(() -> removeHandler.execute(context));
            verifyNoInteractions(nonPlayer);
        }

        @Test
        @DisplayName("Given a Player and valid index, when remove handler executes, then lore line is removed")
        void execute_removesLoreLine_whenIndexIsValid() {
            registerAndGetHandlers();
            CommandExecutionHandler<CommandSourceStack> removeHandler = capturedHandlers.get(2);

            Player player = mock(Player.class);
            PlayerInventory inventory = mock(PlayerInventory.class);
            ItemStack itemStack = mock(ItemStack.class);
            ItemMeta itemMeta = mock(ItemMeta.class);
            List<Component> lore = new ArrayList<>(List.of(Component.text("Line 0"), Component.text("Line 1")));

            when(player.getInventory()).thenReturn(inventory);
            when(inventory.getItemInMainHand()).thenReturn(itemStack);
            when(itemStack.getItemMeta()).thenReturn(itemMeta);
            when(itemMeta.lore()).thenReturn(lore);

            CloudKey<Integer> indexKey = CloudKey.of("index", Integer.class);

            CommandContext<CommandSourceStack> context = createMockContext(player);
            when(context.getOrDefault(indexKey, 0)).thenReturn(0);

            removeHandler.execute(context);

            assertEquals(1, lore.size());
            assertEquals(Component.text("Line 1"), lore.get(0));
            verify(itemMeta).lore(lore);
            verify(itemStack).setItemMeta(itemMeta);
            verify(inventory).setItemInMainHand(itemStack);
            verify(player).updateInventory();
        }

        @Test
        @DisplayName("Given a Player and out-of-bounds index, when remove handler executes, then no modification occurs")
        void execute_noOp_whenIndexIsOutOfBounds() {
            registerAndGetHandlers();
            CommandExecutionHandler<CommandSourceStack> removeHandler = capturedHandlers.get(2);

            Player player = mock(Player.class);
            PlayerInventory inventory = mock(PlayerInventory.class);
            ItemStack itemStack = mock(ItemStack.class);
            ItemMeta itemMeta = mock(ItemMeta.class);
            List<Component> lore = new ArrayList<>(List.of(Component.text("Only line")));

            when(player.getInventory()).thenReturn(inventory);
            when(inventory.getItemInMainHand()).thenReturn(itemStack);
            when(itemStack.getItemMeta()).thenReturn(itemMeta);
            when(itemMeta.lore()).thenReturn(lore);

            CloudKey<Integer> indexKey = CloudKey.of("index", Integer.class);

            CommandContext<CommandSourceStack> context = createMockContext(player);
            when(context.getOrDefault(indexKey, 0)).thenReturn(5);

            removeHandler.execute(context);

            assertEquals(1, lore.size());
            verify(itemMeta, never()).lore(any());
            verify(itemStack, never()).setItemMeta(any());
        }

        @Test
        @DisplayName("Given a Player and negative index, when remove handler executes, then no modification occurs")
        void execute_noOp_whenIndexIsNegative() {
            registerAndGetHandlers();
            CommandExecutionHandler<CommandSourceStack> removeHandler = capturedHandlers.get(2);

            Player player = mock(Player.class);
            PlayerInventory inventory = mock(PlayerInventory.class);
            ItemStack itemStack = mock(ItemStack.class);
            ItemMeta itemMeta = mock(ItemMeta.class);
            List<Component> lore = new ArrayList<>(List.of(Component.text("Line")));

            when(player.getInventory()).thenReturn(inventory);
            when(inventory.getItemInMainHand()).thenReturn(itemStack);
            when(itemStack.getItemMeta()).thenReturn(itemMeta);
            when(itemMeta.lore()).thenReturn(lore);

            CloudKey<Integer> indexKey = CloudKey.of("index", Integer.class);

            CommandContext<CommandSourceStack> context = createMockContext(player);
            when(context.getOrDefault(indexKey, 0)).thenReturn(-1);

            removeHandler.execute(context);

            assertEquals(1, lore.size());
            verify(itemMeta, never()).lore(any());
            verify(itemStack, never()).setItemMeta(any());
        }

        @Test
        @DisplayName("Given a Player holding an item with null lore, when remove handler executes, then no modification occurs")
        void execute_noOp_whenLoreIsNull() {
            registerAndGetHandlers();
            CommandExecutionHandler<CommandSourceStack> removeHandler = capturedHandlers.get(2);

            Player player = mock(Player.class);
            PlayerInventory inventory = mock(PlayerInventory.class);
            ItemStack itemStack = mock(ItemStack.class);
            ItemMeta itemMeta = mock(ItemMeta.class);

            when(player.getInventory()).thenReturn(inventory);
            when(inventory.getItemInMainHand()).thenReturn(itemStack);
            when(itemStack.getItemMeta()).thenReturn(itemMeta);
            when(itemMeta.lore()).thenReturn(null);

            CloudKey<Integer> indexKey = CloudKey.of("index", Integer.class);

            CommandContext<CommandSourceStack> context = createMockContext(player);
            when(context.getOrDefault(indexKey, 0)).thenReturn(0);

            removeHandler.execute(context);

            verify(itemMeta, never()).lore(any());
            verify(itemStack, never()).setItemMeta(any());
        }

        @Test
        @DisplayName("Given a Player holding an item with empty lore, when remove handler executes, then no modification occurs")
        void execute_noOp_whenLoreIsEmpty() {
            registerAndGetHandlers();
            CommandExecutionHandler<CommandSourceStack> removeHandler = capturedHandlers.get(2);

            Player player = mock(Player.class);
            PlayerInventory inventory = mock(PlayerInventory.class);
            ItemStack itemStack = mock(ItemStack.class);
            ItemMeta itemMeta = mock(ItemMeta.class);
            List<Component> lore = new ArrayList<>();

            when(player.getInventory()).thenReturn(inventory);
            when(inventory.getItemInMainHand()).thenReturn(itemStack);
            when(itemStack.getItemMeta()).thenReturn(itemMeta);
            when(itemMeta.lore()).thenReturn(lore);

            CloudKey<Integer> indexKey = CloudKey.of("index", Integer.class);

            CommandContext<CommandSourceStack> context = createMockContext(player);
            when(context.getOrDefault(indexKey, 0)).thenReturn(0);

            removeHandler.execute(context);

            assertTrue(lore.isEmpty());
            verify(itemMeta, never()).lore(any());
            verify(itemStack, never()).setItemMeta(any());
        }
    }
}

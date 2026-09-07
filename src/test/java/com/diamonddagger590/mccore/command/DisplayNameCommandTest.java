package com.diamonddagger590.mccore.command;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import io.papermc.paper.command.brigadier.CommandSourceStack;
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
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
class DisplayNameCommandTest {

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
    @DisplayName("Given a valid setup, when registerCommand is called, then one command is registered")
    void registerCommand_registersOneCommand() {
        DisplayNameCommand.registerCommand();

        verify(mockCommandManager, times(1)).command(any(Command.Builder.class));
        assertEquals(1, capturedHandlers.size());
    }

    @Test
    @DisplayName("Given a non-Player sender, when handler executes, then no inventory interaction occurs")
    void execute_noOp_whenSenderIsNotPlayer() {
        DisplayNameCommand.registerCommand();
        CommandExecutionHandler<CommandSourceStack> handler = capturedHandlers.get(0);

        CommandSender nonPlayer = mock(CommandSender.class);
        CommandContext<CommandSourceStack> context = mock(CommandContext.class);
        CommandSourceStack sourceStack = mock(CommandSourceStack.class);
        when(context.sender()).thenReturn(sourceStack);
        when(sourceStack.getSender()).thenReturn(nonPlayer);

        CloudKey<String> nameKey = CloudKey.of("name", String.class);
        when(context.get("name")).thenReturn(nameKey);
        when(context.get(nameKey)).thenReturn("test");

        assertDoesNotThrow(() -> handler.execute(context));
        verifyNoInteractions(nonPlayer);
    }

    @Test
    @DisplayName("Given a Player sender, when handler executes, then display name is set on main hand item")
    void execute_setsDisplayName_whenSenderIsPlayer() {
        DisplayNameCommand.registerCommand();
        CommandExecutionHandler<CommandSourceStack> handler = capturedHandlers.get(0);

        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        ItemStack itemStack = mock(ItemStack.class);
        ItemMeta itemMeta = mock(ItemMeta.class);

        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getItemInMainHand()).thenReturn(itemStack);
        when(itemStack.getItemMeta()).thenReturn(itemMeta);

        CommandContext<CommandSourceStack> context = mock(CommandContext.class);
        CommandSourceStack sourceStack = mock(CommandSourceStack.class);
        when(context.sender()).thenReturn(sourceStack);
        when(sourceStack.getSender()).thenReturn(player);

        CloudKey<String> nameKey = CloudKey.of("name", String.class);
        when(context.get("name")).thenReturn(nameKey);
        when(context.get(nameKey)).thenReturn("<red>My Item");

        handler.execute(context);

        verify(itemMeta).displayName(any());
        verify(itemStack).setItemMeta(itemMeta);
        verify(inventory).setItemInMainHand(itemStack);
        verify(player).updateInventory();
    }

    @Test
    @DisplayName("Given a Player sender with MiniMessage input, when handler executes, then MiniMessage is deserialized")
    void execute_deserializesMiniMessage_whenNameContainsTags() {
        DisplayNameCommand.registerCommand();
        CommandExecutionHandler<CommandSourceStack> handler = capturedHandlers.get(0);

        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        ItemStack itemStack = mock(ItemStack.class);
        ItemMeta itemMeta = mock(ItemMeta.class);

        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getItemInMainHand()).thenReturn(itemStack);
        when(itemStack.getItemMeta()).thenReturn(itemMeta);

        CommandContext<CommandSourceStack> context = mock(CommandContext.class);
        CommandSourceStack sourceStack = mock(CommandSourceStack.class);
        when(context.sender()).thenReturn(sourceStack);
        when(sourceStack.getSender()).thenReturn(player);

        String input = "<bold>Fancy Name";
        CloudKey<String> nameKey = CloudKey.of("name", String.class);
        when(context.get("name")).thenReturn(nameKey);
        when(context.get(nameKey)).thenReturn(input);

        handler.execute(context);

        MiniMessage miniMessage = MiniMessage.miniMessage();
        verify(itemMeta).displayName(miniMessage.deserialize(input));
    }
}

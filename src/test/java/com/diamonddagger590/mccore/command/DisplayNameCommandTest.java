package com.diamonddagger590.mccore.command;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.testing.RegistryResetExtension;
import io.papermc.paper.command.brigadier.CommandSourceStack;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisplayNameCommandTest {

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
        when(mockBuilder.required(anyString(), any(ParserDescriptor.class))).thenReturn(mockBuilder);

        capturedHandlers = new ArrayList<>();
        when(mockBuilder.handler(any())).thenAnswer(invocation -> {
            capturedHandlers.add(invocation.getArgument(0));
            return mockBuilder;
        });

        DisplayNameCommand.registerCommand();
    }

    @AfterEach
    void tearDown() {
        corePluginStatic.close();
        RegistryResetExtension.resetRegistry();
    }

    @Nested
    @DisplayName("registerCommand")
    class RegisterCommand {

        @Test
        @DisplayName("Given valid command manager, when registerCommand is called, then one handler is registered")
        void registerCommand_registersOneHandler_whenCalled() {
            assertEquals(1, capturedHandlers.size());
        }
    }

    @Nested
    @DisplayName("name handler")
    class NameHandler {

        @Test
        @DisplayName("Given player holding item, when name handler executes, then display name is set via MiniMessage")
        void nameHandler_setsDisplayName_whenPlayerHoldsItem() {
            CommandExecutionHandler<CommandSourceStack> nameHandler = capturedHandlers.get(0);

            Player player = mock(Player.class);
            PlayerInventory inventory = mock(PlayerInventory.class);
            ItemStack itemStack = mock(ItemStack.class);
            ItemMeta itemMeta = mock(ItemMeta.class);
            when(player.getInventory()).thenReturn(inventory);
            when(inventory.getItemInMainHand()).thenReturn(itemStack);
            when(itemStack.getItemMeta()).thenReturn(itemMeta);

            @SuppressWarnings("unchecked")
            CommandContext<CommandSourceStack> context = mock(CommandContext.class);
            CommandSourceStack sourceStack = mock(CommandSourceStack.class);
            when(context.sender()).thenReturn(sourceStack);
            when(sourceStack.getSender()).thenReturn(player);

            // DisplayNameCommand calls context.get("name") which returns a value assigned to CloudKey<String>.
            // Due to type erasure, we return a CloudKey so the cast succeeds at runtime.
            CloudKey<String> nameKey = CloudKey.of("name", String.class);
            when(context.get("name")).thenReturn(nameKey);
            when(context.get(nameKey)).thenReturn("Test Name");

            nameHandler.execute(context);

            verify(itemMeta).displayName(any());
            verify(itemStack).setItemMeta(itemMeta);
            verify(inventory).setItemInMainHand(itemStack);
            verify(player).updateInventory();
        }

        @Test
        @DisplayName("Given non-player sender, when name handler executes, then no action is taken")
        void nameHandler_doesNothing_whenSenderIsNotPlayer() {
            CommandExecutionHandler<CommandSourceStack> nameHandler = capturedHandlers.get(0);

            @SuppressWarnings("unchecked")
            CommandContext<CommandSourceStack> context = mock(CommandContext.class);
            CommandSourceStack sourceStack = mock(CommandSourceStack.class);
            when(context.sender()).thenReturn(sourceStack);
            when(sourceStack.getSender()).thenReturn(mock(ConsoleCommandSender.class));

            // context.get("name") is called before the instanceof check
            CloudKey<String> nameKey = CloudKey.of("name", String.class);
            when(context.get("name")).thenReturn(nameKey);
            when(context.get(nameKey)).thenReturn("Test Name");

            nameHandler.execute(context);
        }
    }
}

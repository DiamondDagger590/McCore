package com.diamonddagger590.mccore.command;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.parser.standard.StringParser;

public class DisplayNameCommand {

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = CorePlugin.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        MiniMessage miniMessage = CorePlugin.getInstance().getMiniMessage();
        commandManager.command(commandManager.commandBuilder("name")
                .required("name", StringParser.stringParser())
                .handler(commandContext -> {
                            CommandSender commandSender = commandContext.sender().getSender();
                            CloudKey<String> nameKey = commandContext.get("name");
                            String name = commandContext.get(nameKey);
                            if (commandSender instanceof Player player) {
                                ItemStack itemStack = player.getInventory().getItemInMainHand();
                                ItemMeta itemMeta = itemStack.getItemMeta();
                                itemMeta.displayName(miniMessage.deserialize(name));
                                itemStack.setItemMeta(itemMeta);
                                player.getInventory().setItemInMainHand(itemStack);
                                player.updateInventory();
                            }
                        }
                ));
    }
}

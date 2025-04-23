package com.diamonddagger590.mccore.command;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.List;

public class LoreCommand {

    public static void registerCommand() {
        CommandManager<CommandSourceStack> commandManager = CorePlugin.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        MiniMessage miniMessage = CorePlugin.getInstance().getMiniMessage();
        commandManager.command(commandManager.commandBuilder("lore")
                .literal("clear")
                .handler(commandContext -> {
                            CommandSender commandSender = commandContext.sender().getSender();
                            if (commandSender instanceof Player player) {
                                ItemStack itemStack = player.getInventory().getItemInMainHand();
                                ItemMeta itemMeta = itemStack.getItemMeta();
                                List<Component> lore = itemMeta.lore();
                                if (lore != null) {
                                    lore.clear();
                                    itemMeta.lore(lore);
                                    itemStack.setItemMeta(itemMeta);
                                    player.getInventory().setItemInMainHand(itemStack);
                                    player.updateInventory();
                                }
                            }
                        }
                ));

        commandManager.command(commandManager.commandBuilder("lore")
                .literal("add")
                .required("lore", StringParser.stringParser())
                .optional("index", IntegerParser.integerParser())
                .handler(commandContext -> {
                            CommandSender commandSender = commandContext.sender().getSender();
                            if (commandSender instanceof Player player) {
                                CloudKey<String> loreKey = CloudKey.of("lore", String.class);
                                String loreLine = commandContext.get(loreKey);
                                CloudKey<Integer> indexKey = CloudKey.of("index", Integer.class);
                                int index = commandContext.getOrDefault(indexKey, 0);
                                boolean useIndex = commandContext.contains(indexKey);

                                ItemStack itemStack = player.getInventory().getItemInMainHand();
                                ItemMeta itemMeta = itemStack.getItemMeta();
                                List<Component> lore = itemMeta.lore();
                                if (lore != null) {
                                    if (useIndex) {
                                        lore.add(index, miniMessage.deserialize(loreLine));
                                    }
                                    else {
                                        lore.add(miniMessage.deserialize(loreLine));
                                    }
                                    itemMeta.lore(lore);
                                    itemStack.setItemMeta(itemMeta);
                                    player.getInventory().setItemInMainHand(itemStack);
                                    player.updateInventory();
                                }
                            }
                        }
                ));

        commandManager.command(commandManager.commandBuilder("lore")
                .literal("remove")
                .optional("index", IntegerParser.integerParser())
                .handler(commandContext -> {
                            CommandSender commandSender = commandContext.sender().getSender();
                            if (commandSender instanceof Player player) {
                                CloudKey<Integer> indexKey = CloudKey.of("index", Integer.class);
                                int index = commandContext.getOrDefault(indexKey, 0);

                                ItemStack itemStack = player.getInventory().getItemInMainHand();
                                ItemMeta itemMeta = itemStack.getItemMeta();
                                List<Component> lore = itemMeta.lore();
                                if (lore != null && index >= 0 && index < lore.size()) {
                                    lore.remove(index);
                                    itemMeta.lore(lore);
                                    itemStack.setItemMeta(itemMeta);
                                    player.getInventory().setItemInMainHand(itemStack);
                                    player.updateInventory();
                                }
                            }
                        }
                ));
    }
}

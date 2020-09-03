package com.rteenagers.parrot;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SheetsFetcherCommand implements CommandExecutor {
    private final Parrot plugin;

    public SheetsFetcherCommand(Parrot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.RED + " Hello " + sender.getName());
        return true;
    }
}

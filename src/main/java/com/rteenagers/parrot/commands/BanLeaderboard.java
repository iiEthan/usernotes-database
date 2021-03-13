package com.rteenagers.parrot.commands;

import com.rteenagers.parrot.DatabaseHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.List;

public class BanLeaderboard implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        DatabaseHandler.checkConnection(sender);

        if (!sender.hasPermission("points.banleaderboard")) {
            sender.sendMessage(ChatColor.RED + "You are not permitted to do this!");
            return true;
        }

        DatabaseHandler.banLeaderboard(sender);

        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // No subcommands, we do not want any tab completion
        return new ArrayList<>();
    }
}
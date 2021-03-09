package com.rteenagers.parrot.commands;

import com.rteenagers.parrot.DatabaseHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class PointsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Please provide a player to lookup");
        }

        String target = args[0];

        OfflinePlayer op = Bukkit.getOfflinePlayer(target); // Deprecated but should work without worry of it being removed, please replace if there's a better way :^)
        if (op.hasPlayedBefore()) {
            UUID uuid = op.getUniqueId();
            DatabaseHandler.getPlayer(uuid);
        } else {
            sender.sendMessage(ChatColor.RED + "Player not found. Please check spelling or if they have changed their username.");
        }

        return true;
    }
}

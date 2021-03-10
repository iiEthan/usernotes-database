package com.rteenagers.parrot.commands;

import com.rteenagers.parrot.DatabaseHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PointsCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("points.all")) {
            sender.sendMessage(ChatColor.RED + "You are not permitted to do this!");
            return true;
        }

        if (args.length == 0) { // User must provide an input
            sender.sendMessage(ChatColor.RED + "Please provide a player to lookup.");
            return true;
        }

        String target = args[0];

        @SuppressWarnings("deprecation")
        OfflinePlayer op = Bukkit.getOfflinePlayer(target); // Deprecated but should work without worry of it being removed, please replace if there's a better way :^)
            UUID uuid = op.getUniqueId();
            try {
                DatabaseHandler.getPoints(String.valueOf(uuid), target, sender);
            } catch (SQLException e) {
                e.printStackTrace();
            }
         return true;
        }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("points")) {
            if (args.length == 1) {
                ArrayList<String> arguments = new ArrayList<>();
                if (!args[0].equals("")) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                            arguments.add(p.getName());
                        }
                    }
                } else {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        arguments.add(p.getName());
                    }
                }
                Collections.sort(arguments);

                return arguments;
            }
            if (args.length > 1) {
                return new ArrayList<>();
            }
        }
        return null;
    }
}

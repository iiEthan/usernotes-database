package com.rteenagers.parrot.commands;

import com.rteenagers.parrot.DatabaseHandler;
import com.rteenagers.parrot.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PointLookup implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("points.lookup.all")) {
            sender.sendMessage(ChatColor.RED + "You are not permitted to do this!");
            return true;
        }

        if (args.length < 2) { // User must give an input
            sender.sendMessage(ChatColor.RED + "Please provide more arguments! Usage is /pointlookup [ban/mute/] [id]");
            return true;
        }

        if (!(args[0].equalsIgnoreCase("ban") || args[0].equalsIgnoreCase("mute"))) { // Check if punishment input is valid
            sender.sendMessage(ChatColor.RED + "Please provide a valid punishment to remove as the first argument. Usage is /removepoint [ban/mute] [id]");
            return true;
        }

        if (!Utils.isInteger(args[1])) { // Check if point is valid
            sender.sendMessage(ChatColor.RED + "Please provide a valid number as the ID argument.");
            return true;
        }

        try {
            DatabaseHandler.pointLookup(args[0], args[1], sender);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("pointlookup")) {

            ArrayList<String> arguments = new ArrayList<>();
            switch (args.length) {
                case 1:
                    String[] punishments = {"ban", "mute"};
                    for (String p : punishments) {
                        if (p.startsWith(args[0].toLowerCase())) {
                            arguments.add(p);
                        }
                    }
                    return arguments;
                case 2:
                    if (args[1].equals("")) {
                        arguments.add("noteid");
                    }
                    return arguments;
                default:
                    return new ArrayList<>();
            }
        }
        return null;
    }
}